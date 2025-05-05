package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.InputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyConnection {
    private static MyConnection instance;
    private Connection connection;
    private static final Logger LOGGER = LoggerFactory.getLogger(MyConnection.class);

    private MyConnection() {
        try {
            // Chargement du driver MySQL
            Class.forName("com.mysql.cj.jdbc.Driver");
            LOGGER.info("Driver MySQL chargé avec succès");

            // Établissement de la connexion
            String url = "jdbc:mysql://localhost:3306/art?createDatabaseIfNotExist=true";
            String user = "root";
            String password = ""; // Mettez votre mot de passe ici si vous en avez défini un
            
            LOGGER.info("Tentative de connexion à la base de données...");
            LOGGER.info("URL: {}", url);
            LOGGER.info("Utilisateur: {}", user);
            
            try {
                connection = DriverManager.getConnection(url, user, password);
                LOGGER.info("Connexion établie avec succès à la base de données art!");
                
                // Exécuter le script de mise à jour
                executeUpdateScript();
                
                // Vérifier si les tables existent et sont correctement configurées
                checkTables();
            } catch (SQLException e) {
                LOGGER.error("Erreur de connexion à la base de données: {}", e.getMessage());
                LOGGER.error("Code d'erreur SQL: {}", e.getErrorCode());
                LOGGER.error("État SQL: {}", e.getSQLState());
                
                // Afficher un message d'erreur plus convivial
                LOGGER.error("\n=== ERREUR DE CONNEXION À LA BASE DE DONNÉES ===");
                LOGGER.error("Veuillez vérifier que:");
                LOGGER.error("1. MySQL est installé et en cours d'exécution");
                LOGGER.error("2. Les identifiants de connexion sont corrects");
                LOGGER.error("3. Le port 3306 est disponible");
                LOGGER.error("=============================================\n");
            }
        } catch (ClassNotFoundException e) {
            LOGGER.error("Erreur de chargement du driver JDBC: {}", e.getMessage());
        }
    }

    public static MyConnection getInstance() {
        if (instance == null) {
            instance = new MyConnection();
        }
        return instance;
    }

    public Connection getCnx() {
        try {
            if (connection == null || connection.isClosed()) {
                LOGGER.info("La connexion est nulle ou fermée. Tentative de reconnexion...");
                String url = "jdbc:mysql://localhost:3306/art?createDatabaseIfNotExist=true";
                String user = "root";
                String password = "";
                connection = DriverManager.getConnection(url, user, password);
                LOGGER.info("Reconnexion réussie!");
            }
        } catch (SQLException e) {
            LOGGER.error("Erreur lors de la vérification/établissement de la connexion: {}", e.getMessage());
        }
        return connection;
    }

    private void executeUpdateScript() {
        LOGGER.info("Début de l'exécution du script de mise à jour...");
        try {
            // Vérifier si les tables existent
            Statement checkStmt = connection.createStatement();
            ResultSet rs = checkStmt.executeQuery("SHOW TABLES LIKE 'reclamation'");
            
            if (!rs.next()) {
                LOGGER.info("Les tables n'existent pas, création en cours...");
                createTables();
            } else {
                // Vérifier si la contrainte ON DELETE CASCADE existe
                rs = checkStmt.executeQuery(
                    "SELECT * FROM information_schema.TABLE_CONSTRAINTS " +
                    "WHERE CONSTRAINT_SCHEMA = 'art' " +
                    "AND TABLE_NAME = 'reclamation' " +
                    "AND CONSTRAINT_NAME = 'reclamation_ibfk_1' " +
                    "AND CONSTRAINT_TYPE = 'FOREIGN KEY'"
                );
                
                if (!rs.next()) {
                    LOGGER.info("La contrainte ON DELETE CASCADE n'existe pas, recréation des tables...");
                    // Supprimer les tables existantes
                    String dropReclamationTable = "DROP TABLE IF EXISTS reclamation";
                    String dropTypeReclamationTable = "DROP TABLE IF EXISTS type_reclamation";
                    connection.createStatement().execute(dropReclamationTable);
                    connection.createStatement().execute(dropTypeReclamationTable);
                    
                    // Recréer les tables avec la nouvelle contrainte
                    createTables();
                } else {
                    LOGGER.info("Les tables existent déjà avec la bonne structure");
                }
            }
        } catch (Exception e) {
            LOGGER.error("Erreur lors de l'exécution du script de mise à jour: {}", e.getMessage());
        }
    }

    private void checkTables() {
        try {
            Statement stmt = connection.createStatement();
            
            // Vérifier si la table type_reclamation existe et a des données
            try {
                ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM type_reclamation");
                if (rs.next()) {
                    int count = rs.getInt("count");
                    LOGGER.info("Nombre de types de réclamation: {}", count);
                    if (count == 0) {
                        // Insérer les types par défaut si la table est vide
                        insertDefaultTypes();
                    }
                }
            } catch (SQLException e) {
                LOGGER.error("Erreur lors de la vérification de type_reclamation: {}", e.getMessage());
                createTables();
            }

            // Vérifier si la table reclamation existe
            try {
                ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM reclamation");
                if (rs.next()) {
                    LOGGER.info("Nombre de réclamations: {}", rs.getInt("count"));
                }
            } catch (SQLException e) {
                LOGGER.error("Erreur lors de la vérification de reclamation: {}", e.getMessage());
                createTables();
            }

            // Vérifier si la table reponse existe
            try {
                ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM reponse");
                if (!rs.next()) {
                    LOGGER.info("La table reponse n'existe pas, création en cours...");
                    createReponseTable();
                }
            } catch (SQLException e) {
                LOGGER.error("Erreur lors de la vérification de reponse: {}", e.getMessage());
                createReponseTable();
            }
        } catch (SQLException e) {
            LOGGER.error("Erreur lors de la vérification des tables: {}", e.getMessage());
        }
    }

    private void createTables() {
        try {
            // Suppression des tables existantes pour une réinitialisation propre
            String dropReclamationTable = "DROP TABLE IF EXISTS reclamation";
            String dropTypeReclamationTable = "DROP TABLE IF EXISTS type_reclamation";
            String dropReponseTable = "DROP TABLE IF EXISTS reponse";
            connection.createStatement().execute(dropReclamationTable);
            connection.createStatement().execute(dropTypeReclamationTable);
            connection.createStatement().execute(dropReponseTable);

            // Création de la table type_reclamation
            String createTypeReclamationTable = "CREATE TABLE type_reclamation ("
                    + "id INT PRIMARY KEY AUTO_INCREMENT,"
                    + "name LONGTEXT"
                    + ")";
            connection.createStatement().execute(createTypeReclamationTable);

            // Insertion des types de réclamation par défaut
            String insertDefaultTypes = "INSERT INTO type_reclamation (name) VALUES "
                    + "('Bug'), ('Feature Request'), ('Support'), ('Other')";
            connection.createStatement().execute(insertDefaultTypes);

            // Création de la table reclamation avec ON DELETE CASCADE
            String createReclamationTable = "CREATE TABLE reclamation ("
                    + "id INT PRIMARY KEY AUTO_INCREMENT,"
                    + "type_reclamation_id INT,"
                    + "title VARCHAR(255) NOT NULL,"
                    + "description LONGTEXT NOT NULL,"
                    + "created_at DATETIME NOT NULL,"
                    + "fingerprint_verified BOOLEAN DEFAULT FALSE,"
                    + "FOREIGN KEY (type_reclamation_id) REFERENCES type_reclamation(id) ON DELETE CASCADE"
                    + ")";
            connection.createStatement().execute(createReclamationTable);

            // Création de la table reponse
            String createReponseTable = "CREATE TABLE reponse ("
                    + "id INT PRIMARY KEY AUTO_INCREMENT,"
                    + "reclamation_id INT,"
                    + "contenu LONGTEXT NOT NULL,"
                    + "created_at DATETIME NOT NULL,"
                    + "FOREIGN KEY (reclamation_id) REFERENCES reclamation(id) ON DELETE CASCADE"
                    + ")";
            connection.createStatement().execute(createReponseTable);

            LOGGER.info("Tables créées avec succès");
        } catch (SQLException e) {
            LOGGER.error("Erreur lors de la création des tables : {}", e.getMessage());
            e.printStackTrace();
        }
    }

    private void insertDefaultTypes() {
        try {
            Statement stmt = connection.createStatement();
            stmt.execute("INSERT INTO type_reclamation (name) VALUES " +
                    "('Bug'), ('Feature Request'), ('Support'), ('Other') " +
                    "ON DUPLICATE KEY UPDATE name = VALUES(name)");
            LOGGER.info("Types de réclamation par défaut insérés avec succès");
        } catch (SQLException e) {
            LOGGER.error("Erreur lors de l'insertion des types par défaut: {}", e.getMessage());
        }
    }

    private void createReponseTable() {
        try {
            // Création de la table reponse
            String createReponseTable = "CREATE TABLE reponse ("
                    + "id INT PRIMARY KEY AUTO_INCREMENT,"
                    + "reclamation_id INT,"
                    + "contenu LONGTEXT NOT NULL,"
                    + "created_at DATETIME NOT NULL,"
                    + "FOREIGN KEY (reclamation_id) REFERENCES reclamation(id) ON DELETE CASCADE"
                    + ")";
            connection.createStatement().execute(createReponseTable);
            LOGGER.info("Table reponse créée avec succès");
        } catch (SQLException e) {
            LOGGER.error("Erreur lors de la création de la table reponse : {}", e.getMessage());
            e.printStackTrace();
        }
    }
}