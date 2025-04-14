package controller;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import Services.ProduitService;
import models.Produit;
import models.Categorie;

import java.util.Date;

public class EditProduitController {

    @FXML
    private TextField idField;
    @FXML
    private TextField titreField;
    @FXML
    private TextField descriptionField;
    @FXML
    private TextField artisteIdField;
    @FXML
    private TextField prixField;
    @FXML
    private TextField statutField;
    @FXML
    private TextField dateCreationField;
    @FXML
    private TextField categorieIdField;
    @FXML
    private TextField imageField;

    private final ProduitService produitService = new ProduitService();

    @FXML
    private void handleUpdateProduit() {
        int id = Integer.parseInt(idField.getText());
        String titre = titreField.getText();
        String description = descriptionField.getText();
        int artisteId = Integer.parseInt(artisteIdField.getText());
        float prix = Float.parseFloat(prixField.getText());
        String statut = statutField.getText();
        Date dateCreation = new Date(); // Vous pouvez ajuster cela selon vos besoins
        int categorieId = Integer.parseInt(categorieIdField.getText());
        String image = imageField.getText();

        Produit produit = new Produit(id, titre, description, artisteId, prix, statut, dateCreation, new Categorie(categorieId, ""), image);
        produitService.update(produit);

        // Ajoutez ici le code pour gérer la réponse ou rediriger l'utilisateur
    }
}
