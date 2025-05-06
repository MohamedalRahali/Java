package esprit.tn.service;

import esprit.tn.entities.Produit;

import java.util.ArrayList;
import java.util.List;

public class PanierService {
    private List<Produit> cartItems;

    public PanierService() {
        this.cartItems = new ArrayList<>();
    }

    public void ajouterProduit(Produit produit) {
        cartItems.add(produit);
    }

    public void removeProduit(Produit produit) {
        cartItems.remove(produit);
    }

    public List<Produit> getCartItems() {
        return new ArrayList<>(cartItems);
    }

    public void clearCart() {
        cartItems.clear();
    }

    public int getNombreProduits() {
        return cartItems.size();
    }
}