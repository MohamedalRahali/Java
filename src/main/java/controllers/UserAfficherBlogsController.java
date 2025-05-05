package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import model.Blogs;
import service.BlogsService;
import java.io.IOException;
import java.util.List;

public class UserAfficherBlogsController {
    @FXML
    private ListView<Blogs> listView;

    @FXML
    public void initialize() {
        // Load all blogs (no add/delete/update for users)
        BlogsService blogsService = new BlogsService();
        List<Blogs> blogs;
        try {
            blogs = blogsService.getAll();
        } catch (Exception e) {
            blogs = java.util.Collections.emptyList();
            e.printStackTrace();
        }
        listView.getItems().setAll(blogs);
        listView.setCellFactory(lv -> new ListCell<Blogs>() {
            @Override
            protected void updateItem(Blogs blog, boolean empty) {
                super.updateItem(blog, empty);
                if (empty || blog == null) {
                    setText(null);
                } else {
                    setStyle("-fx-padding: 12 8; -fx-font-size: 15px; -fx-background-color: #f8fafc;");
                    setText(blog.getTitre() + "\n" +
                            "Auteur: " + blog.getAuteurId() +
                            " | Type: " + (blog.getType() != null ? blog.getType().getLibelle() : "-") +
                            "\nCréé le: " + (blog.getDate_pub() != null ? blog.getDate_pub().toString() : "-") +
                            (blog.getDescr() != null && !blog.getDescr().isEmpty() ? "\nRésumé: " + blog.getDescr() : ""));
                }
            }
        });
        listView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                handleConsulterBlog();
            }
        });
    }

    @FXML
    private void handleConsulterBlog() {
        Blogs selected = listView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/UserConsulterBlog.fxml"));
                Parent root = loader.load();
                UserConsulterBlogController controller = loader.getController();
                controller.setBlog(selected);
                Stage stage = (Stage) listView.getScene().getWindow();
                stage.setScene(new Scene(root));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
