package esprit.tn.interfaces;

import esprit.tn.entities.Panier;
import esprit.tn.entities.Produit;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public interface IPanierService {
    Panier ajouterPanier(Panier panier) throws SQLException;
    void deletePanier(Long idPanier) throws SQLException;
    void updatePanier(Panier panier) throws SQLException;
    Panier getPanierById(Long idPanier) throws SQLException;
    Panier getPanierByUserId(Long userId) throws SQLException;
    void addProduitToPanier(Long panierId, Produit produit) throws SQLException;
    void removeProduitFromPanier(Long panierId, Produit produit) throws SQLException;
    List<Panier> getAllPaniers() throws SQLException;
    List<Produit> getProduitsDansPanier(Long userId) throws SQLException;
    boolean passerCommande(Long userId) throws SQLException;
    Map<String, Integer> getPaniersCreatedPerDay() throws SQLException;
    Map<String, Double> getPanierTotalValuePerDay() throws SQLException;
    Map<Long, Integer> getProduitsCountByPanier() throws SQLException;
}