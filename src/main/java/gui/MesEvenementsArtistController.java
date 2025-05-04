package gui;

import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Button;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.event.ActionEvent;
import javafx.scene.control.TableCell;
import javafx.util.Callback;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.layout.HBox;
import javafx.beans.property.SimpleStringProperty;
import models.Event;
import Services.EventService;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class MesEvenementsArtistController {
    @FXML private TableView<models.Event> evenementsTable;
    @FXML private TableColumn<models.Event, String> titreColumn;
    @FXML private TableColumn<models.Event, String> dateColumn;
    @FXML private TableColumn<models.Event, String> lieuColumn;
    @FXML private TableColumn<models.Event, String> descriptionColumn;
    @FXML private TableColumn<models.Event, String> dureeColumn;
    @FXML private TableColumn<models.Event, Integer> placesColumn;
    @FXML private TableColumn<models.Event, String> typeColumn;
    @FXML private TableColumn<models.Event, String> statusColumn;
    @FXML private TableColumn<models.Event, Void> actionsColumn;

    private final Services.EventService eventService = new Services.EventService();

    @FXML
    public void initialize() {
        titreColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTitle()));
        dateColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDateString()));
        lieuColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getLieux()));
        descriptionColumn.setCellValueFactory(cellData -> cellData.getValue().descriptionProperty());
        dureeColumn.setCellValueFactory(cellData -> cellData.getValue().dureeProperty());
        placesColumn.setCellValueFactory(cellData -> cellData.getValue().nbPlacesProperty().asObject());
        typeColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
            cellData.getValue().getTypeEvent() != null ? cellData.getValue().getTypeEvent().getName() : ""
        ));
        statusColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getStatus()));
        refreshTable();
        addActionsToTable();
    }

    private void refreshTable() {
        int artistId = models.CurrentUser.getCurrentUserId();
        ObservableList<models.Event> data = javafx.collections.FXCollections.observableArrayList(eventService.getByArtistId(artistId));
        evenementsTable.setItems(data);
    }

    private void addActionsToTable() {
        actionsColumn.setCellFactory(new Callback<TableColumn<models.Event, Void>, TableCell<models.Event, Void>>() {
            @Override
            public TableCell<models.Event, Void> call(final TableColumn<models.Event, Void> param) {
                final TableCell<models.Event, Void> cell = new TableCell<models.Event, Void>() {
                    private final Button updateBtn = new Button("Modifier");
                    private final Button deleteBtn = new Button("Supprimer");
                    {
                        updateBtn.setOnAction((ActionEvent event) -> {
                            models.Event e = getTableView().getItems().get(getIndex());
                            try {
                                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/modification_evenement_artist.fxml"));
                                Parent root = loader.load();
                                gui.ModifyEvenementArtistController controller = loader.getController();
                                controller.setEventToEdit(e);
                                Stage stage = new Stage();
                                stage.setTitle("Modifier l'Événement");
                                stage.setScene(new Scene(root));
                                // Listen for window close to refresh table
                                stage.setOnHiding(ev -> refreshTable());
                                stage.show();
                            } catch (IOException ex) {
                                Alert alert = new Alert(AlertType.ERROR, "Erreur lors de l'ouverture de la fenêtre de modification: " + ex.getMessage());
                                alert.showAndWait();
                            }
                        });
                        updateBtn.setStyle("-fx-background-color: #43a047; -fx-text-fill: white; -fx-background-radius: 10; -fx-margin-right: 4px;");
                        deleteBtn.setOnAction((ActionEvent event) -> {
                            models.Event e = getTableView().getItems().get(getIndex());
                            // TODO: Remove from service and refresh table
                            Alert alert = new Alert(AlertType.INFORMATION, "Suppression de l'événement: " + e.getTitle());
                            alert.showAndWait();
                        });
                        deleteBtn.setStyle("-fx-background-color: #e53935; -fx-text-fill: white; -fx-background-radius: 10;");
                    }
                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            HBox hbox = new HBox(6, updateBtn, deleteBtn);
                            setGraphic(hbox);
                        }
                    }
                };
                return cell;
            }
        });
    }

    // Dummy Event class for demonstration
    public static class Event {
        private String titre;
        private String date;
        private String lieu;
        public Event(String titre, String date, String lieu) {
            this.titre = titre;
            this.date = date;
            this.lieu = lieu;
        }
        public String getTitre() { return titre; }
        public String getDate() { return date; }
        public String getLieu() { return lieu; }
    }
}
