package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.Blogs;
import models.CurrentUser;
import service.BlogsService;
import service.IService;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class ArtistAfficherBlogsController {
    @FXML private ListView<Blogs> listView;
    private final IService<Blogs> blogService = new BlogsService();

    @FXML
    public void initialize() {
        refreshList();
        setupListView();
    }

    private void refreshList() {
        try {
            List<Blogs> allBlogs = blogService.getAll();
            listView.getItems().setAll(allBlogs);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupListView() {
        listView.setCellFactory(lv -> new ListCell<Blogs>() {
            @Override
            protected void updateItem(Blogs blog, boolean empty) {
                super.updateItem(blog, empty);
                if (empty || blog == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    HBox container = new HBox(18);
                    container.setStyle("-fx-background-color: #fff; -fx-background-radius: 10; -fx-padding: 18 24 18 24; -fx-effect: dropshadow(gaussian, #31406011, 4, 0, 0, 0);");
                    container.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

                    VBox infoContainer = new VBox(4);
                    infoContainer.setPrefWidth(420);
                    Label titleLabel = new Label(blog.getTitre());
                    titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #314060;");
                    Label descLabel = new Label(blog.getDescr());
                    descLabel.setStyle("-fx-font-size: 15px; -fx-text-fill: #505b6d; -fx-wrap-text: true;");
                    Label typeLabel = new Label("Type: " + (blog.getType() != null ? blog.getType().getLibelle() : "Non défini"));
                    typeLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #888;");
                    Label authorLabel = new Label("Auteur: " + blog.getAuteurId());
                    authorLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #888;");
                    Label createdLabel = new Label("Créé le: " + (blog.getDate_crea() != null ? blog.getDate_crea().toString() : "?"));
                    createdLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #888;");
                    Label publishedLabel = new Label("Publié le: " + (blog.getDate_pub() != null ? blog.getDate_pub().toString() : "?"));
                    publishedLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #888;");
                    infoContainer.getChildren().addAll(titleLabel, descLabel, typeLabel, authorLabel, createdLabel, publishedLabel);

                    HBox buttonsBox = new HBox(10);
                    buttonsBox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
                    Button consulterBtn = new Button("Consulter");
                    consulterBtn.setStyle("-fx-background-color: #00b4d8; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8;");
                    consulterBtn.setOnAction(e -> handleConsult(blog));
                    buttonsBox.getChildren().add(consulterBtn);
                    int artistId = CurrentUser.getCurrentUserId();
                    if (blog.getAuteurId() == artistId) {
                        Button editBtn = new Button("Modifier");
                        editBtn.setStyle("-fx-background-color: #ffd166; -fx-text-fill: #314060; -fx-font-weight: bold; -fx-background-radius: 8;");
                        editBtn.setOnAction(e -> handleEdit(blog));
                        Button deleteBtn = new Button("Supprimer");
                        deleteBtn.setStyle("-fx-background-color: #ef476f; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8;");
                        deleteBtn.setOnAction(e -> handleDelete(blog));
                        buttonsBox.getChildren().addAll(editBtn, deleteBtn);
                    }
                    container.getChildren().addAll(infoContainer, buttonsBox);
                    setGraphic(container);
                }
            }
        });
    }

    private void handleEdit(Blogs blog) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ArtistModifierBlog.fxml"));
            Parent root = loader.load();
            ArtistModifierBlogController controller = loader.getController();
            controller.setBlog(blog);
            Stage stage = (Stage) listView.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleDelete(Blogs blog) {
        try {
            blogService.delete(blog.getId());
            refreshList();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void handleConsult(Blogs blog) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ArtistConsulterBlog.fxml"));
            Parent root = loader.load();
            controllers.ArtistConsulterBlogController controller = loader.getController();
            controller.setBlog(blog);
            Stage stage = (Stage) listView.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void retourAjoutBlog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ArtistAjouterBlog.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) listView.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void display() {
        refreshList();
    }

    @FXML
    private void goToTypes() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ArtistGestionTypes.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) listView.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
