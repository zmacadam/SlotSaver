package zmacadam.recslots.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import zmacadam.recslots.model.Date;

public interface DayRepository extends JpaRepository<Date, Integer> {
    Date findByDate(String date);
}
