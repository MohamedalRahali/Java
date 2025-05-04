package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import io.github.cdimascio.dotenv.Dotenv;

public class MyConnection {
    // Ajout du paramètre zeroDateTimeBehavior dans l'URL
    private static final Dotenv dotenv = Dotenv.load();
    final String URL = dotenv.get("DB_URL");
    final String USER = dotenv.get("DB_USER");
    final String PASS = dotenv.get("DB_PWD");

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

    public Connection getConnection() {
        try {
            if (cnx == null || cnx.isClosed()) {
                // Reconnexion si la connexion est fermée
                cnx = DriverManager.getConnection(URL, USER, PASS);
            }
        } catch (SQLException e) {
            System.err.println("Erreur de vérification de la connexion: " + e.getMessage());
        }
        return cnx;
    }
}