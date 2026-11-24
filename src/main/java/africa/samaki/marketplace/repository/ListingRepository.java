package africa.samaki.marketplace.repository;

import africa.samaki.marketplace.model.Listing;
import africa.samaki.marketplace.model.ListingStatus;
import africa.samaki.marketplace.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ListingRepository extends JpaRepository<Listing, Long> {

    List<Listing> findAllByOrderByCreatedAtDesc();

    List<Listing> findByStatusNotOrderByCreatedAtDesc(ListingStatus status);

    List<Listing> findTop6ByStatusNotOrderByCreatedAtDesc(ListingStatus status);

    long countByStatusNot(ListingStatus status);

    List<Listing> findByFisherOrderByCreatedAtDesc(User fisher);

    long countByFisher(User fisher);

    @Query("SELECT l FROM Listing l WHERE l.status <> :sold AND (" +
           "LOWER(l.fishType) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(l.fishingGround) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(l.landingSite) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(l.country) LIKE LOWER(CONCAT('%', :q, '%'))) " +
           "ORDER BY l.createdAt DESC")
    List<Listing> search(@Param("q") String q, @Param("sold") ListingStatus sold);
}
