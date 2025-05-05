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
import model.Comment;
import model.Reaction;
import models.CurrentUser;
import service.CommentCRUD;
import service.ReactionCRUD;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.EnumMap;
import java.util.List;

public class UserConsulterBlogController {
    @FXML private Label blogTitleLabel;
    @FXML private Label blogDescrLabel;
    @FXML private Label blogDateLabel;
    @FXML private Label blogTypeLabel;
    @FXML private TextArea blogContentArea;
    @FXML private VBox commentsContainer;
    @FXML private TextArea commentTextArea;
    @FXML private Label likesCount;
    @FXML private Label lovesCount;
    @FXML private Label sadsCount;
    @FXML private Label laughsCount;
    @FXML private Label angrysCount;
    @FXML private Button likeButton;
    @FXML private Button loveButton;
    @FXML private Button sadButton;
    @FXML private Button laughButton;
    @FXML private Button angryButton;
    private Blogs currentBlog;
    private final CommentCRUD commentCRUD = new CommentCRUD();
    private final ReactionCRUD reactionCRUD = new ReactionCRUD();
    private EnumMap<Reaction.ReactionType, Integer> reactionCounts = new EnumMap<>(Reaction.ReactionType.class);
    private Reaction.ReactionType currentUserReaction = null;
    private final int currentUserId = CurrentUser.getCurrentUserId();
    private Comment editingComment = null;

    public void setBlog(Blogs blog) {
        this.currentBlog = blog;
        blogTitleLabel.setText(blog.getTitre());
        blogDescrLabel.setText(blog.getDescr());
        blogDateLabel.setText(blog.getDatePub() != null ? new SimpleDateFormat("dd/MM/yyyy").format(blog.getDatePub()) : "");
        blogTypeLabel.setText(blog.getType() != null ? blog.getType().getLibelle() : "");
        blogContentArea.setText(blog.getDescr());
        updateReactionCounts();
        loadComments();
    }

    private void updateReactionCounts() {
        if (currentBlog == null) return;
        try {
            reactionCounts.clear();
            for (Reaction.ReactionType type : Reaction.ReactionType.values()) {
                reactionCounts.put(type, 0);
            }
            List<Reaction> reactions = reactionCRUD.getReactionsByBlogId(currentBlog.getId());
            for (Reaction reaction : reactions) {
                reactionCounts.merge(reaction.getType(), 1, Integer::sum);
            }
            Reaction userReaction = reactionCRUD.getUserReaction(currentBlog.getId(), currentUserId);
            currentUserReaction = userReaction != null ? userReaction.getType() : null;
            updateReactionUI();
        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour des réactions : " + e.getMessage());
        }
    }

    private void updateReactionUI() {
        likesCount.setText(reactionCounts.get(Reaction.ReactionType.LIKE) + " J'aime");
        lovesCount.setText(reactionCounts.get(Reaction.ReactionType.LOVE) + " J'adore");
        sadsCount.setText(reactionCounts.get(Reaction.ReactionType.SAD) + " Triste");
        laughsCount.setText(reactionCounts.get(Reaction.ReactionType.LAUGH) + " Rire");
        angrysCount.setText(reactionCounts.get(Reaction.ReactionType.ANGRY) + " Fâché");
    }

    @FXML
    private void handleReaction(javafx.event.ActionEvent event) {
        if (currentBlog == null) return;
        Button source = (Button) event.getSource();
        String reactionTypeStr = (String) source.getUserData();
        try {
            Reaction.ReactionType newReactionType = Reaction.ReactionType.valueOf(reactionTypeStr);
            Reaction reaction = new Reaction();
            reaction.setBlogId(currentBlog.getId());
            reaction.setUserId(currentUserId);
            reaction.setType(newReactionType);
            reactionCRUD.addOrUpdateReaction(reaction);
            currentUserReaction = newReactionType;
            updateReactionCounts();
        } catch (SQLException e) {
            System.err.println("Erreur lors de la gestion de la réaction : " + e.getMessage());
        }
    }

    private void loadComments() {
        commentsContainer.getChildren().clear();
        if (currentBlog == null) return;
        try {
            List<Comment> comments = commentCRUD.getCommentsByBlogId(currentBlog.getId());
            for (Comment comment : comments) {
                VBox commentBox = new VBox(5);
                commentBox.setStyle("-fx-padding: 10; -fx-background-color: #f0f0f0; -fx-background-radius: 5;");
                HBox contentBox = new HBox(10);
                Label content = new Label(comment.getContenu());
                content.setWrapText(true);
                content.setStyle("-fx-font-size: 14px;");
                contentBox.getChildren().add(content);
                commentBox.getChildren().add(contentBox);
                Label date = new Label(new SimpleDateFormat("dd/MM/yyyy HH:mm").format(comment.getDateCreation()));
                date.setStyle("-fx-font-size: 10px; -fx-text-fill: #666;");
                HBox buttons = new HBox(8);
                if (comment.getUserId() == currentUserId) {
                    Button editBtn = new Button("Modifier");
                    editBtn.setOnAction(e -> startEditComment(comment));
                    Button delBtn = new Button("Supprimer");
                    delBtn.setOnAction(e -> deleteComment(comment));
                    buttons.getChildren().addAll(editBtn, delBtn);
                }
                commentBox.getChildren().addAll(date, buttons);
                commentsContainer.getChildren().add(commentBox);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors du chargement des commentaires : " + e.getMessage());
        }
    }

    private void startEditComment(Comment comment) {
        editingComment = comment;
        commentTextArea.setText(comment.getContenu());
        commentTextArea.requestFocus();
    }

    @FXML
    private void handleAddComment() {
        String content = commentTextArea.getText().trim();
        if (content.isEmpty() || currentBlog == null) return;
        try {
            if (editingComment != null) {
                editingComment.setContenu(content);
                commentCRUD.update(editingComment);
                editingComment = null;
            } else {
                Comment comment = new Comment(content, currentBlog.getId(), currentUserId);
                commentCRUD.add(comment);
            }
            commentTextArea.clear();
            loadComments();
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'ajout/modification du commentaire : " + e.getMessage());
        }
    }

    @FXML
    private void handleRetour() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/UserAfficherBlogs.fxml"));
            Stage stage = (Stage) blogTitleLabel.getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void deleteComment(Comment comment) {
        try {
            commentCRUD.delete(comment.getId());
            loadComments();
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression du commentaire : " + e.getMessage());
        }
    }
}
