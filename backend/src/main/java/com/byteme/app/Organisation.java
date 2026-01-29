package com.byteme.app;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "organisation")
public class Organisation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID orgId;

    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private UserAccount user;

    @Column(nullable = false)
    private String name;

    private String locationText;
    private String billingEmail;

    // Gamification
    @Column(nullable = false)
    private Integer currentStreakWeeks = 0;

    @Column(nullable = false)
    private Integer bestStreakWeeks = 0;

    private LocalDate lastOrderWeekStart;

    @Column(nullable = false)
    private Integer totalOrders = 0;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    // Getters
    public UUID getOrgId() { return orgId; }
    public UserAccount getUser() { return user; }
    public String getName() { return name; }
    public String getLocationText() { return locationText; }
    public String getBillingEmail() { return billingEmail; }
    public Integer getCurrentStreakWeeks() { return currentStreakWeeks; }
    public Integer getBestStreakWeeks() { return bestStreakWeeks; }
    public LocalDate getLastOrderWeekStart() { return lastOrderWeekStart; }
    public Integer getTotalOrders() { return totalOrders; }
    public Instant getCreatedAt() { return createdAt; }

    // Setters
    public void setOrgId(UUID orgId) { this.orgId = orgId; }
    public void setUser(UserAccount user) { this.user = user; }
    public void setName(String name) { this.name = name; }
    public void setLocationText(String locationText) { this.locationText = locationText; }
    public void setBillingEmail(String billingEmail) { this.billingEmail = billingEmail; }
    public void setCurrentStreakWeeks(Integer currentStreakWeeks) { this.currentStreakWeeks = currentStreakWeeks; }
    public void setBestStreakWeeks(Integer bestStreakWeeks) { this.bestStreakWeeks = bestStreakWeeks; }
    public void setLastOrderWeekStart(LocalDate lastOrderWeekStart) { this.lastOrderWeekStart = lastOrderWeekStart; }
    public void setTotalOrders(Integer totalOrders) { this.totalOrders = totalOrders; }
}
