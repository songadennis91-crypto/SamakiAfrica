package africa.samaki.marketplace.repository;

import africa.samaki.marketplace.model.CommissionSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommissionSettingRepository extends JpaRepository<CommissionSetting, Long> {
    Optional<CommissionSetting> findByCategoryAndCountryCode(String category, String countryCode);
    List<CommissionSetting> findByCategoryOrderByCountryCodeAsc(String category);
    List<CommissionSetting> findByCountryCodeOrderByCategoryAsc(String countryCode);
    List<CommissionSetting> findAllByOrderByCategoryAscCountryCodeAsc();
    List<CommissionSetting> findByCountryCodeNotOrderByCountryCodeAscCategoryAsc(String countryCode);
}
