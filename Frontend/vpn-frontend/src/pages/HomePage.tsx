import { Button } from "@/components/ui/button";
import { Card, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";
import { useNavigate } from "react-router-dom"; // Import useNavigate

export default function HomePage() {
  const navigate = useNavigate(); // Initialize navigate function

  return (
    <div className="min-h-screen flex flex-col items-center justify-center bg-gradient-to-r from-blue-900 to-blue-700 p-6">
      <h1 className="text-5xl font-extrabold text-center mb-6 text-white drop-shadow-lg">
        Welcome to SecureVPN
      </h1>
      <p className="text-center text-gray-300 mb-10 text-lg">
        Your ultimate solution for secure and private internet browsing. Explore our features and get started today!
      </p>

      {/* Features Section */}
      <h2 className="text-3xl font-bold text-center mb-8 text-white">Features</h2>
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        <Card className="bg-gray-800 shadow-lg hover:shadow-xl transition-transform transform hover:scale-105">
          <CardHeader>
            <CardTitle className="text-white">Secure Browsing</CardTitle>
            <CardDescription className="text-gray-400">
              Encrypt your internet traffic and protect your data.
            </CardDescription>
          </CardHeader>
        </Card>
        <Card className="bg-gray-800 shadow-lg hover:shadow-xl transition-transform transform hover:scale-105">
          <CardHeader>
            <CardTitle className="text-white">Multiple Plans</CardTitle>
            <CardDescription className="text-gray-400">
              Choose from flexible subscription plans.
            </CardDescription>
          </CardHeader>
        </Card>
        <Card className="bg-gray-800 shadow-lg hover:shadow-xl transition-transform transform hover:scale-105">
          <CardHeader>
            <CardTitle className="text-white">No Activity Logs</CardTitle>
            <CardDescription className="text-gray-400">
              We ensure complete privacy with zero tracking.
            </CardDescription>
          </CardHeader>
        </Card>
      </div>

      {/* Call to Action */}
      <div className="text-center mt-12">
        <Button
          onClick={() => navigate("/register")} // Redirect to Register page
          className="bg-blue-600 hover:bg-blue-500 text-white py-3 px-6 rounded-lg font-semibold shadow-md transition-transform transform hover:scale-105"
        >
          Get Started
        </Button>
      </div>
    </div>
  );
}
