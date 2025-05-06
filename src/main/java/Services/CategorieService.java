package esprit.tn.service;

import esprit.tn.entities.Categorie;
import esprit.tn.interfaces.ICategorieService;
import esprit.tn.tools.MyConnection;

import java.sql.*;
import java.util.*;

public class CategorieService implements ICategorieService {
    private final Connection conn;

    public CategorieService() {
        this.conn = MyConnection.getInstance().getCnx();
    }

    @Override
    public Categorie ajouterCategorie(Categorie categorie) throws SQLException {
        if (categorie.getLibelle() == null) {
            throw new IllegalArgumentException("Catégorie invalide");
        }

        String sql = "INSERT INTO categorie (libelle, description) VALUES (?, ?)";

        try (PreparedStatement pst = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pst.setString(1, categorie.getLibelle());
            pst.setString(2, categorie.getDescription());

            int affectedRows = pst.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("La création de la catégorie a échoué");
            }

            try (ResultSet generatedKeys = pst.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    categorie.setId(generatedKeys.getLong(1));
                } else {
                    throw new SQLException("La création a échoué, aucun ID obtenu.");
                }
            }
        }

        return categorie;
    }

    @Override
    public void deleteCategorie(Long idCategorie) throws SQLException {

    }

    public boolean deleteCategorie(int idCategorie) throws SQLException {
        String sql = "DELETE FROM categorie WHERE id = ?";

        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setLong(1, idCategorie);

            int affectedRows = pst.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Aucune catégorie trouvée avec l'ID " + idCategorie);
            }
        }
        return false;
    }

    @Override
    public void updateCategorie(Categorie categorie) throws SQLException {
        if (categorie.getLibelle() == null) {
            throw new IllegalArgumentException("Catégorie invalide");
        }

        String sql = "UPDATE categorie SET libelle = ?, description = ? WHERE id = ?";

        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, categorie.getLibelle());
            pst.setString(2, categorie.getDescription());
            pst.setLong(3, categorie.getId());

            int affectedRows = pst.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Aucune catégorie trouvée avec l'ID " + categorie.getId());
            }
        }
    }

    @Override
    public List<Categorie> getAllCategories() throws SQLException {
        List<Categorie> categories = new ArrayList<>();
        String sql = "SELECT * FROM categorie";

        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                categories.add(mapResultSetToCategorie(rs));
            }
        }

        return categories;
    }

    @Override
    public Categorie getCategorieById(Long idCategorie) throws SQLException {
        String sql = "SELECT * FROM categorie WHERE id = ?";

        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setLong(1, idCategorie);

            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToCategorie(rs);
                }
            }
        }

        return null;
    }

    @Override
    public Categorie getCategorieById(int idCategorie) throws SQLException {
        return getCategorieById((long) idCategorie);
    }

    @Override
    public List<Categorie> searchCategories(String keyword) throws SQLException {
        List<Categorie> categories = new ArrayList<>();
        String sql = "SELECT * FROM categorie WHERE libelle LIKE ? OR description LIKE ?";

        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, "%" + keyword + "%");
            pst.setString(2, "%" + keyword + "%");

            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    categories.add(mapResultSetToCategorie(rs));
                }
            }
        }

        return categories;
    }

    @Override
    public Map<String, Integer> getProduitsCountByCategorie() throws SQLException {
        Map<String, Integer> produitsCount = new LinkedHashMap<>();
        String sql = "SELECT c.libelle, COUNT(p.id) AS count " +
                "FROM categorie c LEFT JOIN produit p ON c.id = p.categorie_id GROUP BY c.libelle";

        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                String libelle = rs.getString("libelle");
                int count = rs.getInt("count");
                produitsCount.put(libelle, count);
            }
        }

        return produitsCount;
    }

    @Override
    public Map<String, Integer> getCategoriesCreatedPerDay() throws SQLException {
        Map<String, Integer> categoriesPerDay = new LinkedHashMap<>();
        String sql = "SELECT DAYNAME(id) AS day, COUNT(*) AS count " +
                "FROM categorie GROUP BY DAYNAME(id)";

        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                String day = rs.getString("day");
                int count = rs.getInt("count");
                categoriesPerDay.put(day, count);
            }
        }

        String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
        for (String day : days) {
            categoriesPerDay.putIfAbsent(day, 0);
        }

        return categoriesPerDay;
    }

    private Categorie mapResultSetToCategorie(ResultSet rs) throws SQLException {
        Categorie categorie = new Categorie();
        categorie.setId(rs.getLong("id"));
        categorie.setLibelle(rs.getString("libelle"));
        categorie.setDescription(rs.getString("description"));
        return categorie;
    }

    public Categorie getCategorieByLibelle(String categorieName) throws SQLException {
        if (categorieName == null || categorieName.trim().isEmpty()) {
            return null; // Handle null or empty input
        }

        List<Categorie> categories = getAllCategories();
        if (categories != null) {
            for (Categorie cat : categories) {
                if (cat.getLibelle() != null && cat.getLibelle().equalsIgnoreCase(categorieName.trim())) {
                    return cat;
                }
            }
        }
        return null; // Return null if no match is found
    }
}