package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import models.User;
import service.user.UserLogService;
import service.user.UserService;
import utils.Session;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

public class AdminDashboardController {

    @FXML private Label totalUsersLabel;
    @FXML private Label activeUsersLabel;
    @FXML private Label bannedUsersLabel;
    @FXML private Label todayLoginsLabel;
    @FXML private Label pendingAdminsLabel;

    private final UserService userService = new UserService();
    private final UserLogService userLogService = new UserLogService();

    @FXML
    public void initialize() {
        loadDashboardData();
    }

    private void loadDashboardData() {
        List<User> allUsers = userService.getAllUsers();

        int totalUsers = allUsers.size();
        int activeUsers = (int) allUsers.stream().filter(user -> !user.isBanned()).count();
        int bannedUsers = (int) allUsers.stream().filter(User::isBanned).count();
        int pendingAdmins = (int) allUsers.stream()
                .filter(user -> user.getRoles().equals("ROLE_ADMIN") && !user.isApproved())
                .count();

        int todayLogins = userLogService.countLoginsToday();

        totalUsersLabel.setText(String.valueOf(totalUsers));
        activeUsersLabel.setText(String.valueOf(activeUsers));
        bannedUsersLabel.setText(String.valueOf(bannedUsers));
        todayLoginsLabel.setText(String.valueOf(todayLogins));
        pendingAdminsLabel.setText(String.valueOf(pendingAdmins));
    }

    @FXML
    private void handleCloseDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/user/Admin.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) totalUsersLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Gestion Utilisateurs - EduTech");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
