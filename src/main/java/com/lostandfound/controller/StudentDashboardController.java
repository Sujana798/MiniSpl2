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
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.geometry.Pos;
import com.lostandfound.dao.ItemReportDao;
import com.lostandfound.dao.ClaimDao;
import com.lostandfound.dao.MatchDao;
import com.lostandfound.dao.ItemReportDao;
import com.lostandfound.model.Claim;
import com.lostandfound.model.Match;
import com.lostandfound.model.ItemReport;
import java.util.List;

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

        ItemReportDao itemReportDao = new ItemReportDao();
        int myReportsCount = itemReportDao.countByReporterId(currentUser.getUserId());

        HBox statRow = new HBox(20);
        statRow.getChildren().addAll(
                buildStatCard(String.valueOf(myReportsCount), "My Reports"),
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
        contentArea.getChildren().clear();
        contentArea.setStyle("-fx-padding: 30; -fx-background-color: #f4f6f8;");
        contentArea.setSpacing(15);

        Label heading = new Label("Browse Lost & Found Items");
        heading.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        ComboBox<String> typeFilter = new ComboBox<>();
        typeFilter.getItems().addAll("All", "LOST", "FOUND");
        typeFilter.setValue("All");

        ComboBox<String> categoryFilter = new ComboBox<>();
        categoryFilter.getItems().addAll("All", "Electronics", "Documents", "Bags", "Clothing", "Accessories", "Others");
        categoryFilter.setValue("All");

        HBox filterBox = new HBox(10);
        filterBox.setAlignment(Pos.CENTER_LEFT);
        filterBox.getChildren().addAll(new Label("Type:"), typeFilter, new Label("Category:"), categoryFilter);

        VBox resultsBox = new VBox(12);

        ItemReportDao itemReportDao = new ItemReportDao();
        List<ItemReport> allReports = itemReportDao.findAll();

        Runnable refreshResults = () -> {
            resultsBox.getChildren().clear();

            String selectedType = typeFilter.getValue();
            String selectedCategory = categoryFilter.getValue();

            List<ItemReport> filtered = allReports.stream()
                    .filter(r -> selectedType.equals("All") || r.getType().equals(selectedType))
                    .filter(r -> selectedCategory.equals("All") || r.getCategory().equals(selectedCategory))
                    .toList();

            if (filtered.isEmpty()) {
                Label emptyLabel = new Label("No items found matching your filters.");
                emptyLabel.setStyle("-fx-text-fill: #777;");
                resultsBox.getChildren().add(emptyLabel);
                return;
            }

            for (ItemReport report : filtered) {
                resultsBox.getChildren().add(buildBrowseCard(report));
            }
        };

        typeFilter.setOnAction(e -> refreshResults.run());
        categoryFilter.setOnAction(e -> refreshResults.run());

        refreshResults.run();

        contentArea.getChildren().addAll(heading, filterBox, resultsBox);
    }

    private VBox buildBrowseCard(ItemReport report) {
        VBox card = new VBox(6);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-padding: 15; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);");

        String badgeColor = report.getType().equals("LOST") ? "#e74c3c" : "#2e8b57";

        Label typeBadge = new Label(report.getType());
        typeBadge.setStyle("-fx-background-color: " + badgeColor + "; -fx-text-fill: white; " +
                "-fx-padding: 2 10; -fx-background-radius: 10; -fx-font-size: 11px; -fx-font-weight: bold;");

        Label titleLabel = new Label(report.getTitle());
        titleLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold;");

        HBox topRow = new HBox(10, typeBadge, titleLabel);
        topRow.setAlignment(Pos.CENTER_LEFT);

        Label detailsLabel = new Label(
                "Category: " + report.getCategory() +
                        (report.getBrand() != null && !report.getBrand().isBlank() ? "   |   Brand: " + report.getBrand() : "") +
                        (report.getColor() != null && !report.getColor().isBlank() ? "   |   Color: " + report.getColor() : "")
        );
        detailsLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #555;");

        Label locationLabel = new Label("Location: " + report.getLocation() + "   |   Date: " + report.getDateOccurred());
        locationLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #555;");

        Label statusLabel = new Label("Status: " + report.getStatus());
        statusLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #145DA0; -fx-font-weight: bold;");

        card.getChildren().addAll(topRow, detailsLabel, locationLabel, statusLabel);

        if (report.getDescription() != null && !report.getDescription().isBlank()) {
            Label descLabel = new Label(report.getDescription());
            descLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #777; -fx-font-style: italic;");
            descLabel.setWrapText(true);
            card.getChildren().add(descLabel);
        }

        return card;
    }
    @FXML
    private void handleMyClaims() {
        contentArea.getChildren().clear();
        contentArea.setStyle("-fx-padding: 30; -fx-background-color: #f4f6f8;");
        contentArea.setSpacing(15);

        Label heading = new Label("My Matches & Claims");
        heading.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        contentArea.getChildren().add(heading);

        ItemReportDao itemReportDao = new ItemReportDao();
        MatchDao matchDao = new MatchDao();
        ClaimDao claimDao = new ClaimDao();

        List<ItemReport> myReports = itemReportDao.findByReporterId(currentUser.getUserId());
        List<Match> allMatches = matchDao.findAll();

        List<Match> myMatches = allMatches.stream()
                .filter(m -> !"REJECTED".equalsIgnoreCase(m.getStatus()))
                .filter(m -> myReports.stream().anyMatch(r ->
                        r.getReportId() == m.getLostReportId() || r.getReportId() == m.getFoundReportId()))
                .toList();

        if (myMatches.isEmpty()) {
            Label emptyLabel = new Label("No matches found for your reports yet.");
            emptyLabel.setStyle("-fx-text-fill: #777;");
            contentArea.getChildren().add(emptyLabel);
            return;
        }

        for (Match match : myMatches) {
            ItemReport lost = itemReportDao.findById(match.getLostReportId());
            ItemReport found = itemReportDao.findById(match.getFoundReportId());
            boolean alreadyClaimed = claimDao.existsForMatch(match.getMatchId());

            contentArea.getChildren().add(buildMatchCard(match, lost, found, claimDao));
        }
    }

    private VBox buildMatchCard(Match match, ItemReport lost, ItemReport found, ClaimDao claimDao) {
        VBox card = new VBox(8);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-padding: 15; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);");

        Label scoreLabel = new Label(String.format("Match Score: %.0f%% (%s)", match.getMatchScore(), match.getConfidence()));
        scoreLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #145DA0;");

        Label lostLabel = new Label("Lost: " + (lost != null ? lost.getTitle() + " - " + lost.getLocation() : "N/A"));
        lostLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #e74c3c;");

        Label foundLabel = new Label("Found: " + (found != null ? found.getTitle() + " - " + found.getLocation() : "N/A"));
        foundLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #2e8b57;");

        Label reasonLabel = new Label(match.getMatchReason());
        reasonLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #999; -fx-font-style: italic;");
        reasonLabel.setWrapText(true);

        card.getChildren().addAll(scoreLabel, lostLabel, foundLabel, reasonLabel);

        Claim existingClaim = claimDao.findByMatchId(match.getMatchId());

        if (existingClaim != null) {
            String statusColor = switch (existingClaim.getStatus()) {
                case "PENDING" -> "#b8860b";
                case "APPROVED" -> "#145DA0";
                case "REJECTED" -> "#e74c3c";
                case "RETURNED" -> "#2e8b57";
                default -> "#555";
            };

            Label claimStatusLabel = new Label("Claim Status: " + existingClaim.getStatus());
            claimStatusLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: " + statusColor + ";");
            card.getChildren().add(claimStatusLabel);

        } else {
            Button claimButton = new Button("This is Mine — Submit Claim");
            claimButton.setStyle("-fx-background-color: #145DA0; -fx-text-fill: white; -fx-background-radius: 6; -fx-cursor: hand;");
            claimButton.setOnAction(e -> submitClaim(match, claimDao));
            card.getChildren().add(claimButton);
        }

        return card;
    }

    private void submitClaim(Match match, ClaimDao claimDao) {
        Claim claim = new Claim();
        claim.setMatchId(match.getMatchId());
        claim.setClaimantId(currentUser.getUserId());
        claim.setStatus("PENDING");

        boolean success = claimDao.insertClaim(claim);

        if (success) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Your claim has been submitted for review.");
            alert.showAndWait();
            handleMyClaims();
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to submit claim. Please try again.");
            alert.showAndWait();
        }
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