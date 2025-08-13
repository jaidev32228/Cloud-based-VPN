package com.example.backendvpn.service;

import java.math.BigDecimal;
import java.util.UUID;

import org.apache.commons.codec.digest.HmacUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.backendvpn.model.Payment;
import com.example.backendvpn.model.PaymentStatus;
import com.example.backendvpn.model.User;
import com.example.backendvpn.repository.PaymentRepository;
import com.example.backendvpn.repository.UserRepository;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;

@Service
public class PaymentService {
    @Value("${razorpay.key.id}") 
    private String keyId;
    @Value("${razorpay.key.secret}") 
    private String keySecret;

    private final PaymentRepository paymentRepo;
    private final UserRepository userRepo;

    public PaymentService(PaymentRepository paymentRepo, UserRepository userRepo) {
        this.paymentRepo = paymentRepo;
        this.userRepo = userRepo;
    }

    /**
     * Create a Razorpay order, persist a Payment record (PENDING), and return the orderId.
     */
    public String createOrder(Long userId, double amount, String referralCode) throws RazorpayException {
        // 1) Lookup user
        User user = userRepo.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        // 2) Compute final amount with referral discount
        BigDecimal finalAmt = applyReferralDiscount(amount, referralCode);

        // 3) Build Razorpay order request
        RazorpayClient client = new RazorpayClient(keyId, keySecret);
        JSONObject req = new JSONObject();
        req.put("amount", finalAmt.multiply(BigDecimal.valueOf(100))); // in paise
        req.put("currency", "INR");
        req.put("receipt", "rcpt_" + UUID.randomUUID());
        req.put("payment_capture", 1);

        JSONObject notes = new JSONObject();
        notes.put("userId", userId);
        notes.put("referralCode", referralCode);
        req.put("notes", notes);

        // 4) Create order
        Order order = client.orders.create(req);

        // 5) Persist Payment record
        Payment p = new Payment();
        p.setUser(user);
        p.setAmount(finalAmt);
        p.setRazorpayOrderId(order.get("id"));
        p.setStatus(PaymentStatus.PENDING);
        paymentRepo.save(p);

        return order.get("id");
    }

    private BigDecimal applyReferralDiscount(double amount, String referralCode) {
        BigDecimal orig = BigDecimal.valueOf(amount);
        if (referralCode != null && !referralCode.isEmpty()) {
            BigDecimal discount = orig.multiply(BigDecimal.valueOf(0.10)); // 10%
            return orig.subtract(discount);
        }
        return orig;
    }

    /**
     * Verify signature, update Payment to COMPLETED, store paymentId & signature.
     */
    public void handlePaymentSuccess(String orderId, String paymentId, String signature) {
        Payment p = paymentRepo.findByRazorpayOrderId(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found"));

        // Verify signature
        String payload = orderId + "|" + paymentId;
        String generatedSig = HmacUtils.hmacSha256Hex(keySecret, payload);
        if (!generatedSig.equals(signature)) {
            throw new RuntimeException("Invalid payment signature");
        }

        // Update record
        p.setRazorpayPaymentId(paymentId);
        p.setRazorpaySignature(signature);
        p.setStatus(PaymentStatus.COMPLETED);
        paymentRepo.save(p);
    }
}
