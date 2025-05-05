package gui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import models.Reclamation;
import Services.ReclamationService;
import models.TypeReclamation;
import Services.TypeReclamationService;

import java.io.IOException;
import java.util.List;

public class MainController {

    @FXML private TextField tfTitle;
    @FXML private TextArea taDescription;
    @FXML private ComboBox<TypeReclamation> typeComboBox;
    @FXML private TableView<Reclamation> reclamationTable;
    @FXML private TableColumn<Reclamation, String> colTitle;
    @FXML private TableColumn<Reclamation, String> colDescription;

    private final ReclamationService service = new ReclamationService();
    private final TypeReclamationService typeService = new TypeReclamationService();

    @FXML
    public void initialize() {
        // Initialisation des colonnes de la table
        colTitle.setCellValueFactory(cell -> cell.getValue().titleProperty());
        colDescription.setCellValueFactory(cell -> cell.getValue().descriptionProperty());

        // Charger les types de réclamation dans le ComboBox
        List<TypeReclamation> types = typeService.getAll();
        typeComboBox.setItems(FXCollections.observableArrayList(types));

        loadReclamations();
    }

    private void loadReclamations() {
        ObservableList<Reclamation> reclamationList = FXCollections.observableArrayList(service.getAll());
        reclamationTable.setItems(reclamationList);
    }

    @FXML
    private void ajouterReclamation() {
        try {
            // Validate all fields are filled
            if (tfTitle.getText().isEmpty() || taDescription.getText().isEmpty() || typeComboBox.getValue() == null) {
                showAlert("Erreur", "Veuillez remplir tous les champs et sélectionner un type de réclamation!");
                return;
            }

            // Create new reclamation
            Reclamation r = new Reclamation(
                    tfTitle.getText().trim(),
                    taDescription.getText().trim(),
                    typeComboBox.getValue()
            );

            service.ajouter(r);
            loadReclamations();
            clearFields();

        } catch (Exception e) {
            showAlert("Erreur", "Une erreur est survenue: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void modifierReclamation() {
        try {
            Reclamation selected = reclamationTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert("Erreur", "Veuillez sélectionner une réclamation à modifier!");
                return;
            }

            // Charger la page de modification
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/modification_reclamation.fxml"));
            Parent root = loader.load();

            // Passer la réclamation sélectionnée au contrôleur de modification
            ModifyReclamationController controller = loader.getController();
            controller.setReclamationToEdit(selected);

            // Afficher la nouvelle scène
            Stage stage = new Stage();
            stage.setTitle("Modifier Réclamation");
            stage.setScene(new Scene(root));
            stage.show();

            // Fermer la fenêtre actuelle
            Stage currentStage = (Stage) tfTitle.getScene().getWindow();
            currentStage.close();

        } catch (IOException e) {
            showAlert("Erreur", "Impossible de charger la page de modification: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void supprimerReclamation() {
        try {
            Reclamation selected = reclamationTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert("Erreur", "Veuillez sélectionner une réclamation à supprimer!");
                return;
            }

            // Confirmation de suppression
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation");
            alert.setHeaderText("Suppression de réclamation");
            alert.setContentText("Voulez-vous vraiment supprimer cette réclamation ?");

            if (alert.showAndWait().get() == ButtonType.OK) {
                service.supprimer(selected.getId());
                loadReclamations();
                clearFields();
            }

        } catch (Exception e) {
            showAlert("Erreur", "Une erreur est survenue: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void clearFields() {
        tfTitle.clear();
        taDescription.clear();
        typeComboBox.setValue(null);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}