package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;
import javafx.scene.Node;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import model.CategorieFormation;
import model.Formation;
import service.CategorieFormationCRUD;
import service.FormationService;
import service.IService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class AjouterFormation {

    public AjouterFormation() {
        System.out.println("AjouterFormation controller initialized");
    }

    @FXML
    private TextField titre;
    @FXML
    private TextField description;
    @FXML
    private TextField prix;
    @FXML
    private TextField places_disponibles; // Must match fx:id in FXML
    @FXML
    private ComboBox<CategorieFormation> categorieComboBox;

    private final IService<Formation> fs = new FormationService();
    // To store the selected image file
    @FXML
    void save(ActionEvent event) {
        resetFieldStyles();

        if (!validateFields()) return;
        
        if (categorieComboBox.getValue() == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please select a category");
            return;
        }

        try {
            Formation formation = new Formation();
            formation.setTitre(titre.getText());
            formation.setDescription(description.getText());
            formation.setPrix(Double.parseDouble(prix.getText()));
            formation.setPlacesDisponibles(Integer.parseInt(places_disponibles.getText()));
            formation.setCategorieId(categorieComboBox.getValue().getId());
            
            boolean success = fs.add(formation);
            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Formation added successfully!");
                clearForm();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to add formation!");
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Invalid Input", "Please enter valid numbers for price and places");
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", e.getMessage());
        }
    }

    private boolean validateFields() {
        boolean isValid = true;

        if (titre.getText().trim().isEmpty() || titre.getText().trim().length() < 4) {
            setFieldInvalid(titre, "Le titre doit contenir au moins 4 caractères.");
            isValid = false;
        }

        if (description.getText().trim().isEmpty() || description.getText().trim().length() < 6) {
            setFieldInvalid(description, "La description doit contenir au moins 6 caractères.");
            isValid = false;
        }

        try {
            double prixValue = Double.parseDouble(prix.getText().trim());
            if (prixValue < 0) {
                setFieldInvalid(prix, "Le prix doit être un nombre positif.");
                isValid = false;
            }
        } catch (NumberFormatException e) {
            setFieldInvalid(prix, "Le prix doit être un nombre valide.");
            isValid = false;
        }

        try {
            int places = Integer.parseInt(places_disponibles.getText().trim());
            if (places <= 0) {
                setFieldInvalid(places_disponibles, "Les places disponibles doivent être supérieures à zéro.");
                isValid = false;
            }
        } catch (NumberFormatException e) {
            setFieldInvalid(places_disponibles, "Les places disponibles doivent être un entier valide.");
            isValid = false;
        }

        return isValid;
    }

    private void clearForm() {
        titre.clear();
        description.clear();
        prix.clear();
        places_disponibles.clear();
        categorieComboBox.getSelectionModel().clearSelection();
        resetFieldStyles();
    }

    private void setFieldInvalid(TextField field, String message) {
        if (field != null) {
            field.setStyle("-fx-border-color: red; -fx-border-width: 2;");
            showAlert(Alert.AlertType.WARNING, "Validation", message);
        }
    }

    private void setFieldInvalid(ComboBox<CategorieFormation> field, String message) {
        if (field != null) {
            field.setStyle("-fx-border-color: red; -fx-border-width: 2;");
            showAlert(Alert.AlertType.WARNING, "Validation", message);
        }
    }

    private void resetFieldStyles() {
        if (titre != null) titre.setStyle("");
        if (description != null) description.setStyle("");
        if (prix != null) prix.setStyle("");
        if (places_disponibles != null) places_disponibles.setStyle("");
        if (categorieComboBox != null) categorieComboBox.setStyle("");
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    public void initialize() {
        System.out.println("FXML initialize() called");
        
        // Initialize category combo box
        try {
            CategorieFormationCRUD categoryService = new CategorieFormationCRUD();
            List<CategorieFormation> categories = categoryService.getAll();
            
            // Set items and converter
            categorieComboBox.getItems().addAll(categories);
            
            categorieComboBox.setCellFactory(lv -> new ListCell<CategorieFormation>() {
                @Override
                protected void updateItem(CategorieFormation item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? "" : item.getNom());
                }
            });
            
            categorieComboBox.setButtonCell(new ListCell<CategorieFormation>() {
                @Override
                protected void updateItem(CategorieFormation item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? "" : item.getNom());
                }
            });
            
        } catch (SQLException e) {
            System.err.println("Error loading categories: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load categories");
        }
        
        if (places_disponibles == null) {
            System.err.println("FXML INJECTION ERROR: places_disponibles is null");
        } else {
            System.out.println("FXML injection successful for places_disponibles");
        }
    }

    @FXML
    private void goToCategorieView(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/CategorieFormation.fxml"));
            Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Could not load categories view");
        }
    }

    @FXML
    private void gererCategories(ActionEvent event) {
        goToCategorieView(event);
    }

    @FXML
    void display(ActionEvent event) {
        changeScene(event, "/FXML/AfficherFormation.fxml", "Liste des Formations");
    }

    @FXML
    private void afficherCarte(ActionEvent event) {
        changeScene(event, "/FXML/Carte.fxml", "Carte");
    }

    private void changeScene(ActionEvent event, String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur FXML", "Impossible de charger la vue : " + e.getMessage());
        }
    }
    @FXML
    private void afficherCalendrier(ActionEvent event) {
        changeScene(event, "/FXML/Calendrier.fxml", "Calendrier");
    }

    @FXML
    private void goToAdminHome(ActionEvent event) {
        changeScene(event, "/fxml/admin_home.fxml", "Admin Home");
    }

    @FXML
    private void goToHomeReclamation(ActionEvent event) {
        changeScene(event, "/fxml/HomeReclamation.fxml", "Reclamation Home");
    }

    @FXML
    private void goToEventView(ActionEvent event) {
        changeScene(event, "/fxml/event_view.fxml", "Event Management");
    }

    @FXML
    private void goToBlogView(ActionEvent event) {
        changeScene(event, "/fxml/AfficherBlogs.fxml", "Blogs");
    }

    @FXML
    private void logout(ActionEvent event) {
        changeScene(event, "/fxml/login.fxml", "Login");
    }

    @FXML
    private void goToCreateUser(ActionEvent event) {
        changeScene(event, "/fxml/CreateUser.fxml", "Create User");
    }

    @FXML
    private void goToUserView(ActionEvent event) {
        changeScene(event, "/fxml/UserView.fxml", "User View");
    }

    @FXML
    private void goToAjouterFormation(ActionEvent event) {
        // We're already on this page, so do nothing
    }

}
