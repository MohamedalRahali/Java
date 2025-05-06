package service;

import model.Formation;
import model.Participant;
import utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FormationService implements IService<Formation> {

    private Connection con;

    public FormationService() {
        this.con = MyDatabase.getInstance().getCnx();
    }

    @Override
    public boolean add(Formation formation) throws SQLException {
        String query = "INSERT INTO formation (titre, description, prix, places_disponibles, categorie_id) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(query)) {
            ps.setString(1, formation.getTitre());
            ps.setString(2, formation.getDescription());
            ps.setDouble(3, formation.getPrix());
            ps.setInt(4, formation.getPlacesDisponibles());
            ps.setInt(5, formation.getCategorieId());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean update(Formation formation) throws SQLException {
        String sql = "UPDATE formation SET titre = ?, description = ?, prix = ?, places_disponibles = ?, categorie_id = ? WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, formation.getTitre());
            ps.setString(2, formation.getDescription());
            ps.setDouble(3, formation.getPrix());
            ps.setInt(4, formation.getPlacesDisponibles());
            ps.setInt(5, formation.getCategorieId());
            ps.setInt(6, formation.getId());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public void delete(int id) {
        String titreFormation = "";
        List<Participant> participants = new ArrayList<>();

        // استرجاع عنوان التكوين
        String requeteSelection = "SELECT titre FROM formation WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(requeteSelection)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                titreFormation = rs.getString("titre");
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur sélection : " + e.getMessage());
            return; // إنهاء الدالة إذا فشل الاستعلام
        }

        // استرجاع المشاركين
        try {
            ParticipantService serviceParticipant = new ParticipantService();
            participants = serviceParticipant.getParticipantsParFormation(id);
        } catch (SQLException e) {
            System.err.println("❌ Erreur récupération participants : " + e.getMessage());
            return; // إنهاء الدالة إذا فشل استرجاع المشاركين
        }

        // حذف التكوين
        String requeteSuppression = "DELETE FROM formation WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(requeteSuppression)) {
            ps.setInt(1, id);
            ps.executeUpdate();
            System.out.println("✅ Formation supprimée !");
        } catch (SQLException e) {
            System.err.println("❌ Erreur de suppression : " + e.getMessage());
            return; // إنهاء الدالة إذا فشل الحذف
        }

        // إرسال إشعارات الإلغاء إذا كان هناك مشاركون
        if (!participants.isEmpty()) {
            try {
                NotificationService serviceNotification = new NotificationService();
                serviceNotification.envoyerNotificationAnnulation(id, titreFormation, participants);
            } catch (Exception e) {
                System.err.println("❌ Erreur envoi notification : " + e.getMessage());
            }
        }
    }

    @Override
    public boolean validate(Formation formation) throws IllegalArgumentException {
        if (formation.getTitre() == null || formation.getTitre().isEmpty()) {
            throw new IllegalArgumentException("Title is required");
        }
        if (formation.getPrix() <= 0) {
            throw new IllegalArgumentException("Price must be positive");
        }
        if (formation.getPlacesDisponibles() < 0) {
            throw new IllegalArgumentException("Available places cannot be negative");
        }
        return true;
    }

    @Override
    public Formation getById(int id) throws SQLException {
        String sql = "SELECT * FROM formation WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new Formation(
                    rs.getInt("id"),
                    rs.getString("titre"),
                    rs.getString("description"),
                    rs.getDouble("prix"),
                    rs.getInt("places_disponibles"),
                    rs.getInt("categorie_id")
                );
            }
        }
        return null;
    }

    @Override
    public List<Formation> getAll() throws SQLException {
        List<Formation> formations = new ArrayList<>();
        String query = "SELECT * FROM formation";

        try (Statement statement = con.createStatement();
             ResultSet rs = statement.executeQuery(query)) {
            while (rs.next()) {
                formations.add(new Formation(
                    rs.getInt("id"),
                    rs.getString("titre"),
                    rs.getString("description"),
                    rs.getDouble("prix"),
                    rs.getInt("places_disponibles"),
                    rs.getInt("categorie_id")
                ));
            }
        }
        return formations;
    }

    @Override
    public List<Formation> display() throws SQLException {
        return getAll();
    }

    public List<Formation> getByCategory(int categorieId) throws SQLException {
        List<Formation> formations = new ArrayList<>();
        String query = "SELECT * FROM formation WHERE categorieId = ?";
        
        try (PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setInt(1, categorieId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                formations.add(new Formation(
                    rs.getInt("id"),
                    rs.getString("titre"),
                    rs.getString("description"),
                    rs.getDouble("prix"),
                    rs.getInt("placesDisponibles"),
                    rs.getInt("categorieId")
                ));
            }
        }
        return formations;
    }
}
