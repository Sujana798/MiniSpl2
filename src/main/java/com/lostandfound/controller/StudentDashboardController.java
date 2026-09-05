package com.lostandfound.controller;

import com.lostandfound.model.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

public class StudentDashboardController {

    @FXML
    private Label welcomeLabel;

    @FXML
    private VBox contentArea;

    private User currentUser;

    public void setCurrentUser(User user) {
        this.currentUser = user;
        welcomeLabel.setText("Welcome, " + user.getName());
    }

    @FXML
    private void handleReportItem() {
        showPlaceholder("Report Lost/Found Item", "This feature is coming soon.");
    }

    @FXML
    private void handleMyReports() {
        showPlaceholder("My Reports", "This feature is coming soon.");
    }

    @FXML
    private void handleBrowseItems() {
        showPlaceholder("Browse Items", "This feature is coming soon.");
    }

    @FXML
    private void handleMyClaims() {
        showPlaceholder("My Claims", "This feature is coming soon.");
    }

    @FXML
    private void handleNotifications() {
        showPlaceholder("Notifications", "This feature is coming soon.");
    }

    private void showPlaceholder(String title, String message) {
        contentArea.getChildren().clear();
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        Label messageLabel = new Label(message);
        messageLabel.setStyle("-fx-text-fill: #777;");
        contentArea.getChildren().addAll(titleLabel, messageLabel);
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/login.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 500, 500));
            stage.setTitle("Campus Lost & Found - Login");
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Could not return to login screen.");
            alert.showAndWait();
        }
    }
}