package gui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import javafx.scene.layout.StackPane;
import javafx.scene.control.SpinnerValueFactory;
import models.Event;
import models.TypeEvent;
import Services.EventService;
import Services.TypeEventService;
import java.io.IOException;
import java.sql.Date;
import java.time.LocalDate;
import java.util.regex.Pattern;

public class ModifyEvenementArtistController {
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

    private Event eventToEdit;
    private final EventService eventService = new EventService();
    private final TypeEventService typeEventService = new TypeEventService();
    private double currentLat = 36.8065;
    private double currentLng = 10.1815;

    public void setEventToEdit(Event event) {
        this.eventToEdit = event;
        titleField.setText(event.getTitle());
        datePicker.setValue(event.getDate().toLocalDate());
        descriptionField.setText(event.getDescription());
        // Parse address/city from lieux
        String lieux = event.getLieux();
        String[] parts = lieux.split(",");
        if (parts.length >= 2) {
            adresseField.setText(parts[0].trim());
            villeField.setText(parts[1].replaceAll("\\(.*\\)", "").trim());
        }
        Pattern coordPattern = Pattern.compile("\\(([-+]?[0-9]*\\.?[0-9]+), ([-+]?[0-9]*\\.?[0-9]+)\\)");
        java.util.regex.Matcher matcher = coordPattern.matcher(lieux);
        if (matcher.find()) {
            try {
                currentLat = Double.parseDouble(matcher.group(1));
                currentLng = Double.parseDouble(matcher.group(2));
            } catch (NumberFormatException ignored) {}
        }
        dureeField.setText(event.getDuree());
        SpinnerValueFactory<Integer> valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 1000, event.getNb_place_dispo());
        nbPlacesSpinner.setValueFactory(valueFactory);
        typeEventComboBox.getItems().clear();
        typeEventComboBox.getItems().addAll(typeEventService.getAll());
        typeEventComboBox.setValue(event.getTypeEvent());
        initializeMap();
    }

    private void initializeMap() {
        javafx.scene.web.WebEngine engine = mapView.getEngine();
        engine.setJavaScriptEnabled(true);
        String mapUrl = getClass().getResource("/html/map.html").toExternalForm();
        engine.load(mapUrl);
        engine.getLoadWorker().stateProperty().addListener((obs, old, newState) -> {
            if (newState == javafx.concurrent.Worker.State.SUCCEEDED) {
                netscape.javascript.JSObject window = (netscape.javascript.JSObject) engine.executeScript("window");
                window.setMember("app", new JavaBridge());
                // Center map on currentLat/currentLng
                engine.executeScript(String.format("setLocation(%f, %f);", currentLat, currentLng));
            }
        });
    }

    public class JavaBridge {
        public void onMapReady() {}
        public void onMapClick(double lat, double lng) {
            if (isValidCoordinate(lat, lng)) {
                currentLat = lat;
                currentLng = lng;
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
    private void updateEvenement() {
        if (!validateAll()) {
            return;
        }
        eventToEdit.setTitle(titleField.getText().trim());
        eventToEdit.setDate(Date.valueOf(datePicker.getValue()));
        eventToEdit.setDescription(descriptionField.getText().trim());
        eventToEdit.setLieux(String.format("%s, %s (%.6f, %.6f)", adresseField.getText().trim(), villeField.getText().trim(), currentLat, currentLng));
        eventToEdit.setDuree(dureeField.getText().trim());
        eventToEdit.setNb_place_dispo(nbPlacesSpinner.getValue());
        eventToEdit.setTypeEvent(typeEventComboBox.getValue());
        try {
            eventService.modifier(eventToEdit);
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Événement modifié avec succès!");
            alert.showAndWait();
            // Close window
            Stage stage = (Stage) titleField.getScene().getWindow();
            stage.close();
        } catch (Exception ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
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
