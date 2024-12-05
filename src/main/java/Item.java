import java.io.Serial;
import java.io.Serializable;

public class Item implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private final String title;
    private final String description;

    // Constructor to initialize title and description
    public Item(String title, String description) {
        this.title = title;
        this.description = description;
    }

    // Get the title of the item
    public String getTitle() {
        return title;
    }

    @Override
    // Return the title when converting to a string
    public String toString() {
        return title;
    }
}