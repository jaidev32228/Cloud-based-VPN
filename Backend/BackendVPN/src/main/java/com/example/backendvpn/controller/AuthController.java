package com.example.backendvpn.controller;

import com.example.backendvpn.model.User;
import com.example.backendvpn.service.JwtService;
import com.example.backendvpn.service.UserService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class AuthController {

    @Autowired private AuthenticationManager authManager;
    @Autowired private JwtService jwtService;
    @Autowired private UserService userService;

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestParam String username,
            @RequestParam String password,
            HttpServletResponse response) {
        try {
            Authentication authentication = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
            );

            User user = (User) authentication.getPrincipal();

            if (userService.isPasswordExpired(user)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("❌ Password expired. Please change your password.");
            }

            // Generate JWT with embedded id & role
            String jwt = jwtService.generateToken(user);

            // Set HTTP-only cookie
            Cookie cookie = new Cookie("jwt", jwt);
            cookie.setHttpOnly(true);
            cookie.setSecure(true);   // set to false if not using HTTPS in dev
            cookie.setPath("/");
            cookie.setMaxAge((int) (jwtService.getExpiration() / 1000));
            response.addCookie(cookie);

            // Return the token in JSON as well for frontend JS
            Map<String, String> body = new HashMap<>();
            body.put("token", jwt);
            body.put("username", user.getUsername());
            body.put("role", user.getRole().name());

            return ResponseEntity.ok(body);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("Invalid username or password.");
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(
            @RequestParam String username,
            @RequestParam String email,
            @RequestParam String password) {

        if (userService.userExists(username)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("❌ Username already taken.");
        }

        userService.registerUser(username, email, password);
        return ResponseEntity.ok("✅ User registered successfully.");
    }
}
