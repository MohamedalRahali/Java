package Services;

import models.Categorie;
import models.Produit;
import util.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProduitService implements IService<Produit> {

    private final Connection conn;

    public ProduitService() {
        this.conn = MyConnection.getInstance().getCnx();
    }

    @Override
    public void add(Produit produit) {
        String SQL = "INSERT INTO produit (titre, description, artisteId, prix, statut, dateDeCreation, categorie_id, image) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(SQL)) {
            pstmt.setString(1, produit.getTitre());
            pstmt.setString(2, produit.getDescription());
            pstmt.setInt(3, produit.getArtisteId());
            pstmt.setFloat(4, produit.getPrix());
            pstmt.setString(5, produit.getStatut());
            pstmt.setDate(6, new java.sql.Date(produit.getDateDeCreation().getTime()));
            pstmt.setInt(7, produit.getCategorie().getId());
            pstmt.setString(8, produit.getImage());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void update(Produit produit) {
        String SQL = "UPDATE produit SET titre = ?, description = ?, artisteId = ?, prix = ?, statut = ?, dateDeCreation = ?, categorie_id = ?, image = ? WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(SQL)) {
            pstmt.setString(1, produit.getTitre());
            pstmt.setString(2, produit.getDescription());
            pstmt.setInt(3, produit.getArtisteId());
            pstmt.setFloat(4, produit.getPrix());
            pstmt.setString(5, produit.getStatut());
            pstmt.setDate(6, new java.sql.Date(produit.getDateDeCreation().getTime()));
            pstmt.setInt(7, produit.getCategorie().getId());
            pstmt.setString(8, produit.getImage());
            pstmt.setInt(9, produit.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void delete(Produit produit) {
        String SQL = "DELETE FROM produit WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(SQL)) {
            pstmt.setInt(1, produit.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public List<Produit> getAll() {
        List<Produit> produits = new ArrayList<>();
        String SQL = "SELECT p.*, c.libelle, c.description FROM produit p JOIN categorie c ON p.categorie_id = c.id";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(SQL)) {
            while (rs.next()) {
                Categorie categorie = new Categorie(rs.getString("libelle"), rs.getString("description"));
                categorie.setId(rs.getInt("categorie_id"));

                Produit produit = new Produit(
                        rs.getString("titre"),
                        rs.getString("description"),
                        rs.getInt("artisteId"),
                        rs.getFloat("prix"),
                        rs.getString("statut"),
                        rs.getDate("dateDeCreation"),
                        categorie,
                        rs.getString("image")
                );
                produit.setId(rs.getInt("id"));
                produits.add(produit);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return produits;
    }
}
