package zmacadam.recslots.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import zmacadam.recslots.model.Date;
import zmacadam.recslots.repository.DayRepository;

@Service
public class DateService {

    private DayRepository dayRepository;

    @Autowired
    public DateService(DayRepository dayRepository) {
        this.dayRepository = dayRepository;
    }

    public Date findByDate(String date) {
        return dayRepository.findByDate(date);
    }

    public Date saveDate(Date date) {
        return dayRepository.save(date);
    }
}
