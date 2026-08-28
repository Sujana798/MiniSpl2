package com.lostlink.app;



import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        AppBootstrap.initializeDatabase();

        Parent root = FXMLLoader.load(getClass().getResource("/fxml/login.fxml"));
        primaryStage.setTitle("LostLink - Login");
        primaryStage.setScene(new Scene(root, 400, 350));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
