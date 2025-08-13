package com.example.backendvpn.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.backendvpn.model.User;
import com.example.backendvpn.model.UserActivity;

public interface UserActivityRepository extends JpaRepository<UserActivity, Long> {
    List<UserActivity> findByUser(User user);
}