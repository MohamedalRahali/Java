package utils;

import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.Parameter;
import com.restfb.Version;
import com.restfb.types.FacebookType;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class FacebookService {
    // Remplacez ces valeurs par vos identifiants Facebook Developer
    private static final String APP_ID = "709707414831957";
    private static final String APP_SECRET = "eef80244cc46c2693a69cd3c57b3cf26";
    private static final String REDIRECT_URI = "http://localhost:8080/facebook/callback";
    private static final Version FACEBOOK_VERSION = Version.LATEST;
    
    private static FacebookClient facebookClient;
    private static Stage authStage;

    public static void initialize() {
        if (facebookClient == null) {
            facebookClient = new DefaultFacebookClient(FACEBOOK_VERSION);
        }
    }

    public static void shareBlog(String title, String description, String type) {
        try {
            // Demander confirmation avant de procéder
            Optional<ButtonType> result = showConfirmation(
                "Partage sur Facebook",
                "Voulez-vous partager ce blog sur Facebook ?"
            );

            if (result.isPresent() && result.get() == ButtonType.OK) {
                // Construire l'URL d'authentification
                String authUrl = String.format(
                    "https://www.facebook.com/v18.0/dialog/oauth?" +
                    "client_id=%s" +
                    "&redirect_uri=%s" +
                    "&scope=pages_manage_posts,publish_to_groups",
                    APP_ID,
                    URLEncoder.encode(REDIRECT_URI, StandardCharsets.UTF_8.toString())
                );

                // Créer une nouvelle fenêtre pour l'authentification
                Platform.runLater(() -> {
                    authStage = new Stage();
                    WebView webView = new WebView();
                    
                    // Écouter les changements d'URL
                    webView.getEngine().locationProperty().addListener((observable, oldValue, newValue) -> {
                        if (newValue != null && newValue.startsWith(REDIRECT_URI)) {
                            try {
                                // Extraire le code d'autorisation
                                String code = extractCode(newValue);
                                if (code != null) {
                                    // Fermer la fenêtre d'authentification
                                    authStage.close();
                                    
                                    // Publier le contenu
                                    publishContent(code, title, description, type);
                                }
                            } catch (Exception e) {
                                showError("Erreur", "Erreur lors de l'authentification: " + e.getMessage());
                                authStage.close();
                            }
                        }
                    });

                    webView.getEngine().load(authUrl);
                    authStage.setScene(new javafx.scene.Scene(webView, 800, 600));
                    authStage.setTitle("Connexion Facebook");
                    authStage.show();
                });
            }
        } catch (Exception e) {
            showError("Erreur", "Impossible d'initialiser le partage: " + e.getMessage());
        }
    }

    private static void publishContent(String code, String title, String description, String type) {
        try {
            // Obtenir le token d'accès
            FacebookClient.AccessToken accessToken = facebookClient.obtainUserAccessToken(
                APP_ID,
                APP_SECRET,
                REDIRECT_URI,
                code
            );

            // Créer un client authentifié
            FacebookClient authenticatedFacebookClient = new DefaultFacebookClient(
                accessToken.getAccessToken(),
                APP_SECRET,
                FACEBOOK_VERSION
            );

            // Préparer le message
            String message = String.format("%s\n\n%s\n\nType: %s", 
                title, 
                description,
                type != null ? type : "Non spécifié"
            );

            // Publier sur Facebook
            authenticatedFacebookClient.publish("me/feed", 
                FacebookType.class,
                Parameter.with("message", message)
            );

            showSuccess("Succès", "Le blog a été partagé sur Facebook avec succès!");
        } catch (Exception e) {
            showError("Erreur de publication", "Impossible de publier le contenu: " + e.getMessage());
        }
    }

    private static String extractCode(String url) {
        try {
            String code = url.split("code=")[1];
            if (code.contains("&")) {
                code = code.split("&")[0];
            }
            return code;
        } catch (Exception e) {
            return null;
        }
    }

    private static Optional<ButtonType> showConfirmation(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setContentText(content);
        return alert.showAndWait();
    }

    private static void showError(String title, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }

    private static void showSuccess(String title, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }
} 