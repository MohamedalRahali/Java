package Services;

import models.Categorie;
import util.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategorieService implements IService<Categorie> {

    private final Connection conn;

    public CategorieService() {
        this.conn = MyConnection.getInstance().getCnx();
    }

    @Override
    public void add(Categorie categorie) {
        String SQL = "INSERT INTO categorie (libelle, description) VALUES (?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(SQL)) {
            pstmt.setString(1, categorie.getLibelle());
            pstmt.setString(2, categorie.getDescription());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void update(Categorie categorie) {
        String SQL = "UPDATE categorie SET libelle = ?, description = ? WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(SQL)) {
            pstmt.setString(1, categorie.getLibelle());
            pstmt.setString(2, categorie.getDescription());
            pstmt.setInt(3, categorie.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void delete(Categorie categorie) {
        String SQL = "DELETE FROM categorie WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(SQL)) {
            pstmt.setInt(1, categorie.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public List<Categorie> getAll() {
        List<Categorie> categories = new ArrayList<>();
        String SQL = "SELECT * FROM categorie";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(SQL)) {
            while (rs.next()) {
                Categorie categorie = new Categorie(
                        rs.getString("libelle"),
                        rs.getString("description")
                );
                categorie.setId(rs.getInt("id"));
                categories.add(categorie);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return categories;
    }
}
