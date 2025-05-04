package gui;

import Services.ReclamationService;
import Services.TypeReclamationService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import models.Reclamation;
import models.TypeReclamation;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class EditReclamationController implements Initializable {
    @FXML private TextField titleField;
    @FXML private TextArea descriptionArea;
    @FXML private ComboBox<TypeReclamation> typeComboBox;

    private Reclamation reclamation;
    private final ReclamationService reclamationService = new ReclamationService();
    private final TypeReclamationService typeService = new TypeReclamationService();
    private Stage dialogStage;
    private Runnable onSaveCallback;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        List<TypeReclamation> types = typeService.getAll();
        typeComboBox.setItems(FXCollections.observableArrayList(types));
    }

    public void setDialogStage(Stage stage) {
        this.dialogStage = stage;
    }

    public void setReclamation(Reclamation reclamation) {
        this.reclamation = reclamation;
        titleField.setText(reclamation.getTitle());
        descriptionArea.setText(reclamation.getDescription());
        typeComboBox.setValue(reclamation.getTypeReclamation());
    }

    public void setOnSaveCallback(Runnable callback) {
        this.onSaveCallback = callback;
    }

    @FXML
    private void handleSave() {
        if (titleField.getText().isEmpty() || descriptionArea.getText().isEmpty() || typeComboBox.getValue() == null) {
            showAlert("Veuillez remplir tous les champs et sélectionner un type de réclamation.");
            return;
        }
        reclamation.setTitle(titleField.getText());
        reclamation.setDescription(descriptionArea.getText());
        reclamation.setTypeReclamation(typeComboBox.getValue());
        try {
            reclamationService.modifier(reclamation);
            if (onSaveCallback != null) onSaveCallback.run();
            dialogStage.close();
        } catch (Exception e) {
            showAlert("Erreur lors de la sauvegarde: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        dialogStage.close();
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Attention");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
