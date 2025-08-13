package com.example.backendvpn.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.backendvpn.model.PasswordHistory;
import com.example.backendvpn.model.User;

public interface PasswordHistoryRepository extends JpaRepository<PasswordHistory, Long> {
    List<PasswordHistory> findTop5ByUserOrderByChangedAtDesc(User user);
}
