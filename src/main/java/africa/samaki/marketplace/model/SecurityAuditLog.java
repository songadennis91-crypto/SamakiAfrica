package africa.samaki.marketplace.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "security_audit_logs")
public class SecurityAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // PASSWORD_CHANGE, PASSWORD_RESET_REQUEST, PASSWORD_RESET_SUCCESS, ADMIN_RESET_INITIATED,
    // 2FA_ENABLED, 2FA_DISABLED, ADMIN_CREATED, ADMIN_ROLE_CHANGED, ADMIN_STATUS_TOGGLED, SESSION_REVOKED
    @Column(nullable = false, length = 60)
    private String eventType;

    @Column(length = 120)
    private String targetUserEmail;

    @Column(nullable = false, length = 120)
    private String performedBy;

    private String ipAddress;

    @Column(nullable = false, length = 30)
    private String status = "SUCCESS"; // "SUCCESS", "FAILED"

    @Column(columnDefinition = "TEXT")
    private String details;

    @Column(nullable = false)
    private LocalDateTime timestamp = LocalDateTime.now();

    public SecurityAuditLog() {}

    public SecurityAuditLog(String eventType, String targetUserEmail, String performedBy,
                            String ipAddress, String status, String details) {
        this.eventType = eventType;
        this.targetUserEmail = targetUserEmail;
        this.performedBy = performedBy;
        this.ipAddress = ipAddress != null ? ipAddress : "127.0.0.1";
        this.status = status;
        this.details = details;
        this.timestamp = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public String getTargetUserEmail() { return targetUserEmail; }
    public void setTargetUserEmail(String targetUserEmail) { this.targetUserEmail = targetUserEmail; }

    public String getPerformedBy() { return performedBy; }
    public void setPerformedBy(String performedBy) { this.performedBy = performedBy; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
