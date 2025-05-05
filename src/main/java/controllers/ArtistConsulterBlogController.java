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
import service.BlogsService;
import service.CommentCRUD;
import service.ReactionCRUD;
import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.EnumMap;
import java.util.List;

public class ArtistConsulterBlogController {
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
    private final BlogsService blogService = new BlogsService();
    private final CommentCRUD commentCRUD = new CommentCRUD();
    private final ReactionCRUD reactionCRUD = new ReactionCRUD();
    private EnumMap<Reaction.ReactionType, Integer> reactionCounts = new EnumMap<>(Reaction.ReactionType.class);
    private Reaction.ReactionType currentUserReaction = null;
    private final int currentUserId = CurrentUser.getCurrentUserId();

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
        updateButtonStyle(likeButton, Reaction.ReactionType.LIKE);
        updateButtonStyle(loveButton, Reaction.ReactionType.LOVE);
        updateButtonStyle(sadButton, Reaction.ReactionType.SAD);
        updateButtonStyle(laughButton, Reaction.ReactionType.LAUGH);
        updateButtonStyle(angryButton, Reaction.ReactionType.ANGRY);
    }

    private void updateButtonStyle(Button button, Reaction.ReactionType type) {
        if (currentUserReaction == type) {
            button.setStyle(button.getStyle() + "; -fx-border-color: black; -fx-border-width: 2;");
        } else {
            button.setStyle(button.getStyle().replace("; -fx-border-color: black; -fx-border-width: 2;", ""));
        }
    }

    @FXML
    private void handleReaction(javafx.event.ActionEvent event) {
        if (currentBlog == null) return;
        Button clickedButton = (Button) event.getSource();
        Reaction.ReactionType newReactionType = Reaction.ReactionType.valueOf(clickedButton.getUserData().toString());
        try {
            if (currentUserReaction == newReactionType) {
                reactionCRUD.removeReaction(currentBlog.getId(), currentUserId);
                currentUserReaction = null;
            } else {
                Reaction reaction = new Reaction(currentBlog.getId(), currentUserId, newReactionType);
                reactionCRUD.addOrUpdateReaction(reaction);
                currentUserReaction = newReactionType;
            }
            updateReactionCounts();
        } catch (SQLException e) {
            System.err.println("Erreur lors de la gestion de la réaction : " + e.getMessage());
        }
    }

    @FXML
    private void handleShareFacebook() {
        if (currentBlog != null) {
            String type = currentBlog.getType() != null ? currentBlog.getType().getLibelle() : null;
            utils.FacebookService.shareBlog(
                currentBlog.getTitre(),
                currentBlog.getDescr(),
                type
            );
        }
    }

    private void loadComments() {
        try {
            List<Comment> comments = commentCRUD.getCommentsByBlogId(currentBlog.getId());
            commentsContainer.getChildren().clear();
            for (Comment comment : comments) {
                VBox commentBox = new VBox(5);
                String backgroundColor = comment.isReported() ? "#FFE4E1" : "#f0f0f0";
                commentBox.setStyle("-fx-padding: 10; -fx-background-color: " + backgroundColor + "; -fx-background-radius: 5;");
                HBox contentBox = new HBox(10);
                Label content = new Label(comment.getContenu());
                content.setWrapText(true);
                content.setStyle("-fx-font-size: 14px;");
                contentBox.getChildren().add(content);
                if (comment.isReported()) {
                    Label warning = new Label("Ce commentaire a été signalé.");
                    warning.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 12px; -fx-font-weight: bold;");
                    contentBox.getChildren().add(warning);
                }
                commentBox.getChildren().add(contentBox);
                Label date = new Label(new SimpleDateFormat("dd/MM/yyyy HH:mm").format(comment.getDateCreation()));
                date.setStyle("-fx-font-size: 10px; -fx-text-fill: #666;");
                HBox buttons = new HBox(8);
                if (comment.getUserId() == currentUserId) {
                    Button editBtn = new Button("Modifier");
                    editBtn.setOnAction(e -> handleEditComment(comment));
                    Button delBtn = new Button("Supprimer");
                    delBtn.setOnAction(e -> handleDeleteComment(comment));
                    buttons.getChildren().addAll(editBtn, delBtn);
                }
                Button reportBtn = new Button("Signaler");
                reportBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-padding: 5 10;");
                reportBtn.setOnAction(e -> handleReportComment(comment));
                buttons.getChildren().add(reportBtn);
                commentBox.getChildren().addAll(date, buttons);
                commentsContainer.getChildren().add(commentBox);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAddComment() {
        String content = commentTextArea.getText().trim();
        if (!content.isEmpty()) {
            try {
                Comment comment = new Comment(content, currentBlog.getId(), currentUserId);
                commentCRUD.add(comment);
                commentTextArea.clear();
                loadComments();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleEditComment(Comment comment) {
        TextInputDialog dialog = new TextInputDialog(comment.getContenu());
        dialog.setTitle("Modifier le commentaire");
        dialog.setHeaderText(null);
        dialog.setContentText("Nouveau contenu:");
        dialog.showAndWait().ifPresent(newContent -> {
            if (!newContent.trim().isEmpty()) {
                try {
                    comment.setContenu(newContent.trim());
                    commentCRUD.update(comment);
                    loadComments();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void handleDeleteComment(Comment comment) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Supprimer ce commentaire?", ButtonType.YES, ButtonType.NO);
        alert.setTitle("Confirmation");
        alert.setHeaderText(null);
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    commentCRUD.delete(comment.getId());
                    loadComments();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void handleReportComment(Comment comment) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Signaler ce commentaire?", ButtonType.YES, ButtonType.NO);
        alert.setTitle("Signaler un commentaire");
        alert.setHeaderText(null);
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    if (commentCRUD.hasUserReported(comment.getId(), currentUserId)) {
                        Alert alreadyReportedAlert = new Alert(Alert.AlertType.WARNING, "Vous avez déjà signalé ce commentaire.", ButtonType.OK);
                        alreadyReportedAlert.setTitle("Déjà signalé");
                        alreadyReportedAlert.setHeaderText(null);
                        alreadyReportedAlert.showAndWait();
                        return;
                    }
                    boolean success = commentCRUD.reportCommentByUser(comment.getId(), currentUserId);
                    if (success) {
                        Alert successAlert = new Alert(Alert.AlertType.INFORMATION, "Commentaire signalé avec succès !", ButtonType.OK);
                        successAlert.setTitle("Signalement réussi");
                        successAlert.setHeaderText(null);
                        successAlert.showAndWait();
                    }
                    loadComments();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @FXML
    private void handleRetour() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ArtistAfficherBlogs.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) blogTitleLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
