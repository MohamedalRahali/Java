package Services;

import models.Reclamation;
import models.TypeReclamation;
import util.MyConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReclamationService {
    private Connection connection;
    private static final Logger LOGGER = LoggerFactory.getLogger(ReclamationService.class);

    public ReclamationService() {
        this.connection = MyConnection.getInstance().getCnx();
        if (this.connection == null) {
            LOGGER.error("La connexion à la base de données est nulle!");
            throw new RuntimeException("Impossible d'établir la connexion à la base de données");
        }
        LOGGER.info("ReclamationService initialisé avec succès");
    }

    public void ajouter(Reclamation reclamation) {
        LOGGER.info("Tentative d'ajout d'une nouvelle réclamation: {}", reclamation.getTitle());
        
        // Vérifier la connexion
        if (connection == null) {
            LOGGER.error("La connexion est nulle!");
            connection = MyConnection.getInstance().getCnx();
            if (connection == null) {
                LOGGER.error("Impossible d'établir la connexion à la base de données");
                throw new RuntimeException("Impossible d'établir la connexion à la base de données");
            }
        }
        
        String sql = "INSERT INTO reclamation (title, description, created_at, type_reclamation_id, fingerprint_verified) VALUES (?, ?, ?, ?, ?)";
        PreparedStatement pstmt = null;
        ResultSet generatedKeys = null;
        
        try {
            pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            
            // Vérifier les données avant l'insertion
            LOGGER.info("Données à insérer:");
            LOGGER.info("Titre: {}", reclamation.getTitle());
            LOGGER.info("Description: {}", reclamation.getDescription());
            LOGGER.info("Date de création: {}", reclamation.getCreatedAt());
            LOGGER.info("Type ID: {}", reclamation.getTypeReclamation().getId());
            
            pstmt.setString(1, reclamation.getTitle());
            pstmt.setString(2, reclamation.getDescription());
            pstmt.setTimestamp(3, Timestamp.valueOf(reclamation.getCreatedAt()));
            pstmt.setInt(4, reclamation.getTypeReclamation().getId());
            pstmt.setBoolean(5, reclamation.isFingerprintVerified());
            
            LOGGER.info("Exécution de la requête SQL: {}", sql);
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("La création de la réclamation a échoué, aucune ligne n'a été affectée.");
            }
            
            generatedKeys = pstmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                reclamation.setId(generatedKeys.getInt(1));
                LOGGER.info("Réclamation ajoutée avec succès. ID: {}", reclamation.getId());
            } else {
                throw new SQLException("La création de la réclamation a échoué, aucun ID n'a été généré.");
            }
            
            // Vérifier que la réclamation a bien été ajoutée
            String checkSql = "SELECT * FROM reclamation WHERE id = ?";
            PreparedStatement checkStmt = null;
            ResultSet checkRs = null;
            
            try {
                checkStmt = connection.prepareStatement(checkSql);
                checkStmt.setInt(1, reclamation.getId());
                checkRs = checkStmt.executeQuery();
                if (checkRs.next()) {
                    LOGGER.info("Vérification réussie: la réclamation existe dans la base de données");
                } else {
                    LOGGER.error("La réclamation n'a pas été trouvée dans la base de données après l'insertion!");
                }
            } finally {
                if (checkRs != null) try { checkRs.close(); } catch (SQLException e) { }
                if (checkStmt != null) try { checkStmt.close(); } catch (SQLException e) { }
            }
            
        } catch (SQLException e) {
            LOGGER.error("Erreur lors de l'ajout de la réclamation", e);
            throw new RuntimeException("Erreur lors de l'ajout de la réclamation: " + e.getMessage());
        } finally {
            try {
                if (generatedKeys != null) generatedKeys.close();
                if (pstmt != null) pstmt.close();
            } catch (SQLException e) {
                LOGGER.error("Erreur lors de la fermeture des ressources", e);
            }
        }
    }

    public List<Reclamation> getAll() {
        LOGGER.info("Récupération de toutes les réclamations");
        List<Reclamation> reclamations = new ArrayList<>();
        
        // Vérifier la connexion
        if (connection == null) {
            LOGGER.error("La connexion est nulle!");
            connection = MyConnection.getInstance().getCnx();
            if (connection == null) {
                LOGGER.error("Impossible d'établir la connexion à la base de données");
                return reclamations;
            }
        }
        
        // Vérifier d'abord si la table existe
        Statement checkTableStmt = null;
        ResultSet tableRs = null;
        try {
            checkTableStmt = connection.createStatement();
            tableRs = checkTableStmt.executeQuery("SHOW TABLES LIKE 'reclamation'");
            if (!tableRs.next()) {
                LOGGER.error("La table 'reclamation' n'existe pas dans la base de données!");
                return reclamations;
            }
            LOGGER.info("La table 'reclamation' existe");
        } catch (SQLException e) {
            LOGGER.error("Erreur lors de la vérification de la table: {}", e.getMessage());
            return reclamations;
        } finally {
            if (tableRs != null) try { tableRs.close(); } catch (SQLException e) { }
            if (checkTableStmt != null) try { checkTableStmt.close(); } catch (SQLException e) { }
        }
        
        // Vérifier le nombre de réclamations dans la table
        Statement countStmt = null;
        ResultSet countRs = null;
        try {
            countStmt = connection.createStatement();
            countRs = countStmt.executeQuery("SELECT COUNT(*) as count FROM reclamation");
            if (countRs.next()) {
                int count = countRs.getInt("count");
                LOGGER.info("Nombre total de réclamations dans la base de données: {}", count);
            }
        } catch (SQLException e) {
            LOGGER.error("Erreur lors du comptage des réclamations: {}", e.getMessage());
        } finally {
            if (countRs != null) try { countRs.close(); } catch (SQLException e) { }
            if (countStmt != null) try { countStmt.close(); } catch (SQLException e) { }
        }
        
        String sql = "SELECT r.*, tr.name as type_name FROM reclamation r LEFT JOIN type_reclamation tr ON r.type_reclamation_id = tr.id ORDER BY r.created_at DESC";
        Statement stmt = null;
        ResultSet rs = null;
        
        try {
            LOGGER.info("Exécution de la requête SQL: {}", sql);
            stmt = connection.createStatement();
            rs = stmt.executeQuery(sql);
            
            int count = 0;
            while (rs.next()) {
                count++;
                LOGGER.info("Lecture de la réclamation #{}", count);
                
                Reclamation reclamation = new Reclamation();
                reclamation.setId(rs.getInt("id"));
                reclamation.setTitle(rs.getString("title"));
                reclamation.setDescription(rs.getString("description"));
                reclamation.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                
                TypeReclamation type = new TypeReclamation();
                type.setId(rs.getInt("type_reclamation_id"));
                type.setName(rs.getString("type_name"));
                reclamation.setTypeReclamation(type);
                
                reclamations.add(reclamation);
                LOGGER.info("Réclamation chargée: {} (ID: {})", reclamation.getTitle(), reclamation.getId());
            }
            
            LOGGER.info("Nombre total de réclamations récupérées: {}", reclamations.size());
            
            if (reclamations.isEmpty()) {
                LOGGER.warn("Aucune réclamation n'a été trouvée dans la base de données");
            }
            
        } catch (SQLException e) {
            LOGGER.error("Erreur lors de la récupération des réclamations: {}", e.getMessage(), e);
            throw new RuntimeException("Erreur lors de la récupération des réclamations: " + e.getMessage());
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) { }
            if (stmt != null) try { stmt.close(); } catch (SQLException e) { }
        }
        return reclamations;
    }

    public void modifier(Reclamation reclamation) {
        LOGGER.info("Tentative de modification de la réclamation: {}", reclamation.getTitle());
        
        // Vérifier la connexion
        if (connection == null) {
            LOGGER.error("La connexion est nulle!");
            connection = MyConnection.getInstance().getCnx();
            if (connection == null) {
                LOGGER.error("Impossible d'établir la connexion à la base de données");
                throw new RuntimeException("Impossible d'établir la connexion à la base de données");
            }
        }
        
        String sql = "UPDATE reclamation SET title = ?, description = ?, type_reclamation_id = ? WHERE id = ?";
        PreparedStatement pstmt = null;
        
        try {
            pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, reclamation.getTitle());
            pstmt.setString(2, reclamation.getDescription());
            pstmt.setInt(3, reclamation.getTypeReclamation().getId());
            pstmt.setInt(4, reclamation.getId());
            
            LOGGER.info("Exécution de la requête SQL: {}", sql);
            int result = pstmt.executeUpdate();
            if (result > 0) {
                LOGGER.info("Réclamation modifiée avec succès: {}", reclamation.getTitle());
            } else {
                LOGGER.warn("Aucune réclamation n'a été modifiée");
            }
        } catch (SQLException e) {
            LOGGER.error("Erreur lors de la modification de la réclamation: {}", e.getMessage(), e);
            throw new RuntimeException("Erreur lors de la modification de la réclamation: " + e.getMessage());
        } finally {
            if (pstmt != null) try { pstmt.close(); } catch (SQLException e) { }
        }
    }

    public void supprimer(int id) {
        LOGGER.info("Tentative de suppression de la réclamation avec l'ID: {}", id);
        
        // Vérifier la connexion
        if (connection == null) {
            LOGGER.error("La connexion est nulle!");
            connection = MyConnection.getInstance().getCnx();
            if (connection == null) {
                LOGGER.error("Impossible d'établir la connexion à la base de données");
                throw new RuntimeException("Impossible d'établir la connexion à la base de données");
            }
        }
        
        String sql = "DELETE FROM reclamation WHERE id = ?";
        PreparedStatement pstmt = null;
        
        try {
            pstmt = connection.prepareStatement(sql);
            pstmt.setInt(1, id);
            
            LOGGER.info("Exécution de la requête SQL: {}", sql);
            int result = pstmt.executeUpdate();
            if (result > 0) {
                LOGGER.info("Réclamation supprimée avec succès (ID: {})", id);
            } else {
                LOGGER.warn("Aucune réclamation n'a été supprimée (ID: {})", id);
            }
        } catch (SQLException e) {
            LOGGER.error("Erreur lors de la suppression de la réclamation: {}", e.getMessage(), e);
            throw new RuntimeException("Erreur lors de la suppression de la réclamation: " + e.getMessage());
        } finally {
            if (pstmt != null) try { pstmt.close(); } catch (SQLException e) { }
        }
    }

    public void update(Reclamation reclamation) {
        try {
            LOGGER.info("Tentative de mise à jour de la réclamation #{}", reclamation.getId());
            
            String sql = "UPDATE reclamation SET title = ?, description = ?, type_reclamation_id = ?, fingerprint_verified = ? WHERE id = ?";
            PreparedStatement ps = connection.prepareStatement(sql);
            
            ps.setString(1, reclamation.getTitle());
            ps.setString(2, reclamation.getDescription());
            ps.setInt(3, reclamation.getTypeReclamation().getId());
            ps.setBoolean(4, reclamation.isFingerprintVerified());
            ps.setInt(5, reclamation.getId());
            
            int rowsAffected = ps.executeUpdate();
            LOGGER.info("Mise à jour effectuée. {} ligne(s) modifiée(s)", rowsAffected);
            
        } catch (SQLException e) {
            LOGGER.error("Erreur lors de la mise à jour de la réclamation: {}", e.getMessage());
            e.printStackTrace();
        }
    }
}