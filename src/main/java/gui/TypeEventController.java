package gui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import models.TypeEvent;
import Services.TypeEventService;

import java.io.IOException;

public class TypeEventController {
    @FXML private TextField nameField;
    @FXML private TextField descriptionField;
    @FXML private TableView<TypeEvent> typeTable;
    @FXML private TableColumn<TypeEvent, String> nameColumn;
    @FXML private TableColumn<TypeEvent, String> descriptionColumn;
    @FXML private TableColumn<TypeEvent, String> countColumn;

    private final TypeEventService service = new TypeEventService();
    private final ObservableList<TypeEvent> typeEvents = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Configure table columns
        nameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        descriptionColumn.setCellValueFactory(cellData -> cellData.getValue().descriptionProperty());
        countColumn.setCellValueFactory(cellData -> cellData.getValue().countProperty());

        // Load data
        loadTypeEvents();

        // Add selection listener
        typeTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                nameField.setText(newSelection.getName());
                descriptionField.setText(newSelection.getDescription());
            }
        });
    }

    private void loadTypeEvents() {
        typeEvents.setAll(service.getAll());
        typeTable.setItems(typeEvents);
    }

    private boolean validateInputs() {
        String name = nameField.getText().trim();
        String description = descriptionField.getText().trim();
        
        if (name.isEmpty()) {
            showAlert("Erreur", "Le nom du type d'événement est obligatoire");
            nameField.requestFocus();
            return false;
        }
        
        if (name.length() < 3) {
            showAlert("Erreur", "Le nom doit contenir au moins 3 caractères");
            nameField.requestFocus();
            return false;
        }
        
        if (description.isEmpty()) {
            showAlert("Erreur", "La description du type d'événement est obligatoire");
            descriptionField.requestFocus();
            return false;
        }
        
        if (description.length() < 10) {
            showAlert("Erreur", "La description doit contenir au moins 10 caractères");
            descriptionField.requestFocus();
            return false;
        }
        
        return true;
    }

    @FXML
    private void handleAdd() {
        if (!validateInputs()) {
            return;
        }

        TypeEvent typeEvent = new TypeEvent();
        typeEvent.setName(nameField.getText().trim());
        typeEvent.setDescription(descriptionField.getText().trim());

        service.add(typeEvent);
        loadTypeEvents();
        clearFields();
    }

    @FXML
    private void handleEdit() {
        TypeEvent selectedTypeEvent = typeTable.getSelectionModel().getSelectedItem();
        if (selectedTypeEvent == null) {
            showAlert("Erreur", "Veuillez sélectionner un type d'événement à modifier");
            return;
        }

        if (!validateInputs()) {
            return;
        }

        TypeEvent updatedTypeEvent = new TypeEvent();
        updatedTypeEvent.setId(selectedTypeEvent.getId());
        updatedTypeEvent.setName(nameField.getText().trim());
        updatedTypeEvent.setDescription(descriptionField.getText().trim());

        service.update(updatedTypeEvent);
        loadTypeEvents();
        clearFields();
    }

    @FXML
    private void handleDelete() {
        TypeEvent selectedTypeEvent = typeTable.getSelectionModel().getSelectedItem();
        if (selectedTypeEvent == null) {
            showAlert("Erreur", "Veuillez sélectionner un type d'événement à supprimer");
            return;
        }

        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirmation");
        confirmDialog.setHeaderText("Supprimer le type d'événement");
        confirmDialog.setContentText("Êtes-vous sûr de vouloir supprimer ce type d'événement ?");

        if (confirmDialog.showAndWait().get() == ButtonType.OK) {
            service.delete(selectedTypeEvent.getId());
            loadTypeEvents();
            clearFields();
        }
    }

    @FXML
    private void goToCreateEvent() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/create_event.fxml"));
        Parent root = loader.load();
        Stage stage = (Stage) typeTable.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("Create Event");
    }

    @FXML
    private void goToEventView() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/event_view.fxml"));
        Parent root = loader.load();
        Stage stage = (Stage) typeTable.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("View Events");
    }

    @FXML
    private void goToStatistics() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/statistics_view.fxml"));
        Parent root = loader.load();
        Stage stage = (Stage) typeTable.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("Statistics");
    }

    private void clearFields() {
        nameField.clear();
        descriptionField.clear();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}