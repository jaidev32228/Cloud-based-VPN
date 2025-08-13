import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Card, CardHeader, CardTitle, CardContent } from "@/components/ui/card";
import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { loginUser } from "@/services/api";
import Cookies from "js-cookie";

export default function Login() {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const navigate = useNavigate();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!username || !password) {
      setError("❌ Username and Password are required.");
      return;
    }

    try {
      await loginUser(username, password);
      // Redirect based on role
      const role = Cookies.get("role");
      if (role === "ADMIN") navigate("/admin"); 
      else navigate("/dashboard"); 

    } catch (err) {
      setError(err instanceof Error ? err.message : "An unexpected error occurred.");
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-r from-blue-900 to-blue-600 p-6">
      <Card className="w-full max-w-lg bg-gray-900 shadow-2xl border border-gray-700 rounded-lg">
        <CardHeader>
          <CardTitle className="text-white text-center text-2xl font-semibold">
            CloudVPN - Secure Browsing
          </CardTitle>
        </CardHeader>
        <CardContent>
          {error && <p className="text-red-500 text-center mb-4">{error}</p>}
          <form onSubmit={handleSubmit} className="space-y-6">
            <Input placeholder="Username" value={username} onChange={(e) => setUsername(e.target.value)} required />
            <Input type="password" placeholder="Password" value={password} onChange={(e) => setPassword(e.target.value)} required />
            <Button type="submit" className="w-full bg-blue-600">Login Securely</Button>
          </form>
        </CardContent>
      </Card>
    </div>
  );
}
