package com.example.backendvpn.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.backendvpn.service.PaymentService;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin
public class PaymentController {
    @Autowired private PaymentService paymentService;

    @PostMapping("/create-order/{userId}")
    public ResponseEntity<String> createOrder(
        @PathVariable Long userId,
        @RequestParam double amount,
        @RequestParam(required=false) String referralCode) throws Exception {
        String orderId = paymentService.createOrder(userId, amount, referralCode);
        return ResponseEntity.ok(orderId);
    }

    @PostMapping("/success")
    public ResponseEntity<String> handleSuccess(
        @RequestParam String razorpayOrderId,
        @RequestParam String razorpayPaymentId,
        @RequestParam String razorpaySignature) {
      paymentService.handlePaymentSuccess(razorpayOrderId, razorpayPaymentId, razorpaySignature);
      return ResponseEntity.ok("Payment verified");
    }
}
