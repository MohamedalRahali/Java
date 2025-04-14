package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import Services.CategorieService;
import models.Categorie;

import java.net.URL;
import java.util.ResourceBundle;

public class ListCategorieController implements Initializable {

    @FXML
    private TableView<Categorie> categorieTable;
    @FXML
    private TableColumn<Categorie, Integer> idColumn;
    @FXML
    private TableColumn<Categorie, String> libelleColumn;
    @FXML
    private TableColumn<Categorie, String> descriptionColumn;

    private final CategorieService categorieService = new CategorieService();
    private final ObservableList<Categorie> categorieList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        libelleColumn.setCellValueFactory(new PropertyValueFactory<>("libelle"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));

        categorieTable.setItems(categorieList);
        loadCategorieData();
    }

    private void loadCategorieData() {
        categorieList.clear();
        categorieList.addAll(categorieService.getAll());
    }
}
