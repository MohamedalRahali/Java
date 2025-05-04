package gui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import models.Reclamation;
import Services.ReclamationService;
import models.TypeReclamation;
import Services.TypeReclamationService;
import Services.FingerprintScanner;

import java.io.IOException;
import java.util.List;
import java.util.Map; // Ajout de l'import Map

public class ReclamationMainController {

    @FXML private TextField tfTitle;
    @FXML private TextArea taDescription;

    @FXML private ComboBox<TypeReclamation> typeComboBox;
    @FXML private TableView<Reclamation> reclamationTable;
    @FXML private TableColumn<Reclamation, String> colTitle;
    @FXML private TableColumn<Reclamation, String> colDescription;
    @FXML private Button btnVerifyFingerprint;

    private final ReclamationService service = new ReclamationService();
    private final TypeReclamationService typeService = new TypeReclamationService();
    private final FingerprintScanner fingerprintScanner = new FingerprintScanner();

    @FXML
    public void initialize() {
        // Initialisation des colonnes de la table
        colTitle.setCellValueFactory(cell -> cell.getValue().titleProperty());
        colDescription.setCellValueFactory(cell -> cell.getValue().descriptionProperty());

        // Charger les types de réclamation dans le ComboBox
        List<TypeReclamation> types = typeService.getAll();
        typeComboBox.setItems(FXCollections.observableArrayList(types));

        // Configurer le bouton de vérification d'empreinte
        btnVerifyFingerprint.setOnAction(event -> handleFingerprintVerification());

        loadReclamations();
    }

    private void loadReclamations() {
    ObservableList<Reclamation> reclamationList = FXCollections.observableArrayList(service.getAll());
    reclamationTable.setItems(reclamationList);

    // Calcul des statistiques par type
    // Utilisation de getTypeName() pour obtenir le nom du type de réclamation
    Map<String, Long> stats = reclamationList.stream()
        .collect(java.util.stream.Collectors.groupingBy(
            Reclamation::getTypeName,
            java.util.stream.Collectors.counting()
        ));

    // Construction du message de statistiques
    StringBuilder statsMsg = new StringBuilder("Statistiques par type de réclamation :\n");
    for (Map.Entry<String, Long> entry : stats.entrySet()) {
        statsMsg.append("- ").append(entry.getKey()).append(" : ").append(entry.getValue()).append("\n");
    }

    // Affichage dans une boîte de dialogue d'information seulement si la liste n'est pas vide
    if (!reclamationList.isEmpty()) {
        Alert statsAlert = new Alert(Alert.AlertType.INFORMATION);
        statsAlert.setTitle("Statistiques");
        statsAlert.setHeaderText("Nombre de réclamations par type");
        statsAlert.setContentText(statsMsg.toString());
        statsAlert.show();
    }
}

    @FXML
    private void ajouterReclamation() {
        try {
            // Validate all fields are filled
            if (tfTitle.getText().isEmpty() || taDescription.getText().isEmpty() || typeComboBox.getValue() == null) {
                showAlert("Erreur", "Veuillez remplir tous les champs et sélectionner un type de réclamation!");
                return;
            }

            // Create new reclamation 
            Reclamation r = new Reclamation(
                    tfTitle.getText().trim(),
                    taDescription.getText().trim(),
                    typeComboBox.getValue()
    
            );

            // Si le modèle Reclamation n'a pas de champ capacité, tu peux le stocker ailleurs ou l'ajouter au modèle
    

            service.ajouter(r);
            loadReclamations();
            clearFields();

        } catch (Exception e) {
            showAlert("Erreur", "Une erreur est survenue: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void modifierReclamation() {
        try {
            Reclamation selected = reclamationTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert("Erreur", "Veuillez sélectionner une réclamation à modifier!");
                return;
            }
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/modification_reclamation.fxml"));
            Parent root = loader.load();
            ModifyReclamationController controller = loader.getController();
            controller.setReclamationToEdit(selected);
            Stage stage = (Stage) tfTitle.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Modifier Réclamation");
        } catch (IOException e) {
            showAlert("Erreur", "Impossible de charger la page de modification: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void supprimerReclamation() {
        try {
            Reclamation selected = reclamationTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert("Erreur", "Veuillez sélectionner une réclamation à supprimer!");
                return;
            }

            // Confirmation de suppression
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation");
            alert.setHeaderText("Suppression de réclamation");
            alert.setContentText("Voulez-vous vraiment supprimer cette réclamation ?");

            if (alert.showAndWait().get() == ButtonType.OK) {
                service.supprimer(selected.getId());
                loadReclamations();
                clearFields();
            }

        } catch (Exception e) {
            showAlert("Erreur", "Une erreur est survenue: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleFingerprintVerification() {
        try {
            // Vérifier si une réclamation est sélectionnée
            Reclamation selected = reclamationTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert("Erreur", "Veuillez sélectionner une réclamation!");
                return;
            }

            // Afficher une boîte de dialogue pour guider l'utilisateur
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Vérification d'empreinte digitale");
            alert.setHeaderText("Veuillez placer votre doigt sur le scanner");
            alert.setContentText("Le scanner est prêt à lire votre empreinte digitale.");
            alert.showAndWait();

            // Simuler la vérification d'empreinte (à remplacer par la vraie vérification)
            boolean verified = fingerprintScanner.verifyFingerprint();
            
            if (verified) {
                // Mettre à jour la réclamation avec la vérification
                selected.setFingerprintVerified(true);
                service.update(selected);
                showAlert("Succès", "Empreinte digitale vérifiée avec succès!");
            } else {
                showAlert("Erreur", "Échec de la vérification d'empreinte digitale");
            }
        } catch (Exception e) {
            showAlert("Erreur", "Une erreur est survenue: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void clearFields() {
        tfTitle.clear();
        taDescription.clear();
    
        typeComboBox.setValue(null);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}