package com.example.backendvpn.service;

import com.example.backendvpn.model.User;
import com.example.backendvpn.model.UserCredentials;
import com.example.backendvpn.repository.UserRepository;
import com.example.backendvpn.repository.UserCredentialsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserCredentialsRepository userCredentialsRepository;

    @Autowired
    private PasswordEncoder passwordEncoder; // Uses BCryptPasswordEncoder

    public User registerUser(String username, String email, String password) {
        System.out.println("📝 Registering new user: " + username);

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setRole(User.Role.USER); // Assign default role

        UserCredentials credentials = new UserCredentials();
        credentials.setPassword(passwordEncoder.encode(password)); // Proper encoding
        credentials.setUser(user);
        user.setCredentials(credentials);

        return userRepository.save(user);
    }

    public boolean isPasswordExpired(User user) {
        return user.getCredentials().isPasswordExpired();
    }

    public boolean userExists(String username) {
        return userRepository.findByUsername(username).isPresent();
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println("🔍 Searching for user: " + username);
        return userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    System.out.println("❌ User not found: " + username);
                    return new UsernameNotFoundException("User not found: " + username);
                });
    }
}
