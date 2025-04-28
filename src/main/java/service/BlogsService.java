package service;

import model.Blogs;
import model.Type_b;
import utils.MyDatabse;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BlogsService implements IService<Blogs> {

    private Connection cnx;

    public BlogsService() {
        cnx = MyDatabse.getInstance().getCnx();
    }

    @Override
    public void add(Blogs blog) throws SQLException {
        String sql = "INSERT INTO blogs (titre, descr, date_crea, date_pub) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, blog.getTitre());
            ps.setString(2, blog.getDescr());
            ps.setDate(3, new java.sql.Date(blog.getDate_crea().getTime()));
            ps.setDate(4, blog.getDate_pub() != null ? new java.sql.Date(blog.getDate_pub().getTime()) : null);
            ps.executeUpdate();
            System.out.println("Blog ajouté !");
        } catch (SQLException e) {
            System.err.println("Erreur ajout : " + e.getMessage());
            throw e;
        }
    }

    @Override
    public void update(Blogs blog) throws SQLException {
        String query = "UPDATE blogs SET titre = ?, descr = ?, date_pub = ?, type_id = ? WHERE id = ?";
        
        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setString(1, blog.getTitre());
            ps.setString(2, blog.getDescr());
            ps.setDate(3, blog.getDate_pub());
            
            if (blog.getType() != null) {
                ps.setInt(4, blog.getType().getId());
            } else {
                ps.setNull(4, java.sql.Types.INTEGER);
            }
            
            ps.setInt(5, blog.getId());
            
            ps.executeUpdate();
            System.out.println("Blog mis à jour !");
        } catch (SQLException e) {
            System.err.println("Erreur mise à jour : " + e.getMessage());
            throw e;
        }
    }

    @Override
    public void delete(int id) throws SQLException {
        // First delete all associated reactions
        String deleteReactionsSql = "DELETE FROM reaction WHERE blog_id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(deleteReactionsSql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }

        // Then delete all associated comments
        String deleteCommentsSql = "DELETE FROM comment WHERE relat_id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(deleteCommentsSql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }

        // Finally delete the blog
        String deleteBlogSql = "DELETE FROM blogs WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(deleteBlogSql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
            System.out.println("Blog supprimé !");
        }
    }

    @Override
    public List<Blogs> getAll() throws SQLException {
        return null;
    }

    @Override
    public Blogs getById(int id) throws SQLException {
        return null;
    }

    @Override
    public boolean validate(Blogs blogs) throws IllegalArgumentException {
        return false;
    }

    @Override
    public List<Blogs> display() throws SQLException {
        List<Blogs> blogsList = new ArrayList<>();
        String query = "SELECT b.*, t.id as type_id, t.libelle as type_libelle " +
                      "FROM blogs b " +
                      "LEFT JOIN type_b t ON b.type_id = t.id";

        try (Statement statement = cnx.createStatement();
             ResultSet rs = statement.executeQuery(query)) {

            while (rs.next()) {
                Type_b type = null;
                if (rs.getObject("type_id") != null) {
                    type = new Type_b(
                        rs.getInt("type_id"),
                        rs.getString("type_libelle")
                    );
                }

                Blogs blog = Blogs.fromDatabase(
                    rs.getInt("id"),
                    rs.getString("titre"),
                    rs.getString("descr"),
                    rs.getDate("date_pub"),
                    rs.getDate("date_crea")
                );

                if (type != null) {
                    blog.setType(type);
                }

                blogsList.add(blog);
            }

        } catch (SQLException e) {
            System.err.println("Erreur affichage : " + e.getMessage());
            throw e;
        }

        return blogsList;
    }

    public List<Blogs> searchByTitle(String keyword) throws SQLException {
        List<Blogs> results = new ArrayList<>();
        String sql = "SELECT b.*, t.id as type_id, t.libelle as type_libelle " +
                    "FROM blogs b " +
                    "LEFT JOIN type_b t ON b.type_id = t.id " +
                    "WHERE b.titre LIKE ?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, "%" + keyword + "%");
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Blogs blog = new Blogs(
                    rs.getString("titre"),
                    rs.getString("descr"),
                    rs.getDate("date_pub")
                );
                blog.setId(rs.getInt("id"));
                blog.setDate_crea(rs.getDate("date_crea"));

                if (rs.getObject("type_id") != null) {
                    Type_b type = new Type_b(
                        rs.getInt("type_id"),
                        rs.getString("type_libelle")
                    );
                    blog.setType(type);
                }
                
                results.add(blog);
            }
        }

        return results;
    }
}