package zmacadam.recslots.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import zmacadam.recslots.model.TextMessage;

import java.util.List;

public interface MessageRepository extends JpaRepository<TextMessage, Integer> {
    List<TextMessage> findAll();
}
