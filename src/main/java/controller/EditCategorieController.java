package controller;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import Services.CategorieService;
import models.Categorie;

public class EditCategorieController {

    @FXML
    private TextField idField;
    @FXML
    private TextField libelleField;
    @FXML
    private TextField descriptionField;

    private final CategorieService categorieService = new CategorieService();

    @FXML
    private void handleUpdateCategorie() {
        int id = Integer.parseInt(idField.getText());
        String libelle = libelleField.getText();
        String description = descriptionField.getText();

        Categorie categorie = new Categorie(id, libelle, description);
        categorieService.update(categorie);

        // Ajoutez ici le code pour gérer la réponse ou rediriger l'utilisateur
    }
}
