package com.lostandfound.controller;


import com.lostandfound.dao.UserDao;
import com.lostandfound.model.User;
import com.lostandfound.service.AuthService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    private final AuthService authService = new AuthService(new UserDao());

    @FXML
    private void handleLogin(ActionEvent event) {
        try {
            User user = authService.login(emailField.getText(), passwordField.getText());
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            showSuccessScreen(user, stage);

        } catch (IllegalArgumentException e) {
            showAlert(Alert.AlertType.ERROR, "Login Failed", e.getMessage());
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Unexpected Error", "Something went wrong: " + e.getMessage());
        }
    }
    @FXML
    private void handleForgotPassword(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/forgot_password.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 450, 480));
            stage.setTitle("Campus Lost & Found - Reset Password");
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Could not open reset password screen.");
        }
    }
    @FXML
    private void handleGoToRegister(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/registration.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 400, 400));
            stage.setTitle("Campus Lost & Found - Register");
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Could not open registration screen.");
        }
    }

    private void showSuccessScreen(User user, Stage stage) {
        Label title = new Label("Login Successful");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        Label nameLabel = new Label("Welcome, " + user.getName() + "!");
        Label roleLabel = new Label("Role: " + user.getRole());

        Button logoutButton = new Button("Log Out");
        logoutButton.setOnAction(e -> {
            try {
                Parent root = FXMLLoader.load(getClass().getResource("/fxml/login.fxml"));
                stage.setScene(new Scene(root, 400, 350));
                stage.setTitle("Campus Lost & Found - Login");
            } catch (IOException ex) {
                showAlert(Alert.AlertType.ERROR, "Navigation Error", "Could not return to login screen.");
            }
        });

        VBox layout = new VBox(15, title, nameLabel, roleLabel, logoutButton);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(30));

        stage.setScene(new Scene(layout, 400, 300));
        stage.setTitle("Campus Lost & Found - Welcome");
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}