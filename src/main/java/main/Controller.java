package main;

import builder.RubberBandSelectionBuilder;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
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
import logging.Log;
import logging.LogView;
import logging.Logger;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import options.OptionsDialog;
import options.TesseractConstants;
import org.kordamp.ikonli.javafx.FontIcon;
import util.PropertiesService;
import util.RubberBandSelection;
import util.ZoomableScrollPane;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.kordamp.ikonli.fontawesome.FontAwesome.MAGIC;
import static org.kordamp.ikonli.fontawesome.FontAwesome.PHOTO;

public class Controller {

    public static Controller CONTROLLER;
    private static int SELECTION_COUNT=1;
    //TODO IN FXML: "RUN" button + Profile selection (SubList in .properties)-> Select Profil in OptionsDialog to change individual, reset to defaults

    /**
     * See window.fxml for definitions*/
    @FXML
    private BorderPane rootPane;
    @FXML
    private StackPane imgStack;
    @FXML
    private Menu recentlyMenu;
    @FXML
    private VBox vboxBottom;
    @FXML
    private TitledPane propertiesMenu;
    @FXML
    private ListView<RubberBandSelection> listSelections;
    @FXML
    private TextField fieldName,fieldX,fieldY,fieldWidth,fieldHeight;

    private Stage stage;
    private static Log log=new Log();
    private static Logger logger;
    static{
        logger=new Logger(log, "main");
    }
    private ImageView imgView;
    /**
     * Extended ScrollPane for Image/Selection View */
    private ZoomableScrollPane imgScroll;
    /**
     * List of all created RubberbandSelections*/
    private ObservableList<RubberBandSelection> selections;
    /**
     * Property for storing currently active selection index - bound to RubberBandSelection's corners visibility(show only if correct selection is active*/
    private SimpleIntegerProperty activeSelection=new SimpleIntegerProperty();
    /**
     * Currently opened Imagefile*/
    private File file;
    private Image currImage;
    /**
     * Options for Tesseract OCR Object*/
    private static HashMap<String,String> tesseractOptions;


