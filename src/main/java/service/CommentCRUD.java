package service;

import model.Comment;
import utils.MyDatabse;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CommentCRUD {
    private Connection cnx;

    public CommentCRUD() {
        cnx = MyDatabse.getInstance().getCnx();
    }

    public Connection getCnx() {
        return cnx;
    }

    public void add(Comment comment) throws SQLException {
        String sql = "INSERT INTO comment (content, created_at, relat_id, is_reported) VALUES (?, ?, ?, 0)";
        try (PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, comment.getContenu());
            ps.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
            ps.setInt(3, comment.getBlogId());
            ps.executeUpdate();

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    comment.setId(generatedKeys.getInt(1));
                }
            }
            System.out.println("Comment ajouté avec succès !");
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'ajout du comment : " + e.getMessage());
            throw e;
        }
    }

    public List<Comment> getCommentsByBlogId(int blogId) throws SQLException {
        List<Comment> comments = new ArrayList<>();
        String sql = "SELECT * FROM comment WHERE relat_id = ? ORDER BY created_at DESC";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, blogId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Comment comment = new Comment();
                    comment.setId(rs.getInt("id"));
                    comment.setContenu(rs.getString("content"));
                    comment.setDateCreation(rs.getTimestamp("created_at"));
                    comment.setBlogId(rs.getInt("relat_id"));
                    comment.setReported(rs.getBoolean("is_reported"));
                    comment.setReportCount(rs.getInt("report_count"));
                    comments.add(comment);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des comments : " + e.getMessage());
            throw e;
        }

        return comments;
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM comment WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Comment supprimé avec succès !");
            } else {
                System.out.println("Aucun comment trouvé avec l'ID : " + id);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression du comment : " + e.getMessage());
            throw e;
        }
    }

    public void reportComment(int commentId) throws SQLException {
        String selectSql = "SELECT report_count FROM comment WHERE id = ?";
        int currentReportCount = 0;
        
        try (PreparedStatement ps = cnx.prepareStatement(selectSql)) {
            ps.setInt(1, commentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    currentReportCount = rs.getInt("report_count");
                }
            }
        }

        if (currentReportCount >= 3) {
            System.out.println("Ce commentaire a déjà été signalé le maximum de fois permis.");
            return;
        }

        currentReportCount++;

        if (currentReportCount >= 3) {
            delete(commentId);
            System.out.println("Comment supprimé car signalé 3 fois !");
        } else {
            String updateSql = "UPDATE comment SET is_reported = 1, report_count = ? WHERE id = ?";
            try (PreparedStatement ps = cnx.prepareStatement(updateSql)) {
                ps.setInt(1, currentReportCount);
                ps.setInt(2, commentId);
                int rowsAffected = ps.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("Comment signalé avec succès ! (Signalement " + currentReportCount + "/3)");
                } else {
                    System.out.println("Aucun comment trouvé avec l'ID : " + commentId);
                }
            } catch (SQLException e) {
                System.err.println("Erreur lors du signalement du comment : " + e.getMessage());
                throw e;
            }
        }
    }

    public void update(Comment comment) throws SQLException {
        String sql = "UPDATE comment SET content = ? WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, comment.getContenu());
            ps.setInt(2, comment.getId());
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Comment modifié avec succès !");
            } else {
                System.out.println("Aucun comment trouvé avec l'ID : " + comment.getId());
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la modification du comment : " + e.getMessage());
            throw e;
        }
    }
}