package gui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import models.TypeReclamation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import Services.TypeReclamationService;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class TypeReclamationViewController implements Initializable {
    private static final Logger logger = LoggerFactory.getLogger(TypeReclamationViewController.class);
    private TypeReclamationService typeReclamationService;
    private ObservableList<TypeReclamation> typeList;
    private TypeReclamation selectedType;

    @FXML private TextField nameField;
    @FXML private TableView<TypeReclamation> typeTableView;
    @FXML private TableColumn<TypeReclamation, Integer> idColumn;
    @FXML private TableColumn<TypeReclamation, String> nameColumn;
    @FXML private Button saveButton;
    @FXML private Button clearButton;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            logger.info("Initialisation du contrôleur de types de réclamation");
            
            // Initialisation du service
            typeReclamationService = new TypeReclamationService();
            
            // Configuration de la TableView
            setupTableView();
            
            // Chargement des types
            loadTypes();
            
            logger.info("Initialisation terminée avec succès");
        } catch (Exception e) {
            logger.error("Erreur lors de l'initialisation: {}", e.getMessage(), e);
            showError("Erreur", "Impossible d'initialiser la page: " + e.getMessage());
        }
    }

    private void setupTableView() {
        try {
            logger.info("Configuration de la TableView");
            
            // Configuration des colonnes
            idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
            nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
            
            // Initialisation de la liste observable
            typeList = FXCollections.observableArrayList();
            typeTableView.setItems(typeList);
            
            // Sélection d'une ligne
            typeTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
                if (newSelection != null) {
                    selectedType = newSelection;
                    nameField.setText(selectedType.getName());
                    saveButton.setText("💾 Modifier");
                }
            });
            
            logger.info("Configuration de la TableView terminée");
        } catch (Exception e) {
            logger.error("Erreur lors de la configuration de la TableView: {}", e.getMessage(), e);
            showError("Erreur", "Impossible de configurer la table: " + e.getMessage());
        }
    }

    private void loadTypes() {
        try {
            logger.info("Chargement des types de réclamation");
            typeList.clear();
            typeList.addAll(typeReclamationService.getAll());
            logger.info("{} types chargés", typeList.size());
        } catch (Exception e) {
            logger.error("Erreur lors du chargement des types: {}", e.getMessage(), e);
            showError("Erreur", "Impossible de charger les types: " + e.getMessage());
        }
    }

    @FXML
    private void handleSave() {
        try {
            String name = nameField.getText().trim();
            if (name.isEmpty()) {
                showError("Erreur", "Le nom du type ne peut pas être vide");
                return;
            }

            if (selectedType == null) {
                // Création d'un nouveau type
                TypeReclamation newType = new TypeReclamation();
                newType.setName(name);
                typeReclamationService.ajouter(newType);
                logger.info("Nouveau type créé: {}", name);
            } else {
                // Modification d'un type existant
                selectedType.setName(name);
                typeReclamationService.modifier(selectedType);
                logger.info("Type modifié: {}", name);
            }

            // Rafraîchissement de la liste
            loadTypes();
            handleClear();
            
        } catch (Exception e) {
            logger.error("Erreur lors de la sauvegarde: {}", e.getMessage(), e);
            showError("Erreur", "Impossible de sauvegarder: " + e.getMessage());
        }
    }

    @FXML
    private void handleClear() {
        nameField.clear();
        selectedType = null;
        saveButton.setText("💾 Enregistrer");
        typeTableView.getSelectionModel().clearSelection();
    }

    @FXML
    private void handleEdit() {
        selectedType = typeTableView.getSelectionModel().getSelectedItem();
        if (selectedType == null) {
            showError("Attention", "Veuillez sélectionner un type à modifier");
            return;
        }
        nameField.setText(selectedType.getName());
        saveButton.setText("💾 Modifier");
    }

    @FXML
    private void handleDelete() {
        try {
            selectedType = typeTableView.getSelectionModel().getSelectedItem();
            if (selectedType == null) {
                showError("Attention", "Veuillez sélectionner un type à supprimer");
                return;
            }

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation de suppression");
            alert.setHeaderText("Supprimer le type");
            alert.setContentText("Êtes-vous sûr de vouloir supprimer le type '" + selectedType.getName() + "' ?");
            
            if (alert.showAndWait().get() == ButtonType.OK) {
                typeReclamationService.supprimer(selectedType.getId());
                logger.info("Type supprimé: {}", selectedType.getName());
                loadTypes();
                handleClear();
            }
        } catch (Exception e) {
            logger.error("Erreur lors de la suppression: {}", e.getMessage(), e);
            showError("Erreur", "Impossible de supprimer: " + e.getMessage());
        }
    }

    @FXML
    private void goBackToHome() throws IOException {
        logger.info("Retour à la page d'accueil");
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/Home.fxml"));
        Stage stage = new Stage();
        stage.setTitle("Accueil");
        stage.setScene(new Scene(root));
        stage.show();

        // Fermer la fenêtre actuelle
        ((Stage) typeTableView.getScene().getWindow()).close();
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 