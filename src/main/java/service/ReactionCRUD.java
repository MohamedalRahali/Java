package service;

import model.Reaction;
import utils.MyDatabse;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReactionCRUD {
    private Connection cnx;

    public ReactionCRUD() {
        cnx = MyDatabse.getInstance().getCnx();
    }

    public void addOrUpdateReaction(Reaction reaction) throws SQLException {
        String checkSql = "SELECT id, type FROM reaction WHERE blog_id = ? AND user_id = ?";
        try (PreparedStatement checkPs = cnx.prepareStatement(checkSql)) {
            checkPs.setInt(1, reaction.getBlogId());
            checkPs.setInt(2, reaction.getUserId());
            
            try (ResultSet rs = checkPs.executeQuery()) {
                if (rs.next()) {
                    String updateSql = "UPDATE reaction SET type = ? WHERE id = ?";
                    try (PreparedStatement updatePs = cnx.prepareStatement(updateSql)) {
                        updatePs.setString(1, reaction.getType().name());
                        updatePs.setInt(2, rs.getInt("id"));
                        updatePs.executeUpdate();
                        System.out.println("Réaction mise à jour avec succès !");
                    }
                } else {
                    String insertSql = "INSERT INTO reaction (blog_id, user_id, type) VALUES (?, ?, ?)";
                    try (PreparedStatement insertPs = cnx.prepareStatement(insertSql)) {
                        insertPs.setInt(1, reaction.getBlogId());
                        insertPs.setInt(2, reaction.getUserId());
                        insertPs.setString(3, reaction.getType().name());
                        insertPs.executeUpdate();
                        System.out.println("Réaction ajoutée avec succès !");
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'ajout/mise à jour de la réaction : " + e.getMessage());
            throw e;
        }
    }

    public void removeReaction(int blogId, int userId) throws SQLException {
        String sql = "DELETE FROM reaction WHERE blog_id = ? AND user_id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, blogId);
            ps.setInt(2, userId);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Réaction supprimée avec succès !");
            } else {
                System.out.println("Aucune réaction trouvée pour cet utilisateur sur ce blog.");
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression de la réaction : " + e.getMessage());
            throw e;
        }
    }

    public List<Reaction> getReactionsByBlogId(int blogId) throws SQLException {
        List<Reaction> reactions = new ArrayList<>();
        String sql = "SELECT * FROM reaction WHERE blog_id = ?";
        
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, blogId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Reaction reaction = new Reaction();
                    reaction.setId(rs.getInt("id"));
                    reaction.setBlogId(rs.getInt("blog_id"));
                    reaction.setUserId(rs.getInt("user_id"));
                    reaction.setType(Reaction.ReactionType.valueOf(rs.getString("type")));
                    reactions.add(reaction);
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la récupération des réactions : " + e.getMessage());
            throw e;
        }
        
        return reactions;
    }

    public Reaction getUserReaction(int blogId, int userId) throws SQLException {
        String sql = "SELECT * FROM reaction WHERE blog_id = ? AND user_id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, blogId);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Reaction reaction = new Reaction();
                    reaction.setId(rs.getInt("id"));
                    reaction.setBlogId(rs.getInt("blog_id"));
                    reaction.setUserId(rs.getInt("user_id"));
                    reaction.setType(Reaction.ReactionType.valueOf(rs.getString("type")));
                    return reaction;
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération de la réaction : " + e.getMessage());
            throw e;
        }
        return null;
    }
} 