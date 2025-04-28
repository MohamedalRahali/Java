package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.Blogs;
import model.Comment;
import model.Reaction;
import service.BlogsService;
import service.CommentCRUD;
import service.ReactionCRUD;
import utils.BadWordsDetector;
import org.kordamp.ikonli.javafx.FontIcon;
import javafx.geometry.Pos;

import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class ConsulterBlogController {
    @FXML
    private Label titreLabel;
    @FXML
    private Label dateLabel;
    @FXML
    private Label typeLabel;
    @FXML
    private TextArea descriptionArea;
    @FXML
    private VBox commentsContainer;
    @FXML
    private TextArea commentTextArea;
    @FXML
    private Label likesCount;
    @FXML
    private Label lovesCount;
    @FXML
    private Label sadsCount;
    @FXML
    private Label laughsCount;
    @FXML
    private Label angrysCount;
    @FXML
    private javafx.scene.control.Button likeButton;
    @FXML
    private javafx.scene.control.Button loveButton;
    @FXML
    private javafx.scene.control.Button sadButton;
    @FXML
    private javafx.scene.control.Button laughButton;
    @FXML
    private javafx.scene.control.Button angryButton;

    private Blogs currentBlog;
    private final BlogsService blogService = new BlogsService();
    private final CommentCRUD commentCRUD = new CommentCRUD();
    private final ReactionCRUD reactionCRUD = new ReactionCRUD();
    private int currentUserId;
    private Map<Reaction.ReactionType, Integer> reactionCounts = new EnumMap<>(Reaction.ReactionType.class);
    private Reaction.ReactionType currentUserReaction = null;

    private static final String FB_APP_ID = "709707414831957";
    private static final String FB_REDIRECT_URI = "https://votre-domaine.com/callback";
    private static final String FB_GRAPH_API_URL = "https://graph.facebook.com/v18.0";

    public void setBlog(Blogs blog) {
        this.currentBlog = blog;
        updateReactionCounts();
        updateUI();
    }

    public void setUserId(int userId) {
        this.currentUserId = userId;
    }

    @FXML
    private void initialize() {
        updateReactionCounts();
    }

    private void updateUI() {
        if (currentBlog != null) {
            titreLabel.setText(currentBlog.getTitre());
            descriptionArea.setText(currentBlog.getDescr());

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            String dateText = "Créé le: " + dateFormat.format(currentBlog.getDate_crea());
            if (currentBlog.getDate_pub() != null) {
                dateText += " | Publié le: " + dateFormat.format(currentBlog.getDate_pub());
            }
            dateLabel.setText(dateText);

            if (currentBlog.getType() != null) {
                typeLabel.setText("Type: " + currentBlog.getType().getLibelle());
            }

            loadComments();
        }
    }

    private void loadComments() {
        try {
            List<Comment> comments = commentCRUD.getCommentsByBlogId(currentBlog.getId());
            commentsContainer.getChildren().clear();

            for (Comment comment : comments) {
                VBox commentBox = new VBox(5);
                String backgroundColor = BadWordsDetector.containsBadWords(comment.getContenu()) 
                    ? "#FFE4E1"
                    : "#f0f0f0";
                commentBox.setStyle("-fx-padding: 10; -fx-background-color: " + backgroundColor + "; -fx-background-radius: 5;");

                javafx.scene.layout.HBox contentBox = new javafx.scene.layout.HBox(10);

                String displayContent = BadWordsDetector.containsBadWords(comment.getContenu())
                    ? BadWordsDetector.censorBadWords(comment.getContenu())
                    : comment.getContenu();
                
                javafx.scene.text.Text content = new javafx.scene.text.Text(displayContent);
                content.setWrappingWidth(380);

                if (comment.isReported()) {
                    String reportText = "✓ (" + comment.getReportCount() + "/3)";
                    javafx.scene.text.Text reportedIcon = new javafx.scene.text.Text(reportText);

                    String textColor;
                    if (comment.getReportCount() == 2) {
                        textColor = "#FF4500";
                    } else {
                        textColor = "#e74c3c";
                    }
                    reportedIcon.setStyle("-fx-fill: " + textColor + "; -fx-font-size: 14px;");
                    
                    contentBox.getChildren().addAll(content, reportedIcon);

                    if (BadWordsDetector.containsBadWords(comment.getContenu())) {
                        javafx.scene.text.Text warningIcon = new javafx.scene.text.Text(" ⚠");
                        warningIcon.setStyle("-fx-fill: #FFD700; -fx-font-size: 14px;");
                        contentBox.getChildren().add(warningIcon);
                    }
                } else {
                    contentBox.getChildren().add(content);
                }

                javafx.scene.text.Text date = new javafx.scene.text.Text(
                    new SimpleDateFormat("dd/MM/yyyy HH:mm").format(comment.getDateCreation())
                );
                date.setStyle("-fx-font-size: 10px; -fx-fill: #666;");

                javafx.scene.layout.HBox buttonsBox = new javafx.scene.layout.HBox(10);
                buttonsBox.setStyle("-fx-padding: 5 0 0 0;");

                javafx.scene.control.Button editButton = new javafx.scene.control.Button();
                editButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-padding: 5 10;");
                javafx.scene.layout.HBox editContent = new javafx.scene.layout.HBox(5);
                editContent.setAlignment(javafx.geometry.Pos.CENTER);
                FontIcon editIcon = new FontIcon("fas-edit");
                editIcon.setIconSize(14);
                editIcon.setIconColor(javafx.scene.paint.Color.WHITE);
                javafx.scene.control.Label editLabel = new javafx.scene.control.Label("Modifier");
                editLabel.setStyle("-fx-text-fill: white;");
                editContent.getChildren().addAll(editIcon, editLabel);
                editButton.setGraphic(editContent);
                editButton.setOnAction(e -> handleEditComment(comment, commentBox));

                javafx.scene.control.Button deleteButton = new javafx.scene.control.Button();
                deleteButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-padding: 5 10;");
                javafx.scene.layout.HBox deleteContent = new javafx.scene.layout.HBox(5);
                deleteContent.setAlignment(javafx.geometry.Pos.CENTER);
                FontIcon deleteIcon = new FontIcon("fas-trash");
                deleteIcon.setIconSize(14);
                deleteIcon.setIconColor(javafx.scene.paint.Color.WHITE);
                javafx.scene.control.Label deleteLabel = new javafx.scene.control.Label("Supprimer");
                deleteLabel.setStyle("-fx-text-fill: white;");
                deleteContent.getChildren().addAll(deleteIcon, deleteLabel);
                deleteButton.setGraphic(deleteContent);
                deleteButton.setOnAction(e -> handleDeleteComment(comment));

                buttonsBox.getChildren().addAll(editButton, deleteButton);

                commentBox.getChildren().addAll(contentBox, date, buttonsBox);

                if (comment.getReportCount() < 3) {
                    javafx.scene.control.Button reportButton = new javafx.scene.control.Button();

                    String buttonColor = comment.getReportCount() == 2 ? "#FF4500" : "#e74c3c";
                    reportButton.setStyle("-fx-background-color: " + buttonColor + "; -fx-text-fill: white; -fx-padding: 5 10;");
                    
                    javafx.scene.layout.HBox reportContent = new javafx.scene.layout.HBox(5);
                    reportContent.setAlignment(javafx.geometry.Pos.CENTER);
                    FontIcon reportIcon = new FontIcon("fas-flag");
                    reportIcon.setIconSize(14);
                    reportIcon.setIconColor(javafx.scene.paint.Color.WHITE);
                    javafx.scene.control.Label reportLabel = new javafx.scene.control.Label("Signaler");
                    reportLabel.setStyle("-fx-text-fill: white;");
                    reportContent.getChildren().addAll(reportIcon, reportLabel);
                    reportButton.setGraphic(reportContent);

                    reportButton.setOnAction(e -> {
                        if (comment.getReportCount() == 2) {
                            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                                javafx.scene.control.Alert.AlertType.CONFIRMATION,
                                "Ce signalement va supprimer le commentaire. Êtes-vous sûr ?",
                                javafx.scene.control.ButtonType.YES,
                                javafx.scene.control.ButtonType.NO
                            );
                            alert.setTitle("Confirmation de suppression");
                            alert.setHeaderText("Dernier signalement");
                            
                            alert.showAndWait().ifPresent(response -> {
                                if (response == javafx.scene.control.ButtonType.YES) {
                                    try {
                                        commentCRUD.reportComment(comment.getId());
                                        loadComments();
                                    } catch (SQLException ex) {
                                        System.err.println("Erreur lors du signalement : " + ex.getMessage());
                                    }
                                }
                            });
                        } else {
                            try {
                                commentCRUD.reportComment(comment.getId());
                                loadComments();
                            } catch (SQLException ex) {
                                System.err.println("Erreur lors du signalement : " + ex.getMessage());
                            }
                        }
                    });
                    
                    commentBox.getChildren().add(reportButton);

                    if (comment.getReportCount() == 2) {
                        javafx.scene.text.Text warningText = new javafx.scene.text.Text(
                            "Attention : Le prochain signalement supprimera ce commentaire !"
                        );
                        warningText.setStyle("-fx-fill: #FF4500; -fx-font-size: 12px;");
                        commentBox.getChildren().add(warningText);
                    }
                }

                commentsContainer.getChildren().add(commentBox);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors du chargement des comments : " + e.getMessage());
        }
    }

    private void handleEditComment(Comment comment, VBox commentBox) {
        TextArea editArea = new TextArea(comment.getContenu());
        editArea.setWrapText(true);
        editArea.setPrefRowCount(3);

        javafx.scene.control.Button saveButton = new javafx.scene.control.Button("Enregistrer");
        saveButton.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white;");

        javafx.scene.control.Button cancelButton = new javafx.scene.control.Button("Annuler");
        cancelButton.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white;");

        javafx.scene.layout.HBox editButtonsBox = new javafx.scene.layout.HBox(10);
        editButtonsBox.getChildren().addAll(saveButton, cancelButton);

        VBox editContainer = new VBox(10);
        editContainer.getChildren().addAll(editArea, editButtonsBox);

        commentBox.getChildren().setAll(editContainer);

        saveButton.setOnAction(e -> {
            String newContent = editArea.getText().trim();
            if (!newContent.isEmpty()) {
                try {
                    comment.setContenu(newContent);
                    commentCRUD.update(comment);
                    loadComments(); // Reload all comments
                } catch (SQLException ex) {
                    showError("Erreur lors de la modification du commentaire", ex.getMessage());
                }
            }
        });

        cancelButton.setOnAction(e -> loadComments());
    }

    private void handleDeleteComment(Comment comment) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
            javafx.scene.control.Alert.AlertType.CONFIRMATION,
            "Êtes-vous sûr de vouloir supprimer ce commentaire ?",
            javafx.scene.control.ButtonType.YES,
            javafx.scene.control.ButtonType.NO
        );
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer le commentaire");
        
        alert.showAndWait().ifPresent(response -> {
            if (response == javafx.scene.control.ButtonType.YES) {
                try {
                    commentCRUD.delete(comment.getId());
                    loadComments(); // Reload comments after deletion
                } catch (SQLException ex) {
                    showError("Erreur lors de la suppression du commentaire", ex.getMessage());
                }
            }
        });
    }

    private void showError(String title, String content) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
            javafx.scene.control.Alert.AlertType.ERROR
        );
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void handleAddComment() {
        String content = commentTextArea.getText().trim();
        if (!content.isEmpty()) {
            boolean containsBadWords = BadWordsDetector.containsBadWords(content);

            if (containsBadWords) {
                content = BadWordsDetector.censorBadWords(content);

                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                    javafx.scene.control.Alert.AlertType.WARNING,
                    "Votre commentaire contient des mots inappropriés qui ont été censurés. " +
                    "Le commentaire sera automatiquement signalé.",
                    javafx.scene.control.ButtonType.OK
                );
                alert.setTitle("Contenu inapproprié détecté");
                alert.setHeaderText("Avertissement");
                alert.show();
            }

            try {
                Comment comment = new Comment(content, currentBlog.getId(), currentUserId);
                commentCRUD.add(comment);

                if (containsBadWords) {
                    String updateSql = "UPDATE comment SET is_reported = 1, report_count = 1 WHERE id = ?";
                    try (java.sql.PreparedStatement ps = commentCRUD.getCnx().prepareStatement(updateSql)) {
                        ps.setInt(1, comment.getId());
                        ps.executeUpdate();
                    }
                }
                
                commentTextArea.clear();
                loadComments();
            } catch (SQLException e) {
                System.err.println("Erreur lors de l'ajout du comment : " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleRetour() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/FXML/AfficherBlogs.fxml"));
            Stage stage = (Stage) titreLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Liste des Blogs");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    private void updateButtonStyle(javafx.scene.control.Button button, Reaction.ReactionType type) {
        if (currentUserReaction == type) {
            button.setStyle(button.getStyle() + "; -fx-border-color: black; -fx-border-width: 2;");
        } else {
            button.setStyle(button.getStyle().replace("; -fx-border-color: black; -fx-border-width: 2;", ""));
        }
    }

    @FXML
    private void handleReaction(javafx.event.ActionEvent event) {
        if (currentBlog == null) return;

        javafx.scene.control.Button clickedButton = (javafx.scene.control.Button) event.getSource();
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
            showError("Erreur de réaction", "Une erreur est survenue lors de la gestion de votre réaction.");
        }
    }

    private String createTemporaryHtmlPage() {
        try {
            java.nio.file.Path tempFile = java.nio.file.Files.createTempFile("blog_", ".html");

            String htmlContent = String.format("""
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <title>%s</title>
                    <meta property="og:title" content="%s">
                    <meta property="og:description" content="%s">
                    <meta property="og:type" content="article">
                    <style>
                        body { font-family: Arial, sans-serif; max-width: 800px; margin: 0 auto; padding: 20px; }
                        h1 { color: #2c3e50; }
                        .description { line-height: 1.6; color: #333; }
                        .type { color: #3498db; }
                    </style>
                </head>
                <body>
                    <h1>%s</h1>
                    <div class="description">%s</div>
                    <p class="type">Type: %s</p>
                </body>
                </html>
                """,
                currentBlog.getTitre(),
                currentBlog.getTitre(),
                currentBlog.getDescr(),
                currentBlog.getTitre(),
                currentBlog.getDescr(),
                currentBlog.getType() != null ? currentBlog.getType().getLibelle() : "Non spécifié"
            );

            java.nio.file.Files.writeString(tempFile, htmlContent);

            return tempFile.toUri().toString();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
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

    private String makeHttpRequest(String url, Map<String, String> params) throws IOException {
        java.net.URL apiUrl = new java.net.URL(url);
        java.net.HttpURLConnection conn = (java.net.HttpURLConnection) apiUrl.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);

        StringBuilder postData = new StringBuilder();
        for (Map.Entry<String, String> param : params.entrySet()) {
            if (postData.length() != 0) postData.append('&');
            postData.append(encodeURIComponent(param.getKey()));
            postData.append('=');
            postData.append(encodeURIComponent(param.getValue()));
        }

        try (java.io.OutputStream os = conn.getOutputStream()) {
            byte[] input = postData.toString().getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        try (java.io.BufferedReader br = new java.io.BufferedReader(
            new java.io.InputStreamReader(conn.getInputStream(), "utf-8"))) {
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            return response.toString();
        }
    }

    private String encodeURIComponent(String str) {
        try {
            return java.net.URLEncoder.encode(str, "UTF-8")
                .replaceAll("\\+", "%20")
                .replaceAll("\\%21", "!")
                .replaceAll("\\%27", "'")
                .replaceAll("\\%28", "(")
                .replaceAll("\\%29", ")")
                .replaceAll("\\%7E", "~");
        } catch (java.io.UnsupportedEncodingException e) {
            return str;
        }
    }
} 