    @FXML
    public void initialize(){
        CONTROLLER=this;
        /** Init StackPane with ZoomableScrollPane with ImageView and RubberbandSelction inside**/
        imgView=new ImageView();
        Group selectionLayer=new Group();
        selectionLayer.setAutoSizeChildren(false);
        selectionLayer.getChildren().add(imgView);
        selections=FXCollections.<RubberBandSelection>observableArrayList();
        activeSelection.set(0);
        imgScroll=new ZoomableScrollPane(selectionLayer);
        imgStack.getChildren().add(imgScroll);
        imgStack.setOnMousePressed(event -> {
            if(event.getButton() == MouseButton.MIDDLE)
                event.consume();
        });
        /*    imgStack.setOnKeyPressed(event -> {
                if(event.getCode()==KeyCode.ESCAPE)
                    selections.get(activeSelection.get()).reset();
            }
        );*/
        rootPane.setCenter(imgStack);
        /** Show Context menu when left clicked inside StackPane **/
        final ContextMenu contextMenu = new ContextMenu();
        MenuItem cropMenuItem = new MenuItem("Analyze");
        FontIcon cropIcon=new FontIcon(MAGIC);
        cropIcon.setIconSize(18);
        cropMenuItem.setDisable(true);
        cropMenuItem.setGraphic(cropIcon);
        cropMenuItem.setOnAction(e -> {
            Bounds selectionBounds = selections.get(activeSelection.get()).getBounds();
            System.out.println( "Selected area: " + selectionBounds);
            analyze( selectionBounds);
        });
        contextMenu.getItems().add( cropMenuItem);
        /** Spawn new Selection if mouse clicked**/
        imgView.setOnMouseClicked(event -> {
            if(event.getButton().equals(MouseButton.PRIMARY)) {
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
                    cropMenuItem.setDisable(false);
                    logger.info("Added " + selection.toString());
                }
                contextMenu.hide();
            }
            else if (event.getButton().equals(MouseButton.SECONDARY)) {
                contextMenu.show(selectionLayer, event.getScreenX(), event.getScreenY());
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
        initLog();
        //PropertiesService.saveTesseractProperties(TesseractConstants.DEFAULTS.getAll());
        tesseractOptions=PropertiesService.getAllTesseractProperties();
        if(tesseractOptions.isEmpty()){
            PropertiesService.saveTesseractProperties(TesseractConstants.DEFAULTS.getAll());
            tesseractOptions=PropertiesService.getAllTesseractProperties();
        }
    }

    private void initLog() {

        LogView logView = new LogView(logger);
        logView.setPrefWidth(400);
        vboxBottom.getChildren().add(logView);
        VBox.setVgrow(logView, Priority.ALWAYS);
        logger.info("Log initialized");
    }
    /**
     * Set Handlers for Properties (on ENTER PRESSED) and set TextFormatter for Double validity*/
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
        fieldX.setText(""+rect.getX());
        fieldY.setText(""+rect.getY());
        fieldWidth.setText(""+rect.getWidth());
        fieldHeight.setText(""+rect.getHeight());
    }

    /**
     * Init ListView and listeners for selections in Properties Pane */
    private void initSelectionListView() {
        listSelections.setItems(selections);
        listSelections.setEditable(false);
        listSelections.setCellFactory(param -> new TextFieldListCell<RubberBandSelection>() {
            @Override
            public void updateItem(RubberBandSelection item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getName());
                }
            }
        });
        /** Bind List selection to activeSelection Property (bidirectional with Listeners*/
        activeSelection.addListener((obs,old,newVal)->{
            if(newVal.intValue()!=listSelections.getSelectionModel().getSelectedIndex() && newVal.intValue()>=0)
                listSelections.getSelectionModel().select(newVal.intValue());
        });
        listSelections.getSelectionModel().selectedIndexProperty().addListener((obs,old,newVal)->{
            if(newVal.intValue()!=activeSelection.get()) {
                activeSelection.set(newVal.intValue());
            }
            /** Update Properties Window*/
            if(newVal.intValue()!=-1)
                updatePropertiesWindow(selections.get(newVal.intValue()));

        });
    }

    /**
     * Adds recently opened Files into File Menu
     */
    private void loadRecentlyOpenedFiles() {
        recentlyMenu.getItems().clear();
        List<String> files=PropertiesService.getRecentlyOpenedFiles();
        files.forEach(
                file->{
                    MenuItem fileItem=new MenuItem(file);
                    fileItem.setOnAction(ev->{
                        if(!openImage(file)) {
                            PropertiesService.removeInvalidRecentlyOpened(file);
                            loadRecentlyOpenedFiles();
                        }
                    });
                    FontIcon icon=new FontIcon(PHOTO);
                    icon.setIconSize(16);
                    fileItem.setGraphic(icon);
                    recentlyMenu.getItems().add(fileItem);
                }
        );
    }

    /**
     * Invoked when File - Open Image clicked
     */
    public void onFileOpen(ActionEvent event) {
        FileChooser fileChooser=new FileChooser();
        fileChooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("Image files (.jpg,.png,.jpeg)", "*.jpg", "*.png", "*.jpeg"));
        fileChooser .setTitle("Open Image File");
        String lastDirectory=PropertiesService.getLastOpenedDirectory();
        if(lastDirectory!=null ) {
            File folder=new File(lastDirectory);
            if(folder.exists())
                fileChooser.setInitialDirectory(folder);
            else
                logger.error("onFileOpen: LastDirectory set but not found!");
        }
        File file=fileChooser.showOpenDialog(stage);
        if(file!=null)
            openImage(file.getAbsolutePath());
    }

    public boolean openImage(String filePath){
        File oldFile=file;
        file=new File(filePath);
        if (file.exists()) {
            System.out.println(file.getAbsolutePath()+" opened");
            logger.info(file.getAbsolutePath()+" opened");
            PropertiesService.saveLastOpenedDirectory(file);
            /** Open Image File **/
            Image img=new Image(file.toURI().toString());
            currImage=img;
            imgView.setImage(img);
            imgView.setFitWidth(img.getWidth());
            imgView.setFitHeight(img.getHeight());

            PropertiesService.saveRecentlyOpened(file.getAbsolutePath());
            loadRecentlyOpenedFiles();
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
     * @param rec Rectangle inside the ImageView
     */
    private void analyze(Bounds rec) {
        //final File imageFile = new File(getClass().getResource("testmessung.jpeg").toURI());
        File imageFile=file;
        logger.info("ANALYZING "+imageFile.getAbsolutePath()+" ...");
        ITesseract instance= getTesseract();
        if(instance==null){
            logger.error("Analyze: Could not analyze selection!");
            return;
        }
        //Rectangle rect = new Rectangle(25, 3, 83, 27);
       // java.awt.Rectangle rect = new java.awt.Rectangle(506, 93, 93, 23);
        final String result;
        try {
            result=instance.doOCR(imageFile,new java.awt.Rectangle((int)rec.getMinX(),(int)rec.getMinY(),(int)rec.getWidth(),(int)rec.getHeight()));
            System.out.println(result);
            logger.info("RESULT: "+result);
        }
        catch(TesseractException e) {
            e.printStackTrace();
            logger.error(e.getMessage());
        }

    }

    private ITesseract getTesseract() {
        final ITesseract instance = new Tesseract();
        HashMap<String,String> options=new HashMap<>(tesseractOptions);
        try {
            //File tessdata =new File(getClass().getClassLoader().getResource("tessdata").toURI());
            instance.setDatapath(options.get(TesseractConstants.TESSDATA));
            options.remove(TesseractConstants.TESSDATA);
            instance.setLanguage(options.get(TesseractConstants.LANGUAGE));
            for(Map.Entry<String,String> option: options.entrySet())
                instance.setTessVariable(option.getKey(), option.getValue());
        }
        catch(Exception e) {
            e.printStackTrace();
            logger.error(e.getMessage());
            return null;
        }
        return instance;
    }

    /**
     * Commit text recognition of all selections*/
    public void onAnalyzeAll(ActionEvent event) {
        for(RubberBandSelection selection : selections)
            analyze(selection.getBounds());
    }
    /**
     * Show Options Dialog and update global settings if closed */
    public void onAnalyzeOptions(ActionEvent event) {
        OptionsDialog dialog=new OptionsDialog(tesseractOptions);
        /** Show OptionsDialog and save changed settings afterr close */
        Optional<HashMap<String, String>> newOptions=dialog.showAndWait();
        if(newOptions.isPresent()) {
            for(Map.Entry<String, String> option : newOptions.get().entrySet())
                tesseractOptions.put(option.getKey(),option.getValue());
            PropertiesService.saveTesseractProperties(tesseractOptions);
        }
    }

    /**
     * Delete active selection if Edit - Delete selection or DEL pressed*/
    public void onDeleteSelection(ActionEvent event) {
        if(activeSelection.get()==-1){
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
        if(selections.isEmpty()){
            logger.warn("No selections to be deleted");
            return;
        }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation ");
        alert.initStyle(StageStyle.UTILITY);
        alert.setHeaderText("Delete All");
        alert.setContentText("Are you sure you want to delete all selections?");
        alert.getButtonTypes().setAll(ButtonType.YES,ButtonType.NO);
        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.YES){
            while(!selections.isEmpty())
                removeSelection(0);
        } else {
           alert.close();
        }

    }
    private void removeSelection(int idx){
        RubberBandSelection sel=selections.get(idx);
        sel.reset();
        selections.remove(sel);
        logger.info("Deleted selection "+sel.getName());
    }

    /**
     * Update selection when Properties are changed */
    private void onPropertyFieldChanged(KeyEvent keyEvent) {
        if(keyEvent.getCode().equals(KeyCode.ENTER) && activeSelection.get()!=-1) {
            TextField field=(TextField)keyEvent.getSource();
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
            catch(NumberFormatException ex){
                logger.error("onChangeProperty: Invalid value for "+propertyID);
            }
            logger.info("Property "+propertyID+" changed to: "+field.getText());
            refreshSelectedIteminList();
        }
    }
    public void onMenuFileClose(ActionEvent actionEvent) {
        System.exit(0);
    }

    public void refreshSelectedIteminList() {
        listSelections.refresh();
        updatePropertiesWindow(selections.get(activeSelection.get()));
    }

    public SimpleIntegerProperty activeSelectionProperty() {
        return activeSelection;
    }
    public void setActiveSelection(int activeSelection) {
        this.activeSelection.set(activeSelection);
    }
    public void setScrollPanePannable(boolean value){
        imgScroll.setPannable(value);
    }

    private TextFormatter.Change textDoubleFormat(TextFormatter.Change change) {
        if(!change.getControlNewText().matches("[\\d\\.*]+"))
            change.setText("");
        return change;
    }

    public void setStage(Stage primaryStage) {
        stage=primaryStage;
    }
    public static Logger getLogger(){
        return logger;
    }
}
