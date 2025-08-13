import axios from "axios";
import Cookies from "js-cookie";

const API_URL = "http://localhost:8080/api";

// Automatically attach JWT
axios.interceptors.request.use(cfg => {
  const token = Cookies.get("token");
  if (token) cfg.headers!["Authorization"] = `Bearer ${token}`;
  return cfg;
});

// Auth
export interface LoginResponse { token: string; role: string; username: string; }
export const loginUser = async (username: string, password: string): Promise<void> => {
  const resp = await axios.post<LoginResponse>(`${API_URL}/auth/login`, null, {
    params: { username, password },
  });
  Cookies.set("token", resp.data.token, { expires: 1 });
  Cookies.set("role", resp.data.role, { expires: 1 });
  Cookies.set("username", resp.data.username, { expires: 1 });
};

export interface User { id: number; email: string; role: string; }
export const registerUser = async (username: string, email: string, password: string): Promise<User> => {
  const resp = await axios.post<User>(`${API_URL}/auth/register`, null, {
    params: { username, email, password },
  });
  return resp.data;
};
export const checkSubscription = async (userId: number) => {
  const res = await fetch(`/api/subscription/${userId}`, {
    credentials: "include"
  });
  if (!res.ok) throw new Error("Subscription not found");
  const data = await res.json();
  return {
    isActive: data?.active || false,
    planType: data?.planType || null
  };
};


// // Subscription
// export interface SubscriptionResponse { isActive: boolean; }

export const createFreeSubscription = () =>
  axios.post(`${API_URL}/subscription/create-free`).then(r => r.data);

export const createPaidSubscription = (plan: "MONTHLY" | "YEARLY", orderId: string) =>
  axios.post(`${API_URL}/subscription/create`, null, {
    params: { planType: plan, razorpayOrderId: orderId }
  }).then(r => r.data);

// Payments
export const createOrder = (userId: number, amount: number, referralCode?: string) =>
  axios.post<string>(`${API_URL}/payments/create-order/${userId}`, null, {
    params: { amount, referralCode }
  }).then(r => r.data);

export const handlePaymentSuccess = (orderId: string, paymentId: string, signature: string) =>
  axios.post(`${API_URL}/payments/success`, null, {
    params: { razorpayOrderId: orderId, razorpayPaymentId: paymentId, razorpaySignature: signature }
  }).then(r => r.data);

// VPN
export const connectToVpn = () =>
  axios.post<{ config: string; qrCode: string }>(`${API_URL}/vpn/connect`).then(r => r.data);

export const stopVpn = () =>
  axios.post(`${API_URL}/vpn/disconnect`);
export const getAllUsers = async (): Promise<User[]> => {
  const response = await axios.get<User[]>(`${API_URL}/admin/users`, {
    headers: { Authorization: `Bearer ${Cookies.get("token")}` },
  });
  return response.data;
};