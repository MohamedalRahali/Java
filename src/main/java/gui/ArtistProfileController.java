package gui;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import models.CurrentUser;
import models.User;
import Services.UserService;
import java.time.LocalDate;

public class ArtistProfileController {
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private DatePicker dobPicker;
    @FXML private Button saveButton;
    @FXML private Label statusLabel;

    private User user;

    @FXML
    public void initialize() {
        user = CurrentUser.getCurrentUser();
        if (user != null) {
            firstNameField.setText(user.getName());
            lastNameField.setText(user.getLastname());
            emailField.setText(user.getEmail());
            passwordField.setText(user.getPassword());
            if (user.dateOfBirthProperty().get() != null) {
                java.util.Date dob = user.dateOfBirthProperty().get();
                if (dob instanceof java.sql.Date) {
                    dobPicker.setValue(((java.sql.Date) dob).toLocalDate());
                } else {
                    dobPicker.setValue(dob.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate());
                }
            }
        }
        statusLabel.setText("");
        saveButton.setOnAction(this::handleSave);
    }

    private void handleSave(ActionEvent event) {
        if (user != null) {
            user.setName(firstNameField.getText());
            user.setLastname(lastNameField.getText());
            user.setEmail(emailField.getText());
            user.setPassword(passwordField.getText());
            LocalDate dob = dobPicker.getValue();
            if (dob != null) {
                java.sql.Date sqlDate = java.sql.Date.valueOf(dob);
                user.setDateOfBirth(sqlDate);
            }
            // Save user changes to DB
            new UserService().modifier(user);
            statusLabel.setText("Profile updated successfully.");
        }
    }
}
