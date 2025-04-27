package test;

import model.Type_b;
import service.Type_bCRUD;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {
        Type_b type = new Type_b("Nouveau Type");

        Type_bCRUD typeService = new Type_bCRUD();

        try {
            typeService.add(type);
            System.out.println("Type ajouté avec succès !");

            System.out.println("\nListe des types :");
            typeService.display().forEach(t -> {
                System.out.println("ID: " + t.getId() + " - Libellé: " + t.getLibelle());
            });

        } catch (SQLException e) {
            System.err.println("Erreur: " + e.getMessage());
        }
    }
}