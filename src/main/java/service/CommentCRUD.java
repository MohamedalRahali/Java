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
        String sql = "INSERT INTO comment (content, created_at, relat_id, user_id, is_reported) VALUES (?, ?, ?, ?, 0)";
        try (PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, comment.getContenu());
            ps.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
            ps.setInt(3, comment.getBlogId());
            ps.setInt(4, comment.getUserId());
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
                    comment.setUserId(rs.getInt("user_id"));
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

    public List<Comment> getCommentsByBlog(int blogId) throws SQLException {
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
                    comment.setUserId(rs.getInt("user_id"));
                    comment.setReported(rs.getBoolean("is_reported"));
                    comment.setReportCount(rs.getInt("report_count"));
                    comments.add(comment);
                }
            }
        }
        return comments;
    }

    public List<Comment> getCommentsByBlogAndUser(int blogId, int userId) throws SQLException {
        List<Comment> comments = new ArrayList<>();
        String sql = "SELECT * FROM comment WHERE relat_id = ? AND user_id = ? ORDER BY created_at DESC";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, blogId);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Comment comment = new Comment();
                    comment.setId(rs.getInt("id"));
                    comment.setContenu(rs.getString("content"));
                    comment.setDateCreation(rs.getTimestamp("created_at"));
                    comment.setBlogId(rs.getInt("relat_id"));
                    comment.setUserId(rs.getInt("user_id"));
                    comment.setReported(rs.getBoolean("is_reported"));
                    comment.setReportCount(rs.getInt("report_count"));
                    comments.add(comment);
                }
            }
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

    public boolean hasUserReported(int commentId, int userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM comment_report WHERE comment_id = ? AND user_id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, commentId);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    public boolean reportCommentByUser(int commentId, int userId) throws SQLException {
        if (hasUserReported(commentId, userId)) {
            System.out.println("Cet utilisateur a déjà signalé ce commentaire.");
            return false;
        }
        // Insert into comment_report
        System.out.println("[DEBUG] Reporting comment. commentId: " + commentId + ", userId: " + userId);
        String insertSql = "INSERT INTO comment_report (comment_id, user_id) VALUES (?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(insertSql)) {
            ps.setInt(1, commentId);
            ps.setInt(2, userId);
            ps.executeUpdate();
        }
        // Increment report count and proceed as before
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
                    // NO CONSOLE PRINT HERE, UI will show feedback instead
                } else {
                    System.out.println("Aucun comment trouvé avec l'ID : " + commentId);
                }
            }
        }
        return true;
    }

    public boolean report(int commentId, int userId) throws SQLException {
        return reportCommentByUser(commentId, userId);
    }

    public List<Comment> getAll() throws SQLException {
        List<Comment> comments = new ArrayList<>();
        String sql = "SELECT * FROM comment ORDER BY created_at DESC";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Comment comment = new Comment();
                    comment.setId(rs.getInt("id"));
                    comment.setContenu(rs.getString("content"));
                    comment.setDateCreation(rs.getTimestamp("created_at"));
                    comment.setBlogId(rs.getInt("relat_id"));
                    comment.setUserId(rs.getInt("user_id"));
                    comment.setReported(rs.getBoolean("is_reported"));
                    comment.setReportCount(rs.getInt("report_count"));
                    comments.add(comment);
                }
            }
        }
        return comments;
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