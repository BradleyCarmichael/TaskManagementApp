import java.io.Serial;
import java.io.Serializable;

// Basic item class that tasks are built from
// Contains just a title that can't be changed
public class Item implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private final String title;

    // Creates a new item with a title
    public Item(String title) {
        this.title = title;
    }

    // Gets the item's title
    public String getTitle() {
        return title;
    }

    // Shows the item as a string (just shows the title)
    @Override
    public String toString() {
        return title;
    }
}