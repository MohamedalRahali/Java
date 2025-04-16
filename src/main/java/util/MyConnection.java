package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyConnection {
    // Ajout du paramètre zeroDateTimeBehavior dans l'URL
    final String URL = "jdbc:mysql://localhost:3306/art";
    final String USER = "root";
    final String PASS = "";

    private Connection cnx;
    private static MyConnection instance;

    // Privatisation du constructeur
    private MyConnection() {
        try {
            // Chargement du driver MySQL
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Établissement de la connexion
            cnx = DriverManager.getConnection(URL, USER, PASS);
            System.out.println("Connexion établie avec succès !");

        } catch (ClassNotFoundException e) {
            System.err.println("Erreur de chargement du driver JDBC: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("Erreur de connexion à la base de données: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static synchronized MyConnection getInstance() {
        if (instance == null) {
            instance = new MyConnection();
        }
        return instance;
    }

    public Connection getCnx() {
        try {
            if (cnx == null || cnx.isClosed()) {
                // Reconnection si la connexion est fermée
                cnx = DriverManager.getConnection(URL, USER, PASS);
            }
        } catch (SQLException e) {
            System.err.println("Erreur de vérification de la connexion: " + e.getMessage());
        }
        return cnx;
    }

    public Connection getConnection() {
        return null;
    }
}