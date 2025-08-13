import React from "react";
import { Navigate } from "react-router-dom";
import Cookies from "js-cookie";

interface ProtectedRouteProps {
  element: React.ReactElement;
  roleRequired: "USER" | "ADMIN";
}

export default function ProtectedRoute({ element, roleRequired }: ProtectedRouteProps) {
  const token = Cookies.get("token");
  const role = Cookies.get("role");

  if (!token || role !== roleRequired) {
    return <Navigate to="/" />;
  }

  return element;
}
