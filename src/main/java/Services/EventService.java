package Services;

import models.Event;
import models.TypeEvent;
import util.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EventService {
    private final TypeEventService typeEventService;

    public EventService() {
        typeEventService = new TypeEventService();
    }

    public List<Event> getAll() {
        List<Event> events = new ArrayList<>();
        String query = "SELECT * FROM evenment";
        try (Connection connection = MyConnection.getInstance().getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {
            while (resultSet.next()) {
                Event event = new Event();
                event.setId(resultSet.getInt("id"));
                event.setTitle(resultSet.getString("title"));
                event.setDescription(resultSet.getString("description"));
                event.setDate(resultSet.getDate("date"));
                event.setLieux(resultSet.getString("lieux"));
                event.setStatus(resultSet.getString("status"));
                event.setDuree(resultSet.getString("duree"));
                event.setNb_place_dispo(resultSet.getInt("nb_place_dispo"));
                int typeEventId = resultSet.getInt("type_event_id");
                if (typeEventId > 0) {
                    event.setTypeEvent(typeEventService.getById(typeEventId));
                }
                events.add(event);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return events;
    }

    public void ajouter(Event e) {
        String query = "INSERT INTO evenment (title, date, description, lieux, status, duree, nb_place_dispo, type_event_id, artist_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        Connection connection = null;
        try {
            connection = MyConnection.getInstance().getConnection();
            connection.setAutoCommit(false);

            // Vérifier si la table evenment existe
            try (Statement stmt = connection.createStatement()) {
                ResultSet rs = stmt.executeQuery("SHOW TABLES LIKE 'evenment'");
                if (!rs.next()) {
                    throw new SQLException("La table 'evenment' n'existe pas.");
                }
            }

            try (PreparedStatement pstmt = connection.prepareStatement(query)) {
                pstmt.setString(1, e.getTitle());
                pstmt.setDate(2, e.getDate());
                pstmt.setString(3, e.getDescription());
                pstmt.setString(4, e.getLieux());
                pstmt.setString(5, e.getStatus());
                pstmt.setString(6, e.getDuree());
                pstmt.setInt(7, e.getNb_place_dispo());
                pstmt.setInt(8, e.getTypeEvent().getId());
                pstmt.setInt(9, e.getArtistId());

                int affectedRows = pstmt.executeUpdate();
                if (affectedRows == 0) {
                    throw new SQLException("Creating event failed, no rows affected.");
                }
                connection.commit();
            }
        } catch (SQLException ex) {
            if (connection != null) {
                try { connection.rollback(); } catch (SQLException e1) { }
            }
            ex.printStackTrace();
        } finally {
            if (connection != null) {
                try {
                    connection.setAutoCommit(true);
                    connection.close();
                } catch (SQLException e1) {
                    System.err.println("Erreur lors de la fermeture de la connexion : " + e1.getMessage());
                }
            }
        }
    }

    public void update(Event e) {
        String query = "UPDATE evenment SET title = ?, date = ?, description = ?, lieux = ?, " +
                      "status = ?, duree = ?, nb_place_dispo = ?, type_event_id = ? WHERE id = ?";

        try (PreparedStatement pstmt = MyConnection.getInstance().getConnection().prepareStatement(query)) {
            pstmt.setString(1, e.getTitle());
            pstmt.setDate(2, e.getDate());
            pstmt.setString(3, e.getDescription());
            pstmt.setString(4, e.getLieux());
            pstmt.setString(5, e.getStatus());
            pstmt.setString(6, e.getDuree());
            pstmt.setInt(7, e.getNb_place_dispo());
            pstmt.setInt(8, e.getTypeEvent() != null ? e.getTypeEvent().getId() : 0);
            pstmt.setInt(9, e.getId());

            pstmt.executeUpdate();
        } catch (SQLException ex) {
            System.err.println("Error updating event: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public void delete(int id) {
        String query = "DELETE FROM evenment WHERE id = ?";
        try (PreparedStatement pstmt = MyConnection.getInstance().getConnection().prepareStatement(query)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException ex) {
            System.err.println("Error deleting event: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public void supprimer(int id) {
        String query = "DELETE FROM evenment WHERE id = ?";
        try (PreparedStatement pstmt = MyConnection.getInstance().getConnection().prepareStatement(query)) {
            pstmt.setInt(1, id);
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("Event deleted successfully: ID " + id);
            } else {
                System.out.println("No event found with ID " + id);
            }
        } catch (SQLException ex) {
            System.err.println("Error deleting event: " + ex.getMessage());
            ex.printStackTrace();
            throw new RuntimeException("Failed to delete event: " + ex.getMessage(), ex);
        }
    }

    public void modifier(Event eventToModify) {
        String query = "UPDATE evenment SET title = ?, date = ?, description = ?, lieux = ?, " +
                      "status = ?, duree = ?, nb_place_dispo = ?, type_event_id = ? WHERE id = ?";

        Connection connection = null;
        try {
            connection = MyConnection.getInstance().getConnection();
            connection.setAutoCommit(false);

            // Vérifier si le type d'événement existe
            String checkTypeQuery = "SELECT id FROM type_event WHERE id = ?";
            try (PreparedStatement checkStmt = connection.prepareStatement(checkTypeQuery)) {
                checkStmt.setInt(1, eventToModify.getTypeEvent().getId());
                ResultSet rs = checkStmt.executeQuery();
                if (!rs.next()) {
                    System.err.println("ERREUR: Le type d'événement avec l'ID " + eventToModify.getTypeEvent().getId() + " n'existe pas");
                    return;
                }
            }

            try (PreparedStatement pstmt = connection.prepareStatement(query)) {
                System.out.println("\nTentative de modification d'un événement avec les valeurs suivantes :");
                System.out.println("ID: " + eventToModify.getId());
                System.out.println("Titre: " + eventToModify.getTitle());
                System.out.println("Date: " + eventToModify.getDate());
                System.out.println("Description: " + eventToModify.getDescription());
                System.out.println("Lieu: " + eventToModify.getLieux());
                System.out.println("Statut: " + eventToModify.getStatus());
                System.out.println("Durée: " + eventToModify.getDuree());
                System.out.println("Nombre de places: " + eventToModify.getNb_place_dispo());
                System.out.println("Type d'événement ID: " + eventToModify.getTypeEvent().getId());

                pstmt.setString(1, eventToModify.getTitle());
                pstmt.setDate(2, eventToModify.getDate());
                pstmt.setString(3, eventToModify.getDescription());
                pstmt.setString(4, eventToModify.getLieux());
                pstmt.setString(5, eventToModify.getStatus());
                pstmt.setString(6, eventToModify.getDuree());
                pstmt.setInt(7, eventToModify.getNb_place_dispo());
                pstmt.setInt(8, eventToModify.getTypeEvent().getId());
                pstmt.setInt(9, eventToModify.getId());

                int affectedRows = pstmt.executeUpdate();
                System.out.println("Nombre de lignes affectées : " + affectedRows);

                if (affectedRows > 0) {
                    connection.commit();
                    System.out.println("Modification réussie");
                } else {
                    System.err.println("Aucune ligne n'a été modifiée");
                    connection.rollback();
                }
            }
        } catch (SQLException ex) {
            System.err.println("Erreur SQL lors de la modification de l'événement : " + ex.getMessage());
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException e) {
                    System.err.println("Erreur lors de l'annulation de la transaction : " + e.getMessage());
                }
            }
            ex.printStackTrace();
        } finally {
            if (connection != null) {
                try {
                    connection.setAutoCommit(true);
                    connection.close();
                } catch (SQLException e) {
                    System.err.println("Erreur lors de la fermeture de la connexion : " + e.getMessage());
                }
            }
        }
    }

    // Get all events created by a specific artist (user)
    public List<Event> getByArtistId(int artistId) {
        List<Event> events = new ArrayList<>();
        String query = "SELECT * FROM evenment WHERE artist_id = ?";
        try (Connection connection = MyConnection.getInstance().getConnection();
             PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, artistId);
            ResultSet resultSet = pstmt.executeQuery();
            while (resultSet.next()) {
                Event event = new Event();
                event.setId(resultSet.getInt("id"));
                event.setTitle(resultSet.getString("title"));
                event.setDescription(resultSet.getString("description"));
                event.setDate(resultSet.getDate("date"));
                event.setLieux(resultSet.getString("lieux"));
                event.setStatus(resultSet.getString("status"));
                event.setDuree(resultSet.getString("duree"));
                event.setNb_place_dispo(resultSet.getInt("nb_place_dispo"));
                int typeEventId = resultSet.getInt("type_event_id");
                if (typeEventId > 0) {
                    event.setTypeEvent(typeEventService.getById(typeEventId));
                }
                events.add(event);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return events;
    }
}