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

public class RegisterController {
    @FXML private TextField nameField, lastnameField, emailField;
    @FXML private PasswordField passwordField;
    @FXML private DatePicker dateOfBirthPicker;
    @FXML private ComboBox<String> roleComboBox;

    private final UserService service = new UserService();
    private static final Set<String> VALID_ROLES = new HashSet<>(Arrays.asList("ADMIN", "USER", "ARTIST"));

    @FXML
    private void initialize() {
        // Initialize role options
        roleComboBox.getItems().addAll("USER", "ARTIST");
    }

    @FXML
    private void register() {
        try {
            if (!validateFields()) return;

            User user = new User(
                    nameField.getText().trim(),
                    lastnameField.getText().trim(),
                    emailField.getText().trim(),
                    passwordField.getText().trim(),
                    Arrays.asList(roleComboBox.getValue()), // Use selected role
                    Date.valueOf(dateOfBirthPicker.getValue()),
                    false, // Not blocked by default
                    nameField.getText().trim() // Using name as username for now
            );

            service.ajouter(user);
            showAlert(Alert.AlertType.INFORMATION, "Success", "Registration successful! Please login.");
            goBackToLogin();

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Registration failed: " + e.getMessage());
        }
    }

    @FXML
    private void goBackToLogin() {
        try {
            Stage stage = (Stage) nameField.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Parent root = loader.load();
            stage.setScene(new Scene(root));
            stage.setTitle("Login");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to return to login page: " + e.getMessage());
            e.printStackTrace();
        }
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

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 