package main;

import config.*;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Pair;
import logging.Log;
import logging.LogView;
import logging.Logger;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import options.OptionsDialog;
import options.TesseractConstants;
import org.kordamp.ikonli.javafx.FontIcon;
import util.RubberBandSelection;
import util.RubberBandSelectionBuilder;
import util.ZoomableScrollPane;

import java.io.File;
import java.nio.file.Paths;
import java.util.*;

import static org.kordamp.ikonli.fontawesome.FontAwesome.*;

public class Controller {

    public static Controller CONTROLLER;
    private static int SELECTION_COUNT=1;
    //TODO Package all properties/.ini loadings onto another Thread(TASK)
    /**
     * See window.fxml for definitions
     */
    @FXML
    private BorderPane rootPane;
    @FXML
    private StackPane imgStack;
    @FXML
    private Menu recentlyMenu;
    @FXML
    private Menu recentProjectsMenu;
    @FXML
    private VBox vboxBottom;
    @FXML
    private TitledPane propertiesMenu;
    @FXML
    private ListView<RubberBandSelection> listSelections;
    @FXML
    private TextField fieldName, fieldX, fieldY, fieldWidth, fieldHeight;
    @FXML
    private ChoiceBox<String> choiceProfile;
    @FXML
    private ProgressBar progressTask;
    @FXML
    private Button btnRun;
    @FXML
    private MenuItem menuAnalyzeAll;

    public static int THEME=0;

    private Stage stage;
    private static Log log=new Log();
    private static Logger logger;

    static {
        logger=new Logger(log, "main");
    }

    private ImageView imgView;
    /**
     * Extended ScrollPane for Image/Selection View
     */
    private ZoomableScrollPane imgScroll;
    /**
     * List of all created RubberbandSelections
     */
    private ObservableList<RubberBandSelection> selections;
    /**
     * Property for storing currently active selection index - bound to RubberBandSelection's corners visibility(show only if correct selection is active
     */
    private SimpleIntegerProperty activeSelection=new SimpleIntegerProperty();
    /**
     * Currently opened Imagefile
     */
    private File file;
    private Image currImage;
    /**
     * Options for Tesseract OCR Object
     */
    private static Profile currProfile;
    private LogView logView;

    private Project curProject;

    /**
     * Stores if Analysis is running - bound to analysis invokers (RunButton,...)
     */
    private BooleanProperty running=new SimpleBooleanProperty(true);
    private Group selectionLayer;
    //TODO CHANGE STACK-> Bind to Save MenuItem disable property
    /*private static HashMap<String,String> tesseractOptions;
    private String currProfile="default";*/

