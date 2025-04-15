package gui;

import Services.BlogService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import models.Blog;

import java.io.IOException;
import java.sql.Date;
import java.util.Optional;

public class BlogViewController {

    @FXML private TableView<Blog> blogTable;
    @FXML private TableColumn<Blog, String> colTitre;
    @FXML private TableColumn<Blog, String> colDescr;
    @FXML private TableColumn<Blog, Date> colDateCrea;
    @FXML private TableColumn<Blog, Date> colDatePub;
    @FXML private TableColumn<Blog, String> colType;

    private final BlogService service = new BlogService();

    @FXML
    public void initialize() {
        // Configuration des colonnes
        colTitre.setCellValueFactory(new PropertyValueFactory<>("titre"));
        colDescr.setCellValueFactory(new PropertyValueFactory<>("descr"));
        colDateCrea.setCellValueFactory(new PropertyValueFactory<>("date_crea"));
        colDatePub.setCellValueFactory(new PropertyValueFactory<>("date_pub"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));

        loadBlogs();
    }

    private void loadBlogs() {
        ObservableList<Blog> blogList = FXCollections.observableArrayList(service.getAll());
        blogTable.setItems(blogList);
    }

    @FXML
    private void handleEditBlog() throws IOException {
        Blog selectedBlog = blogTable.getSelectionModel().getSelectedItem();
        if (selectedBlog == null) {
            showAlert("Veuillez sélectionner un blog à modifier.");
            return;
        }

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/modify_blog.fxml"));
        Parent root = loader.load();

        ModifyBlogController controller = loader.getController();
        controller.setBlogToEdit(selectedBlog);

        Stage stage = new Stage();
        stage.setTitle("Modifier Blog");
        stage.setScene(new Scene(root));
        stage.show();

        closeCurrentWindow();
    }

    @FXML
    private void handleDeleteBlog() {
        Blog selectedBlog = blogTable.getSelectionModel().getSelectedItem();
        if (selectedBlog == null) {
            showAlert("Veuillez sélectionner un blog à supprimer.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Suppression de blog");
        alert.setContentText("Voulez-vous vraiment supprimer ce blog ?");
        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            service.delete(selectedBlog);
            loadBlogs(); // Rafraîchir la table
        }
    }

    @FXML
    private void goBackToHome() throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/home.fxml"));
        Stage stage = new Stage();
        stage.setTitle("Accueil");
        stage.setScene(new Scene(root));
        stage.show();

        closeCurrentWindow();
    }

    private void closeCurrentWindow() {
        ((Stage) blogTable.getScene().getWindow()).close();
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Attention");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}