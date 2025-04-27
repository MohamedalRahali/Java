package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Callback;
import model.Blogs;
import service.BlogsService;
import service.IService;

import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.List;

public class AfficherBlogs {

    @FXML
    private ListView<Blogs> listView;

    private final IService<Blogs> blogService = new BlogsService();

    @FXML
    private void retourAjoutBlog(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/FXML/AjouterBlog.fxml"));
            Stage stage = (Stage)((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Ajouter un Blog");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void initialize() {
        try {
            List<Blogs> blogs = blogService.display();
            ObservableList<Blogs> observableList = FXCollections.observableList(blogs);
            listView.setItems(observableList);

            listView.setCellFactory(new Callback<>() {
                @Override
                public ListCell<Blogs> call(ListView<Blogs> param) {
                    return new ListCell<>() {
                        @Override
                        protected void updateItem(Blogs blog, boolean empty) {
                            super.updateItem(blog, empty);
                            if (empty || blog == null) {
                                setText(null);
                                setGraphic(null);
                            } else {
                                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

                                Text text = new Text(
                                        "Titre: " + blog.getTitre() + "\n" +
                                                "Description: " + blog.getDescr() + "\n" +
                                                "Créé le: " + dateFormat.format(blog.getDate_crea()) + "\n" +
                                                (blog.getDate_pub() != null ?
                                                        "Publié le: " + dateFormat.format(blog.getDate_pub()) :
                                                        "Non publié")
                                );
                                text.setWrappingWidth(400);

                                Button btnSupprimer = new Button("Supprimer");
                                btnSupprimer.setStyle("-fx-background-color: #ff4444; -fx-text-fill: white;");
                                btnSupprimer.setOnAction(e -> {
                                    try {
                                        blogService.delete(blog.getId());
                                        listView.getItems().remove(blog);
                                    } catch (SQLException ex) {
                                        showAlert("Erreur", "Échec de la suppression", ex.getMessage());
                                    }
                                });

                                Button btnModifier = new Button("Modifier");
                                btnModifier.setStyle("-fx-background-color: #4285F4; -fx-text-fill: white;");
                                btnModifier.setOnAction(e -> openModifierBlog(blog, observableList));

                                HBox box = new HBox(10, text, btnModifier, btnSupprimer);
                                box.setPadding(new Insets(5));
                                setGraphic(box);
                            }
                        }
                    };
                }
            });

        } catch (SQLException e) {
            showAlert("Erreur", "Problème de chargement des blogs", e.getMessage());
        }
    }

    private void openModifierBlog(Blogs blog, ObservableList<Blogs> blogsList) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/ModifierBlog.fxml"));
            AnchorPane root = loader.load();
            
            ModifierBlog controller = loader.getController();
            controller.setBlog(blog, blogsList);
            
            Stage stage = new Stage();
            stage.setTitle("Modifier Blog");
            stage.setScene(new Scene(root));
            stage.show();
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir l'éditeur", 
                     "Détails : " + e.getMessage());
        }
    }

    private void showAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void display(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, "Action display exécutée !");
        alert.showAndWait();
    }

    @FXML
    private void goToTypes(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/FXML/GestionTypes.fxml"));
            Stage stage = (Stage)((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Gestion des Types");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}