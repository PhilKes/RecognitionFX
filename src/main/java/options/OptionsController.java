package options;

import config.Profile;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;

import java.util.Arrays;
import java.util.HashMap;

/**
 * Controller for options.fxml */
public class OptionsController {
    @FXML
    private ChoiceBox<String> choiceLanguage;
    @FXML
    private TextField fieldWhitelist;
    @FXML
    private Button btnAdd,btnRemove;
    @FXML
    private TextField fieldName;
    private OptionsDialog dialog;

    private Profile profile;
    private boolean newProfile=false;

    @FXML
    public void initialize(){
        choiceLanguage.setItems(FXCollections.observableArrayList(Arrays.asList(TesseractConstants.LANGUAGES)));
        choiceLanguage.getSelectionModel().selectedIndexProperty().addListener(((observable, oldValue, newValue) ->
                profile.getOptions().put(TesseractConstants.LANGUAGE,TesseractConstants.LANGUAGES[newValue.intValue()])));
        //TODO PREFERENCES (A-Z) (a-z) (0-9),...
        fieldWhitelist.setTextFormatter(new TextFormatter<>(this::whiteListFormatter));
        fieldWhitelist.textProperty().addListener(((observable, oldValue, newValue) -> {
            profile.getOptions().put(TesseractConstants.WHITELIST,newValue);
        }));
        fieldName.textProperty().addListener(((observable, oldValue, newValue) -> {
            if(!newValue.isEmpty())
                profile.setName(newValue);
        }));
    }

    public void setOptions(HashMap<String, String> options) {
        //this.options=new HashMap<>(options);
        if(options.isEmpty()) {
            options=TesseractConstants.DEFAULTS.getAll();
            profile.setOptions(options);
        }
        fieldWhitelist.setText(options.get(TesseractConstants.WHITELIST));
        choiceLanguage.getSelectionModel().select(options.get(TesseractConstants.LANGUAGE));
    }

    public HashMap<String, String> getOptions() {
        return profile.getOptions();
    }
    private TextFormatter.Change whiteListFormatter(TextFormatter.Change change) {
        if(change.getControlText().contains(change.getText()))
            change.setText("");
        return change;
    }

    public void setProfile(Profile profile) {
        this.profile=profile;
        setOptions(profile.getOptions());
        fieldName.setText(profile.getName());
    }

    public Profile getProfile() {
        return profile;
    }

    public boolean isNewProfile() {
        return newProfile;
    }

    public void onAddProfile(ActionEvent event) {
        newProfile=true;
        setProfile(new Profile("Profile"));
        btnAdd.setDisable(true);
    }

    public void onRemoveProfile(ActionEvent event) {
      //TODO Remove Profile from settings and close Dialog
        profile.setRemove(true);
        dialog.close();
    }

    public void onSetDefault(ActionEvent event) {
        setOptions(new HashMap<>());
    }

    public void setDialog(OptionsDialog optionsDialog) {
        dialog=optionsDialog;
    }
}
