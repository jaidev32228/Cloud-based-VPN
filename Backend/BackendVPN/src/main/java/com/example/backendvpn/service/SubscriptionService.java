package com.example.backendvpn.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.example.backendvpn.model.*;
import com.example.backendvpn.repository.*;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class SubscriptionService {
    @Autowired private SubscriptionRepository subscriptionRepo;
    @Autowired private UserRepository userRepo;
    @Autowired private PaymentRepository paymentRepo;

    public Optional<Subscription> getActiveSubscription() {
        User user = getCurrentUser();
        return subscriptionRepo.findByUserAndActiveTrue(user);
    }

    public Subscription createFreeSubscription() {
        User user = getCurrentUser();
        subscriptionRepo.findByUserAndActiveTrue(user).ifPresent(existing -> {
            if (existing.getPlanType() != PlanType.FREE) {
                throw new RuntimeException("Already has active paid subscription");
            }
        });
        Subscription s = new Subscription();
        s.setUser(user);
        s.setPlanType(PlanType.FREE);
        s.setStartDate(LocalDateTime.now());
        s.setEndDate(LocalDateTime.now().plusDays(7));
        s.setActive(true);
        return subscriptionRepo.save(s);
    }
    public Optional<Subscription> getActiveSubscriptionByUserId(Long userId) {
        return userRepo.findById(userId)
            .flatMap(user -> subscriptionRepo.findByUserAndActiveTrue(user));
    }


    public Subscription createSubscription(String planType, String razorpayOrderId) {
        User user = getCurrentUser();
        Payment pay = paymentRepo.findByRazorpayOrderId(razorpayOrderId)
            .orElseThrow(() -> new RuntimeException("Payment not found"));
        if (pay.getStatus() != PaymentStatus.COMPLETED)
            throw new RuntimeException("Payment not completed");

        subscriptionRepo.findByUserAndActiveTrue(user).ifPresent(existing -> {
            existing.setActive(false);
            subscriptionRepo.save(existing);
        });

        PlanType pt = PlanType.valueOf(planType);
        Subscription s = new Subscription();
        s.setUser(user);
        s.setPlanType(pt);
        s.setStartDate(LocalDateTime.now());
        s.setEndDate(calculateEndDate(pt));
        s.setActive(true);
        return subscriptionRepo.save(s);
    }

    private LocalDateTime calculateEndDate(PlanType planType) {
        return switch (planType) {
            case MONTHLY -> LocalDateTime.now().plusMonths(1);
            case YEARLY -> LocalDateTime.now().plusYears(1);
            default -> LocalDateTime.now().plusDays(7);
        };
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated())
            throw new RuntimeException("Not authenticated");
        return userRepo.findByUsername(auth.getName())
            .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
