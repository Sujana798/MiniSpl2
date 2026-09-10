package com.lostandfound;

import com.lostandfound.dao.ItemReportDao;
import com.lostandfound.dao.MatchDao;
import com.lostandfound.dao.UserDao;
import com.lostandfound.db.DatabaseConnection;
import com.lostandfound.db.DatabaseInitializer;
import com.lostandfound.db.DataRepairUtility;
import com.lostandfound.db.DatabaseSeeder;
import com.lostandfound.service.MatchService;
import com.lostandfound.service.WeightedMatchingStrategy;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import com.lostandfound.notification.NotificationPublisher;
import com.lostandfound.notification.InAppNotificationObserver;
import com.lostandfound.dao.NotificationDao;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        DatabaseConnection.getConnection();
        DatabaseInitializer.initialize();
        DataRepairUtility.runOnce();
        DatabaseSeeder.seed();
        NotificationPublisher.getInstance().subscribe(new InAppNotificationObserver(new NotificationDao()));

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root, 500, 400);
        primaryStage.setTitle("Campus Lost & Found");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}