    @FXML
    public void initialize() {
        CONTROLLER=this;
        initLog();
        loadTheme(IniProperties.Program.getTheme());
        /** Init StackPane with ZoomableScrollPane with ImageView and RubberbandSelction inside**/
        imgView=new ImageView();
        selectionLayer=new Group();
        selectionLayer.setAutoSizeChildren(false);
        selectionLayer.getChildren().add(imgView);
        selections=FXCollections.observableArrayList();
        activeSelection.set(0);
        imgScroll=new ZoomableScrollPane(selectionLayer);
        imgStack.getChildren().add(imgScroll);
        imgStack.setOnMousePressed(event -> {
            if(event.getButton()==MouseButton.MIDDLE) {
                event.consume();
            }
        });
        /*    imgStack.setOnKeyPressed(event -> {
                if(event.getCode()==KeyCode.ESCAPE)
                    selections.get(activeSelection.get()).reset();
            }
        );*/
        rootPane.setCenter(imgStack);
        /** Show Context menu when left clicked inside StackPane **/
        final ContextMenu contextMenu=new ContextMenu();
        MenuItem cropMenuItem=new MenuItem("Analyze");
        FontIcon cropIcon=new FontIcon(MAGIC);
        cropIcon.setIconSize(18);
        cropMenuItem.setGraphic(cropIcon);
        cropMenuItem.setOnAction(e -> {
            RubberBandSelection selected=selections.get(activeSelection.get());
            System.out.println("Selected area: " + selected);
            analyze(Arrays.asList(selected));
        });
        contextMenu.getItems().add(cropMenuItem);
        /**
         * Bind execute Analysis invokers disabled to running property*/
        running.set(false);
        cropMenuItem.disableProperty().bind(running);
        btnRun.disableProperty().bind(running);
        menuAnalyzeAll.disableProperty().bind(running);
        /**
         * Spawn new Selection if mouse clicked**/
        imgView.setOnMouseClicked(event -> {
            if(event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount()==2) {
                if(currImage!=null && !contextMenu.isShowing()) {
                    RubberBandSelection selection=new RubberBandSelectionBuilder()
                            .setRootGroup(selectionLayer)
                            .setX(event.getX()).setY(event.getY())
                            .setImgWidth(currImage.getWidth()).setImgHeight(currImage.getHeight())
                            .setIdx(selections.size())
                            .setScaleProperty(imgScroll.scaleValueProperty())
                            .setName("Selection " + SELECTION_COUNT++)
                            .createRubberBandSelection();
                    selections.add(selection);
                    activeSelection.set(selections.size() - 1);
                    //cropMenuItem.setDisable(false);
                    logger.info("Added " + selection.toString());
                }
                contextMenu.hide();
            }
            else if(event.getButton().equals(MouseButton.SECONDARY)) {
                contextMenu.show(selectionLayer, event.getScreenX(), event.getScreenY());
            }
            else {
                //event.consume();
            }
        });
        propertiesMenu.expandedProperty().addListener((observable, oldValue, newValue) -> {
            if(!newValue) {
                /*propertiesMenu.setMinHeight(100.0);
                propertiesMenu.setPrefHeight(100.0);*/
                //TODO RESIZING PANE
                propertiesMenu.setManaged(false);
                propertiesMenu.setRotate(90);
            }
            else {
                //TODO RESIZING PANE
                propertiesMenu.setRotate(0);
                propertiesMenu.setManaged(true);
            }
        });
        initSelectionListView();
        initPropertiesWindow();
        loadRecentlyOpenedFiles();

        //IniProperties.saveTesseractProperties(TesseractConstants.DEFAULTS.getAll());
        /**
         * Load Properties from file */
        currProfile=new Profile("default", IniProperties.Tesseract.getDefaultSettings());
        choiceProfile.getSelectionModel().selectedItemProperty().addListener(((observable, oldValue, newValue) -> {
            if(oldValue!=newValue && newValue!=null) {
                changeProfile(newValue);
            }
        }));
        loadProfiles();

        //choiceProfile.getSelectionModel().select("default");
    }

    /**
     * Loads .CSS Stylesheets for theme
     */
    private void loadTheme(int theme) {
        THEME=theme;
        logView.getStylesheets().clear();
        rootPane.getStylesheets().clear();
        if(THEME==0) {
            rootPane.getStylesheets().add("style/stylesheet_default.css");
            logView.getStylesheets().add(
                    this.getClass().getClassLoader().getResource("style/log-view_default.css").toExternalForm()
            );
        }
        else {
            rootPane.getStylesheets().add("style/stylesheet_dark.css");
            logView.getStylesheets().add(
                    this.getClass().getClassLoader().getResource("style/log-view_dark.css").toExternalForm()
            );
        }

    }

    private void loadProfiles() {
        choiceProfile.setItems(FXCollections.observableArrayList(IniProperties.Tesseract.getProfiles()));
        choiceProfile.getSelectionModel().select(currProfile.getName());
    }

    private void changeProfile(String profile) {
        currProfile=new Profile(profile, IniProperties.Tesseract.getProfile(profile));
        logger.info(profile + " profile loaded");
    }

    private void initLog() {
        logView=new LogView(logger);
        logView.setPrefWidth(400);
        vboxBottom.getChildren().add(0, logView);
        VBox.setVgrow(logView, Priority.ALWAYS);
        logger.info("Log initialized");
    }

