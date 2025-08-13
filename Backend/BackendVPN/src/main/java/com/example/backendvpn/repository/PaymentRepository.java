package com.example.backendvpn.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.backendvpn.model.Payment;
import com.example.backendvpn.model.User;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByUser(User user);

	Optional<Payment> findByRazorpayOrderId(String razorpayOrderId);
}
