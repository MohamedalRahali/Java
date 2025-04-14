package models;

import java.util.Date;
import java.io.File;

public class Produit {
    private int id;
    private String titre;
    private String description;
    private int artisteId;
    private float prix;
    private String statut;
    private Date dateDeCreation;
    private Categorie categorie;
    private String image;
    private File imageFile;

    // Constructeurs
    public Produit() {}

    public Produit(String titre, String description, int artisteId, float prix, String statut, Date dateDeCreation, Categorie categorie, String image) {
        this.titre = titre;
        this.description = description;
        this.artisteId = artisteId;
        this.prix = prix;
        this.statut = statut;
        this.dateDeCreation = dateDeCreation;
        this.categorie = categorie;
        this.image = image;
    }

    public Produit(int id, String titre, String description, int artisteId, float prix, String statut, Date dateCreation, Categorie categorie, String image) {
    }

    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getArtisteId() { return artisteId; }
    public void setArtisteId(int artisteId) { this.artisteId = artisteId; }

    public float getPrix() { return prix; }
    public void setPrix(float prix) { this.prix = prix; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public Date getDateDeCreation() { return dateDeCreation; }
    public void setDateDeCreation(Date dateDeCreation) { this.dateDeCreation = dateDeCreation; }

    public Categorie getCategorie() { return categorie; }
    public void setCategorie(Categorie categorie) { this.categorie = categorie; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    public File getImageFile() { return imageFile; }
    public void setImageFile(File imageFile) { this.imageFile = imageFile; }

    @Override
    public String toString() {
        return "Produit{" +
                "id=" + id +
                ", titre='" + titre + '\'' +
                ", description='" + description + '\'' +
                ", artisteId=" + artisteId +
                ", prix=" + prix +
                ", statut='" + statut + '\'' +
                ", dateDeCreation=" + dateDeCreation +
                ", categorie=" + categorie +
                ", image='" + image + '\'' +
                '}';
    }
}
