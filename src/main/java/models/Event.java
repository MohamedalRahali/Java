package models;

import javafx.beans.property.*;
import java.sql.Date;

public class Event {
    private int id;
    private int artistId;
    private final StringProperty title = new SimpleStringProperty();
    private final ObjectProperty<Date> date = new SimpleObjectProperty<>();
    private final StringProperty description = new SimpleStringProperty();
    private final StringProperty lieux = new SimpleStringProperty();
    private final StringProperty status = new SimpleStringProperty();
    private final StringProperty duree = new SimpleStringProperty();
    private final IntegerProperty nb_place_dispo = new SimpleIntegerProperty();
    private final ObjectProperty<TypeEvent> typeEvent = new SimpleObjectProperty<>();

    public Event() {}

    public Event(String title, Date date, String description, String lieux,
                 String status, String duree, int nb_place_dispo, TypeEvent typeEvent) {
        setTitle(title);
        setDate(date);
        setDescription(description);
        setLieux(lieux);
        setStatus(status);
        setDuree(duree);
        setNb_place_dispo(nb_place_dispo);
        setTypeEvent(typeEvent);
    }

    public Event(String text, Date date, String text1, String text2, String text3, String text4, int nbPlaces) {
    }

    // Getters and setters for regular fields
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getArtistId() { return artistId; }
    public void setArtistId(int artistId) { this.artistId = artistId; }

    public Date getDate() { return date.get(); }
    public void setDate(Date date) { this.date.set(date); }
    public ObjectProperty<Date> dateProperty() { return date; }

    // Property accessors
    public String getTitle() { return title.get(); }
    public void setTitle(String title) { this.title.set(title); }
    public StringProperty titleProperty() { return title; }

    public String getDescription() { return description.get(); }
    public void setDescription(String description) { this.description.set(description); }
    public StringProperty descriptionProperty() { return description; }

    public String getLieux() { return lieux.get(); }
    public void setLieux(String lieux) { this.lieux.set(lieux); }
    public StringProperty lieuxProperty() { return lieux; }

    public String getStatus() { return status.get(); }
    public void setStatus(String status) { this.status.set(status); }
    public StringProperty statusProperty() { return status; }

    public String getDuree() { return duree.get(); }
    public void setDuree(String duree) { this.duree.set(duree); }
    public StringProperty dureeProperty() { return duree; }

    public int getNb_place_dispo() { return nb_place_dispo.get(); }
    public void setNb_place_dispo(int nb_place_dispo) { this.nb_place_dispo.set(nb_place_dispo); }
    public IntegerProperty nbPlacesProperty() { return nb_place_dispo; }

    public TypeEvent getTypeEvent() { return typeEvent.get(); }
    public void setTypeEvent(TypeEvent typeEvent) { this.typeEvent.set(typeEvent); }
    public ObjectProperty<TypeEvent> typeEventProperty() { return typeEvent; }

    // For date display in TableView (optional)
    public String getDateString() {
        return date.get() != null ? date.get().toString() : "";
    }
}
