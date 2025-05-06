package esprit.tn.entities;

import java.time.LocalDateTime;

public class Produit {
    private Long id;
    private String titre;
    private String description;
    private String nomArtiste;
    private float prix;
    private String statut;
    private Categorie categorie;
    private String image;
    private LocalDateTime dateCreation;
    private Integer quantite; // Added field for quantity

    // Constructor (updated to include quantite)
    public Produit(Long id, String titre, String description, String nomArtiste, float prix, String statut,
                   Categorie categorie, String image, LocalDateTime dateCreation, Integer quantite) {
        this.id = id;
        this.titre = titre;
        this.description = description;
        this.nomArtiste = nomArtiste;
        this.prix = prix;
        this.statut = statut;
        this.categorie = categorie;
        this.image = image;
        this.dateCreation = dateCreation;
        this.quantite = quantite;
    }

    // Default constructor (updated to initialize quantite)
    public Produit() {
        this.quantite = 1; // Default quantity to 1
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        if (titre == null || titre.trim().isEmpty()) {
            throw new IllegalArgumentException("Titre ne peut pas être vide");
        }
        this.titre = titre;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getNomArtiste() {
        return nomArtiste;
    }

    public void setNomArtiste(String nomArtiste) {
        if (nomArtiste == null || nomArtiste.trim().isEmpty()) {
            throw new IllegalArgumentException("Nom de l'artiste ne peut pas être vide");
        }
        this.nomArtiste = nomArtiste;
    }

    public float getPrix() {
        return prix;
    }

    public void setPrix(float prix) {
        if (prix < 0) {
            throw new IllegalArgumentException("Prix ne peut pas être négatif");
        }
        this.prix = prix;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        if (statut == null || statut.trim().isEmpty()) {
            throw new IllegalArgumentException("Statut ne peut pas être vide");
        }
        this.statut = statut;
    }

    public Categorie getCategorie() {
        return categorie;
    }

    public void setCategorie(Categorie categorie) {
        this.categorie = categorie;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }

    // Getter for quantite
    public Integer getQuantite() {
        return quantite;
    }

    // Completed setQuantite method
    public void setQuantite(Integer value) {
        if (value == null || value < 0) {
            throw new IllegalArgumentException("La quantité ne peut pas être négative ou nulle");
        }
        this.quantite = value;
    }

    @Override
    public String toString() {
        return "Produit{" +
                "id=" + id +
                ", titre='" + titre + '\'' +
                ", description='" + description + '\'' +
                ", nomArtiste='" + nomArtiste + '\'' +
                ", prix=" + prix +
                ", statut='" + statut + '\'' +
                ", categorie=" + categorie +
                ", image='" + image + '\'' +
                ", dateCreation=" + dateCreation +
                ", quantite=" + quantite +
                '}';
    }
}