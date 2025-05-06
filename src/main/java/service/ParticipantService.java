package service;

import model.Participant;
import utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ParticipantService {

    private Connection con;

    public ParticipantService() throws SQLException {
        con = MyDatabase.getInstance().getCnx();
    }

    public void ajouterParticipant(Participant participant) throws SQLException {
        String sql = "INSERT INTO participant (name, email) VALUES (?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, participant.getName());
            ps.setString(2, participant.getEmail());
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                participant.setId(rs.getInt(1));
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur ajout participant : " + e.getMessage());
            throw e;
        }
    }

    public void inscrireParticipantFormation(int formationId, int participantId) throws SQLException {
        String sql = "INSERT INTO formation_participant (formation_id, participant_id) VALUES (?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, formationId);
            ps.setInt(2, participantId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("❌ Erreur inscription participant : " + e.getMessage());
            throw e;
        }
    }

    public List<Participant> getParticipantsParFormation(int formationId) throws SQLException {
        List<Participant> participants = new ArrayList<>();
        String sql = "SELECT p.* FROM participant p JOIN formation_participant fp ON p.id = fp.participant_id WHERE fp.formation_id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, formationId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                participants.add(new Participant(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("email")
                ));
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur récupération participants : " + e.getMessage());
            throw e;
        }
        return participants;
    }
}