package gui;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import models.Event;
import Services.EventService;

import java.sql.Date;

public class ModifyEventController {

    @FXML private TextField titleField;
    @FXML private DatePicker datePicker;
    @FXML private TextArea descriptionArea;
    @FXML private TextField lieuxField;
    @FXML private TextField statusField;
    @FXML private TextField dureeField;
    @FXML private Spinner<Integer> nbPlaceSpinner;

    private Event eventToEdit;
    private final EventService service = new EventService();

    public void setEventToEdit(Event event) {
        this.eventToEdit = event;

        // Remplir les champs avec les données existantes
        titleField.setText(event.getTitle());
        datePicker.setValue(event.getDate().toLocalDate());
        descriptionArea.setText(event.getDescription());
        lieuxField.setText(event.getLieux());
        statusField.setText(event.getStatus());
        dureeField.setText(event.getDuree());
        nbPlaceSpinner.getValueFactory().setValue(event.getNb_place_dispo());
    }

    @FXML
    private void updateEvent() {
        if (eventToEdit == null) return;

        // Mettre à jour les données de l'objet
        eventToEdit.setTitle(titleField.getText());
        eventToEdit.setDate(Date.valueOf(datePicker.getValue()));
        eventToEdit.setDescription(descriptionArea.getText());
        eventToEdit.setLieux(lieuxField.getText());
        eventToEdit.setStatus(statusField.getText());
        eventToEdit.setDuree(dureeField.getText());
        eventToEdit.setNb_place_dispo(nbPlaceSpinner.getValue());

        // Mise à jour dans la base de données
        service.modifier(eventToEdit);

        // Fermer la fenêtre
        Stage stage = (Stage) titleField.getScene().getWindow();
        stage.close();
    }
}
