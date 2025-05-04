package Services;

import models.Reponse;
import util.MyConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReponseService {
    private Connection connection;
    private static final Logger LOGGER = LoggerFactory.getLogger(ReponseService.class);

    public ReponseService() {
        this.connection = MyConnection.getInstance().getConnection();
        if (this.connection == null) {
            LOGGER.error("La connexion à la base de données est nulle!");
            throw new RuntimeException("Impossible d'établir la connexion à la base de données");
        }
        LOGGER.info("ReponseService initialisé avec succès");
    }

    public void ajouter(Reponse reponse) {
        LOGGER.info("Tentative d'ajout d'une nouvelle réponse");
        
        String sql = "INSERT INTO reponse (reclamation_id, message, created_at) VALUES (?, ?, ?)";
        PreparedStatement pstmt = null;
        ResultSet generatedKeys = null;
        
        try {
            pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setInt(1, reponse.getReclamationId());
            pstmt.setString(2, reponse.getMessage());
            pstmt.setTimestamp(3, Timestamp.valueOf(reponse.getCreatedAt()));
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("La création de la réponse a échoué, aucune ligne n'a été affectée.");
            }
            
            generatedKeys = pstmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                reponse.setId(generatedKeys.getInt(1));
                LOGGER.info("Réponse ajoutée avec succès. ID: {}", reponse.getId());
            } else {
                throw new SQLException("La création de la réponse a échoué, aucun ID n'a été généré.");
            }
            
        } catch (SQLException e) {
            LOGGER.error("Erreur lors de l'ajout de la réponse", e);
            throw new RuntimeException("Erreur lors de l'ajout de la réponse: " + e.getMessage());
        } finally {
            try {
                if (generatedKeys != null) generatedKeys.close();
                if (pstmt != null) pstmt.close();
            } catch (SQLException e) {
                LOGGER.error("Erreur lors de la fermeture des ressources", e);
            }
        }
    }

    public List<Reponse> getReponsesByReclamationId(int reclamationId) {
        LOGGER.info("Récupération des réponses pour la réclamation ID: {}", reclamationId);
        List<Reponse> reponses = new ArrayList<>();
        
        String sql = "SELECT * FROM reponse WHERE reclamation_id = ? ORDER BY created_at DESC";
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            pstmt = connection.prepareStatement(sql);
            pstmt.setInt(1, reclamationId);
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Reponse reponse = new Reponse(
                    rs.getInt("id"),
                    rs.getInt("reclamation_id"),
                    rs.getString("message"),
                    rs.getTimestamp("created_at").toLocalDateTime()
                );
                reponses.add(reponse);
            }
            
            LOGGER.info("{} réponses trouvées pour la réclamation ID: {}", reponses.size(), reclamationId);
            
        } catch (SQLException e) {
            LOGGER.error("Erreur lors de la récupération des réponses", e);
            throw new RuntimeException("Erreur lors de la récupération des réponses: " + e.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
            } catch (SQLException e) {
                LOGGER.error("Erreur lors de la fermeture des ressources", e);
            }
        }
        
        return reponses;
    }

    public void supprimer(int id) {
        LOGGER.info("Tentative de suppression de la réponse ID: {}", id);
        
        String sql = "DELETE FROM reponse WHERE id = ?";
        PreparedStatement pstmt = null;
        
        try {
            pstmt = connection.prepareStatement(sql);
            pstmt.setInt(1, id);
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                LOGGER.info("Réponse supprimée avec succès. ID: {}", id);
            } else {
                LOGGER.warn("Aucune réponse n'a été supprimée. ID: {}", id);
            }
            
        } catch (SQLException e) {
            LOGGER.error("Erreur lors de la suppression de la réponse", e);
            throw new RuntimeException("Erreur lors de la suppression de la réponse: " + e.getMessage());
        } finally {
            try {
                if (pstmt != null) pstmt.close();
            } catch (SQLException e) {
                LOGGER.error("Erreur lors de la fermeture des ressources", e);
            }
        }
    }
} 