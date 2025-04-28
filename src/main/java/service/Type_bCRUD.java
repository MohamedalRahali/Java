package service;

import model.Type_b;
import utils.MyDatabse;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Type_bCRUD {

    private Connection cnx;

    public Type_bCRUD() {
        cnx = MyDatabse.getInstance().getCnx();
    }

    public void add(Type_b type) throws SQLException {
        String sql = "INSERT INTO type_b (libelle) VALUES (?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, type.getLibelle());
            ps.executeUpdate();

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    type.setId(generatedKeys.getInt(1));
                }
            }
            System.out.println("Type_b ajouté avec succès !");
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'ajout : " + e.getMessage());
            throw e;
        }
    }

    public void update(Type_b type) throws SQLException {
        String sql = "UPDATE type_b SET libelle = ? WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, type.getLibelle());
            ps.setInt(2, type.getId());
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Type_b modifié avec succès !");
            } else {
                System.out.println("Aucun Type_b trouvé avec l'ID : " + type.getId());
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la modification : " + e.getMessage());
            throw e;
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM type_b WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Type_b supprimé avec succès !");
            } else {
                System.out.println("Aucun Type_b trouvé avec l'ID : " + id);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression : " + e.getMessage());
            throw e;
        }
    }

    public List<Type_b> display() throws SQLException {
        List<Type_b> types = new ArrayList<>();
        String query = "SELECT * FROM type_b ORDER BY libelle";

        try (Statement statement = cnx.createStatement();
             ResultSet rs = statement.executeQuery(query)) {

            while (rs.next()) {
                types.add(new Type_b(
                        rs.getInt("id"),
                        rs.getString("libelle")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération : " + e.getMessage());
            throw e;
        }

        return types;
    }

    public List<Type_b> searchByLibelle(String keyword) throws SQLException {
        List<Type_b> results = new ArrayList<>();
        String sql = "SELECT * FROM type_b WHERE libelle LIKE ?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, "%" + keyword + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(new Type_b(
                            rs.getInt("id"),
                            rs.getString("libelle")
                    ));
                }
            }
        }

        return results;
    }

    public boolean libelleExists(String libelle) throws SQLException {
        String sql = "SELECT COUNT(*) FROM type_b WHERE libelle = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, libelle);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    public List<Type_b> getAll() throws SQLException {
        return display();
    }
}