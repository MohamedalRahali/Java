package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;
import javafx.scene.control.Alert;
import javafx.scene.text.Font;
import javafx.scene.web.WebView;
import javafx.scene.web.WebEngine;
import javafx.concurrent.Worker;
import javafx.application.Platform;

import models.Event;
import Services.EventService;
import Services.AIService;
import utils.TranslationService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.scene.text.Text;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;
import netscape.javascript.JSObject;


import java.io.IOException;
import java.util.List;
import javafx.scene.control.ScrollPane;
import javafx.geometry.Pos;
import javafx.scene.text.TextFlow;
import javafx.scene.text.Text;
import javafx.scene.paint.Color;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.util.Duration;

public class ClientEventsController {
    @FXML
    private Button backButton;
    @FXML
    private FlowPane eventsContainer;

    @FXML
    private Button toggleModeButton;
    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> languageComboBox;


    private EventService eventService;
    private TranslationService translationService;
    private String currentLanguage = "fr";
    private boolean isArtisticMode = true;
    private Event selectedEvent;

    @FXML
    private ScrollPane chatScrollPane;
    @FXML
    private VBox chatMessagesBox;
    @FXML
    private TextField chatInput;
    @FXML
    private Button sendButton;

    private AIService aiService;

    public void initialize() {
        eventService = new EventService();
        translationService = new TranslationService();
        aiService = new AIService();

        // Afficher le message de bienvenue du chatbot
        Platform.runLater(() -> {
            addBotMessage(aiService.getWelcomeMessage());
        });
        updateTranslations();
        loadEvents();

        // Setup language selection
        languageComboBox.getItems().addAll(
            "English",
            "Français"
        );
        languageComboBox.setValue("Français");

        languageComboBox.setOnAction(e -> {
            String selectedLanguage = languageComboBox.getValue();
            String langCode = switch (selectedLanguage) {
                case "Français" -> "fr";
                default -> "en";
            };

            loadEvents();
            if (!langCode.equals(currentLanguage)) {
                currentLanguage = langCode;
                updateTranslations();
                loadEvents();
            }
        });
    }

