package gui;

import Services.TypeReclamationService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import models.TypeReclamation;
import models.CurrentUser;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;
import java.util.logging.Logger;

public class TypeReclamationController {
    @FXML
    private AdminBarController adminBarController;

    private static final Logger logger = Logger.getLogger(TypeReclamationController.class.getName());
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
        if (adminBarController != null && CurrentUser.getCurrentUser() != null) {
            adminBarController.setUserEmail(CurrentUser.getCurrentUser().getEmail());
        }
        try {
            loadTypes();
        } catch (SQLException e) {
            showError("Erreur", "Impossible de charger les types de réclamation");
            logger.severe("Erreur lors du chargement des types: " + e.getMessage());
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
            logger.severe("Erreur lors de l'ajout du type: " + e.getMessage());
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
            logger.severe("Erreur lors de la modification du type: " + e.getMessage());
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
                logger.severe("Erreur lors de la suppression du type: " + e.getMessage());
            }
        }
    }

    @FXML
    private void logout(javafx.event.ActionEvent event) throws IOException {
        CurrentUser.clear();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
        Parent root = loader.load();
        Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("Login");
    }

    @FXML
    private void goToReclamationView(javafx.event.ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/reclamation_view.fxml"));
        Parent root = loader.load();
        Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("Liste des Réclamations");
    }

    @FXML
    private void goToHomeReclamation(javafx.event.ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/HomeReclamation.fxml"));
        Parent root = loader.load();
        Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("Home Reclamation");
    }

    @FXML
    private void goToAdminHome(javafx.event.ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin_home.fxml"));
        Parent root = loader.load();
        Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("User Dashboard");
    }

    @FXML
    private void goBackToHome() throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/HomeReclamation.fxml"));
        Stage stage = (Stage) typeListView.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("Accueil");
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