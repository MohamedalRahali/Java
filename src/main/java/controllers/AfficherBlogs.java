package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
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
import javafx.scene.Node;
import org.kordamp.ikonli.javafx.FontIcon;
import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;

public class AfficherBlogs {

    @FXML private ListView<Blogs> listView;
    private final IService<Blogs> blogService = new BlogsService();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

    @FXML
    public void initialize() {
        refreshList();
        setupListView();
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
                    HBox container = new HBox(10);
                    container.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

                    VBox infoContainer = new VBox(5);

                    Label titleLabel = new Label(blog.getTitre());
                    titleLabel.getStyleClass().add("blog-title");

                    Label descLabel = new Label(blog.getDescr());
                    descLabel.getStyleClass().add("blog-description");

                    HBox datesBox = new HBox(10);
                    datesBox.getStyleClass().add("blog-metadata");
                    
                    Label createdLabel = new Label("Créé le: " + dateFormat.format(blog.getDate_crea()));
                    Label publishedLabel = new Label("Publié le: " + dateFormat.format(blog.getDate_pub()));
                    datesBox.getChildren().addAll(createdLabel, publishedLabel);
                    
                    infoContainer.getChildren().addAll(titleLabel, descLabel, datesBox);

                    HBox buttonsBox = new HBox(5);
                    buttonsBox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
                    HBox.setHgrow(buttonsBox, javafx.scene.layout.Priority.ALWAYS);

                    Button consultBtn = createButton("Consulter", "fas-eye", "consult-button", event -> handleConsult(blog));

                    Button editBtn = createButton("Modifier", "fas-edit", "edit-button", event -> handleEdit(blog));

                    Button deleteBtn = createButton("Supprimer", "fas-trash", "delete-button", event -> handleDelete(blog));

                    buttonsBox.getChildren().addAll(consultBtn, editBtn, deleteBtn);
                    
                    container.getChildren().addAll(infoContainer, buttonsBox);
                    setGraphic(container);
                }
            }
        });
    }

    private Button createButton(String text, String iconLiteral, String styleClass, javafx.event.EventHandler<ActionEvent> handler) {
        Button button = new Button();
        button.getStyleClass().add(styleClass);
        
        HBox content = new HBox(5);
        content.setAlignment(javafx.geometry.Pos.CENTER);
        
        FontIcon icon = new FontIcon(iconLiteral);
        Label label = new Label(text);
        label.setStyle("-fx-text-fill: white;");
        
        content.getChildren().addAll(icon, label);
        button.setGraphic(content);
        button.setOnAction(handler);
        
        return button;
    }

    private void handleConsult(Blogs blog) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/ConsulterBlog.fxml"));
            Parent root = loader.load();
            
            ConsulterBlogController controller = loader.getController();
            controller.setBlog(blog);
            // Dynamically set the user ID using CurrentUser
            int userId = CurrentUser.getCurrentUserId();
            controller.setUserId(userId);
            
            Stage stage = (Stage) listView.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Consultation du Blog");
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de consultation", "Erreur lors de l'ouverture de la consultation : " + e.getMessage());
        }
    }

    private void handleEdit(Blogs blog) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/ModifierBlog.fxml"));
            Parent root = loader.load();
            
            ModifierBlog controller = loader.getController();
            controller.setBlog(blog, listView.getItems());
            
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Modifier le Blog");
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de navigation", e.getMessage());
        }
    }

    private void handleDelete(Blogs blog) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation de suppression");
        confirmation.setHeaderText("Êtes-vous sûr de vouloir supprimer ce blog ?");
        confirmation.setContentText("Cette action est irréversible.");

        if (confirmation.showAndWait().get() == ButtonType.OK) {
            try {
                blogService.delete(blog.getId());
                refreshList();
                showAlert(Alert.AlertType.INFORMATION, "Suppression réussie", "Le blog a été supprimé avec succès.");
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur de suppression", e.getMessage());
            }
        }
    }

    private void refreshList() {
        try {
            ObservableList<Blogs> blogs = FXCollections.observableArrayList(blogService.display());
            listView.setItems(blogs);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de chargement", "Impossible de charger la liste des blogs : " + e.getMessage());
        }
    }

    @FXML
    private void retourAjoutBlog(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/FXML/AjouterBlog.fxml"));
            Stage stage = (Stage)((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Ajouter un Blog");
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de navigation", "Impossible d'ouvrir la page d'ajout de blog : " + e.getMessage());
        }
    }

    @FXML
    private void display(ActionEvent event) {
        refreshList();
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
            showAlert(Alert.AlertType.ERROR, "Erreur de navigation", "Impossible d'ouvrir la page de gestion des types : " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(type == Alert.AlertType.ERROR ? "Erreur" : "Information");
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}