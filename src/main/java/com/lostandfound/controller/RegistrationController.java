package com.lostandfound.controller;

import com.lostandfound.dao.UserDao;
import com.lostandfound.service.AuthService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;

public class RegistrationController {

    @FXML
    private TextField nameField;

    @FXML
    private TextField emailField;

    @FXML
    private TextField studentIdField;

    @FXML
    private PasswordField passwordField;

    private final AuthService authService = new AuthService(new UserDao());

    @FXML
    private void handleRegister(ActionEvent event) {
        try {
            String recoveryCode = authService.register(
                    nameField.getText(),
                    emailField.getText(),
                    studentIdField.getText(),
                    passwordField.getText()
            );

            showAlert(Alert.AlertType.INFORMATION, "Save Your Recovery Code",
                    "Registration successful!\n\nYour recovery code is:\n\n" + recoveryCode +
                            "\n\nSave this somewhere safe. You will need it to reset your password if you forget it.");

            goToLogin(event);

        } catch (IllegalArgumentException | IllegalStateException e) {
            showAlert(Alert.AlertType.ERROR, "Registration Failed", e.getMessage());
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

        alert.setResizable(true);
        alert.getDialogPane().getScene().getWindow().sizeToScene();

        alert.showAndWait();
    }
}