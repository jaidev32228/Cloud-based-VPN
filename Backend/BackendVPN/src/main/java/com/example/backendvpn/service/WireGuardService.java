package com.example.backendvpn.service;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.common.IOUtils;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

@Service
public class WireGuardService {

    private static final Logger logger = Logger.getLogger(WireGuardService.class.getName());

    @Value("${ssh.username}")
    private String sshUsername;

    @Value("${ssh.pemFile:SecretKeyForVPN.pem}")
    private String pemFileName;

    public WireGuardResult configureWireGuard(String publicIp) {
        waitForSsh(publicIp);

        try (SSHClient client = new SSHClient()) {
            // Configure timeouts
            client.setTimeout(600_000); // 10 minutes
            client.getTransport().setTimeoutMs(600_000);
            
            client.addHostKeyVerifier(new PromiscuousVerifier());
            client.connect(publicIp, 22);

            File pem = loadPrivateKeyFromResource();
            client.authPublickey(sshUsername, pem.getAbsolutePath());

            // Package setup with retries
            executeCommand(client, "sudo apt-get update -y");
            executeCommand(client, "sudo apt-get install -y software-properties-common");
            executeCommand(client, "sudo add-apt-repository universe -y");
            executeCommand(client, "sudo apt-get update -y");

            // Install packages with individual error handling
            installPackage(client, "wireguard-tools");
            installPackage(client, "qrencode");
            executeCommand(client, "sudo DEBIAN_FRONTEND=noninteractive apt-get -y install iptables-persistent");

            // Generate keys in secure location
            executeCommand(client, "sudo mkdir -p /etc/wireguard/keys");
            executeCommand(client, "wg genkey | sudo tee /etc/wireguard/keys/server_private.key | wg pubkey | sudo tee /etc/wireguard/keys/server_public.key");
            executeCommand(client, "sudo sh -c 'wg genkey > /etc/wireguard/keys/client_private.key'");
            executeCommand(client, "sudo sh -c 'wg pubkey < /etc/wireguard/keys/client_private.key > /etc/wireguard/keys/client_public.key'");

            // Get default interface dynamically
            String interfaceCmd = "default_iface=$(ip route list default | awk '{print $5}'); " +
                "echo \"Detected interface: $default_iface\"; " +
                "sudo sh -c 'echo $default_iface > /tmp/wg_iface'";
            executeCommand(client, interfaceCmd);

            // Create server config with MASQUERADE rules
         // Revised WireGuard config creation
            String wgConfig = "sudo bash -c 'umask 077 && cat > /etc/wireguard/wg0.conf <<EOL\n" +
                "[Interface]\n" +
                "Address = 10.66.66.1/24\n" +
                "ListenPort = 51820\n" +
                "PrivateKey = $(sudo cat /etc/wireguard/keys/server_private.key)\n\n" +
                "MTU = 1420\n" +
                "PostUp = iptables -t nat -A POSTROUTING -o %i -j MASQUERADE --random-fully\n" +
                "PostDown = iptables -t nat -D POSTROUTING -o %i -j MASQUERADE --random-fully\n\n" +
                "[Peer]\n" +
                "PublicKey = $(sudo cat /etc/wireguard/keys/client_public.key)\n" +
                "AllowedIPs = 10.66.66.2/32\n" +
                "PersistentKeepalive = 21\n" +
                "EOL'";
            executeCommand(client, wgConfig);

            // Network configuration
            executeCommand(client, "sudo sysctl -w net.ipv4.ip_forward=1");
            executeCommand(client, "sudo bash -c 'echo \"net.ipv4.ip_forward=1\" >> /etc/sysctl.conf'");
            executeCommand(client, "sudo bash -c 'echo \"net.core.default_qdisc=fq\" >> /etc/sysctl.conf'");
            executeCommand(client, "sudo bash -c 'echo \"net.ipv4.tcp_congestion_control=bbr\" >> /etc/sysctl.conf'");
            executeCommand(client, "sudo sysctl -p");
            // Start WireGuard
            executeCommand(client, "sudo systemctl enable wg-quick@wg0");
            executeCommand(client, "sudo systemctl start wg-quick@wg0");

            // Create client config
            String clientConfCmd = "sudo bash -c 'cat > /home/ubuntu/client.conf <<EOL\n" +
                "[Interface]\n" +
                "PrivateKey = $(sudo cat /etc/wireguard/keys/client_private.key)\n" +
                "Address = 10.66.66.2/24\n" +
                "MTU = 1420\n" +
                "DNS = 1.1.1.1\n\n" +
                "[Peer]\n" +
                "PublicKey = $(sudo cat /etc/wireguard/keys/server_public.key)\n" +
                "Endpoint = " + publicIp + ":51820\n" +
                "AllowedIPs = 0.0.0.0/0\n" +
                "PersistentKeepalive = 21\n" +
                "EOL'";
            executeCommand(client, clientConfCmd);

            // Generate QR code
            executeCommand(client, "qrencode -t PNG -o /home/ubuntu/client.png -r /home/ubuntu/client.conf");

            // Retrieve config
            String clientConf = readFileFromRemote(client, "client.conf");
            String qrBase64 = readBase64FileFromRemote(client, "client.png");
            
            return new WireGuardResult(clientConf, qrBase64);

        } catch (Exception e) {
            logger.severe("WireGuard setup failed: " + e.getMessage());
            throw new RuntimeException("WireGuard setup failed", e);
        }
    }

