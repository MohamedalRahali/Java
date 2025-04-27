package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.Blogs;
import service.BlogsService;
import service.IService;
import javafx.scene.control.ComboBox;
import model.Type_b;
import service.Type_bCRUD;
import javafx.collections.FXCollections;
import javafx.scene.control.ListCell;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;

public class AjouterBlog {

    @FXML
    private TextField titre;
    @FXML
    private TextArea descr;
    @FXML
    private DatePicker datePub;
    @FXML
    private ComboBox<Type_b> typeCombo;

    private final IService<Blogs> blogService = new BlogsService();
    private final Type_bCRUD typeCRUD = new Type_bCRUD();

    @FXML
    public void initialize() {
        try {
            // Charger les types dans le ComboBox
            typeCombo.setItems(FXCollections.observableArrayList(typeCRUD.display()));
            
            // Définir comment afficher les types dans le ComboBox
            typeCombo.setCellFactory(param -> new ListCell<Type_b>() {
                @Override
                protected void updateItem(Type_b item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.getLibelle());
                    }
                }
            });
            
            // Même chose pour l'affichage du type sélectionné
            typeCombo.setButtonCell(new ListCell<Type_b>() {
                @Override
                protected void updateItem(Type_b item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.getLibelle());
                    }
                }
            });
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du chargement des types : " + e.getMessage());
        }
    }

    @FXML
    private void save(ActionEvent event) {
        resetFieldStyles();

        if (!validateFields()) return;

        try {
            Type_b selectedType = typeCombo.getValue();
            Blogs blog = new Blogs(
                    titre.getText().trim(),
                    descr.getText().trim(),
                    datePub.getValue() != null ? java.sql.Date.valueOf(datePub.getValue()) : null,
                    selectedType
            );

            blogService.add(blog);
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Blog ajouté avec succès !");
            clearFields();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur SQL", e.getMessage());
        }
    }

    private boolean validateFields() {
        boolean isValid = true;

        String titreText = titre.getText().trim();
        if (titreText.isEmpty() || titreText.length() < 3) {
            setFieldInvalid(titre);
            showAlert(Alert.AlertType.WARNING, "Validation", "Le titre doit contenir au moins 3 caractères.");
            isValid = false;
        }

        String descText = descr.getText().trim();
        if (descText.isEmpty() || descText.length() < 10) {
            setFieldInvalid(descr);
            showAlert(Alert.AlertType.WARNING, "Validation", "La description doit contenir au moins 10 caractères.");
            isValid = false;
        }

        if (datePub.getValue() != null && datePub.getValue().isBefore(LocalDate.now())) {
            setFieldInvalid(datePub);
            showAlert(Alert.AlertType.WARNING, "Validation", "La date de publication ne peut pas être dans le passé.");
            isValid = false;
        }

        if (typeCombo.getValue() == null) {
            setFieldInvalid(typeCombo);
            showAlert(Alert.AlertType.WARNING, "Validation", "Veuillez sélectionner un type de blog.");
            isValid = false;
        }

        return isValid;
    }

    private void clearFields() {
        titre.clear();
        descr.clear();
        datePub.setValue(null);
        typeCombo.setValue(null);
        resetFieldStyles();
    }

    private void setFieldInvalid(Node field) {
        if (field != null) {
            field.setStyle("-fx-border-color: red; -fx-border-width: 2;");
        }
    }

    private void resetFieldStyles() {
        if (titre != null) titre.setStyle("");
        if (descr != null) descr.setStyle("");
        if (datePub != null) datePub.setStyle("");
        if (typeCombo != null) typeCombo.setStyle("");
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void display(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/FXML/AfficherBlogs.fxml"));
            Stage stage = (Stage)((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Liste des Blogs");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goToCategories(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/GestionTypes.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage)((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Gestion des Types");
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de navigation", e.getMessage());
        }
    }
}