    /**
     * Set Handlers for Properties (on ENTER PRESSED) and set TextFormatter for Double validity
     */
    private void initPropertiesWindow() {
        fieldName.setOnKeyPressed(this::onPropertyFieldChanged);
        fieldX.setOnKeyPressed(this::onPropertyFieldChanged);
        fieldX.setTextFormatter(new TextFormatter<>(this::textDoubleFormat));
        fieldY.setOnKeyPressed(this::onPropertyFieldChanged);
        fieldY.setTextFormatter(new TextFormatter<>(this::textDoubleFormat));
        fieldWidth.setOnKeyPressed(this::onPropertyFieldChanged);
        fieldWidth.setTextFormatter(new TextFormatter<>(this::textDoubleFormat));
        fieldHeight.setOnKeyPressed(this::onPropertyFieldChanged);
        fieldHeight.setTextFormatter(new TextFormatter<>(this::textDoubleFormat));
    }

    public void updatePropertiesWindow(RubberBandSelection newSelection) {
        fieldName.setText(newSelection.getName());
        Rectangle rect=newSelection.getRect();
        fieldX.setText("" + rect.getX());
        fieldY.setText("" + rect.getY());
        fieldWidth.setText("" + rect.getWidth());
        fieldHeight.setText("" + rect.getHeight());
    }

    /**
     * Init ListView and listeners for selections in Properties Pane
     */
    private void initSelectionListView() {
        listSelections.setItems(selections);
        listSelections.setEditable(false);
        listSelections.setCellFactory(param -> new TextFieldListCell<RubberBandSelection>() {
            @Override
            public void updateItem(RubberBandSelection item, boolean empty) {
                super.updateItem(item, empty);
                if(empty || item==null) {
                    setText(null);
                }
                else {
                    setText(item.getName());
                }
            }
        });
        /** Bind List selection to activeSelection Property (bidirectional with Listeners*/
        activeSelection.addListener((obs, old, newVal) -> {
            if(newVal.intValue()!=listSelections.getSelectionModel().getSelectedIndex() && newVal.intValue() >= 0) {
                listSelections.getSelectionModel().select(newVal.intValue());
            }
        });
        listSelections.getSelectionModel().selectedIndexProperty().addListener((obs, old, newVal) -> {
            if(newVal.intValue()!=activeSelection.get()) {
                activeSelection.set(newVal.intValue());
            }
            /** Update Properties Window*/
            if(newVal.intValue()!=-1) {
                updatePropertiesWindow(selections.get(newVal.intValue()));
            }

        });
    }

    /**
     * Adds recently opened Files into File Menu
     */
    private void loadRecentlyOpenedFiles() {
        recentlyMenu.setDisable(true);
        recentlyMenu.getItems().clear();
        List<String> files=IniProperties.Program.getRecentlyOpenedImages();
        recentlyMenu.setDisable(files.isEmpty());
        files.forEach(
                file -> {
                    MenuItem fileItem=new MenuItem(file);
                    fileItem.setOnAction(ev -> {
                        if(!openImage(file)) {
                            IniProperties.Program.removeInvalidRecentlyOpened(file);
                            loadRecentlyOpenedFiles();
                        }
                    });
                    FontIcon icon=new FontIcon(PHOTO);
                    icon.setIconSize(16);
                    icon.getStyleClass().add("font-icon");
                    fileItem.setGraphic(icon);
                    recentlyMenu.getItems().add(fileItem);
                }
        );
        recentProjectsMenu.setDisable(true);
        recentProjectsMenu.getItems().clear();
        files=IniProperties.Program.getRecentProjects();
        recentProjectsMenu.setDisable(files.isEmpty());
        files.forEach(
                file -> {
                    MenuItem fileItem=new MenuItem(file);
                    fileItem.setOnAction(ev -> {
                        if(!openProject(new File(file))) {
                            IniProperties.Program.removeInvalidRecentProject(file);
                            loadRecentlyOpenedFiles();
                        }
                    });
                    FontIcon icon=new FontIcon(LIST_ALT);
                    icon.setIconSize(16);
                    icon.getStyleClass().add("font-icon");
                    fileItem.setGraphic(icon);
                    recentProjectsMenu.getItems().add(fileItem);
                }
        );
    }

