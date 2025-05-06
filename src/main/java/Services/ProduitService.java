package esprit.tn.service;

import esprit.tn.entities.Produit;
import esprit.tn.entities.Categorie;
import esprit.tn.interfaces.IProduitService;
import esprit.tn.tools.MyConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class ProduitService implements IProduitService {
    private final Connection conn;

    public ProduitService() {
        this.conn = MyConnection.getInstance().getCnx();
    }

    @Override
    public Produit ajouterProduit(Produit produit) throws SQLException {
        if (produit.getTitre() == null || produit.getNomArtiste() == null || produit.getStatut() == null) {
            throw new IllegalArgumentException("Produit invalide");
        }

        String sql = "INSERT INTO produit (titre, description, nom_artiste, prix, statut, categorie_id, image, date_creation) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pst = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pst.setString(1, produit.getTitre());
            pst.setString(2, produit.getDescription());
            pst.setString(3, produit.getNomArtiste());
            pst.setFloat(4, produit.getPrix());
            pst.setString(5, produit.getStatut());
            if (produit.getCategorie() != null) {
                pst.setInt(6, (int) produit.getCategorie().getId());
            } else {
                pst.setNull(6, Types.INTEGER);
            }
            pst.setString(7, produit.getImage());
            pst.setTimestamp(8, Timestamp.valueOf(produit.getDateCreation() != null ? produit.getDateCreation() : LocalDateTime.now()));

            int affectedRows = pst.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("La création du produit a échoué");
            }

            try (ResultSet generatedKeys = pst.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    produit.setId(generatedKeys.getLong(1));
                } else {
                    throw new SQLException("La création a échoué, aucun ID obtenu.");
                }
            }
        }

        return produit;
    }

    @Override
    public void deleteProduit(Long idProduit) throws SQLException {
        String sql = "DELETE FROM produit WHERE id = ?";

        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setLong(1, idProduit);

            int affectedRows = pst.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Aucun produit trouvé avec l'ID " + idProduit);
            }
        }
    }

    @Override
    public void updateProduit(Produit produit) throws SQLException {
        if (produit.getTitre() == null || produit.getNomArtiste() == null || produit.getStatut() == null) {
            throw new IllegalArgumentException("Produit invalide");
        }

        String sql = "UPDATE produit SET titre = ?, description = ?, nom_artiste = ?, prix = ?, statut = ?, " +
                "categorie_id = ?, image = ?, date_creation = ? WHERE id = ?";

        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, produit.getTitre());
            pst.setString(2, produit.getDescription());
            pst.setString(3, produit.getNomArtiste());
            pst.setFloat(4, produit.getPrix());
            pst.setString(5, produit.getStatut());
            if (produit.getCategorie() != null) {
                pst.setInt(6, produit.getCategorie().getId());
            } else {
                pst.setNull(6, Types.INTEGER);
            }
            pst.setString(7, produit.getImage());
            pst.setTimestamp(8, Timestamp.valueOf(produit.getDateCreation() != null ? produit.getDateCreation() : LocalDateTime.now()));
            pst.setLong(9, produit.getId());

            int affectedRows = pst.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Aucun produit trouvé avec l'ID " + produit.getId());
            }
        }
    }

    @Override
    public List<Produit> getAllProduits() throws SQLException {
        List<Produit> produits = new ArrayList<>();
        String sql = "SELECT p.*, c.id AS cat_id, c.libelle, c.description AS cat_description " +
                "FROM produit p LEFT JOIN categorie c ON p.categorie_id = c.id";

        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                produits.add(mapResultSetToProduit(rs));
            }
        }

        return produits;
    }

    @Override
    public Produit getProduitById(Long idProduit) throws SQLException {
        String sql = "SELECT p.*, c.id AS cat_id, c.libelle, c.description AS cat_description " +
                "FROM produit p LEFT JOIN categorie c ON p.categorie_id = c.id WHERE p.id = ?";

        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setLong(1, idProduit);

            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToProduit(rs);
                }
            }
        }

        return null;
    }

    @Override
    public Produit getProduitById(int idProduit) throws SQLException {
        return getProduitById((long) idProduit);
    }

    @Override
    public List<Produit> searchProduits(String keyword) throws SQLException {
        List<Produit> produits = new ArrayList<>();
        String sql = "SELECT p.*, c.id AS cat_id, c.libelle, c.description AS cat_description " +
                "FROM produit p LEFT JOIN categorie c ON p.categorie_id = c.id " +
                "WHERE p.titre LIKE ? OR p.description LIKE ? OR p.nom_artiste LIKE ?";

        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, "%" + keyword + "%");
            pst.setString(2, "%" + keyword + "%");
            pst.setString(3, "%" + keyword + "%");

            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    produits.add(mapResultSetToProduit(rs));
                }
            }
        }

        return produits;
    }

    @Override
    public List<Produit> getProduitsByCategorie(Long categorieId) throws SQLException {
        List<Produit> produits = new ArrayList<>();
        String sql = "SELECT p.*, c.id AS cat_id, c.libelle, c.description AS cat_description " +
                "FROM produit p LEFT JOIN categorie c ON p.categorie_id = c.id WHERE p.categorie_id = ?";

        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setLong(1, categorieId);

            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    produits.add(mapResultSetToProduit(rs));
                }
            }
        }

        return produits;
    }

    @Override
    public List<Produit> getProduitsByArtiste(String nomArtiste) throws SQLException {
        List<Produit> produits = new ArrayList<>();
        String sql = "SELECT p.*, c.id AS cat_id, c.libelle, c.description AS cat_description " +
                "FROM produit p LEFT JOIN categorie c ON p.categorie_id = c.id WHERE p.nom_artiste = ?";

        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, nomArtiste);

            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    produits.add(mapResultSetToProduit(rs));
                }
            }
        }

        return produits;
    }

    @Override
    public void updateProduitPrix(Long produitId, float prix) throws SQLException {
        String sql = "UPDATE produit SET prix = ?, date_creation = ? WHERE id = ?";

        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            if (prix < 0) {
                throw new IllegalArgumentException("Prix ne peut pas être négatif");
            }
            pst.setFloat(1, prix);
            pst.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            pst.setLong(3, produitId);

            int affectedRows = pst.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Aucun produit trouvé avec l'ID " + produitId);
            }
        }
    }

    @Override
    public Map<String, Integer> getProduitsCreatedPerDay() throws SQLException {
        Map<String, Integer> produitsPerDay = new LinkedHashMap<>();
        String sql = "SELECT DAYNAME(date_creation) AS day, COUNT(*) AS count " +
                "FROM produit GROUP BY DAYNAME(date_creation)";

        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                String day = rs.getString("day");
                int count = rs.getInt("count");
                produitsPerDay.put(day, count);
            }
        }

        String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
        for (String day : days) {
            produitsPerDay.putIfAbsent(day, 0);
        }

        return produitsPerDay;
    }

    @Override
    public Map<String, Double> getRevenuePerProduit() throws SQLException {
        Map<String, Double> revenuePerProduit = new LinkedHashMap<>();
        String sql = "SELECT titre, prix FROM produit";

        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                String titre = rs.getString("titre");
                double prix = rs.getDouble("prix");
                revenuePerProduit.put(titre, prix);
            }
        }

        return revenuePerProduit;
    }

    @Override
    public Map<String, Integer> getProduitsByStatut() throws SQLException {
        Map<String, Integer> produitsByStatut = new LinkedHashMap<>();
        String sql = "SELECT statut, COUNT(*) AS count FROM produit GROUP BY statut";

        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                String statut = rs.getString("statut");
                int count = rs.getInt("count");
                produitsByStatut.put(statut, count);
            }
        }

        return produitsByStatut;
    }

    private Produit mapResultSetToProduit(ResultSet rs) throws SQLException {
        Produit produit = new Produit();
        produit.setId(rs.getLong("id"));
        produit.setTitre(rs.getString("titre"));
        produit.setDescription(rs.getString("description"));
        produit.setNomArtiste(rs.getString("nom_artiste"));
        produit.setPrix(rs.getFloat("prix"));
        produit.setStatut(rs.getString("statut"));
        produit.setImage(rs.getString("image"));

        Timestamp dateCreation = rs.getTimestamp("date_creation");
        if (dateCreation != null) {
            produit.setDateCreation(dateCreation.toLocalDateTime());
        }

        long catId = rs.getLong("cat_id");
        if (!rs.wasNull()) {
            Categorie categorie = new Categorie();
            categorie.setId(catId);
            categorie.setLibelle(rs.getString("libelle"));
            categorie.setDescription(rs.getString("cat_description"));
            produit.setCategorie(categorie);
        }

        return produit;
    }

    public List<Produit> getByCategory(int categoryId) {
        List<Produit> produits = new ArrayList<>();
        String sql = "SELECT p.*, c.id AS cat_id, c.libelle, c.description AS cat_description " +
                "FROM produit p LEFT JOIN categorie c ON p.categorie_id = c.id WHERE p.categorie_id = ?";

        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setInt(1, categoryId);

            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    produits.add(mapResultSetToProduit(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Consider logging this instead of printing in a production environment
            throw new RuntimeException("Erreur lors de la récupération des produits par catégorie: " + e.getMessage());
        }

        return produits;
    }
}