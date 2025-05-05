package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.io.IOException;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import model.Blogs;
import model.Type_b;
import models.CurrentUser;
import service.BlogsService;
import service.IService;
import service.Type_bCRUD;

public class ArtistAjouterBlogController {
    @FXML private TextField titreField;
    @FXML private TextArea descrField;
    @FXML private DatePicker datePubField;
    @FXML private ComboBox<Type_b> typeComboBox;
    private final IService<Blogs> blogService = new BlogsService();
    private final Type_bCRUD typeService = new Type_bCRUD();

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
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

    @FXML
    private void handleAddBlog() {
        try {
            int artistId = CurrentUser.getCurrentUserId();
            if (artistId == -1) {
                showError("Utilisateur non authentifié. Veuillez vous reconnecter.");
                return;
            }
            String titre = titreField.getText();
            String descr = descrField.getText();
            LocalDate datePub = datePubField.getValue();
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
            Blogs blog = new Blogs();
            blog.setAuteurId(artistId);
            blog.setTitre(titre);
            blog.setDescr(descr);
            blog.setDate_pub(Date.valueOf(datePub));
            blog.setType(selectedType);
            blogService.add(blog);
            // Redirect to blogs list instead of closing
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ArtistAfficherBlogs.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) titreField.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
        } catch (Exception e) {
            showError("Erreur lors de l'ajout du blog : " + e.getMessage());
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
