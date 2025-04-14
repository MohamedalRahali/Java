package pro.example;

import models.Produit;
import models.Categorie;
import Services.ProduitService;
import Services.CategorieService;
import util.MyConnection;

import java.sql.Connection;
import java.sql.Date;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static ProduitService produitService = new ProduitService();
    private static CategorieService categorieService = new CategorieService();
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        Connection connection = MyConnection.getInstance().getCnx();
        showMainMenu();
    }

    public static void showMainMenu() {
        while (true) {
            System.out.println("\n===== GESTION DES PRODUITS =====");
            System.out.println("1. Gérer les Produits");
            System.out.println("2. Gérer les Catégories");
            System.out.println("0. Quitter");
            System.out.print("Votre choix : ");

            int choice = getValidIntInput(0, 2);
            scanner.nextLine(); // consommer la nouvelle ligne

            switch (choice) {
                case 1:
                    manageProduits();
                    break;
                case 2:
                    manageCategories();
                    break;
                case 0:
                    System.out.println("Au revoir !");
                    System.exit(0);
            }
        }
    }

    public static void manageProduits() {
        while (true) {
            System.out.println("\n===== GESTION DES PRODUITS =====");
            System.out.println("1. Ajouter un produit");
            System.out.println("2. Afficher tous les produits");
            System.out.println("3. Modifier un produit");
            System.out.println("4. Supprimer un produit");
            System.out.println("0. Retour au menu principal");
            System.out.print("Votre choix : ");

            int choice = getValidIntInput(0, 4);
            scanner.nextLine(); // consommer la nouvelle ligne

            switch (choice) {
                case 1:
                    addProduit();
                    break;
                case 2:
                    displayAllProduits();
                    break;
                case 3:
                    updateProduit();
                    break;
                case 4:
                    deleteProduit();
                    break;
                case 0:
                    return;
            }
        }
    }

    public static void manageCategories() {
        while (true) {
            System.out.println("\n===== GESTION DES CATÉGORIES =====");
            System.out.println("1. Ajouter une catégorie");
            System.out.println("2. Afficher toutes les catégories");
            System.out.println("3. Modifier une catégorie");
            System.out.println("4. Supprimer une catégorie");
            System.out.println("0. Retour au menu principal");
            System.out.print("Votre choix : ");

            int choice = getValidIntInput(0, 4);
            scanner.nextLine(); // consommer la nouvelle ligne

            switch (choice) {
                case 1:
                    addCategorie();
                    break;
                case 2:
                    displayAllCategories();
                    break;
                case 3:
                    updateCategorie();
                    break;
                case 4:
                    deleteCategorie();
                    break;
                case 0:
                    return;
            }
        }
    }

    private static int getValidIntInput(int min, int max) {
        while (true) {
            try {
                int input = scanner.nextInt();
                if (input >= min && input <= max) {
                    return input;
                } else {
                    System.out.printf("Veuillez entrer un nombre entre %d et %d : ", min, max);
                }
            } catch (java.util.InputMismatchException e) {
                System.out.print("Entrée invalide. Veuillez entrer un nombre : ");
                scanner.nextLine(); // vider le buffer
            }
        }
    }

    private static void addProduit() {
        System.out.println("\n--- Ajout d'un nouveau produit ---");

        System.out.print("Titre : ");
        String titre = scanner.nextLine();

        System.out.print("Description : ");
        String description = scanner.nextLine();

        System.out.print("ID de l'artiste : ");
        int artisteId = getValidIntInput(1, Integer.MAX_VALUE);
        scanner.nextLine(); // consommer la nouvelle ligne

        System.out.print("Prix : ");
        float prix = scanner.nextFloat();
        scanner.nextLine(); // consommer la nouvelle ligne

        System.out.print("Statut : ");
        String statut = scanner.nextLine();

        Date dateDeCreation = new Date(System.currentTimeMillis());

        displayAllCategories();
        System.out.print("ID de la catégorie : ");
        int categorieId = getValidIntInput(1, Integer.MAX_VALUE);
        Categorie categorie = new Categorie();
        categorie.setId(categorieId);

        System.out.print("Chemin de l'image : ");
        String image = scanner.nextLine();

        Produit produit = new Produit(titre, description, artisteId, prix, statut, dateDeCreation, categorie, image);
        produitService.add(produit);
        System.out.println("Produit ajouté avec succès !");
    }

    private static void displayAllProduits() {
        System.out.println("\n--- Liste des produits ---");
        List<Produit> produits = produitService.getAll();

        if (produits.isEmpty()) {
            System.out.println("Aucun produit trouvé.");
        } else {
            for (Produit produit : produits) {
                System.out.println(produit);
            }
        }
    }

    private static void updateProduit() {
        displayAllProduits();
        System.out.print("\nID du produit à modifier : ");
        int id = getValidIntInput(1, Integer.MAX_VALUE);
        scanner.nextLine(); // consommer la nouvelle ligne

        System.out.println("\n--- Modification du produit ---");

        System.out.print("Nouveau titre : ");
        String titre = scanner.nextLine();

        System.out.print("Nouvelle description : ");
        String description = scanner.nextLine();

        System.out.print("Nouvel ID de l'artiste : ");
        int artisteId = getValidIntInput(1, Integer.MAX_VALUE);
        scanner.nextLine(); // consommer la nouvelle ligne

        System.out.print("Nouveau prix : ");
        float prix = scanner.nextFloat();
        scanner.nextLine(); // consommer la nouvelle ligne

        System.out.print("Nouveau statut : ");
        String statut = scanner.nextLine();

        Date dateDeCreation = new Date(System.currentTimeMillis());

        displayAllCategories();
        System.out.print("Nouvel ID de la catégorie : ");
        int categorieId = getValidIntInput(1, Integer.MAX_VALUE);
        Categorie categorie = new Categorie();
        categorie.setId(categorieId);

        System.out.print("Nouveau chemin de l'image : ");
        String image = scanner.nextLine();

        Produit produit = new Produit(titre, description, artisteId, prix, statut, dateDeCreation, categorie, image);
        produit.setId(id);
        produitService.update(produit);
    }

    private static void deleteProduit() {
        displayAllProduits();
        System.out.print("\nID du produit à supprimer : ");
        int id = getValidIntInput(1, Integer.MAX_VALUE);
        scanner.nextLine(); // consommer la nouvelle ligne

        Produit produit = new Produit();
        produit.setId(id);
        produitService.delete(produit);
    }

    private static void addCategorie() {
        System.out.println("\n--- Ajout d'une nouvelle catégorie ---");

        System.out.print("Libellé : ");
        String libelle = scanner.nextLine();

        System.out.print("Description : ");
        String description = scanner.nextLine();

        Categorie categorie = new Categorie(libelle, description);
        categorieService.add(categorie);
        System.out.println("Catégorie ajoutée avec succès !");
    }

    private static void displayAllCategories() {
        System.out.println("\n--- Liste des catégories ---");
        List<Categorie> categories = categorieService.getAll();

        if (categories.isEmpty()) {
            System.out.println("Aucune catégorie trouvée.");
        } else {
            for (Categorie categorie : categories) {
                System.out.println(categorie);
            }
        }
    }

    private static void updateCategorie() {
        displayAllCategories();
        System.out.print("\nID de la catégorie à modifier : ");
        int id = getValidIntInput(1, Integer.MAX_VALUE);
        scanner.nextLine(); // consommer la nouvelle ligne

        System.out.println("\n--- Modification de la catégorie ---");

        System.out.print("Nouveau libellé : ");
        String libelle = scanner.nextLine();

        System.out.print("Nouvelle description : ");
        String description = scanner.nextLine();

        Categorie categorie = new Categorie(libelle, description);
        categorie.setId(id);
        categorieService.update(categorie);
    }

    private static void deleteCategorie() {
        displayAllCategories();
        System.out.print("\nID de la catégorie à supprimer : ");
        int id = getValidIntInput(1, Integer.MAX_VALUE);
        scanner.nextLine(); // consommer la nouvelle ligne

        Categorie categorie = new Categorie();
        categorie.setId(id);
        categorieService.delete(categorie);
    }
}
