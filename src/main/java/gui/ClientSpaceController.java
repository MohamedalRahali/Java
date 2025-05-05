package gui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javafx.event.ActionEvent;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.geometry.Insets;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

import models.TypeReclamation;
import models.Reclamation;
import Services.TypeReclamationService;
import Services.ReclamationService;

public class ClientSpaceController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientSpaceController.class);
    private final TypeReclamationService typeService = new TypeReclamationService();
    private final ReclamationService reclamationService = new ReclamationService();

    @FXML
    private TextField titleField;
    @FXML
    private TextArea descriptionField;
    @FXML
    private ComboBox<String> typeComboBox;
    @FXML
    private VBox reclamationsContainer;

    @FXML
    public void initialize() {
        try {
            // Charger les types de réclamation
            List<TypeReclamation> types = typeService.getAll();
            ObservableList<String> typeNames = FXCollections.observableArrayList(
                types.stream()
                    .map(TypeReclamation::getName)
                    .collect(Collectors.toList())
            );
            typeComboBox.setItems(typeNames);

            // Charger les réclamations existantes
            loadExistingReclamations();
        } catch (RuntimeException e) {
            LOGGER.error("Erreur lors de l'initialisation", e);
            showError("Erreur", "Impossible de charger les données");
        }
    }

    private void loadExistingReclamations() {
        try {
            List<Reclamation> reclamations = reclamationService.getAll();
            reclamationsContainer.getChildren().clear();

            for (Reclamation reclamation : reclamations) {
                VBox reclamationBox = new VBox(10);
                reclamationBox.setStyle("-fx-background-color: #fff3e0; -fx-padding: 15px; -fx-background-radius: 8px;");
                reclamationBox.setPadding(new Insets(10));

                Text titleText = new Text("Titre: " + reclamation.getTitle());
                titleText.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

                Text descriptionText = new Text("Description: " + reclamation.getDescription());
                descriptionText.setStyle("-fx-font-size: 12px;");

                Text typeText = new Text("Type: " + reclamation.getTypeReclamation().getName());
                typeText.setStyle("-fx-font-size: 12px;");

                reclamationBox.getChildren().addAll(titleText, descriptionText, typeText);
                reclamationsContainer.getChildren().add(reclamationBox);
            }
        } catch (RuntimeException e) {
            LOGGER.error("Erreur lors du chargement des réclamations", e);
            showError("Erreur", "Impossible de charger les réclamations existantes");
        }
    }

    @FXML
    private void handleSubmitReclamation() {
        String title = titleField.getText();
        String description = descriptionField.getText();
        String typeName = typeComboBox.getValue();

        if (title == null || title.trim().isEmpty()) {
            showError("Erreur", "Le titre est obligatoire");
            return;
        }

        if (description == null || description.trim().isEmpty()) {
            showError("Erreur", "La description est obligatoire");
            return;
        }

        if (typeName == null) {
            showError("Erreur", "Veuillez sélectionner un type de réclamation");
            return;
        }

        try {
            // Créer un objet TypeReclamation
            TypeReclamation type = new TypeReclamation();
            type.setName(typeName);
            
            // Créer un objet Reclamation
            Reclamation reclamation = new Reclamation();
            reclamation.setTitle(title);
            reclamation.setDescription(description);
            reclamation.setTypeReclamation(type);

            // Ajouter la réclamation
            reclamationService.ajouter(reclamation);
            showSuccess("Succès", "Réclamation ajoutée avec succès");
            
            // Réinitialiser le formulaire
            titleField.clear();
            descriptionField.clear();
            typeComboBox.setValue(null);

            // Recharger les réclamations
            loadExistingReclamations();
        } catch (RuntimeException e) {
            LOGGER.error("Erreur lors de l'ajout de la réclamation", e);
            showError("Erreur", "Impossible d'ajouter la réclamation");
        }
    }

    @FXML
    private void handleBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Home.fxml"));
            Parent root = loader.load();
            Stage currentStage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            currentStage.setScene(new Scene(root));
            currentStage.show();
        } catch (IOException e) {
            LOGGER.error("Erreur lors du retour à la page d'accueil", e);
            showError("Erreur", "Impossible de retourner à la page d'accueil");
        }
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
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
} 