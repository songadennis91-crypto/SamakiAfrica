package africa.samaki.marketplace.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_login_sessions")
public class UserLoginSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String sessionId;

    @ManyToOne(optional = false)
    private User user;

    private String ipAddress;

    @Column(length = 500)
    private String userAgent;

    private String deviceType = "Desktop"; // "Desktop", "Mobile", "Tablet"

    private LocalDateTime loginTime = LocalDateTime.now();

    private LocalDateTime lastAccessTime = LocalDateTime.now();

    @Column(nullable = false)
    private boolean active = true;

    private String revokedBy;

    private LocalDateTime revokedAt;

    public UserLoginSession() {}

    public UserLoginSession(String sessionId, User user, String ipAddress, String userAgent, String deviceType) {
        this.sessionId = sessionId;
        this.user = user;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.deviceType = deviceType != null ? deviceType : "Desktop";
        this.loginTime = LocalDateTime.now();
        this.lastAccessTime = LocalDateTime.now();
        this.active = true;
    }

    public Long getId() { return id; }
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }

    public String getDeviceType() { return deviceType; }
    public void setDeviceType(String deviceType) { this.deviceType = deviceType; }

    public LocalDateTime getLoginTime() { return loginTime; }
    public void setLoginTime(LocalDateTime loginTime) { this.loginTime = loginTime; }

    public LocalDateTime getLastAccessTime() { return lastAccessTime; }
    public void setLastAccessTime(LocalDateTime lastAccessTime) { this.lastAccessTime = lastAccessTime; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public String getRevokedBy() { return revokedBy; }
    public void setRevokedBy(String revokedBy) { this.revokedBy = revokedBy; }

    public LocalDateTime getRevokedAt() { return revokedAt; }
    public void setRevokedAt(LocalDateTime revokedAt) { this.revokedAt = revokedAt; }

    public void revoke(String revokedBy) {
        this.active = false;
        this.revokedBy = revokedBy;
        this.revokedAt = LocalDateTime.now();
    }
}
