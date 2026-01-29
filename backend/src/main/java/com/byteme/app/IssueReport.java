package com.byteme.app;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "issue_report")
public class IssueReport {

    public enum Type { UNAVAILABLE, QUALITY, OTHER }
    public enum Status { OPEN, RESPONDED, RESOLVED }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID issueId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private OrgOrder order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "org_id")
    private Organisation organisation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Type type;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.OPEN;

    @Column(columnDefinition = "TEXT")
    private String sellerResponse;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    private Instant resolvedAt;

    // Getters
    public UUID getIssueId() { return issueId; }
    public OrgOrder getOrder() { return order; }
    public Organisation getOrganisation() { return organisation; }
    public Type getType() { return type; }
    public String getDescription() { return description; }
    public Status getStatus() { return status; }
    public String getSellerResponse() { return sellerResponse; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getResolvedAt() { return resolvedAt; }

    // Setters
    public void setIssueId(UUID issueId) { this.issueId = issueId; }
    public void setOrder(OrgOrder order) { this.order = order; }
    public void setOrganisation(Organisation organisation) { this.organisation = organisation; }
    public void setType(Type type) { this.type = type; }
    public void setDescription(String description) { this.description = description; }
    public void setStatus(Status status) { this.status = status; }
    public void setSellerResponse(String sellerResponse) { this.sellerResponse = sellerResponse; }
    public void setResolvedAt(Instant resolvedAt) { this.resolvedAt = resolvedAt; }
}
