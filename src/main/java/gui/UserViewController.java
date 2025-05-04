package gui;

import Services.UserService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import models.User;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import java.util.Comparator;

public class UserViewController {
    @FXML
    private AdminBarController adminBarController;

    @FXML private ListView<User> userList;
    @FXML private TextField searchField;
    @FXML private ChoiceBox<String> sortChoiceBox;


    private final UserService service = new UserService();
    private ObservableList<User> userData;
    private FilteredList<User> filteredData;
    private SortedList<User> sortedData;

    @FXML
    public void initialize() {
        if (adminBarController != null && models.CurrentUser.getCurrentUser() != null) {
            adminBarController.setUserEmail(models.CurrentUser.getCurrentUser().getEmail());
        }
        System.out.println("Starting UserViewController initialization...");
        
        try {
            // Initialize sort choice box
            sortChoiceBox.getItems().clear();
            sortChoiceBox.getItems().addAll(
                "None",
                "Date of Birth (Ascending)",
                "Date of Birth (Descending)",
                "Blocked Users",
                "Unblocked Users"
            );
            sortChoiceBox.setValue("None");
            System.out.println("Sort choice box initialized");

            // Load users from database
            System.out.println("Attempting to load users from database...");
            List<User> users = service.getAll();
            System.out.println("Retrieved " + users.size() + " users from database");
            
            if (users.isEmpty()) {
                System.out.println("Warning: No users found in database");
            } else {
                for (User user : users) {
                    System.out.println("User: " + user.getName() + " " + user.getLastname());
                }
            }

            // Initialize data structures
            userData = FXCollections.observableArrayList(users);
            filteredData = new FilteredList<>(userData);
            sortedData = new SortedList<>(filteredData);
            System.out.println("Data structures initialized");

            // Configure ListView
            userList.getSelectionModel().setSelectionMode(javafx.scene.control.SelectionMode.SINGLE);
            userList.setCellFactory(lv -> new ListCell<User>() {
                @Override
                protected void updateItem(User user, boolean empty) {
                    super.updateItem(user, empty);
                    if (empty || user == null) {
                        setText(null);
                        setStyle("");
                    } else {
                        String status = user.getIsBlocked() ? "🔴 Blocked" : "🟢 Active";
                        String roles = user.getRoles() != null ? String.join(", ", user.getRoles()) : "No roles";
                        String dateStr = user.getDateOfBirth() != null ? user.getDateOfBirth().toString() : "No date";
                        String text = String.format(
                            "👤 %s %s\n" +
                            "📧 %s\n" +
                            "📅 %s\n" +
                            "👥 %s\n" +
                            "⚡ %s",
                            user.getName() != null ? user.getName() : "No name",
                            user.getLastname() != null ? user.getLastname() : "No lastname",
                            user.getEmail() != null ? user.getEmail() : "No email",
                            dateStr,
                            roles,
                            status
                        );
                        setText(text);
                        setWrapText(true);
                        // Highlight if selected
                        if (isSelected()) {
                            setStyle("-fx-background-color: #00b4d8; -fx-text-fill: #fff; -fx-font-size: 15px; -fx-padding: 10px; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, #31406022, 8, 0, 0, 0); -fx-border-color: #0077b6; -fx-border-width: 2px; -fx-border-radius: 10;");
                        } else {
                            setStyle("-fx-background-color: #fff; -fx-text-fill: #314060; -fx-font-size: 15px; -fx-padding: 10px; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, #31406022, 8, 0, 0, 0); -fx-border-color: #00b4d8; -fx-border-width: 1px; -fx-border-radius: 10;");
                        }
                    }
                }
            });
            userList.setItems(sortedData);
            System.out.println("ListView items set: " + userList.getItems().size());
            
            // Set up search
            searchField.textProperty().addListener((obs, oldValue, newValue) -> {
                filteredData.setPredicate(user -> {
                    if (newValue == null || newValue.isEmpty()) {
                        return true;
                    }
                    String lowerCaseFilter = newValue.toLowerCase();
                    return (user.getName() != null && user.getName().toLowerCase().contains(lowerCaseFilter)) ||
                           (user.getLastname() != null && user.getLastname().toLowerCase().contains(lowerCaseFilter)) ||
                           (user.getEmail() != null && user.getEmail().toLowerCase().contains(lowerCaseFilter));
                });
            });
            System.out.println("Search functionality configured");

            // Set up sorting
            sortChoiceBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    if (newVal.equals("Blocked Users")) {
                        filteredData.setPredicate(user -> user.getIsBlocked());
                    } else if (newVal.equals("Unblocked Users")) {
                        filteredData.setPredicate(user -> !user.getIsBlocked());
                    } else if (newVal.equals("Date of Birth (Ascending)")) {
                        filteredData.setPredicate(user -> true);
                        sortedData.setComparator(Comparator.comparing(User::getDateOfBirth, Comparator.nullsLast(Comparator.naturalOrder())));
                    } else if (newVal.equals("Date of Birth (Descending)")) {
                        filteredData.setPredicate(user -> true);
                        sortedData.setComparator(Comparator.comparing(User::getDateOfBirth, Comparator.nullsLast(Comparator.reverseOrder())));
                    } else {
                        filteredData.setPredicate(user -> true);
                        sortedData.setComparator(null);
                    }
                }
            });
            System.out.println("Sorting functionality configured");

            System.out.println("UserViewController initialization completed successfully");
            
        } catch (Exception e) {
            System.err.println("Error in UserViewController initialization: " + e.getMessage());
            e.printStackTrace();
            showAlert("Error loading users: " + e.getMessage());
        }
    }

    private void loadUsers() {
        userData.setAll(service.getAll());
    }

    @FXML
    private void handleEditUser() throws IOException {
        User selectedUser = userList.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            showAlert("Please select a user to modify.");
            return;
        }
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/modify_user.fxml"));
        Parent root = loader.load();
        ModifyUserController controller = loader.getController();
        controller.setUserToEdit(selectedUser);
        Stage stage = (Stage) userList.getScene().getWindow();
        stage.setTitle("Modify User");
        stage.setScene(new Scene(root));
    }

    @FXML
    private void handleDeleteUser() {
        User selectedUser = userList.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            showAlert("Please select a user to delete.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Delete User");
        alert.setContentText("Are you sure you want to delete this user?");
        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            service.supprimer(selectedUser.getId());
            loadUsers(); // Refresh the list
        }
    }

    @FXML
    private void goBackToHome() throws IOException {
        loadScene(null, "/fxml/admin_home.fxml", "Admin Home");
    }

    @FXML
    private void logout(ActionEvent event) throws IOException {
        models.CurrentUser.clear();
        loadScene(event, "/fxml/login.fxml", "Login");
    }

    @FXML
    private void handleBlockUser() {
        User selectedUser = userList.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            showAlert("Please select a user to block/unblock.");
            return;
        }

        String action = selectedUser.getIsBlocked() ? "unblock" : "block";
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText(action.substring(0, 1).toUpperCase() + action.substring(1) + " User");
        alert.setContentText("Are you sure you want to " + action + " this user?");
        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                selectedUser.setIsBlocked(!selectedUser.getIsBlocked());
                service.modifier(selectedUser);
                loadUsers(); // Refresh the list
                showAlert("User " + action + "ed successfully!");
            } catch (Exception e) {
                showAlert("Error " + action + "ing user: " + e.getMessage());
            }
        }
    }

    private void showAlert(String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void loadScene(ActionEvent event, String fxmlPath, String title) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
        Stage stage = event != null ? (Stage)((Node)event.getSource()).getScene().getWindow() : (Stage) userList.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle(title);
    }

    @FXML
    private void goToUserView(ActionEvent event) throws IOException {
        loadScene(event, "/fxml/user_view.fxml", "User Table");
    }

    @FXML
    private void goToCreateUser(ActionEvent event) throws IOException {
        loadScene(event, "/fxml/create_user.fxml", "Add User");
    }

    @FXML
    private void goToAdminHome(ActionEvent event) throws IOException {
        loadScene(event, "/fxml/admin_home.fxml", "User Dashboard");
    }
}
