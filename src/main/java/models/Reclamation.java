package models;

import java.time.LocalDateTime;
import javafx.beans.property.*;

public class Reclamation {
    private int id;
    private final StringProperty title = new SimpleStringProperty();
    private final StringProperty description = new SimpleStringProperty();
    private TypeReclamation typeReclamation;
    private LocalDateTime createdAt;
    private boolean fingerprintVerified;

    public Reclamation() {
        this.createdAt = LocalDateTime.now();
        this.fingerprintVerified = false;
    }

    public Reclamation(String title, String description, TypeReclamation typeReclamation) {
        setTitle(title);
        setDescription(description);
        this.typeReclamation = typeReclamation;
        this.createdAt = LocalDateTime.now();
        this.fingerprintVerified = true;
    }

    public Reclamation(int id, String title, String description, TypeReclamation typeReclamation) {
        this.id = id;
        setTitle(title);
        setDescription(description);
        this.typeReclamation = typeReclamation;
        this.createdAt = LocalDateTime.now();
        this.fingerprintVerified = true;
    }

    public Reclamation(int id, String title, String description, TypeReclamation typeReclamation, LocalDateTime createdAt) {
        this.id = id;
        setTitle(title);
        setDescription(description);
        this.typeReclamation = typeReclamation;
        this.createdAt = createdAt;
        this.fingerprintVerified = true;
    }

    // Getters and setters for regular fields
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    // Property accessors
    public String getTitle() { return title.get(); }
    public void setTitle(String title) { this.title.set(title); }
    public StringProperty titleProperty() { return title; }

    public String getDescription() { return description.get(); }
    public void setDescription(String description) { this.description.set(description); }
    public StringProperty descriptionProperty() { return description; }

    public TypeReclamation getTypeReclamation() {
        return typeReclamation;
    }

    public void setTypeReclamation(TypeReclamation typeReclamation) {
        this.typeReclamation = typeReclamation;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isFingerprintVerified() {
        return fingerprintVerified;
    }

    public void setFingerprintVerified(boolean fingerprintVerified) {
        this.fingerprintVerified = fingerprintVerified;
    }

    public String getTypeName() {
        return typeReclamation != null ? typeReclamation.getName() : "";
    }

    @Override
    public String toString() {
        return getTitle();
    }
}