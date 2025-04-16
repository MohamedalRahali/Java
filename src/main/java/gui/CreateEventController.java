package gui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.Date;
import java.time.LocalDate;

public class CreateEventController {

    @FXML private TextField titleField, lieuxField, dureeField, nbPlacesField;
    @FXML private DatePicker datePicker;
    @FXML private TextArea descriptionArea;
    @FXML private ChoiceBox<String> statusChoiceBox;
    @FXML private Button retourButton;

    @FXML
    public void initialize() {
        statusChoiceBox.getItems().addAll("Pending", "Confirmed", "Cancelled");
        statusChoiceBox.setValue("Pending");
    }

    @FXML
    private void createEvent() {
        String title = titleField.getText();
        String lieu = lieuxField.getText();
        String description = descriptionArea.getText();
        String duree = dureeField.getText();
        String nbPlaces = nbPlacesField.getText();
        LocalDate date = datePicker.getValue();
        String status = statusChoiceBox.getValue();

        // Juste un exemple de validation simple
        if (title.isEmpty() || lieu.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Champs manquants", "Veuillez remplir tous les champs.");
            return;
        }

        // Simule l'enregistrement
        System.out.println("Événement créé : " + title);
        showAlert(Alert.AlertType.INFORMATION, "Succès", "Événement créé !");
    }

    @FXML
    private void handleRetour() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("EventView.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) retourButton.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger la page précédente.");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
