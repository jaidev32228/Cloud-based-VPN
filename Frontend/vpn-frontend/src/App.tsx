import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import NavigationBar from "@/components/Navbar";
import Login from "@/pages/Login";
import Register from "@/pages/Register";
import UserDashboard from "@/pages/UserDashboard";
import PaymentPage from "./pages/PaymentPage";
import AdminDashboard from "@/pages/AdminDashboard";

import HomePage from "@/pages/HomePage";
import ProtectedRoute from "@/components/ProtectedRoute";
import Cookies from "js-cookie";

export default function App() {
  const token = Cookies.get("token");
  const role = Cookies.get("role");

  return (
    <BrowserRouter>
      <NavigationBar />
      <Routes>
        <Route path="/" element={<HomePage />} />
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />
        
        {/* Protected Routes */}
        <Route path="/dashboard" element={<ProtectedRoute element={<UserDashboard />} roleRequired="USER" />} />
        <Route path="/payment" element={<ProtectedRoute element={<PaymentPage />} roleRequired="USER" />} />
        <Route path="/admin" element={<ProtectedRoute element={<AdminDashboard />} roleRequired="ADMIN" />} />

        {/* Redirect all other routes */}
        <Route path="*" element={<Navigate to={token ? (role === "ADMIN" ? "/admin" : "/dashboard") : "/"} />} />
      </Routes>
    </BrowserRouter>
  );
}
