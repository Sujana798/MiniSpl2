package com.lostandfound;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) {
        com.lostandfound.db.DatabaseConnection.getConnection();
        Label label = new Label("Campus Lost & Found System - Setup Successful!");
        StackPane root = new StackPane(label);
        Scene scene = new Scene(root, 500, 300);

        primaryStage.setTitle("Campus Lost & Found");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}