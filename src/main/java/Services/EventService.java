package Services;

import models.Event;
import util.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EventService {
    private Connection conn;

    public EventService() {
        conn = MyConnection.getInstance().getCnx();
    }

    public List<Event> getAll() {
        List<Event> events = new ArrayList<>();
        String query = "SELECT * FROM evenment";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                Event e = new Event();
                e.setId(rs.getInt("id"));
                e.setTitle(rs.getString("title"));
                e.setDate(rs.getDate("date"));
                e.setDescription(rs.getString("description"));
                e.setLieux(rs.getString("lieux"));
                e.setStatus(rs.getString("status"));
                e.setDuree(rs.getString("duree"));
                e.setNb_place_dispo(rs.getInt("nb_place_dispo"));
                events.add(e);
            }
        } catch (SQLException ex) {
            System.err.println("Error retrieving events: " + ex.getMessage());
            ex.printStackTrace();
        }

        return events;
    }

    public void ajouter(Event e) {
        String query = "INSERT INTO evenment (title, date, description, lieux, status, duree, nb_place_dispo) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, e.getTitle());
            pstmt.setDate(2, e.getDate());
            pstmt.setString(3, e.getDescription());
            pstmt.setString(4, e.getLieux());
            pstmt.setString(5, e.getStatus());
            pstmt.setString(6, e.getDuree());
            pstmt.setInt(7, e.getNb_place_dispo());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                // Get the generated ID
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        e.setId(generatedKeys.getInt(1));
                    }
                }
                System.out.println("Event added successfully with ID: " + e.getId());
            }
        } catch (SQLException ex) {
            System.err.println("Error adding event: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public void modifier(Event updated) {
        String query = "UPDATE evenment SET title = ?, date = ?, description = ?, lieux = ?, " +
                "status = ?, duree = ?, nb_place_dispo = ? WHERE id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, updated.getTitle());
            pstmt.setDate(2, updated.getDate());
            pstmt.setString(3, updated.getDescription());
            pstmt.setString(4, updated.getLieux());
            pstmt.setString(5, updated.getStatus());
            pstmt.setString(6, updated.getDuree());
            pstmt.setInt(7, updated.getNb_place_dispo());
            pstmt.setInt(8, updated.getId());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("Event updated successfully: ID " + updated.getId());
            } else {
                System.out.println("No event found with ID " + updated.getId());
            }
        } catch (SQLException ex) {
            System.err.println("Error updating event: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public void supprimer(int id) {
        String query = "DELETE FROM evenment WHERE id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
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
        }
    }
}