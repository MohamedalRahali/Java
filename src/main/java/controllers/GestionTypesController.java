package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.scene.Node;
import java.io.IOException;
import java.sql.SQLException;
import model.Type_b;
import service.Type_bCRUD;
import javafx.event.ActionEvent;

public class GestionTypesController {

    @FXML private TextField libelleField;
    @FXML private ListView<Type_b> typesListView;

    private final Type_bCRUD service = new Type_bCRUD();
    private Type_b selectedType;

    @FXML
    public void initialize() {
        refreshList();

        typesListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Type_b item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText("🏷️ " + item.getLibelle());
                }
            }
        });

        typesListView.setOnMouseClicked((MouseEvent e) -> {
            selectedType = typesListView.getSelectionModel().getSelectedItem();
            if (selectedType != null) {
                libelleField.setText(selectedType.getLibelle());
            }
        });
    }

    @FXML
    public void handleAdd() {
        if (!validateFields()) return;

        String libelle = libelleField.getText().trim();

        try {
            Type_b type = new Type_b(libelle);
            service.add(type);
            refreshList();
            clearForm();
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Type ajouté avec succès !");
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur SQL", e.getMessage());
        }
    }

    @FXML
    public void handleModify() {
        if (selectedType == null) {
            showAlert(Alert.AlertType.WARNING, "Aucune sélection", "Veuillez sélectionner un type à modifier.");
            return;
        }

        if (!validateFields()) return;

        selectedType.setLibelle(libelleField.getText().trim());

        try {
            service.update(selectedType);
            refreshList();
            clearForm();
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Type modifié avec succès !");
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur SQL", e.getMessage());
        }
    }

    @FXML
    public void handleDelete() {
        if (selectedType == null) {
            showAlert(Alert.AlertType.WARNING, "Aucune sélection", "Veuillez sélectionner un type à supprimer.");
            return;
        }

        try {
            service.delete(selectedType.getId());
            refreshList();
            clearForm();
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Type supprimé avec succès !");
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur SQL", e.getMessage());
        }
    }

    @FXML
    public void handleClear() {
        clearForm();
    }

    @FXML
    public void handleTypesAction(ActionEvent event) {
    }

    @FXML
    public void handleBlogsAction(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/FXML/AfficherBlogs.fxml"));
            Stage stage = (Stage)((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Liste des Blogs");
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de navigation", e.getMessage());
        }
    }

    private void clearForm() {
        libelleField.clear();
        selectedType = null;
        typesListView.getSelectionModel().clearSelection();
    }

    private void refreshList() {
        try {
            ObservableList<Type_b> list = FXCollections.observableArrayList(service.display());
            typesListView.setItems(list);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur SQL", "Impossible de charger les types: " + e.getMessage());
        }
    }

    private boolean validateFields() {
        String libelle = libelleField.getText().trim();

        if (libelle.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Le libellé ne peut pas être vide.");
            return false;
        }

        if (libelle.length() < 3) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Le libellé doit contenir au moins 3 caractères.");
            return false;
        }

        return true;
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}