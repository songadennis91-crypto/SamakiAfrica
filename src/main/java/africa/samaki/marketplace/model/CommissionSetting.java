package africa.samaki.marketplace.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "commission_settings", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"category", "countryCode"})
})
public class CommissionSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // FISH_MARKETPLACE, EQUIPMENT_MARKETPLACE, SUPPLIER_COMMISSION, COLD_CHAIN_LOGISTICS, OTHER_TRANSACTIONS
    @Column(nullable = false, length = 60)
    private String category;

    @Column(nullable = false)
    private String categoryLabel;

    // "ALL" indicates default / pan-African base rate. Otherwise country name (e.g. Kenya, Tanzania, Nigeria)
    @Column(nullable = false, length = 80)
    private String countryCode = "ALL";

    @Column(nullable = false, precision = 6, scale = 2)
    private BigDecimal ratePercentage;

    @Column(precision = 12, scale = 2)
    private BigDecimal minAmount;

    @Column(precision = 12, scale = 2)
    private BigDecimal maxAmount;

    @Column(nullable = false, length = 10)
    private String currency = "KES";

    @Column(nullable = false)
    private boolean active = true;

    @Column(length = 500)
    private String notes;

    private LocalDateTime updatedAt = LocalDateTime.now();

    private String updatedBy;

    public CommissionSetting() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getCategoryLabel() { return categoryLabel; }
    public void setCategoryLabel(String categoryLabel) { this.categoryLabel = categoryLabel; }

    public String getCountryCode() { return countryCode; }
    public void setCountryCode(String countryCode) { this.countryCode = countryCode; }

    public BigDecimal getRatePercentage() { return ratePercentage; }
    public void setRatePercentage(BigDecimal ratePercentage) { this.ratePercentage = ratePercentage; }

    public BigDecimal getMinAmount() { return minAmount; }
    public void setMinAmount(BigDecimal minAmount) { this.minAmount = minAmount; }

    public BigDecimal getMaxAmount() { return maxAmount; }
    public void setMaxAmount(BigDecimal maxAmount) { this.maxAmount = maxAmount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
}
