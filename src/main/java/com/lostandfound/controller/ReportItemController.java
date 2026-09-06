package com.lostandfound.controller;

import com.lostandfound.dao.ItemReportDao;
import com.lostandfound.dao.MatchDao;
import com.lostandfound.dao.UserDao;
import com.lostandfound.model.User;
import com.lostandfound.service.ItemReportService;
import com.lostandfound.service.MatchService;
import com.lostandfound.service.WeightedMatchingStrategy;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class ReportItemController {

    @FXML
    private ComboBox<String> typeComboBox;

    @FXML
    private ComboBox<String> categoryComboBox;

    @FXML
    private TextField titleField;

    @FXML
    private TextField brandField;

    @FXML
    private TextField colorField;

    @FXML
    private TextField locationField;

    @FXML
    private DatePicker datePicker;

    @FXML
    private TextArea descriptionField;

    private final ItemReportService itemReportService = new ItemReportService(
            new ItemReportDao(),
            new MatchService(new ItemReportDao(), new MatchDao(), new WeightedMatchingStrategy(), new UserDao())
    );

    private User currentUser;

    @FXML
    private void initialize() {
        typeComboBox.getItems().addAll("LOST", "FOUND");
        categoryComboBox.getItems().addAll("Electronics", "Documents", "Bags", "Clothing", "Accessories", "Others");
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    @FXML
    private void handleSubmit() {
        try {
            String dateStr = datePicker.getValue() != null ? datePicker.getValue().toString() : null;

            itemReportService.submitReport(
                    currentUser.getUserId(),
                    typeComboBox.getValue(),
                    categoryComboBox.getValue(),
                    brandField.getText(),
                    colorField.getText(),
                    titleField.getText(),
                    descriptionField.getText(),
                    locationField.getText(),
                    dateStr
            );

            showAlert(Alert.AlertType.INFORMATION, "Success", "Your report has been submitted successfully.");
            clearForm();

        } catch (IllegalArgumentException | IllegalStateException e) {
            showAlert(Alert.AlertType.ERROR, "Submission Failed", e.getMessage());
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Unexpected Error", "Something went wrong: " + e.getMessage());
        }
    }

    private void clearForm() {
        typeComboBox.getSelectionModel().clearSelection();
        categoryComboBox.getSelectionModel().clearSelection();
        titleField.clear();
        brandField.clear();
        colorField.clear();
        locationField.clear();
        datePicker.setValue(null);
        descriptionField.clear();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);

        Label content = new Label(message);
        content.setWrapText(true);
        content.setMaxWidth(380);

        alert.getDialogPane().setContent(content);
        alert.getDialogPane().setPrefWidth(430);

        alert.showAndWait();
    }
}