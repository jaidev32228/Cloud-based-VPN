package com.example.backendvpn.repository;

import java.util.*;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.backendvpn.model.User;
import com.example.backendvpn.model.UserCredentials;

public interface UserCredentialsRepository extends JpaRepository<UserCredentials, Long> {
    Optional<UserCredentials> findByUser(User user);
}