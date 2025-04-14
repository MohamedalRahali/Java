package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import Services.ProduitService;
import models.Produit;

import java.net.URL;
import java.util.Date;
import java.util.ResourceBundle;

public class ListProduitController implements Initializable {

    @FXML
    private TableView<Produit> produitTable;
    @FXML
    private TableColumn<Produit, Integer> idColumn;
    @FXML
    private TableColumn<Produit, String> titreColumn;
    @FXML
    private TableColumn<Produit, String> descriptionColumn;
    @FXML
    private TableColumn<Produit, Integer> artisteIdColumn;
    @FXML
    private TableColumn<Produit, Float> prixColumn;
    @FXML
    private TableColumn<Produit, String> statutColumn;
    @FXML
    private TableColumn<Produit, Date> dateCreationColumn;
    @FXML
    private TableColumn<Produit, Integer> categorieIdColumn;
    @FXML
    private TableColumn<Produit, String> imageColumn;

    private final ProduitService produitService = new ProduitService();
    private final ObservableList<Produit> produitList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        titreColumn.setCellValueFactory(new PropertyValueFactory<>("titre"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        artisteIdColumn.setCellValueFactory(new PropertyValueFactory<>("artisteId"));
        prixColumn.setCellValueFactory(new PropertyValueFactory<>("prix"));
        statutColumn.setCellValueFactory(new PropertyValueFactory<>("statut"));
        dateCreationColumn.setCellValueFactory(new PropertyValueFactory<>("dateDeCreation"));
        categorieIdColumn.setCellValueFactory(new PropertyValueFactory<>("categorie"));
        imageColumn.setCellValueFactory(new PropertyValueFactory<>("image"));

        produitTable.setItems(produitList);
        loadProduitData();
    }

    private void loadProduitData() {
        produitList.clear();
        produitList.addAll(produitService.getAll());
    }
}
