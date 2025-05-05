package models;

import java.time.LocalDateTime;
import javafx.beans.property.*;

public class Reponse {
    private int id;
    private int reclamationId;
    private final StringProperty message = new SimpleStringProperty();
    private LocalDateTime createdAt;

    public Reponse() {
        this.createdAt = LocalDateTime.now();
    }

    public Reponse(int reclamationId, String message) {
        this.reclamationId = reclamationId;
        setMessage(message);
        this.createdAt = LocalDateTime.now();
    }

    public Reponse(int id, int reclamationId, String message, LocalDateTime createdAt) {
        this.id = id;
        this.reclamationId = reclamationId;
        setMessage(message);
        this.createdAt = createdAt;
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getReclamationId() { return reclamationId; }
    public void setReclamationId(int reclamationId) { this.reclamationId = reclamationId; }

    public String getMessage() { return message.get(); }
    public void setMessage(String message) { this.message.set(message); }
    public StringProperty messageProperty() { return message; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return getMessage();
    }
} 