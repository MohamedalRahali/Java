package models;

import javafx.beans.property.*;

import java.sql.Date;

public class Blog {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final StringProperty titre = new SimpleStringProperty();
    private final StringProperty description = new SimpleStringProperty();
    private final ObjectProperty<Date> date_crea = new SimpleObjectProperty<>();
    private final ObjectProperty<Date> date_pub = new SimpleObjectProperty<>();
    private final StringProperty type = new SimpleStringProperty();




    // No arguments const

    public Blog() {
        // Initialize properties with default values
        this.titre.set("");
        this.description.set("");
        this.date_crea.set(new Date(System.currentTimeMillis())); // default to current date
        this.date_pub.set(new Date(System.currentTimeMillis())); // default to current date
        this.type.set("");
    }

    // Constructor without ID
    public Blog(String titre, String description, Date date_crea, Date date_pub, String type) {
        setTitre(titre);
        setDescription(description);
        setDate_crea(date_crea);
        setDate_pub(date_pub);
        setType(type);
    }

    // Constructor with ID (used in getAll(), getById(), etc.)
    public Blog(int id, String titre, String description, Date date_crea, Date date_pub, String type) {
        setId(id);
        setTitre(titre);
        setDescription(description);
        setDate_crea(date_crea);
        setDate_pub(date_pub);
        setType(type);
    }

    // Getters
    public int getId() {
        return id.get();
    }

    public String getTitre() {
        return titre.get();
    }

    public String getDescription() {
        return description.get();
    }

    public String getDescr() { // Alias for compatibility with the service class
        return getDescription();
    }

    public Date getDate_crea() {
        return date_crea.get();
    }

    public Date getDate_pub() {
        return date_pub.get();
    }

    public String getType() {
        return type.get();
    }

    // Setters
    public void setId(int id) {
        this.id.set(id);
    }

    public void setTitre(String titre) {
        this.titre.set(titre);
    }

    public void setDescription(String description) {
        this.description.set(description);
    }

    public void setDate_crea(Date date_crea) {
        this.date_crea.set(date_crea);
    }

    public void setDate_pub(Date date_pub) {
        this.date_pub.set(date_pub);
    }

    public void setType(String type) {
        this.type.set(type);
    }

    // Property methods for JavaFX bindings
    public IntegerProperty idProperty() {
        return id;
    }

    public StringProperty titreProperty() {
        return titre;
    }

    public StringProperty descriptionProperty() {
        return description;
    }

    public ObjectProperty<Date> dateCreaProperty() {
        return date_crea;
    }

    public ObjectProperty<Date> datePubProperty() {
        return date_pub;
    }

    public StringProperty typeProperty() {
        return type;
    }

    @Override
    public String toString() {
        return "Blog{" +
                "id=" + getId() +
                ", titre='" + getTitre() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", date_crea=" + getDate_crea() +
                ", date_pub=" + getDate_pub() +
                ", type='" + getType() + '\'' +
                '}';
    }
}
