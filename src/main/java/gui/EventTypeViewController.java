package gui;

import Services.TypeEvenementService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import models.TypeEvenement;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

public class EventTypeViewController {

    @FXML private TableView<TypeEvenement> typeTable;
    @FXML private TableColumn<TypeEvenement, String> colNom;
    @FXML private TableColumn<TypeEvenement, String> colDescription;

    private final TypeEvenementService service = new TypeEvenementService();

    @FXML
    public void initialize() {
        colNom.setCellValueFactory(cell -> cell.getValue().nomProperty());
        colDescription.setCellValueFactory(cell -> cell.getValue().descriptionProperty());
        refreshTable();
    }

    private void refreshTable() {
        try {
            typeTable.setItems(FXCollections.observableArrayList(service.getAll()));
        } catch (SQLException e) {
            showAlert("Erreur", "Échec du chargement des données");
        }
    }

    @FXML
    private void handleAddType() throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/create_type.fxml"));
        Stage stage = new Stage();
        stage.setTitle("Nouveau Type d'Événement");
        stage.setScene(new Scene(root));
        stage.show();
    }

    @FXML
    private void handleDeleteType() {
        TypeEvenement selected = typeTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer le type : " + selected.getNom());
        alert.setContentText("Êtes-vous sûr de vouloir supprimer ce type ?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                service.supprimer(selected.getId());
                refreshTable();
            } catch (SQLException e) {
                showAlert("Erreur", "Échec de la suppression");
            }
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}