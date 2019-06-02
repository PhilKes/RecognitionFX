package main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class RecognitionFX extends Application {
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/window.fxml"));
        Parent root = loader.load();
        Controller controller = (Controller) loader.getController();
        controller.setStage(primaryStage);
        primaryStage.setTitle("Recognition FX");
        primaryStage.getIcons().addAll(
                new Image(getClass().getResourceAsStream("/icon.png"))
        );
        Scene scene=new Scene(root);
        /*scene.getStylesheets().add(
                this.getClass().getClassLoader().getResource("log-view_dark.css").toExternalForm()
        );*/
        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest((ev)->{
            System.exit(0);
        });
        primaryStage.show();
    }
    public static void main(String[] args) {
        launch(args);
    }
}
