package gui;

import Services.ReclamationService;
import Services.TypeReclamationService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import models.CurrentUser;
import models.Reclamation;
import models.TypeReclamation;

import java.io.IOException;
import java.util.List;

public class CreateReclamationController {

    @FXML private Button micButton; // bouton micro

    @FXML private TextField titleField;
    @FXML private TextArea descriptionArea;
    @FXML private ComboBox<TypeReclamation> typeComboBox;
    @FXML private Button logoutButton;

    private final ReclamationService reclamationService = new ReclamationService();
    private final TypeReclamationService typeService = new TypeReclamationService();

    @FXML
    public void initialize() {
        // Charger les types de réclamation dans le ComboBox
        List<TypeReclamation> types = typeService.getAll();
        typeComboBox.setItems(FXCollections.observableArrayList(types));
    }

    @FXML
    private void createReclamation() {
        // Validation des champs
        if (titleField.getText().isEmpty() || descriptionArea.getText().isEmpty() || typeComboBox.getValue() == null) {
            showAlert("Veuillez remplir tous les champs et sélectionner un type de réclamation.");
            return;
        }

        // Récupérer l'utilisateur courant
        int userId = CurrentUser.getCurrentUserId();
        if (userId == -1) {
            showAlert("Erreur: utilisateur non connecté.");
            return;
        }

        // Création de la réclamation avec userId
        Reclamation reclamation = new Reclamation(
                titleField.getText(),
                descriptionArea.getText(),
                typeComboBox.getValue(),
                userId
        );

        try {
            reclamationService.ajouter(reclamation);
            showSuccess("Succès", "Réclamation créée avec succès!");
            // Aller à Mes Réclamations après création
            goToMesReclamations(null);
        } catch (Exception e) {
            showAlert("Erreur lors de la création de la réclamation: " + e.getMessage());
        }
    }

    @FXML
    private void goBackToHome(javafx.event.ActionEvent event) throws IOException {
        // Aller à Mes Réclamations au lieu de l'accueil
        goToMesReclamations(null);
    }

    private void goToReclamationView() throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/reclamation_view.fxml"));
        Stage stage = (Stage) titleField.getScene().getWindow();
        stage.setTitle("Liste des Réclamations");
        stage.setScene(new Scene(root));
    }

    private void goToMesReclamations(javafx.event.ActionEvent event) throws java.io.IOException {
        javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/mes_reclamations.fxml"));
        javafx.scene.Parent root = loader.load();
        javafx.stage.Stage stage = (javafx.stage.Stage) titleField.getScene().getWindow();
        stage.setTitle("Mes Réclamations");
        stage.setScene(new javafx.scene.Scene(root));
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Attention");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccess(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Utilise Vosk pour faire de la reconnaissance vocale et remplir la description.
     */
    @FXML
    private void handleVoiceToText() {
        new Thread(() -> {
            try {
                // Charger le modèle de reconnaissance vocale
                org.vosk.Model model = new org.vosk.Model("models/fr"); // chemin vers le modèle français

                javax.sound.sampled.AudioFormat format = new javax.sound.sampled.AudioFormat(16000, 16, 1, true, false);
                javax.sound.sampled.DataLine.Info info = new javax.sound.sampled.DataLine.Info(javax.sound.sampled.TargetDataLine.class, format);
                javax.sound.sampled.TargetDataLine microphone = (javax.sound.sampled.TargetDataLine) javax.sound.sampled.AudioSystem.getLine(info);
                microphone.open(format);
                microphone.start();

                org.vosk.Recognizer recognizer = new org.vosk.Recognizer(model, 16000.0f);

                byte[] buffer = new byte[4096];
                javafx.application.Platform.runLater(() -> descriptionArea.setPromptText("Parlez maintenant..."));
                StringBuilder resultText = new StringBuilder();

                long start = System.currentTimeMillis();
                while (System.currentTimeMillis() - start < 6000) { // 6 secondes max
                    int nbytes = microphone.read(buffer, 0, buffer.length);
                    if (recognizer.acceptWaveForm(buffer, nbytes)) {
                        // Optionnel : afficher partiellement
                    }
                }
                resultText.append(recognizer.getFinalResult());
                microphone.stop();
                microphone.close();

                // Extraire le texte JSON
                String json = resultText.toString();
                String recognized = json.replaceAll(".*\\\"text\\\"\\s*:\\s*\\\"([^\\\"]*)\\\".*", "$1");

                javafx.application.Platform.runLater(() -> {
                    if (recognized != null && !recognized.trim().isEmpty()) {
                        String old = descriptionArea.getText();
                        if (old == null || old.isEmpty()) {
                            descriptionArea.setText(recognized.trim());
                        } else {
                            descriptionArea.setText(old + " " + recognized.trim());
                        }
                    } else {
                        descriptionArea.setPromptText("Aucune voix reconnue.");
                    }
                });

            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> descriptionArea.setPromptText("Erreur reconnaissance vocale : " + e.getMessage()));
            }
        }).start();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
