package Services;

import models.BlogType;
import util.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BlogTypeService implements IService<BlogType> {

    private final Connection conn;

    public BlogTypeService() {
        this.conn = MyConnection.getInstance().getCnx();
    }

    @Override
    public void add(BlogType blogType) {
        String SQL = "INSERT INTO blog_type (blog_id, type_id) VALUES (?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(SQL)) {
            pstmt.setInt(1, blogType.getBlog_id());
            pstmt.setInt(2, blogType.getType_id());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de l'ajout de l'association: " + e.getMessage());
        }
    }

    @Override
    public void update(BlogType blogType) {
    }

    public void updateAssociation(BlogType oldBt, BlogType newBt) {
        delete(oldBt);
        add(newBt);
    }

    @Override
    public void delete(BlogType blogType) {
        String SQL = "DELETE FROM blog_type WHERE blog_id=? AND type_id=?";
        try (PreparedStatement pstmt = conn.prepareStatement(SQL)) {
            pstmt.setInt(1, blogType.getBlog_id());
            pstmt.setInt(2, blogType.getType_id());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la suppression de l'association: " + e.getMessage());
        }
    }

    @Override
    public List<BlogType> getAll() {
        List<BlogType> associations = new ArrayList<>();
        String SQL = "SELECT * FROM blog_type";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SQL)) {

            while (rs.next()) {
                BlogType bt = new BlogType(
                        rs.getInt("blog_id"),
                        rs.getInt("type_id")
                );
                associations.add(bt);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération des associations: " + e.getMessage());
        }
        return associations;
    }

    public List<BlogType> getByBlogId(int blogId) {
        List<BlogType> results = new ArrayList<>();
        String SQL = "SELECT * FROM blog_type WHERE blog_id=?";

        try (PreparedStatement pstmt = conn.prepareStatement(SQL)) {
            pstmt.setInt(1, blogId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                results.add(new BlogType(rs.getInt("blog_id"), rs.getInt("type_id")));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche: " + e.getMessage());
        }
        return results;
    }

    public void update(BlogType oldBlogType, BlogType newBlogType) {
    }
}