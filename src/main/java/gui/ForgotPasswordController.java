package gui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.scene.layout.GridPane;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.util.Pair;
import Services.UserService;
import Services.EmailService;
import models.PasswordReset;
import javax.mail.MessagingException;
import java.util.Optional;
import java.io.IOException;

public class ForgotPasswordController {
    @FXML private TextField emailField;
    private final UserService userService = new UserService();
    private boolean emailConfigured = false;

    @FXML
    private void initialize() {
        checkEmailConfiguration();
    }

    private void checkEmailConfiguration() {
        try {
            // This will throw an exception if credentials are not set up
            EmailService.sendResetCode("test@test.com", "000000");
            emailConfigured = true;
        } catch (MessagingException e) {
            // Show dialog to configure email
            showEmailConfigDialog();
        }
    }

    private void showEmailConfigDialog() {
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Email Configuration");
        dialog.setHeaderText("Please enter your Gmail credentials\n" +
                           "Note: You need to generate an App Password from your Google Account");

        // Set the button types
        ButtonType loginButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

        // Create the email and password labels and fields
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField gmailField = new TextField();
        gmailField.setPromptText("your.email@gmail.com");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("App Password");

        grid.add(new Label("Gmail:"), 0, 0);
        grid.add(gmailField, 1, 0);
        grid.add(new Label("App Password:"), 0, 1);
        grid.add(passwordField, 1, 1);

        dialog.getDialogPane().setContent(grid);

        // Enable/Disable login button depending on whether both fields are filled
        Node loginButton = dialog.getDialogPane().lookupButton(loginButtonType);
        loginButton.setDisable(true);

        // Do validation
        gmailField.textProperty().addListener((observable, oldValue, newValue) -> {
            loginButton.setDisable(newValue.trim().isEmpty() || passwordField.getText().trim().isEmpty());
        });
        passwordField.textProperty().addListener((observable, oldValue, newValue) -> {
            loginButton.setDisable(newValue.trim().isEmpty() || gmailField.getText().trim().isEmpty());
        });

        // Convert the result to a pair when the login button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == loginButtonType) {
                return new Pair<>(gmailField.getText(), passwordField.getText());
            }
            return null;
        });

        Optional<Pair<String, String>> result = dialog.showAndWait();

        result.ifPresent(credentials -> {
            try {
                EmailService.setupCredentials(credentials.getKey(), credentials.getValue());
                EmailService.reloadCredentials();
                emailConfigured = true;
                showAlert("Success", "Email configuration saved successfully!");
            } catch (IOException e) {
                showAlert("Error", "Failed to save email configuration: " + e.getMessage());
            }
        });
    }

    @FXML
    private void sendResetCode() {
        if (!emailConfigured) {
            showEmailConfigDialog();
            if (!emailConfigured) {
                showAlert("Error", "Email must be configured before sending reset codes");
                return;
            }
        }

        String email = emailField.getText().trim();

        if (email.isEmpty()) {
            showAlert("Error", "Please enter your email address");
            return;
        }

        // Check if email exists
        if (!userService.emailExists(email)) {
            showAlert("Error", "No account found with this email address");
            return;
        }

        try {
            // Generate a 6-digit reset code
            String resetCode = generateResetCode();
            
            // Store the reset code
            PasswordReset.storeResetCode(email, resetCode);
            
            // Send email with reset code
            try {
                EmailService.sendResetCode(email, resetCode);
                showAlert("Success", "A reset code has been sent to your email address");
            } catch (MessagingException e) {
                showAlert("Error", "Failed to send reset code email: " + e.getMessage());
                return;
            }
            
            // Go to reset password page
            goToResetPassword(email);
        } catch (Exception e) {
            showAlert("Error", "Failed to process reset code: " + e.getMessage());
        }
    }

    @FXML
    private void goBackToLogin() {
        try {
            Stage stage = (Stage) emailField.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Parent root = loader.load();
            stage.setScene(new Scene(root));
            stage.setTitle("Login");
        } catch (Exception e) {
            showAlert("Error", "Failed to return to login page: " + e.getMessage());
        }
    }

    private void goToResetPassword(String email) {
        try {
            Stage currentStage = (Stage) emailField.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/fxml/reset_password.fxml"));
            Parent root = loader.load();
            
            // Pass the email to the reset password controller
            ResetPasswordController controller = loader.getController();
            controller.setEmail(email);
            
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
            Stage stage = new Stage();
            stage.setTitle("Reset Password");
            stage.setScene(scene);
            stage.show();
            currentStage.close();
        } catch (Exception e) {
            showAlert("Error", "Failed to open reset password page: " + e.getMessage());
        }
    }

    private String generateResetCode() {
        // Generate a 6-digit number
        return String.format("%06d", (int)(Math.random() * 1000000));
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 