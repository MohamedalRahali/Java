package gui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import Services.UserService;
import models.User;
import models.PasswordReset;

public class ResetPasswordController {
    @FXML private TextField codeField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    
    private String email;
    private final UserService userService = new UserService();

    public void setEmail(String email) {
        this.email = email;
    }

    @FXML
    private void resetPassword() {
        String code = codeField.getText().trim();
        String newPassword = newPasswordField.getText().trim();
        String confirmPassword = confirmPasswordField.getText().trim();

        if (code.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            showAlert("Error", "Please fill in all fields");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            showAlert("Error", "Passwords do not match");
            return;
        }

        if (newPassword.length() < 6) {
            showAlert("Error", "Password must be at least 6 characters long");
            return;
        }

        try {
            // Verify reset code
            if (!PasswordReset.verifyResetCode(email, code)) {
                showAlert("Error", "Invalid or expired reset code");
                return;
            }

            // Update password
            User user = userService.getUserByEmail(email);
            if (user != null) {
                user.setPassword(newPassword);
                userService.modifier(user);
                
                // Clear the reset code
                PasswordReset.clearResetCode(email);
                
                showAlert("Success", "Password has been reset successfully");
                goBackToLogin();
            } else {
                showAlert("Error", "User not found");
            }
        } catch (Exception e) {
            showAlert("Error", "Failed to reset password: " + e.getMessage());
        }
    }

    @FXML
    private void goBackToLogin() {
        try {
            Stage stage = (Stage) codeField.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/fxml/login.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
            stage.setScene(scene);
            stage.setTitle("Login");
        } catch (Exception e) {
            showAlert("Error", "Failed to return to login page: " + e.getMessage());
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 