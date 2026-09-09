package com.lostandfound.controller;

import com.lostandfound.dao.ClaimDao;
import com.lostandfound.dao.ItemReportDao;
import com.lostandfound.dao.MatchDao;
import com.lostandfound.dao.UserDao;
import com.lostandfound.model.Claim;
import com.lostandfound.model.ItemReport;
import com.lostandfound.model.Match;
import com.lostandfound.model.User;
import com.lostandfound.notification.NotificationPublisher;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.List;
import com.lostandfound.dao.NotificationDao;
import com.lostandfound.model.Notification;
import com.lostandfound.state.ReportState;
import com.lostandfound.state.ReportStateFactory;

public class AdminDashboardController {

    @FXML
    private Label welcomeLabel;

    @FXML
    private VBox contentArea;

    private User currentUser;
    private final UserDao userDao = new UserDao();

    public void setCurrentUser(User user) {
        this.currentUser = user;
        if (!"ADMIN".equalsIgnoreCase(user.getRole())) {
            throw new IllegalStateException("AdminDashboardController requires a user with ADMIN role.");
        }

        welcomeLabel.setText("Welcome, " + user.getName() + " (Admin)");
        handleDashboardOverview();
    }

    @FXML
    private void handleDashboardOverview() {
        buildDashboardOverview();
    }

    @FXML
    private void handleManageReports() {
        showPlaceholderPanel("Lost & Found Report Management",
                "Review, filter, and manage all lost and found item reports submitted by students.");
    }

    @FXML
    private void handleMatchReview() {
        showPlaceholderPanel("Match Review",
                "Review candidate matches generated between lost and found reports.");
    }

    @FXML
    private void handleClaimVerification() {
        buildClaimVerification();
    }

    @FXML
    private void handleUserManagement() {
        buildUserManagement();
    }

    @FXML
    private void handleAnalytics() {
        showPlaceholderPanel("Reports & Analytics",
                "Basic analytics such as open cases, resolved cases, and average resolution time.");
    }

    @FXML
    private void handleNotifications() {
        contentArea.getChildren().clear();
        contentArea.setSpacing(15);

        Label heading = new Label("Notifications");
        heading.getStyleClass().add("dashboard-heading");
        contentArea.getChildren().add(heading);

        NotificationDao notificationDao = new NotificationDao();
        List<Notification> notifications = notificationDao.findByUserId(currentUser.getUserId());

        if (notifications.isEmpty()) {
            Label emptyLabel = new Label("No notifications yet.");
            emptyLabel.setStyle("-fx-text-fill: #777;");
            contentArea.getChildren().add(emptyLabel);
            return;
        }

        for (Notification notification : notifications) {
            contentArea.getChildren().add(buildAdminNotificationCard(notification, notificationDao));
        }
    }

