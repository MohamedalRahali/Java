package service;

import model.Commentaire;
import utils.MyDatabse;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CommentaireCRUD {
    private Connection cnx;

    public CommentaireCRUD() {
        cnx = MyDatabse.getInstance().getCnx();
    }

    public void add(Commentaire commentaire) throws SQLException {
        String sql = "INSERT INTO commentaire (contenu, date_creation, blog_id, user_id) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, commentaire.getContenu());
            ps.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
            ps.setInt(3, commentaire.getBlogId());
            ps.setInt(4, commentaire.getUserId());
            ps.executeUpdate();

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    commentaire.setId(generatedKeys.getInt(1));
                }
            }
            System.out.println("✅ Commentaire ajouté avec succès !");
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de l'ajout du commentaire : " + e.getMessage());
            throw e;
        }
    }

    public List<Commentaire> getCommentsByBlogId(int blogId) throws SQLException {
        List<Commentaire> comments = new ArrayList<>();
        String sql = "SELECT c.*, u.nom, u.prenom FROM commentaire c " +
                    "JOIN user u ON c.user_id = u.id " +
                    "WHERE c.blog_id = ? " +
                    "ORDER BY c.date_creation DESC";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, blogId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Commentaire comment = new Commentaire();
                    comment.setId(rs.getInt("id"));
                    comment.setContenu(rs.getString("contenu"));
                    comment.setDateCreation(rs.getTimestamp("date_creation"));
                    comment.setBlogId(rs.getInt("blog_id"));
                    comment.setUserId(rs.getInt("user_id"));
                    comments.add(comment);
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la récupération des commentaires : " + e.getMessage());
            throw e;
        }

        return comments;
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM commentaire WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("✅ Commentaire supprimé avec succès !");
            } else {
                System.out.println("⚠️ Aucun commentaire trouvé avec l'ID : " + id);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la suppression du commentaire : " + e.getMessage());
            throw e;
        }
    }
} 