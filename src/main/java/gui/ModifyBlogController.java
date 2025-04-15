package gui;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import models.Blog;
import Services.BlogService;

import java.sql.Date;
import java.time.LocalDate;

public class ModifyBlogController {

    @FXML private TextField titleField;
    @FXML private TextArea descriptionArea;
    @FXML private DatePicker creationDatePicker;
    @FXML private DatePicker publicationDatePicker;
    @FXML private ComboBox<String> typeComboBox;

    private Blog blogToEdit;
    private final BlogService service = new BlogService();

    @FXML
    public void initialize() {
        // Initialisation des types de blog disponibles
        typeComboBox.getItems().addAll("Actualité", "Tutoriel", "Opinion", "Revue");
    }

    public void setBlogToEdit(Blog blog) {
        this.blogToEdit = blog;

        // Remplir les champs avec les données existantes
        titleField.setText(blog.getTitre());
        descriptionArea.setText(blog.getDescription());
        creationDatePicker.setValue(blog.getDate_crea().toLocalDate());

        if (blog.getDate_pub() != null) {
            publicationDatePicker.setValue(blog.getDate_pub().toLocalDate());
        }

        typeComboBox.setValue(blog.getType());
    }

    @FXML
    private void updateBlog() {
        if (blogToEdit == null) return;

        // Validation des champs obligatoires
        if (titleField.getText().isEmpty() || descriptionArea.getText().isEmpty()
                || creationDatePicker.getValue() == null || typeComboBox.getValue() == null) {
            showAlert("Erreur", "Veuillez remplir tous les champs obligatoires");
            return;
        }

        // Validation des dates
        if (publicationDatePicker.getValue() != null
                && publicationDatePicker.getValue().isBefore(creationDatePicker.getValue())) {
            showAlert("Erreur", "La date de publication ne peut pas être antérieure à la date de création");
            return;
        }

        // Mettre à jour les données de l'objet Blog
        blogToEdit.setTitre(titleField.getText());
        blogToEdit.setDescription(descriptionArea.getText());
        blogToEdit.setDate_crea(Date.valueOf(creationDatePicker.getValue()));

        if (publicationDatePicker.getValue() != null) {
            blogToEdit.setDate_pub(Date.valueOf(publicationDatePicker.getValue()));
        } else {
            blogToEdit.setDate_pub(null);
        }

        blogToEdit.setType(typeComboBox.getValue());

        // Mise à jour dans la base de données
        service.update(blogToEdit);

        // Fermer la fenêtre
        Stage stage = (Stage) titleField.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}