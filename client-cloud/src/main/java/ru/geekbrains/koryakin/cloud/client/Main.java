package ru.geekbrains.koryakin.cloud.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/main.fxml"));
        Parent root = loader.load();

        Controller controller = loader.getController();
        controller.setStageAndSetupListeners(primaryStage);

        primaryStage.setTitle("Cloud Storage");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    public static void main(String[] args) {
        new File("clientStorage/").mkdirs();
        launch(args);
    }
}
