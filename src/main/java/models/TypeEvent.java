package models;

import javafx.beans.property.*;

public class TypeEvent {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final StringProperty name = new SimpleStringProperty();
    private final StringProperty description = new SimpleStringProperty();
    private final StringProperty count = new SimpleStringProperty("0");

    public TypeEvent() {}

    public TypeEvent(int id, String name, String description) {
        setId(id);
        setName(name);
        setDescription(description);
    }

    // Property getters
    public IntegerProperty idProperty() { return id; }
    public StringProperty nameProperty() { return name; }
    public StringProperty descriptionProperty() { return description; }
    public StringProperty countProperty() { return count; }

    // Regular getters and setters
    public int getId() { return id.get(); }
    public void setId(int id) { this.id.set(id); }

    public String getName() { return name.get(); }
    public void setName(String name) { this.name.set(name); }

    public String getDescription() { return description.get(); }
    public void setDescription(String description) { this.description.set(description); }

    public String getCount() { return count.get(); }
    public void setCount(String count) { this.count.set(count); }

    @Override
    public String toString() {
        return getName();
    }
}
