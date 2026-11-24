package africa.samaki.marketplace;

import africa.samaki.marketplace.model.*;
import africa.samaki.marketplace.repository.*;
import africa.samaki.marketplace.service.FinancialControlService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class FinancialControlServiceTests {

    @Autowired
    private FinancialControlService financialService;

    @Autowired
    private SubscriptionPackageRepository packageRepo;

    @Autowired
    private CommissionSettingRepository commissionRepo;

    @Autowired
    private FinancialAuditLogRepository auditRepo;

    @Test
    @DisplayName("Should retrieve seeded subscription packages with non-hardcoded prices")
    void testGetSubscriptionPackages() {
        List<SubscriptionPackage> packages = financialService.getAllPackages();
        assertFalse(packages.isEmpty(), "Subscription packages should be populated");
        
        SubscriptionPackage pro = packageRepo.findByCode("PRO_FISHER").orElse(null);
        assertNotNull(pro);
        assertEquals(new BigDecimal("1500.00"), pro.getMonthlyPrice());
        assertEquals(new BigDecimal("15000.00"), pro.getAnnualPrice());
        assertTrue(pro.hasActivePromo());
    }

    @Test
    @DisplayName("Should dynamically update subscription price and record forensic audit trail")
    void testUpdateSubscriptionPricingWithAudit() {
        SubscriptionPackage pro = packageRepo.findByCode("PRO_FISHER").orElseThrow();
        BigDecimal newMonthly = new BigDecimal("1850.00");
        pro.setMonthlyPrice(newMonthly);

        financialService.savePackage(pro, "Q2 Inflation & Infrastructure Adjustment", "owner@samaki.africa");

        SubscriptionPackage updated = packageRepo.findByCode("PRO_FISHER").orElseThrow();
        assertEquals(newMonthly, updated.getMonthlyPrice());

        List<FinancialAuditLog> logs = financialService.getAuditLogs("Inflation");
        assertFalse(logs.isEmpty(), "Audit log must record the price change reason");
        FinancialAuditLog latestLog = logs.get(0);
        assertEquals("owner@samaki.africa", latestLog.getChangedBy());
        assertEquals("SUBSCRIPTION", latestLog.getDomain());
        assertTrue(latestLog.getNewValue().contains("1850.00"));
    }

    @Test
    @DisplayName("Should validate commission rate bounds (0.00% to 100.00%)")
    void testCommissionValidation() {
        CommissionSetting invalidSetting = new CommissionSetting();
        invalidSetting.setCategory("FISH_MARKETPLACE");
        invalidSetting.setCategoryLabel("Fish Catch Marketplace");
        invalidSetting.setCountryCode("Ghana");
        invalidSetting.setRatePercentage(new BigDecimal("105.00")); // Invalid > 100%

        assertThrows(IllegalArgumentException.class, () -> {
            financialService.saveCommissionSetting(invalidSetting, "Test invalid rate", "owner@samaki.africa");
        });
    }

    @Test
    @DisplayName("Should calculate dynamic commission using country-specific override and base fallback")
    void testCalculateCommission() {
        // Kenya has 3.00% override with 20 KES min and 12,000 KES max
        BigDecimal grossKenya = new BigDecimal("100000.00");
        FinancialControlService.CommissionCalculationResult resKenya =
                financialService.calculateCommission("FISH_MARKETPLACE", "Kenya", grossKenya);

        assertEquals(new BigDecimal("3.00"), resKenya.effectiveRatePercentage());
        assertEquals(new BigDecimal("3000.00"), resKenya.commissionAmount());
        assertEquals(new BigDecimal("97000.00"), resKenya.netPayout());
        assertTrue(resKenya.ruleApplied().contains("Kenya"));

        // Unlisted country falls back to 3.50% Pan-African Base Rate
        BigDecimal grossGhana = new BigDecimal("10000.00");
        FinancialControlService.CommissionCalculationResult resGhana =
                financialService.calculateCommission("FISH_MARKETPLACE", "Ghana", grossGhana);

        assertEquals(new BigDecimal("3.50"), resGhana.effectiveRatePercentage());
        assertEquals(new BigDecimal("350.00"), resGhana.commissionAmount());
        assertEquals(new BigDecimal("9650.00"), resGhana.netPayout());
        assertTrue(resGhana.ruleApplied().contains("Pan-African Base Rate"));
    }

    @Test
    @DisplayName("Should clamp commission to configured min floor and max cap")
    void testCommissionClamping() {
        // Very small transaction: 100 KES in Kenya -> 3% = 3 KES, but min floor is 20 KES
        FinancialControlService.CommissionCalculationResult small =
                financialService.calculateCommission("FISH_MARKETPLACE", "Kenya", new BigDecimal("100.00"));
        assertEquals(new BigDecimal("20.00"), small.commissionAmount(), "Should apply 20 KES minimum floor");

        // Very large transaction: 1,000,000 KES in Kenya -> 3% = 30,000 KES, but max cap is 12,000 KES
        FinancialControlService.CommissionCalculationResult large =
                financialService.calculateCommission("FISH_MARKETPLACE", "Kenya", new BigDecimal("1000000.00"));
        assertEquals(new BigDecimal("12000.00"), large.commissionAmount(), "Should apply 12,000 KES maximum cap");
    }
}
