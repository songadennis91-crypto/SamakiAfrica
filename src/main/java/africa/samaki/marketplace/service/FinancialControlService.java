package africa.samaki.marketplace.service;

import africa.samaki.marketplace.model.*;
import africa.samaki.marketplace.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class FinancialControlService {

    private final SubscriptionPackageRepository subscriptionPackageRepository;
    private final CommissionSettingRepository commissionSettingRepository;
    private final PlatformFeeSettingRepository platformFeeSettingRepository;
    private final FinancialAuditLogRepository auditLogRepository;

    public FinancialControlService(SubscriptionPackageRepository subscriptionPackageRepository,
                                   CommissionSettingRepository commissionSettingRepository,
                                   PlatformFeeSettingRepository platformFeeSettingRepository,
                                   FinancialAuditLogRepository auditLogRepository) {
        this.subscriptionPackageRepository = subscriptionPackageRepository;
        this.commissionSettingRepository = commissionSettingRepository;
        this.platformFeeSettingRepository = platformFeeSettingRepository;
        this.auditLogRepository = auditLogRepository;
    }

    // ==========================================
    // 1. SUBSCRIPTION PACKAGES MANAGEMENT
    // ==========================================

    public List<SubscriptionPackage> getAllPackages() {
        return subscriptionPackageRepository.findAllByOrderByMonthlyPriceAsc();
    }

    public List<SubscriptionPackage> getActivePackages() {
        return subscriptionPackageRepository.findByActiveTrueOrderByMonthlyPriceAsc();
    }

    public Optional<SubscriptionPackage> getPackageById(Long id) {
        return subscriptionPackageRepository.findById(id);
    }

    @Transactional
    public SubscriptionPackage savePackage(SubscriptionPackage input, String reason, String changedBy) {
        if (input.getMonthlyPrice() == null || input.getMonthlyPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Monthly price cannot be negative.");
        }
        if (input.getAnnualPrice() == null || input.getAnnualPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Annual price cannot be negative.");
        }
        if (input.getPromoPrice() != null && input.getPromoPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Promotional price cannot be negative.");
        }

        String action = (input.getId() == null) ? "CREATE" : "UPDATE";
        String prevVal = "None (New)";

        SubscriptionPackage target;
        if (input.getId() != null) {
            target = subscriptionPackageRepository.findById(input.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Package not found with id: " + input.getId()));
            prevVal = String.format("Name: %s | Monthly: %s %s | Annual: %s %s | Active: %b | Promo: %s",
                    target.getName(), target.getMonthlyPrice(), target.getCurrency(),
                    target.getAnnualPrice(), target.getCurrency(), target.isActive(), target.getPromoPrice());

            target.setName(input.getName());
            target.setDescription(input.getDescription());
            target.setTierTarget(input.getTierTarget());
            target.setMonthlyPrice(input.getMonthlyPrice());
            target.setAnnualPrice(input.getAnnualPrice());
            target.setCurrency(input.getCurrency() != null ? input.getCurrency().trim().toUpperCase() : "KES");
            target.setBenefits(input.getBenefits());
            target.setActive(input.isActive());
            target.setPromoPrice(input.getPromoPrice());
            target.setDiscountPercentage(input.getDiscountPercentage());
            target.setPromoExpiryDate(input.getPromoExpiryDate());
            target.setFreeTrialDays(input.getFreeTrialDays() != null ? input.getFreeTrialDays() : 0);
            target.setUpdatedAt(LocalDateTime.now());
        } else {
            target = input;
            if (target.getCode() == null || target.getCode().isBlank()) {
                target.setCode(target.getName().toUpperCase().replaceAll("[^A-Z0-9]+", "_"));
            }
            if (target.getCurrency() == null) {
                target.setCurrency("KES");
            }
            target.setCreatedAt(LocalDateTime.now());
            target.setUpdatedAt(LocalDateTime.now());
        }

        SubscriptionPackage saved = subscriptionPackageRepository.save(target);

        String newVal = String.format("Name: %s | Monthly: %s %s | Annual: %s %s | Active: %b | Promo: %s",
                saved.getName(), saved.getMonthlyPrice(), saved.getCurrency(),
                saved.getAnnualPrice(), saved.getCurrency(), saved.isActive(), saved.getPromoPrice());

        logAudit("SUBSCRIPTION", saved.getCode(), action, prevVal, newVal, reason, changedBy, "GLOBAL (ALL)");
        return saved;
    }

    @Transactional
    public boolean togglePackageStatus(Long id, String reason, String changedBy) {
        SubscriptionPackage pkg = subscriptionPackageRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Package not found: " + id));
        boolean oldState = pkg.isActive();
        pkg.setActive(!oldState);
        pkg.setUpdatedAt(LocalDateTime.now());
        subscriptionPackageRepository.save(pkg);

        logAudit("SUBSCRIPTION", pkg.getCode(), "TOGGLE_STATUS",
                "Active: " + oldState, "Active: " + pkg.isActive(),
                reason != null && !reason.isBlank() ? reason : "Toggled subscription package activation status",
                changedBy, "GLOBAL (ALL)");
        return pkg.isActive();
    }

    // ==========================================
    // 2. COMMISSION MANAGEMENT
    // ==========================================

    public List<CommissionSetting> getAllCommissionSettings() {
        return commissionSettingRepository.findAllByOrderByCategoryAscCountryCodeAsc();
    }

    public List<CommissionSetting> getBaseCommissionSettings() {
        return commissionSettingRepository.findByCountryCodeOrderByCategoryAsc("ALL");
    }

    public List<CommissionSetting> getCountryOverrides() {
        return commissionSettingRepository.findByCountryCodeNotOrderByCountryCodeAscCategoryAsc("ALL");
    }

    public Optional<CommissionSetting> getCommissionSettingById(Long id) {
        return commissionSettingRepository.findById(id);
    }

    @Transactional
    public CommissionSetting saveCommissionSetting(CommissionSetting input, String reason, String changedBy) {
        if (input.getRatePercentage() == null ||
            input.getRatePercentage().compareTo(BigDecimal.ZERO) < 0 ||
            input.getRatePercentage().compareTo(new BigDecimal("100.00")) > 0) {
            throw new IllegalArgumentException("Commission percentage must be between 0.00% and 100.00%.");
        }
        if (input.getMinAmount() != null && input.getMaxAmount() != null &&
            input.getMinAmount().compareTo(input.getMaxAmount()) > 0) {
            throw new IllegalArgumentException("Minimum commission amount cannot exceed maximum commission amount.");
        }

        String action = (input.getId() == null) ? "CREATE" : "UPDATE";
        String prevVal = "None (New)";

        CommissionSetting target;
        if (input.getId() != null) {
            target = commissionSettingRepository.findById(input.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Commission setting not found with id: " + input.getId()));
            prevVal = String.format("Category: %s | Scope: %s | Rate: %s%% | Min: %s | Max: %s | Active: %b",
                    target.getCategory(), target.getCountryCode(), target.getRatePercentage(),
                    target.getMinAmount(), target.getMaxAmount(), target.isActive());

            target.setRatePercentage(input.getRatePercentage());
            target.setMinAmount(input.getMinAmount());
            target.setMaxAmount(input.getMaxAmount());
            if (input.getCurrency() != null && !input.getCurrency().isBlank()) {
                target.setCurrency(input.getCurrency().trim().toUpperCase());
            }
            target.setActive(input.isActive());
            target.setNotes(input.getNotes());
            target.setUpdatedAt(LocalDateTime.now());
            target.setUpdatedBy(changedBy);
        } else {
            target = input;
            if (target.getCountryCode() == null || target.getCountryCode().isBlank()) {
                target.setCountryCode("ALL");
            }
            if (target.getCurrency() == null) {
                target.setCurrency("KES");
            }
            target.setUpdatedAt(LocalDateTime.now());
            target.setUpdatedBy(changedBy);
        }

        CommissionSetting saved = commissionSettingRepository.save(target);

        String newVal = String.format("Category: %s | Scope: %s | Rate: %s%% | Min: %s | Max: %s | Active: %b",
                saved.getCategory(), saved.getCountryCode(), saved.getRatePercentage(),
                saved.getMinAmount(), saved.getMaxAmount(), saved.isActive());

        String domain = "ALL".equalsIgnoreCase(saved.getCountryCode()) ? "COMMISSION" : "COUNTRY_OVERRIDE";
        logAudit(domain, saved.getCategory() + " (" + saved.getCountryCode() + ")", action,
                prevVal, newVal, reason, changedBy, saved.getCountryCode());

        return saved;
    }

    @Transactional
    public boolean toggleCommissionStatus(Long id, String reason, String changedBy) {
        CommissionSetting setting = commissionSettingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Commission setting not found: " + id));
        boolean oldState = setting.isActive();
        setting.setActive(!oldState);
        setting.setUpdatedAt(LocalDateTime.now());
        setting.setUpdatedBy(changedBy);
        commissionSettingRepository.save(setting);

        String domain = "ALL".equalsIgnoreCase(setting.getCountryCode()) ? "COMMISSION" : "COUNTRY_OVERRIDE";
        logAudit(domain, setting.getCategory() + " (" + setting.getCountryCode() + ")", "TOGGLE_STATUS",
                "Active: " + oldState, "Active: " + setting.isActive(),
                reason != null && !reason.isBlank() ? reason : "Toggled commission activation status",
                changedBy, setting.getCountryCode());
        return setting.isActive();
    }

    // Dynamic Server-Side Calculation (Never trusts frontend commission values)
    public CommissionCalculationResult calculateCommission(String category, String country, BigDecimal grossAmount) {
        if (grossAmount == null || grossAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return new CommissionCalculationResult(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                    BigDecimal.ZERO, "None", "No transaction amount", "KES");
        }

        String safeCategory = (category != null && !category.isBlank()) ? category.trim() : "FISH_MARKETPLACE";
        String safeCountry = (country != null && !country.isBlank()) ? country.trim() : "Kenya";

        // Try exact country override first
        Optional<CommissionSetting> countryMatch = commissionSettingRepository
                .findByCategoryAndCountryCode(safeCategory, safeCountry)
                .filter(CommissionSetting::isActive);

        CommissionSetting effectiveSetting;
        String ruleApplied;

        if (countryMatch.isPresent()) {
            effectiveSetting = countryMatch.get();
            ruleApplied = "Country Override (" + safeCountry + ")";
        } else {
            // Fallback to pan-African base rate
            effectiveSetting = commissionSettingRepository
                    .findByCategoryAndCountryCode(safeCategory, "ALL")
                    .filter(CommissionSetting::isActive)
                    .orElseGet(() -> {
                        // Safe fallback rule if no row exists in DB
                        CommissionSetting fallback = new CommissionSetting();
                        fallback.setRatePercentage(new BigDecimal("3.50"));
                        fallback.setCurrency("KES");
                        fallback.setCategory(safeCategory);
                        return fallback;
                    });
            ruleApplied = "Pan-African Base Rate (ALL)";
        }

        BigDecimal rate = effectiveSetting.getRatePercentage();
        BigDecimal rawCommission = grossAmount.multiply(rate)
                .divide(new BigDecimal("100.00"), 2, RoundingMode.HALF_UP);

        BigDecimal finalCommission = rawCommission;

        if (effectiveSetting.getMinAmount() != null && finalCommission.compareTo(effectiveSetting.getMinAmount()) < 0) {
            finalCommission = effectiveSetting.getMinAmount();
            ruleApplied += " [Min floor applied]";
        }
        if (effectiveSetting.getMaxAmount() != null && finalCommission.compareTo(effectiveSetting.getMaxAmount()) > 0) {
            finalCommission = effectiveSetting.getMaxAmount();
            ruleApplied += " [Max cap applied]";
        }

        BigDecimal netPayout = grossAmount.subtract(finalCommission);
        String currency = (effectiveSetting.getCurrency() != null) ? effectiveSetting.getCurrency() : "KES";

        return new CommissionCalculationResult(grossAmount, rate, finalCommission, netPayout,
                effectiveSetting.getCategory(), ruleApplied, currency);
    }

    public record CommissionCalculationResult(
            BigDecimal grossAmount,
            BigDecimal effectiveRatePercentage,
            BigDecimal commissionAmount,
            BigDecimal netPayout,
            String category,
            String ruleApplied,
            String currency
    ) {}

    // ==========================================
    // 3. PLATFORM FEE & ADVERTISING MANAGEMENT
    // ==========================================

    public List<PlatformFeeSetting> getAllFees() {
        return platformFeeSettingRepository.findAllByOrderByFeeCategoryAscNameAsc();
    }

    public List<PlatformFeeSetting> getFeesByCategory(String feeCategory) {
        return platformFeeSettingRepository.findByFeeCategoryOrderByNameAsc(feeCategory);
    }

    public Optional<PlatformFeeSetting> getFeeById(Long id) {
        return platformFeeSettingRepository.findById(id);
    }

    public Optional<PlatformFeeSetting> getFeeByKey(String feeKey) {
        return platformFeeSettingRepository.findByFeeKey(feeKey);
    }

    @Transactional
    public PlatformFeeSetting saveFee(PlatformFeeSetting input, String reason, String changedBy) {
        if (input.getAmount() == null || input.getAmount().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Fee amount cannot be negative.");
        }

        String action = (input.getId() == null) ? "CREATE" : "UPDATE";
        String prevVal = "None (New)";

        PlatformFeeSetting target;
        if (input.getId() != null) {
            target = platformFeeSettingRepository.findById(input.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Fee setting not found with id: " + input.getId()));
            prevVal = String.format("Name: %s | Key: %s | Category: %s | Amount: %s %s (%s) | Active: %b",
                    target.getName(), target.getFeeKey(), target.getFeeCategory(),
                    target.getAmount(), target.getCurrency(), target.getBillingUnit(), target.isActive());

            target.setName(input.getName());
            target.setDescription(input.getDescription());
            target.setAmount(input.getAmount());
            if (input.getCurrency() != null && !input.getCurrency().isBlank()) {
                target.setCurrency(input.getCurrency().trim().toUpperCase());
            }
            if (input.getBillingUnit() != null && !input.getBillingUnit().isBlank()) {
                target.setBillingUnit(input.getBillingUnit().trim());
            }
            target.setActive(input.isActive());
            target.setUpdatedAt(LocalDateTime.now());
            target.setUpdatedBy(changedBy);
        } else {
            target = input;
            if (target.getCurrency() == null) {
                target.setCurrency("KES");
            }
            target.setUpdatedAt(LocalDateTime.now());
            target.setUpdatedBy(changedBy);
        }

        PlatformFeeSetting saved = platformFeeSettingRepository.save(target);

        String newVal = String.format("Name: %s | Key: %s | Category: %s | Amount: %s %s (%s) | Active: %b",
                saved.getName(), saved.getFeeKey(), saved.getFeeCategory(),
                saved.getAmount(), saved.getCurrency(), saved.getBillingUnit(), saved.isActive());

        String domain = "ADVERTISING".equalsIgnoreCase(saved.getFeeCategory()) ? "ADVERTISING" : "SERVICE_FEE";
        logAudit(domain, saved.getFeeKey(), action, prevVal, newVal, reason, changedBy, "GLOBAL (ALL)");

        return saved;
    }

    @Transactional
    public boolean toggleFeeStatus(Long id, String reason, String changedBy) {
        PlatformFeeSetting fee = platformFeeSettingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Fee setting not found: " + id));
        boolean oldState = fee.isActive();
        fee.setActive(!oldState);
        fee.setUpdatedAt(LocalDateTime.now());
        fee.setUpdatedBy(changedBy);
        platformFeeSettingRepository.save(fee);

        String domain = "ADVERTISING".equalsIgnoreCase(fee.getFeeCategory()) ? "ADVERTISING" : "SERVICE_FEE";
        logAudit(domain, fee.getFeeKey(), "TOGGLE_STATUS",
                "Active: " + oldState, "Active: " + fee.isActive(),
                reason != null && !reason.isBlank() ? reason : "Toggled fee activation status",
                changedBy, "GLOBAL (ALL)");
        return fee.isActive();
    }

    // ==========================================
    // 4. FINANCIAL AUDIT HISTORY
    // ==========================================

    public List<FinancialAuditLog> getAuditLogs(String query) {
        if (query == null || query.isBlank()) {
            return auditLogRepository.findTop100ByOrderByTimestampDesc();
        }
        return auditLogRepository.searchAuditLogs(query.trim());
    }

    @Transactional
    public void logAudit(String domain, String targetIdentifier, String action,
                         String previousValue, String newValue, String reason,
                         String changedBy, String scopeCountry) {
        FinancialAuditLog log = new FinancialAuditLog(
                domain,
                targetIdentifier,
                action,
                previousValue,
                newValue,
                (reason != null && !reason.isBlank()) ? reason : "Direct owner adjustment via Financial Control Centre",
                (changedBy != null && !changedBy.isBlank()) ? changedBy : "System / Platform Owner",
                (scopeCountry != null && !scopeCountry.isBlank()) ? scopeCountry : "GLOBAL (ALL)"
        );
        auditLogRepository.save(log);
    }
}
