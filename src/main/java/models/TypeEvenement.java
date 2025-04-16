package models;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class TypeEvenement {
    private final SimpleIntegerProperty id;
    private final SimpleStringProperty nom;
    private final SimpleStringProperty description;

    public TypeEvenement(int id, String nom, String description) {
        this.id = new SimpleIntegerProperty(id);
        this.nom = new SimpleStringProperty(nom);
        this.description = new SimpleStringProperty(description);
    }

    // Getters
    public int getId() { return id.get(); }
    public String getNom() { return nom.get(); }
    public String getDescription() { return description.get(); }

    // Property getters
    public SimpleIntegerProperty idProperty() { return id; }
    public SimpleStringProperty nomProperty() { return nom; }
    public SimpleStringProperty descriptionProperty() { return description; }

    // Setters
    public void setNom(String nom) { this.nom.set(nom); }
    public void setDescription(String description) { this.description.set(description); }
}