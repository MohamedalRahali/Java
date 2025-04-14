package controller;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import Services.CategorieService;
import models.Categorie;

public class AddCategorieController {

    @FXML
    private TextField libelleField;
    @FXML
    private TextField descriptionField;

    private final CategorieService categorieService = new CategorieService();

    @FXML
    private void handleAddCategorie() {
        String libelle = libelleField.getText();
        String description = descriptionField.getText();

        Categorie categorie = new Categorie(libelle, description);
        categorieService.add(categorie);

        // Ajoutez ici le code pour gérer la réponse ou rediriger l'utilisateur
    }
}
