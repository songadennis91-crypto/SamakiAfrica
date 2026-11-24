package africa.samaki.marketplace.repository;

import africa.samaki.marketplace.model.SubscriptionPackage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionPackageRepository extends JpaRepository<SubscriptionPackage, Long> {
    Optional<SubscriptionPackage> findByCode(String code);
    List<SubscriptionPackage> findAllByOrderByMonthlyPriceAsc();
    List<SubscriptionPackage> findByActiveTrueOrderByMonthlyPriceAsc();
}
