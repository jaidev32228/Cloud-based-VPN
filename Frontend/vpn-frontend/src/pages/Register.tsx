import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Card, CardHeader, CardTitle, CardContent } from "@/components/ui/card";
import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { registerUser } from "@/services/api";

export default function Register() {
  const [username, setUsername] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const navigate = useNavigate();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await registerUser(username, email, password);
      navigate("/login");
    } catch (err) {
      setError(`Registration failed. Please try again. ${err}`);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-r from-blue-900 to-blue-600 p-6">
      <Card className="w-full max-w-lg bg-gray-900 shadow-2xl border border-gray-700 rounded-lg">
        <CardHeader>
          <CardTitle className="text-white text-center text-2xl font-semibold">
            CloudVPN - Register
          </CardTitle>
          <p className="text-gray-400 text-center text-sm">
            Create an account for secure browsing
          </p>
        </CardHeader>
        <CardContent>
          {error && (
            <p className="text-red-500 text-center mb-4 font-medium">{error}</p>
          )}
          <form onSubmit={handleSubmit} className="space-y-6">
            <div>
              <label className="block text-gray-300 mb-1">Username</label>
              <Input
                type="text"
                placeholder="Choose a username"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                className="bg-gray-800 text-white border border-gray-600 focus:ring focus:ring-blue-400"
                required
              />
            </div>
            <div>
              <label className="block text-gray-300 mb-1">Email Address</label>
              <Input
                type="email"
                placeholder="Enter your email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                className="bg-gray-800 text-white border border-gray-600 focus:ring focus:ring-blue-400"
                required
              />
            </div>
            <div>
              <label className="block text-gray-300 mb-1">Password</label>
              <Input
                type="password"
                placeholder="Enter your password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                className="bg-gray-800 text-white border border-gray-600 focus:ring focus:ring-blue-400"
                required
              />
            </div>
            <Button
              type="submit"
              className="w-full bg-blue-600 hover:bg-blue-500 text-white py-2 rounded-lg font-semibold shadow-md"
            >
              Register
            </Button>
          </form>
          <p className="text-gray-400 text-sm text-center mt-4">
            Already have an account?{" "}
            <a href="/login" className="text-blue-400 hover:underline">
              Login here
            </a>
          </p>
        </CardContent>
      </Card>
    </div>
  );
}