
package africa.samaki.marketplace.repository;

import africa.samaki.marketplace.model.Order;
import africa.samaki.marketplace.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByBuyerOrderByCreatedAtDesc(User buyer);
    List<Order> findByListing_FisherOrderByCreatedAtDesc(User fisher);
}