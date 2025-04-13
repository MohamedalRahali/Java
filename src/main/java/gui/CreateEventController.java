package gui;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import models.Event;
import Services.EventService;
import java.sql.Date;
import java.time.LocalDate;
import java.util.regex.Pattern;

public class CreateEventController {

    @FXML private TextField titleField, lieuxField, dureeField, nbPlacesField;
    @FXML private DatePicker datePicker;
    @FXML private TextArea descriptionArea;
    @FXML private ChoiceBox<String> statusChoiceBox;

    private final EventService service = new EventService();

    @FXML
    private void initialize() {
        statusChoiceBox.getItems().addAll("Pending", "Confirmed", "Cancelled");
        statusChoiceBox.setValue("Pending");
    }

    @FXML
    private void createEvent() {
        try {
            if (!validateFields()) return;

            Event e = new Event(
                    titleField.getText().trim(),
                    Date.valueOf(datePicker.getValue()),
                    descriptionArea.getText().trim(),
                    lieuxField.getText().trim(),
                    statusChoiceBox.getValue(),
                    dureeField.getText().trim(),
                    Integer.parseInt(nbPlacesField.getText().trim())
            );

            service.ajouter(e);
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Événement créé avec succès");
            clearFields();

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }
    }

    private boolean validateFields() {
        String title = titleField.getText().trim();
        String lieux = lieuxField.getText().trim();
        String description = descriptionArea.getText().trim();
        String duree = dureeField.getText().trim();
        String nbPlacesStr = nbPlacesField.getText().trim();
        LocalDate date = datePicker.getValue();
        String status = statusChoiceBox.getValue();

        if (title.isEmpty() || lieux.isEmpty() || description.isEmpty() || duree.isEmpty() ||
                nbPlacesStr.isEmpty() || date == null || status == null) {
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

        if (!Pattern.matches("\\d+h(\\d{1,2})?", duree)) {
            showAlert(Alert.AlertType.WARNING, "Validation", "La durée doit être au format correct (ex: 2h ou 1h30).");
            return false;
        }

        try {
            int nbPlaces = Integer.parseInt(nbPlacesStr);
            if (nbPlaces <= 0) {
                showAlert(Alert.AlertType.WARNING, "Validation", "Le nombre de places doit être supérieur à zéro.");
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Le nombre de places doit être un entier valide.");
            return false;
        }

        if (date.isBefore(LocalDate.now())) {
            showAlert(Alert.AlertType.WARNING, "Validation", "La date de l’événement doit être future.");
            return false;
        }

        return true;
    }

    private void clearFields() {
        titleField.clear();
        lieuxField.clear();
        descriptionArea.clear();
        dureeField.clear();
        nbPlacesField.clear();
        datePicker.setValue(null);
        statusChoiceBox.setValue("Pending");
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
