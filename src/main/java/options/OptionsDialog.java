package options;

import config.Profile;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.util.Pair;
import main.Controller;

import java.io.IOException;

/**
 * Custom Dialog for Analyze Options Menu */
public class OptionsDialog extends Dialog<Pair<Boolean,Profile>>{

    public OptionsDialog(Profile profile) {
        OptionsController controller;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/options.fxml"));
            Node root = loader.load();
            controller=loader.getController();
            controller.setProfile(new Profile(profile.getName(),profile.getOptions()));
            controller.setDialog(this);
         /*   LoginDialogController controller = loader.<LoginDialogController>getController();
            controller.setModel(new LoginModel(data));*/
            setTitle("Profile Settings");
            getDialogPane().setContent(root);
            String theme=Controller.THEME==0? "style/stylesheet_default.css" : "style/stylesheet_dark.css";
            getDialogPane().getStylesheets()
                    .add(this.getClass().getClassLoader().getResource(theme).toExternalForm());
            //initStyle(StageStyle.DECORATED);
            getDialogPane().getButtonTypes()
                    .addAll(new ButtonType("Save", ButtonBar.ButtonData.FINISH),ButtonType.CLOSE);
            getDialogPane().lookupButton(getDialogPane().getButtonTypes().get(0)).getStyleClass().add("highlight-button");
            ((Stage)getDialogPane().getScene().getWindow()).getIcons().addAll(
                    new Image(getClass().getResourceAsStream("/icon.png"))
            );
            setOnCloseRequest(e->close());
            /**
             * Returns commited Options as HashMap */
            setResultConverter(btn->{
                if(btn.getButtonData().equals(ButtonBar.ButtonData.FINISH) ||controller.getProfile().isRemove()){
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
