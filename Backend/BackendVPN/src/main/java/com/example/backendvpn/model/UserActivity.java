package com.example.backendvpn.model;

import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
public class UserActivity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
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
	public String getActivityType() {
		return activityType;
	}
	public void setActivityType(String activityType) {
		this.activityType = activityType;
	}
	public LocalDateTime getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(LocalDateTime timestamp) {
		this.timestamp = timestamp;
	}
	@ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    private String activityType;
    private LocalDateTime timestamp = LocalDateTime.now();
}