    private WebView createMapForEvent(Event event) {
        class JavaConnector {
            @SuppressWarnings("unused")
            public void showEventDetails(int eventId) {
                Platform.runLater(() -> navigateToEventDetails(eventId));
            }
        }
        
        JavaConnector javaConnector = new JavaConnector();
        WebView mapView = new WebView();
        mapView.setPrefSize(200, 150);
        mapView.setStyle("-fx-border-color: #d4a373; -fx-border-width: 1px; -fx-border-radius: 4px;");
        
        WebEngine webEngine = mapView.getEngine();
        webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                JSObject window = (JSObject) webEngine.executeScript("window");
                window.setMember("java", javaConnector);
            }
        });
        String location = event.getLieux();
        if (location == null || location.trim().isEmpty()) {
            System.err.println("Event location is null or empty for event: " + event.getTitle());
            return mapView;
        }
        double[] coordinates = getCoordinatesFromLocation(location);
        if (coordinates == null || coordinates.length != 2) {
            System.err.println("Invalid coordinates for event location: '" + location + "' (event: " + event.getTitle() + ")");
            return mapView;
        }
        
        webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                Platform.runLater(() -> {
                    String popupContent = String.format(
                        "<div style='text-align: center;'>"
                        + "<h3 style='margin: 0 0 10px 0;'>%s</h3>"
                        + "<button onclick='window.showEventDetails(%d)' "
                        + "style='background: #d4a373; color: white; border: none; "
                        + "padding: 5px 10px; border-radius: 5px; cursor: pointer;'>"
                        + "Voir les détails</button></div>",
                        event.getTitle().replace("'", "\\'")
                        , event.getId()
                    );

                    webEngine.executeScript(String.format(
                        "map.setView([%f, %f], 13);"
                        + "L.marker([%f, %f]).addTo(map).bindPopup(`%s`);"
                        + "window.showEventDetails = function(eventId) {"
                        + "    window.java.showEventDetails(eventId);"
                        + "};",
                        coordinates[0], coordinates[1],
                        coordinates[0], coordinates[1],
                        popupContent
                    ));
                });
            }
        });
        
        webEngine.loadContent("""
            <!DOCTYPE html>
            <html>
            <head>
                <link rel="stylesheet" href="https://unpkg.com/leaflet@1.7.1/dist/leaflet.css"/>
                <script src="https://unpkg.com/leaflet@1.7.1/dist/leaflet.js"></script>
                <style>
                    html, body, #map { height: 100%; width: 100%; margin: 0; padding: 0; }
                </style>
            </head>
            <body>
                <div id="map"></div>
                <script>
                    var map;
                    window.onload = function() {
                        map = L.map('map').setView([36.8065, 10.1815], 13);
                        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
                            attribution: '© OpenStreetMap contributors'
                        }).addTo(map);
                    };
                </script>
            </body>
            </html>
        """);
        
        return mapView;
    }



    private double[] getCoordinatesFromLocation(String location) {
        if (location == null) {
            // showError("Invalid Location", "The provided location is empty or null.");
            return null;
        }
        // Simulation de coordonnées pour les villes tunisiennes
        switch (location.trim().toLowerCase()) {
            case "tunis": return new double[]{36.8065, 10.1815};
            case "sfax": return new double[]{34.7400, 10.7600};
            case "sousse": return new double[]{35.8250, 10.6375};
            case "gabes": return new double[]{33.8811, 10.0982};
            case "bizerte": return new double[]{37.2744, 9.8739};
            case "monastir": return new double[]{35.7780, 10.8262};
            default:
                String errorMsg = "Unknown location for coordinates: '" + location + "'";
                System.err.println(errorMsg);
                // showError("Invalid Location", "Could not find coordinates for: " + location + ". Please enter a valid Tunisian city.");
                return null;
        }
    }

    private void loadEvents() {
        List<Event> events = eventService.getAll();
        eventsContainer.getChildren().clear();
        
        for (Event event : events) {
            VBox ticket = isArtisticMode ? createArtisticTicket(event) : createEventTicket(event);
            eventsContainer.getChildren().add(ticket);
        }
        

    }

    private VBox createArtisticTicket(Event event) {
        VBox ticket = new VBox(10);
        ticket.setStyle("-fx-background-color: #2d1f17; " +
                       "-fx-background-radius: 15; " +
                       "-fx-padding: 15; " +
                       "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 2);");
        ticket.setPrefWidth(400);
        ticket.setMaxWidth(400);
        ticket.setMinHeight(500);

        // En-tête du ticket
        HBox header = new HBox();
        header.setStyle("-fx-background-color: #d4a373; " +
                       "-fx-background-radius: 10 10 0 0; " +
                       "-fx-padding: 15;");
        header.setPrefWidth(800);

        Text title = new Text(translationService.translateText(event.getTitle(), currentLanguage));
        title.setStyle("-fx-fill: #2d1f17; -fx-font-weight: bold; -fx-font-size: 22;");
        title.setFont(Font.font("Georgia"));
        header.getChildren().add(title);

        // Corps du ticket
        VBox body = new VBox(15);
        body.setStyle("-fx-background-color: #f8f1e6; " +
                     "-fx-background-radius: 0 0 10 10; " +
                     "-fx-padding: 20;");

        // Carte en haut
        WebView mapView = createMapForEvent(event);
        mapView.setPrefSize(370, 200);
        
        // Informations en dessous
        VBox infoBox = new VBox(10);
        infoBox.setStyle("-fx-padding: 10 0;");
        
        // Grille d'informations
        GridPane infoGrid = new GridPane();
        infoGrid.setHgap(10);
        infoGrid.setVgap(10);
        
        String typeEventStr = event.getTypeEvent() != null ? event.getTypeEvent().toString() : "Non spécifié";
        
        // Première ligne
        infoGrid.add(createArtisticInfoBox("Type", typeEventStr), 0, 0);
        infoGrid.add(createArtisticInfoBox("Date", event.getDate().toString()), 1, 0);
        
        // Deuxième ligne
        infoGrid.add(createArtisticInfoBox("Lieu", event.getLieux()), 0, 1);
        infoGrid.add(createArtisticInfoBox("Prix", String.format("%.2f DT", event.getNb_place_dispo() * 10.0)), 1, 1);
        
        // Troisième ligne
        infoGrid.add(createArtisticInfoBox("Capacité", String.valueOf(event.getNb_place_dispo())), 0, 2);
        infoGrid.add(createArtisticInfoBox("Status", event.getStatus()), 1, 2);
        
        infoBox.getChildren().add(infoGrid);

        body.getChildren().addAll(mapView, infoBox);

        // Bouton de réservation
        Button reserveBtn = new Button(translationService.translateText("Réserver", currentLanguage));
        reserveBtn.setStyle("-fx-background-color: #d4a373; " +
                          "-fx-text-fill: #2d1f17; " +
                          "-fx-font-weight: bold; " +
                          "-fx-background-radius: 8; " +
                          "-fx-padding: 10 25; " +
                          "-fx-font-size: 14; " +
                          "-fx-cursor: hand;");
        reserveBtn.setOnAction(e -> {
            selectedEvent = event;
            handleReserveButton();
        });

        body.getChildren().add(reserveBtn);

        ticket.getChildren().addAll(header, body);
        return ticket;
    }

    private VBox createEventTicket(Event event) {
        VBox ticket = new VBox(10);
        ticket.setStyle("-fx-background-color: #ffffff; " +
                       "-fx-background-radius: 15; " +
                       "-fx-padding: 20; " +
                       "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2); " +
                       "-fx-border-color: #e0e0e0; " +
                       "-fx-border-width: 1; " +
                       "-fx-border-radius: 15;");
        ticket.setPrefWidth(800);
        ticket.setMaxWidth(800);

        // En-tête du ticket
        HBox header = new HBox();
        header.setStyle("-fx-background-color: #f5f5f5; " +
                       "-fx-background-radius: 10 10 0 0; " +
                       "-fx-padding: 15;");
        header.setPrefWidth(800);

        Text title = new Text(translationService.translateText(event.getTitle(), currentLanguage));
        title.setStyle("-fx-fill: #333333; -fx-font-weight: bold; -fx-font-size: 22;");
        title.setFont(Font.font("Segoe UI"));
        header.getChildren().add(title);

        // Corps du ticket
        VBox body = new VBox(15);
        body.setStyle("-fx-background-color: #ffffff; " +
                     "-fx-background-radius: 0 0 10 10; " +
                     "-fx-padding: 20;");

        // Informations de l'événement
        HBox infoRow1 = new HBox(30);
        infoRow1.setStyle("-fx-padding: 5;");

        String typeEventStr = event.getTypeEvent() != null ? event.getTypeEvent().toString() : "Non spécifié";
        VBox typeBox = createInfoBox("Type", typeEventStr);
        VBox dateBox = createInfoBox("Date", event.getDate().toString());
        VBox locationBox = createInfoBox("Lieu", event.getLieux());

        infoRow1.getChildren().addAll(typeBox, dateBox, locationBox);

        HBox infoRow2 = new HBox(30);
        infoRow2.setStyle("-fx-padding: 5;");

        VBox priceBox = createInfoBox("Prix", String.format("%.2f DT", event.getNb_place_dispo() * 10.0));
        VBox capacityBox = createInfoBox("Capacité", String.valueOf(event.getNb_place_dispo()));

        infoRow2.getChildren().addAll(priceBox, capacityBox);

        body.getChildren().addAll(infoRow1, infoRow2);

        // Bouton de réservation
        Button reserveBtn = new Button(translationService.translateText("Réserver", currentLanguage));
        reserveBtn.setStyle("-fx-background-color: #4CAF50; " +
                          "-fx-text-fill: white; " +
                          "-fx-font-weight: bold; " +
                          "-fx-background-radius: 8; " +
                          "-fx-padding: 10 25; " +
                          "-fx-font-size: 14; " +
                          "-fx-cursor: hand;");
        reserveBtn.setOnAction(e -> {
            selectedEvent = event;
            handleReserveButton();
        });

        body.getChildren().add(reserveBtn);

        ticket.getChildren().addAll(header, body);
        return ticket;
    }

    private VBox createArtisticInfoBox(String label, String value) {
        VBox box = new VBox(5);
        
        Text labelText = new Text(translationService.translateText(label, currentLanguage));
        labelText.setStyle("-fx-fill: #2d1f17; -fx-font-size: 12;");
        labelText.setFont(Font.font("Georgia"));
        
        Text valueText = new Text(value);
        valueText.setStyle("-fx-fill: #d4a373; -fx-font-size: 14; -fx-font-weight: bold;");
        valueText.setFont(Font.font("Georgia"));
        
        box.getChildren().addAll(labelText, valueText);
        return box;
    }

    private VBox createInfoBox(String label, String value) {
        VBox box = new VBox(5);
        
        Text labelText = new Text(translationService.translateText(label, currentLanguage));
        labelText.setStyle("-fx-fill: #666666; -fx-font-size: 12;");
        labelText.setFont(Font.font("Segoe UI"));
        
        Text valueText = new Text(value);
        valueText.setStyle("-fx-fill: #2196F3; -fx-font-size: 14; -fx-font-weight: bold;");
        valueText.setFont(Font.font("Segoe UI"));
        
        box.getChildren().addAll(labelText, valueText);
        return box;
    }

    @FXML
    private void navigateToEventDetails(int eventId) {
        Event event = eventService.getAll().stream()
            .filter(e -> e.getId() == eventId)
            .findFirst()
            .orElse(null);
        if (event == null) return;
        
        navigateToEventDetails(event);
    }

    private void navigateToEventDetails(Event event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/event_details.fxml"));
            Parent root = loader.load();

            // On passe l'événement et la langue au contrôleur
            Object controller = loader.getController();
            if (controller != null) {
                try {
                    controller.getClass().getMethod("setEvent", Event.class).invoke(controller, event);
                    controller.getClass().getMethod("setLanguage", String.class).invoke(controller, currentLanguage);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            Stage stage = (Stage) backButton.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur", "Impossible d'ouvrir les détails de l'événement");
        }
    }

    private void showError(String title, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(translationService.translateText(title, currentLanguage));
            alert.setHeaderText(null);
            alert.setContentText(translationService.translateText(content, currentLanguage));
            alert.showAndWait();
        });
    }

    @FXML
    private void handleReserveButton() {
        if (selectedEvent != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/reservation_view.fxml"));
                Parent root = loader.load();
                
                // Passer l'événement sélectionné au contrôleur de réservation
                ReservationController reservationController = loader.getController();
                reservationController.setEvent(selectedEvent);
                reservationController.setLanguage(currentLanguage);
                
                Stage stage = (Stage) backButton.getScene().getWindow();
                stage.setScene(new Scene(root));
            } catch (IOException e) {
                e.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR);

                alert.setTitle("Erreur");
                alert.setHeaderText(null);
                alert.setContentText("Impossible de charger la page de paiement.");
                alert.showAndWait();
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Avertissement");
            alert.setHeaderText(null);
            alert.setContentText("Veuillez sélectionner un événement à réserver.");
            alert.showAndWait();
        }
    }

    @FXML
    private void handleSearch() {
        String searchText = searchField.getText().toLowerCase();
        List<Event> allEvents = eventService.getAll();
        List<Event> filteredEvents = allEvents.stream()
                .filter(event -> event.getTitle().toLowerCase().contains(searchText) ||
                        event.getLieux().toLowerCase().contains(searchText))
                .toList();

        eventsContainer.getChildren().clear();
        filteredEvents.forEach(event -> {
            VBox ticket = isArtisticMode ? createArtisticTicket(event) : createEventTicket(event);
            eventsContainer.getChildren().add(ticket);
        });


    }

    @FXML
    private void toggleMode() {
        isArtisticMode = !isArtisticMode;
        toggleModeButton.setText(translationService.translateText(isArtisticMode ? "Event Mode" : "Artistic Mode", currentLanguage));
        loadEvents();
    }

    private void updateTranslations() {
        // Translate column headers
        toggleModeButton.setText(translationService.translateText(isArtisticMode ? "Event Mode" : "Artistic Mode", currentLanguage));
                searchField.setPromptText(translationService.translateText("Rechercher un événement...", currentLanguage));
    }

    public void setLanguage(String language) {
        currentLanguage = language;
        updateTranslations();
    }

    @FXML
    private void handleChatSend() {
        String userMessage = chatInput.getText().trim();
        if (!userMessage.isEmpty()) {
            // Ajouter le message de l'utilisateur
            addUserMessage(userMessage);
            chatInput.clear();

            // Désactiver le champ de saisie pendant le traitement
            chatInput.setDisable(true);
            sendButton.setDisable(true);

            // Ajouter un indicateur de frappe
            HBox typingBox = new HBox();
            typingBox.setAlignment(Pos.CENTER_LEFT);
            typingBox.setStyle("-fx-padding: 5 10 5 10;");
            Text typingText = new Text("En train d'écrire...");
            typingText.setFill(Color.gray(0.5));
            typingBox.getChildren().add(typingText);
            chatMessagesBox.getChildren().add(typingBox);

            // Simuler un délai de traitement naturel
            Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
                // Retirer l'indicateur de frappe
                chatMessagesBox.getChildren().remove(typingBox);

                // Obtenir et afficher la réponse de l'IA
                String response = aiService.processQuestion(userMessage);
                addBotMessage(response);

                // Réactiver le champ de saisie
                chatInput.setDisable(false);
                sendButton.setDisable(false);
                chatInput.requestFocus();

                // Faire défiler jusqu'au dernier message
                chatScrollPane.setVvalue(1.0);
            }));
            timeline.play();
        }
    }

    private void addUserMessage(String message) {
        HBox messageBox = new HBox();
        messageBox.setAlignment(Pos.CENTER_RIGHT);
        messageBox.setStyle("-fx-padding: 5 10 5 10;");

        TextFlow textFlow = new TextFlow();
        textFlow.setStyle("-fx-background-color: #d4a373; -fx-background-radius: 10 0 10 10; -fx-padding: 10; -fx-max-width: 300;");

        Text text = new Text(message);
        text.setFill(Color.WHITE);
        text.setWrappingWidth(280);
        textFlow.getChildren().add(text);

        messageBox.getChildren().add(textFlow);
        chatMessagesBox.getChildren().add(messageBox);
    }

    private void addBotMessage(String message) {
        VBox messageContainer = new VBox();
        messageContainer.setAlignment(Pos.CENTER_LEFT);
        messageContainer.setSpacing(10);
        messageContainer.setStyle("-fx-padding: 5 10 5 10;");

        // Message principal
        TextFlow textFlow = new TextFlow();
        textFlow.setStyle("-fx-background-color: #f8f1e6; -fx-background-radius: 0 10 10 10; -fx-padding: 10; -fx-max-width: 300;");

        Text text = new Text(message);
        text.setFill(Color.web("#2d1f17"));
        text.setWrappingWidth(280);
        textFlow.getChildren().add(text);
        messageContainer.getChildren().add(textFlow);

        // Extraire et ajouter les suggestions si présentes
        if (message.contains("💡")) {
            FlowPane suggestionsPane = new FlowPane();
            suggestionsPane.setHgap(8);
            suggestionsPane.setVgap(8);
            suggestionsPane.setPrefWrapLength(280);
            suggestionsPane.setStyle("-fx-padding: 5;");

            String[] lines = message.split("\n");
            for (String line : lines) {
                if (line.startsWith("💡")) {
                    String suggestion = line.substring(2).trim();
                    Button suggestionButton = createSuggestionButton(suggestion);
                    suggestionsPane.getChildren().add(suggestionButton);
                }
            }

            if (!suggestionsPane.getChildren().isEmpty()) {
                messageContainer.getChildren().add(suggestionsPane);
            }
        }

        chatMessagesBox.getChildren().add(messageContainer);
    }

    private Button createSuggestionButton(String suggestion) {
        Button button = new Button(suggestion);
        String baseStyle = String.format("-fx-background-color: #d4a373; " +
                       "-fx-text-fill: white; " +
                       "-fx-background-radius: 15; " +
                       "-fx-padding: 8 15; " +
                       "-fx-cursor: hand; " +
                       "-fx-font-size: 12; " +
                       "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 2, 0, 0, 1); " +
                       "-fx-border-color: transparent; " +
                       "-fx-font-family: 'System'; " +
                       "-fx-font-weight: bold;");

        String hoverStyle = String.format("-fx-background-color: #c69c6d; " +
                       "-fx-text-fill: white; " +
                       "-fx-background-radius: 15; " +
                       "-fx-padding: 8 15; " +
                       "-fx-cursor: hand; " +
                       "-fx-font-size: 12; " +
                       "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 3, 0, 0, 2); " +
                       "-fx-border-color: transparent; " +
                       "-fx-font-family: 'System'; " +
                       "-fx-font-weight: bold; " +
                       "-fx-scale-x: 1.02; " +
                       "-fx-scale-y: 1.02;");

        button.setStyle(baseStyle);

        // Effet de survol avec transition douce
        button.setOnMouseEntered(e -> {
            button.setStyle(hoverStyle);
        });

        button.setOnMouseExited(e -> {
            button.setStyle(baseStyle);
        });

        // Effet de clic
        button.setOnMousePressed(e -> {
            button.setStyle(baseStyle + "-fx-scale-x: 0.98; -fx-scale-y: 0.98;");
        });

        button.setOnMouseReleased(e -> {
            button.setStyle(baseStyle);
        });

        // Action au clic
        button.setOnAction(e -> {
            chatInput.setText(suggestion);
            handleChatSend();
        });

        return button;
    }
}