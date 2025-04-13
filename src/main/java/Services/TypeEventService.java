package Services;

import models.TypeEvent;
import util.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public abstract class TypeEventService implements IService<TypeEvent> {

    public final Connection conn;

    public TypeEventService() {
        this.conn = MyConnection.getInstance().getCnx();
    }

    @Override
    public void add(TypeEvent typeEvent) {
        if (associationExists(typeEvent)) {
            throw new RuntimeException("Cette association existe déjà");
        }

        String SQL = "INSERT INTO evenment_t (evenment_id, t_id) VALUES (?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(SQL)) {
            pstmt.setInt(1, typeEvent.getEvenment_id());
            pstmt.setInt(2, typeEvent.getT_id());
            pstmt.executeUpdate();
            System.out.println("Association ajoutée avec succès !");
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de l'ajout de l'association : " + e.getMessage());
        }
    }



    public void update(TypeEvent oldTypeEvent, TypeEvent newTypeEvent) {
        if (!associationExists(oldTypeEvent)) {
            throw new RuntimeException("L'association à modifier n'existe pas");
        }

        if (associationExists(newTypeEvent)) {
            throw new RuntimeException("La nouvelle association existe déjà");
        }

        // D'abord supprimer l'ancienne association
        delete(oldTypeEvent);

        // Puis ajouter la nouvelle
        add(newTypeEvent);
    }

    @Override
    public void delete(TypeEvent typeEvent) {
        String SQL = "DELETE FROM evenment_t WHERE evenment_id = ? AND t_id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(SQL)) {
            pstmt.setInt(1, typeEvent.getEvenment_id());
            pstmt.setInt(2, typeEvent.getT_id());
            int rowsDeleted = pstmt.executeUpdate();
            if (rowsDeleted > 0) {
                System.out.println("Association supprimée avec succès !");
            } else {
                throw new RuntimeException("Aucune association trouvée avec ces IDs");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la suppression de l'association : " + e.getMessage());
        }
    }

    @Override
    public List<TypeEvent> getAll() {
        ArrayList<TypeEvent> typeEvents = new ArrayList<>();
        String req = "SELECT * FROM evenment_t";

        try {
            Statement stm = conn.createStatement();
            ResultSet rs = stm.executeQuery(req);

            while (rs.next()) {
                TypeEvent te = new TypeEvent(
                        rs.getInt("evenment_id"),
                        rs.getInt("t_id")
                );
                typeEvents.add(te);
            }

            rs.close();
            stm.close();

        } catch (SQLException ex) {
            throw new RuntimeException("Erreur lors de la récupération des associations : " + ex.getMessage());
        }

        return typeEvents;
    }

    private boolean associationExists(TypeEvent typeEvent) {
        String SQL = "SELECT COUNT(*) FROM evenment_t WHERE evenment_id = ? AND t_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(SQL)) {
            pstmt.setInt(1, typeEvent.getEvenment_id());
            pstmt.setInt(2, typeEvent.getT_id());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la vérification de l'association : " + e.getMessage());
        }
        return false;
    }

    public List<TypeEvent> getByEventId(int eventId) {
        ArrayList<TypeEvent> typeEvents = new ArrayList<>();
        String req = "SELECT * FROM evenment_t WHERE evenment_id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(req)) {
            pstmt.setInt(1, eventId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                TypeEvent te = new TypeEvent(
                        rs.getInt("evenment_id"),
                        rs.getInt("t_id")
                );
                typeEvents.add(te);
            }

            rs.close();
        } catch (SQLException ex) {
            throw new RuntimeException("Erreur lors de la récupération par ID événement : " + ex.getMessage());
        }

        return typeEvents;
    }

    public List<TypeEvent> getByTypeId(int typeId) {
        ArrayList<TypeEvent> typeEvents = new ArrayList<>();
        String req = "SELECT * FROM evenment_t WHERE t_id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(req)) {
            pstmt.setInt(1, typeId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                TypeEvent te = new TypeEvent(
                        rs.getInt("evenment_id"),
                        rs.getInt("t_id")
                );
                typeEvents.add(te);
            }

            rs.close();
        } catch (SQLException ex) {
            throw new RuntimeException("Erreur lors de la récupération par ID type : " + ex.getMessage());
        }

        return typeEvents;
    }
}