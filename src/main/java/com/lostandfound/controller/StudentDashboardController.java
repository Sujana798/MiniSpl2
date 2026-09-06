package com.lostandfound.controller;

import com.lostandfound.model.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import com.lostandfound.dao.ItemReportDao;
import com.lostandfound.model.ItemReport;
import javafx.scene.layout.GridPane;
import java.util.List;
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
        buildHomeContent();
    }

    @FXML
    private void handleDashboardHome() {
        buildHomeContent();
    }

    private void buildHomeContent() {
        contentArea.getChildren().clear();
        contentArea.setSpacing(20);
        contentArea.setStyle("-fx-padding: 30; -fx-background-color: #f4f6f8;");

        Label heading = new Label("Dashboard Overview");
        heading.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #333333;");

        HBox statRow = new HBox(20);
        statRow.getChildren().addAll(
                buildStatCard("0", "My Reports"),
                buildStatCard("0", "Active Claims"),
                buildStatCard("0", "Notifications")
        );

        HBox ctaCard = new HBox(20);
        ctaCard.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        ctaCard.getStyleClass().add("cta-card");

        VBox ctaText = new VBox(5);
        HBox.setHgrow(ctaText, Priority.ALWAYS);
        Label ctaTitle = new Label("Lost or Found something on campus?");
        ctaTitle.getStyleClass().add("cta-title");
        Label ctaSubtitle = new Label("Report it now and let the matching system find it for you.");
        ctaSubtitle.getStyleClass().add("cta-subtitle");
        ctaText.getChildren().addAll(ctaTitle, ctaSubtitle);

        Button ctaButton = new Button("+ Report Item");
        ctaButton.getStyleClass().add("cta-button");
        ctaButton.setOnAction(e -> handleReportItem());

        ctaCard.getChildren().addAll(ctaText, ctaButton);

        VBox recentActivity = new VBox(10);
        recentActivity.setStyle("-fx-padding: 10 0 0 0;");
        Label recentTitle = new Label("Recent Activity");
        recentTitle.setStyle("-fx-font-size: 15px; -fx-font-weight: bold;");
        Label recentEmpty = new Label("No recent activity yet.");
        recentEmpty.setStyle("-fx-text-fill: #999; -fx-font-size: 12px;");
        recentActivity.getChildren().addAll(recentTitle, recentEmpty);

        contentArea.getChildren().addAll(heading, statRow, ctaCard, recentActivity);
    }

    private VBox buildStatCard(String number, String label) {
        VBox card = new VBox();
        card.getStyleClass().add("stat-card");
        HBox.setHgrow(card, Priority.ALWAYS);
        card.setMaxWidth(Double.MAX_VALUE);

        Label numberLabel = new Label(number);
        numberLabel.getStyleClass().add("stat-number");
        Label textLabel = new Label(label);
        textLabel.getStyleClass().add("stat-label");

        card.getChildren().addAll(numberLabel, textLabel);
        return card;
    }

    @FXML
    private void handleReportItem() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/report_item.fxml"));
            Parent formRoot = loader.load();

            ReportItemController controller = loader.getController();
            controller.setCurrentUser(currentUser);

            contentArea.getChildren().clear();
            contentArea.getChildren().add(formRoot);
            VBox.setVgrow(formRoot, Priority.ALWAYS);

        } catch (IOException e) {
            showAlert("Could not load the report form.");
        }
    }

    @FXML
    private void handleMyReports() {
        contentArea.getChildren().clear();
        contentArea.setStyle("-fx-padding: 30; -fx-background-color: #f4f6f8;");
        contentArea.setSpacing(15);

        Label heading = new Label("My Reports");
        heading.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        contentArea.getChildren().add(heading);

        ItemReportDao itemReportDao = new ItemReportDao();
        List<ItemReport> myReports = itemReportDao.findByReporterId(currentUser.getUserId());

        if (myReports.isEmpty()) {
            Label emptyLabel = new Label("You haven't submitted any reports yet.");
            emptyLabel.setStyle("-fx-text-fill: #777;");
            contentArea.getChildren().add(emptyLabel);
            return;
        }

        for (ItemReport report : myReports) {
            contentArea.getChildren().add(buildReportCard(report));
        }
    }

    private VBox buildReportCard(ItemReport report) {
        VBox card = new VBox(6);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-padding: 15; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);");

        Label titleLabel = new Label(report.getTitle() + "  (" + report.getType() + ")");
        titleLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold;");

        Label detailsLabel = new Label(
                "Category: " + report.getCategory() +
                        "   |   Location: " + report.getLocation() +
                        "   |   Date: " + report.getDateOccurred()
        );
        detailsLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #555;");

        Label statusLabel = new Label("Status: " + report.getStatus());
        statusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #145DA0; -fx-font-weight: bold;");

        card.getChildren().addAll(titleLabel, detailsLabel, statusLabel);
        return card;
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
        contentArea.setStyle("-fx-padding: 40;");
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        Label messageLabel = new Label(message);
        messageLabel.setStyle("-fx-text-fill: #777;");
        contentArea.getChildren().addAll(titleLabel, messageLabel);
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message);
        alert.showAndWait();
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/login.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 500, 500));
            stage.setTitle("Campus Lost & Found - Login");
        } catch (IOException e) {
            showAlert("Could not return to login screen.");
        }
    }
}