    private VBox buildAdminNotificationCard(Notification notification, NotificationDao notificationDao) {
        VBox card = new VBox(6);
        String bgColor = notification.isRead() ? "white" : "#eaf2fb";
        card.setStyle("-fx-background-color: " + bgColor + "; -fx-background-radius: 10; -fx-padding: 15; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);");

        Label messageLabel = new Label(notification.getMessage());
        messageLabel.setStyle("-fx-font-size: 13px; " + (notification.isRead() ? "" : "-fx-font-weight: bold;"));
        messageLabel.setWrapText(true);

        Label timeLabel = new Label(notification.getCreatedAt());
        timeLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #999;");

        card.getChildren().addAll(messageLabel, timeLabel);

        if (!notification.isRead()) {
            Button markReadBtn = new Button("Mark as Read");
            markReadBtn.getStyleClass().add("secondary-button");
            markReadBtn.setOnAction(e -> {
                notificationDao.markAsRead(notification.getNotificationId());
                handleNotifications();
            });
            card.getChildren().add(markReadBtn);
        }

        return card;
    }
    @FXML
    private void handleLogout(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/login.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 500, 500));
            stage.setTitle("Campus Lost & Found - Login");
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Could not return to login screen.");
        }
    }

    private void buildDashboardOverview() {
        VBox panel = new VBox(20);

        Label heading = new Label("Admin Dashboard Overview");
        heading.getStyleClass().add("dashboard-heading");

        int totalUsers = userDao.getAllUsers().size();
        int totalStudents = userDao.countByRole("STUDENT");
        int totalAdmins = userDao.countByRole("ADMIN");
        long pendingClaims = new ClaimDao().findAll().stream()
                .filter(c -> "PENDING".equals(c.getStatus()))
                .count();
        long unreadNotifs = new NotificationDao().countUnread(currentUser.getUserId());

        HBox statRow = new HBox(20,
                createStatCard(String.valueOf(totalUsers), "Total Users"),
                createStatCard(String.valueOf(totalStudents), "Students"),
                createStatCard(String.valueOf(totalAdmins), "Admins"),
                createStatCard(String.valueOf(pendingClaims), "Pending Claims"));

        HBox ctaCard = new HBox(20);
        ctaCard.getStyleClass().add("cta-card");
        ctaCard.setAlignment(Pos.CENTER_LEFT);

        VBox ctaText = new VBox(5);
        Label ctaTitle = new Label("Priority Workflow: Claim -> Verification -> Approve/Reject -> Return -> Closure");
        ctaTitle.getStyleClass().add("cta-title");
        ctaTitle.setWrapText(true);
        Label ctaSubtitle = new Label("Review pending claims and move them through verification to case closure.");
        ctaSubtitle.getStyleClass().add("cta-subtitle");
        ctaText.getChildren().addAll(ctaTitle, ctaSubtitle);
        HBox.setHgrow(ctaText, Priority.ALWAYS);

        Button ctaButton = new Button("Review Pending Claims");
        ctaButton.getStyleClass().add("cta-button");
        ctaButton.setOnAction(e -> handleClaimVerification());

        ctaCard.getChildren().addAll(ctaText, ctaButton);

        panel.getChildren().addAll(heading, statRow, ctaCard);
        contentArea.getChildren().setAll(panel);
    }

    private void buildUserManagement() {
        VBox panel = new VBox(15);

        Label heading = new Label("User Management");
        heading.getStyleClass().add("dashboard-heading");

        Label note = new Label("All registered users (read-only view).");
        note.getStyleClass().add("section-note");

        TableView<User> table = new TableView<>();
        table.setItems(FXCollections.observableArrayList(userDao.getAllUsers()));

        TableColumn<User, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("userId"));

        TableColumn<User, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<User, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));

        TableColumn<User, String> studentIdCol = new TableColumn<>("Student ID");
        studentIdCol.setCellValueFactory(new PropertyValueFactory<>("studentId"));

        TableColumn<User, String> roleCol = new TableColumn<>("Role");
        roleCol.setCellValueFactory(new PropertyValueFactory<>("role"));

        table.getColumns().addAll(idCol, nameCol, emailCol, studentIdCol, roleCol);
        table.setPrefHeight(320);

        panel.getChildren().addAll(heading, note, table);
        contentArea.getChildren().setAll(panel);
    }

    private void buildClaimVerification() {
        VBox panel = new VBox(15);

        Label heading = new Label("Claim Verification");
        heading.getStyleClass().add("dashboard-heading");

        Label note = new Label(
                "Workflow: Claim -> Verification -> Approve/Reject -> Return Confirmation -> Case Closure.");
        note.getStyleClass().add("section-note");
        note.setWrapText(true);

        ClaimDao claimDao = new ClaimDao();
        MatchDao matchDao = new MatchDao();
        ItemReportDao itemReportDao = new ItemReportDao();

        List<Claim> claims = claimDao.findAll();

        VBox listBox = new VBox(12);

        if (claims.isEmpty()) {
            Label emptyLabel = new Label("No claims submitted yet.");
            emptyLabel.setStyle("-fx-text-fill: #777;");
            listBox.getChildren().add(emptyLabel);
        } else {
            for (Claim claim : claims) {
                listBox.getChildren().add(buildClaimCard(claim, matchDao, itemReportDao, claimDao));
            }
        }

        panel.getChildren().addAll(heading, note, listBox);
        contentArea.getChildren().setAll(panel);
    }

    private VBox buildClaimCard(Claim claim, MatchDao matchDao, ItemReportDao itemReportDao, ClaimDao claimDao) {
        VBox card = new VBox(8);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-padding: 15; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);");

        Match match = matchDao.findAll().stream()
                .filter(m -> m.getMatchId() == claim.getMatchId())
                .findFirst()
                .orElse(null);

        String itemInfo = "Unknown item";
        if (match != null) {
            ItemReport lost = itemReportDao.findById(match.getLostReportId());
            ItemReport found = itemReportDao.findById(match.getFoundReportId());
            itemInfo = "Lost: " + (lost != null ? lost.getTitle() : "N/A") +
                    "   |   Found: " + (found != null ? found.getTitle() : "N/A") +
                    String.format("   |   Score: %.0f%%", match.getMatchScore());
        }

        User claimant = userDao.findById(claim.getClaimantId());
        String claimantName = claimant != null ? claimant.getName() : "Unknown";

        Label claimLabel = new Label("Claim #" + claim.getClaimId() + " by " + claimantName);
        claimLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        Label detailsLabel = new Label(itemInfo);
        detailsLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #555;");
        detailsLabel.setWrapText(true);

        String statusColor = switch (claim.getStatus()) {
            case "PENDING" -> "#b8860b";
            case "APPROVED" -> "#145DA0";
            case "REJECTED" -> "#e74c3c";
            case "RETURNED" -> "#2e8b57";
            default -> "#555";
        };

        Label statusLabel = new Label("Status: " + claim.getStatus());
        statusLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: " + statusColor + ";");

        card.getChildren().addAll(claimLabel, detailsLabel, statusLabel);

        HBox actions = new HBox(8);

        if ("PENDING".equals(claim.getStatus())) {
            Button approveBtn = new Button("Approve");
            approveBtn.getStyleClass().add("success-button");
            approveBtn.setOnAction(e -> {
                claimDao.updateStatus(claim.getClaimId(), "APPROVED", currentUser.getUserId());
                NotificationPublisher.getInstance().publish(claim.getClaimantId(),
                        "Your claim #" + claim.getClaimId() + " has been approved!");
                handleClaimVerification();
            });

            Button rejectBtn = new Button("Reject");
            rejectBtn.getStyleClass().add("danger-button");
            rejectBtn.setOnAction(e -> {
                claimDao.updateStatus(claim.getClaimId(), "REJECTED", currentUser.getUserId());
                NotificationPublisher.getInstance().publish(claim.getClaimantId(),
                        "Your claim #" + claim.getClaimId() + " has been rejected.");
                handleClaimVerification();
            });

            actions.getChildren().addAll(approveBtn, rejectBtn);

        } else if ("APPROVED".equals(claim.getStatus())) {
            Button returnBtn = new Button("Confirm Return");
            returnBtn.getStyleClass().add("secondary-button");
            returnBtn.setOnAction(e -> {
                claimDao.updateStatus(claim.getClaimId(), "RETURNED", currentUser.getUserId());

                if (match != null) {
                    updateReportToReturned(match.getLostReportId());
                    updateReportToReturned(match.getFoundReportId());
                }

                NotificationPublisher.getInstance().publish(claim.getClaimantId(),
                        "Your claimed item (Claim #" + claim.getClaimId() + ") has been marked as returned. Please collect it!");
                handleClaimVerification();
            });
            actions.getChildren().add(returnBtn);
        }

        if (!actions.getChildren().isEmpty()) {
            card.getChildren().add(actions);
        }

        return card;
    }

    private void updateReportToReturned(int reportId) {
        ItemReportDao itemReportDao = new ItemReportDao();
        ItemReport report = itemReportDao.findById(reportId);
        if (report == null) return;

        try {
            ReportState currentState = ReportStateFactory.fromString(report.getStatus());
            ReportState newState = currentState.markReturned();
            itemReportDao.updateStatus(reportId, newState.getName());
        } catch (IllegalStateException e) {

        }
    }

    private void showPlaceholderPanel(String title, String message) {
        VBox panel = new VBox(10);

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("dashboard-heading");

        Label messageLabel = new Label(message);
        messageLabel.setWrapText(true);
        messageLabel.setStyle("-fx-text-fill: #777; -fx-font-size: 13px;");

        panel.getChildren().addAll(titleLabel, messageLabel);
        contentArea.getChildren().setAll(panel);
    }

    private VBox createStatCard(String number, String label) {
        VBox card = new VBox();
        card.getStyleClass().add("stat-card");
        card.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(card, Priority.ALWAYS);

        Label numberLabel = new Label(number);
        numberLabel.getStyleClass().add("stat-number");

        Label textLabel = new Label(label);
        textLabel.getStyleClass().add("stat-label");

        card.getChildren().addAll(numberLabel, textLabel);
        return card;
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}