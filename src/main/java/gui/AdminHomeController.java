package gui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import models.CurrentUser;
import models.User;

import java.io.IOException;
import java.util.List;

public class AdminHomeController {
    @FXML
    private AdminBarController adminBarController;

    @FXML
    private Label totalUsersLabel;
    @FXML
    private Label adminCountLabel;
    @FXML
    private Label artistCountLabel;
    @FXML
    private Label userCountLabel;
    @FXML
    private Label blockedCountLabel;
    @FXML
    private PieChart blockedPieChart;
    
    private User currentUser;
    private final Services.UserService userService = new Services.UserService();
    
    @FXML
    private void initialize() {
        if (adminBarController != null && CurrentUser.getCurrentUser() != null) {
            adminBarController.setUserEmail(CurrentUser.getCurrentUser().getEmail());
        }
        // Display current user's email if available

        // Populate dashboard stats
        updateDashboardStats();
    }

    public void initData(User user) {
        this.currentUser = user;
        // Set user email in AdminBarController after loading FXML
    }

    @FXML
    private void manageUsers(ActionEvent event) throws IOException {
        loadScene(event, "/fxml/user_view.fxml", "User List");
    }

    @FXML
    private void createUser(ActionEvent event) throws IOException {
        loadScene(event, "/fxml/create_user.fxml", "Create User");
    }

    @FXML
    private void logout(ActionEvent event) throws IOException {
        // Clear current user information
        models.CurrentUser.clear();
        loadScene(event, "/fxml/login.fxml", "Login");
    }

    @FXML
    private void goToAdminHome(ActionEvent event) throws IOException {
        loadScene(event, "/fxml/admin_home.fxml", "User Dashboard");
    }

    @FXML
    private void goToHomeReclamation(ActionEvent event) throws IOException {
        loadScene(event, "/fxml/HomeReclamation.fxml", "Home Reclamation");
    }

    @FXML
    private void logoutSidebar(ActionEvent event) throws IOException {
        models.CurrentUser.clear();
        loadScene(event, "/fxml/login.fxml", "Login");
    }

    @FXML
    private void goToCreateUser(ActionEvent event) throws IOException {
        loadScene(event, "/fxml/create_user.fxml", "Add User");
    }

    @FXML
    private void goToUserView(ActionEvent event) throws IOException {
        loadScene(event, "/fxml/user_view.fxml", "User Table");
    }

    private void loadScene(ActionEvent event, String fxmlPath, String title) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
        Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root, 800, 600)); 
        stage.setTitle(title);
    }

    private void showAdminHome(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin_home.fxml"));
        Parent root = loader.load();
        stage.setTitle("Admin Home");
        stage.setScene(new Scene(root, 800, 600)); 
        stage.show();
    }

    private void updateDashboardStats() {
        List<models.User> users = userService.getAll();
        int total = users.size();
        int admins = 0, artists = 0, regularUsers = 0, blocked = 0;
        int blockedAdmins = 0, blockedArtists = 0, blockedUsers = 0;
        for (models.User u : users) {
            boolean isBlocked = u.getIsBlocked();
            if (u.getRoles() != null) {
                for (String role : u.getRoles()) {
                    switch (role.trim().toUpperCase()) {
                        case "ADMIN":
                            admins++;
                            if (isBlocked) blockedAdmins++;
                            break;
                        case "ARTIST":
                            artists++;
                            if (isBlocked) blockedArtists++;
                            break;
                        case "USER":
                            regularUsers++;
                            if (isBlocked) blockedUsers++;
                            break;
                    }
                }
            }
            if (isBlocked) blocked++;
        }
        totalUsersLabel.setText(String.valueOf(total));
        adminCountLabel.setText(String.valueOf(admins));
        artistCountLabel.setText(String.valueOf(artists));
        userCountLabel.setText(String.valueOf(regularUsers));
        blockedCountLabel.setText(String.valueOf(blocked));
        // Pie chart: % blocked by type
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
                new PieChart.Data("Blocked Admins", admins == 0 ? 0 : 100.0 * blockedAdmins / admins),
                new PieChart.Data("Blocked Artists", artists == 0 ? 0 : 100.0 * blockedArtists / artists),
                new PieChart.Data("Blocked Users", regularUsers == 0 ? 0 : 100.0 * blockedUsers / regularUsers)
        );
        blockedPieChart.setData(pieChartData);
        blockedPieChart.setTitle("Blocked % by Type");
        blockedPieChart.setLabelsVisible(true);
        blockedPieChart.setLegendVisible(true);
        // Set colors and label style for visibility
        setPieChartColorsAndLabels();
    }

    private void setPieChartColorsAndLabels() {
        // Set custom colors for each slice
        String[] colors = {"#ff6f3c", "#ffc300", "#43aa8b"}; // Admin: orange, Artist: yellow, User: green
        int i = 0;
        for (PieChart.Data data : blockedPieChart.getData()) {
            data.getNode().setStyle("-fx-pie-color: " + colors[i % colors.length] + ";");
            i++;
        }
        // Improve legend and label text color and size for visibility
        blockedPieChart.lookupAll(".chart-legend-item").forEach(node -> node.setStyle("-fx-text-fill: #222; -fx-font-weight: bold; -fx-font-size: 16px;"));
        blockedPieChart.lookupAll(".chart-pie-label").forEach(node -> node.setStyle("-fx-text-fill: #222; -fx-font-weight: bold; -fx-font-size: 16px;"));
        // Make legend background more visible
        blockedPieChart.lookupAll(".chart-legend").forEach(node -> node.setStyle("-fx-background-color: #fff; -fx-border-color: #00b4d8; -fx-border-width: 1; -fx-padding: 8;"));
    }
}
