package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import model.Comment;
import models.CurrentUser;
import service.CommentCRUD;
import java.sql.SQLException;

public class ArtistCommentController {
    @FXML private ListView<Comment> commentListView;
    @FXML private TextArea commentTextArea;
    private final CommentCRUD commentCRUD = new CommentCRUD();
    private int currentBlogId;

    public void setBlogId(int blogId) {
        this.currentBlogId = blogId;
        loadComments();
    }

    private void loadComments() {
        try {
            int artistId = CurrentUser.getCurrentUserId();
            commentListView.getItems().setAll(commentCRUD.getCommentsByBlogAndUser(currentBlogId, artistId));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAddComment() {
        String content = commentTextArea.getText().trim();
        int artistId = CurrentUser.getCurrentUserId();
        if (!content.isEmpty()) {
            try {
                Comment comment = new Comment(content, currentBlogId, artistId);
                commentCRUD.add(comment);
                commentTextArea.clear();
                loadComments();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
