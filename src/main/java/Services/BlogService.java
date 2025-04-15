package Services;

import models.Blog;
import util.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BlogService implements IService<Blog> {

    private final Connection conn;

    public BlogService() {
        this.conn = MyConnection.getInstance().getCnx();
    }

    @Override
    public void add(Blog blog) {
        String SQL = "INSERT INTO blog (titre, descr, date_crea, date_pub, type) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(SQL, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, blog.getTitre());
            pstmt.setString(2, blog.getDescr());
            pstmt.setDate(3, new java.sql.Date(blog.getDate_crea().getTime()));
            pstmt.setDate(4, new java.sql.Date(blog.getDate_pub().getTime()));
            pstmt.setString(5, blog.getType());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        blog.setId(rs.getInt(1));
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de l'ajout du blog: " + e.getMessage());
        }
    }

    @Override
    public void update(Blog blog) {
        String SQL = "UPDATE blog SET titre=?, descr=?, date_crea=?, date_pub=?, type=? WHERE id=?";
        try (PreparedStatement pstmt = conn.prepareStatement(SQL)) {
            pstmt.setString(1, blog.getTitre());
            pstmt.setString(2, blog.getDescr());
            pstmt.setDate(3, new java.sql.Date(blog.getDate_crea().getTime()));
            pstmt.setDate(4, new java.sql.Date(blog.getDate_pub().getTime()));
            pstmt.setString(5, blog.getType());
            pstmt.setInt(6, blog.getId());

            int rowsUpdated = pstmt.executeUpdate();
            if (rowsUpdated == 0) {
                throw new RuntimeException("Aucun blog trouvé avec l'ID: " + blog.getId());
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la mise à jour du blog: " + e.getMessage());
        }
    }

    @Override
    public void delete(Blog blog) {
        String SQL = "DELETE FROM blog WHERE id=?";
        try (PreparedStatement pstmt = conn.prepareStatement(SQL)) {
            pstmt.setInt(1, blog.getId());
            int rowsDeleted = pstmt.executeUpdate();
            if (rowsDeleted == 0) {
                throw new RuntimeException("Aucun blog trouvé avec l'ID: " + blog.getId());
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la suppression du blog: " + e.getMessage());
        }
    }

    @Override
    public List<Blog> getAll() {
        List<Blog> blogs = new ArrayList<>();
        String SQL = "SELECT * FROM blog";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SQL)) {

            while (rs.next()) {
                Blog b = new Blog(
                        rs.getInt("id"),
                        rs.getString("titre"),
                        rs.getString("descr"),
                        rs.getDate("date_crea"),
                        rs.getDate("date_pub"),
                        rs.getString("type")
                );
                blogs.add(b);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération des blogs: " + e.getMessage());
        }
        return blogs;
    }

    public Blog getById(int id) {
        String SQL = "SELECT * FROM blog WHERE id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(SQL)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return new Blog(
                        rs.getInt("id"),
                        rs.getString("titre"),
                        rs.getString("descr"),
                        rs.getDate("date_crea"),
                        rs.getDate("date_pub"),
                        rs.getString("type")
                );
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération du blog: " + e.getMessage());
        }
        return null;
    }

    public List<Blog> searchByTitre(String titre) {
        List<Blog> results = new ArrayList<>();
        String SQL = "SELECT * FROM blog WHERE titre LIKE ?";

        try (PreparedStatement pstmt = conn.prepareStatement(SQL)) {
            pstmt.setString(1, "%" + titre + "%");
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Blog b = new Blog(
                        rs.getInt("id"),
                        rs.getString("titre"),
                        rs.getString("descr"),
                        rs.getDate("date_crea"),
                        rs.getDate("date_pub"),
                        rs.getString("type")
                );
                results.add(b);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche: " + e.getMessage());
        }
        return results;
    }
}