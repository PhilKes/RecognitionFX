import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class RecognitionFX extends Application {
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("window.fxml"));
        Parent root = loader.load();
        Controller controller = (Controller) loader.getController();
        controller.setStage(primaryStage);
        primaryStage.setTitle("Recognition FX");
        primaryStage.setScene(new Scene(root));
        primaryStage.setOnCloseRequest((ev)->{
            System.exit(0);
        });
        primaryStage.show();
    }
    public static void main(String[] args) {
        launch(args);
    }
}
