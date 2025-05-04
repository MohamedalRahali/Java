package gui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.scene.layout.StackPane;
import models.Event;
import models.TypeEvent;
import Services.EventService;
import Services.TypeEventService;
import netscape.javascript.JSObject;

import java.io.IOException;
import java.sql.Date;
import java.time.LocalDate;
import java.util.regex.Pattern;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class CreateEventController {
    @FXML private TextField titleField;
    @FXML private DatePicker datePicker;
    @FXML private TextArea descriptionField;
    @FXML private TextField adresseField;
    @FXML private TextField villeField;
    @FXML private ChoiceBox<String> statusChoiceBox;
    @FXML private TextField dureeField;
    @FXML private Spinner<Integer> nbPlacesSpinner;
    @FXML private ComboBox<TypeEvent> typeEventComboBox;
    @FXML private WebView mapView;
    @FXML private StackPane mapContainer;
    @FXML private Label titleErrorLabel;
    @FXML private Label dateErrorLabel;
    @FXML private Label descriptionErrorLabel;
    @FXML private Label locationErrorLabel;
    @FXML private Label statusErrorLabel;
    @FXML private Label dureeErrorLabel;
    @FXML private Label nbPlacesErrorLabel;
    @FXML private Label typeEventErrorLabel;

    private final EventService eventService = new EventService();
    private final TypeEventService typeEventService = new TypeEventService();
    private double currentLat = 36.8065;  // Default to Tunisia
    private double currentLng = 10.1815;

    @FXML
    public void initialize() {
        // Initialize status choices
        statusChoiceBox.getItems().addAll("En cours", "Terminé", "Annulé");
        
        // Initialize number of places spinner
        SpinnerValueFactory<Integer> valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 1000, 50);
        nbPlacesSpinner.setValueFactory(valueFactory);
        
        // Load event types
        typeEventComboBox.getItems().addAll(typeEventService.getAll());

        // Initialize map
        initializeMap();

        // Add validation listeners
        titleField.textProperty().addListener((obs, oldVal, newVal) -> validateTitle());
        datePicker.valueProperty().addListener((obs, oldVal, newVal) -> validateDate());
        descriptionField.textProperty().addListener((obs, oldVal, newVal) -> validateDescription());
        adresseField.textProperty().addListener((obs, oldVal, newVal) -> validateLocation());
        villeField.textProperty().addListener((obs, oldVal, newVal) -> validateLocation());
        statusChoiceBox.valueProperty().addListener((obs, oldVal, newVal) -> validateStatus());
        dureeField.textProperty().addListener((obs, oldVal, newVal) -> validateDuration());
        nbPlacesSpinner.valueProperty().addListener((obs, oldVal, newVal) -> validateNbPlaces());
        typeEventComboBox.valueProperty().addListener((obs, oldVal, newVal) -> validateTypeEvent());
    }

    private void initializeMap() {
        WebEngine engine = mapView.getEngine();
        engine.setJavaScriptEnabled(true);

        // Load the map HTML
        String mapUrl = getClass().getResource("/html/map.html").toExternalForm();
        engine.load(mapUrl);

        // Add JavaScript bridge
        engine.getLoadWorker().stateProperty().addListener((obs, old, newState) -> {
            if (newState == javafx.concurrent.Worker.State.SUCCEEDED) {
                JSObject window = (JSObject) engine.executeScript("window");
                window.setMember("app", new JavaBridge());
            }
        });
    }

    public class JavaBridge {
        public void onMapReady() {
            System.out.println("Map is ready");
        }

        public void onMapClick(double lat, double lng) {
            if (isValidCoordinate(lat, lng)) {
                currentLat = lat;
                currentLng = lng;
                System.out.println("Location selected: " + lat + ", " + lng);
            }
        }
    }

    private boolean isValidCoordinate(double lat, double lng) {
        return !Double.isNaN(lat) && !Double.isNaN(lng)
                && lat >= 30.0 && lat <= 38.0  // Tunisia latitude range
                && lng >= 7.0 && lng <= 12.0;  // Tunisia longitude range
    }

    @FXML
    private void handleSearchLocation() {
        String address = adresseField.getText();
        String city = villeField.getText();
        
        if (address.isEmpty() && city.isEmpty()) {
            locationErrorLabel.setText("Please enter an address or city");
            return;
        }
        
        String searchQuery = address + ", " + city + ", Tunisia";
        try {
            String encodedQuery = URLEncoder.encode(searchQuery, StandardCharsets.UTF_8.toString());
            String nominatimUrl = String.format("https://nominatim.openstreetmap.org/search?q=%s&format=json&limit=1", encodedQuery);
            
            WebEngine engine = mapView.getEngine();
            String script = String.format("""
                fetch('%s')
                    .then(response => response.json())
                    .then(data => {
                        if (data && data.length > 0) {
                            const lat = parseFloat(data[0].lat);
                            const lon = parseFloat(data[0].lon);
                            setLocation(lat, lon);
                            app.onMapClick(lat, lon);
                        }
                    });
            """, nominatimUrl);
            
            engine.executeScript(script);
        } catch (Exception e) {
            locationErrorLabel.setText("Error searching location");
            e.printStackTrace();
        }
    }

    private boolean validateTitle() {
        String title = titleField.getText().trim();
        if (title.isEmpty()) {
            titleErrorLabel.setText("Title is required");
            return false;
        }
        if (title.length() < 3) {
            titleErrorLabel.setText("Title must be at least 3 characters");
            return false;
        }
        titleErrorLabel.setText("");
        return true;
    }

    private boolean validateDate() {
        LocalDate date = datePicker.getValue();
        if (date == null) {
            dateErrorLabel.setText("Date is required");
            return false;
        }
        if (date.isBefore(LocalDate.now())) {
            dateErrorLabel.setText("Date cannot be in the past");
            return false;
        }
        dateErrorLabel.setText("");
        return true;
    }

    private boolean validateDescription() {
        String description = descriptionField.getText().trim();
        if (description.isEmpty()) {
            descriptionErrorLabel.setText("Description is required");
            return false;
        }
        if (description.length() < 10) {
            descriptionErrorLabel.setText("Description must be at least 10 characters");
            return false;
        }
        descriptionErrorLabel.setText("");
        return true;
    }

    private boolean validateLocation() {
        String address = adresseField.getText().trim();
        String city = villeField.getText().trim();
        if (address.isEmpty() || city.isEmpty()) {
            locationErrorLabel.setText("Both address and city are required");
            return false;
        }
        locationErrorLabel.setText("");
        return true;
    }

    private boolean validateStatus() {
        String status = statusChoiceBox.getValue();
        if (status == null || status.isEmpty()) {
            statusErrorLabel.setText("Status is required");
            return false;
        }
        statusErrorLabel.setText("");
        return true;
    }

    private boolean validateDuration() {
        String duration = dureeField.getText().trim();
        if (duration.isEmpty()) {
            dureeErrorLabel.setText("Duration is required");
            return false;
        }
        if (!Pattern.matches("^\\d+h\\d*$", duration)) {
            dureeErrorLabel.setText("Format must be like '2h' or '2h30'");
            return false;
        }
        dureeErrorLabel.setText("");
        return true;
    }

    private boolean validateNbPlaces() {
        Integer nbPlaces = nbPlacesSpinner.getValue();
        if (nbPlaces == null || nbPlaces < 1) {
            nbPlacesErrorLabel.setText("Number of places must be at least 1");
            return false;
        }
        nbPlacesErrorLabel.setText("");
        return true;
    }

    private boolean validateTypeEvent() {
        TypeEvent type = typeEventComboBox.getValue();
        if (type == null) {
            typeEventErrorLabel.setText("Event type is required");
            return false;
        }
        typeEventErrorLabel.setText("");
        return true;
    }


    @FXML
    private void createEvent() {
        if (!validateAll()) {
            return;
        }

        Event event = new Event();
        event.setTitle(titleField.getText().trim());
        event.setDate(Date.valueOf(datePicker.getValue()));
        event.setDescription(descriptionField.getText().trim());
        event.setLieux(String.format("%s, %s (%.6f, %.6f)", 
            adresseField.getText().trim(), 
            villeField.getText().trim(),
            currentLat,
            currentLng));
        event.setStatus(statusChoiceBox.getValue());
        event.setDuree(dureeField.getText().trim());
        event.setNb_place_dispo(nbPlacesSpinner.getValue());
        event.setTypeEvent(typeEventComboBox.getValue());

        try {
            eventService.ajouter(event);
        } catch (Exception ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText(ex.getMessage());
            alert.showAndWait();
        }
    }

    private boolean validateAll() {
        boolean isValid = true;
        
        if (!validateTitle()) isValid = false;
        if (!validateDate()) isValid = false;
        if (!validateDescription()) isValid = false;
        if (!validateLocation()) isValid = false;
        if (!validateStatus()) isValid = false;
        if (!validateDuration()) isValid = false;
        if (!validateNbPlaces()) isValid = false;
        if (!validateTypeEvent()) isValid = false;
        
        return isValid;
    }

    @FXML
    private void goToTypeEventManagement() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/type_event.fxml"));
            Parent root = loader.load();
            titleField.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goToEventView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/event_view.fxml"));
            Parent root = loader.load();
            titleField.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goToStatistics() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/statistics_view.fxml"));
            Parent root = loader.load();
            titleField.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goBackToHome() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/home.fxml"));
            Parent root = loader.load();
            titleField.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
