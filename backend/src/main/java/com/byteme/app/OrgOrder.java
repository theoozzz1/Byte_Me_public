package com.byteme.app;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "org_order")
public class OrgOrder {

    public enum Status { RESERVED, COLLECTED, CANCELLED, EXPIRED }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID orderId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "org_id", nullable = false)
    private Organisation organisation;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "posting_id", nullable = false)
    private BundlePosting posting;

    @Column(nullable = false)
    private Integer quantity = 1;

    @Column(nullable = false)
    private Integer totalPriceCents;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.RESERVED;

    @Column(nullable = false)
    private Instant reservedAt = Instant.now();

    private Instant collectedAt;
    private Instant cancelledAt;

    // Getters
    public UUID getOrderId() { return orderId; }
    public Organisation getOrganisation() { return organisation; }
    public BundlePosting getPosting() { return posting; }
    public Integer getQuantity() { return quantity; }
    public Integer getTotalPriceCents() { return totalPriceCents; }
    public Status getStatus() { return status; }
    public Instant getReservedAt() { return reservedAt; }
    public Instant getCollectedAt() { return collectedAt; }
    public Instant getCancelledAt() { return cancelledAt; }

    // Setters
    public void setOrderId(UUID orderId) { this.orderId = orderId; }
    public void setOrganisation(Organisation organisation) { this.organisation = organisation; }
    public void setPosting(BundlePosting posting) { this.posting = posting; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public void setTotalPriceCents(Integer totalPriceCents) { this.totalPriceCents = totalPriceCents; }
    public void setStatus(Status status) { this.status = status; }
    public void setReservedAt(Instant reservedAt) { this.reservedAt = reservedAt; }
    public void setCollectedAt(Instant collectedAt) { this.collectedAt = collectedAt; }
    public void setCancelledAt(Instant cancelledAt) { this.cancelledAt = cancelledAt; }
}
