package com.lostlink.controller;


import com.lostlink.dao.SQLiteUserDAO;
import com.lostlink.service.AuthService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class RegistrationController {

    @FXML
    private TextField nameField;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private ComboBox<String> roleComboBox;

    private final AuthService authService = new AuthService(new SQLiteUserDAO());

    @FXML
    private void initialize() {
        roleComboBox.getItems().addAll("REPORTER", "ADMIN_VERIFIER");
        roleComboBox.getSelectionModel().selectFirst();
    }

    @FXML
    private void handleRegister(ActionEvent event) {
        try {
            String role = roleComboBox.getValue();
            authService.register(
                    nameField.getText(),
                    emailField.getText(),
                    passwordField.getText(),
                    role
            );

            showAlert(Alert.AlertType.INFORMATION, "Registration Successful",
                    "Account created successfully. You can now log in.");

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
            stage.setScene(new Scene(root, 400, 350));
            stage.setTitle("LostLink - Login");
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Could not open login screen.");
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