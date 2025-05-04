package gui;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import models.User;
import Services.UserService;


import java.sql.Date;
import java.time.LocalDate;

import java.util.List;
import java.io.IOException;

public class ModifyUserController {

    @FXML private TextField nameField, lastnameField, emailField;
    @FXML private PasswordField passwordField;
    @FXML private DatePicker dateOfBirthPicker;
    @FXML private CheckBox isBlockedCheckBox;
    @FXML private ComboBox<String> roleComboBox;


    private User userToEdit;
    private final UserService service = new UserService();

    @FXML
    private void initialize() {
        // Initialize role options
        roleComboBox.getItems().addAll("ADMIN", "USER", "ARTIST");
    }

    public void setUserToEdit(User user) {
        this.userToEdit = user;

        // Fill fields with existing data
        nameField.setText(user.getName());
        lastnameField.setText(user.getLastname());
        emailField.setText(user.getEmail());
        passwordField.setText(user.getPassword());
        roleComboBox.setValue(user.getRoles().get(0)); // Set the first role
        dateOfBirthPicker.setValue(user.getDateOfBirth().toLocalDate());
        isBlockedCheckBox.setSelected(user.getIsBlocked());
    }

    @FXML
    private void updateUser() {
        if (userToEdit == null) return;

        try {
            if (!validateFields()) return;

            // Update user object
            userToEdit.setName(nameField.getText().trim());
            userToEdit.setLastname(lastnameField.getText().trim());
            userToEdit.setEmail(emailField.getText().trim());
            userToEdit.setPassword(passwordField.getText().trim());
            userToEdit.setRoles(List.of(roleComboBox.getValue())); // Set selected role
            userToEdit.setDateOfBirth(Date.valueOf(dateOfBirthPicker.getValue()));
            userToEdit.setIsBlocked(isBlockedCheckBox.isSelected());

            // Update in database
            service.modifier(userToEdit);

            // Show success message
            showAlert(Alert.AlertType.INFORMATION, "Success", "User updated successfully!");

            // Navigate back to user view
            Stage stage = (Stage) nameField.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/user_view.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("User Management");
            stage.show();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to update user: " + e.getMessage());
        }
    }

    @FXML
    private void goBackToTable() {
        try {
            Stage stage = (Stage) nameField.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/user_view.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("User Management");
            stage.show();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to navigate back: " + e.getMessage());
        }
    }

    private void showModifyUser(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/modify_user.fxml"));
        Parent root = loader.load();
        stage.setTitle("Modify User");
        stage.setScene(new Scene(root));
        stage.show();
    }

    @FXML
    private void goToAdminHome(javafx.event.ActionEvent event) throws java.io.IOException {
        javafx.stage.Stage stage = new javafx.stage.Stage();
        FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/admin_home.fxml"));
        javafx.scene.Parent root = loader.load();
        stage.setTitle("User Dashboard");
        stage.setScene(new javafx.scene.Scene(root));
        stage.show();
        ((javafx.stage.Stage)((javafx.scene.Node)event.getSource()).getScene().getWindow()).close();
    }

    @FXML
    private void goToCreateUser(javafx.event.ActionEvent event) throws java.io.IOException {
        javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/create_user.fxml"));
        javafx.scene.Parent root = loader.load();
        javafx.stage.Stage stage = new javafx.stage.Stage();
        stage.setTitle("Add User");
        stage.setScene(new javafx.scene.Scene(root));
        stage.show();
        ((javafx.stage.Stage)((javafx.scene.Node)event.getSource()).getScene().getWindow()).close();
    }

    @FXML
    private void goToUserView(javafx.event.ActionEvent event) throws java.io.IOException {
        javafx.stage.Stage stage = new javafx.stage.Stage();
        showModifyUser(stage);
        ((javafx.stage.Stage)((javafx.scene.Node)event.getSource()).getScene().getWindow()).close();
    }

    @FXML
    private void logout(javafx.event.ActionEvent event) throws java.io.IOException {
        models.CurrentUser.clear();
        javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/login.fxml"));
        javafx.scene.Parent root = loader.load();
        javafx.stage.Stage stage = new javafx.stage.Stage();
        stage.setTitle("Login");
        stage.setScene(new javafx.scene.Scene(root));
        stage.show();
        ((javafx.stage.Stage)((javafx.scene.Node)event.getSource()).getScene().getWindow()).close();
    }

    private boolean validateFields() {
        String name = nameField.getText().trim();
        String lastname = lastnameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();

        if (name.isEmpty() || lastname.isEmpty() || email.isEmpty() || password.isEmpty() ||
                dateOfBirthPicker.getValue() == null || roleComboBox.getValue() == null) {
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

        if (dateOfBirthPicker.getValue().isAfter(LocalDate.now())) {
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