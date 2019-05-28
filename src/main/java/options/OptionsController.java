package options;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
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

    private HashMap<String,String> options=new HashMap<>();

    @FXML
    public void initialize(){
        choiceLanguage.setItems(FXCollections.observableArrayList(Arrays.asList(TesseractConstants.LANGUAGES)));
        choiceLanguage.getSelectionModel().select(0);
        choiceLanguage.getSelectionModel().selectedIndexProperty().addListener(((observable, oldValue, newValue) ->
                options.put(TesseractConstants.LANGUAGE,TesseractConstants.LANGUAGES[newValue.intValue()])));
        fieldWhitelist.setTextFormatter(new TextFormatter<>(this::whiteListFormatter));
        fieldWhitelist.textProperty().addListener(((observable, oldValue, newValue) -> {
            options.put(TesseractConstants.WHITELIST,newValue);
        }));

    }

    public void setOptions(HashMap<String, String> options) {
        this.options=new HashMap<>(options);
        fieldWhitelist.setText(options.get(TesseractConstants.WHITELIST));
    }

    public HashMap<String, String> getOptions() {
        return options;
    }
    private TextFormatter.Change whiteListFormatter(TextFormatter.Change change) {
        if(change.getControlText().contains(change.getText()))
            change.setText("");
        return change;
    }
}
