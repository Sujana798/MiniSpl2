package com.lostandfound.controller;

import com.lostandfound.dao.UserDao;
import com.lostandfound.service.AuthService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class ForgotPasswordController {

    @FXML
    private TextField emailField;

    @FXML
    private TextField recoveryCodeField;

    @FXML
    private PasswordField newPasswordField;

    @FXML
    private PasswordField confirmPasswordField;

    private final AuthService authService = new AuthService(new UserDao());

    @FXML
    private void handleResetPassword(ActionEvent event) {
        try {
            String newPassword = newPasswordField.getText();
            String confirmPassword = confirmPasswordField.getText();

            if (!newPassword.equals(confirmPassword)) {
                showAlert(Alert.AlertType.ERROR, "Password Mismatch", "The two passwords do not match.");
                return;
            }

            authService.resetPassword(
                    emailField.getText(),
                    recoveryCodeField.getText(),
                    newPassword
            );

            showAlert(Alert.AlertType.INFORMATION, "Success", "Your password has been reset. Please log in with your new password.");
            goToLogin(event);

        } catch (IllegalArgumentException | IllegalStateException e) {
            showAlert(Alert.AlertType.ERROR, "Reset Failed", e.getMessage());
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Unexpected Error", "Something went wrong: " + e.getMessage());
        }
    }

    @FXML
    private void handleBackToLogin(ActionEvent event) {
        goToLogin(event);
    }

    private void goToLogin(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/login.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 500, 500));
            stage.setTitle("Campus Lost & Found - Login");
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Could not open login screen.");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);

        Label content = new Label(message);
        content.setWrapText(true);
        content.setMaxWidth(380);
        content.setStyle("-fx-font-size: 13px;");

        alert.getDialogPane().setContent(content);
        alert.getDialogPane().setPrefWidth(430);

        alert.showAndWait();
    }
}