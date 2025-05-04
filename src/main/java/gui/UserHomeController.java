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

public class UserHomeController {
    @FXML
    private Button myTicketsButton;
    
    @FXML
    private Button eventsButton;
    
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
    private void viewMyTickets(ActionEvent event) {
        try {
            // TODO: Implement view my tickets functionality
            System.out.println("View my tickets clicked");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @FXML
    private void viewEvents(ActionEvent event) {
        try {
            // TODO: Implement view events functionality
            System.out.println("View events clicked");
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
    
    @FXML
    private void goToCreateReclamation(ActionEvent event) throws java.io.IOException {
        javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/create_reclamation.fxml"));
        javafx.scene.Parent root = loader.load();
        javafx.stage.Stage stage = (javafx.stage.Stage) logoutButton.getScene().getWindow();
        stage.setTitle("Créer une Réclamation");
        stage.setScene(new javafx.scene.Scene(root));
    }

    @FXML
    private void goToMesReclamations(ActionEvent event) throws java.io.IOException {
        javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/mes_reclamations.fxml"));
        javafx.scene.Parent root = loader.load();
        javafx.stage.Stage stage = (javafx.stage.Stage) logoutButton.getScene().getWindow();
        stage.setTitle("Mes Réclamations");
        stage.setScene(new javafx.scene.Scene(root));
    }
    
    @FXML
    private void goToUserHome(ActionEvent event) throws java.io.IOException {
        javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/user_home.fxml"));
        javafx.scene.Parent root = loader.load();
        javafx.stage.Stage stage = (javafx.stage.Stage) logoutButton.getScene().getWindow();
        stage.setTitle("Accueil");
        stage.setScene(new javafx.scene.Scene(root));
    }
} 