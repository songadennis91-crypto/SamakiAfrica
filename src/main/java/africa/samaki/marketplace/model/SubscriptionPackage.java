package africa.samaki.marketplace.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "subscription_packages")
public class SubscriptionPackage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code; // e.g. FREE_FISHER, PRO_FISHER, FLEET_COMMERCIAL, ENTERPRISE_BUYER

    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    private String tierTarget; // e.g. "Artisanal Fishers", "Commercial Fleets", "Wholesalers"

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal monthlyPrice;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal annualPrice;

    @Column(nullable = false, length = 10)
    private String currency = "KES";

    @Column(length = 2000)
    private String benefits; // line-delimited or bullet-pointed benefits

    @Column(nullable = false)
    private boolean active = true;

    @Column(precision = 12, scale = 2)
    private BigDecimal promoPrice;

    @Column(precision = 5, scale = 2)
    private BigDecimal discountPercentage;

    private LocalDateTime promoExpiryDate;

    private Integer freeTrialDays = 0;

    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt = LocalDateTime.now();

    public SubscriptionPackage() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getTierTarget() { return tierTarget; }
    public void setTierTarget(String tierTarget) { this.tierTarget = tierTarget; }

    public BigDecimal getMonthlyPrice() { return monthlyPrice; }
    public void setMonthlyPrice(BigDecimal monthlyPrice) { this.monthlyPrice = monthlyPrice; }

    public BigDecimal getAnnualPrice() { return annualPrice; }
    public void setAnnualPrice(BigDecimal annualPrice) { this.annualPrice = annualPrice; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getBenefits() { return benefits; }
    public void setBenefits(String benefits) { this.benefits = benefits; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public BigDecimal getPromoPrice() { return promoPrice; }
    public void setPromoPrice(BigDecimal promoPrice) { this.promoPrice = promoPrice; }

    public BigDecimal getDiscountPercentage() { return discountPercentage; }
    public void setDiscountPercentage(BigDecimal discountPercentage) { this.discountPercentage = discountPercentage; }

    public LocalDateTime getPromoExpiryDate() { return promoExpiryDate; }
    public void setPromoExpiryDate(LocalDateTime promoExpiryDate) { this.promoExpiryDate = promoExpiryDate; }

    public Integer getFreeTrialDays() { return freeTrialDays; }
    public void setFreeTrialDays(Integer freeTrialDays) { this.freeTrialDays = freeTrialDays; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public boolean hasActivePromo() {
        if (promoPrice == null || promoPrice.compareTo(BigDecimal.ZERO) < 0) {
            return false;
        }
        if (promoExpiryDate == null) {
            return true;
        }
        return LocalDateTime.now().isBefore(promoExpiryDate);
    }

    public BigDecimal getEffectiveMonthlyPrice() {
        if (hasActivePromo()) {
            return promoPrice;
        }
        return monthlyPrice;
    }
}