    /**
     * Invoked when File - Open Image clicked
     */
    public void onImageOpen(ActionEvent event) {
        FileChooser fileChooser=new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image files (.jpg,.png,.jpeg)", "*.jpg", "*.png", "*.jpeg"));
        fileChooser.setTitle("Open Image File");
        String lastDirectory=IniProperties.Program.getLastOpenedDirectory();
        if(lastDirectory!=null) {
            File folder=new File(lastDirectory);
            if(folder.exists()) {
                fileChooser.setInitialDirectory(folder);
            }
            else {
                logger.error("onImageOpen: LastDirectory set but not found!");
            }
        }
        File file=fileChooser.showOpenDialog(stage);
        if(file!=null) {
            if(openImage(file.getAbsolutePath())) {
                curProject=new Project("Unnamed");
                curProject.setImagePath(file.getAbsolutePath());
                IniProperties.Program.saveLastOpenedDirectory(file);
                IniProperties.Program.saveRecentImage(file.getAbsolutePath());
                loadRecentlyOpenedFiles();
            }
        }
    }

    public void onOpenProject(ActionEvent event) {
        FileChooser fileChooser=new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Project files (.rfx)", "*.rfx"));
        fileChooser.setTitle("Open Project File");
        String lastDirectory=IniProperties.Program.getLastOpenedProjectDirectory();
        if(lastDirectory==null) {
            String currentPath=Paths.get(".").toAbsolutePath().normalize().toString();
            lastDirectory=currentPath + "\\projects";
            System.out.println(lastDirectory);
        }
        File folder=new File(lastDirectory);
        if(folder.exists()) {
            fileChooser.setInitialDirectory(folder);
        }
        else {
            logger.error("onProjectOpen: LastDirectory set but not found!");
        }

        File file=fileChooser.showOpenDialog(stage);
        if(file!=null) {
            if(openProject(file)) {
                IniProperties.Program.saveLastOpenedProjectDirectory(file);
                IniProperties.Program.saveRecentProject(file.getAbsolutePath());
                loadRecentlyOpenedFiles();
            }
        }
    }

    /**
     * Parses Selections+Image from .rfx file if possible
     *
     * @return returns if Project was successfully loaded
     */
    public boolean openProject(File file) {
        Project oldProject=curProject;
        if(file.exists()) {
            System.out.println(file.getAbsolutePath() + " opened");
            logger.info(file.getAbsolutePath() + " opened");
            /*IniProperties.Program.saveLastOpenedDirectory(file);*/
            Project project=XMLService.parseProject(file);
            if(project!=null) {
                curProject=project;
            }
            /** Project could not be parsed from XML*/
            else {
                logger.error("Could not open project file: " + file.getAbsolutePath());
                return false;
            }
            /*IniProperties.Program.saveRecentImage(file.getAbsolutePath());*/
            if(openImage(project.getImagePath())) {
                for(RectangleXML rect : project.getSelections()) {
                    RubberBandSelection selection=new RubberBandSelectionBuilder()
                            .setRootGroup(selectionLayer)
                            .setX(rect.getX()).setY(rect.getY())
                            .setImgWidth(currImage.getWidth()).setImgHeight(currImage.getHeight())
                            .setIdx(selections.size())
                            .setScaleProperty(imgScroll.scaleValueProperty())
                            .setName(rect.getName())
                            .createRubberBandSelection();
                    selection.setWidth(rect.getWidth());
                    selection.setHeight(rect.getHeight());
                    selections.add(selection);

                    //TODO
                }
            }
            /** Image file was not found*/
            else {
                logger.error("Could not open image: " + project.getImagePath());
                curProject=oldProject;
                return false;
            }
        }
        else {
            logger.error("Project file not found:" + file.getAbsolutePath());
            return false;
        }
        return true;

    }

    /**
     * Open Image from path if possible
     *
     * @return Returns if Image could be loaded or not
     */
    public boolean openImage(String filePath) {
        File oldFile=file;
        file=new File(filePath);
        if(file.exists()) {
            System.out.println(file.getAbsolutePath() + " opened");
            logger.info(file.getAbsolutePath() + " opened");
            /*IniProperties.Program.saveLastOpenedDirectory(file);*/
            /** Open Image File **/
            Image img=new Image(file.toURI().toString());
            currImage=img;
            imgView.setImage(img);
            imgView.setFitWidth(img.getWidth());
            imgView.setFitHeight(img.getHeight());

            /*IniProperties.Program.saveRecentImage(file.getAbsolutePath());*/

            for(int i=0; i<selections.size(); i++) {
                selections.get(i).reset();
            }
            selections.clear();
            activeSelection.set(-1);
            imgScroll.resetScale();
            return true;
        }
        else {
            System.err.println("ERROR: Could not open Image \"" + file.getAbsolutePath() + "\"");
            logger.error("Could not open Image \"" + file.getAbsolutePath());
            file=oldFile;
            return false;
        }
    }

