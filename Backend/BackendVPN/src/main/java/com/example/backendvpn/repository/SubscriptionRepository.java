package com.example.backendvpn.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.backendvpn.model.Subscription;
import com.example.backendvpn.model.User;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    Optional<Subscription> findByUserAndActiveTrue(User user);
}
