package org.example;

import models.Blog;
import Services.BlogService;
import Services.BlogTypeService;
import util.MyConnection;

import java.sql.Connection;
import java.sql.Date;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static BlogService blogService = new BlogService();
    private static BlogTypeService blogTypeService = new BlogTypeService();
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        Connection connection = MyConnection.getInstance().getCnx();
        showMainMenu();
    }

    public static void showMainMenu() {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("\n===== MENU PRINCIPALE =====");
            System.out.println("1. Gérer les blogs");
            System.out.println("2. Gérer les types de blogs");
            System.out.println("0. Quitter");
            System.out.print("Choisissez une option : ");

            int choice = scanner.nextInt();
            scanner.nextLine(); // consume newline

            switch (choice) {
                case 1:
                    manageBlogs(scanner);
                    break;
                case 2:
                    manageBlogTypes(scanner);
                    break;
                case 0:
                    System.out.println("Au revoir !");
                    System.exit(0);
                    break;
                default:
                    System.out.println("Option invalide. Veuillez réessayer.");
                    break;
            }
        }
    }

    public static void manageBlogs(Scanner scanner) {
        while (true) {
            System.out.println("\n===== GESTION DES BLOGS =====");
            System.out.println("1. Ajouter un blog");
            System.out.println("2. Lister les blogs");
            System.out.println("3. Modifier un blog");
            System.out.println("4. Supprimer un blog");
            System.out.println("0. Retour au menu principal");
            System.out.print("Choisissez une option : ");

            int choice = scanner.nextInt();
            scanner.nextLine(); // consume newline

            switch (choice) {
                case 1:
                    addBlog();
                    break;
                case 2:
                    displayAllBlogs();
                    break;
                case 3:
                    updateBlog();
                    break;
                case 4:
                    deleteBlog();
                    break;
                case 0:
                    return; // Go back to the main menu
                default:
                    System.out.println("Option invalide. Veuillez réessayer.");
                    break;
            }
        }
    }

    public static void manageBlogTypes(Scanner scanner) {
        while (true) {
            System.out.println("\n===== GESTION DES TYPES DE BLOG =====");
            System.out.println("1. Ajouter un type de blog");
            System.out.println("2. Lister les types de blogs");
            System.out.println("3. Modifier un type de blog");
            System.out.println("4. Supprimer un type de blog");
            System.out.println("0. Retour au menu principal");
            System.out.print("Choisissez une option : ");

            int choice = scanner.nextInt();
            scanner.nextLine(); // consume newline

            switch (choice) {
                case 1:
                    // ajouterTypeBlog();
                    System.out.println("Fonction 'Ajouter un type de blog' appelée.");
                    break;
                case 2:
                    // listerTypesBlogs();
                    System.out.println("Fonction 'Lister les types de blogs' appelée.");
                    break;
                case 3:
                    // modifierTypeBlog();
                    System.out.println("Fonction 'Modifier un type de blog' appelée.");
                    break;
                case 4:
                    // supprimerTypeBlog();
                    System.out.println("Fonction 'Supprimer un type de blog' appelée.");
                    break;
                case 0:
                    return; // Go back to the main menu
                default:
                    System.out.println("Option invalide. Veuillez réessayer.");
                    break;
            }
        }
    }

    // Other methods for handling blogs (add, display, update, delete)
    private static void addBlog() {
        System.out.println("\n--- Ajout d'un nouveau blog ---");

        try {
            System.out.print("Titre (min 3 caractères) : ");
            String titre = scanner.nextLine().trim();
            while (titre.length() < 3) {
                System.out.print("Titre invalide. Réessayez : ");
                titre = scanner.nextLine().trim();
            }

            System.out.print("Description (min 10 caractères) : ");
            String descr = scanner.nextLine().trim();
            while (descr.length() < 10) {
                System.out.print("Description invalide. Réessayez : ");
                descr = scanner.nextLine().trim();
            }

            Date date_crea = new Date(System.currentTimeMillis());

            System.out.print("Date à publier (AAAA-MM-JJ) [laissez vide si pas de publication] : ");
            String dateStr = scanner.nextLine().trim();
            Date date_pub = dateStr.isEmpty() ? null : Date.valueOf(dateStr);

            System.out.print("Type : ");
            String type = scanner.nextLine().trim();
            while (type.isEmpty()) {
                System.out.print("Type invalide. Réessayez : ");
                type = scanner.nextLine().trim();
            }

            Blog blog = new Blog(titre, descr, date_crea, date_pub, type);
            blogService.add(blog);
            System.out.println("Blog ajouté avec succès !");
        } catch (Exception e) {
            System.out.println("Erreur : " + e.getMessage());
        }
    }

    private static void displayAllBlogs() {
        System.out.println("\n--- Liste des blogs ---");
        List<Blog> blogs = blogService.getAll();
        if (blogs.isEmpty()) {
            System.out.println("Aucun blog trouvé.");
        } else {
            blogs.forEach(System.out::println);
        }
    }

    private static void updateBlog() {
        displayAllBlogs();
        System.out.print("\nID du blog à modifier : ");
        int id = getValidIntInput(1, Integer.MAX_VALUE);
        scanner.nextLine();

        Blog existingBlog = blogService.getById(id);
        if (existingBlog == null) {
            System.out.println("Blog introuvable !");
            return;
        }

        System.out.print("Nouveau titre [" + existingBlog.getTitre() + "] : ");
        String titre = scanner.nextLine().trim();
        if (titre.isEmpty()) titre = existingBlog.getTitre();

        System.out.print("Nouvelle description [" + existingBlog.getDescription() + "] : ");
        String description = scanner.nextLine().trim();
        if (description.isEmpty()) description = existingBlog.getDescription();

        System.out.print("Nouvelle date de publication (AAAA-MM-JJ) [" + existingBlog.getDate_pub() + "] : ");
        String dateStr = scanner.nextLine().trim();
        Date date_pub = dateStr.isEmpty() ? existingBlog.getDate_pub() : Date.valueOf(dateStr);

        System.out.print("Nouveau type [" + existingBlog.getType() + "] : ");
        String type = scanner.nextLine().trim();
        if (type.isEmpty()) type = existingBlog.getType();

        Blog updatedBlog = new Blog(titre, description, existingBlog.getDate_crea(), date_pub, type);
        updatedBlog.setId(id);
        blogService.update(updatedBlog);
        System.out.println("Blog modifié avec succès !");
    }

    private static void deleteBlog() {
        displayAllBlogs();
        System.out.print("\nID du blog à supprimer : ");
        int id = getValidIntInput(1, Integer.MAX_VALUE);
        scanner.nextLine();

        Blog blog = new Blog();
        blog.setId(id);
        blogService.delete(blog);
        System.out.println("Blog supprimé avec succès !");
    }

    private static int getValidIntInput(int min, int max) {
        while (true) {
            try {
                int input = scanner.nextInt();
                if (input >= min && input <= max) return input;
                System.out.printf("Veuillez entrer un nombre entre %d et %d : ", min, max);
            } catch (java.util.InputMismatchException e) {
                System.out.print("Entrée invalide. Veuillez entrer un nombre : ");
                scanner.nextLine(); // consume invalid input
            }
        }
    }

}

