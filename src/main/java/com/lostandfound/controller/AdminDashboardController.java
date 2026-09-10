package com.lostandfound.controller;

import com.lostandfound.dao.ClaimDao;
import com.lostandfound.dao.ItemReportDao;
import com.lostandfound.dao.MatchDao;
import com.lostandfound.dao.NotificationDao;
import com.lostandfound.dao.UserDao;
import com.lostandfound.db.DatabaseConnection;
import com.lostandfound.model.Claim;
import com.lostandfound.model.ItemReport;
import com.lostandfound.model.Match;
import com.lostandfound.model.MatchView;
import com.lostandfound.model.Notification;
import com.lostandfound.model.User;
import com.lostandfound.notification.NotificationPublisher;
import com.lostandfound.service.MatchService;
import com.lostandfound.service.WeightedMatchingStrategy;
import com.lostandfound.state.ReportState;
import com.lostandfound.state.ReportStateFactory;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
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
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AdminDashboardController {

    @FXML
    private Label welcomeLabel;

    @FXML
    private VBox contentArea;

    private User currentUser;

    private final UserDao userDao = new UserDao();
    private final ItemReportDao itemReportDao = new ItemReportDao();
    private final MatchDao matchDao = new MatchDao();
    private final ClaimDao claimDao = new ClaimDao();
    private final NotificationDao notificationDao = new NotificationDao();

    private final MatchService matchService =
            new MatchService(
                    itemReportDao,
                    matchDao,
                    new WeightedMatchingStrategy(),
                    userDao
            );

    // ===============================================================
    // Current User
    // ===============================================================

    public void setCurrentUser(User user) {

        this.currentUser = user;

        if (!"ADMIN".equalsIgnoreCase(user.getRole())) {
            throw new IllegalStateException(
                    "AdminDashboardController requires a user with ADMIN role."
            );
        }

        welcomeLabel.setText(
                "Welcome, " + user.getName() + " (Admin)"
        );

        handleDashboardOverview();
    }

    // ===============================================================
    // SIDEBAR NAVIGATION
    // ===============================================================

    @FXML
    private void handleDashboardOverview() {
        buildDashboardOverview();
    }

    @FXML
    private void handleManageReports() {
        buildManageReports();
    }

    @FXML
    private void handleMatchReview() {
        buildMatchReview();
    }

    // IMPORTANT: Claim Verification
    @FXML
    private void handleClaimVerification() {
        buildClaimVerification();
    }

    @FXML
    private void handleReturnsClosure() {
        buildReturnsClosure();
    }

    @FXML
    private void handleUserManagement() {
        buildUserManagement();
    }

    @FXML
    private void handleAnalytics() {
        buildReportsAnalytics();
    }

    @FXML
    private void handleNotifications() {

        contentArea.getChildren().clear();
        contentArea.setSpacing(15);

        Label heading = new Label("Notifications");
        heading.getStyleClass().add("dashboard-heading");

        contentArea.getChildren().add(heading);

        List<Notification> notifications =
                notificationDao.findByUserId(
                        currentUser.getUserId()
                );

        if (notifications.isEmpty()) {

            Label emptyLabel =
                    new Label("No notifications yet.");

            emptyLabel.setStyle("-fx-text-fill: #777;");

            contentArea.getChildren().add(emptyLabel);

            return;
        }

        for (Notification notification : notifications) {

            contentArea.getChildren().add(
                    buildAdminNotificationCard(notification)
            );
        }
    }

    // ===============================================================
    // ADMIN NOTIFICATION CARD
    // ===============================================================

    private VBox buildAdminNotificationCard(
            Notification notification) {

        VBox card = new VBox(6);

        String bgColor =
                notification.isRead()
                        ? "white"
                        : "#eaf2fb";

        card.setStyle(
                "-fx-background-color: " + bgColor + ";" +
                        "-fx-background-radius: 10;" +
                        "-fx-padding: 15;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);"
        );

        Label messageLabel =
                new Label(notification.getMessage());

        messageLabel.setStyle(
                "-fx-font-size: 13px;" +
                        (notification.isRead()
                                ? ""
                                : "-fx-font-weight: bold;")
        );

        messageLabel.setWrapText(true);

        Label timeLabel =
                new Label(notification.getCreatedAt());

        timeLabel.setStyle(
                "-fx-font-size: 11px;" +
                        "-fx-text-fill: #999;"
        );

        card.getChildren().addAll(
                messageLabel,
                timeLabel
        );

        if (!notification.isRead()) {

            Button markReadBtn =
                    new Button("Mark as Read");

            markReadBtn
                    .getStyleClass()
                    .add("secondary-button");

            markReadBtn.setOnAction(e -> {

                notificationDao.markAsRead(
                        notification.getNotificationId()
                );

                handleNotifications();
            });

            card.getChildren().add(markReadBtn);
        }

        return card;
    }

    // ===============================================================
    // LOGOUT
    // ===============================================================

    @FXML
    private void handleLogout(ActionEvent event) {

        try {

            Parent root =
                    FXMLLoader.load(
                            getClass()
                                    .getResource("/fxml/login.fxml")
                    );

            Stage stage =
                    (Stage)
                            ((Node) event.getSource())
                                    .getScene()
                                    .getWindow();

            stage.setScene(
                    new Scene(root, 500, 500)
            );

            stage.setTitle(
                    "Campus Lost & Found - Login"
            );

        } catch (IOException e) {

            showAlert(
                    Alert.AlertType.ERROR,
                    "Navigation Error",
                    "Could not return to login screen."
            );
        }
    }

    // ===============================================================
    // DASHBOARD OVERVIEW
    // ===============================================================

    private void buildDashboardOverview() {

        VBox panel = new VBox(20);

        Label heading =
                new Label("Admin Dashboard Overview");

        heading.getStyleClass()
                .add("dashboard-heading");

        int totalUsers =
                userDao.getAllUsers().size();

        int totalStudents =
                userDao.countByRole("STUDENT");

        int totalAdmins =
                userDao.countByRole("ADMIN");

        long pendingClaims =
                claimDao.findAll()
                        .stream()
                        .filter(c ->
                                "PENDING"
                                        .equalsIgnoreCase(
                                                c.getStatus()))
                        .count();

        HBox statRow =
                new HBox(
                        20,
                        createStatCard(
                                String.valueOf(totalUsers),
                                "Total Users"
                        ),
                        createStatCard(
                                String.valueOf(totalStudents),
                                "Students"
                        ),
                        createStatCard(
                                String.valueOf(totalAdmins),
                                "Admins"
                        ),
                        createStatCard(
                                String.valueOf(pendingClaims),
                                "Pending Claims"
                        )
                );

        HBox ctaCard = new HBox(20);

        ctaCard.getStyleClass()
                .add("cta-card");

        ctaCard.setAlignment(
                Pos.CENTER_LEFT
        );

        VBox ctaText = new VBox(5);

        Label ctaTitle =
                new Label(
                        "Priority Workflow: Match -> Claim -> Verification -> Return -> Closure"
                );

        ctaTitle.getStyleClass()
                .add("cta-title");

        ctaTitle.setWrapText(true);

        Label ctaSubtitle =
                new Label(
                        "Review pending claims and move them through verification to case closure."
                );

        ctaSubtitle.getStyleClass()
                .add("cta-subtitle");

        ctaText.getChildren().addAll(
                ctaTitle,
                ctaSubtitle
        );

        HBox.setHgrow(
                ctaText,
                Priority.ALWAYS
        );

        Button ctaButton =
                new Button("Review Pending Claims");

        ctaButton.getStyleClass()
                .add("cta-button");

        ctaButton.setOnAction(
                e -> handleClaimVerification()
        );

        ctaCard.getChildren().addAll(
                ctaText,
                ctaButton
        );

        panel.getChildren().addAll(
                heading,
                statRow,
                ctaCard
        );

        contentArea
                .getChildren()
                .setAll(panel);
    }

    // ===============================================================
    // MANAGE REPORTS
    // ===============================================================

    private void buildManageReports() {

        VBox panel = new VBox(15);

        Label heading =
                new Label(
                        "Lost & Found Report Management"
                );

        heading.getStyleClass()
                .add("dashboard-heading");

        Label note =
                new Label(
                        "Search and filter all reports. Status changes automatically through the Match and Claim workflow."
                );

        note.getStyleClass()
                .add("section-note");

        note.setWrapText(true);

        TextField searchField =
                new TextField();

        searchField.setPromptText(
                "Search by title, location, or reporter..."
        );

        searchField.setPrefWidth(240);

        ComboBox<String> typeFilter =
                new ComboBox<>();

        typeFilter.getItems().addAll(
                "All Types",
                "LOST",
                "FOUND"
        );

        typeFilter.getSelectionModel()
                .selectFirst();

        ComboBox<String> categoryFilter =
                new ComboBox<>();

        categoryFilter.getItems().addAll(
                "All Categories",
                "Electronics",
                "Documents",
                "Bags",
                "Clothing",
                "Accessories",
                "Others"
        );

        categoryFilter.getSelectionModel()
                .selectFirst();

        ComboBox<String> statusFilter =
                new ComboBox<>();

        statusFilter.getItems().addAll(
                "All Status",
                "REPORTED",
                "MATCHED",
                "CLAIMED",
                "RETURNED"
        );

        statusFilter.getSelectionModel()
                .selectFirst();

        DatePicker datePicker =
                new DatePicker();

        datePicker.setPromptText(
                "Filter by date"
        );

        Button clearDateButton =
                new Button("Clear Date");

        clearDateButton
                .getStyleClass()
                .add("secondary-button");

        Button refreshButton =
                new Button("Refresh");

        refreshButton
                .getStyleClass()
                .add("secondary-button");

        HBox filterRow1 =
                new HBox(
                        10,
                        searchField,
                        typeFilter,
                        categoryFilter
                );

        filterRow1.setAlignment(
                Pos.CENTER_LEFT
        );

        HBox filterRow2 =
                new HBox(
                        10,
                        statusFilter,
                        datePicker,
                        clearDateButton,
                        refreshButton
                );

        filterRow2.setAlignment(
                Pos.CENTER_LEFT
        );

        TableView<ItemReport> table =
                new TableView<>();

        table.setPrefHeight(360);

        TableColumn<ItemReport, Integer> idCol =
                new TableColumn<>("ID");

        idCol.setCellValueFactory(
                new PropertyValueFactory<>("reportId")
        );

        TableColumn<ItemReport, String> typeCol =
                new TableColumn<>("Type");

        typeCol.setCellValueFactory(
                new PropertyValueFactory<>("type")
        );

        TableColumn<ItemReport, String> titleCol =
                new TableColumn<>("Title");

        titleCol.setCellValueFactory(
                new PropertyValueFactory<>("title")
        );

        TableColumn<ItemReport, String> categoryCol =
                new TableColumn<>("Category");

        categoryCol.setCellValueFactory(
                new PropertyValueFactory<>("category")
        );

        TableColumn<ItemReport, String> locationCol =
                new TableColumn<>("Location");

        locationCol.setCellValueFactory(
                new PropertyValueFactory<>("location")
        );

        TableColumn<ItemReport, String> dateCol =
                new TableColumn<>("Date");

        dateCol.setCellValueFactory(
                new PropertyValueFactory<>("dateOccurred")
        );

        TableColumn<ItemReport, String> statusCol =
                new TableColumn<>("Status");

        statusCol.setCellValueFactory(
                new PropertyValueFactory<>("status")
        );

        statusCol.setCellFactory(
                col -> new TableCell<>() {

                    @Override
                    protected void updateItem(
                            String value,
                            boolean empty) {

                        super.updateItem(
                                value,
                                empty
                        );

                        if (empty || value == null) {

                            setText(null);
                            setStyle("");

                            return;
                        }

                        setText(value);

                        switch (value) {

                            case "REPORTED" ->
                                    setStyle(
                                            "-fx-text-fill: #b8860b;" +
                                                    "-fx-font-weight: bold;"
                                    );

                            case "MATCHED" ->
                                    setStyle(
                                            "-fx-text-fill: #145DA0;" +
                                                    "-fx-font-weight: bold;"
                                    );

                            case "CLAIMED" ->
                                    setStyle(
                                            "-fx-text-fill: #8e44ad;" +
                                                    "-fx-font-weight: bold;"
                                    );

                            case "RETURNED" ->
                                    setStyle(
                                            "-fx-text-fill: #2e8b57;" +
                                                    "-fx-font-weight: bold;"
                                    );

                            default ->
                                    setStyle("");
                        }
                    }
                }
        );

        TableColumn<ItemReport, String> reporterCol =
                new TableColumn<>("Reporter");

        reporterCol.setCellValueFactory(
                data ->
                        new SimpleStringProperty(
                                resolveUserName(
                                        data.getValue()
                                                .getReporterId()
                                )
                        )
        );

        TableColumn<ItemReport, Void> actionCol =
                new TableColumn<>("Action");

        actionCol.setCellFactory(
                col -> new TableCell<>() {

                    private final Button viewBtn =
                            new Button("View Details");

                    {
                        viewBtn.getStyleClass()
                                .add("secondary-button");

                        viewBtn.setOnAction(
                                e ->
                                        showReportDetailsDialog(
                                                getTableView()
                                                        .getItems()
                                                        .get(getIndex())
                                        )
                        );
                    }

                    @Override
                    protected void updateItem(
                            Void item,
                            boolean empty) {

                        super.updateItem(
                                item,
                                empty
                        );

                        setGraphic(
                                empty
                                        ? null
                                        : viewBtn
                        );
                    }
                }
        );

        table.getColumns().addAll(
                idCol,
                typeCol,
                titleCol,
                categoryCol,
                locationCol,
                dateCol,
                statusCol,
                reporterCol,
                actionCol
        );

        List<ItemReport> allReports =
                itemReportDao.findAll();

        table.setItems(
                FXCollections.observableArrayList(
                        allReports
                )
        );

        Runnable applyFilter = () -> {

            String keyword =
                    searchField.getText() == null
                            ? ""
                            : searchField
                              .getText()
                              .trim()
                              .toLowerCase();

            String typeSel =
                    typeFilter.getValue();

            String catSel =
                    categoryFilter.getValue();

            String statSel =
                    statusFilter.getValue();

            LocalDate dateSel =
                    datePicker.getValue();

            List<ItemReport> filtered =
                    allReports
                            .stream()

                            .filter(r ->
                                    keyword.isEmpty()
                                            ||
                                            safe(r.getTitle())
                                                    .toLowerCase()
                                                    .contains(keyword)
                                            ||
                                            safe(r.getLocation())
                                                    .toLowerCase()
                                                    .contains(keyword)
                                            ||
                                            resolveUserName(
                                                    r.getReporterId()
                                            )
                                                    .toLowerCase()
                                                    .contains(keyword)
                            )

                            .filter(r ->
                                    typeSel == null
                                            ||
                                            typeSel.startsWith("All")
                                            ||
                                            typeSel.equalsIgnoreCase(
                                                    r.getType()
                                            )
                            )

                            .filter(r ->
                                    catSel == null
                                            ||
                                            catSel.startsWith("All")
                                            ||
                                            catSel.equalsIgnoreCase(
                                                    r.getCategory()
                                            )
                            )

                            .filter(r ->
                                    statSel == null
                                            ||
                                            statSel.startsWith("All")
                                            ||
                                            statSel.equalsIgnoreCase(
                                                    r.getStatus()
                                            )
                            )

                            .filter(r ->
                                    dateSel == null
                                            ||
                                            dateSel.toString()
                                                    .equals(
                                                            r.getDateOccurred()
                                                    )
                            )

                            .collect(
                                    Collectors.toList()
                            );

            table.setItems(
                    FXCollections.observableArrayList(
                            filtered
                    )
            );
        };

        searchField.textProperty()
                .addListener(
                        (obs, oldVal, newVal) ->
                                applyFilter.run()
                );

        typeFilter.setOnAction(
                e -> applyFilter.run()
        );

        categoryFilter.setOnAction(
                e -> applyFilter.run()
        );

        statusFilter.setOnAction(
                e -> applyFilter.run()
        );

        datePicker.setOnAction(
                e -> applyFilter.run()
        );

        clearDateButton.setOnAction(e -> {

            datePicker.setValue(null);

            applyFilter.run();
        });

        refreshButton.setOnAction(
                e -> buildManageReports()
        );

        panel.getChildren().addAll(
                heading,
                note,
                filterRow1,
                filterRow2,
                table
        );

        contentArea
                .getChildren()
                .setAll(panel);
    }

    // ===============================================================
    // REPORT DETAILS
    // ===============================================================

    private void showReportDetailsDialog(
            ItemReport report) {

        Stage dialog = new Stage();

        dialog.initModality(
                Modality.APPLICATION_MODAL
        );

        dialog.setTitle(
                "Report Details"
        );

        VBox root = new VBox(12);

        root.setStyle(
                "-fx-padding: 25;" +
                        "-fx-background-color: white;"
        );

        Label heading =
                new Label(
                        report.getType()
                                + " Report #"
                                + report.getReportId()
                );

        heading.getStyleClass()
                .add("dashboard-heading");

        VBox details = new VBox(6);

        details.getChildren().addAll(

                new Label(
                        "Title: "
                                + safe(report.getTitle())
                ),

                new Label(
                        "Category: "
                                + safe(report.getCategory())
                ),

                new Label(
                        "Brand: "
                                + safe(report.getBrand())
                ),

                new Label(
                        "Color: "
                                + safe(report.getColor())
                ),

                new Label(
                        "Location: "
                                + safe(report.getLocation())
                ),

                new Label(
                        "Date Occurred: "
                                + safe(report.getDateOccurred())
                ),

                new Label(
                        "Status: "
                                + safe(report.getStatus())
                ),

                new Label(
                        "Reported By: "
                                + resolveUserName(
                                report.getReporterId()
                        )
                ),

                new Label(
                        "Submitted On: "
                                + safe(report.getCreatedAt())
                )
        );

        for (Node n :
                details.getChildren()) {

            ((Label) n).setStyle(
                    "-fx-font-size: 13px;"
            );
        }

        Label descTitle =
                new Label("Description:");

        descTitle.setStyle(
                "-fx-font-weight: bold;" +
                        "-fx-font-size: 13px;"
        );

        Label descLabel =
                new Label(
                        safe(report.getDescription())
                );

        descLabel.setWrapText(true);

        descLabel.setStyle(
                "-fx-text-fill: #555;"
        );

        Button closeButton =
                new Button("Close");

        closeButton.getStyleClass()
                .add("primary-button");

        closeButton.setOnAction(
                e -> dialog.close()
        );

        root.getChildren().addAll(
                heading,
                details,
                descTitle,
                descLabel,
                closeButton
        );

        ScrollPane scrollPane =
                new ScrollPane(root);

        scrollPane.setFitToWidth(true);

        dialog.setScene(
                new Scene(
                        scrollPane,
                        480,
                        480
                )
        );

        dialog.showAndWait();
    }

    // ===============================================================
    // MATCH REVIEW
    // ===============================================================

    private void buildMatchReview() {

        VBox panel = new VBox(15);

        Label heading =
                new Label("Match Review");

        heading.getStyleClass()
                .add("dashboard-heading");

        Label note =
                new Label(
                        "Automatically generated Lost <-> Found matches. " +
                                "Weighted scoring: Category 25%, Description 30%, " +
                                "Location 20%, Date 15%, Attributes 10%."
                );

        note.getStyleClass()
                .add("section-note");

        note.setWrapText(true);

        TextField searchField =
                new TextField();

        searchField.setPromptText(
                "Search by item title, category, or location..."
        );

        searchField.setPrefWidth(260);

        ComboBox<String> confidenceFilter =
                new ComboBox<>();

        confidenceFilter.getItems().addAll(
                "All Confidence",
                "HIGH",
                "MEDIUM",
                "LOW"
        );

        confidenceFilter.getSelectionModel()
                .selectFirst();

        ComboBox<String> statusFilter =
                new ComboBox<>();

        statusFilter.getItems().addAll(
                "All Status",
                "PENDING",
                "CONFIRMED",
                "REJECTED"
        );

        statusFilter.getSelectionModel()
                .selectFirst();

        Button recalcButton =
                new Button(
                        "Recalculate Matches"
                );

        recalcButton.getStyleClass()
                .add("secondary-button");

        HBox filterRow =
                new HBox(
                        10,
                        searchField,
                        confidenceFilter,
                        statusFilter,
                        recalcButton
                );

        filterRow.setAlignment(
                Pos.CENTER_LEFT
        );

        TableView<MatchView> table =
                new TableView<>();

        table.setPrefHeight(340);

        TableColumn<MatchView, Integer> idCol =
                new TableColumn<>("Match ID");

        idCol.setCellValueFactory(
                new PropertyValueFactory<>("matchId")
        );

        TableColumn<MatchView, String> lostCol =
                new TableColumn<>("Lost Item");

        lostCol.setCellValueFactory(
                new PropertyValueFactory<>("lostTitle")
        );

        TableColumn<MatchView, String> foundCol =
                new TableColumn<>("Found Item");

        foundCol.setCellValueFactory(
                new PropertyValueFactory<>("foundTitle")
        );

        TableColumn<MatchView, String> scoreCol =
                new TableColumn<>("Score %");

        scoreCol.setCellValueFactory(
                new PropertyValueFactory<>(
                        "matchScoreDisplay"
                )
        );

        TableColumn<MatchView, String> confidenceCol =
                new TableColumn<>("Confidence");

        confidenceCol.setCellValueFactory(
                new PropertyValueFactory<>(
                        "confidence"
                )
        );

        TableColumn<MatchView, String> statusCol =
                new TableColumn<>("Status");

        statusCol.setCellValueFactory(
                new PropertyValueFactory<>(
                        "status"
                )
        );

        TableColumn<MatchView, Void> actionsCol =
                new TableColumn<>("Action");

        actionsCol.setCellFactory(
                col -> new TableCell<>() {

                    private final Button viewBtn =
                            new Button("View Details");

                    private final Button confirmBtn =
                            new Button("Confirm");

                    private final Button rejectBtn =
                            new Button("Reject");

                    private final HBox box =
                            new HBox(
                                    6,
                                    viewBtn,
                                    confirmBtn,
                                    rejectBtn
                            );

                    {

                        viewBtn.getStyleClass()
                                .add("secondary-button");

                        confirmBtn.getStyleClass()
                                .add("success-button");

                        rejectBtn.getStyleClass()
                                .add("danger-button");

                        viewBtn.setOnAction(
                                e ->
                                        showMatchDetailsDialog(
                                                getTableView()
                                                        .getItems()
                                                        .get(getIndex())
                                        )
                        );

                        confirmBtn.setOnAction(e -> {

                            MatchView mv =
                                    getTableView()
                                            .getItems()
                                            .get(getIndex());

                            boolean confirmed =
                                    matchService.confirmMatch(
                                            mv.getMatchId()
                                    );

                            if (confirmed) {

                                showAlert(
                                        Alert.AlertType.INFORMATION,
                                        "Match Confirmed",
                                        "The match has been confirmed."
                                );

                            } else {

                                showAlert(
                                        Alert.AlertType.WARNING,
                                        "Cannot Confirm",
                                        "This match can no longer be confirmed."
                                );
                            }

                            buildMatchReview();
                        });

                        rejectBtn.setOnAction(e -> {

                            MatchView mv =
                                    getTableView()
                                            .getItems()
                                            .get(getIndex());

                            boolean rejected =
                                    matchService.rejectMatch(
                                            mv.getMatchId()
                                    );

                            if (rejected) {

                                showAlert(
                                        Alert.AlertType.INFORMATION,
                                        "Match Rejected",
                                        "The match has been rejected."
                                );

                            } else {

                                showAlert(
                                        Alert.AlertType.WARNING,
                                        "Cannot Reject",
                                        "This match can no longer be rejected."
                                );
                            }

                            buildMatchReview();
                        });
                    }

                    @Override
                    protected void updateItem(
                            Void item,
                            boolean empty) {

                        super.updateItem(
                                item,
                                empty
                        );

                        if (empty) {

                            setGraphic(null);

                            return;
                        }

                        MatchView mv =
                                getTableView()
                                        .getItems()
                                        .get(getIndex());

                        boolean pending =
                                "PENDING"
                                        .equals(mv.getStatus());

                        confirmBtn.setDisable(
                                !pending
                        );

                        rejectBtn.setDisable(
                                !pending
                        );

                        setGraphic(box);
                    }
                }
        );

        table.getColumns().addAll(
                idCol,
                lostCol,
                foundCol,
                scoreCol,
                confidenceCol,
                statusCol,
                actionsCol
        );

        List<MatchView> allMatches =
                matchService.getAllMatchViews();

        table.setItems(
                FXCollections.observableArrayList(
                        allMatches
                )
        );

        Runnable applyFilter = () -> {

            String keyword =
                    searchField.getText() == null
                            ? ""
                            : searchField
                              .getText()
                              .trim()
                              .toLowerCase();

            String confSel =
                    confidenceFilter.getValue();

            String statSel =
                    statusFilter.getValue();

            List<MatchView> filtered =
                    allMatches
                            .stream()

                            .filter(mv ->
                                    keyword.isEmpty()
                                            ||
                                            safe(mv.getLostTitle())
                                                    .toLowerCase()
                                                    .contains(keyword)
                                            ||
                                            safe(mv.getFoundTitle())
                                                    .toLowerCase()
                                                    .contains(keyword)
                                            ||
                                            safe(mv.getCategory())
                                                    .toLowerCase()
                                                    .contains(keyword)
                                            ||
                                            safe(mv.getLostLocation())
                                                    .toLowerCase()
                                                    .contains(keyword)
                            )

                            .filter(mv ->
                                    confSel == null
                                            ||
                                            confSel.startsWith("All")
                                            ||
                                            confSel.equals(
                                                    mv.getConfidence()
                                            )
                            )

                            .filter(mv ->
                                    statSel == null
                                            ||
                                            statSel.startsWith("All")
                                            ||
                                            statSel.equals(
                                                    mv.getStatus()
                                            )
                            )

                            .collect(
                                    Collectors.toList()
                            );

            table.setItems(
                    FXCollections.observableArrayList(
                            filtered
                    )
            );
        };

        searchField.textProperty()
                .addListener(
                        (obs, oldVal, newVal) ->
                                applyFilter.run()
                );

        confidenceFilter.setOnAction(
                e -> applyFilter.run()
        );

        statusFilter.setOnAction(
                e -> applyFilter.run()
        );

        recalcButton.setOnAction(e -> {

            matchService.recalculateAllMatches();

            showAlert(
                    Alert.AlertType.INFORMATION,
                    "Matches Refreshed",
                    "All lost/found reports have been re-evaluated."
            );

            buildMatchReview();
        });

        panel.getChildren().addAll(
                heading,
                note,
                filterRow
        );

        if (allMatches.isEmpty()) {

            Label emptyLabel =
                    new Label(
                            "No matches yet. Click Recalculate Matches to scan existing reports."
                    );

            emptyLabel.setWrapText(true);

            emptyLabel.setStyle(
                    "-fx-text-fill: #777;" +
                            "-fx-font-size: 12px;"
            );

            panel.getChildren()
                    .add(emptyLabel);

        } else {

            panel.getChildren()
                    .add(table);
        }

        contentArea
                .getChildren()
                .setAll(panel);
    }

    // ===============================================================
    // MATCH DETAILS
    // ===============================================================

    private void showMatchDetailsDialog(
            MatchView mv) {

        Stage dialog = new Stage();

        dialog.initModality(
                Modality.APPLICATION_MODAL
        );

        dialog.setTitle(
                "Match Details"
        );

        VBox root = new VBox(15);

        root.setStyle(
                "-fx-padding: 25;" +
                        "-fx-background-color: white;"
        );

        Label heading =
                new Label("Match Details");

        heading.getStyleClass()
                .add("dashboard-heading");

        HBox itemsRow =
                new HBox(
                        20,

                        buildReportDetailBox(
                                "Lost Item",
                                mv.getLostTitle(),
                                mv.getLostCategory(),
                                mv.getLostLocation(),
                                mv.getLostDate(),
                                mv.getLostBrand(),
                                mv.getLostColor(),
                                mv.getLostDescription(),
                                mv.getLostReporterName()
                        ),

                        buildReportDetailBox(
                                "Found Item",
                                mv.getFoundTitle(),
                                mv.getFoundCategory(),
                                mv.getFoundLocation(),
                                mv.getFoundDate(),
                                mv.getFoundBrand(),
                                mv.getFoundColor(),
                                mv.getFoundDescription(),
                                mv.getFoundReporterName()
                        )
                );

        VBox criteriaBox =
                new VBox(8);

        criteriaBox.setStyle(
                "-fx-background-color: #f4f6f8;" +
                        "-fx-background-radius: 10;" +
                        "-fx-padding: 15;"
        );

        Label criteriaTitle =
                new Label(
                        "Matching Criteria Breakdown"
                );

        criteriaTitle.setStyle(
                "-fx-font-weight: bold;" +
                        "-fx-font-size: 14px;"
        );

        criteriaBox.getChildren().addAll(

                criteriaTitle,

                buildCriterionRow(
                        "Category Match (25%)",
                        mv.getCategoryScore()
                ),

                buildCriterionRow(
                        "Description Similarity (30%)",
                        mv.getDescriptionScore()
                ),

                buildCriterionRow(
                        "Location Similarity (20%)",
                        mv.getLocationScore()
                ),

                buildCriterionRow(
                        "Date Proximity (15%)",
                        mv.getDateScore()
                ),

                buildCriterionRow(
                        "Attribute Similarity (10%)",
                        mv.getAttributeScore()
                )
        );

        HBox finalScoreRow =
                new HBox(10);

        finalScoreRow.setAlignment(
                Pos.CENTER_LEFT
        );

        Label finalLabel =
                new Label(
                        "Final Match Score:"
                );

        finalLabel.setStyle(
                "-fx-font-size: 15px;" +
                        "-fx-font-weight: bold;"
        );

        Label finalValue =
                new Label(
                        String.format(
                                "%.0f%% (%s Confidence)",
                                mv.getMatchScore(),
                                mv.getConfidence()
                        )
                );

        finalValue.setStyle(
                "-fx-font-size: 15px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #145DA0;"
        );

        finalScoreRow.getChildren()
                .addAll(
                        finalLabel,
                        finalValue
                );

        Label reasonTitle =
                new Label(
                        "Why these were matched:"
                );

        reasonTitle.setStyle(
                "-fx-font-weight: bold;"
        );

        Label reasonText =
                new Label(
                        mv.getReason()
                );

        reasonText.setWrapText(true);

        reasonText.setStyle(
                "-fx-text-fill: #555;"
        );

        Button closeButton =
                new Button("Close");

        closeButton.getStyleClass()
                .add("primary-button");

        closeButton.setOnAction(
                e -> dialog.close()
        );

        root.getChildren().addAll(
                heading,
                itemsRow,
                criteriaBox,
                finalScoreRow,
                reasonTitle,
                reasonText,
                closeButton
        );

        ScrollPane scrollPane =
                new ScrollPane(root);

        scrollPane.setFitToWidth(true);

        dialog.setScene(
                new Scene(
                        scrollPane,
                        640,
                        640
                )
        );

        dialog.showAndWait();
    }

    private VBox buildReportDetailBox(
            String heading,
            String title,
            String category,
            String location,
            String date,
            String brand,
            String color,
            String description,
            String reporterName) {

        VBox box = new VBox(6);

        box.setStyle(
                "-fx-background-color: #f4f6f8;" +
                        "-fx-background-radius: 10;" +
                        "-fx-padding: 15;"
        );

        box.setPrefWidth(280);

        Label headingLabel =
                new Label(heading);

        headingLabel.setStyle(
                "-fx-font-weight: bold;" +
                        "-fx-font-size: 14px;" +
                        "-fx-text-fill: #145DA0;"
        );

        Label titleLabel =
                new Label(
                        "Title: " + safe(title)
                );

        Label categoryLabel =
                new Label(
                        "Category: " + safe(category)
                );

        Label locationLabel =
                new Label(
                        "Location: " + safe(location)
                );

        Label dateLabel =
                new Label(
                        "Date: " + safe(date)
                );

        Label brandLabel =
                new Label(
                        "Brand: " + safe(brand)
                );

        Label colorLabel =
                new Label(
                        "Color: " + safe(color)
                );

        Label reporterLabel =
                new Label(
                        "Reported by: "
                                + safe(reporterName)
                );

        Label descLabel =
                new Label(
                        "Description: "
                                + safe(description)
                );

        descLabel.setWrapText(true);

        for (Label l :
                List.of(
                        titleLabel,
                        categoryLabel,
                        locationLabel,
                        dateLabel,
                        brandLabel,
                        colorLabel,
                        reporterLabel,
                        descLabel
                )) {

            l.setStyle(
                    "-fx-font-size: 12px;"
            );
        }

        box.getChildren().addAll(
                headingLabel,
                titleLabel,
                categoryLabel,
                locationLabel,
                dateLabel,
                brandLabel,
                colorLabel,
                reporterLabel,
                descLabel
        );

        return box;
    }

    private HBox buildCriterionRow(
            String label,
            double score) {

        HBox row = new HBox(10);

        row.setAlignment(
                Pos.CENTER_LEFT
        );

        Label nameLabel =
                new Label(label);

        nameLabel.setPrefWidth(220);

        nameLabel.setStyle(
                "-fx-font-size: 12px;"
        );

        ProgressBar bar =
                new ProgressBar(
                        score / 100.0
                );

        bar.setPrefWidth(200);

        Label valueLabel =
                new Label(
                        String.format(
                                "%.0f%%",
                                score
                        )
                );

        valueLabel.setStyle(
                "-fx-font-size: 12px;" +
                        "-fx-font-weight: bold;"
        );

        row.getChildren().addAll(
                nameLabel,
                bar,
                valueLabel
        );

        return row;
    }

    // ===============================================================
    // CLAIM VERIFICATION
    // ===============================================================

    private void buildClaimVerification() {

        VBox panel = new VBox(15);

        Label heading =
                new Label("Claim Verification");

        heading.getStyleClass()
                .add("dashboard-heading");

        Label note =
                new Label(
                        "Pending claims awaiting admin verification. " +
                                "Approve valid claims or reject claims that fail verification."
                );

        note.getStyleClass()
                .add("section-note");

        note.setWrapText(true);

        List<Claim> pendingClaims =
                claimDao.findAll()
                        .stream()
                        .filter(c ->
                                "PENDING"
                                        .equalsIgnoreCase(
                                                c.getStatus()
                                        )
                        )
                        .collect(
                                Collectors.toList()
                        );

        VBox listBox =
                new VBox(12);

        if (pendingClaims.isEmpty()) {

            Label emptyLabel =
                    new Label(
                            "No pending claims to review."
                    );

            emptyLabel.setStyle(
                    "-fx-text-fill: #777;" +
                            "-fx-font-size: 13px;"
            );

            listBox.getChildren()
                    .add(emptyLabel);

        } else {

            for (Claim claim :
                    pendingClaims) {

                listBox.getChildren()
                        .add(
                                buildClaimCard(claim)
                        );
            }
        }

        panel.getChildren().addAll(
                heading,
                note,
                listBox
        );

        contentArea
                .getChildren()
                .setAll(panel);
    }

    // ===============================================================
    // CLAIM CARD
    // ===============================================================

    private VBox buildClaimCard(
            Claim claim) {

        VBox card =
                new VBox(8);

        card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 10;" +
                        "-fx-padding: 15;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);"
        );

        Match match =
                matchDao
                        .findById(
                                claim.getMatchId()
                        )
                        .orElse(null);

        String itemInfo =
                "Unknown item";

        if (match != null) {

            ItemReport lost =
                    itemReportDao.findById(
                            match.getLostReportId()
                    );

            ItemReport found =
                    itemReportDao.findById(
                            match.getFoundReportId()
                    );

            itemInfo =
                    "Lost: "
                            + (lost != null
                            ? lost.getTitle()
                            : "N/A")
                            +
                            "   |   Found: "
                            +
                            (found != null
                                    ? found.getTitle()
                                    : "N/A")
                            +
                            String.format(
                                    "   |   Match Score: %.0f%%",
                                    match.getMatchScore()
                            );
        }

        String claimantName =
                resolveUserName(
                        claim.getClaimantId()
                );

        Label claimLabel =
                new Label(
                        "Claim #"
                                + claim.getClaimId()
                                + " by "
                                + claimantName
                );

        claimLabel.setStyle(
                "-fx-font-size: 14px;" +
                        "-fx-font-weight: bold;"
        );

        Label detailsLabel =
                new Label(itemInfo);

        detailsLabel.setStyle(
                "-fx-font-size: 12px;" +
                        "-fx-text-fill: #555;"
        );

        detailsLabel.setWrapText(true);

        Label statusLabel =
                new Label(
                        "Status: "
                                + claim.getStatus()
                );

        statusLabel.setStyle(
                "-fx-font-size: 12px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #b8860b;"
        );

        card.getChildren().addAll(
                claimLabel,
                detailsLabel,
                statusLabel
        );

        HBox actions =
                new HBox(8);

        // -----------------------------------------------------------
        // APPROVE
        // -----------------------------------------------------------

        Button approveBtn =
                new Button("Approve");

        approveBtn.getStyleClass()
                .add("success-button");

        approveBtn.setOnAction(e -> {

            Match approvalMatch =
                    matchDao.findById(claim.getMatchId())
                            .orElse(null);

            if (approvalMatch == null
                    || !"CONFIRMED".equalsIgnoreCase(approvalMatch.getStatus())) {

                showAlert(
                        Alert.AlertType.WARNING,
                        "Cannot Approve",
                        "This claim's match is not confirmed, so it cannot be approved."
                );

                handleClaimVerification();
                return;
            }

            boolean[] approvedOk = {false};

            DatabaseConnection.runInTransaction(() -> {
                boolean updated = claimDao.updateStatus(
                        claim.getClaimId(),
                        "APPROVED",
                        currentUser.getUserId()
                );
                if (!updated) {
                    throw new IllegalStateException(
                            "Failed to approve claim " + claim.getClaimId()
                    );
                }
                // Match stays CONFIRMED; the two linked reports move from
                // MATCHED to CLAIMED now that the claim is approved.
                markReportClaimed(approvalMatch.getLostReportId());
                markReportClaimed(approvalMatch.getFoundReportId());
                approvedOk[0] = true;
            });

            if (approvedOk[0]) {

                NotificationPublisher
                        .getInstance()
                        .publish(
                                claim.getClaimantId(),
                                "Your claim #"
                                        + claim.getClaimId()
                                        + " has been approved!"
                        );

                showAlert(
                        Alert.AlertType.INFORMATION,
                        "Claim Approved",
                        "Claim #" + claim.getClaimId()
                                + " has been approved."
                );

            } else {

                showAlert(
                        Alert.AlertType.ERROR,
                        "Approval Failed",
                        "Could not approve claim #" + claim.getClaimId() + "."
                );
            }

            handleClaimVerification();
        });

        // -----------------------------------------------------------
        // REJECT
        // -----------------------------------------------------------

        Button rejectBtn =
                new Button("Reject");

        rejectBtn.getStyleClass()
                .add("danger-button");

        rejectBtn.setOnAction(e -> {

            claimDao.updateStatus(
                    claim.getClaimId(),
                    "REJECTED",
                    currentUser.getUserId()
            );

            NotificationPublisher
                    .getInstance()
                    .publish(
                            claim.getClaimantId(),
                            "Your claim #"
                                    + claim.getClaimId()
                                    + " has been rejected."
                    );

            showAlert(
                    Alert.AlertType.INFORMATION,
                    "Claim Rejected",
                    "Claim #" + claim.getClaimId()
                            + " has been rejected."
            );

            handleClaimVerification();
        });

        actions.getChildren().addAll(
                approveBtn,
                rejectBtn
        );

        card.getChildren()
                .add(actions);

        return card;
    }

    // ===============================================================
    // RETURNS / CLOSURE
    // ===============================================================

    private void buildReturnsClosure() {

        VBox panel =
                new VBox(15);

        Label heading =
                new Label("Returns / Closure");

        heading.getStyleClass()
                .add("dashboard-heading");

        Label note =
                new Label(
                        "Confirm physical return for approved claims. " +
                                "Returned claims remain visible as closure history."
                );

        note.getStyleClass()
                .add("section-note");

        note.setWrapText(true);

        List<Claim> relevantClaims =
                claimDao.findAll()
                        .stream()
                        .filter(c ->
                                "APPROVED"
                                        .equalsIgnoreCase(
                                                c.getStatus()
                                        )
                                        ||
                                        "RETURNED"
                                                .equalsIgnoreCase(
                                                        c.getStatus()
                                                )
                        )
                        .collect(
                                Collectors.toList()
                        );

        VBox listBox =
                new VBox(12);

        if (relevantClaims.isEmpty()) {

            Label emptyLabel =
                    new Label(
                            "No approved or returned claims yet."
                    );

            emptyLabel.setStyle(
                    "-fx-text-fill: #777;"
            );

            listBox.getChildren()
                    .add(emptyLabel);

        } else {

            for (Claim claim :
                    relevantClaims) {

                listBox.getChildren()
                        .add(
                                buildReturnClaimCard(
                                        claim
                                )
                        );
            }
        }

        panel.getChildren().addAll(
                heading,
                note,
                listBox
        );

        contentArea
                .getChildren()
                .setAll(panel);
    }

    // ===============================================================
    // RETURN CLAIM CARD
    // ===============================================================

    private VBox buildReturnClaimCard(
            Claim claim) {

        VBox card =
                new VBox(8);

        card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 10;" +
                        "-fx-padding: 15;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);"
        );

        Match match =
                matchDao
                        .findById(
                                claim.getMatchId()
                        )
                        .orElse(null);

        String itemInfo =
                "Unknown item";

        if (match != null) {

            ItemReport lost =
                    itemReportDao.findById(
                            match.getLostReportId()
                    );

            ItemReport found =
                    itemReportDao.findById(
                            match.getFoundReportId()
                    );

            itemInfo =
                    "Lost: "
                            + (lost != null
                            ? lost.getTitle()
                            : "N/A")
                            +
                            " | Found: "
                            +
                            (found != null
                                    ? found.getTitle()
                                    : "N/A");
        }

        Label claimLabel =
                new Label(
                        "Claim #"
                                + claim.getClaimId()
                                + " by "
                                + resolveUserName(
                                claim.getClaimantId()
                        )
                );

        claimLabel.setStyle(
                "-fx-font-size: 14px;" +
                        "-fx-font-weight: bold;"
        );

        Label itemLabel =
                new Label(itemInfo);

        itemLabel.setWrapText(true);

        itemLabel.setStyle(
                "-fx-font-size: 12px;" +
                        "-fx-text-fill: #555;"
        );

        Label statusLabel =
                new Label(
                        "Status: "
                                + claim.getStatus()
                );

        statusLabel.setStyle(
                "-fx-font-size: 12px;" +
                        "-fx-font-weight: bold;"
        );

        card.getChildren().addAll(
                claimLabel,
                itemLabel,
                statusLabel
        );

        if ("APPROVED".equalsIgnoreCase(
                claim.getStatus())) {

            Button returnBtn =
                    new Button(
                            "Confirm Return"
                    );

            returnBtn.getStyleClass()
                    .add("secondary-button");

            returnBtn.setOnAction(e -> {

                boolean[] returnedOk = {false};

                DatabaseConnection.runInTransaction(() -> {
                    boolean updated = claimDao.updateStatus(
                            claim.getClaimId(),
                            "RETURNED",
                            currentUser.getUserId()
                    );
                    if (!updated) {
                        throw new IllegalStateException(
                                "Failed to confirm return for claim " + claim.getClaimId()
                        );
                    }

                    if (match != null) {

                        updateReportToReturned(
                                match.getLostReportId()
                        );

                        updateReportToReturned(
                                match.getFoundReportId()
                        );
                    }

                    returnedOk[0] = true;
                });

                if (returnedOk[0]) {

                    NotificationPublisher
                            .getInstance()
                            .publish(
                                    claim.getClaimantId(),
                                    "Your claimed item (Claim #"
                                            + claim.getClaimId()
                                            + ") has been marked as returned."
                            );

                    showAlert(
                            Alert.AlertType.INFORMATION,
                            "Return Confirmed",
                            "The item return has been confirmed."
                    );

                } else {

                    showAlert(
                            Alert.AlertType.ERROR,
                            "Return Failed",
                            "Could not confirm the return for claim #" + claim.getClaimId() + "."
                    );
                }

                buildReturnsClosure();
            });

            card.getChildren()
                    .add(returnBtn);
        }

        return card;
    }

    // ===============================================================
    // UPDATE REPORT TO CLAIMED
    // ===============================================================

    /**
     * Moves a report from MATCHED to CLAIMED once its claim is approved.
     * This is the missing link that previously left reports stuck at
     * MATCHED forever: without it, Return Confirmation could never
     * legally transition a report to RETURNED (a report can only be
     * returned once it has been claimed).
     */
    private void markReportClaimed(int reportId) {

        ItemReport report =
                itemReportDao.findById(reportId);

        if (report == null) {
            return;
        }

        try {

            ReportState currentState =
                    ReportStateFactory.fromString(
                            report.getStatus()
                    );

            ReportState newState =
                    currentState.markClaimed();

            itemReportDao.updateStatus(
                    reportId,
                    newState.getName()
            );

        } catch (IllegalStateException e) {

            throw new IllegalStateException(
                    "Report " + reportId + " is not in a claimable state ("
                            + report.getStatus() + ")", e
            );
        }
    }

    // ===============================================================
    // UPDATE REPORT TO RETURNED
    // ===============================================================

    private void updateReportToReturned(
            int reportId) {

        ItemReport report =
                itemReportDao.findById(
                        reportId
                );

        if (report == null) {
            return;
        }

        try {

            ReportState currentState =
                    ReportStateFactory.fromString(
                            report.getStatus()
                    );

            ReportState newState =
                    currentState.markReturned();

            itemReportDao.updateStatus(
                    reportId,
                    newState.getName()
            );

        } catch (IllegalStateException e) {

            // Previously swallowed silently, which let a claim become
            // RETURNED while its report stayed stuck at an earlier status.
            // Now propagated so the surrounding transaction rolls back and
            // the admin sees the failure instead of a silent partial update.
            throw new IllegalStateException(
                    "Report " + reportId + " could not be marked returned ("
                            + report.getStatus() + ")", e
            );
        }
    }

    // ===============================================================
    // USER MANAGEMENT
    // ===============================================================

    private void buildUserManagement() {

        VBox panel =
                new VBox(15);

        Label heading =
                new Label("User Management");

        heading.getStyleClass()
                .add("dashboard-heading");

        Label note =
                new Label(
                        "All registered users (read-only view)."
                );

        note.getStyleClass()
                .add("section-note");

        TableView<User> table =
                new TableView<>();

        table.setItems(
                FXCollections.observableArrayList(
                        userDao.getAllUsers()
                )
        );

        TableColumn<User, Integer> idCol =
                new TableColumn<>("ID");

        idCol.setCellValueFactory(
                new PropertyValueFactory<>(
                        "userId"
                )
        );

        TableColumn<User, String> nameCol =
                new TableColumn<>("Name");

        nameCol.setCellValueFactory(
                new PropertyValueFactory<>(
                        "name"
                )
        );

        TableColumn<User, String> emailCol =
                new TableColumn<>("Email");

        emailCol.setCellValueFactory(
                new PropertyValueFactory<>(
                        "email"
                )
        );

        TableColumn<User, String> studentIdCol =
                new TableColumn<>("Student ID");

        studentIdCol.setCellValueFactory(
                new PropertyValueFactory<>(
                        "studentId"
                )
        );

        TableColumn<User, String> roleCol =
                new TableColumn<>("Role");

        roleCol.setCellValueFactory(
                new PropertyValueFactory<>(
                        "role"
                )
        );

        table.getColumns().addAll(
                idCol,
                nameCol,
                emailCol,
                studentIdCol,
                roleCol
        );

        table.setPrefHeight(320);

        panel.getChildren().addAll(
                heading,
                note,
                table
        );

        contentArea
                .getChildren()
                .setAll(panel);
    }

    // ===============================================================
    // REPORTS & ANALYTICS
    // ===============================================================

    private void buildReportsAnalytics() {

        VBox panel =
                new VBox(20);

        Label heading =
                new Label(
                        "Reports & Analytics"
                );

        heading.getStyleClass()
                .add("dashboard-heading");

        List<ItemReport> allReports =
                itemReportDao.findAll();

        List<Match> allMatches =
                matchDao.findAll();

        List<Claim> allClaims =
                claimDao.findAll();

        int total =
                allReports.size();

        long lostCount =
                allReports.stream()
                        .filter(r ->
                                "LOST".equalsIgnoreCase(
                                        r.getType()
                                ))
                        .count();

        long foundCount =
                allReports.stream()
                        .filter(r ->
                                "FOUND".equalsIgnoreCase(
                                        r.getType()
                                ))
                        .count();

        long openCount =
                allReports.stream()
                        .filter(r ->
                                "REPORTED".equalsIgnoreCase(
                                        r.getStatus()
                                ))
                        .count();

        long matchedCount =
                allReports.stream()
                        .filter(r ->
                                "MATCHED".equalsIgnoreCase(
                                        r.getStatus()
                                ))
                        .count();

        long claimedCount =
                allReports.stream()
                        .filter(r ->
                                "CLAIMED".equalsIgnoreCase(
                                        r.getStatus()
                                ))
                        .count();

        long closedCount =
                allReports.stream()
                        .filter(r ->
                                "RETURNED".equalsIgnoreCase(
                                        r.getStatus()
                                ))
                        .count();

        double resolutionRate =
                total == 0
                        ? 0.0
                        : closedCount * 100.0 / total;

        HBox statsRow1 =
                new HBox(
                        15,
                        createStatCard(
                                String.valueOf(total),
                                "Total Reports"
                        ),
                        createStatCard(
                                String.valueOf(lostCount),
                                "Lost"
                        ),
                        createStatCard(
                                String.valueOf(foundCount),
                                "Found"
                        ),
                        createStatCard(
                                String.valueOf(openCount),
                                "Open / Pending"
                        )
                );

        HBox statsRow2 =
                new HBox(
                        15,
                        createStatCard(
                                String.valueOf(matchedCount),
                                "Matched"
                        ),
                        createStatCard(
                                String.valueOf(claimedCount),
                                "Claimed"
                        ),
                        createStatCard(
                                String.valueOf(closedCount),
                                "Closed / Returned"
                        ),
                        createStatCard(
                                String.format(
                                        "%.0f%%",
                                        resolutionRate
                                ),
                                "Resolution Rate"
                        )
                );

        // -----------------------------------------------------------
        // STATUS CHART
        // -----------------------------------------------------------

        BarChart<String, Number> statusChart =
                new BarChart<>(
                        new CategoryAxis(),
                        new NumberAxis()
                );

        statusChart.setTitle(
                "Reports by Status"
        );

        statusChart.setLegendVisible(false);

        statusChart.setPrefHeight(260);

        XYChart.Series<String, Number> statusSeries =
                new XYChart.Series<>();

        for (String status :
                List.of(
                        "REPORTED",
                        "MATCHED",
                        "CLAIMED",
                        "RETURNED"
                )) {

            long count =
                    allReports.stream()
                            .filter(r ->
                                    status.equalsIgnoreCase(
                                            r.getStatus()
                                    ))
                            .count();

            statusSeries.getData()
                    .add(
                            new XYChart.Data<>(
                                    status,
                                    count
                            )
                    );
        }

        statusChart.getData()
                .add(statusSeries);

        // -----------------------------------------------------------
        // LOST VS FOUND
        // -----------------------------------------------------------

        PieChart typeChart =
                new PieChart();

        typeChart.setTitle(
                "Lost vs Found"
        );

        typeChart.setPrefHeight(260);

        typeChart.getData().add(
                new PieChart.Data(
                        "Lost (" + lostCount + ")",
                        lostCount
                )
        );

        typeChart.getData().add(
                new PieChart.Data(
                        "Found (" + foundCount + ")",
                        foundCount
                )
        );

        HBox chartsRow1 =
                new HBox(
                        20,
                        statusChart,
                        typeChart
                );

        HBox.setHgrow(
                statusChart,
                Priority.ALWAYS
        );

        HBox.setHgrow(
                typeChart,
                Priority.ALWAYS
        );

        // -----------------------------------------------------------
        // CATEGORY
        // -----------------------------------------------------------

        Map<String, Long> categoryCounts =
                allReports.stream()
                        .collect(
                                Collectors.groupingBy(
                                        r ->
                                                r.getCategory() == null
                                                        ||
                                                        r.getCategory()
                                                                .isBlank()
                                                        ? "Uncategorized"
                                                        : r.getCategory(),

                                        LinkedHashMap::new,

                                        Collectors.counting()
                                )
                        );

        BarChart<String, Number> categoryChart =
                new BarChart<>(
                        new CategoryAxis(),
                        new NumberAxis()
                );

        categoryChart.setTitle(
                "Reports by Category"
        );

        categoryChart.setLegendVisible(false);

        categoryChart.setPrefHeight(260);

        XYChart.Series<String, Number> categorySeries =
                new XYChart.Series<>();

        categoryCounts.forEach(
                (cat, count) ->
                        categorySeries.getData()
                                .add(
                                        new XYChart.Data<>(
                                                cat,
                                                count
                                        )
                                )
        );

        categoryChart.getData()
                .add(categorySeries);

        // -----------------------------------------------------------
        // LOCATIONS
        // -----------------------------------------------------------

        Map<String, Long> locationCounts =
                allReports.stream()
                        .collect(
                                Collectors.groupingBy(
                                        r ->
                                                r.getLocation() == null
                                                        ||
                                                        r.getLocation()
                                                                .isBlank()
                                                        ? "Unspecified"
                                                        : r.getLocation(),
                                        Collectors.counting()
                                )
                        );

        List<Map.Entry<String, Long>> topLocations =
                locationCounts.entrySet()
                        .stream()
                        .sorted(
                                (a, b) ->
                                        Long.compare(
                                                b.getValue(),
                                                a.getValue()
                                        )
                        )
                        .limit(6)
                        .collect(
                                Collectors.toList()
                        );

        BarChart<String, Number> locationChart =
                new BarChart<>(
                        new CategoryAxis(),
                        new NumberAxis()
                );

        locationChart.setTitle(
                "Top Locations"
        );

        locationChart.setLegendVisible(false);

        locationChart.setPrefHeight(260);

        XYChart.Series<String, Number> locationSeries =
                new XYChart.Series<>();

        for (Map.Entry<String, Long> entry :
                topLocations) {

            locationSeries.getData()
                    .add(
                            new XYChart.Data<>(
                                    entry.getKey(),
                                    entry.getValue()
                            )
                    );
        }

        locationChart.getData()
                .add(locationSeries);

        HBox chartsRow2 =
                new HBox(
                        20,
                        categoryChart,
                        locationChart
                );

        HBox.setHgrow(
                categoryChart,
                Priority.ALWAYS
        );

        HBox.setHgrow(
                locationChart,
                Priority.ALWAYS
        );

        // -----------------------------------------------------------
        // MATCH & CLAIM SUMMARY
        // -----------------------------------------------------------

        long totalMatches =
                allMatches.size();

        long pendingMatches =
                allMatches.stream()
                        .filter(m ->
                                "PENDING".equalsIgnoreCase(
                                        m.getStatus()
                                ))
                        .count();

        long confirmedMatches =
                allMatches.stream()
                        .filter(m ->
                                "CONFIRMED".equalsIgnoreCase(
                                        m.getStatus()
                                ))
                        .count();

        long rejectedMatches =
                allMatches.stream()
                        .filter(m ->
                                "REJECTED".equalsIgnoreCase(
                                        m.getStatus()
                                ))
                        .count();

        long totalClaims =
                allClaims.size();

        long pendingClaimsCount =
                allClaims.stream()
                        .filter(c ->
                                "PENDING".equalsIgnoreCase(
                                        c.getStatus()
                                ))
                        .count();

        long approvedClaims =
                allClaims.stream()
                        .filter(c ->
                                "APPROVED".equalsIgnoreCase(
                                        c.getStatus()
                                ))
                        .count();

        long returnedClaims =
                allClaims.stream()
                        .filter(c ->
                                "RETURNED".equalsIgnoreCase(
                                        c.getStatus()
                                ))
                        .count();

        long rejectedClaims =
                allClaims.stream()
                        .filter(c ->
                                "REJECTED".equalsIgnoreCase(
                                        c.getStatus()
                                ))
                        .count();

        Label matchesClaimsTitle =
                new Label(
                        "Matches & Claims Summary"
                );

        matchesClaimsTitle.setStyle(
                "-fx-font-size: 16px;" +
                        "-fx-font-weight: bold;"
        );

        HBox matchesRow =
                new HBox(
                        15,
                        createStatCard(
                                String.valueOf(
                                        totalMatches
                                ),
                                "Total Matches"
                        ),
                        createStatCard(
                                String.valueOf(
                                        pendingMatches
                                ),
                                "Pending Matches"
                        ),
                        createStatCard(
                                String.valueOf(
                                        confirmedMatches
                                ),
                                "Confirmed Matches"
                        ),
                        createStatCard(
                                String.valueOf(
                                        rejectedMatches
                                ),
                                "Rejected Matches"
                        )
                );

        HBox claimsRow =
                new HBox(
                        15,
                        createStatCard(
                                String.valueOf(
                                        totalClaims
                                ),
                                "Total Claims"
                        ),
                        createStatCard(
                                String.valueOf(
                                        pendingClaimsCount
                                ),
                                "Pending Claims"
                        ),
                        createStatCard(
                                String.valueOf(
                                        approvedClaims
                                ),
                                "Approved Claims"
                        ),
                        createStatCard(
                                String.valueOf(
                                        returnedClaims
                                ),
                                "Returned Claims"
                        ),
                        createStatCard(
                                String.valueOf(
                                        rejectedClaims
                                ),
                                "Rejected Claims"
                        )
                );

        panel.getChildren().addAll(
                heading,
                statsRow1,
                statsRow2,
                chartsRow1,
                chartsRow2,
                matchesClaimsTitle,
                matchesRow,
                claimsRow
        );

        contentArea
                .getChildren()
                .setAll(panel);
    }

    // ===============================================================
    // SHARED HELPERS
    // ===============================================================

    private VBox createStatCard(
            String number,
            String label) {

        VBox card =
                new VBox();

        card.getStyleClass()
                .add("stat-card");

        card.setMaxWidth(
                Double.MAX_VALUE
        );

        HBox.setHgrow(
                card,
                Priority.ALWAYS
        );

        Label numberLabel =
                new Label(number);

        numberLabel.getStyleClass()
                .add("stat-number");

        Label textLabel =
                new Label(label);

        textLabel.getStyleClass()
                .add("stat-label");

        card.getChildren().addAll(
                numberLabel,
                textLabel
        );

        return card;
    }

    private String resolveUserName(
            int userId) {

        User user =
                userDao.findById(userId);

        return user != null
                ? user.getName()
                : "Unknown";
    }

    private String safe(
            String value) {

        return value == null
                || value.trim().isEmpty()
                ? "-"
                : value;
    }

    private void showAlert(
            Alert.AlertType type,
            String title,
            String message) {

        Alert alert =
                new Alert(type);

        alert.setTitle(title);

        alert.setHeaderText(null);

        alert.setContentText(message);

        alert.showAndWait();
    }
}