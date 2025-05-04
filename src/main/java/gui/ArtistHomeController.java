package gui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.event.ActionEvent;
import models.CurrentUser;
import models.User;

public class ArtistHomeController {
    @FXML
    private Button myEventsButton;
    
    @FXML
    private Button createEventButton;
    
    @FXML
    private Button logoutButton;
    
    @FXML
    private Label userEmailLabel;
    
    private User currentUser;
    
    @FXML
    private void initialize() {
        // Display current user's email if available
        if (CurrentUser.getCurrentUser() != null) {
            userEmailLabel.setText("Logged in as: " + CurrentUser.getCurrentUser().getEmail());
        }
    }
    
    public void initData(User user) {
        this.currentUser = user;
        userEmailLabel.setText("Logged in as: " + user.getEmail());
    }
    
    @FXML
    private void viewMyEvents(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/mes_evenements_artist.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @FXML
    private void createEvent(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/create_evenement_artist.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @FXML
    private void logout(ActionEvent event) {
        try {
            // Clear current user information
            CurrentUser.clear();
            
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
            
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
} 