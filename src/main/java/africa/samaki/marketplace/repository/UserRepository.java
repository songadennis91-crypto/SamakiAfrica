package africa.samaki.marketplace.repository;

import africa.samaki.marketplace.model.Role;
import africa.samaki.marketplace.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByPhone(String phone);
    Optional<User> findByEmail(String email);
    boolean existsByPhone(String phone);
    boolean existsByEmail(String email);

    List<User> findByRoleInOrderByFullNameAsc(List<Role> roles);
    List<User> findAllByOrderByFullNameAsc();

    @Query("SELECT u FROM User u WHERE " +
           "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(u.phone) LIKE LOWER(CONCAT('%', :q, '%')) " +
           "ORDER BY u.fullName ASC")
    List<User> searchUsers(@Param("q") String query);
}