    private void installPackage(SSHClient client, String pkg) throws IOException {
        try {
            executeCommand(client, "sudo apt-get install -y --no-install-recommends " + pkg);
        } catch (IOException e) {
            logger.warning("First install attempt failed for " + pkg + ", retrying...");
            executeCommand(client, "sudo apt-get update -y");
            executeCommand(client, "sudo apt-get install -y " + pkg);
        }
    }

    private void executeCommand(SSHClient client, String command) throws IOException {
        try (Session session = client.startSession()) {
            logger.info("Executing: " + command);
            Session.Command cmd = session.exec(command);
            
            String output = IOUtils.readFully(cmd.getInputStream()).toString();
            String error = IOUtils.readFully(cmd.getErrorStream()).toString();
            
            // Extended timeout for long operations
            cmd.join(10, TimeUnit.MINUTES);
            
            if (cmd.getExitStatus() != 0) {
                throw new IOException("Command failed (" + cmd.getExitStatus() + "): " + command 
                    + "\nError: " + error + "\nOutput: " + output);
            }
            
            logger.info("Output: " + output);
            if (!error.isEmpty()) {
                logger.warning("Error output: " + error);
            }
        }
    }

    private File loadPrivateKeyFromResource() throws IOException {
        File pem = File.createTempFile("vpn-key", ".pem");
        try (InputStream keyStream = getClass().getClassLoader().getResourceAsStream(pemFileName)) {
            if (keyStream == null) throw new FileNotFoundException(pemFileName + " not found in resources");
            Files.copy(keyStream, pem.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
        pem.setReadable(false, false);
        pem.setReadable(true, true);
        return pem;
    }

    private String readFileFromRemote(SSHClient client, String filename) throws IOException {
        try (Session session = client.startSession()) {
            Session.Command cmd = session.exec("cat /home/ubuntu/" + filename);
            String result = IOUtils.readFully(cmd.getInputStream()).toString();
            cmd.join(1, TimeUnit.MINUTES);
            return result;
        }
    }

    private String readBase64FileFromRemote(SSHClient client, String filename) throws IOException {
        try (Session session = client.startSession()) {
            Session.Command cmd = session.exec("base64 -w0 /home/ubuntu/" + filename);
            String encoded = IOUtils.readFully(cmd.getInputStream()).toString();
            cmd.join(1, TimeUnit.MINUTES);
            return encoded.trim();
        }
    }

    private void waitForSsh(String publicIp) {
        int retries = 30;
        while (retries-- > 0) {
            try (Socket socket = new Socket(publicIp, 22)) {
                logger.info("SSH connection established");
                return;
            } catch (IOException e) {
                logger.info("Waiting for SSH availability... (" + retries + " retries left)");
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException ignored) {}
            }
        }
        throw new RuntimeException("SSH connection to " + publicIp + " timed out");
    }


    public record WireGuardResult(String clientConfig, String qrCodeBase64) {}
}