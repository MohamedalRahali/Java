package gui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import models.Blog;
import Services.BlogService;

import java.io.IOException;
import java.sql.Date;
import java.time.LocalDate;

public class ModificationBlogController {

    @FXML private TextField tfTitre;
    @FXML private TextArea taDescription;
    @FXML private DatePicker dpDateCreation;
    @FXML private DatePicker dpDatePublication;
    @FXML private ComboBox<String> cbType;

    private Blog blogToModify;
    private final BlogService service = new BlogService();

    @FXML
    public void initialize() {
        // Initialiser les types de blog disponibles
        cbType.getItems().addAll("Actualité", "Tutoriel", "Opinion", "Revue");
    }

    public void initData(Blog blog) {
        this.blogToModify = blog;

        // Remplir les champs avec les données du blog
        tfTitre.setText(blog.getTitre());
        taDescription.setText(blog.getDescription());
        dpDateCreation.setValue(blog.getDate_crea().toLocalDate());

        if (blog.getDate_pub() != null) {
            dpDatePublication.setValue(blog.getDate_pub().toLocalDate());
        }

        cbType.setValue(blog.getType());
    }

    @FXML
    private void sauvegarderModification() {
        try {
            // Validation des champs obligatoires
            if (tfTitre.getText().isEmpty() || taDescription.getText().isEmpty()
                    || dpDateCreation.getValue() == null || cbType.getValue() == null) {
                showAlert("Erreur", "Veuillez remplir tous les champs obligatoires!");
                return;
            }

            // Validation des dates
            if (dpDatePublication.getValue() != null
                    && dpDatePublication.getValue().isBefore(dpDateCreation.getValue())) {
                showAlert("Erreur", "La date de publication ne peut pas être antérieure à la date de création!");
                return;
            }

            // Mise à jour de l'objet Blog
            blogToModify.setTitre(tfTitre.getText());
            blogToModify.setDescription(taDescription.getText());
            blogToModify.setDate_crea(Date.valueOf(dpDateCreation.getValue()));

            if (dpDatePublication.getValue() != null) {
                blogToModify.setDate_pub(Date.valueOf(dpDatePublication.getValue()));
            } else {
                blogToModify.setDate_pub(null);
            }

            blogToModify.setType(cbType.getValue());

            // Sauvegarde dans la base de données
            service.update(blogToModify);

            showAlert(Alert.AlertType.INFORMATION, "Succès", "Le blog a été modifié avec succès!");
            returnToMainScreen();

        } catch (Exception e) {
            showAlert("Erreur", "Une erreur est survenue: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void annulerModification() {
        returnToMainScreen();
    }

    private void returnToMainScreen() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/interface_blog.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) tfTitre.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showAlert("Erreur", "Impossible de charger l'écran principal: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}