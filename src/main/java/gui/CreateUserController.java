package gui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import models.User;
import Services.UserService;
import java.sql.Date;
import java.time.LocalDate;
import java.time.Period;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.io.IOException;
import javafx.event.ActionEvent;
import javafx.scene.Node;

public class CreateUserController {
    @FXML
    private AdminBarController adminBarController;

    @FXML private TextField nameField, lastnameField, emailField;
    @FXML private PasswordField passwordField;
    @FXML private DatePicker dateOfBirthPicker;
    @FXML private ComboBox<String> roleComboBox;


    private final UserService service = new UserService();
    private static final Set<String> VALID_ROLES = new HashSet<>(Arrays.asList("ADMIN", "USER", "ARTIST"));

    @FXML
    private void initialize() {
        if (adminBarController != null && models.CurrentUser.getCurrentUser() != null) {
            adminBarController.setUserEmail(models.CurrentUser.getCurrentUser().getEmail());
        }
        // Initialize role options
        roleComboBox.getItems().addAll("ADMIN", "USER", "ARTIST");
    }

    @FXML
    private void createUser() {
        try {
            if (!validateFields()) return;

            User user = new User(
                    nameField.getText().trim(),
                    lastnameField.getText().trim(),
                    emailField.getText().trim(),
                    passwordField.getText().trim(),
                    Arrays.asList(roleComboBox.getValue()),
                    Date.valueOf(dateOfBirthPicker.getValue()),
                    false,
                    nameField.getText().trim() // Using name as username for now
            );

            try {
                // Add user to database
                service.ajouter(user);
                
                // Show success message
                showAlert(Alert.AlertType.INFORMATION, "Success", "User created successfully");
                
                // Clear fields
                clearFields();
                
                // Navigate back to home
                goBackToHome();
            } catch (RuntimeException e) {
                if (e.getMessage().contains("Duplicate entry") && e.getMessage().contains("password")) {
                    showAlert(Alert.AlertType.WARNING, "Password Security", 
                        "This password is already in use. For security reasons, please choose a different password.");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Database Error", 
                        "Failed to create user: " + e.getMessage());
                }
            }

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", 
                "An unexpected error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void goBackToHome() {
        try {
            Stage stage = (Stage) nameField.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin_home.fxml"));
            Parent root = loader.load();
            stage.setTitle("Admin Home");
            stage.setScene(new Scene(root, 800, 600));
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to open admin home page");
        }
    }

    @FXML
    private void goToAdminHome(ActionEvent event) throws IOException {
        Stage stage = (Stage) nameField.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin_home.fxml"));
        Parent root = loader.load();
        stage.setTitle("User Dashboard");
        stage.setScene(new Scene(root, 800, 600));
    }

    @FXML
    private void goToCreateUser(ActionEvent event) throws IOException {
        Stage stage = (Stage) nameField.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/create_user.fxml"));
        Parent root = loader.load();
        stage.setTitle("Add User");
        stage.setScene(new Scene(root, 800, 600));
    }

    @FXML
    private void goToUserView(ActionEvent event) throws IOException {
        Stage stage = (Stage) nameField.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/user_view.fxml"));
        Parent root = loader.load();
        stage.setTitle("User Table");
        stage.setScene(new Scene(root, 800, 600));
    }

    @FXML
    private void logout(ActionEvent event) throws IOException {
        models.CurrentUser.clear();
        Stage stage = (Stage) nameField.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
        Parent root = loader.load();
        stage.setTitle("Login");
        stage.setScene(new Scene(root, 800, 600));
    }

    private boolean validateFields() {
        String name = nameField.getText().trim();
        String lastname = lastnameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();
        LocalDate dateOfBirth = dateOfBirthPicker.getValue();

        if (name.isEmpty() || lastname.isEmpty() || email.isEmpty() || password.isEmpty() ||
                dateOfBirth == null || roleComboBox.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Validation", "All fields are required");
            return false;
        }

        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Please enter a valid email address");
            return false;
        }

        if (password.length() < 6) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Password must be at least 6 characters long");
            return false;
        }

        // Calculate age
        LocalDate today = LocalDate.now();
        Period period = Period.between(dateOfBirth, today);
        int age = period.getYears();

        if (age < 18) {
            showAlert(Alert.AlertType.WARNING, "Validation", 
                "User must be at least 18 years old. Current age: " + age + " years");
            return false;
        }

        if (dateOfBirth.isAfter(today)) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Date of birth cannot be in the future");
            return false;
        }

        return true;
    }

    private void clearFields() {
        nameField.clear();
        lastnameField.clear();
        emailField.clear();
        passwordField.clear();
        roleComboBox.setValue(null);
        dateOfBirthPicker.setValue(null);
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
