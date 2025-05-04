package gui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.stage.Stage;
import models.Reclamation;
import Services.ReclamationService;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.List;
import javafx.scene.control.ListCell;
import javafx.util.Callback;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;

public class MesReclamationsController implements Initializable {
    @FXML
    private ListView<Reclamation> mesReclamationsListView;

    private ObservableList<Reclamation> mesReclamations;
    private ReclamationService reclamationService;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        reclamationService = new ReclamationService();
        loadMesReclamations();
        mesReclamationsListView.setCellFactory(new Callback<ListView<Reclamation>, ListCell<Reclamation>>() {
            @Override
            public ListCell<Reclamation> call(ListView<Reclamation> listView) {
                return new CustomReclamationCell(new CustomReclamationCell.ReclamationActionHandler() {
                    @Override
                    public void onEdit(Reclamation reclamation) {
                        handleEditReclamation(reclamation);
                    }
                    @Override
                    public void onDelete(Reclamation reclamation) {
                        handleDeleteReclamation(reclamation);
                    }
                });
            }
        });
    }

    private void loadMesReclamations() {
        try {
            int currentUserId = models.CurrentUser.getCurrentUserId();
            java.util.List<Reclamation> userRecs = reclamationService.getByUserId(currentUserId);
            mesReclamations = javafx.collections.FXCollections.observableArrayList(userRecs);
            mesReclamationsListView.setItems(mesReclamations);
        } catch (Exception e) {
            showError("Erreur", "Impossible de charger vos réclamations");
        }
    }

    @FXML
    private void goToCreateReclamation(ActionEvent event) throws java.io.IOException {
        javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/create_reclamation.fxml"));
        javafx.scene.Parent root = loader.load();
        javafx.stage.Stage stage = (javafx.stage.Stage) mesReclamationsListView.getScene().getWindow();
        stage.setTitle("Créer une Réclamation");
        stage.setScene(new javafx.scene.Scene(root));
    }

    private void handleEditReclamation(Reclamation reclamation) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/edit_reclamation.fxml"));
            Parent root = loader.load();
            EditReclamationController controller = loader.getController();
            Stage dialogStage = new Stage();
            controller.setDialogStage(dialogStage);
            controller.setReclamation(reclamation);
            controller.setOnSaveCallback(() -> {
                loadMesReclamations();
            });
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setTitle("Modifier la Réclamation");
            dialogStage.setScene(new Scene(root));
            dialogStage.showAndWait();
        } catch (Exception e) {
            showError("Erreur", "Impossible d'ouvrir la fenêtre de modification.");
        }
    }

    private void handleDeleteReclamation(Reclamation reclamation) {
        Alert confirm = new Alert(AlertType.CONFIRMATION);
        confirm.setTitle("Confirmer la suppression");
        confirm.setHeaderText(null);
        confirm.setContentText("Voulez-vous vraiment supprimer cette réclamation ?");
        confirm.showAndWait().ifPresent(response -> {
            if (response == javafx.scene.control.ButtonType.OK) {
                try {
                    reclamationService.supprimer(reclamation.getId());
                    mesReclamations.remove(reclamation);
                } catch (Exception e) {
                    showError("Erreur", "Impossible de supprimer la réclamation.");
                }
            }
        });
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