    /**
     * Uses Tesseract to recognize text in selected Rectangle
     *
     * @param selections RubberbandSelections inside the ImageView
     */
    private void analyze(List<RubberBandSelection> selections) {
        running.set(true);
        Task<ArrayList<String>> ocrTask=new Task<ArrayList<String>>() {
            @Override
            protected ArrayList<String> call() {
                ArrayList<String> results=new ArrayList<>();
                for(int i=0; i<selections.size(); i++) {

                    RubberBandSelection selection=selections.get(i);
                    logger.info("Analyzing \"" + selection.getName() + "\" ...");
                    Rectangle rec=selection.getRect();
                    File imageFile=file;
                    ITesseract instance=getTesseract();
                    if(instance==null) {
                        logger.error("Analyze: Could not analyze selection!");
                        results.add(null);
                    }
                    final String result;
                    try {
                        /*BufferedImage imgSelection=SwingFXUtils.fromFXImage(currImage, null).getSubimage(
                                (int) rec.getX(), (int) rec.getY(), (int) rec.getWidth(), (int) rec.getHeight());*/
                        //TODO Save BufferedImage as temporary file to use for createDocuments()
                        result=instance.doOCR(imageFile, new java.awt.Rectangle((int) rec.getX(), (int) rec.getY(), (int) rec.getWidth(), (int) rec.getHeight()));
                        logger.info("RESULT for \""+selection.getName()+"\" :\n-----------------------\n"+result+"\n-----------------------");
                        results.add(result);
                    }
                    catch(Exception e) {
                        e.printStackTrace();
                        logger.error("Analyzing \"" + selection.getName() + "\": " + e.getMessage());
                        results.add(null);
                    }
                    updateProgress(i, selections.size() - 1);
                }
                return results;
            }
        };
        /**
         * Reset progressBar, Log OCR results*/
        ocrTask.setOnSucceeded(event -> {
           /* for(int i=0; i<selections.size(); i++) {
                logger.info("RESULT \""+selections.get(i).getName()+"\": "+ocrTask.getValue().get(i));
            }*/
            progressTask.setDisable(true);
            progressTask.progressProperty().unbind();
            running.set(false);
        });
        progressTask.progressProperty().bind(ocrTask.progressProperty());
        progressTask.setDisable(false);
        new Thread(ocrTask).start();

    }
    private void convertToPDF(File file){
        ITesseract instance=getTesseract();
        List<ITesseract.RenderedFormat> list=new ArrayList<ITesseract.RenderedFormat>();
        list.add(ITesseract.RenderedFormat.PDF);
        FileChooser fileChooser=new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF files (.pdf)", "*.pdf"));
        fileChooser.setTitle("Save Image as searchable PDF");

        String lastDirectory=IniProperties.Program.getLastOpenedProjectDirectory();
        if(lastDirectory==null) {
            String currentPath=Paths.get(".").toAbsolutePath().normalize().toString();
            lastDirectory=currentPath + "\\output";
            System.out.println(lastDirectory);
        }
        File folder=new File(lastDirectory);
        if(folder.exists()) {
            fileChooser.setInitialDirectory(folder);
        }
        else {
            logger.error("onProjectOpen: LastDirectory set but not found!");
        }
        File outFile=fileChooser.showSaveDialog(stage);

        String outPath=outFile.getAbsolutePath();
        while(outPath.endsWith(".pdf"))
            outPath=outPath.substring(0,outFile.getAbsolutePath().length()-4);
        String finalOutPath=outPath;
        Task convertTask=new Task() {
            @Override
            protected Void call() {
                try {
                    instance.createDocuments(file.getAbsolutePath(), finalOutPath, list);
                    logger.info("Saved PDF as "+ finalOutPath +".pdf");
                }
                catch(TesseractException e) {
                    e.printStackTrace();
                }
                return null;
            }
        };
        new Thread(convertTask).start();
        //String outputPath=file.getAbsolutePath().replaceFirst("\\..?.?.?","");

    }
    /**
     * Returns an instace of ITesseract with options set from currProfile
     */
    private ITesseract getTesseract() {
        final ITesseract instance=new Tesseract();
        HashMap<String, String> options=new HashMap<>(currProfile.getOptions());
        try {
            //File tessdata =new File(getClass().getClassLoader().getResource("tessdata").toURI());
            instance.setDatapath(options.get(TesseractConstants.TESSDATA));
            options.remove(TesseractConstants.TESSDATA);
            instance.setLanguage(options.get(TesseractConstants.LANGUAGE));
            for(Map.Entry<String, String> option : options.entrySet())
                instance.setTessVariable(option.getKey(), option.getValue());
        }
        catch(Exception e) {
            e.printStackTrace();
            logger.error(e.getMessage());
            return null;
        }
        return instance;
    }

