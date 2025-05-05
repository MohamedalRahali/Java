package gui;

import Services.TypeReclamationService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import models.TypeReclamation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

public class TypeReclamationController {

    private static final Logger logger = LoggerFactory.getLogger(TypeReclamationController.class);
    private final TypeReclamationService typeService;

    @FXML
    private TextField typeNameField;
    @FXML
    private ListView<TypeReclamation> typeListView;

    public TypeReclamationController() {
        this.typeService = new TypeReclamationService();
    }

    @FXML
    private void initialize() {
        try {
            loadTypes();
        } catch (SQLException e) {
            showError("Erreur", "Impossible de charger les types de réclamation");
            logger.error("Erreur lors du chargement des types: {}", e.getMessage(), e);
        }
    }

    private void loadTypes() throws SQLException {
        ObservableList<TypeReclamation> types = FXCollections.observableArrayList(typeService.getAll());
        typeListView.setItems(types);
        typeListView.setCellFactory(lv -> new ListCell<TypeReclamation>() {
            @Override
            protected void updateItem(TypeReclamation item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item.getName());
            }
        });
    }

    @FXML
    private void handleAddType() {
        String name = typeNameField.getText().trim();
        if (name.isEmpty()) {
            showError("Erreur", "Veuillez entrer un nom pour le type");
            return;
        }

        try {
            TypeReclamation type = new TypeReclamation();
            type.setName(name);
            typeService.ajouter(type);
            typeNameField.clear();
            loadTypes();
            showSuccess("Succès", "Type de réclamation ajouté avec succès");
        } catch (SQLException e) {
            showError("Erreur", "Impossible d'ajouter le type de réclamation");
            logger.error("Erreur lors de l'ajout du type: {}", e.getMessage(), e);
        }
    }

    @FXML
    private void handleModifyType() {
        TypeReclamation selectedType = typeListView.getSelectionModel().getSelectedItem();
        if (selectedType == null) {
            showError("Erreur", "Veuillez sélectionner un type à modifier");
            return;
        }

        String newName = typeNameField.getText().trim();
        if (newName.isEmpty()) {
            showError("Erreur", "Veuillez entrer un nouveau nom");
            return;
        }

        try {
            selectedType.setName(newName);
            typeService.modifier(selectedType);
            typeNameField.clear();
            loadTypes();
            showSuccess("Succès", "Type de réclamation modifié avec succès");
        } catch (SQLException e) {
            showError("Erreur", "Impossible de modifier le type de réclamation");
            logger.error("Erreur lors de la modification du type: {}", e.getMessage(), e);
        }
    }

    @FXML
    private void handleDeleteType() {
        TypeReclamation selectedType = typeListView.getSelectionModel().getSelectedItem();
        if (selectedType == null) {
            showError("Erreur", "Veuillez sélectionner un type à supprimer");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Suppression de type");
        alert.setContentText("Voulez-vous vraiment supprimer ce type de réclamation ?\n\n" +
                           "Attention : Toutes les réclamations associées à ce type seront également supprimées.\n" +
                           "Cette action est irréversible.");
        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                typeService.supprimer(selectedType.getId());
                loadTypes();
                showSuccess("Succès", "Type de réclamation et ses réclamations associées supprimés avec succès");
            } catch (SQLException e) {
                showError("Erreur", "Une erreur est survenue lors de la suppression : " + e.getMessage());
                logger.error("Erreur lors de la suppression du type: {}", e.getMessage(), e);
            }
        }
    }

    @FXML
    private void goBackToHome() throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/Home.fxml"));
        Stage stage = new Stage();
        stage.setTitle("Accueil");
        stage.setScene(new Scene(root));
        stage.show();

        ((Stage) typeListView.getScene().getWindow()).close();
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showSuccess(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 