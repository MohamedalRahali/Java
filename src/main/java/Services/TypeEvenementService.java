package Services;

import models.TypeEvenement;
import util.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TypeEvenementService {
    private Connection connection;

    public TypeEvenementService() {
        MyConnection DataSource = null;
        connection = DataSource.getInstance().getConnection();
    }

    public void ajouter(TypeEvenement type) throws SQLException {
        String query = "INSERT INTO type_evenement (nom, description) VALUES (?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, type.getNom());
            stmt.setString(2, type.getDescription());
            stmt.executeUpdate();
        }
    }

    public void modifier(TypeEvenement type) throws SQLException {
        String query = "UPDATE type_evenement SET nom = ?, description = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, type.getNom());
            stmt.setString(2, type.getDescription());
            stmt.setInt(3, type.getId());
            stmt.executeUpdate();
        }
    }

    public void supprimer(int id) throws SQLException {
        String query = "DELETE FROM type_evenement WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    public List<TypeEvenement> getAll() throws SQLException {
        List<TypeEvenement> types = new ArrayList<>();
        String query = "SELECT * FROM type_evenement";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                types.add(new TypeEvenement(
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getString("description")));
            }
        }
        return types;
    }
}