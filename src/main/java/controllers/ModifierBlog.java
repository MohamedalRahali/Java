package controllers;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ComboBox;
import javafx.stage.Stage;
import model.Blogs;
import service.BlogsService;
import service.IService;
import model.Type_b;
import service.Type_bCRUD;

import java.sql.Date;
import java.time.LocalDate;
import java.sql.SQLException;
import java.util.List;
import javafx.collections.FXCollections;

public class ModifierBlog {

    @FXML
    private TextField titreField;
    @FXML
    private TextArea descriptionField;
    @FXML
    private DatePicker datePubPicker;
    @FXML
    private Label dateCreationLabel;
    @FXML
    private ComboBox<Type_b> typeComboBox;

    private Blogs blogToModify;
    private ObservableList<Blogs> blogsList;
    private final BlogsService blogService = new BlogsService();
    private final Type_bCRUD typeService = new Type_bCRUD();

    @FXML
    public void initialize() {
        // Vérifier que tous les composants FXML sont correctement injectés
        assert titreField != null : "fx:id=\"titreField\" n'a pas été injecté";
        assert descriptionField != null : "fx:id=\"descriptionField\" n'a pas été injecté";
        assert datePubPicker != null : "fx:id=\"datePubPicker\" n'a pas été injecté";
        assert dateCreationLabel != null : "fx:id=\"dateCreationLabel\" n'a pas été injecté";
        assert typeComboBox != null : "fx:id=\"typeComboBox\" n'a pas été injecté";

        try {
            // Charger les types dans le ComboBox
            List<Type_b> types = typeService.getAll();
            typeComboBox.setItems(FXCollections.observableArrayList(types));
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur de chargement des types", e.getMessage());
        }
    }

    public void setBlog(Blogs blog, ObservableList<Blogs> blogsList) {
        this.blogToModify = blog;
        this.blogsList = blogsList;
        
        if (blog != null) {
            titreField.setText(blog.getTitre());
            descriptionField.setText(blog.getDescr());
            if (blog.getDate_pub() != null) {
                datePubPicker.setValue(blog.getDate_pub().toLocalDate());
            }
            if (blog.getType() != null) {
                typeComboBox.setValue(blog.getType());
            }
            if (blog.getDate_crea() != null) {
                dateCreationLabel.setText("Créé le: " + blog.getDate_crea().toString());
            }
        }
    }

    @FXML
    public void saveChanges() {
        if (!validateFields()) {
            return;
        }

        try {
            String nouveauTitre = titreField.getText().trim();
            String nouvelleDescription = descriptionField.getText().trim();
            Date nouvelleDatePub = datePubPicker.getValue() != null ?
                    Date.valueOf(datePubPicker.getValue()) : null;
            Type_b nouveauType = typeComboBox.getValue();

            blogToModify.setTitre(nouveauTitre);
            blogToModify.setDescr(nouvelleDescription);
            blogToModify.setDate_pub(nouvelleDatePub);
            blogToModify.setType(nouveauType);

            blogService.update(blogToModify);

            int index = blogsList.indexOf(blogToModify);
            if (index >= 0) {
                blogsList.set(index, blogToModify);
            }

            titreField.getScene().getWindow().hide();

        } catch (Exception e) {
            showAlert("Erreur", "Échec de la mise à jour", e.getMessage());
        }
    }

    private boolean validateFields() {
        if (titreField.getText().trim().length() < 3) {
            showAlert("Validation", "Titre trop court", "Le titre doit contenir au moins 3 caractères.");
            return false;
        }

        if (descriptionField.getText().trim().length() < 10) {
            showAlert("Validation", "Description trop courte", "La description doit contenir au moins 10 caractères.");
            return false;
        }

        if (typeComboBox.getValue() == null) {
            showAlert("Validation", "Type manquant", "Veuillez sélectionner un type.");
            return false;
        }

        return true;
    }

    private void showAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}