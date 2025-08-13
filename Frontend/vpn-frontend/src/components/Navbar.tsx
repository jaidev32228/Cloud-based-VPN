import { useState, useEffect } from "react";
import { Link, useNavigate } from "react-router-dom";
import { Button } from "@/components/ui/button";
import { Avatar, AvatarImage, AvatarFallback } from "@/components/ui/avatar";
import { DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger } from "@/components/ui/dropdown-menu";
import { DarkModeToggle } from "@/components/DarkModeToggle";
import Cookies from "js-cookie";

export default function NavigationBar() {
  const navigate = useNavigate();
  const [token, setToken] = useState(Cookies.get("token"));
  const [role, setRole] = useState(Cookies.get("role"));
  const [username, setUsername] = useState("User");

  useEffect(() => {
    setToken(Cookies.get("token"));
    setRole(Cookies.get("role"));
    setUsername(Cookies.get("username") || "User");
  }, []);

  const handleLogout = () => {
    Cookies.remove("token");
    Cookies.remove("role");
    Cookies.remove("username");
    setToken(undefined);
    setRole(undefined);
    navigate("/");
  };

  return (
    <nav className="bg-background border-b border-gray-700">
      <div className="container flex items-center justify-between h-16 px-4 md:px-6">
        {/* Logo */}
        <Link to="/" className="text-2xl font-semibold text-primary">
          SecureVPN
        </Link>

        {/* Right Side: Auth + Dark Mode */}
        <div className="ml-auto flex items-center space-x-4">
          <DarkModeToggle />

          {!token ? (
            <>
              <Link to="/login">
                <Button variant="ghost">Login</Button>
              </Link>
              <Link to="/register">
                <Button className="bg-blue-500 hover:bg-blue-600 text-white">
                  Register
                </Button>
              </Link>
            </>
          ) : (
            <DropdownMenu>
              <DropdownMenuTrigger>
                <Avatar>
                  <AvatarImage src={`https://api.dicebear.com/7.x/initials/svg?seed=${username}`} alt="User Avatar" />
                  <AvatarFallback>{username.charAt(0).toUpperCase()}</AvatarFallback>
                </Avatar>
              </DropdownMenuTrigger>
              <DropdownMenuContent align="end">
                <DropdownMenuItem onClick={() => navigate(role === "ADMIN" ? "/admin" : "/dashboard")}>
                  Dashboard
                </DropdownMenuItem>
                <DropdownMenuItem onClick={handleLogout}>Logout</DropdownMenuItem>
              </DropdownMenuContent>
            </DropdownMenu>
          )}
        </div>
      </div>
    </nav>
  );
}
