package com.byteme.app;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface OrgOrderRepository extends JpaRepository<OrgOrder, UUID> {
    List<OrgOrder> findByOrganisationOrgId(UUID orgId);
    List<OrgOrder> findByOrganisationOrgIdAndStatus(UUID orgId, OrgOrder.Status status);
    List<OrgOrder> findByPostingSellerSellerId(UUID sellerId);
}
