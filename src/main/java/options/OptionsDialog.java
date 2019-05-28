package options;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.stage.StageStyle;
import javafx.util.Pair;
import util.Profile;

import java.io.IOException;

/**
 * Custom Dialog for Analyze Options Menu */
public class OptionsDialog extends Dialog<Pair<Boolean,Profile>>{

    public OptionsDialog(Profile profile) {
        OptionsController controller;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/options.fxml"));
            Node root = loader.load();
            controller=loader.getController();
            controller.setProfile(new Profile(profile.getName(),profile.getOptions()));
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
                    return new Pair<>(controller.isNewProfile(),controller.getProfile());
                }else{
                    return null;
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
