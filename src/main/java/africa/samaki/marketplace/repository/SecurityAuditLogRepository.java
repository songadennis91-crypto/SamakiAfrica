package africa.samaki.marketplace.repository;

import africa.samaki.marketplace.model.SecurityAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SecurityAuditLogRepository extends JpaRepository<SecurityAuditLog, Long> {
    List<SecurityAuditLog> findTop100ByOrderByTimestampDesc();
    List<SecurityAuditLog> findByTargetUserEmailOrderByTimestampDesc(String email);

    @Query("SELECT s FROM SecurityAuditLog s WHERE " +
           "LOWER(s.eventType) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(s.targetUserEmail) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(s.performedBy) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(s.details) LIKE LOWER(CONCAT('%', :q, '%')) " +
           "ORDER BY s.timestamp DESC")
    List<SecurityAuditLog> searchSecurityLogs(@Param("q") String query);
}
