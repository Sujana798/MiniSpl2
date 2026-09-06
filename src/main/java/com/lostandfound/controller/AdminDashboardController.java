package com.lostandfound.controller;

import com.lostandfound.dao.ItemReportDao;
import com.lostandfound.dao.MatchDao;
import com.lostandfound.dao.UserDao;
import com.lostandfound.model.MatchView;
import com.lostandfound.model.User;
import com.lostandfound.service.MatchService;
import com.lostandfound.service.WeightedMatchingStrategy;
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
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
    private final MatchService matchService = new MatchService(
            new ItemReportDao(), new MatchDao(), new WeightedMatchingStrategy(), userDao);

    // Sample data standing in for the future "claims" table (Design Doc Section 3 & 7.4).
    private final List<ClaimRow> sampleClaims = createSampleClaims();

    public void setCurrentUser(User user) {
        this.currentUser = user;

        // Defensive role check: this screen must only be reached via the
        // ADMIN branch of LoginController.goToDashboard.
        if (!"ADMIN".equalsIgnoreCase(user.getRole())) {
            throw new IllegalStateException("AdminDashboardController requires a user with ADMIN role.");
        }

        welcomeLabel.setText("Welcome, " + user.getName() + " (Admin)");
        handleDashboardOverview();
    }

    // ---------------------------------------------------------------
    // Sidebar navigation handlers
    // ---------------------------------------------------------------

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
        buildMatchReview();
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

    // ---------------------------------------------------------------
    // Panel builders
    // ---------------------------------------------------------------

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

    private void buildMatchReview() {
        VBox panel = new VBox(15);

        Label heading = new Label("Match Review");
        heading.getStyleClass().add("dashboard-heading");

        Label note = new Label(
                "Automatically generated Lost <-> Found matches. Weighted scoring: Category 25%, " +
                        "Description 30%, Location 20%, Date 15%, Attributes 10%.");
        note.getStyleClass().add("section-note");
        note.setWrapText(true);

        TextField searchField = new TextField();
        searchField.setPromptText("Search by item title, category, or location...");
        searchField.setPrefWidth(280);

        ComboBox<String> confidenceFilter = new ComboBox<>();
        confidenceFilter.getItems().addAll("All Confidence", "HIGH", "MEDIUM", "LOW");
        confidenceFilter.getSelectionModel().selectFirst();

        ComboBox<String> statusFilter = new ComboBox<>();
        statusFilter.getItems().addAll("All Status", "PENDING", "CONFIRMED", "REJECTED");
        statusFilter.getSelectionModel().selectFirst();

        Button refreshButton = new Button("Refresh Matches");
        refreshButton.getStyleClass().add("secondary-button");

        HBox filterRow = new HBox(10, searchField, confidenceFilter, statusFilter, refreshButton);
        filterRow.setAlignment(Pos.CENTER_LEFT);

        TableView<MatchView> table = new TableView<>();
        table.setPrefHeight(320);

        TableColumn<MatchView, String> lostCol = new TableColumn<>("Lost Item");
        lostCol.setCellValueFactory(new PropertyValueFactory<>("lostTitle"));

        TableColumn<MatchView, String> foundCol = new TableColumn<>("Found Item");
        foundCol.setCellValueFactory(new PropertyValueFactory<>("foundTitle"));

        TableColumn<MatchView, String> categoryCol = new TableColumn<>("Category");
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("category"));

        TableColumn<MatchView, String> locationCol = new TableColumn<>("Location");
        locationCol.setCellValueFactory(new PropertyValueFactory<>("lostLocation"));

        TableColumn<MatchView, String> scoreCol = new TableColumn<>("Score");
        scoreCol.setCellValueFactory(new PropertyValueFactory<>("matchScoreDisplay"));

        TableColumn<MatchView, String> confidenceCol = new TableColumn<>("Confidence");
        confidenceCol.setCellValueFactory(new PropertyValueFactory<>("confidence"));
        confidenceCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null) {
                    setText(null);
                    setStyle("");
                    return;
                }
                setText(value);
                switch (value) {
                    case "HIGH" -> setStyle("-fx-text-fill: #2e8b57; -fx-font-weight: bold;");
                    case "MEDIUM" -> setStyle("-fx-text-fill: #b8860b; -fx-font-weight: bold;");
                    case "LOW" -> setStyle("-fx-text-fill: #999999; -fx-font-weight: bold;");
                    default -> setStyle("");
                }
            }
        });

        TableColumn<MatchView, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null) {
                    setText(null);
                    setStyle("");
                    return;
                }
                setText(value);
                switch (value) {
                    case "PENDING" -> setStyle("-fx-text-fill: #145DA0; -fx-font-weight: bold;");
                    case "CONFIRMED" -> setStyle("-fx-text-fill: #2e8b57; -fx-font-weight: bold;");
                    case "REJECTED" -> setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                    default -> setStyle("");
                }
            }
        });

        TableColumn<MatchView, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setCellFactory(col -> new TableCell<>() {
            private final Button viewBtn = new Button("View Details");
            private final Button rejectBtn = new Button("Reject");
            private final HBox box = new HBox(6, viewBtn, rejectBtn);

            {
                viewBtn.getStyleClass().add("secondary-button");
                rejectBtn.getStyleClass().add("danger-button");

                viewBtn.setOnAction(e -> showMatchDetailsDialog(getTableView().getItems().get(getIndex())));

                rejectBtn.setOnAction(e -> {
                    MatchView mv = getTableView().getItems().get(getIndex());
                    matchService.rejectMatch(mv.getMatchId());
                    showAlert(Alert.AlertType.INFORMATION, "Match Rejected",
                            "The match between \"" + mv.getLostTitle() + "\" and \"" + mv.getFoundTitle()
                                    + "\" has been rejected.");
                    buildMatchReview();
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }
                MatchView mv = getTableView().getItems().get(getIndex());
                rejectBtn.setDisable("REJECTED".equals(mv.getStatus()));
                setGraphic(box);
            }
        });

        table.getColumns().addAll(lostCol, foundCol, categoryCol, locationCol, scoreCol, confidenceCol, statusCol, actionsCol);

        List<MatchView> allMatches = matchService.getAllMatchViews();
        table.setItems(FXCollections.observableArrayList(allMatches));

        Runnable applyFilter = () -> {
            String keyword = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
            String confSel = confidenceFilter.getValue();
            String statSel = statusFilter.getValue();

            List<MatchView> filtered = allMatches.stream()
                    .filter(mv -> keyword.isEmpty()
                            || mv.getLostTitle().toLowerCase().contains(keyword)
                            || mv.getFoundTitle().toLowerCase().contains(keyword)
                            || mv.getCategory().toLowerCase().contains(keyword)
                            || mv.getLostLocation().toLowerCase().contains(keyword))
                    .filter(mv -> confSel == null || confSel.startsWith("All") || confSel.equals(mv.getConfidence()))
                    .filter(mv -> statSel == null || statSel.startsWith("All") || statSel.equals(mv.getStatus()))
                    .collect(Collectors.toList());

            table.setItems(FXCollections.observableArrayList(filtered));
        };

        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilter.run());
        confidenceFilter.setOnAction(e -> applyFilter.run());
        statusFilter.setOnAction(e -> applyFilter.run());

        refreshButton.setOnAction(e -> {
            matchService.recalculateAllMatches();
            showAlert(Alert.AlertType.INFORMATION, "Matches Refreshed",
                    "All lost/found reports have been re-evaluated for matches.");
            buildMatchReview();
        });

        panel.getChildren().addAll(heading, note, filterRow);

        if (allMatches.isEmpty()) {
            Label emptyLabel = new Label(
                    "No matches yet. New matches are generated automatically when reports are submitted, " +
                            "or click Refresh Matches to re-scan existing reports.");
            emptyLabel.setWrapText(true);
            emptyLabel.setStyle("-fx-text-fill: #777; -fx-font-size: 12px;");
            panel.getChildren().add(emptyLabel);
        } else {
            panel.getChildren().add(table);
        }

        contentArea.getChildren().setAll(panel);
    }

    private void showMatchDetailsDialog(MatchView mv) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Match Details");

        VBox root = new VBox(15);
        root.setStyle("-fx-padding: 25; -fx-background-color: white;");

        Label heading = new Label("Match Details");
        heading.getStyleClass().add("dashboard-heading");

        HBox itemsRow = new HBox(20,
                buildReportDetailBox("Lost Item", mv.getLostTitle(), mv.getLostCategory(), mv.getLostLocation(),
                        mv.getLostDate(), mv.getLostBrand(), mv.getLostColor(), mv.getLostDescription(), mv.getLostReporterName()),
                buildReportDetailBox("Found Item", mv.getFoundTitle(), mv.getFoundCategory(), mv.getFoundLocation(),
                        mv.getFoundDate(), mv.getFoundBrand(), mv.getFoundColor(), mv.getFoundDescription(), mv.getFoundReporterName())
        );

        VBox criteriaBox = new VBox(8);
        criteriaBox.setStyle("-fx-background-color: #f4f6f8; -fx-background-radius: 10; -fx-padding: 15;");
        Label criteriaTitle = new Label("Matching Criteria Breakdown");
        criteriaTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        criteriaBox.getChildren().addAll(
                criteriaTitle,
                buildCriterionRow("Category Match (25%)", mv.getCategoryScore()),
                buildCriterionRow("Description Similarity (30%)", mv.getDescriptionScore()),
                buildCriterionRow("Location Similarity (20%)", mv.getLocationScore()),
                buildCriterionRow("Date Proximity (15%)", mv.getDateScore()),
                buildCriterionRow("Attribute Similarity (10%)", mv.getAttributeScore())
        );

        HBox finalScoreRow = new HBox(10);
        finalScoreRow.setAlignment(Pos.CENTER_LEFT);
        Label finalLabel = new Label("Final Match Score:");
        finalLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold;");
        Label finalValue = new Label(String.format("%.0f%%  (%s Confidence)", mv.getMatchScore(), mv.getConfidence()));
        finalValue.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #145DA0;");
        finalScoreRow.getChildren().addAll(finalLabel, finalValue);

        Label reasonTitle = new Label("Why these were matched:");
        reasonTitle.setStyle("-fx-font-weight: bold;");
        Label reasonText = new Label(mv.getReason());
        reasonText.setWrapText(true);
        reasonText.setStyle("-fx-text-fill: #555;");

        Button closeButton = new Button("Close");
        closeButton.getStyleClass().add("primary-button");
        closeButton.setOnAction(e -> dialog.close());

        root.getChildren().addAll(heading, itemsRow, criteriaBox, finalScoreRow, reasonTitle, reasonText, closeButton);

        ScrollPane scrollPane = new ScrollPane(root);
        scrollPane.setFitToWidth(true);

        dialog.setScene(new Scene(scrollPane, 640, 640));
        dialog.showAndWait();
    }

    private VBox buildReportDetailBox(String heading, String title, String category, String location,
                                      String date, String brand, String color, String description, String reporterName) {
        VBox box = new VBox(6);
        box.setStyle("-fx-background-color: #f4f6f8; -fx-background-radius: 10; -fx-padding: 15;");
        box.setPrefWidth(280);

        Label headingLabel = new Label(heading);
        headingLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #145DA0;");

        Label titleLabel = new Label("Title: " + safe(title));
        Label categoryLabel = new Label("Category: " + safe(category));
        Label locationLabel = new Label("Location: " + safe(location));
        Label dateLabel = new Label("Date: " + safe(date));
        Label brandLabel = new Label("Brand: " + safe(brand));
        Label colorLabel = new Label("Color: " + safe(color));
        Label reporterLabel = new Label("Reported by: " + safe(reporterName));
        Label descLabel = new Label("Description: " + safe(description));
        descLabel.setWrapText(true);

        for (Label l : List.of(titleLabel, categoryLabel, locationLabel, dateLabel, brandLabel, colorLabel, reporterLabel, descLabel)) {
            l.setStyle("-fx-font-size: 12px;");
        }

        box.getChildren().addAll(headingLabel, titleLabel, categoryLabel, locationLabel, dateLabel,
                brandLabel, colorLabel, reporterLabel, descLabel);

        return box;
    }

    private HBox buildCriterionRow(String label, double score) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);

        Label nameLabel = new Label(label);
        nameLabel.setPrefWidth(220);
        nameLabel.setStyle("-fx-font-size: 12px;");

        ProgressBar bar = new ProgressBar(score / 100.0);
        bar.setPrefWidth(200);

        Label valueLabel = new Label(String.format("%.0f%%", score));
        valueLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");

        row.getChildren().addAll(nameLabel, bar, valueLabel);
        return row;
    }

    private String safe(String value) {
        return (value == null || value.trim().isEmpty()) ? "-" : value;
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
