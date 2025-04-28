package controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import model.Comment;
import service.CommentCRUD;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

public class CommentController implements Initializable {
    @FXML
    private VBox commentsContainer;
    @FXML
    private TextArea commentTextArea;

    private CommentCRUD commentCRUD;
    private int currentBlogId;
    private int currentUserId;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        commentCRUD = new CommentCRUD();
    }

    public void setBlogId(int blogId) {
        this.currentBlogId = blogId;
        loadComments();
    }

    public void setUserId(int userId) {
        this.currentUserId = userId;
    }

    private void loadComments() {
        try {
            List<Comment> comments = commentCRUD.getCommentsByBlogId(currentBlogId);
            commentsContainer.getChildren().clear();

            for (Comment comment : comments) {
                VBox commentBox = new VBox(5);
                commentBox.setStyle("-fx-padding: 10; -fx-background-color: #f0f0f0; -fx-background-radius: 5;");

                Text content = new Text(comment.getContenu());
                content.setWrappingWidth(400);

                Text date = new Text(comment.getDateCreation().toString());
                date.setStyle("-fx-font-size: 10px; -fx-fill: #666;");

                commentBox.getChildren().addAll(content, date);
                commentsContainer.getChildren().add(commentBox);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors du chargement des comments : " + e.getMessage());
        }
    }

    @FXML
    private void handleAddComment() {
        String content = commentTextArea.getText().trim();
        if (!content.isEmpty()) {
            try {
                Comment comment = new Comment(content, currentBlogId, currentUserId);
                commentCRUD.add(comment);
                commentTextArea.clear();
                loadComments();
            } catch (SQLException e) {
                System.err.println("Erreur lors de l'ajout du comment : " + e.getMessage());
            }
        }
    }
}