package africa.samaki.marketplace.repository;

import africa.samaki.marketplace.model.User;
import africa.samaki.marketplace.model.UserLoginSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserLoginSessionRepository extends JpaRepository<UserLoginSession, Long> {
    Optional<UserLoginSession> findBySessionId(String sessionId);
    List<UserLoginSession> findByUserAndActiveTrueOrderByLastAccessTimeDesc(User user);
    List<UserLoginSession> findByUserOrderByLoginTimeDesc(User user);
    List<UserLoginSession> findAllByActiveTrueOrderByLastAccessTimeDesc();
    List<UserLoginSession> findTop100ByOrderByLastAccessTimeDesc();
}
