package LMS;

import javax.persistence.*;

@Entity
@Table(name="artifacts")
public class Artifact {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    private String type;
    private String name;
    private boolean is_copy;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isIs_copy() {
        return is_copy;
    }

    public void setIs_copy(boolean is_copy) {
        this.is_copy = is_copy;
    }
}
