package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import model.Blogs;
import model.Type_b;
import models.CurrentUser;
import service.BlogsService;
import service.IService;
import service.Type_bCRUD;
import java.io.IOException;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

public class ArtistModifierBlogController {
    @FXML private TextField titreField;
    @FXML private TextArea descrField;
    @FXML private ComboBox<Type_b> typeComboBox;
    @FXML private DatePicker datePubPicker;
    private final IService<Blogs> blogService = new BlogsService();
    private final Type_bCRUD typeService = new Type_bCRUD();
    private Blogs currentBlog;

    public void setBlog(Blogs blog) {
        this.currentBlog = blog;
        titreField.setText(blog.getTitre());
        descrField.setText(blog.getDescr());
        typeComboBox.setValue(blog.getType());
        if (blog.getDate_pub() != null) {
            datePubPicker.setValue(blog.getDate_pub().toLocalDate());
        } else {
            datePubPicker.setValue(null);
        }
    }

    @FXML
    public void initialize() {
        try {
            List<Type_b> types = typeService.getAll();
            if (types == null || types.isEmpty()) {
                showError("Aucun type de blog n'est disponible. Veuillez en ajouter d'abord.");
                typeComboBox.setDisable(true);
            } else {
                typeComboBox.getItems().setAll(types);
                typeComboBox.setDisable(false);
            }
        } catch (Exception e) {
            showError("Erreur lors du chargement des types : " + e.getMessage());
            typeComboBox.setDisable(true);
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleUpdateBlog() {
        try {
            int artistId = CurrentUser.getCurrentUserId();
            if (artistId == -1) {
                showError("Utilisateur non authentifié. Veuillez vous reconnecter.");
                return;
            }
            if (currentBlog.getAuteurId() != artistId) return;
            String titre = titreField.getText();
            String descr = descrField.getText();
            LocalDate datePub = datePubPicker.getValue();
            Type_b selectedType = typeComboBox.getValue();
            if (titre == null || titre.trim().isEmpty()) {
                showError("Le titre ne doit pas être vide.");
                return;
            }
            if (descr == null || descr.trim().length() < 10) {
                showError("La description doit avoir au moins 10 caractères.");
                return;
            }
            if (datePub == null) {
                showError("Veuillez sélectionner une date de publication.");
                return;
            }
            if (selectedType == null) {
                showError("Veuillez sélectionner un type de blog.");
                return;
            }
            currentBlog.setTitre(titre);
            currentBlog.setDescr(descr);
            currentBlog.setType(selectedType);
            currentBlog.setDate_pub(Date.valueOf(datePub));
            blogService.update(currentBlog);
            // Redirect to blogs list instead of closing
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ArtistAfficherBlogs.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) titreField.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            showError("Erreur lors de la modification du blog : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void retourAfficherBlogs() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ArtistAfficherBlogs.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) titreField.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
