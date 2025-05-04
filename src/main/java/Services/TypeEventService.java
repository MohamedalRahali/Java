package Services;

import models.TypeEvent;
import util.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TypeEventService {
    public void add(TypeEvent typeEvent) {
        String query = "INSERT INTO type_event (name, description) VALUES (?, ?)";
        try (Connection connection = MyConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, typeEvent.getName());
            statement.setString(2, typeEvent.getDescription());
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<TypeEvent> getAll() {
        List<TypeEvent> typeEvents = new ArrayList<>();
        String query = "SELECT * FROM type_event";
        try (Connection connection = MyConnection.getInstance().getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {
            while (resultSet.next()) {
                TypeEvent typeEvent = new TypeEvent();
                typeEvent.setId(resultSet.getInt("id"));
                typeEvent.setName(resultSet.getString("name"));
                typeEvent.setDescription(resultSet.getString("description"));
                typeEvents.add(typeEvent);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return typeEvents;
    }

    public void update(TypeEvent typeEvent) {
        String query = "UPDATE type_event SET name = ?, description = ? WHERE id = ?";
        try (Connection connection = MyConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, typeEvent.getName());
            statement.setString(2, typeEvent.getDescription());
            statement.setInt(3, typeEvent.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void delete(int id) {
        String query = "DELETE FROM type_event WHERE id = ?";
        try (Connection connection = MyConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public TypeEvent getById(int id) {
        String SQL = "SELECT * FROM type_event WHERE id = ?";
        try (PreparedStatement pstmt = MyConnection.getInstance().getConnection().prepareStatement(SQL)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new TypeEvent(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("description")
                    );
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération du type d'événement : " + e.getMessage());
        }
        return null;
    }
}