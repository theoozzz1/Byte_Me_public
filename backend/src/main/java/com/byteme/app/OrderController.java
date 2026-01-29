package com.byteme.app;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrgOrderRepository orderRepo;
    private final BundlePostingRepository bundleRepo;
    private final OrganisationRepository orgRepo;

    public OrderController(OrgOrderRepository orderRepo, BundlePostingRepository bundleRepo,
                           OrganisationRepository orgRepo) {
        this.orderRepo = orderRepo;
        this.bundleRepo = bundleRepo;
        this.orgRepo = orgRepo;
    }

    @GetMapping("/org/{orgId}")
    public List<OrgOrder> getByOrg(@PathVariable UUID orgId) {
        return orderRepo.findByOrganisationOrgId(orgId);
    }

    @GetMapping("/seller/{sellerId}")
    public List<OrgOrder> getBySeller(@PathVariable UUID sellerId) {
        return orderRepo.findByPostingSellerSellerId(sellerId);
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody CreateOrderRequest req) {
        var bundle = bundleRepo.findById(req.getPostingId()).orElse(null);
        if (bundle == null) return ResponseEntity.notFound().build();

        int qty = req.getQuantity() != null ? req.getQuantity() : 1;
        if (!bundle.canReserve(qty)) {
            return ResponseEntity.badRequest().body("Not enough bundles available");
        }

        var org = orgRepo.findById(req.getOrgId()).orElse(null);
        if (org == null) return ResponseEntity.badRequest().body("Organisation not found");

        // Calculate total price
        int pricePerUnit = bundle.getPriceCents();
        if (bundle.getDiscountPct() > 0) {
            pricePerUnit = pricePerUnit - (pricePerUnit * bundle.getDiscountPct() / 100);
        }
        int totalPrice = pricePerUnit * qty;

        OrgOrder order = new OrgOrder();
        order.setOrganisation(org);
        order.setPosting(bundle);
        order.setQuantity(qty);
        order.setTotalPriceCents(totalPrice);

        bundle.setQuantityReserved(bundle.getQuantityReserved() + qty);
        bundleRepo.save(bundle);

        var saved = orderRepo.save(order);

        return ResponseEntity.ok(new OrderResponse(
                saved.getOrderId(),
                saved.getQuantity(),
                saved.getTotalPriceCents(),
                bundle.getPickupStartAt(),
                bundle.getPickupEndAt(),
                bundle.getSeller().getName(),
                bundle.getSeller().getLocationText()
        ));
    }

    @PostMapping("/{id}/collect")
    @org.springframework.transaction.annotation.Transactional
    public ResponseEntity<?> collect(@PathVariable UUID id) {
        var order = orderRepo.findById(id).orElse(null);
        if (order == null) return ResponseEntity.notFound().build();

        if (order.getStatus() != OrgOrder.Status.RESERVED) {
            return ResponseEntity.badRequest().body("Order not in RESERVED status");
        }

        order.setStatus(OrgOrder.Status.COLLECTED);
        order.setCollectedAt(Instant.now());
        orderRepo.save(order);

        // Update org streak - fetch org within transaction
        var org = order.getOrganisation();
        updateOrgStreak(org);

        return ResponseEntity.ok(new CollectResponse(true, "Order collected successfully"));
    }

    @PostMapping("/{id}/cancel")
    @org.springframework.transaction.annotation.Transactional
    public ResponseEntity<?> cancel(@PathVariable UUID id) {
        var order = orderRepo.findById(id).orElse(null);
        if (order == null) return ResponseEntity.notFound().build();

        if (order.getStatus() != OrgOrder.Status.RESERVED) {
            return ResponseEntity.badRequest().body("Can only cancel RESERVED orders");
        }

        order.setStatus(OrgOrder.Status.CANCELLED);
        order.setCancelledAt(Instant.now());

        var bundle = order.getPosting();
        bundle.setQuantityReserved(bundle.getQuantityReserved() - order.getQuantity());
        bundleRepo.save(bundle);

        return ResponseEntity.ok(orderRepo.save(order));
    }

    private void updateOrgStreak(Organisation org) {
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(WeekFields.of(Locale.getDefault()).dayOfWeek(), 1);

        if (org.getLastOrderWeekStart() == null) {
            org.setCurrentStreakWeeks(1);
        } else if (org.getLastOrderWeekStart().equals(weekStart)) {
            // Same week, no change to streak
        } else if (org.getLastOrderWeekStart().plusWeeks(1).equals(weekStart)) {
            org.setCurrentStreakWeeks(org.getCurrentStreakWeeks() + 1);
        } else {
            org.setCurrentStreakWeeks(1);
        }

        org.setLastOrderWeekStart(weekStart);
        org.setTotalOrders(org.getTotalOrders() + 1);

        if (org.getCurrentStreakWeeks() > org.getBestStreakWeeks()) {
            org.setBestStreakWeeks(org.getCurrentStreakWeeks());
        }

        orgRepo.save(org);
    }

    // DTOs
    public static class CreateOrderRequest {
        private UUID postingId;
        private UUID orgId;
        private Integer quantity;

        public UUID getPostingId() { return postingId; }
        public void setPostingId(UUID postingId) { this.postingId = postingId; }
        public UUID getOrgId() { return orgId; }
        public void setOrgId(UUID orgId) { this.orgId = orgId; }
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
    }

    public static class OrderResponse {
        private UUID orderId;
        private Integer quantity;
        private Integer totalPriceCents;
        private Instant pickupStartAt;
        private Instant pickupEndAt;
        private String sellerName;
        private String sellerLocation;

        public OrderResponse(UUID orderId, Integer quantity, Integer totalPriceCents,
                             Instant pickupStartAt, Instant pickupEndAt, String sellerName, String sellerLocation) {
            this.orderId = orderId;
            this.quantity = quantity;
            this.totalPriceCents = totalPriceCents;
            this.pickupStartAt = pickupStartAt;
            this.pickupEndAt = pickupEndAt;
            this.sellerName = sellerName;
            this.sellerLocation = sellerLocation;
        }

        public UUID getOrderId() { return orderId; }
        public Integer getQuantity() { return quantity; }
        public Integer getTotalPriceCents() { return totalPriceCents; }
        public Instant getPickupStartAt() { return pickupStartAt; }
        public Instant getPickupEndAt() { return pickupEndAt; }
        public String getSellerName() { return sellerName; }
        public String getSellerLocation() { return sellerLocation; }
    }

    public static class CollectResponse {
        private boolean success;
        private String message;

        public CollectResponse(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
    }
}
