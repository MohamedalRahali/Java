package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import model.Formation;
import service.FormationService;
import javafx.collections.ObservableList;

public class ModifierFormation {

    @FXML
    private TextField titreField;

    @FXML
    private TextArea descriptionField;

    @FXML
    private TextField prixField;

    @FXML
    private TextField placesDisponiblesField;

    private Formation formationToModify;
    private ObservableList<Formation> formationsList;

    private final FormationService formationService = new FormationService();

    @FXML
    public void initialize() {
        // Pré-remplir les champs avec les données de la formation
        if (formationToModify != null) {
            titreField.setText(formationToModify.getTitre());
            descriptionField.setText(formationToModify.getDescription());
            prixField.setText(String.valueOf(formationToModify.getPrix()));
            placesDisponiblesField.setText(String.valueOf(formationToModify.getPlacesDisponibles()));
        }
    }

    public void setFormation(Formation formation, ObservableList<Formation> formationsList) {
        this.formationToModify = formation;
        this.formationsList = formationsList;
    }

    @FXML
    public void saveChanges() {
        // Récupérer les nouvelles données et les mettre à jour
        String titre = titreField.getText();
        String description = descriptionField.getText();
        double prix = Double.parseDouble(prixField.getText());
        int placesDisponibles = Integer.parseInt(placesDisponiblesField.getText());

        // Créer un nouvel objet Formation avec les modifications
        formationToModify.setTitre(titre);
        formationToModify.setDescription(description);
        formationToModify.setPrix(prix);
        formationToModify.setPlacesDisponibles(placesDisponibles);

        // Appeler la méthode de mise à jour dans le service
        try {
            formationService.update(formationToModify);
        } catch (java.sql.SQLException e) {
            showAlert("Erreur SQL", "Erreur lors de la mise à jour de la formation : " + e.getMessage());
            return;
        }

        // Mettre à jour la ListView avec les nouvelles données
        int index = formationsList.indexOf(formationToModify);
        formationsList.set(index, formationToModify); // Mettre à jour l'élément modifié dans la liste

        // Fermer la fenêtre de modification
        ((javafx.stage.Stage) titreField.getScene().getWindow()).close();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
