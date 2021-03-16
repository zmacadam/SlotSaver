package zmacadam.recslots.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import zmacadam.recslots.model.User;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findByPhoneNumber(String phoneNumber);
    User findByUserName(String userName);
    List<User> findByActiveTrue();
    List<User> findByPaidTrue();
}
