package com.example.backendvpn.controller;

import com.example.backendvpn.service.Ec2Service;
import com.example.backendvpn.service.WireGuardService;
import com.example.backendvpn.service.WireGuardService.WireGuardResult;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/vpn")
@CrossOrigin
public class VpnController {

    private static final Logger log = LoggerFactory.getLogger(VpnController.class);

    private final Ec2Service ec2Service;
    private final WireGuardService wireGuardService;

    // In-memory store for user -> instanceId
    private final Map<String, String> activeInstances = new ConcurrentHashMap<>();

    public VpnController(Ec2Service ec2Service, WireGuardService wireGuardService) {
        this.ec2Service = ec2Service;
        this.wireGuardService = wireGuardService;
    }

    @PostMapping("/connect")
    public ResponseEntity<VpnConnectionResponse> connectToVpn(Authentication authentication) {
        String username = authentication.getName();
        String instanceId = null;
        try {
            instanceId = ec2Service.launchInstance();
            String publicIp = ec2Service.getInstancePublicIp(instanceId);

            // Save instance ID
            activeInstances.put(username, instanceId);

            WireGuardResult result = wireGuardService.configureWireGuard(publicIp);
            return ResponseEntity.ok(new VpnConnectionResponse(result.clientConfig(), result.qrCodeBase64(), "VPN ready"));

        } catch (Exception e) {
            log.error("VPN connection failed", e);
            if (instanceId != null) ec2Service.terminateInstance(instanceId);
            activeInstances.remove(username);
            return ResponseEntity.internalServerError().body(new VpnConnectionResponse(null, null, "Error: " + e.getMessage()));
        }
    }

    @PostMapping("/disconnect")
    public ResponseEntity<String> disconnectVpn(Authentication authentication) {
        String username = authentication.getName();
        String instanceId = activeInstances.get(username);

        if (instanceId == null) {
            return ResponseEntity.badRequest().body("No active VPN session found");
        }

        try {
            ec2Service.terminateInstance(instanceId);
            activeInstances.remove(username);
            return ResponseEntity.ok("VPN disconnected and EC2 instance terminated");
        } catch (Exception e) {
            log.error("Failed to disconnect VPN", e);
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }

    public static class VpnConnectionResponse {
        private final String config;
        private final String qrCode;
        private final String message;

        public VpnConnectionResponse(String config, String qrCode, String message) {
            this.config = config;
            this.qrCode = qrCode;
            this.message = message;
        }

        public String getConfig() {
            return config;
        }

        public String getQrCode() {
            return qrCode;
        }

        public String getMessage() {
            return message;
        }
    }
}
