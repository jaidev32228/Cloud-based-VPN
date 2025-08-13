package com.example.backendvpn.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class UserCredentials {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String password;
    private LocalDateTime lastPasswordChange = LocalDateTime.now();

    public boolean isPasswordExpired() {
        LocalDateTime now = LocalDateTime.now();
        long daysSinceLastChange = java.time.temporal.ChronoUnit.DAYS.between(lastPasswordChange, now);
        return daysSinceLastChange >= 90; // Password expires after 90 days
    }

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public LocalDateTime getLastPasswordChange() {
		return lastPasswordChange;
	}

	public void setLastPasswordChange(LocalDateTime lastPasswordChange) {
		this.lastPasswordChange = lastPasswordChange;
	}
	
}