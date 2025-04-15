package gui;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import models.Blog;
import Services.BlogService;

import java.sql.Date;
import java.time.LocalDate;

public class CreateBlogController {

    @FXML private TextField titreField;
    @FXML private TextArea descrArea;
    @FXML private DatePicker datePubPicker;
    @FXML private TextField typeField;

    private final BlogService service = new BlogService();

    @FXML
    private void createBlog() {
        try {
            // Set creation date to current date
            Date dateCrea = Date.valueOf(LocalDate.now());

            // Get publication date from the picker (nullable)
            Date datePub = datePubPicker.getValue() != null ? Date.valueOf(datePubPicker.getValue()) : null;

            Blog blog = new Blog(
                    titreField.getText(),
                    descrArea.getText(),
                    dateCrea,
                    datePub,
                    typeField.getText()
            );

            service.add(blog);
            showAlert("Success", "Blog created successfully!");

            // Optionally, clear the form fields after creation
            clearForm();

        } catch (Exception e) {
            showAlert("Error", e.getMessage());
        }
    }

    private void clearForm() {
        titreField.clear();
        descrArea.clear();
        datePubPicker.setValue(null);
        typeField.clear();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
