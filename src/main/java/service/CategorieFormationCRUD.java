package service;

import model.CategorieFormation;
import utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategorieFormationCRUD {

    private Connection con;

    public CategorieFormationCRUD() throws SQLException {
        this.con = MyDatabase.getInstance().getCnx();
        if (this.con == null) {
            throw new SQLException("Failed to initialize database connection");
        }
    }

    public boolean add(CategorieFormation categorie) {
        String sql = "INSERT INTO categorie_formation (nom, description) VALUES (?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, categorie.getNom());
            ps.setString(2, categorie.getDescription());
            int affectedRows = ps.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        categorie.setId(rs.getInt(1));
                        return true;
                    }
                }
            }
            return false;
        } catch (SQLException e) {
            System.err.println("Error adding category: " + e.getMessage());
            return false;
        }
    }

    public boolean update(CategorieFormation categorie) {
        String sql = "UPDATE categorie_formation SET nom = ?, description = ? WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, categorie.getNom());
            ps.setString(2, categorie.getDescription());
            ps.setInt(3, categorie.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating category: " + e.getMessage());
            return false;
        }
    }

    public boolean delete(int id) throws SQLException {
        con.setAutoCommit(false);
        try {
            // First check if any formations reference this category
            String checkSql = "SELECT COUNT(*) FROM formation WHERE categorie_id = ?";
            try (PreparedStatement checkPs = con.prepareStatement(checkSql)) {
                checkPs.setInt(1, id);
                ResultSet rs = checkPs.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    throw new SQLException("Cannot delete category - it has associated formations");
                }
            }

            // Delete the category
            String deleteSql = "DELETE FROM categorie_formation WHERE id = ?";
            try (PreparedStatement ps = con.prepareStatement(deleteSql)) {
                ps.setInt(1, id);
                int affectedRows = ps.executeUpdate();
                con.commit();
                return affectedRows > 0;
            }
        } catch (SQLException e) {
            con.rollback();
            throw e;
        } finally {
            con.setAutoCommit(true);
        }
    }

    public List<CategorieFormation> display() {
        List<CategorieFormation> categories = new ArrayList<>();
        String query = "SELECT id, nom, description FROM categorie_formation";
        
        try (Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                categories.add(new CategorieFormation(
                    rs.getInt("id"),
                    rs.getString("nom"),
                    rs.getString("description")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching categories: " + e.getMessage());
        }
        return categories;
    }

    public List<CategorieFormation> getAll() throws SQLException {
        List<CategorieFormation> categories = new ArrayList<>();
        String query = "SELECT id, nom, description FROM categorie_formation";
        
        try (Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
        
            while (rs.next()) {
                categories.add(new CategorieFormation(
                    rs.getInt("id"),
                    rs.getString("nom"),
                    rs.getString("description")
                ));
            }
        }
        return categories;
    }
}
