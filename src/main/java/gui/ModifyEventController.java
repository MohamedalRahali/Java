package gui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import models.Event;
import models.TypeEvent;
import Services.EventService;
import Services.TypeEventService;

import java.io.IOException;
import java.sql.Date;
import java.time.LocalDate;
import java.util.regex.Pattern;

public class ModifyEventController {

    @FXML private TextField titleField;
    @FXML private DatePicker datePicker;
    @FXML private TextArea descriptionArea;
    @FXML private TextField lieuxField;
    @FXML private ChoiceBox<String> statusChoiceBox;
    @FXML private TextField dureeField;
    @FXML private Spinner<Integer> nbPlaceSpinner;
    @FXML private ComboBox<TypeEvent> typeEventComboBox;

    // Icônes de validation
    @FXML private Label titleValidationIcon;
    @FXML private Label dateValidationIcon;
    @FXML private Label descriptionValidationIcon;
    @FXML private Label lieuxValidationIcon;
    @FXML private Label statusValidationIcon;
    @FXML private Label dureeValidationIcon;
    @FXML private Label nbPlacesValidationIcon;
    @FXML private Label typeEventValidationIcon;

    // Labels d'erreur
    @FXML private Label titleErrorLabel;
    @FXML private Label dateErrorLabel;
    @FXML private Label descriptionErrorLabel;
    @FXML private Label lieuxErrorLabel;
    @FXML private Label statusErrorLabel;
    @FXML private Label dureeErrorLabel;
    @FXML private Label nbPlacesErrorLabel;
    @FXML private Label typeEventErrorLabel;

    private Event eventToEdit;
    private TypeEvent originalTypeEvent;
    private final EventService service = new EventService();
    private final TypeEventService typeEventService = new TypeEventService();

    public void setEventToEdit(Event event) {
        this.eventToEdit = event;
        this.originalTypeEvent = event.getTypeEvent();

        // Initialiser le Spinner avec un SpinnerValueFactory
        SpinnerValueFactory<Integer> valueFactory = 
            new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 1000, event.getNb_place_dispo());
        nbPlaceSpinner.setValueFactory(valueFactory);

        // Remplir les champs avec les données existantes
        titleField.setText(event.getTitle());
        datePicker.setValue(event.getDate().toLocalDate());
        descriptionArea.setText(event.getDescription());
        lieuxField.setText(event.getLieux());
        statusChoiceBox.getItems().addAll("En cours", "Terminé", "Annulé");
        statusChoiceBox.setValue(event.getStatus());
        dureeField.setText(event.getDuree());

        // Initialiser le ComboBox de type d'événement
        typeEventComboBox.getItems().addAll(typeEventService.getAll());
        typeEventComboBox.setValue(event.getTypeEvent());

