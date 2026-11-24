package africa.samaki.marketplace.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "financial_audit_logs")
public class FinancialAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // SUBSCRIPTION, COMMISSION, SERVICE_FEE, ADVERTISING, COUNTRY_OVERRIDE
    @Column(nullable = false, length = 60)
    private String domain;

    @Column(nullable = false, length = 120)
    private String targetIdentifier;

    @Column(nullable = false, length = 50)
    private String action; // CREATE, UPDATE, TOGGLE_STATUS, DELETE

    @Column(columnDefinition = "TEXT")
    private String previousValue;

    @Column(columnDefinition = "TEXT")
    private String newValue;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @Column(nullable = false, length = 120)
    private String changedBy;

    @Column(length = 80)
    private String scopeCountry;

    @Column(nullable = false)
    private LocalDateTime timestamp = LocalDateTime.now();

    public FinancialAuditLog() {}

    public FinancialAuditLog(String domain, String targetIdentifier, String action,
                             String previousValue, String newValue, String reason,
                             String changedBy, String scopeCountry) {
        this.domain = domain;
        this.targetIdentifier = targetIdentifier;
        this.action = action;
        this.previousValue = previousValue;
        this.newValue = newValue;
        this.reason = reason;
        this.changedBy = changedBy;
        this.scopeCountry = scopeCountry != null ? scopeCountry : "GLOBAL (ALL)";
        this.timestamp = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public String getDomain() { return domain; }
    public void setDomain(String domain) { this.domain = domain; }

    public String getTargetIdentifier() { return targetIdentifier; }
    public void setTargetIdentifier(String targetIdentifier) { this.targetIdentifier = targetIdentifier; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getPreviousValue() { return previousValue; }
    public void setPreviousValue(String previousValue) { this.previousValue = previousValue; }

    public String getNewValue() { return newValue; }
    public void setNewValue(String newValue) { this.newValue = newValue; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getChangedBy() { return changedBy; }
    public void setChangedBy(String changedBy) { this.changedBy = changedBy; }

    public String getScopeCountry() { return scopeCountry; }
    public void setScopeCountry(String scopeCountry) { this.scopeCountry = scopeCountry; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
