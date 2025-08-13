import { Button } from "@/components/ui/button";
import { Card, CardHeader, CardTitle, CardContent } from "@/components/ui/card";
import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { connectToVpn, stopVpn, checkSubscription } from "@/services/api";
import { parseJwt } from "@/utils/jwt"; // ✅ Import parseJwt

type JwtPayload = {
  id: number;
  sub: string;
  role: string;
  exp: number;
};

export default function UserDashboard() {
  const [isSubscribed, setIsSubscribed] = useState(false);
  const [vpnStatus, setVpnStatus] = useState("Disconnected");
  const [error, setError] = useState("");
  const [config, setConfig] = useState("");
  const [qrCode, setQrCode] = useState("");
  const [isConnecting, setIsConnecting] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    (async () => {
      try {
        const jwtCookie = document.cookie
          .split("; ")
          .find(row => row.startsWith("jwt="));

        if (!jwtCookie) throw new Error("JWT cookie not found");

        const token = jwtCookie.split("=")[1];
        const decoded = parseJwt<JwtPayload>(token);
        const userId = decoded.id;

        const resp = await checkSubscription(userId);
        setIsSubscribed(resp.isActive);
        if (!resp.isActive) navigate("/payment");
      } catch (err: any) {
        setError(`Failed to fetch subscription. ${err.message || err}`);
      } finally {
        setIsLoading(false);
      }
    })();
  }, [navigate]);

  const handleConnect = async () => {
    if (!isSubscribed) {
      alert("Please subscribe to a plan to use VPN.");
      return;
    }
    setIsConnecting(true);
    setError("");
    try {
      const resp = await connectToVpn();
      setVpnStatus("Connected");
      setConfig(resp.config);
      setQrCode(resp.qrCode);
    } catch (err: any) {
      setError(`Failed to connect. ${err.message || err}`);
      setVpnStatus("Connection Failed");
    } finally {
      setIsConnecting(false);
    }
  };

  const handleDisconnect = async () => {
    try {
      await stopVpn();
      setVpnStatus("Disconnected");
      setConfig("");
      setQrCode("");
    } catch (err: any) {
      setError(`Failed to disconnect. ${err.message || err}`);
    }
  };

  if (isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center text-white">
        Loading...
      </div>
    );
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-r from-blue-900 to-blue-600 p-6">
      <Card className="w-full max-w-lg bg-gray-900 shadow-2xl border border-gray-700 rounded-lg">
        <CardHeader>
          <CardTitle className="text-white text-center text-2xl">
            CloudVPN – Secure Browsing
          </CardTitle>
          <p className="text-gray-400 text-center text-sm">Powered by AWS</p>
        </CardHeader>
        <CardContent className="space-y-4">
          {error && <p className="text-red-500 text-center">{error}</p>}
          <div className="text-center text-white">
            VPN Status:{" "}
            <span
              className={
                vpnStatus === "Connected"
                  ? "text-green-400"
                  : vpnStatus === "Disconnected"
                  ? "text-gray-400"
                  : "text-red-400"
              }
            >
              {vpnStatus}
            </span>
          </div>
          <div className="flex gap-2">
            <Button
              onClick={handleConnect}
              disabled={vpnStatus === "Connected" || isConnecting}
              className="flex-1"
            >
              {isConnecting ? "Connecting…" : "Connect to VPN"}
            </Button>
            <Button
              onClick={handleDisconnect}
              disabled={vpnStatus !== "Connected"}
              className="flex-1 bg-red-600 hover:bg-red-500"
            >
              Disconnect VPN
            </Button>
          </div>

          {qrCode && (
            <div className="mt-4 p-4 bg-gray-800 rounded-lg">
              <h3 className="text-white text-center mb-2">Scan QR Code</h3>
              <img
                src={`data:image/png;base64,${qrCode}`}
                alt="WireGuard QR Code"
                className="w-48 h-48 mx-auto border rounded-lg"
              />
            </div>
          )}

          {config && (
            <div className="mt-4">
              <h3 className="text-white text-center mb-2">Or use this config:</h3>
              <div className="relative">
                <textarea
                  value={config}
                  readOnly
                  className="w-full h-40 p-3 bg-gray-800 text-gray-200 rounded-lg font-mono text-sm resize-none"
                />
                <Button
                  onClick={() => {
                    navigator.clipboard.writeText(config);
                    alert("Copied!");
                  }}
                  className="absolute top-2 right-2"
                  size="sm"
                >
                  Copy
                </Button>
              </div>
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
