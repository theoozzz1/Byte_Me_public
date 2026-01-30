package com.byteme.app;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final BundlePostingRepository bundleRepo;
    private final OrgOrderRepository orderRepo;
    private final IssueReportRepository issueRepo;
    private final SellerRepository sellerRepo;

    public AnalyticsController(BundlePostingRepository bundleRepo, OrgOrderRepository orderRepo,
                                IssueReportRepository issueRepo, SellerRepository sellerRepo) {
        this.bundleRepo = bundleRepo;
        this.orderRepo = orderRepo;
        this.issueRepo = issueRepo;
        this.sellerRepo = sellerRepo;
    }

    @GetMapping("/dashboard/{sellerId}")
    public ResponseEntity<?> getDashboard(@PathVariable UUID sellerId) {
        var seller = sellerRepo.findById(sellerId).orElse(null);
        if (seller == null) return ResponseEntity.notFound().build();

        var bundles = bundleRepo.findBySeller_SellerId(sellerId);
        var orders = orderRepo.findByPostingSellerSellerId(sellerId);

        int totalPosted = bundles.size();
        int totalQuantity = bundles.stream().mapToInt(BundlePosting::getQuantityTotal).sum();

        long collectedCount = orders.stream().filter(o -> o.getStatus() == OrgOrder.Status.COLLECTED).count();
        long cancelledCount = orders.stream().filter(o -> o.getStatus() == OrgOrder.Status.CANCELLED).count();
        long expiredCount = orders.stream().filter(o -> o.getStatus() == OrgOrder.Status.EXPIRED).count();

        double sellThrough = totalQuantity > 0 ? (double) collectedCount / totalQuantity * 100 : 0;
        int openIssues = issueRepo.findOpenBySeller(sellerId).size();

        return ResponseEntity.ok(new DashboardResponse(
                seller.getName(), totalPosted, totalQuantity, (int) collectedCount,
                (int) cancelledCount, (int) expiredCount, Math.round(sellThrough * 10) / 10.0, openIssues
        ));
    }

    @GetMapping("/sell-through/{sellerId}")
    public ResponseEntity<?> getSellThrough(@PathVariable UUID sellerId) {
        var orders = orderRepo.findByPostingSellerSellerId(sellerId);

        long collected = orders.stream().filter(o -> o.getStatus() == OrgOrder.Status.COLLECTED).count();
        long cancelled = orders.stream().filter(o -> o.getStatus() == OrgOrder.Status.CANCELLED).count();
        long expired = orders.stream().filter(o -> o.getStatus() == OrgOrder.Status.EXPIRED).count();

        long total = collected + cancelled + expired;

        return ResponseEntity.ok(new SellThroughResponse(
                (int) collected, (int) cancelled, (int) expired,
                total > 0 ? (double) collected / total * 100 : 0,
                total > 0 ? (double) cancelled / total * 100 : 0
        ));
    }

    // DTOs
    public static class DashboardResponse {
        private String sellerName;
        private int totalBundlesPosted;
        private int totalQuantity;
        private int collectedCount;
        private int cancelledCount;
        private int expiredCount;
        private double sellThroughRate;
        private int openIssueCount;

        public DashboardResponse(String sellerName, int totalBundlesPosted, int totalQuantity,
                                  int collectedCount, int cancelledCount, int expiredCount,
                                  double sellThroughRate, int openIssueCount) {
            this.sellerName = sellerName;
            this.totalBundlesPosted = totalBundlesPosted;
            this.totalQuantity = totalQuantity;
            this.collectedCount = collectedCount;
            this.cancelledCount = cancelledCount;
            this.expiredCount = expiredCount;
            this.sellThroughRate = sellThroughRate;
            this.openIssueCount = openIssueCount;
        }

        public String getSellerName() { return sellerName; }
        public int getTotalBundlesPosted() { return totalBundlesPosted; }
        public int getTotalQuantity() { return totalQuantity; }
        public int getCollectedCount() { return collectedCount; }
        public int getCancelledCount() { return cancelledCount; }
        public int getExpiredCount() { return expiredCount; }
        public double getSellThroughRate() { return sellThroughRate; }
        public int getOpenIssueCount() { return openIssueCount; }
    }

    public static class SellThroughResponse {
        private int collected;
        private int cancelled;
        private int expired;
        private double collectionRate;
        private double cancelRate;

        public SellThroughResponse(int collected, int cancelled, int expired, double collectionRate, double cancelRate) {
            this.collected = collected;
            this.cancelled = cancelled;
            this.expired = expired;
            this.collectionRate = collectionRate;
            this.cancelRate = cancelRate;
        }

        public int getCollected() { return collected; }
        public int getCancelled() { return cancelled; }
        public int getExpired() { return expired; }
        public double getCollectionRate() { return collectionRate; }
        public double getCancelRate() { return cancelRate; }
    }
}
