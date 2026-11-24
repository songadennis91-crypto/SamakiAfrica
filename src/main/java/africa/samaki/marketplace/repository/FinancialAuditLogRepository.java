package africa.samaki.marketplace.repository;

import africa.samaki.marketplace.model.FinancialAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FinancialAuditLogRepository extends JpaRepository<FinancialAuditLog, Long> {
    List<FinancialAuditLog> findAllByOrderByTimestampDesc();
    List<FinancialAuditLog> findTop100ByOrderByTimestampDesc();
    List<FinancialAuditLog> findByDomainOrderByTimestampDesc(String domain);

    @Query("SELECT a FROM FinancialAuditLog a WHERE " +
           "LOWER(a.domain) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(a.targetIdentifier) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(a.changedBy) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(a.reason) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(a.scopeCountry) LIKE LOWER(CONCAT('%', :q, '%')) " +
           "ORDER BY a.timestamp DESC")
    List<FinancialAuditLog> searchAuditLogs(@Param("q") String query);
}
