package esprit.tn.controllers;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import esprit.tn.entities.Categorie;
import esprit.tn.entities.Produit;
import esprit.tn.service.CategorieService;
import esprit.tn.service.ProduitService;
import javafx.animation.KeyFrame;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class ProduitController {
    private static final Logger logger = LogManager.getLogger(ProduitController.class);
    private static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024; // 5MB

    // Composants FXML
    @FXML private BorderPane rootPane;
    @FXML private TableView<Produit> produitTable;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> categorieFilter;
    @FXML private Slider prixFilter;
    @FXML private Label prixLabel;
    @FXML private TextField titreField;
    @FXML private TextArea descriptionArea;
    @FXML private TextField nomArtisteField;
    @FXML private TextField prixField;
    @FXML private TextField statutField;
    @FXML private ComboBox<Categorie> categorieCombo;
    @FXML private TextField imageField;
    @FXML private Button uploadImageButton;
    @FXML private Button addButton;
    @FXML private Button updateButton;
    @FXML private Button clearFormButton;
    @FXML private Button exportPdfButton;
    @FXML private Button refreshButton;
    @FXML private Button musicToggleButton;
    @FXML private Button toggleStatsButton;
    @FXML private Slider volumeSlider;
    @FXML private BarChart<String, Number> statsChart;
    @FXML private VBox notificationPane;
    @FXML private StackPane loadingOverlay;
    @FXML private ProgressIndicator loadingIndicator;
    @FXML private ImageView imagePreview;

    // Données et services
    private ProduitService produitService;
    private ObservableList<Produit> produits;
    private FilteredList<Produit> filteredProduits;
    private Timeline autoRefreshTimeline;
    private Stage statsStage;
    private BarChart<String, Number> categorieChart;
    private BarChart<String, Number> statutChart;
    private Produit produit; // Added field to store the current Produit

    // Propriétés
    private final DoubleProperty stageWidth = new SimpleDoubleProperty(1280);
    private final DoubleProperty stageHeight = new SimpleDoubleProperty(720);

    @FXML
    public void initialize() {
        logger.info("Initializing ProduitController...");
        produitService = new ProduitService();
        produits = FXCollections.observableArrayList();
        filteredProduits = new FilteredList<>(produits, p -> true);

        configureStageBinding();
        configureProduitTable();
        loadCategories();
        setupTableColumns();
        setupFilters();
        setupCharts();
        setupAutoRefresh();
        setupValidation();
        setupButtonAnimations();
        setupFieldListeners();

        loadProduits();
    }

    private void configureStageBinding() {
        rootPane.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                Stage stage = (Stage) newScene.getWindow();
                stageWidth.bind(stage.widthProperty());
                stageHeight.bind(stage.heightProperty());
            }
        });
    }

    private void configureProduitTable() {
        produitTable.setItems(filteredProduits);
        produitTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        produitTable.setRowFactory(tv -> {
            TableRow<Produit> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getClickCount() == 1) {
                    handleEdit(row.getItem());
                }
            });
            return row;
        });
    }

    private void setupFieldListeners() {
        prixLabel.textProperty().bind(prixFilter.valueProperty().asString("Prix max: %.0f DT"));

        titreField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.trim().isEmpty()) {
                String categorie = categorieCombo.getValue() != null ?
                        categorieCombo.getValue().getLibelle() : "Artisanat";
                descriptionArea.setText(generateFallbackDescription(newValue.trim(), categorie));
            } else {
                descriptionArea.clear();
            }
        });

        produits.addListener((ListChangeListener.Change<? extends Produit> change) -> {
            while (change.next()) {
                updateCharts();
            }
        });
    }

    private void setupButtonAnimations() {
        logger.info("addButton: " + (addButton != null ? "OK" : "NULL"));
        logger.info("updateButton: " + (updateButton != null ? "OK" : "NULL"));
        logger.info("clearFormButton: " + (clearFormButton != null ? "OK" : "NULL"));
        logger.info("exportPdfButton: " + (exportPdfButton != null ? "OK" : "NULL"));
        logger.info("toggleStatsButton: " + (toggleStatsButton != null ? "OK" : "NULL"));
        logger.info("musicToggleButton: " + (musicToggleButton != null ? "OK" : "NULL"));
        logger.info("uploadImageButton: " + (uploadImageButton != null ? "OK" : "NULL"));
        logger.info("refreshButton: " + (refreshButton != null ? "OK" : "NULL"));

        List<Button> buttons = Arrays.asList(
                addButton, updateButton, clearFormButton,
                exportPdfButton, toggleStatsButton,
                musicToggleButton, uploadImageButton, refreshButton
        );

        buttons.forEach(button -> {
            if (button != null) {
                configureButtonAnimation(button);
            } else {
                logger.warn("A button is null in setupButtonAnimations");
            }
        });
    }

    private void configureButtonAnimation(Button button) {
        ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(200), button);
        scaleTransition.setToX(1.1);
        scaleTransition.setToY(1.1);
        scaleTransition.setAutoReverse(true);

        button.setOnMouseEntered(e -> scaleTransition.playFromStart());
        button.setOnMouseExited(e -> {
            scaleTransition.stop();
            button.setScaleX(1.0);
            button.setScaleY(1.0);
        });
    }

    private void loadCategories() {
        try {
            CategorieService categorieService = new CategorieService();
            List<Categorie> categories = categorieService.getAllCategories();

            if (categories == null || categories.isEmpty()) {
                showNotification("Aucune catégorie trouvée", "error");
                return;
            }

            categorieCombo.setItems(FXCollections.observableArrayList(categories));
            categorieCombo.setConverter(new javafx.util.StringConverter<>() {
                @Override public String toString(Categorie cat) {
                    return cat != null && cat.getLibelle() != null ? cat.getLibelle() : "";
                }
                @Override public Categorie fromString(String string) { return null; }
            });

            List<String> categoryLabels = new ArrayList<>();
            categoryLabels.add("Toutes");
            categoryLabels.addAll(categories.stream()
                    .filter(cat -> cat.getLibelle() != null)
                    .map(Categorie::getLibelle)
                    .collect(Collectors.toList()));

            categorieFilter.setItems(FXCollections.observableArrayList(categoryLabels));
            categorieFilter.setValue("Toutes");
        } catch (SQLException e) {
            logger.error("Erreur chargement catégories", e);
            showNotification("Erreur chargement catégories: " + e.getMessage(), "error");
        }
    }

    private void setupTableColumns() {
        TableColumn<Produit, String> titreCol = new TableColumn<>("Titre");
        titreCol.setCellValueFactory(new PropertyValueFactory<>("titre"));

        TableColumn<Produit, Float> prixCol = new TableColumn<>("Prix");
        prixCol.setCellValueFactory(new PropertyValueFactory<>("prix"));
        prixCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Float prix, boolean empty) {
                super.updateItem(prix, empty);
                setText(empty || prix == null ? null : String.format("%.2f DT", prix));
            }
        });

        TableColumn<Produit, String> nomArtisteCol = new TableColumn<>("Artiste");
        nomArtisteCol.setCellValueFactory(new PropertyValueFactory<>("nomArtiste"));

        TableColumn<Produit, String> statutCol = new TableColumn<>("Statut");
        statutCol.setCellValueFactory(new PropertyValueFactory<>("statut"));

        TableColumn<Produit, String> categorieCol = new TableColumn<>();
        categorieCol.setCellValueFactory(cellData -> {
            Categorie cat = cellData.getValue().getCategorie();
            return new SimpleStringProperty(cat != null && cat.getLibelle() != null ? cat.getLibelle() : "");
        });

        TableColumn<Produit, String> imageCol = new TableColumn<>("Image");
        imageCol.setCellValueFactory(new PropertyValueFactory<>("image"));
        imageCol.setCellFactory(col -> new TableCell<Produit, String>() {
            private final ImageView imageView = new ImageView();
            {
                imageView.setFitWidth(50);
                imageView.setFitHeight(50);
                imageView.setPreserveRatio(true);
            }

            @Override protected void updateItem(String imagePath, boolean empty) {
                super.updateItem(imagePath, empty);
                if (empty || imagePath == null || imagePath.isEmpty()) {
                    setGraphic(null);
                } else {
                    try {
                        Image image = loadImage(imagePath);
                        imageView.setImage(image);
                        setGraphic(imageView);
                    } catch (Exception e) {
                        setGraphic(new Label("Erreur image"));
                    }
                }
            }
        });

        TableColumn<Produit, Void> actionCol = new TableColumn<>("Actions");
        actionCol.setCellFactory(col -> new TableCell<Produit, Void>() {
            private final Button editBtn = new Button("Éditer");
            private final Button deleteBtn = new Button("Supprimer");

            {
                editBtn.getStyleClass().add("btn-outline-primary");
                deleteBtn.getStyleClass().add("btn-outline-danger");
                editBtn.setOnAction(e -> handleEdit(getTableView().getItems().get(getIndex())));
                deleteBtn.setOnAction(e -> handleDelete(getTableView().getItems().get(getIndex())));
                configureButtonAnimation(editBtn);
                configureButtonAnimation(deleteBtn);
            }

            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : new HBox(5, editBtn, deleteBtn));
            }
        });

        produitTable.getColumns().setAll(titreCol, prixCol, nomArtisteCol, statutCol, categorieCol, imageCol, actionCol);
    }

    private Image loadImage(String imagePath) throws Exception {
        URL imageUrl = getClass().getResource(imagePath);
        if (imageUrl != null) {
            return new Image(imageUrl.toExternalForm());
        } else {
            String filePath = "target/classes" + imagePath;
            File file = new File(filePath);
            if (file.exists()) {
                return new Image(file.toURI().toString());
            }
        }
        throw new Exception("Image non trouvée: " + imagePath);
    }

    private void setupFilters() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> applyFilters());
        categorieFilter.setOnAction(e -> applyFilters());
        prixFilter.setMin(0);
        prixFilter.setMax(1000);
        prixFilter.setValue(1000);
        prixFilter.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
    }

    private void applyFilters() {
        String searchText = searchField.getText() != null ? searchField.getText().toLowerCase() : "";
        String selectedCategory = categorieFilter.getValue() != null ? categorieFilter.getValue() : "Toutes";
        double maxPrice = prixFilter.getValue();

        filteredProduits.setPredicate(produit -> {
            boolean matchesSearch = searchText.isEmpty() ||
                    (produit.getTitre() != null && produit.getTitre().toLowerCase().contains(searchText)) ||
                    (produit.getDescription() != null && produit.getDescription().toLowerCase().contains(searchText)) ||
                    (produit.getNomArtiste() != null && produit.getNomArtiste().toLowerCase().contains(searchText)) ||
                    (produit.getStatut() != null && produit.getStatut().toLowerCase().contains(searchText));

            boolean matchesCategory = "Toutes".equals(selectedCategory) ||
                    (produit.getCategorie() != null &&
                            produit.getCategorie().getLibelle() != null &&
                            produit.getCategorie().getLibelle().equals(selectedCategory));

            boolean matchesPrice = produit.getPrix() <= maxPrice;

            return matchesSearch && matchesCategory && matchesPrice;
        });
    }

    private void setupAutoRefresh() {
        autoRefreshTimeline = new Timeline(new KeyFrame(Duration.minutes(5), e -> {
            loadProduits();
            showNotification("Données actualisées", "success");
        }));
        autoRefreshTimeline.setCycleCount(Timeline.INDEFINITE);
        autoRefreshTimeline.play();
    }

    private void setupValidation() {
        titreField.textProperty().addListener((obs, oldVal, newVal) -> {
            titreField.setStyle(validateTitre(newVal) ? "" : "-fx-border-color: red;");
        });

        nomArtisteField.textProperty().addListener((obs, oldVal, newVal) -> {
            nomArtisteField.setStyle(validateNomArtiste(newVal) ? "" : "-fx-border-color: red;");
        });

        prixField.textProperty().addListener((obs, oldVal, newVal) -> {
            prixField.setStyle(validatePrix(newVal) ? "" : "-fx-border-color: red;");
        });

        statutField.textProperty().addListener((obs, oldVal, newVal) -> {
            statutField.setStyle(validateStatut(newVal) ? "" : "-fx-border-color: red;");
        });

        imageField.textProperty().addListener((obs, oldVal, newVal) -> {
            imageField.setStyle(validateImagePath(newVal) ? "" : "-fx-border-color: red;");
        });
    }

    private boolean validateTitre(String titre) {
        return titre != null && titre.matches("^[a-zA-ZÀ-ÿ0-9\\s-]{3,255}$");
    }

    private boolean validateNomArtiste(String nomArtiste) {
        return nomArtiste != null && nomArtiste.matches("^[a-zA-ZÀ-ÿ0-9\\s-]{1,20}$");
    }

    private boolean validatePrix(String prixText) {
        try {
            float prix = Float.parseFloat(prixText);
            return prix > 0 && prix <= 10000;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean validateStatut(String statut) {
        return statut != null && statut.matches("^[a-zA-Z0-9\\s-]{1,50}$");
    }

    private boolean validateImagePath(String imagePath) {
        return imagePath == null || imagePath.isEmpty() || imagePath.length() <= 255;
    }

    private String generateFallbackDescription(String titre, String categorie) {
        return String.format("Produit artisanal %s de la catégorie %s, fabriqué avec soin et passion.", titre, categorie);
    }

    private void setupCharts() {
        categorieChart = createChart("Produits par Catégorie", "Catégorie");
        statutChart = createChart("Produits par Statut", "Statut");
        updateCharts();
    }

    private BarChart<String, Number> createChart(String title, String xAxisLabel) {
        BarChart<String, Number> chart = new BarChart<>(new CategoryAxis(), new NumberAxis());
        chart.setTitle(title);
        chart.getXAxis().setLabel(xAxisLabel);
        chart.getYAxis().setLabel("Nombre");
        chart.setStyle("-fx-background-color: #ffffff;");
        return chart;
    }

    private void updateCategorieChart() {
        updateChart(categorieChart, produits.stream()
                .filter(p -> p.getCategorie() != null && p.getCategorie().getLibelle() != null)
                .collect(Collectors.groupingBy(
                        p -> p.getCategorie().getLibelle(),
                        Collectors.counting()
                )));
    }

    private void updateStatutChart() {
        updateChart(statutChart, produits.stream()
                .collect(Collectors.groupingBy(
                        Produit::getStatut,
                        Collectors.counting()
                )));
    }

    private void updateChart(BarChart<String, Number> chart, Map<String, Long> data) {
        chart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName(chart.getTitle());
        data.forEach((key, value) -> series.getData().add(new XYChart.Data<>(key, value)));
        chart.getData().add(series);
    }

    private void updateCharts() {
        updateCategorieChart();
        updateStatutChart();
    }

    @FXML
    private void showStatisticsPopup() {
        if (statsStage == null || !statsStage.isShowing()) {
            statsStage = new Stage();
            statsStage.setTitle("Statistiques des Produits");

            VBox statsVBox = new VBox(10, categorieChart, statutChart);
            statsVBox.setPadding(new Insets(10));
            statsVBox.setStyle("-fx-background-color: #ffffff;");

            Scene scene = new Scene(statsVBox, 600, 500);
            statsStage.setScene(scene);
            statsStage.show();

            updateCharts();
            statsStage.setOnCloseRequest(event -> statsStage = null);
        } else {
            statsStage.toFront();
        }
    }

    @FXML
    private void handleAjouterProduit() {
        if (!validateForm()) return;

        Produit produit = createProduitFromForm();
        if (produit == null) return;

        executeAsync(() -> {
            try {
                Produit nouveauProduit = produitService.ajouterProduit(produit);
                Platform.runLater(() -> {
                    produits.add(nouveauProduit);
                    clearFields();
                    showNotification("Produit ajouté avec succès", "success");
                });
            } catch (SQLException e) {
                Platform.runLater(() ->
                        showNotification("Erreur lors de l'ajout: " + e.getMessage(), "error"));
            }
        });
    }

    @FXML
    private void handleModifierProduit() {
        Produit selectedProduit = produitTable.getSelectionModel().getSelectedItem();
        if (selectedProduit == null) {
            showNotification("Veuillez sélectionner un produit à modifier", "error");
            return;
        }

        if (!validateForm()) return;

        updateProduitFromForm(selectedProduit);
        executeAsync(() -> {
            try {
                produitService.updateProduit(selectedProduit);
                Platform.runLater(() -> {
                    produitTable.refresh();
                    clearFields();
                    showNotification("Produit mis à jour avec succès", "success");
                });
            } catch (SQLException e) {
                Platform.runLater(() ->
                        showNotification("Erreur lors de la mise à jour: " + e.getMessage(), "error"));
            }
        });
    }

    private boolean validateForm() {
        StringBuilder errors = new StringBuilder();

        if (!validateTitre(titreField.getText())) {
            errors.append("Titre invalide (3-255 caractères alphanumériques)\n");
        }
        if (!validateNomArtiste(nomArtisteField.getText())) {
            errors.append("Nom d'artiste invalide (1-20 caractères alphanumériques)\n");
        }
        if (!validatePrix(prixField.getText())) {
            errors.append("Prix invalide (doit être entre 0 et 10000 DT)\n");
        }
        if (!validateStatut(statutField.getText())) {
            errors.append("Statut invalide (1-50 caractères alphanumériques)\n");
        }
        if (!validateImagePath(imageField.getText())) {
            errors.append("Chemin d'image trop long (max 255 caractères)\n");
        }
        if (categorieCombo.getValue() == null) {
            errors.append("Veuillez sélectionner une catégorie\n");
        }

        if (errors.length() > 0) {
            showAlert("Erreur de validation", errors.toString());
            return false;
        }
        return true;
    }

    private Produit createProduitFromForm() {
        try {
            Produit produit = new Produit();
            produit.setTitre(titreField.getText().trim());
            produit.setDescription(descriptionArea.getText().trim());
            produit.setNomArtiste(nomArtisteField.getText().trim());
            produit.setPrix(Float.parseFloat(prixField.getText().trim()));
            produit.setStatut(statutField.getText().trim());
            produit.setCategorie(categorieCombo.getValue());
            produit.setImage(imageField.getText() != null ? imageField.getText().trim() : "");
            produit.setDateCreation(LocalDateTime.now());
            return produit;
        } catch (NumberFormatException e) {
            showNotification("Format de prix invalide", "error");
            return null;
        }
    }

    private void updateProduitFromForm(Produit produit) {
        produit.setTitre(titreField.getText().trim());
        produit.setDescription(descriptionArea.getText().trim());
        produit.setNomArtiste(nomArtisteField.getText().trim());
        try {
            produit.setPrix(Float.parseFloat(prixField.getText().trim()));
        } catch (NumberFormatException e) {
            showNotification("Format de prix invalide", "error");
            return;
        }
        produit.setStatut(statutField.getText().trim());
        produit.setCategorie(categorieCombo.getValue());
        produit.setImage(imageField.getText() != null ? imageField.getText().trim() : "");
        produit.setDateCreation(LocalDateTime.now());
    }

    private void handleEdit(Produit produit) {
        titreField.setText(produit.getTitre() != null ? produit.getTitre() : "");
        descriptionArea.setText(produit.getDescription() != null ? produit.getDescription() : "");
        nomArtisteField.setText(produit.getNomArtiste() != null ? produit.getNomArtiste() : "");
        prixField.setText(String.valueOf(produit.getPrix()));
        statutField.setText(produit.getStatut() != null ? produit.getStatut() : "");
        categorieCombo.setValue(produit.getCategorie());
        imageField.setText(produit.getImage() != null ? produit.getImage() : "");

        if (produit.getImage() != null && !produit.getImage().isEmpty()) {
            try {
                imagePreview.setImage(loadImage(produit.getImage()));
            } catch (Exception e) {
                imagePreview.setImage(null);
                logger.error("Erreur chargement image: " + e.getMessage());
            }
        } else {
            imagePreview.setImage(null);
        }
    }

    private void handleDelete(Produit produit) {
        if (produit == null || produit.getId() == null) {
            showNotification("Produit non sélectionné ou ID invalide", "error");
            return;
        }
        showConfirmation("Confirmation de suppression",
                "Êtes-vous sûr de vouloir supprimer ce produit ?",
                () -> {
                    executeAsync(() -> {
                        try {
                            produitService.deleteProduit(produit.getId());
                            Platform.runLater(() -> {
                                produits.remove(produit);
                                showNotification("Produit supprimé avec succès", "success");
                            });
                        } catch (SQLException e) {
                            Platform.runLater(() ->
                                    showNotification("Erreur lors de la suppression: " + e.getMessage(), "error"));
                        }
                    });
                });
    }

    @FXML
    private void uploadImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner une image");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.jpg", "*.jpeg", "*.png")
        );

        File selectedFile = fileChooser.showOpenDialog(produitTable.getScene().getWindow());
        if (selectedFile == null) return;

        executeAsync(() -> {
            try {
                if (selectedFile.length() > MAX_IMAGE_SIZE) {
                    Platform.runLater(() ->
                            showNotification("L'image est trop volumineuse (max 5MB)", "error"));
                    return;
                }

                String targetDir = "target/classes/esprit/tn/images/";
                Files.createDirectories(Paths.get(targetDir));

                String fileName = UUID.randomUUID() + "_" + selectedFile.getName();
                Path destinationPath = Paths.get(targetDir, fileName);

                Files.copy(selectedFile.toPath(), destinationPath, StandardCopyOption.REPLACE_EXISTING);

                String resourcePath = "/esprit/tn/images/" + fileName;
                Image previewImage = new Image(selectedFile.toURI().toString());

                Platform.runLater(() -> {
                    imageField.setText(resourcePath);
                    imagePreview.setImage(previewImage);
                    showNotification("Image téléchargée avec succès", "success");
                });
            } catch (IOException e) {
                Platform.runLater(() -> {
                    showNotification("Erreur lors du téléchargement: " + e.getMessage(), "error");
                    imagePreview.setImage(null);
                });
            }
        });
    }

    @FXML
    private void exportPDF() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exporter en PDF");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers PDF", "*.pdf"));
        fileChooser.setInitialFileName("produits_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".pdf");

        File file = fileChooser.showSaveDialog(rootPane.getScene().getWindow());
        if (file == null) return;

        executeAsync(() -> {
            try {
                generatePDF(file);
                Platform.runLater(() -> {
                    showNotification("PDF généré avec succès", "success");
                    openPDFFile(file);
                });
            } catch (Exception e) {
                Platform.runLater(() ->
                        showNotification("Erreur génération PDF: " + e.getMessage(), "error"));
            }
        });
    }

    private void generatePDF(File file) throws DocumentException, IOException {
        Document document = new Document(PageSize.A4.rotate());
        PdfWriter.getInstance(document, new FileOutputStream(file));
        document.open();

        addPDFMetadata(document);
        addPDFHeader(document);
        addPDFTable(document);
        addPDFStats(document);
        addPDFFooter(document);

        document.close();
    }

    private void addPDFMetadata(Document document) {
        document.addTitle("Liste des Produits Artistiques");
        document.addSubject("Export PDF des produits");
        document.addKeywords("produits, artistique, export");
        document.addCreator("Gestion d'Artisanat");
    }

    private void addPDFHeader(Document document) throws DocumentException {
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, BaseColor.DARK_GRAY);
        Paragraph title = new Paragraph("LISTE DES PRODUITS ARTISTIQUES", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20);
        document.add(title);

        Font infoFont = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 10, BaseColor.GRAY);
        Paragraph info = new Paragraph("Généré le: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm")) +
                " | Produits: " + produits.size(), infoFont);
        info.setAlignment(Element.ALIGN_RIGHT);
        info.setSpacingAfter(15);
        document.add(info);
    }

    private void addPDFTable(Document document) throws DocumentException {
        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10);
        table.setSpacingAfter(10);
        table.setWidths(new float[]{3, 1.5f, 2, 1.5f, 2, 1.5f});

        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.WHITE);
        String[] headers = {"Titre", "Prix (DT)", "Artiste", "Statut", "Catégorie", "Disponibilité"};
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
            cell.setBackgroundColor(new BaseColor(33, 37, 41));
            cell.setPadding(8);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);
        }

        Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
        for (Produit produit : produits) {
            table.addCell(createTableCell(produit.getTitre() != null ? produit.getTitre() : "", cellFont, Element.ALIGN_LEFT));
            table.addCell(createTableCell(String.format("%.2f", produit.getPrix()), cellFont, Element.ALIGN_RIGHT));
            table.addCell(createTableCell(produit.getNomArtiste() != null ? produit.getNomArtiste() : "", cellFont, Element.ALIGN_LEFT));
            PdfPCell statutCell = createTableCell(produit.getStatut() != null ? produit.getStatut() : "", cellFont, Element.ALIGN_CENTER);
            statutCell.setBackgroundColor(getStatusColor(produit.getStatut()));
            table.addCell(statutCell);
            String categorie = produit.getCategorie() != null && produit.getCategorie().getLibelle() != null ?
                    produit.getCategorie().getLibelle() : "Non catégorisé";
            table.addCell(createTableCell(categorie, cellFont, Element.ALIGN_LEFT));
            String disponibilite = produit.getStatut() != null && produit.getStatut().equalsIgnoreCase("disponible") ?
                    "En stock" : "Sur commande";
            table.addCell(createTableCell(disponibilite, cellFont, Element.ALIGN_CENTER));
        }

        document.add(table);
    }

    private PdfPCell createTableCell(String text, Font font, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(6);
        cell.setHorizontalAlignment(alignment);
        return cell;
    }

    private BaseColor getStatusColor(String statut) {
        if (statut == null) return BaseColor.WHITE;
        switch (statut.toLowerCase()) {
            case "disponible": return new BaseColor(220, 255, 220);
            case "en rupture": return new BaseColor(255, 220, 220);
            case "en commande": return new BaseColor(255, 255, 200);
            default: return BaseColor.WHITE;
        }
    }

    private void addPDFStats(Document document) throws DocumentException {
        PdfPTable statsTable = new PdfPTable(2);
        statsTable.setWidthPercentage(50);
        statsTable.setHorizontalAlignment(Element.ALIGN_CENTER);
        statsTable.setSpacingBefore(20);

        long nbDisponibles = produits.stream()
                .filter(p -> p.getStatut() != null && "disponible".equalsIgnoreCase(p.getStatut()))
                .count();
        double prixMoyen = produits.stream()
                .mapToDouble(Produit::getPrix)
                .average()
                .orElse(0);

        Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
        addStatRow(statsTable, "Produits total", String.valueOf(produits.size()), cellFont);
        addStatRow(statsTable, "Produits disponibles", String.valueOf(nbDisponibles), cellFont);
        addStatRow(statsTable, "Prix moyen", String.format("%.2f DT", prixMoyen), cellFont);

        document.add(statsTable);
    }

    private void addStatRow(PdfPTable table, String label, String value, Font font) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, font));
        labelCell.setBackgroundColor(new BaseColor(240, 240, 240));
        labelCell.setPadding(6);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, font));
        valueCell.setPadding(6);
        valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(valueCell);
    }

    private void addPDFFooter(Document document) throws DocumentException {
        Font footerFont = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 10, BaseColor.GRAY);
        Paragraph footer = new Paragraph("Page " + document.getPageNumber() + " | Système de Gestion d'Artisanat", footerFont);
        footer.setAlignment(Element.ALIGN_CENTER);
        footer.setSpacingBefore(20);
        document.add(footer);
    }

    private void openPDFFile(File file) {
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().open(file);
            } catch (IOException e) {
                logger.warn("Impossible d'ouvrir le PDF", e);
            }
        }
    }

    @FXML
    private void loadProduits() {
        executeAsync(() -> {
            try {
                List<Produit> loadedProduits = produitService.getAllProduits();
                Platform.runLater(() -> {
                    produits.setAll(loadedProduits);
                    showNotification("Produits chargés avec succès", "success");
                });
            } catch (SQLException e) {
                Platform.runLater(() ->
                        showNotification("Erreur chargement produits: " + e.getMessage(), "error"));
            }
        });
    }

    @FXML
    private void resetFilters() {
        searchField.clear();
        categorieFilter.setValue("Toutes");
        prixFilter.setValue(1000);
        applyFilters();
    }

    @FXML
    private void clearFields() {
        titreField.clear();
        descriptionArea.clear();
        nomArtisteField.clear();
        prixField.clear();
        statutField.clear();
        categorieCombo.setValue(null);
        imageField.clear();
        imagePreview.setImage(null);
    }

    private void executeAsync(Runnable task) {
        loadingIndicator.setVisible(true);
        loadingOverlay.setVisible(true);

        new Thread(() -> {
            try {
                task.run();
            } finally {
                Platform.runLater(() -> {
                    loadingIndicator.setVisible(false);
                    loadingOverlay.setVisible(false);
                });
            }
        }).start();
    }

    private void showNotification(String message, String type) {
        Label notification = new Label(message);
        notification.getStyleClass().add("notification-" + type);
        notification.setMaxWidth(Double.MAX_VALUE);
        notification.setAlignment(javafx.geometry.Pos.CENTER);
        notificationPane.getChildren().add(notification);

        new Timeline(new KeyFrame(Duration.seconds(5),
                e -> notificationPane.getChildren().remove(notification))
        ).play();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showConfirmation(String title, String message, Runnable onConfirm) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                onConfirm.run();
            }
        });
    }

    // Placeholder for MusicController (assuming it exists)
    @FXML
    private void toggleMusic() {
        // Implement music toggle logic if MusicController is available
        showNotification("Musique basculée", "success");
    }

    @FXML
    private void adjustVolume() {
        // Implement volume adjustment logic if MusicController is available
        showNotification("Volume ajusté", "success");
    }

    public void cleanup() {
        if (autoRefreshTimeline != null) {
            autoRefreshTimeline.stop();
        }
        if (statsStage != null && statsStage.isShowing()) {
            statsStage.close();
        }
        produits.clear();
    }

    public void setProduit(Produit data) {
        this.produit = data; // Store the provided Produit object

        // Ensure fields are initialized before setting values
        if (titreField != null && descriptionArea != null && nomArtisteField != null &&
                prixField != null && statutField != null && categorieCombo != null &&
                imageField != null && imagePreview != null) {

            // Populate form fields with the Produit data
            titreField.setText(data.getTitre() != null ? data.getTitre() : "");
            descriptionArea.setText(data.getDescription() != null ? data.getDescription() : "");
            nomArtisteField.setText(data.getNomArtiste() != null ? data.getNomArtiste() : "");
            float prix = data.getPrix(); // Assuming getPrix() returns float
            prixField.setText(prix != 0.0f ? String.valueOf(prix) : "");
            statutField.setText(data.getStatut() != null ? data.getStatut() : "");
            categorieCombo.setValue(data.getCategorie());
            imageField.setText(data.getImage() != null ? data.getImage() : "");

            // Update image preview if an image path exists
            if (data.getImage() != null && !data.getImage().isEmpty()) {
                try {
                    imagePreview.setImage(loadImage(data.getImage()));
                } catch (Exception e) {
                    logger.error("Erreur chargement image: " + e.getMessage());
                    imagePreview.setImage(null);
                    showNotification("Erreur chargement de l'image preview", "error");
                }
            } else {
                imagePreview.setImage(null);
            }

            // Reset validation styles
            titreField.setStyle("");
            nomArtisteField.setStyle("");
            prixField.setStyle("");
            statutField.setStyle("");
            imageField.setStyle("");
        } else {
            logger.warn("Some form fields are not initialized in setProduit");
        }
    }
}