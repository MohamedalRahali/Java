package gui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.application.Platform;
import javafx.stage.Stage;
import Services.UserService;
import Services.GoogleAuthService;
import Services.OAuthCallbackServer;
import models.User;
import models.LoginAttempt;
import models.UserPreferences;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.awt.Desktop;
import java.net.URI;
import java.util.Arrays;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class LoginController {
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private CheckBox rememberMeCheckbox;
    private final UserService userService = new UserService();
    private GoogleAuthService googleAuthService;
    private OAuthCallbackServer callbackServer;
    private Timer cooldownTimer;
    private Alert cooldownAlert;

    @FXML
    private void initialize() {
        try {
            googleAuthService = new GoogleAuthService();
            // Check for remembered login
            if (UserPreferences.hasValidRememberMe()) {
                String rememberedEmail = UserPreferences.getRememberedEmail();
                String rememberedPassword = UserPreferences.getRememberedPassword();
                emailField.setText(rememberedEmail);
                passwordField.setText(rememberedPassword);
                // Auto login if not logged out in the last 30 days
                Platform.runLater(() -> {
                    if (!UserPreferences.wasExplicitlyLoggedOut()) {
                        login();
                    }
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Check if there's a saved email and if it's in cooldown
        String savedEmail = emailField.getText().trim();
        if (!savedEmail.isEmpty() && !LoginAttempt.canAttemptLogin(savedEmail)) {
            long[] cooldown = LoginAttempt.getRemainingCooldown(savedEmail);
            showCooldownAlert(cooldown[0], cooldown[1]);
        }
    }

    @FXML
    private void login() {
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();

        if (email.isEmpty() || password.isEmpty()) {
            showAlert("Error", "Please enter both email and password");
            return;
        }

        // Check if user can attempt login
        if (!LoginAttempt.canAttemptLogin(email)) {
            long[] cooldown = LoginAttempt.getRemainingCooldown(email);
            showCooldownAlert(cooldown[0], cooldown[1]);
            return;
        }

        try {
            System.out.println("Attempting to authenticate user: " + email);
            User user = userService.authenticate(email, password);
            if (user != null) {
                System.out.println("User authenticated successfully. Role: " + user.getRoles().get(0));
                if (user.getIsBlocked()) {
                    System.out.println("User is blocked. Access denied.");
                    showAlert("Access Denied", "Your account has been blocked. Please contact an administrator.");
                    return;
                }
                // Reset failed attempts on successful login
                LoginAttempt.resetAttempts(email);
                // Handle "Remember Me" functionality
                if (rememberMeCheckbox.isSelected()) {
                    LocalDateTime expiryDate = LocalDateTime.now().plusDays(30);
                    UserPreferences.saveRememberMe(email, password, expiryDate);
                    UserPreferences.setExplicitlyLoggedOut(false);
                } else {
                    UserPreferences.clearRememberMe();
                    UserPreferences.setExplicitlyLoggedOut(true);
                }
                // Store the current user information
                models.CurrentUser.setCurrentUser(user);
                loginSuccess(user);
            } else {
                LoginAttempt.recordFailedAttempt(email);
                int remainingAttempts = LoginAttempt.getRemainingAttempts(email);
                if (remainingAttempts <= 0) {
                    long[] cooldown = LoginAttempt.getRemainingCooldown(email);
                    showCooldownAlert(cooldown[0], cooldown[1]);
                } else {
                    showAlert("Error", "Invalid email or password. " + remainingAttempts + " attempts remaining.");
                }
            }
        } catch (Exception e) {
            System.out.println("Login error: " + e.getMessage());
            e.printStackTrace();
            showAlert("Error", "Login failed: " + e.getMessage());
        }
    }

    @FXML
    private void handleRegister() {
        goToRegister();
    }

    @FXML
    private void handleForgotPassword() {
        onForgotPassword();
    }

    @FXML
    private void handleGoogleLogin() {
        onGoogleSignIn();
    }

    @FXML
    private void handleExit() {
        // User is closing the window, do NOT touch explicitly_logged_out or clearRememberMe
        Stage stage = (Stage) emailField.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handleLogout() {
        // Called when user logs out
        UserPreferences.setExplicitlyLoggedOut(true);
        UserPreferences.clearRememberMe();
        // ... existing logout logic ...
    }

    @FXML
    private void goToRegister() {
        try {
            Stage currentStage = (Stage) emailField.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/fxml/register.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
            Stage stage = new Stage();
            stage.setTitle("Register");
            stage.setScene(scene);
            stage.show();
            currentStage.close();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to open register page: " + e.getMessage());
        }
    }

    @FXML
    private void exit() {
        Stage stage = (Stage) emailField.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void onGoogleSignIn() {
        try {
            // Start the callback server
            callbackServer = new OAuthCallbackServer();
            callbackServer.start();

            // Update the GoogleAuthService with the actual port
            googleAuthService.setPort(callbackServer.getPort());

            // Get the authorization URL
            String authUrl = googleAuthService.getAuthorizationUrl();

            // Open the default browser
            Desktop.getDesktop().browse(new URI(authUrl));

            // Wait for the authorization code
            callbackServer.getAuthorizationCode().thenAccept(code -> {
                try {
                    // Get user info from Google
                    GoogleAuthService.GoogleUserInfo userInfo = googleAuthService.handleCallback(code);

                    // Check if user exists
                    final User user = userService.getUserByEmail(userInfo.getEmail());
                    if (user == null) {
                        // Create new user
                        final User newUser = new User();
                        newUser.setEmail(userInfo.getEmail());

                        // Ensure username is not empty and unique
                        String baseUsername = userInfo.getName();
                        if (baseUsername == null || baseUsername.trim().isEmpty()) {
                            baseUsername = userInfo.getEmail().split("@")[0];
                        }
                        String username = baseUsername;
                        int suffix = 1;
                        List<User> allUsers = userService.getAll();
                        boolean exists = true;
                        while (exists) {
                            exists = false;
                            for (User u : allUsers) {
                                if (username.equalsIgnoreCase(u.getUsername())) {
                                    exists = true;
                                    username = baseUsername + suffix;
                                    suffix++;
                                    break;
                                }
                            }
                        }
                        newUser.setUsername(username);

                        newUser.setPassword(""); // Google-authenticated users don't need a password
                        newUser.setName(userInfo.getName());
                        newUser.setLastname(""); // Could be added to GoogleUserInfo if needed

                        // Always set role to USER for Google login
                        newUser.setRoles(Arrays.asList("USER"));

                        newUser.setDateOfBirth(new java.sql.Date(System.currentTimeMillis())); // Default date, can be updated later
                        newUser.setIsBlocked(false);
                        userService.ajouter(newUser);

                        // After registration, go directly to loginSuccess (no role prompt)
                        Platform.runLater(() -> {
                            try {
                                loginSuccess(newUser);
                            } catch (Exception e) {
                                showAlert("Error", "Failed to complete login: " + e.getMessage());
                            }
                        });
                    } else {
                        // Log in the existing user on the JavaFX Application Thread
                        Platform.runLater(() -> {
                            try {
                                loginSuccess(user);
                            } catch (Exception e) {
                                showAlert("Error", "Failed to complete login: " + e.getMessage());
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Platform.runLater(() -> showAlert("Error", "Failed to sign in with Google: " + e.getMessage()));
                } finally {
                    callbackServer.stop();
                }
            }).exceptionally(throwable -> {
                Platform.runLater(() -> showAlert("Error", "Failed to sign in with Google: " + throwable.getMessage()));
                callbackServer.stop();
                return null;
            });

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to initiate Google sign-in: " + e.getMessage());
        }
    }

    @FXML
    private void onForgotPassword() {
        try {
            Stage currentStage = (Stage) emailField.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader();
            java.net.URL fxmlUrl = getClass().getResource("/fxml/forgot_password.fxml");
            if (fxmlUrl == null) {
                showAlert("Error", "Could not find FXML resource: /fxml/forgot_password.fxml. Please check your build and classpath.");
                return;
            }
            loader.setLocation(fxmlUrl);
            Parent root = loader.load();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
            Stage stage = new Stage();
            stage.setTitle("Forgot Password");
            stage.setScene(scene);
            stage.show();
            currentStage.close();
        } catch (Exception e) {
            e.printStackTrace(); // Print the full stack trace for debugging
            showAlert("Error", "Failed to open forgot password page: " + e.toString());
        }
    }

    private void loginSuccess(User user) {
        try {
            // Clear any existing cooldown
            if (cooldownTimer != null) {
                cooldownTimer.cancel();
                cooldownTimer = null;
            }
            if (cooldownAlert != null) {
                cooldownAlert.close();
                cooldownAlert = null;
            }
            LoginAttempt.clearAttempts(user.getEmail());

            // Store the current user information
            models.CurrentUser.setCurrentUser(user);

            // Load the appropriate window based on user role
            Stage currentStage = (Stage) emailField.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader();
            
            String role = user.getRoles().get(0);
            String fxmlPath;
            String windowTitle;
            
            switch (role.toUpperCase()) {
                case "ADMIN":
                    fxmlPath = "/fxml/admin_home.fxml";
                    windowTitle = "Admin Dashboard";
                    break;
                case "ARTIST":
                    fxmlPath = "/fxml/artist_home.fxml";
                    windowTitle = "Artist Dashboard";
                    break;
                default: // "USER" and any other role
                    fxmlPath = "/fxml/user_home.fxml";
                    windowTitle = "User Dashboard";
                    break;
            }
            
            loader.setLocation(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            
            // Initialize the controller with user data
            Object controller = loader.getController();
            if (controller instanceof UserHomeController) {
                ((UserHomeController) controller).initData(user);
            } else if (controller instanceof ArtistHomeController) {
                ((ArtistHomeController) controller).initData(user);
            } else if (controller instanceof AdminHomeController) {
                ((AdminHomeController) controller).initData(user);
            }
            
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
            Stage stage = new Stage();
            stage.setTitle(windowTitle + " - " + user.getUsername());
            stage.setScene(scene);
            stage.show();
            currentStage.close();
        } catch (IOException e) {
            showAlert("Error", "Failed to open window: " + e.getMessage());
        }
    }

    private void promptRoleSelection(User user) {
        List<String> roles = Arrays.asList("USER", "ARTIST");
        ChoiceDialog<String> dialog = new ChoiceDialog<>(user.getRoles().get(0), roles);
        dialog.setTitle("Select Role");
        dialog.setHeaderText("Choose your role");
        dialog.setContentText("Role:");
        dialog.initOwner(emailField.getScene().getWindow());
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(selectedRole -> {
            if (!user.getRoles().contains(selectedRole)) {
                user.setRoles(Arrays.asList(selectedRole));
                userService.modifier(user);
                showAlert("Role Updated", "Your role has been set to: " + selectedRole);
            }
        });
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showCooldownAlert(long minutes, long seconds) {
        if (cooldownAlert != null) {
            cooldownAlert.close();
        }
        
        cooldownAlert = new Alert(Alert.AlertType.WARNING);
        cooldownAlert.setTitle("Login Disabled");
        cooldownAlert.setHeaderText(null);
        cooldownAlert.setContentText(String.format("Too many failed attempts. Please wait %d:%02d before trying again.", minutes, seconds));
        cooldownAlert.show();

        // Start timer to update the alert
        if (cooldownTimer != null) {
            cooldownTimer.cancel();
        }
        
        cooldownTimer = new Timer();
        cooldownTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                long[] cooldown = LoginAttempt.getRemainingCooldown(emailField.getText().trim());
                if (cooldown[0] <= 0 && cooldown[1] <= 0) {
                    cooldownTimer.cancel();
                    Platform.runLater(() -> {
                        if (cooldownAlert != null) {
                            cooldownAlert.close();
                            cooldownAlert = null;
                        }
                    });
                } else {
                    Platform.runLater(() -> {
                        if (cooldownAlert != null) {
                            cooldownAlert.setContentText(String.format("Too many failed attempts. Please wait %d:%02d before trying again.", 
                                cooldown[0], cooldown[1]));
                        }
                    });
                }
            }
        }, 1000, 1000); // Update every second
    }
} 