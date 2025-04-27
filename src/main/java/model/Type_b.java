package model;

public class Type_b {
    private int id;
    private String libelle;

    public Type_b() {}

    public Type_b(String libelle) {
        setLibelle(libelle);
    }

    public Type_b(int id, String libelle) {
        this.id = id;
        setLibelle(libelle);
    }

    public int getId() {
        return id;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setLibelle(String libelle) {
        if (libelle == null || libelle.length() < 3) {
            throw new IllegalArgumentException("Le libellé doit avoir au moins 3 caractères");
        }
        this.libelle = libelle;
    }

    @Override
    public String toString() {
        return libelle;
    }
}