    //TODO ANALYZE ENTIRE IMAGE WITHOUT SELECTIONS

    /**
     * Commit text recognition of all selections
     */
    public void onAnalyzeAll(ActionEvent event) {
        if(file==null) {
            logger.warn("ANALYZING: No Image opened...");
            return;
        }
        if(selections.isEmpty()) {
            logger.warn("ANALYZING: No selections found...");
            return;
        }
        logger.info("ANALYZING \"" + file.getAbsolutePath() + "\" ...");
        analyze(selections);
    }

    /**
     * Show Options Dialog and update global settings if closed
     */
    public void onAnalyzeOptions(ActionEvent event) {
        OptionsDialog dialog=new OptionsDialog(currProfile);
        /** Show OptionsDialog and save changed settings afterr close */
        Optional<Pair<Boolean, Profile>> newOptions=dialog.showAndWait();
        if(newOptions.isPresent()) {
            Profile updatedProfile=newOptions.get().getValue();
            /**
             * Has old Profile been renamed? (not new but other name) */
            if(!newOptions.get().getKey() && !currProfile.getName().equals(updatedProfile.getName())) {
                IniProperties.Tesseract.renameProfile(currProfile.getName(), updatedProfile.getName(), updatedProfile.getOptions());
            }
            /**
             * Save Profile/add new*/
            else {
                IniProperties.Tesseract.saveProfile(updatedProfile.getName(), updatedProfile.getOptions());
            }
            currProfile=updatedProfile;
            loadProfiles();
        }
    }

    public void onRunButton(ActionEvent event) {
        onAnalyzeAll(event);
    }

    /**
     * Delete active selection from list
     * Invoked by "Delete" MenuItem
     */
    public void onDeleteSelection(ActionEvent event) {
        if(activeSelection.get()==-1) {
            logger.warn("Delete: No selection selected");
            return;
        }
        removeSelection(activeSelection.get());
        activeSelection.set(-1);
        /** Update selections indices if a selection has been removed from the List*/
        for(int i=0; i<selections.size(); i++)
            selections.get(i).setIdx(i);
    }

    public void onDeleteAll(ActionEvent event) {
        if(selections.isEmpty()) {
            logger.warn("No selections to be deleted");
            return;
        }
        Alert alert=new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation ");
        alert.initStyle(StageStyle.UTILITY);
        alert.setHeaderText("Delete All");
        alert.setContentText("Are you sure you want to delete all selections?");
        alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
        Optional<ButtonType> result=alert.showAndWait();
        if(result.get()==ButtonType.YES) {
            while(!selections.isEmpty()) {
                removeSelection(0);
            }
        }
        else {
            alert.close();
        }

    }

    private void removeSelection(int idx) {
        RubberBandSelection sel=selections.get(idx);
        sel.reset();
        selections.remove(sel);
        logger.info("Deleted selection " + sel.getName());
    }

