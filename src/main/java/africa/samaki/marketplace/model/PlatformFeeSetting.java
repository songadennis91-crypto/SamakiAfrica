package africa.samaki.marketplace.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "platform_fee_settings")
public class PlatformFeeSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // SERVICE_CHARGE, ADVERTISING, AI_PREMIUM, LOGISTICS
    @Column(nullable = false, length = 60)
    private String feeCategory;

    @Column(nullable = false, unique = true, length = 80)
    private String feeKey;

    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 10)
    private String currency = "KES";

    // PER_POST, PER_DAY, PER_ORDER, PER_MONTH
    @Column(nullable = false, length = 40)
    private String billingUnit = "PER_POST";

    @Column(nullable = false)
    private boolean active = true;

    private LocalDateTime updatedAt = LocalDateTime.now();

    private String updatedBy;

    public PlatformFeeSetting() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFeeCategory() { return feeCategory; }
    public void setFeeCategory(String feeCategory) { this.feeCategory = feeCategory; }

    public String getFeeKey() { return feeKey; }
    public void setFeeKey(String feeKey) { this.feeKey = feeKey; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getBillingUnit() { return billingUnit; }
    public void setBillingUnit(String billingUnit) { this.billingUnit = billingUnit; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
}
