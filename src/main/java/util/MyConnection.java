package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyConnection {
    final String URL = "jdbc:mysql://localhost:3306/art1?zeroDateTimeBehavior=CONVERT_TO_NULL&serverTimezone=UTC";
    final String USER = "root";
    final String PASS = "";

    private Connection cnx;
    private static MyConnection instance;

    private MyConnection() {
        try {
            // The driver is loaded automatically with MySQL 5.0+ JDBC driver
            cnx = DriverManager.getConnection(URL, USER, PASS);
            System.out.println("Connexion établie avec succès !");
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
                cnx = DriverManager.getConnection(URL, USER, PASS);
            }
        } catch (SQLException e) {
            System.err.println("Erreur de vérification de la connexion: " + e.getMessage());
        }
        return cnx;
    }

    // Optional: You can add a method to close the connection if needed
    public void closeConnection() {
        try {
            if (cnx != null && !cnx.isClosed()) {
                cnx.close();
                System.out.println("Connexion fermée avec succès !");
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la fermeture de la connexion: " + e.getMessage());
        }
    }
}

