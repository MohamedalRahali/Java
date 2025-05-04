package gui;

import Services.ReclamationService;
import Services.ReponseService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import models.Reclamation;
import models.Reponse;
import utils.PDFGenerator;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javafx.event.ActionEvent;
import javafx.scene.Node;

public class ReclamationViewController implements Initializable {
    // ... autres champs existants ...
    @FXML private Button statButton; // pour le bouton Statistique

    @FXML
    private AdminBarController adminBarController;

    private static final Logger LOGGER = Logger.getLogger(ReclamationViewController.class.getName());
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    
    @FXML private ListView<Reclamation> reclamationListView;
    @FXML private TextField searchField;
    @FXML private Button sortButton;
    @FXML private Label totalCountLabel;
    @FXML private Label bugCountLabel;
    @FXML private Label featureCountLabel;
    @FXML private Label supportCountLabel;

    private ReclamationService reclamationService;
    private ObservableList<Reclamation> allReclamations;
    private FilteredList<Reclamation> filteredData;
    private SortedList<Reclamation> sortedData;
    private boolean ascendingOrder = true;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        if (adminBarController != null && models.CurrentUser.getCurrentUser() != null) {
            adminBarController.setUserEmail(models.CurrentUser.getCurrentUser().getEmail());
        }
        try {
            LOGGER.info("Initialisation du contrôleur de réclamations");
            
            // Initialisation des services
            reclamationService = new ReclamationService();
            
            // Initialisation des listes
            allReclamations = FXCollections.observableArrayList();
            filteredData = new FilteredList<>(allReclamations, p -> true);
            sortedData = new SortedList<>(filteredData);
            
            // Configuration de la ListView
            setupListView();
            
            // Configuration des filtres
            setupFilters();
            
            // Chargement initial des réclamations
            loadReclamations();
            
            LOGGER.info("Initialisation du contrôleur terminée avec succès");
        } catch (Exception e) {
            LOGGER.severe("Erreur lors de l'initialisation: " + e.getMessage());
            showError("Erreur d'initialisation", "Impossible d'initialiser l'application: " + e.getMessage());
        }
    }

    private void setupListView() {
        try {
            LOGGER.info("Configuration de la ListView");
            
            // Configurer la ListView avec la liste triée
            reclamationListView.setItems(sortedData);
            
            // Configurer la cellule personnalisée
            reclamationListView.setCellFactory(lv -> new ListCell<Reclamation>() {
                @Override
                protected void updateItem(Reclamation reclamation, boolean empty) {
                    super.updateItem(reclamation, empty);
                    
                    if (empty || reclamation == null) {
                        setText(null);
                        setGraphic(null);
                        return;
                    }
                    
                    // Créer le conteneur principal
                    HBox mainContainer = new HBox(10);
                    mainContainer.setAlignment(Pos.CENTER_LEFT);
                    mainContainer.setMaxWidth(Double.MAX_VALUE);
                    
                    // Créer le conteneur pour les informations de la réclamation
                    VBox reclamationContainer = new VBox(5);
                    reclamationContainer.setPadding(new Insets(10));
                    reclamationContainer.getStyleClass().add("reclamation-cell");
                    reclamationContainer.setMaxWidth(Double.MAX_VALUE);
                    HBox.setHgrow(reclamationContainer, Priority.ALWAYS);
                    
                    // Titre
                    Label titleLabel = new Label(reclamation.getTitle());
                    titleLabel.getStyleClass().add("reclamation-title");
                    titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
                    
                    // Description
                    Label descriptionLabel = new Label(reclamation.getDescription());
                    descriptionLabel.getStyleClass().add("reclamation-description");
                    descriptionLabel.setWrapText(true);
                    
                    // Type et date
                    HBox footer = new HBox(10);
                    footer.setAlignment(Pos.CENTER_LEFT);
                    
                    Label typeLabel = new Label(reclamation.getTypeName());
                    typeLabel.getStyleClass().add("reclamation-type");
                    typeLabel.setStyle("-fx-text-fill: #666666;");
                    
                    Label dateLabel = new Label(reclamation.getCreatedAt().format(DATE_FORMATTER));
                    dateLabel.getStyleClass().add("reclamation-date");
                    dateLabel.setStyle("-fx-text-fill: #666666;");
                    
                    footer.getChildren().addAll(typeLabel, new Separator(), dateLabel);
                    
                    // Ajouter tous les éléments au conteneur
                    reclamationContainer.getChildren().addAll(titleLabel, descriptionLabel, footer);
                    
                    // Bouton PDF
                    Button pdfButton = new Button("📄");
                    pdfButton.setStyle("-fx-background-color: #ff0000; -fx-text-fill: white; -fx-font-size: 14px; -fx-background-radius: 4px; -fx-padding: 5px 10px;");
                    pdfButton.setOnAction(e -> {
                        try {
                            FileChooser fileChooser = new FileChooser();
                            fileChooser.setTitle("Enregistrer le PDF");
                            fileChooser.setInitialFileName("reclamation_" + reclamation.getId() + ".pdf");
                            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
                            
                            File file = fileChooser.showSaveDialog(getScene().getWindow());
                            if (file != null) {
                                String pdfPath = PDFGenerator.generateReclamationPDF(reclamation, file.getAbsolutePath());
                                showSuccess("Succès", "PDF généré avec succès : " + pdfPath);
                            }
                        } catch (IOException ex) {
                            LOGGER.severe("Erreur lors de la génération du PDF");
                            showError("Erreur", "Impossible de générer le PDF : " + ex.getMessage());
                        }
                    });
                    
                    // Ajouter le conteneur de réclamation et le bouton PDF au conteneur principal
                    mainContainer.getChildren().addAll(reclamationContainer, pdfButton);
                    
                    // Définir le conteneur principal comme graphique de la cellule
                    setGraphic(mainContainer);
                }
            });
            
            // Ajouter un gestionnaire de double-clic
            reclamationListView.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2) {
                    Reclamation selected = reclamationListView.getSelectionModel().getSelectedItem();
                    if (selected != null) {
                        LOGGER.info("Double-clic sur la réclamation: " + selected.getTitle());
                        showReclamationDetails(selected);
                    }
                }
            });
            
            LOGGER.info("Configuration de la ListView terminée");
        } catch (Exception e) {
            LOGGER.severe("Erreur lors de la configuration de la ListView: " + e.getMessage());
            showError("Erreur", "Impossible de configurer l'affichage des réclamations.");
        }
    }

    private void setupFilters() {
        try {
            LOGGER.info("Configuration des filtres");
            
            // Configuration du filtre de recherche
            searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                filterReclamations();
            });
            
            LOGGER.info("Configuration des filtres terminée");
        } catch (Exception e) {
            LOGGER.severe("Erreur lors de la configuration des filtres: " + e.getMessage());
            showError("Erreur", "Impossible de configurer les filtres.");
        }
    }

    private void loadReclamations() {
        try {
            LOGGER.info("Chargement des réclamations depuis la base de données");
            List<Reclamation> reclamations = reclamationService.getAll();
            LOGGER.info("Nombre de réclamations récupérées: " + reclamations.size());
            
            // Mettre à jour la liste principale
            allReclamations.clear();
            allReclamations.addAll(reclamations);
            
            // Mettre à jour la ListView
            reclamationListView.setItems(allReclamations);
            
        } catch (Exception e) {
            LOGGER.severe("Erreur lors du chargement des réclamations: " + e.getMessage());
            showError("Erreur de chargement", "Impossible de charger les réclamations: " + e.getMessage());
        }
    }

    private void filterReclamations() {
        try {
            LOGGER.info("Filtrage des réclamations...");
            String searchText = searchField.getText().toLowerCase();

            List<Reclamation> filteredList = allReclamations.stream()
                .filter(reclamation -> {
                    boolean matchesSearch = searchText.isEmpty() || 
                        (reclamation.getTitle() != null && reclamation.getTitle().toLowerCase().contains(searchText)) ||
                        (reclamation.getDescription() != null && reclamation.getDescription().toLowerCase().contains(searchText));
                    
                    return matchesSearch;
                })
                .collect(Collectors.toList());

            reclamationListView.setItems(FXCollections.observableArrayList(filteredList));
            LOGGER.info("Filtrage terminé. " + filteredList.size() + " réclamations affichées");
        } catch (Exception e) {
            LOGGER.severe("Erreur lors du filtrage des réclamations: " + e.getMessage());
            showError("Erreur", "Impossible de filtrer les réclamations: " + e.getMessage());
        }
    }

    @FXML
    private void handleSort() {
        try {
            LOGGER.info("Tri des réclamations - Ordre actuel: " + (ascendingOrder ? "ascendant" : "descendant"));
            ascendingOrder = !ascendingOrder;
            updateSort();
            sortButton.setText(ascendingOrder ? "🔄 Trier (A-Z)" : "🔄 Trier (Z-A)");
            LOGGER.info("Tri effectué avec succès. Ordre: " + (ascendingOrder ? "ascendant" : "descendant"));
        } catch (Exception e) {
            LOGGER.severe("Erreur lors du tri des réclamations: " + e.getMessage());
            showError("Erreur", "Impossible de trier les réclamations: " + e.getMessage());
        }
    }

    private void updateSort() {
        try {
            ObservableList<Reclamation> currentItems = reclamationListView.getItems();
            List<Reclamation> sortedList = currentItems.stream()
                .sorted((r1, r2) -> {
                    String title1 = r1.getTitle() != null ? r1.getTitle().toLowerCase() : "";
                    String title2 = r2.getTitle() != null ? r2.getTitle().toLowerCase() : "";
                    return ascendingOrder ? 
                        title1.compareTo(title2) : 
                        title2.compareTo(title1);
                })
                .collect(Collectors.toList());
            
            reclamationListView.setItems(FXCollections.observableArrayList(sortedList));
            LOGGER.info("Liste triée avec succès - " + sortedList.size() + " réclamations");
        } catch (Exception e) {
            LOGGER.severe("Erreur lors de la mise à jour du tri: " + e.getMessage());
            showError("Erreur", "Impossible de mettre à jour le tri: " + e.getMessage());
        }
    }

    @FXML
    private void handleEditReclamation() throws IOException {
        Reclamation selectedReclamation = reclamationListView.getSelectionModel().getSelectedItem();
        if (selectedReclamation == null) {
            showError("Attention", "Veuillez sélectionner une réclamation à modifier.");
            return;
        }

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/modification_reclamation.fxml"));
        Parent root = loader.load();

        ModifyReclamationController controller = loader.getController();
        controller.setReclamationToEdit(selectedReclamation);

        Stage stage = (Stage) reclamationListView.getScene().getWindow();
        stage.setTitle("Modifier Réclamation");
        stage.setScene(new Scene(root));
    }

    @FXML
    private void handleDeleteReclamation() {
        Reclamation selectedReclamation = reclamationListView.getSelectionModel().getSelectedItem();
        if (selectedReclamation == null) {
            showError("Attention", "Veuillez sélectionner une réclamation à supprimer.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Suppression de réclamation");
        alert.setContentText("Voulez-vous vraiment supprimer cette réclamation ?");
        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            reclamationService.supprimer(selectedReclamation.getId());
            loadReclamations(); // Rafraîchir la liste
        }
    }

    @FXML
    private void goBackToHome() throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/HomeReclamation.fxml"));
        Stage stage = (Stage) reclamationListView.getScene().getWindow();
        stage.setTitle("Accueil");
        stage.setScene(new Scene(root));
    }

    @FXML
    private void handleGeneratePDF() {
        Reclamation selectedReclamation = reclamationListView.getSelectionModel().getSelectedItem();
        if (selectedReclamation == null) {
            showError("Attention", "Veuillez sélectionner une réclamation pour générer le PDF.");
            return;
        }

        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Enregistrer le PDF");
            fileChooser.setInitialFileName("reclamation_" + selectedReclamation.getId() + ".pdf");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
            
            File file = fileChooser.showSaveDialog(reclamationListView.getScene().getWindow());
            if (file != null) {
                String pdfPath = PDFGenerator.generateReclamationPDF(selectedReclamation, file.getAbsolutePath());
                showSuccess("Succès", "PDF généré avec succès : " + pdfPath);
            }
        } catch (IOException e) {
            LOGGER.severe("Erreur lors de la génération du PDF");
            showError("Erreur", "Impossible de générer le PDF : " + e.getMessage());
        }
    }

    @FXML
    private void handleReplyToReclamation() {
        Reclamation selectedReclamation = reclamationListView.getSelectionModel().getSelectedItem();
        if (selectedReclamation == null) {
            showError("Attention", "Veuillez sélectionner une réclamation à laquelle répondre.");
            return;
        }

        // Créer une boîte de dialogue pour la réponse
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Répondre à la réclamation");
        dialog.setHeaderText("Réponse à la réclamation: " + selectedReclamation.getTitle());

        // Créer le contenu de la boîte de dialogue
        TextArea replyTextArea = new TextArea();
        replyTextArea.setPromptText("Entrez votre réponse ici...");
        replyTextArea.setPrefRowCount(5);
        replyTextArea.setWrapText(true);

        dialog.getDialogPane().setContent(replyTextArea);

        // Ajouter les boutons
        ButtonType replyButtonType = new ButtonType("Répondre", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(replyButtonType, ButtonType.CANCEL);

        // Afficher la boîte de dialogue et attendre la réponse
        Optional<ButtonType> result = dialog.showAndWait();

        if (result.isPresent() && result.get() == replyButtonType) {
            String replyMessage = replyTextArea.getText().trim();
            if (replyMessage.isEmpty()) {
                showError("Erreur", "Le message de réponse ne peut pas être vide.");
                return;
            }

            try {
                // Créer et sauvegarder la réponse
                Reponse reponse = new Reponse(selectedReclamation.getId(), replyMessage);
                ReponseService reponseService = new ReponseService();
                reponseService.ajouter(reponse);

                showSuccess("Succès", "Votre réponse a été ajoutée avec succès.");
                
                // Rafraîchir la liste des réponses
                loadReponses(selectedReclamation);
            } catch (Exception e) {
                showError("Erreur", "Impossible d'ajouter la réponse: " + e.getMessage());
            }
        }
    }

    private void loadReponses(Reclamation reclamation) {
        try {
            ReponseService reponseService = new ReponseService();
            List<Reponse> reponses = reponseService.getReponsesByReclamationId(reclamation.getId());
            
            // Afficher les réponses dans une nouvelle fenêtre
            Stage stage = new Stage();
            stage.setTitle("Réponses - " + reclamation.getTitle());
            
            VBox vbox = new VBox(10);
            vbox.setPadding(new Insets(10));
            
            // Ajouter chaque réponse
            for (Reponse reponse : reponses) {
                VBox reponseBox = new VBox(5);
                reponseBox.setStyle("-fx-background-color: #f0f0f0; -fx-padding: 10px; -fx-background-radius: 5px;");
                
                Label dateLabel = new Label(reponse.getCreatedAt().format(DATE_FORMATTER));
                dateLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #666666;");
                
                Label messageLabel = new Label(reponse.getMessage());
                messageLabel.setWrapText(true);
                
                reponseBox.getChildren().addAll(dateLabel, messageLabel);
                vbox.getChildren().add(reponseBox);
            }
            
            // Ajouter un bouton pour répondre
            Button replyButton = new Button("Répondre");
            replyButton.setOnAction(e -> handleReplyToReclamation());
            vbox.getChildren().add(replyButton);
            
            ScrollPane scrollPane = new ScrollPane(vbox);
            scrollPane.setFitToWidth(true);
            
            Scene scene = new Scene(scrollPane, 400, 300);
            stage.setScene(scene);
            stage.show();
            
        } catch (Exception e) {
            showError("Erreur", "Impossible de charger les réponses: " + e.getMessage());
        }
    }

    private void showReclamationDetails(Reclamation reclamation) {
        try {
            LOGGER.info("Affichage des détails de la réclamation: " + reclamation.getTitle());
            
            // Créer une nouvelle fenêtre pour les détails
            Stage detailsStage = new Stage();
            detailsStage.initStyle(StageStyle.UTILITY);
            detailsStage.setTitle("Détails de la réclamation");
            
            // Créer le contenu
            VBox content = new VBox(10);
            content.setPadding(new Insets(20));
            
            // Titre
            Label titleLabel = new Label(reclamation.getTitle());
            titleLabel.getStyleClass().add("details-title");
            
            // Description
            Label descriptionLabel = new Label(reclamation.getDescription());
            descriptionLabel.getStyleClass().add("details-description");
            descriptionLabel.setWrapText(true);
            
            // Informations supplémentaires
            GridPane infoGrid = new GridPane();
            infoGrid.setHgap(10);
            infoGrid.setVgap(10);
            
            infoGrid.add(new Label("Type:"), 0, 0);
            infoGrid.add(new Label(reclamation.getTypeName()), 1, 0);
            
            infoGrid.add(new Label("Date:"), 0, 1);
            infoGrid.add(new Label(reclamation.getCreatedAt().format(DATE_FORMATTER)), 1, 1);
            
            // Bouton de fermeture
            Button closeButton = new Button("Fermer");
            closeButton.setOnAction(e -> detailsStage.close());
            
            // Ajouter tous les éléments
            content.getChildren().addAll(titleLabel, descriptionLabel, infoGrid, closeButton);
            
            // Créer la scène
            Scene scene = new Scene(content, 400, 300);
            scene.getStylesheets().add(getClass().getResource("/styles/styles.css").toExternalForm());
            
            detailsStage.setScene(scene);
            detailsStage.show();
            
            LOGGER.info("Fenêtre de détails affichée");
        } catch (Exception e) {
            LOGGER.severe("Erreur lors de l'affichage des détails: " + e.getMessage());
            showError("Erreur", "Impossible d'afficher les détails de la réclamation.");
        }
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showSuccess(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private String getTypeColor(String type) {
        switch (type.toLowerCase()) {
            case "bug":
                return "#ffebee"; // Rouge clair
            case "feature request":
                return "#e8f5e9"; // Vert clair
            case "support":
                return "#e3f2fd"; // Bleu clair
            default:
                return "#ffffff"; // Blanc
        }
    }

    /**
     * Affiche les statistiques des réclamations par type dans une boîte de dialogue.
     */
    @FXML
    private void handleShowStats() {
        if (allReclamations == null || allReclamations.isEmpty()) {
            showError("Statistiques", "Aucune réclamation à analyser.");
            return;
        }
        // Affiche la boîte de dialogue stylée avec barres
        StatsBarDialog.show(allReclamations);
    }

    @FXML
    private void goToReclamationView(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/reclamation_view.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Liste des Réclamations");
        } catch (Exception e) {
            LOGGER.severe("Erreur de navigation vers la vue des réclamations: " + e.getMessage());
            showError("Erreur de navigation", "Impossible d'accéder à la vue des réclamations");
        }
    }

    @FXML
    private void handleTypeReclamation(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/type_reclamation.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Ajouter un Type de Réclamation");
        } catch (Exception e) {
            LOGGER.severe("Erreur de navigation vers la vue type réclamation: " + e.getMessage());
            showError("Erreur de navigation", "Impossible d'accéder à la vue type réclamation");
        }
    }

    @FXML
    private void handleClientSpace(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/create_reclamation.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Créer une Réclamation");
        } catch (Exception e) {
            LOGGER.severe("Erreur de navigation vers la création de réclamation: " + e.getMessage());
            showError("Erreur de navigation", "Impossible d'accéder à la création de réclamation");
        }
    }

    @FXML
    private void goToAdminHome(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin_home.fxml"));
        Parent root = loader.load();
        Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("User Dashboard");
    }

    @FXML
    private void goToHomeReclamation(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/HomeReclamation.fxml"));
        Parent root = loader.load();
        Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("Home Reclamation");
    }

    @FXML
    private void logout(ActionEvent event) throws IOException {
        models.CurrentUser.clear();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
        Parent root = loader.load();
        Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("Login");
    }
}