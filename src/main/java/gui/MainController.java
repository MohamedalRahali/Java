package gui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import models.Event;
import Services.EventService;

import java.io.IOException;
import java.sql.Date;

public class MainController {

    @FXML private TextField tfTitle, tfDescription, tfLieux, tfStatus, tfDuree, tfNbPlaces;
    @FXML private DatePicker dpDate;
    @FXML private TableView<Event> eventTable;
    @FXML private TableColumn<Event, String> colTitle, colDate, colLieux, colStatus, colNbPlaces;

    private final EventService service = new EventService();

    @FXML
    public void initialize() {
        // Initialisation des colonnes de la table
        colTitle.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getTitle()));
        colDate.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getDate().toString()));
        colLieux.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getLieux()));
        colStatus.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getStatus()));
        colNbPlaces.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(String.valueOf(cell.getValue().getNb_place_dispo())));

        loadEvents();
    }

    private void loadEvents() {
        ObservableList<Event> eventList = FXCollections.observableArrayList(service.getAll());
        eventTable.setItems(eventList);
    }

    @FXML
    private void ajouterEvent() {
        try {
            // Validate all fields are filled
            if (tfTitle.getText().isEmpty() || dpDate.getValue() == null || tfDescription.getText().isEmpty() ||
                    tfLieux.getText().isEmpty() || tfStatus.getText().isEmpty() || tfDuree.getText().isEmpty() ||
                    tfNbPlaces.getText().isEmpty()) {
                showAlert("Erreur", "Veuillez remplir tous les champs!");
                return;
            }

            // Validate number of places is a positive integer
            int nbPlaces;
            try {
                nbPlaces = Integer.parseInt(tfNbPlaces.getText());
                if (nbPlaces <= 0) {
                    showAlert("Erreur", "Le nombre de places doit être un entier positif!");
                    return;
                }
            } catch (NumberFormatException e) {
                showAlert("Erreur", "Le nombre de places doit être un nombre entier!");
                return;
            }

            // Create new event
            Event e = new Event(
                    tfTitle.getText(),
                    Date.valueOf(dpDate.getValue()),
                    tfDescription.getText(),
                    tfLieux.getText(),
                    tfStatus.getText(),
                    tfDuree.getText(),
                    nbPlaces
            );

            service.ajouter(e);
            loadEvents();
            clearFields();

        } catch (Exception e) {
            showAlert("Erreur", "Une erreur est survenue: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void modifierEvent() {
        try {
            Event selected = eventTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert("Erreur", "Veuillez sélectionner un événement à modifier!");
                return;
            }

            // Charger la page de modification
            FXMLLoader loader = new FXMLLoader(getClass().getResource("resources/modification_event.fxml"));
            Parent root = loader.load();

            // Passer l'événement sélectionné au contrôleur de modification
            ModificationController controller = loader.getController();
            controller.initData(selected);

            // Afficher la nouvelle scène
            Stage stage = (Stage) tfTitle.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            showAlert("Erreur", "Impossible de charger la page de modification: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void supprimerEvent() {
        try {
            Event selected = eventTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert("Erreur", "Veuillez sélectionner un événement à supprimer!");
                return;
            }

            service.supprimer(selected.getId());
            loadEvents();
            clearFields();

        } catch (Exception e) {
            showAlert("Erreur", "Une erreur est survenue: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void clearFields() {
        tfTitle.clear();
        tfDescription.clear();
        tfLieux.clear();
        tfStatus.clear();
        tfDuree.clear();
        tfNbPlaces.clear();
        dpDate.setValue(null);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}