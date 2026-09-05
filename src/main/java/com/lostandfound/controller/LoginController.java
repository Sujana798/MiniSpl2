package com.lostandfound.controller;

import com.lostandfound.controller.StudentDashboardController;
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
            goToDashboard(user, stage);

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

    @FXML
    private void goToDashboard(User user, Stage stage) {
        try {
            String fxmlPath = user.getRole().equals("ADMIN")
                    ? "/fxml/dashboard_admin.fxml"
                    : "/fxml/dashboard_student.fxml";

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            if (!user.getRole().equals("ADMIN")) {
                StudentDashboardController controller = loader.getController();
                controller.setCurrentUser(user);
            }

            stage.setScene(new Scene(root, 800, 550));
            stage.setTitle("Campus Lost & Found - Dashboard");
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Could not load dashboard.");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}