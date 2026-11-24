package africa.samaki.marketplace.repository;

import africa.samaki.marketplace.model.PlatformFeeSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlatformFeeSettingRepository extends JpaRepository<PlatformFeeSetting, Long> {
    Optional<PlatformFeeSetting> findByFeeKey(String feeKey);
    List<PlatformFeeSetting> findByFeeCategoryOrderByNameAsc(String feeCategory);
    List<PlatformFeeSetting> findAllByOrderByFeeCategoryAscNameAsc();
}
