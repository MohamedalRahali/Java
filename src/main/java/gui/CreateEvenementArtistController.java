package gui;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.Label;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.web.WebView;
import javafx.scene.layout.StackPane;
import javafx.scene.control.Spinner;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.SpinnerValueFactory;
import models.TypeEvent;
import Services.TypeEventService;
import Services.EventService;
import models.Event;
import java.time.LocalDate;

public class CreateEvenementArtistController {
    @FXML private TextField titleField;
    @FXML private DatePicker datePicker;
    @FXML private TextArea descriptionField;
    @FXML private TextField adresseField;
    @FXML private TextField villeField;
    @FXML private TextField dureeField;
    @FXML private Spinner<Integer> nbPlacesSpinner;
    @FXML private ComboBox<TypeEvent> typeEventComboBox;
    @FXML private WebView mapView;
    @FXML private StackPane mapContainer;
    @FXML private Label titleErrorLabel;
    @FXML private Label dateErrorLabel;
    @FXML private Label descriptionErrorLabel;
    @FXML private Label locationErrorLabel;
    @FXML private Label dureeErrorLabel;
    @FXML private Label nbPlacesErrorLabel;
    @FXML private Label typeEventErrorLabel;

    private final EventService eventService = new EventService();
    private final TypeEventService typeEventService = new TypeEventService();
    private double currentLat = 36.8065;  // Default to Tunisia
    private double currentLng = 10.1815;

    private void initializeMap() {
        javafx.scene.web.WebEngine engine = mapView.getEngine();
        engine.setJavaScriptEnabled(true);
        String mapUrl = getClass().getResource("/html/map.html").toExternalForm();
        engine.load(mapUrl);
        engine.getLoadWorker().stateProperty().addListener((obs, old, newState) -> {
            if (newState == javafx.concurrent.Worker.State.SUCCEEDED) {
                netscape.javascript.JSObject window = (netscape.javascript.JSObject) engine.executeScript("window");
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
                && lat >= 30.0 && lat <= 38.0
                && lng >= 7.0 && lng <= 12.0;
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
            String encodedQuery = java.net.URLEncoder.encode(searchQuery, java.nio.charset.StandardCharsets.UTF_8.toString());
            String nominatimUrl = String.format("https://nominatim.openstreetmap.org/search?q=%s&format=json&limit=1", encodedQuery);
            javafx.scene.web.WebEngine engine = mapView.getEngine();
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

    private boolean validateDuration() {
        String duration = dureeField.getText().trim();
        if (duration.isEmpty()) {
            dureeErrorLabel.setText("Duration is required");
            return false;
        }
        if (!java.util.regex.Pattern.matches("^\\d+h\\d*$", duration)) {
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

    private boolean validateAll() {
        boolean isValid = true;
        if (!validateTitle()) isValid = false;
        if (!validateDate()) isValid = false;
        if (!validateDescription()) isValid = false;
        if (!validateLocation()) isValid = false;
        if (!validateDuration()) isValid = false;
        if (!validateNbPlaces()) isValid = false;
        if (!validateTypeEvent()) isValid = false;
        return isValid;
    }

    @FXML
    private void createEvenement() {
        if (!validateAll()) {
            return;
        }
        Event event = new Event();
        event.setTitle(titleField.getText().trim());
        event.setDate(java.sql.Date.valueOf(datePicker.getValue()));
        event.setDescription(descriptionField.getText().trim());
        event.setLieux(String.format("%s, %s (%.6f, %.6f)", adresseField.getText().trim(), villeField.getText().trim(), currentLat, currentLng));
        event.setStatus("En cours"); // Default status for new event
        event.setDuree(dureeField.getText().trim());
        event.setNb_place_dispo(nbPlacesSpinner.getValue());
        event.setTypeEvent(typeEventComboBox.getValue());
        event.setArtistId(models.CurrentUser.getCurrentUserId());
        try {
            eventService.ajouter(event);
            Alert alert = new Alert(AlertType.INFORMATION, "Événement créé avec succès!");
            alert.showAndWait();
            // Reset form
            titleField.clear();
            datePicker.setValue(null);
            descriptionField.clear();
            adresseField.clear();
            villeField.clear();
            dureeField.clear();
            nbPlacesSpinner.getValueFactory().setValue(50);
            typeEventComboBox.setValue(null);
        } catch (Exception ex) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText(ex.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    public void initialize() {
        SpinnerValueFactory<Integer> valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 1000, 50);
        nbPlacesSpinner.setValueFactory(valueFactory);
        typeEventComboBox.getItems().addAll(typeEventService.getAll());
        initializeMap();
        titleField.textProperty().addListener((obs, oldVal, newVal) -> validateTitle());
        datePicker.valueProperty().addListener((obs, oldVal, newVal) -> validateDate());
        descriptionField.textProperty().addListener((obs, oldVal, newVal) -> validateDescription());
        adresseField.textProperty().addListener((obs, oldVal, newVal) -> validateLocation());
        villeField.textProperty().addListener((obs, oldVal, newVal) -> validateLocation());
        dureeField.textProperty().addListener((obs, oldVal, newVal) -> validateDuration());
        nbPlacesSpinner.valueProperty().addListener((obs, oldVal, newVal) -> validateNbPlaces());
        typeEventComboBox.valueProperty().addListener((obs, oldVal, newVal) -> validateTypeEvent());
    }
}
