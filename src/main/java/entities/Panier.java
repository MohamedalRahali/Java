package esprit.tn.entities;

import java.util.HashMap;
import java.util.Map;

public class Panier {
    private Map<Produit, Integer> produits;
    private double sousTotal;  // Déclaration de la variable
    private double reduction;  // Déclaration de la variable
    private double total;      // Déclaration de la variable

    public Panier() {
        this.produits = new HashMap<>();
        this.sousTotal = 0;
        this.reduction = 0;
        this.total = 0;
    }

    // Getters et Setters
    public Map<Produit, Integer> getProduits() {
        return produits;
    }

    public void setProduits(Map<Produit, Integer> produits) {
        this.produits = produits;
    }

    public double getSousTotal() {
        return sousTotal;
    }

    public void setSousTotal(double sousTotal) {
        this.sousTotal = sousTotal;
    }

    public double getReduction() {
        return reduction;
    }

    public void setReduction(double reduction) {
        this.reduction = reduction;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    // Méthode utilitaire pour ajouter un produit
    public void ajouterProduit(Produit produit, int quantite) {
        produits.put(produit, produits.getOrDefault(produit, 0) + quantite);
    }
}