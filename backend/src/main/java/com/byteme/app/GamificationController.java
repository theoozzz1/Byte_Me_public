package com.byteme.app;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/gamification")
public class GamificationController {

    private final OrganisationRepository orgRepo;
    private final OrgOrderRepository orderRepo;
    private final BadgeRepository badgeRepo;
    private final OrganisationBadgeRepository orgBadgeRepo;

    public GamificationController(OrganisationRepository orgRepo, OrgOrderRepository orderRepo,
                                   BadgeRepository badgeRepo, OrganisationBadgeRepository orgBadgeRepo) {
        this.orgRepo = orgRepo;
        this.orderRepo = orderRepo;
        this.badgeRepo = badgeRepo;
        this.orgBadgeRepo = orgBadgeRepo;
    }

    @GetMapping("/streak/{orgId}")
    public ResponseEntity<?> getStreak(@PathVariable UUID orgId) {
        var org = orgRepo.findById(orgId).orElse(null);
        if (org == null) return ResponseEntity.notFound().build();

        return ResponseEntity.ok(new StreakResponse(
                org.getCurrentStreakWeeks(),
                org.getBestStreakWeeks(),
                org.getLastOrderWeekStart()
        ));
    }

    @GetMapping("/stats/{orgId}")
    public ResponseEntity<?> getStats(@PathVariable UUID orgId) {
        var org = orgRepo.findById(orgId).orElse(null);
        if (org == null) return ResponseEntity.notFound().build();

        int badgeCount = orgBadgeRepo.findByOrgId(orgId).size();

        return ResponseEntity.ok(new StatsResponse(
                org.getTotalOrders(),
                org.getCurrentStreakWeeks(),
                org.getBestStreakWeeks(),
                badgeCount
        ));
    }

    @GetMapping("/badges/{orgId}")
    public List<OrganisationBadge> getOrgBadges(@PathVariable UUID orgId) {
        return orgBadgeRepo.findByOrgId(orgId);
    }

    @GetMapping("/badges")
    public List<Badge> getAllBadges() {
        return badgeRepo.findAll();
    }

    // DTOs
    public static class StreakResponse {
        private int currentStreakWeeks;
        private int bestStreakWeeks;
        private LocalDate lastOrderWeekStart;

        public StreakResponse(int currentStreakWeeks, int bestStreakWeeks, LocalDate lastOrderWeekStart) {
            this.currentStreakWeeks = currentStreakWeeks;
            this.bestStreakWeeks = bestStreakWeeks;
            this.lastOrderWeekStart = lastOrderWeekStart;
        }

        public int getCurrentStreakWeeks() { return currentStreakWeeks; }
        public int getBestStreakWeeks() { return bestStreakWeeks; }
        public LocalDate getLastOrderWeekStart() { return lastOrderWeekStart; }
    }

    public static class StatsResponse {
        private int totalOrders;
        private int currentStreakWeeks;
        private int bestStreakWeeks;
        private int badgesEarned;

        public StatsResponse(int totalOrders, int currentStreakWeeks, int bestStreakWeeks, int badgesEarned) {
            this.totalOrders = totalOrders;
            this.currentStreakWeeks = currentStreakWeeks;
            this.bestStreakWeeks = bestStreakWeeks;
            this.badgesEarned = badgesEarned;
        }

        public int getTotalOrders() { return totalOrders; }
        public int getCurrentStreakWeeks() { return currentStreakWeeks; }
        public int getBestStreakWeeks() { return bestStreakWeeks; }
        public int getBadgesEarned() { return badgesEarned; }
    }
}
