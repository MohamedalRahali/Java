package gui;

import Services.EventService;
import Services.TypeEventService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import models.Event;
import models.TypeEvent;

import java.io.IOException;
import java.util.Optional;

public class EventViewController {
    @FXML private TableView<Event> eventTable;
    @FXML private TableColumn<Event, String> titleColumn;
    @FXML private TableColumn<Event, String> dateColumn;
    @FXML private TableColumn<Event, String> descriptionColumn;
    @FXML private TableColumn<Event, String> locationColumn;
    @FXML private TableColumn<Event, String> statusColumn;
    @FXML private TableColumn<Event, String> durationColumn;
    @FXML private TableColumn<Event, Integer> availableSeatsColumn;
    @FXML private TableColumn<Event, String> typeColumn;
    
    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterTypeComboBox;
    @FXML private ComboBox<String> filterStatusComboBox;

    private final EventService eventService = new EventService();
    private final TypeEventService typeEventService = new TypeEventService();
    private ObservableList<Event> allEvents;
    private FilteredList<Event> filteredEvents;

    @FXML
    public void initialize() {
        setupTableColumns();
        setupFilters();
        loadEvents();
        setupSearch();
    }

    private void setupTableColumns() {
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        locationColumn.setCellValueFactory(new PropertyValueFactory<>("lieux"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        durationColumn.setCellValueFactory(new PropertyValueFactory<>("duree"));
        availableSeatsColumn.setCellValueFactory(new PropertyValueFactory<>("nb_place_dispo"));
        typeColumn.setCellValueFactory(cellData -> {
            Event event = cellData.getValue();
            TypeEvent type = event.getTypeEvent();
            return type != null ? type.nameProperty() : new SimpleStringProperty("");
        });
    }

    private void setupFilters() {
        // Setup status filter
        filterStatusComboBox.setItems(FXCollections.observableArrayList(
            "All",
            "Upcoming",
            "In Progress",
            "Completed",
            "Cancelled"
        ));
        filterStatusComboBox.setValue("All");

        // Setup type filter
        ObservableList<String> types = FXCollections.observableArrayList("All");
        types.addAll(typeEventService.getAll().stream()
                .map(TypeEvent::getName)
                .toList());
        filterTypeComboBox.setItems(types);
        filterTypeComboBox.setValue("All");

        // Add listeners
        filterStatusComboBox.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        filterTypeComboBox.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
    }

    private void loadEvents() {
        allEvents = FXCollections.observableArrayList(eventService.getAll());
        filteredEvents = new FilteredList<>(allEvents, p -> true);
        eventTable.setItems(filteredEvents);
    }

    private void setupSearch() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> applyFilters());
    }

    private void applyFilters() {
        String searchText = searchField.getText().toLowerCase();
        String statusFilter = filterStatusComboBox.getValue();
        String typeFilter = filterTypeComboBox.getValue();

        filteredEvents.setPredicate(event -> {
            if (!searchText.isEmpty()) {
                boolean matchesSearch = event.getTitle().toLowerCase().contains(searchText) ||
                        event.getDescription().toLowerCase().contains(searchText) ||
                        event.getLieux().toLowerCase().contains(searchText);
                if (!matchesSearch) return false;
            }

            if (!"All".equals(statusFilter) && !event.getStatus().equals(statusFilter)) {
                return false;
            }

            if (!"All".equals(typeFilter)) {
                TypeEvent type = event.getTypeEvent();
                if (type == null || !type.getName().equals(typeFilter)) {
                    return false;
                }
            }

            return true;
        });
    }

    @FXML
    private void handleSearch() {
        applyFilters();
    }

    @FXML
    private void handleEdit() {
        Event selectedEvent = eventTable.getSelectionModel().getSelectedItem();
        if (selectedEvent == null) {
            showAlert("Please select an event to edit.", Alert.AlertType.WARNING);
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/modification_event.fxml"));
            Parent root = loader.load();

            ModifyEventController controller = loader.getController();
            controller.setEventToEdit(selectedEvent);

            Stage stage = new Stage();
            stage.setTitle("Edit Event");
            stage.setScene(new Scene(root));
            stage.show();

            closeCurrentWindow();
        } catch (Exception e) {
            e.printStackTrace(); // Print the full stack trace for debugging
            showAlert("Error opening edit window: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleDelete() {
        Event selectedEvent = eventTable.getSelectionModel().getSelectedItem();
        if (selectedEvent == null) {
            showAlert("Please select an event to delete.", Alert.AlertType.WARNING);
            return;
        }

        Optional<ButtonType> result = showConfirmation(
            "Delete Event",
            "Are you sure you want to delete this event?",
            "This action cannot be undone."
        );

        if (result.isPresent() && result.get() == ButtonType.OK) {
            eventService.supprimer(selectedEvent.getId());
            loadEvents();
        }
    }

    @FXML
    private void goBackToHome() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/Home.fxml"));
            openNewWindow(root, "Home");
        } catch (IOException e) {
            showAlert("Error returning to home: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void goToCreateEvent() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/create_event.fxml"));
            openNewWindow(root, "Create Event");
        } catch (IOException e) {
            showAlert("Error opening create event window: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void goToTypeEventManagement() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/type_event.fxml"));
            openNewWindow(root, "Event Types");
        } catch (IOException e) {
            showAlert("Error opening event types window: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void goToStatistics() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/statistics_view.fxml"));
            openNewWindow(root, "Event Statistics");
        } catch (IOException e) {
            showAlert("Error opening statistics window: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void openNewWindow(Parent root, String title) {
        Stage stage = new Stage();
        stage.setTitle(title);
        stage.setScene(new Scene(root));
        stage.show();
        closeCurrentWindow();
    }

    private void closeCurrentWindow() {
        ((Stage) eventTable.getScene().getWindow()).close();
    }

    private void showAlert(String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(type.toString());
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private Optional<ButtonType> showConfirmation(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        return alert.showAndWait();
    }
}
