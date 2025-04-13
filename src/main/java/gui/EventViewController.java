package gui;

import Services.EventService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import models.Event;

import java.io.IOException;
import java.util.Optional;

public class EventViewController {

    @FXML private TableView<Event> eventTable;
    @FXML private TableColumn<Event, String> colTitle, colDate, colLieux, colStatus, colNbPlaces;

    private final EventService service = new EventService();

    @FXML
    public void initialize() {
        colTitle.setCellValueFactory(cell -> cell.getValue().titleProperty());
        colDate.setCellValueFactory(cell -> cell.getValue().dateProperty().asString());
        colLieux.setCellValueFactory(cell -> cell.getValue().lieuxProperty());
        colStatus.setCellValueFactory(cell -> cell.getValue().statusProperty());
        colNbPlaces.setCellValueFactory(cell -> cell.getValue().nbPlacesProperty().asString());

        loadEvents();
    }

    private void loadEvents() {
        ObservableList<Event> eventList = FXCollections.observableArrayList(service.getAll());
        eventTable.setItems(eventList);
    }

    @FXML
    private void handleEditEvent() throws IOException {
        Event selectedEvent = eventTable.getSelectionModel().getSelectedItem();
        if (selectedEvent == null) {
            showAlert("Veuillez sélectionner un événement à modifier.");
            return;
        }

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/modify.fxml"));
        Parent root = loader.load();

        // Correction ici : utiliser ModificationController
        ModifyEventController controller = loader.getController();
        controller.setEventToEdit(selectedEvent);

        Stage stage = new Stage();
        stage.setTitle("Modifier Événement");
        stage.setScene(new Scene(root));
        stage.show();

        ((Stage) eventTable.getScene().getWindow()).close();
    }

    @FXML
    private void handleDeleteEvent() {
        Event selectedEvent = eventTable.getSelectionModel().getSelectedItem();
        if (selectedEvent == null) {
            showAlert("Veuillez sélectionner un événement à supprimer.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Suppression d'événement");
        alert.setContentText("Voulez-vous vraiment supprimer cet événement ?");
        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            service.supprimer(selectedEvent.getId());
            loadEvents(); // Rafraîchir la table
        }
    }

    @FXML
    private void goBackToHome() throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/Home.fxml"));
        Stage stage = new Stage();
        stage.setTitle("Accueil");
        stage.setScene(new Scene(root));
        stage.show();

        ((Stage) eventTable.getScene().getWindow()).close();
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Attention");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
