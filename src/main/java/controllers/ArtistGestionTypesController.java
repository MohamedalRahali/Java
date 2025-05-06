package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.Type_b;
import models.CurrentUser;
import service.Type_bCRUD;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import java.io.IOException;
import java.sql.SQLException;

public class ArtistGestionTypesController {
    @FXML private ListView<Type_b> typeListView;
    @FXML private TextField typeField;
    private final Type_bCRUD typeCRUD = new Type_bCRUD();

    @FXML
    public void initialize() {
        loadTypes();
        typeListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                typeField.setText(newVal.getLibelle());
            }
        });
    }

    private void loadTypes() {
        try {
            int artistId = CurrentUser.getCurrentUserId();
            typeListView.getItems().setAll(typeCRUD.getTypesByArtist(artistId));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAddType() {
        String typeName = typeField.getText().trim();
        if (!typeName.isEmpty()) {
            try {
                Type_b type = new Type_b(typeName);
                type.setArtistId(CurrentUser.getCurrentUserId());
                typeCRUD.add(type);
                typeField.clear();
                loadTypes();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleEditType() {
        Type_b selected = typeListView.getSelectionModel().getSelectedItem();
        String newLibelle = typeField.getText().trim();
        if (selected != null && selected.getArtistId() == CurrentUser.getCurrentUserId()) {
            if (newLibelle.length() < 3) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erreur");
                alert.setHeaderText(null);
                alert.setContentText("Le libellé doit avoir au moins 3 caractères.");
                alert.showAndWait();
                return;
            }
            try {
                selected.setLibelle(newLibelle);
                typeCRUD.update(selected);
                typeField.clear();
                loadTypes();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleDeleteType() {
        Type_b selected = typeListView.getSelectionModel().getSelectedItem();
        if (selected != null && selected.getArtistId() == CurrentUser.getCurrentUserId()) {
            try {
                typeCRUD.delete(selected.getId());
                loadTypes();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
