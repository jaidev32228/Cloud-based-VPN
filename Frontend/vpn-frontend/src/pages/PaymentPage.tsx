import { Button } from "@/components/ui/button";
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from "@/components/ui/card";
import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import Cookies from "js-cookie";
import {
  createFreeSubscription,
  createOrder,
  handlePaymentSuccess,
  createPaidSubscription
} from "@/services/api";
import { parseJwt } from "@/utils/jwt"

declare const Razorpay: any;

export default function PaymentPage() {
  const [selectedPlan, setSelectedPlan] = useState<"FREE"|"MONTHLY"|"YEARLY"|null>(null);
  const [error, setError] = useState("");
  const [userId, setUserId] = useState<number|null>(null);
  const navigate = useNavigate();

  useEffect(() => {
    const token = Cookies.get("token");
    if (token) {
      try {
        const payload = parseJwt<{ id: number }>(token);
        setUserId(payload.id);
      } catch {
        setError("Invalid session. Please log in again.");
      }
    }
  }, []);

  const handleSubscribe = async () => {
    if (!selectedPlan) return setError("Please select a plan.");
    if (userId == null) return setError("Unable to determine user. Please re-login.");
    setError("");
    try {
      if (selectedPlan === "FREE") {
        await createFreeSubscription();
        navigate("/dashboard");
      } else {
        const amount = selectedPlan === "MONTHLY" ? 49 : 999;
        const orderId = await createOrder(userId, amount);
        const options = {
          key: process.env.REACT_APP_RAZORPAY_KEY!,
          order_id: orderId,
          amount: amount * 100,
          currency: "INR",
          handler: async (res: any) => {
            await handlePaymentSuccess(res.razorpay_order_id, res.razorpay_payment_id, res.razorpay_signature);
            await createPaidSubscription(selectedPlan, res.razorpay_order_id);
            navigate("/dashboard");
          },
          modal: { ondismiss: () => setError("Payment cancelled") }
        };
        new Razorpay(options).open();
      }
    } catch (err: any) {
      setError(err.message || "Subscription failed.");
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-r from-blue-900 to-blue-600 p-6">
      <Card className="w-full max-w-xl bg-gray-900 shadow-2xl border border-gray-700 rounded-lg">
        <CardHeader>
          <CardTitle className="text-white text-center text-2xl">Choose Your Plan</CardTitle>
          <CardDescription className="text-gray-400 text-center">Select a plan to get started</CardDescription>
        </CardHeader>
        <CardContent>
          {error && <p className="text-red-500 text-center mb-4">{error}</p>}
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            {(["FREE","MONTHLY","YEARLY"] as const).map(plan => (
              <Card
                key={plan}
                className={`p-4 cursor-pointer transition ${
                  selectedPlan===plan ? "border-2 border-blue-500" : "border-gray-700"
                }`}
                onClick={() => setSelectedPlan(plan)}
              >
                <CardHeader className="text-center">
                  <CardTitle className="text-white">
                    {plan==="FREE" ? "Free Trial" : plan==="MONTHLY" ? "Monthly Plan" : "Yearly Plan"}
                  </CardTitle>
                  <CardDescription className="text-gray-400">
                    {plan==="FREE" ? "7 Days Free" : plan==="MONTHLY" ? "₹49/month" : "₹999/year"}
                  </CardDescription>
                </CardHeader>
              </Card>
            ))}
          </div>
          <Button onClick={handleSubscribe} disabled={!selectedPlan} className="mt-6 w-full">
            Subscribe Now
          </Button>
        </CardContent>
      </Card>
    </div>
  );
}