        // Initialiser la validation
        setupValidationListeners();
    }

    private void setupValidationListeners() {
        // Validation du titre
        titleField.textProperty().addListener((obs, oldVal, newVal) -> {
            boolean isValid = newVal != null && newVal.trim().length() >= 3;
            updateFieldStyle(titleField, isValid);
            updateValidationIcon(titleValidationIcon, isValid);
            updateErrorLabel(titleErrorLabel, isValid);
        });

        // Validation de la date
        datePicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            boolean isValid = newVal != null && !newVal.isBefore(LocalDate.now());
            updateFieldStyle(datePicker, isValid);
            updateValidationIcon(dateValidationIcon, isValid);
            updateErrorLabel(dateErrorLabel, isValid);
        });

        // Validation du lieu
        lieuxField.textProperty().addListener((obs, oldVal, newVal) -> {
            boolean isValid = newVal != null && !newVal.trim().isEmpty();
            updateFieldStyle(lieuxField, isValid);
            updateValidationIcon(lieuxValidationIcon, isValid);
            updateErrorLabel(lieuxErrorLabel, isValid);
        });

        // Validation de la description
        descriptionArea.textProperty().addListener((obs, oldVal, newVal) -> {
            boolean isValid = newVal != null && newVal.trim().length() >= 10;
            updateFieldStyle(descriptionArea, isValid);
            updateValidationIcon(descriptionValidationIcon, isValid);
            updateErrorLabel(descriptionErrorLabel, isValid);
        });

        // Validation de la durée
        dureeField.textProperty().addListener((obs, oldVal, newVal) -> {
            boolean isValid = newVal != null && Pattern.matches("\\d+h(\\d{1,2})?", newVal.trim());
            updateFieldStyle(dureeField, isValid);
            updateValidationIcon(dureeValidationIcon, isValid);
            updateErrorLabel(dureeErrorLabel, isValid);
        });

        // Validation du nombre de places
        nbPlaceSpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
            boolean isValid = newVal != null && newVal > 0;
            updateFieldStyle(nbPlaceSpinner, isValid);
            updateValidationIcon(nbPlacesValidationIcon, isValid);
            updateErrorLabel(nbPlacesErrorLabel, isValid);
        });

        // Validation du statut
        statusChoiceBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            boolean isValid = newVal != null && !newVal.trim().isEmpty();
            updateFieldStyle(statusChoiceBox, isValid);
            updateValidationIcon(statusValidationIcon, isValid);
            updateErrorLabel(statusErrorLabel, isValid);
        });

        // Validation du type d'événement
        typeEventComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            boolean isValid = newVal != null;
            updateFieldStyle(typeEventComboBox, isValid);
            updateValidationIcon(typeEventValidationIcon, isValid);
            updateErrorLabel(typeEventErrorLabel, isValid);
            
            // Vérifier si le type a été modifié en comparant les IDs
            if (newVal != null && originalTypeEvent != null && 
                newVal.getId() != originalTypeEvent.getId()) {
                showAlert(Alert.AlertType.INFORMATION, "Changement de type", 
                    "Le type d'événement a été modifié de '" + originalTypeEvent.getName() + 
                    "' à '" + newVal.getName() + "'");
            }
        });
    }

    private void updateFieldStyle(Control field, boolean isValid) {
        if (isValid) {
            field.getStyleClass().remove("invalid-field");
            field.getStyleClass().add("valid-field");
        } else {
            field.getStyleClass().remove("valid-field");
            field.getStyleClass().add("invalid-field");
        }
    }

    private void updateValidationIcon(Label icon, boolean isValid) {
        if (isValid) {
            icon.setText("✅");
            icon.getStyleClass().remove("invalid-icon");
            icon.getStyleClass().add("valid-icon");
        } else {
            icon.setText("❌");
            icon.getStyleClass().remove("valid-icon");
            icon.getStyleClass().add("invalid-icon");
        }
    }

    private void updateErrorLabel(Label errorLabel, boolean isValid) {
        if (isValid) {
            errorLabel.getStyleClass().remove("visible");
            errorLabel.getStyleClass().add("hidden");
        } else {
            errorLabel.getStyleClass().remove("hidden");
            errorLabel.getStyleClass().add("visible");
        }
    }

    @FXML
    private void saveModifications() {
        try {
            if (!validateFields()) return;

            // Vérifier si le type d'événement a été modifié en comparant les IDs
            TypeEvent newTypeEvent = typeEventComboBox.getValue();
            if (newTypeEvent != null && originalTypeEvent != null && 
                newTypeEvent.getId() != originalTypeEvent.getId()) {
                Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
                confirmAlert.setTitle("Confirmation de modification");
                confirmAlert.setHeaderText("Changement de type d'événement");
                confirmAlert.setContentText("Êtes-vous sûr de vouloir changer le type d'événement de '" + 
                    originalTypeEvent.getName() + "' à '" + newTypeEvent.getName() + "' ?");
                
                if (confirmAlert.showAndWait().get() != ButtonType.OK) {
                    return;
                }
            }

            // Mettre à jour l'événement
            eventToEdit.setTitle(titleField.getText().trim());
            eventToEdit.setDate(Date.valueOf(datePicker.getValue()));
            eventToEdit.setDescription(descriptionArea.getText().trim());
            eventToEdit.setLieux(lieuxField.getText().trim());
            eventToEdit.setStatus(statusChoiceBox.getValue());
            eventToEdit.setDuree(dureeField.getText().trim());
            eventToEdit.setNb_place_dispo(nbPlaceSpinner.getValue());
            eventToEdit.setTypeEvent(newTypeEvent);

            // Sauvegarder les modifications
            service.modifier(eventToEdit);

            showAlert(Alert.AlertType.INFORMATION, "Succès", "Événement modifié avec succès");
            goBackToEventView();

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }
    }

    private boolean validateFields() {
        String title = titleField.getText().trim();
        String lieux = lieuxField.getText().trim();
        String description = descriptionArea.getText().trim();
        String duree = dureeField.getText().trim();
        LocalDate date = datePicker.getValue();
        String status = statusChoiceBox.getValue();
        Integer nbPlaces = nbPlaceSpinner.getValue();
        TypeEvent typeEvent = typeEventComboBox.getValue();

        if (title.isEmpty() || lieux.isEmpty() || description.isEmpty() || duree.isEmpty() ||
                date == null || status == null || nbPlaces == null || typeEvent == null) {
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

        if (nbPlaces <= 0) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Le nombre de places doit être supérieur à zéro.");
            return false;
        }

        if (date.isBefore(LocalDate.now())) {
            showAlert(Alert.AlertType.WARNING, "Validation", "La date de l'événement doit être future.");
            return false;
        }

        if (typeEvent == null) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Veuillez sélectionner un type d'événement");
            return false;
        }

        return true;
    }

    private void goBackToEventView() throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/event_view.fxml"));
        Stage stage = new Stage();
        stage.setTitle("Liste des événements");
        stage.setScene(new Scene(root));
        stage.show();

        // Fermer la fenêtre actuelle
        Stage currentStage = (Stage) titleField.getScene().getWindow();
        currentStage.close();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    public void goToCreateEvent(javafx.event.ActionEvent event) {
        // TODO: Implement navigation logic to the Create Event view here
        // Example (if using FXML):
        // try {
        //     FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/create_event.fxml"));
        //     Parent root = loader.load();
        //     Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        //     stage.setScene(new Scene(root));
        // } catch (IOException e) {
        //     showAlert(Alert.AlertType.ERROR, "Navigation Error", "Could not open Create Event view: " + e.getMessage());
        // }
    }

    @FXML
    public void goToTypeEventManagement(javafx.event.ActionEvent event) {
        // TODO: Implement navigation logic to the Type Event Management view here
        // Example:
        // try {
        //     FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/type_event_management.fxml"));
        //     Parent root = loader.load();
        //     Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        //     stage.setScene(new Scene(root));
        // } catch (IOException e) {
        //     showAlert(Alert.AlertType.ERROR, "Navigation Error", "Could not open Type Event Management view: " + e.getMessage());
        // }
    }

    @FXML
    public void goToStatistics(javafx.event.ActionEvent event) {
        // TODO: Implement navigation logic to the Statistics view here
        // Example:
        // try {
        //     FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/statistics.fxml"));
        //     Parent root = loader.load();
        //     Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        //     stage.setScene(new Scene(root));
        // } catch (IOException e) {
        //     showAlert(Alert.AlertType.ERROR, "Navigation Error", "Could not open Statistics view: " + e.getMessage());
        // }
    }

    @FXML
    public void goBackToEventView(javafx.event.ActionEvent event) {
        // TODO: Implement navigation logic to return to the Event View here
        // Example:
        // try {
        //     FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/event_view.fxml"));
        //     Parent root = loader.load();
        //     Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        //     stage.setScene(new Scene(root));
        // } catch (IOException e) {
        //     showAlert(Alert.AlertType.ERROR, "Navigation Error", "Could not open Event View: " + e.getMessage());
        // }
    }
}