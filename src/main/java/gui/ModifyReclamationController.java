package gui;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import models.Reclamation;
import models.TypeReclamation;
import Services.ReclamationService;
import Services.TypeReclamationService;

import java.io.IOException;
import java.util.List;

public class ModifyReclamationController {

    @FXML private TextField titleField;
    @FXML private TextArea descriptionArea;
    @FXML private ComboBox<TypeReclamation> typeComboBox;

    private Reclamation reclamationToEdit;
    private final ReclamationService service = new ReclamationService();
    private final TypeReclamationService typeService = new TypeReclamationService();

    @FXML
    public void initialize() {
        // Charger les types de réclamation dans le ComboBox
        List<TypeReclamation> types = typeService.getAll();
        typeComboBox.setItems(FXCollections.observableArrayList(types));
    }

    public void setReclamationToEdit(Reclamation reclamation) {
        this.reclamationToEdit = reclamation;

        // Remplir les champs avec les données existantes
        titleField.setText(reclamation.getTitle());
        descriptionArea.setText(reclamation.getDescription());
        typeComboBox.setValue(reclamation.getTypeReclamation());
    }

    @FXML
    private void updateReclamation() {
        try {
            if (!validateFields()) return;

            // Mettre à jour les données de l'objet
            reclamationToEdit.setTitle(titleField.getText().trim());
            reclamationToEdit.setDescription(descriptionArea.getText().trim());
            reclamationToEdit.setTypeReclamation(typeComboBox.getValue());

            // Mise à jour dans la base de données
            service.modifier(reclamationToEdit);

            goBackToReclamationList();

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Une erreur est survenue lors de la modification: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean validateFields() {
        String title = titleField.getText().trim();
        String description = descriptionArea.getText().trim();

        if (title.isEmpty() || description.isEmpty() || typeComboBox.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Tous les champs doivent être remplis");
            return false;
        }

        if (title.length() < 3) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Le titre doit contenir au moins 3 caractères.");
            return false;
        }

        if (description.length() < 10) {
            showAlert(Alert.AlertType.WARNING, "Validation", "La description doit contenir au moins 10 caractères.");
            return false;
        }

        return true;
    }

    @FXML
    private void goBackToHome() throws IOException {
        goBackToReclamationList();
    }

    private void goBackToReclamationList() throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/reclamation_view.fxml"));
        Stage stage = (Stage) titleField.getScene().getWindow();
        stage.setTitle("Liste des Réclamations");
        stage.setScene(new Scene(root));
    }

    @FXML
    private void goToAdminHome(javafx.event.ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin_home.fxml"));
        Parent root = loader.load();
        Stage stage = (Stage) titleField.getScene().getWindow();
        stage.setTitle("User Dashboard");
        stage.setScene(new Scene(root));
    }

    @FXML
    private void goToHomeReclamation(javafx.event.ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/HomeReclamation.fxml"));
        Parent root = loader.load();
        Stage stage = (Stage) titleField.getScene().getWindow();
        stage.setTitle("Home Reclamation");
        stage.setScene(new Scene(root));
    }

    @FXML
    private void logout(javafx.event.ActionEvent event) throws IOException {
        models.CurrentUser.clear();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
        Parent root = loader.load();
        Stage stage = (Stage) titleField.getScene().getWindow();
        stage.setTitle("Login");
        stage.setScene(new Scene(root));
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void goToReclamationView(javafx.event.ActionEvent event) throws java.io.IOException {
        javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/reclamation_view.fxml"));
        javafx.scene.Parent root = loader.load();
        javafx.stage.Stage stage = (javafx.stage.Stage) titleField.getScene().getWindow();
        stage.setTitle("Liste des Réclamations");
        stage.setScene(new javafx.scene.Scene(root));
    }
}