package africa.samaki.marketplace.repository;

import africa.samaki.marketplace.model.PasswordResetToken;
import africa.samaki.marketplace.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByToken(String token);
    List<PasswordResetToken> findByUserOrderByCreatedAtDesc(User user);
    void deleteByExpiryDateBefore(LocalDateTime dateTime);
}
