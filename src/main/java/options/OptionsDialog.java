package options;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.util.HashMap;

/**
 * Custom Dialog for Analyze Options Menu */
public class OptionsDialog extends Dialog<HashMap<String,String>> {

    public OptionsDialog(HashMap<String, String> tesseractOptions) {

        OptionsController controller;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/options.fxml"));
            Node root = loader.load();
            controller=loader.getController();
            controller.setOptions(tesseractOptions);
         /*   LoginDialogController controller = loader.<LoginDialogController>getController();
            controller.setModel(new LoginModel(data));*/
            setTitle("Analyze Options");
            getDialogPane().setContent(root);
            initStyle(StageStyle.DECORATED);
            getDialogPane().getButtonTypes().addAll(ButtonType.OK,ButtonType.CLOSE);
            setOnCloseRequest(e->close());
            /**
             * Returns commited Options as HashMap */
            setResultConverter(btn->{
                if(btn.getButtonData().equals(ButtonBar.ButtonData.OK_DONE)){
                    return controller.getOptions();
                }else{
                    return new HashMap<>();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
