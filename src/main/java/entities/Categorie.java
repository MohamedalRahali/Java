package esprit.tn.entities;

import java.util.Objects;

public class Categorie {
    private Long id;
    private String libelle;
    private String description;

    // Constructeurs
    public Categorie() {
        // Constructeur par défaut nécessaire pour JPA, Jackson, etc.
    }

    public Categorie(String libelle, String description) {
        this(null, libelle, description);
    }

    public Categorie(Long id, String libelle, String description) {
        this.setId(id);
        this.setLibelle(libelle);
        this.setDescription(description);
    }

    // Getters et Setters avec validation
    public int getId() {
        return Math.toIntExact(id);
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        if (libelle == null || libelle.trim().isEmpty()) {
            throw new IllegalArgumentException("Le libellé ne peut pas être vide");
        }
        if (libelle.length() > 100) {
            throw new IllegalArgumentException("Le libellé ne peut pas dépasser 100 caractères");
        }
        this.libelle = libelle.trim();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = (description != null) ? description.trim() : null;
    }

    // Méthodes utilitaires
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Categorie categorie = (Categorie) o;
        return Objects.equals(id, categorie.id) &&
                Objects.equals(libelle, categorie.libelle);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, libelle);
    }

    @Override
    public String toString() {
        return "Categorie [" +
                "id=" + id +
                ", libelle='" + libelle + '\'' +
                (description != null ? ", description='" + description + '\'' : "") +
                ']';
    }

    // Builder pattern (optionnel mais utile)
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private String libelle;
        private String description;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder libelle(String libelle) {
            this.libelle = libelle;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Categorie build() {
            return new Categorie(id, libelle, description);
        }
    }
}