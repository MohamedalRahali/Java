package esprit.tn.interfaces;

import esprit.tn.entities.Produit;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public interface IProduitService {
    Produit ajouterProduit(Produit produit) throws SQLException;

    void deleteProduit(Long idProduit) throws SQLException;

    void updateProduit(Produit produit) throws SQLException;

    List<Produit> getAllProduits() throws SQLException;

    Produit getProduitById(Long idProduit) throws SQLException;

    Produit getProduitById(int idProduit) throws SQLException;

    List<Produit> searchProduits(String keyword) throws SQLException;

    List<Produit> getProduitsByCategorie(Long categorieId) throws SQLException;

    List<Produit> getProduitsByArtiste(String nomArtiste) throws SQLException;

    void updateProduitPrix(Long produitId, float prix) throws SQLException;

    Map<String, Integer> getProduitsCreatedPerDay() throws SQLException;

    Map<String, Double> getRevenuePerProduit() throws SQLException;

    Map<String, Integer> getProduitsByStatut() throws SQLException;
}