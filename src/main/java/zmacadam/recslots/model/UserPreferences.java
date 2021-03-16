package zmacadam.recslots.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.lang.reflect.Field;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "preferences")
public class UserPreferences {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    private boolean week1;
    private boolean week2;
    private boolean week3;
    private boolean week4;

    private boolean weekend1;
    private boolean weekend2;
    private boolean weekend3;

    private boolean day1;
    private boolean day2;
    private boolean day3;

    private boolean stop;

    @OneToOne(mappedBy = "userPreferences")
    private User user;

    public UserPreferences initPreferences() {
        this.week1 = true;
        this.week2 = true;
        this.week3 = true;
        this.week4 = true;

        this.weekend1 = true;
        this.weekend2 = true;
        this.weekend3 = true;

        this.day1 = true;
        this.day2 = true;
        this.day3 = true;

        this.stop = false;

        return this;
    }

    public boolean getField(String fieldName) throws NoSuchFieldException, IllegalAccessException {
        Field field = this.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        boolean value = (boolean) field.get(this);
        field.setAccessible(false);
        return value;
    }

    public void setField(String fieldName, boolean value) throws NoSuchFieldException, IllegalAccessException {
        Field field = this.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(this, value);
        field.setAccessible(false);
    }
}
