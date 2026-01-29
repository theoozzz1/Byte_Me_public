package com.byteme.app;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "organisation_badge")
@IdClass(OrganisationBadge.Key.class)
public class OrganisationBadge {

    @Id
    @Column(name = "org_id")
    private UUID orgId;

    @Id
    @Column(name = "badge_id")
    private UUID badgeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "org_id", insertable = false, updatable = false)
    private Organisation organisation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "badge_id", insertable = false, updatable = false)
    private Badge badge;

    @Column(nullable = false)
    private Instant awardedAt = Instant.now();

    // Getters
    public UUID getOrgId() { return orgId; }
    public UUID getBadgeId() { return badgeId; }
    public Organisation getOrganisation() { return organisation; }
    public Badge getBadge() { return badge; }
    public Instant getAwardedAt() { return awardedAt; }

    // Setters
    public void setOrgId(UUID orgId) { this.orgId = orgId; }
    public void setBadgeId(UUID badgeId) { this.badgeId = badgeId; }
    public void setOrganisation(Organisation organisation) { this.organisation = organisation; }
    public void setBadge(Badge badge) { this.badge = badge; }
    public void setAwardedAt(Instant awardedAt) { this.awardedAt = awardedAt; }

    public static class Key implements java.io.Serializable {
        private UUID orgId;
        private UUID badgeId;

        public UUID getOrgId() { return orgId; }
        public UUID getBadgeId() { return badgeId; }
        public void setOrgId(UUID orgId) { this.orgId = orgId; }
        public void setBadgeId(UUID badgeId) { this.badgeId = badgeId; }
    }
}
