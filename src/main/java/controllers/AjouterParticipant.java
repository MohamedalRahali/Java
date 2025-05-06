package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import model.Participant;
import service.ParticipantService;

import java.sql.SQLException;

public class AjouterParticipant {

    @FXML
    private TextField name, email;

    private final ParticipantService participantService = new ParticipantService();
    private int formationId; // Set this when opening the participant form

    public void setFormationId(int formationId) {
        this.formationId = formationId;
    }

    @FXML
    void saveParticipant(ActionEvent event) {
        if (name.getText().trim().isEmpty() || email.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Veuillez remplir tous les champs.");
            return;
        }

        try {
            Participant participant = new Participant(name.getText().trim(), email.getText().trim());
            participantService.ajouterParticipant(participant);
            participantService.inscrireParticipantFormation(formationId, participant.getId());
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Participant ajouté avec succès !");
            name.clear();
            email.clear();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur SQL", e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}