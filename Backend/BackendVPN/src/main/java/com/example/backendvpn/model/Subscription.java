package com.example.backendvpn.model;

import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
public class Subscription {
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
	public PlanType getPlanType() {
		return planType;
	}
	public void setPlanType(PlanType planType) {
		this.planType = planType;
	}
	public LocalDateTime getStartDate() {
		return startDate;
	}
	public void setStartDate(LocalDateTime startDate) {
		this.startDate = startDate;
	}
	public LocalDateTime getEndDate() {
		return endDate;
	}
	public void setEndDate(LocalDateTime endDate) {
		this.endDate = endDate;
	}
	public boolean isActive() {
		return active;
	}
	public void setActive(boolean active) {
		this.active = active;
	}
	@ManyToOne
    @JoinColumn(name = "user_id", nullable = false,unique=true)
    private User user;
    
    @Enumerated(EnumType.STRING)
    private PlanType planType;
    
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private boolean active;
}