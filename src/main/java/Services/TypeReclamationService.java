package Services;

import models.TypeReclamation;
import util.MyConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TypeReclamationService {
    private static final Logger logger = LoggerFactory.getLogger(TypeReclamationService.class);
    private Connection connection;

    public TypeReclamationService() {
        this.connection = MyConnection.getInstance().getConnection();
        if (this.connection == null) {
            logger.error("La connexion à la base de données est nulle!");
            throw new RuntimeException("Impossible d'établir la connexion à la base de données");
        }
        logger.info("TypeReclamationService initialisé avec succès");
    }

    public void ajouter(TypeReclamation type) throws SQLException {
        String sql = "INSERT INTO type_reclamation (name) VALUES (?)";
        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setString(1, type.getName());
            pst.executeUpdate();
            logger.info("Type de réclamation ajouté avec succès: {}", type.getName());
        } catch (SQLException e) {
            logger.error("Erreur lors de l'ajout du type de réclamation: {}", e.getMessage(), e);
            throw e;
        }
    }

    public void modifier(TypeReclamation type) throws SQLException {
        String sql = "UPDATE type_reclamation SET name = ? WHERE id = ?";
        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setString(1, type.getName());
            pst.setInt(2, type.getId());
            pst.executeUpdate();
            logger.info("Type de réclamation modifié avec succès: {}", type.getName());
        } catch (SQLException e) {
            logger.error("Erreur lors de la modification du type de réclamation: {}", e.getMessage(), e);
            throw e;
        }
    }

    public void supprimerReclamationsByType(int typeId) throws SQLException {
        String sql = "DELETE FROM reclamation WHERE type_reclamation_id = ?";
        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setInt(1, typeId);
            int deletedCount = pst.executeUpdate();
            logger.info("{} réclamations supprimées pour le type ID: {}", deletedCount, typeId);
        } catch (SQLException e) {
            logger.error("Erreur lors de la suppression des réclamations du type: {}", e.getMessage(), e);
            throw e;
        }
    }

    public void supprimer(int id) throws SQLException {
        logger.info("Tentative de suppression du type de réclamation ID: {}", id);
        
        // D'abord supprimer toutes les réclamations associées
        supprimerReclamationsByType(id);
        
        // Ensuite supprimer le type
        String sql = "DELETE FROM type_reclamation WHERE id = ?";
        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setInt(1, id);
            int result = pst.executeUpdate();
            if (result > 0) {
                logger.info("Type de réclamation supprimé avec succès, ID: {}", id);
            } else {
                logger.warn("Aucun type de réclamation n'a été supprimé (ID: {})", id);
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de la suppression du type de réclamation: {}", e.getMessage(), e);
            throw e;
        }
    }

    public List<TypeReclamation> getAll() {
        List<TypeReclamation> types = new ArrayList<>();
        String sql = "SELECT * FROM type_reclamation ORDER BY name";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                TypeReclamation type = new TypeReclamation();
                type.setId(rs.getInt("id"));
                type.setName(rs.getString("name"));
                types.add(type);
            }
            
            logger.info("{} types de réclamation récupérés", types.size());
        } catch (SQLException e) {
            logger.error("Erreur lors de la récupération des types de réclamation: {}", e.getMessage(), e);
            throw new RuntimeException("Erreur lors de la récupération des types de réclamation", e);
        }
        
        return types;
    }

    public TypeReclamation getById(int id) throws SQLException {
        String sql = "SELECT * FROM type_reclamation WHERE id = ?";
        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setInt(1, id);
            ResultSet rs = pst.executeQuery();
            
            if (rs.next()) {
                TypeReclamation type = new TypeReclamation();
                type.setId(rs.getInt("id"));
                type.setName(rs.getString("name"));
                return type;
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de la récupération du type de réclamation: {}", e.getMessage(), e);
            throw e;
        }
        return null;
    }
} 