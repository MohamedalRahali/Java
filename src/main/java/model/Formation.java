package model;
import java.util.ArrayList;
import java.util.List;

public class Formation {
    private int id;
    private String titre;
    private String description;
    private double prix;
    private int placesDisponibles;
    private int categorieId;  
    private CategorieFormation categorie;
    private List<Participant> participants = new ArrayList<>();

    // Default constructor
    public Formation() {}

    // Constructor with all fields
    public Formation(int id, String titre, String description, double prix, int placesDisponibles, int categorieId) {
        this.id = id;
        this.titre = titre;
        this.description = description;
        this.prix = prix;
        this.placesDisponibles = placesDisponibles;
        this.categorieId = categorieId;
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public double getPrix() { return prix; }
    public void setPrix(double prix) { this.prix = prix; }
    
    public int getPlacesDisponibles() { return placesDisponibles; }
    public void setPlacesDisponibles(int placesDisponibles) { this.placesDisponibles = placesDisponibles; }
    
    public int getCategorieId() { return categorieId; }
    public void setCategorieId(int categorieId) { this.categorieId = categorieId; }

    public CategorieFormation getCategorie() { return categorie; }
    public void setCategorie(CategorieFormation categorie) { this.categorie = categorie; }

    public List<Participant> getParticipants() { return participants; }
    public void setParticipants(List<Participant> participants) { this.participants = participants; }

    public void addParticipant(Participant participant) { participants.add(participant); }

    public boolean isValid() {
        return titre != null && !titre.trim().isEmpty()
            && description != null && !description.trim().isEmpty()
            && prix > 0
            && placesDisponibles >= 0
            && categorieId > 0;
    }

    public String getValidationErrors() {
        StringBuilder errors = new StringBuilder();
        if (titre == null || titre.trim().isEmpty()) errors.append("Title is required\n");
        if (description == null || description.trim().isEmpty()) errors.append("Description is required\n");
        if (prix <= 0) errors.append("Price must be positive\n");
        if (placesDisponibles < 0) errors.append("Available places cannot be negative\n");
        if (categorieId <= 0) errors.append("Category is required\n");
        return errors.toString();
    }

    @Override
    public String toString() {
        return titre + " (" + prix + " DT)";
    }
}