    /**
     * Update selection when Properties are changed
     */
    private void onPropertyFieldChanged(KeyEvent keyEvent) {
        if(keyEvent.getCode().equals(KeyCode.ENTER) && activeSelection.get()!=-1) {
            TextField field=(TextField) keyEvent.getSource();
            String propertyID=field.getId().substring(5);
            RubberBandSelection selection=listSelections.getSelectionModel().getSelectedItem();
            try {
                switch(propertyID) {
                    case "Name":
                        selection.setName(field.getText());
                        break;
                    case "X":
                        selection.setX(Double.parseDouble(field.getText()));
                        break;
                    case "Y":
                        selection.setY(Double.parseDouble(field.getText()));
                        break;
                    case "Width":
                        selection.setWidth(Double.parseDouble(field.getText()));
                        break;
                    case "Height":
                        selection.setHeight(Double.parseDouble(field.getText()));
                        break;
                    default:
                        logger.warn("onChangeProperty: Property field not found");
                        return;
                }
            }
            catch(NumberFormatException ex) {
                logger.error("onChangeProperty: Invalid value for " + propertyID);
            }
            refreshSelectedIteminList();
            logger.info("Property " + propertyID + " changed to: " + field.getText());

        }
    }

    public void onMenuFileClose(ActionEvent actionEvent) {
        System.exit(0);
    }

    public void refreshSelectedIteminList() {
        listSelections.refresh();
        updatePropertiesWindow(selections.get(activeSelection.get()));
    }

    /**
     * Quick saves .rfx project if a project is opened
     * Invoked by "Save" MenuItem
     */
    public void onSave(ActionEvent event) {
        if(curProject==null) {
            return;
        }
        curProject.setImagePath(file.getAbsolutePath());
        curProject.getSelections().clear();
        for(RubberBandSelection sel : selections) {
            Rectangle rec=sel.getRect();
            curProject.getSelections().add(new RectangleXML(sel.getName(), rec.getX(), rec.getY(), rec.getWidth(), rec.getHeight()));
        }
        if(curProject.getPath()==null) {
            onSaveAs(event);
            return;
        }
        else {
            File file=new File(curProject.getPath());
            if(file.exists()) {
                XMLService.saveProject(curProject, file.getAbsolutePath());
            }
        }
    }

    /**
     * Saves project as .rfx with FileChooser
     * Invoked by "Save as.." MenuItem
     */
    public void onSaveAs(ActionEvent event) {
        if(curProject==null) {
            return;
        }
        curProject.setImagePath(file.getAbsolutePath());
        curProject.getSelections().clear();
        for(RubberBandSelection sel : selections) {
            Rectangle rec=sel.getRect();
            curProject.getSelections().add(new RectangleXML(sel.getName(), rec.getX(), rec.getY(), rec.getWidth(), rec.getHeight()));
        }
        FileChooser fileChooser=new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Project files (.rfx)", "*.rfx"));
        fileChooser.setTitle("Save Project File");
        String lastDirectory=IniProperties.Program.getLastOpenedProjectDirectory();
        if(lastDirectory==null) {
            String currentPath=Paths.get(".").toAbsolutePath().normalize().toString();
            lastDirectory=currentPath + "\\projects";
            System.out.println(lastDirectory);
        }
        File folder=new File(lastDirectory);
        if(folder.exists()) {
            fileChooser.setInitialDirectory(folder);
        }
        else {
            logger.error("onProjectOpen: LastDirectory set but not found!");
        }
        File file=fileChooser.showSaveDialog(stage);
        if(file!=null) {
            XMLService.saveProject(curProject, file.getAbsolutePath());
            curProject.setPath(file.getAbsolutePath());
        }
    }

    /**
     * Triggered by Options - Switch Theme
     */
    public void switchTheme(ActionEvent event) {
        loadTheme(THEME==0 ? 1 : 0);
        IniProperties.Program.saveTheme(THEME);
    }

    public SimpleIntegerProperty activeSelectionProperty() {
        return activeSelection;
    }

    public void setActiveSelection(int activeSelection) {
        this.activeSelection.set(activeSelection);
    }

    public void setScrollPanePannable(boolean value) {
        imgScroll.setPannable(value);
    }

    private TextFormatter.Change textDoubleFormat(TextFormatter.Change change) {
        if(!change.getControlNewText().matches("[\\d]+\\.?\\d?\\d?")) {
            change.setText("");
        }
        return change;
    }

    public void setStage(Stage primaryStage) {
        stage=primaryStage;
    }

    public static Logger getLogger() {
        return logger;
    }

    public void onConvertToPDF(ActionEvent event) {
        if(file!=null)
            convertToPDF(file);
        else
            logger.warn("Convert to PDF: no Image file opened!");
    }
}
