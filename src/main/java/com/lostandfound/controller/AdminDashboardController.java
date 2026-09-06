package com.lostandfound.controller;

import com.lostandfound.dao.UserDao;
import com.lostandfound.model.User;
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
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller for the Admin Dashboard.
 * Reuses UserDao/User exactly as defined for the Student side.
 * Report/Match/Claim data does not exist in the database yet (see Design
 * Document phases), so the Claim Verification workflow is demonstrated
 * against an in-memory sample list until the real "claims" table exists.
 */
public class AdminDashboardController {

    @FXML
    private Label welcomeLabel;

    @FXML
    private VBox contentArea;

    private User currentUser;
    private final UserDao userDao = new UserDao();
    private final List<ClaimRow> sampleClaims = createSampleClaims();

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
                "Review, filter, and manage all lost and found item reports submitted by students. " +
                        "This panel will connect to the lost_reports/found_reports tables once the reporting " +
                        "module is implemented (see Design Document, Sections 3 and 4).");
    }

    @FXML
    private void handleMatchReview() {
        showPlaceholderPanel("Match Review",
                "Review candidate matches generated between lost and found reports. This panel will " +
                        "connect to the matches table and the Strategy-based MatchingService once implemented " +
                        "(see Design Document, Section 7.1).");
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
                "Basic analytics such as open cases, resolved cases, and average resolution time will " +
                        "be available once report and claim data exists.");
    }

    @FXML
    private void handleNotifications() {
        showPlaceholderPanel("Notifications",
                "Admin notifications will be available once the notification module is implemented.");
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
        long pendingClaims = sampleClaims.stream().filter(c -> "PENDING".equals(c.getStatus())).count();

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
                "Core workflow: Claim -> Verification -> Approve/Reject -> Return Confirmation -> Case Closure. " +
                        "Sample data shown below; will connect to the real claims table once implemented.");
        note.getStyleClass().add("section-note");
        note.setWrapText(true);

        TableView<ClaimRow> table = new TableView<>();
        table.setItems(FXCollections.observableArrayList(sampleClaims));

        TableColumn<ClaimRow, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<ClaimRow, String> itemCol = new TableColumn<>("Item");
        itemCol.setCellValueFactory(new PropertyValueFactory<>("itemDescription"));

        TableColumn<ClaimRow, String> claimantCol = new TableColumn<>("Claimant");
        claimantCol.setCellValueFactory(new PropertyValueFactory<>("claimant"));

        TableColumn<ClaimRow, String> scoreCol = new TableColumn<>("Match Score");
        scoreCol.setCellValueFactory(new PropertyValueFactory<>("matchScore"));

        TableColumn<ClaimRow, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                    return;
                }
                setText(status);
                switch (status) {
                    case "PENDING" -> setStyle("-fx-text-fill: #b8860b; -fx-font-weight: bold;");
                    case "APPROVED" -> setStyle("-fx-text-fill: #145DA0; -fx-font-weight: bold;");
                    case "REJECTED" -> setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                    case "RETURNED" -> setStyle("-fx-text-fill: #2e8b57; -fx-font-weight: bold;");
                    default -> setStyle("");
                }
            }
        });

        TableColumn<ClaimRow, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setCellFactory(col -> new TableCell<>() {
            private final Button approveBtn = new Button("Approve");
            private final Button rejectBtn = new Button("Reject");
            private final Button returnBtn = new Button("Confirm Return");
            private final HBox box = new HBox(6, approveBtn, rejectBtn, returnBtn);

            {
                approveBtn.getStyleClass().add("success-button");
                rejectBtn.getStyleClass().add("danger-button");
                returnBtn.getStyleClass().add("secondary-button");

                approveBtn.setOnAction(e -> updateStatus("APPROVED"));
                rejectBtn.setOnAction(e -> updateStatus("REJECTED"));
                returnBtn.setOnAction(e -> updateStatus("RETURNED"));
            }

            private void updateStatus(String newStatus) {
                ClaimRow row = getTableView().getItems().get(getIndex());
                row.setStatus(newStatus);
                getTableView().refresh();
                showAlert(Alert.AlertType.INFORMATION, "Claim Updated",
                        "Claim #" + row.getId() + " (" + row.getItemDescription() + ") is now " + newStatus + ".");
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }
                ClaimRow row = getTableView().getItems().get(getIndex());
                approveBtn.setDisable(!"PENDING".equals(row.getStatus()));
                rejectBtn.setDisable(!"PENDING".equals(row.getStatus()));
                returnBtn.setDisable(!"APPROVED".equals(row.getStatus()));
                setGraphic(box);
            }
        });

        table.getColumns().addAll(idCol, itemCol, claimantCol, scoreCol, statusCol, actionsCol);
        table.setPrefHeight(280);

        panel.getChildren().addAll(heading, note, table);
        contentArea.getChildren().setAll(panel);
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

    // ---------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------

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

    private List<ClaimRow> createSampleClaims() {
        List<ClaimRow> list = new ArrayList<>();
        list.add(new ClaimRow(1, "Blue Backpack", "Ayesha Rahman", "92%", "PENDING"));
        list.add(new ClaimRow(2, "Student ID Card", "Tanvir Hasan", "88%", "PENDING"));
        list.add(new ClaimRow(3, "Black Wallet", "Nusrat Jahan", "75%", "APPROVED"));
        return list;
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Temporary in-memory row used only to demonstrate the claim workflow
     * in the UI. Replace with a real Claim model/DAO once the "claims"
     * table exists (Design Document Section 3).
     */
    public static class ClaimRow {
        private final int id;
        private final String itemDescription;
        private final String claimant;
        private final String matchScore;
        private String status;

        public ClaimRow(int id, String itemDescription, String claimant, String matchScore, String status) {
            this.id = id;
            this.itemDescription = itemDescription;
            this.claimant = claimant;
            this.matchScore = matchScore;
            this.status = status;
        }

        public int getId() { return id; }
        public String getItemDescription() { return itemDescription; }
        public String getClaimant() { return claimant; }
        public String getMatchScore() { return matchScore; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }
}
