package com.example.backendvpn.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.backendvpn.model.Subscription;
import com.example.backendvpn.service.SubscriptionService;

import java.util.Optional;

@RestController
@RequestMapping("/api/subscription")
@CrossOrigin
public class SubscriptionController {
    @Autowired private SubscriptionService subscriptionService;

    @GetMapping
    public ResponseEntity<Subscription> getSubscription() {
        Optional<Subscription> sub = subscriptionService.getActiveSubscription();
        return sub
          .map(ResponseEntity::ok)
          .orElse(ResponseEntity.noContent().build());
    }
    @GetMapping("/{userId}")
    public ResponseEntity<Subscription> getSubscription(@PathVariable Long userId) {
        Optional<Subscription> sub = subscriptionService.getActiveSubscriptionByUserId(userId);
        return sub.map(ResponseEntity::ok)
                  .orElse(ResponseEntity.noContent().build());
    }


    @PostMapping("/create-free")
    public ResponseEntity<Subscription> createFree() {
        Subscription s = subscriptionService.createFreeSubscription();
        return ResponseEntity.ok(s);
    }

    @PostMapping("/create")
    public ResponseEntity<Subscription> createPaid(
        @RequestParam String planType,
        @RequestParam String razorpayOrderId) {
      Subscription s = subscriptionService.createSubscription(planType, razorpayOrderId);
      return ResponseEntity.ok(s);
    }
}
