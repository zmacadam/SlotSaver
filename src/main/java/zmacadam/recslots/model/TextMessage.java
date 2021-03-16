package zmacadam.recslots.model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "message")
public class TextMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    private String date;
    private String slot;
    private String day;

    public TextMessage(String date, String slotType, String dayNumber) {
        this.date = date;
        this.slot = slotType;
        this.day = dayNumber;
    }
}
