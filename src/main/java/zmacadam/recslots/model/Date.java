package zmacadam.recslots.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.lang.reflect.Field;
import java.util.HashMap;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "date")
public class Date {

    public Date(String date) {
        this.date = date;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    private String date;
    private int slot1;
    private int slot2;
    private int slot3;
    private int slot4;
    private int count1;
    private int count2;
    private int count3;
    private int count4;

    @Transient
    private final HashMap<String, String> slotMap = new HashMap<>() {{
        put("6 - 9 AM", "slot1");
        put("W10 AM - 2 PM", "slot1");
        put("10 AM - 2 PM", "slot2");
        put("W3 - 6 PM", "slot2");
        put( "3 - 7 PM", "slot3");
        put("W7 - 10 PM", "slot3");
        put( "8 - 11 PM", "slot4");
        put("8 - 10 PM", "slot4");
    }};

    public void initSlots() {
        this.slot1 = 350;
        this.slot2 = 350;
        this.slot3 = 350;
        this.slot4 = 350;
        this.count1 = 0;
        this.count2 = 0;
        this.count3 = 0;
        this.count4 = 0;
    }

    public void setSlot(String slot, int value){
        String field = slotMap.get(slot);
        switch (field) {
            case "slot1":
                slot1 = value;
                break;
            case "slot2":
                slot2 = value;
                break;
            case "slot3":
                slot3 = value;
                break;
            case "slot4":
                slot4 = value;
                break;
            default: return;
        }
    }

    public int getSlotValue(String slot) {
        String field = slotMap.get(slot);
        switch (field) {
            case "slot1":
                return slot1;
            case "slot2":
                return slot2;
            case "slot3":
                return slot3;
            case "slot4":
                return slot4;
            default:
                return 0;
        }
    }

    public void setCount(String slot, int value){
        String field = slotMap.get(slot);
        switch (field) {
            case "slot1":
                count1 += value;
                break;
            case "slot2":
                count2 += value;
                break;
            case "slot3":
                count3 += value;
                break;
            case "slot4":
                count4 += value;
                break;
            default: return;
        }
    }


    @Override
    public String toString() {
        return date + " " + slot1 + " " + slot2 + " " + slot3 + " " + slot4;
    }

}
