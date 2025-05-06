package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import model.CategorieFormation;
import service.CategorieFormationCRUD;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class CategorieFormationController implements Initializable {

    @FXML private TableView<CategorieFormation> categoriesTable;
    @FXML private TableColumn<CategorieFormation, String> nomColumn;
    @FXML private TextField libelleField;

    private CategorieFormationCRUD categorieCRUD;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            categorieCRUD = new CategorieFormationCRUD();
            
            // Configure table columns
            nomColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));
            
            // Load initial data
            refreshTable();
            
        } catch (SQLException e) {
            showAlert("Database Error", "Failed to initialize: " + e.getMessage());
        }
    }

    @FXML
    private void handleAddCategory() {
        String nom = libelleField.getText().trim();
        
        if (nom.isEmpty()) {
            showAlert("Input Error", "Nom cannot be empty");
            return;
        }
        
        CategorieFormation newCategory = new CategorieFormation();
        newCategory.setNom(nom);
        
        if (categorieCRUD.add(newCategory)) {
            refreshTable();
            libelleField.clear();
        } else {
            showAlert("Error", "Failed to add category");
        }
    }

    @FXML
    private void ajouterCategorie(javafx.event.ActionEvent event) {
        handleAddCategory();
    }

    private void refreshTable() {
        categoriesTable.getItems().setAll(categorieCRUD.display());
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void retourAccueil(javafx.event.ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/FXML/AjouterFormation.fxml"));
            Stage stage = (Stage)((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Accueil");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void modifierCategorie(javafx.event.ActionEvent event) {
        CategorieFormation selectedCategory = categoriesTable.getSelectionModel().getSelectedItem();
        if (selectedCategory == null) {
            showAlert("Selection Error", "Please select a category to modify");
            return;
        }

        String newNom = libelleField.getText().trim();
        if (newNom.isEmpty()) {
            showAlert("Input Error", "Nom cannot be empty");
            return;
        }

        selectedCategory.setNom(newNom);
        if (categorieCRUD.update(selectedCategory)) {
            refreshTable();
            libelleField.clear();
            showAlert("Success", "Category updated successfully");
        } else {
            showAlert("Error", "Failed to update category");
        }
    }

    @FXML
    private void supprimerCategorie(javafx.event.ActionEvent event) {
        CategorieFormation selectedCategory = categoriesTable.getSelectionModel().getSelectedItem();
        if (selectedCategory == null) {
            showAlert("Selection Error", "Please select a category to delete");
            return;
        }

        try {
            if (categorieCRUD.delete(selectedCategory.getId())) {
                refreshTable();
                libelleField.clear();
                showAlert("Success", "Category deleted successfully");
            } else {
                showAlert("Error", "Failed to delete category");
            }
        } catch (SQLException e) {
            showAlert("Database Error", "Error deleting category: " + e.getMessage());
        }
    }

    @FXML
    private void clearForm(javafx.event.ActionEvent event) {
        libelleField.clear();
        categoriesTable.getSelectionModel().clearSelection();
    }
}
