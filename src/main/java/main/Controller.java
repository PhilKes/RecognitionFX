package main;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TitledPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import logging.Log;
import logging.LogView;
import logging.Logger;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.kordamp.ikonli.javafx.FontIcon;
import util.PropertiesService;
import util.RubberBandSelection;
import util.ZoomableScrollPane;

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import static org.kordamp.ikonli.fontawesome.FontAwesome.FILE_IMAGE_O;

public class Controller {

    /** See window.fxml for definitions*/
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

    private Stage stage;
    private Logger logger;
    private ImageView imgView;
    /** Extended ScrollPane for Image/Selection View */
    private ZoomableScrollPane imgScroll;
    //private RubberBandSelection rubberBandSelection;
    /** List of all created RubberbandSelections*/
    private ArrayList<RubberBandSelection> selections;
    /** Property for storing currently active selection index -> bound to RubberBandSelection's corners visibility(show only if correct selection is active*/
    private SimpleIntegerProperty activeSelection=new SimpleIntegerProperty();
    //private int activeSelection=-1;
    /** Currently opened Imagefile*/
    private File file;
    private Image currImage;

    @FXML
    public void initialize(){

        /** Init StackPane with ZoomableScrollPane with ImageView and RubberbandSelction inside**/
        imgView=new ImageView();
        Group selectionLayer=new Group();
        selectionLayer.setAutoSizeChildren(false);
        selectionLayer.getChildren().add(imgView);
        selections=new ArrayList<>();
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
        FontIcon cropIcon=new FontIcon("fa-magic");
        cropIcon.setText("");
        cropIcon.setIconSize(16);
        cropMenuItem.setDisable(true);
        cropMenuItem.setGraphic(cropIcon);
        cropMenuItem.setOnAction(e -> {
            Bounds selectionBounds = selections.get(activeSelection.get()).getBounds();
            System.out.println( "Selected area: " + selectionBounds);
            analyze( selectionBounds);
        });
        contextMenu.getItems().add( cropMenuItem);
        imgStack.setOnMousePressed(event -> {
            contextMenu.hide();
            if (event.isSecondaryButtonDown()) {
                contextMenu.show(selectionLayer, event.getScreenX(), event.getScreenY());
            }
        });
        /** Spawn new Selection if mouse clicked**/
        imgView.setOnMouseClicked(event -> {
            if(currImage!=null) {
                RubberBandSelection selection=new RubberBandSelection(this, selectionLayer, event.getX(), event.getY()
                        , currImage.getWidth(), currImage.getHeight(),selections.size(), imgScroll.scaleValueProperty());
                selections.add(selection);
                activeSelection.set(selections.size() - 1);
                cropMenuItem.setDisable(false);
                logger.info("Added "+selection.toString());
            }
        });
        propertiesMenu.expandedProperty().addListener((ChangeListener<Boolean>)
            (observable, oldValue, newValue) -> {
                if(!newValue) {
                    /*propertiesMenu.setMinHeight(100.0);
                    propertiesMenu.setPrefHeight(100.0);*/
                    //TODO RESIZING PANE
                    propertiesMenu.setManaged(false);
                    propertiesMenu.setRotate(90);
                }
                else {
                    //TODO RESIZING PANE
                   /* propertiesMenu.setMinHeight(100.0);
                    propertiesMenu.setPrefHeight(100.0);*/
                    propertiesMenu.setRotate(0);
                  /*  propertiesMenu.setPrefHeight(Double.MAX_VALUE);
                    propertiesMenu.setMinHeight(Double.MAX_VALUE);*/
                    propertiesMenu.setManaged(true);
                }
        });
                loadRecentlyOpenedFiles();
        initLog();
    }

    public int getActiveSelection() {
        return activeSelection.get();
    }

    public SimpleIntegerProperty activeSelectionProperty() {
        return activeSelection;
    }

    public void setActiveSelection(int activeSelection) {
        this.activeSelection.set(activeSelection);
    }

    private void initLog() {
        Log log    = new Log();
        logger = new Logger(log, "main");

        LogView logView = new LogView(logger);
        logView.setPrefWidth(400);
        vboxBottom.getChildren().add(logView);
        VBox.setVgrow(logView, Priority.ALWAYS);
        logger.info("Log initialized");
    }

    public void setScrollPanePannable(boolean value){
        imgScroll.setPannable(value);
    }

    /**
     * Uses Tesseract to recognize text in selected Rectangle
     * @param rec Rectangle inside the ImageView
     */
    private void analyze(Bounds rec) {
        //final File imageFile = new File(getClass().getResource("testmessung.jpeg").toURI());
        File imageFile=file;
        logger.info("ANALYZING "+imageFile.getAbsolutePath()+" ...");
        final ITesseract instance = new Tesseract();
        //System.out.println("E:\\TEMP\\tessdata");
        try {

            File tessdata =new File(getClass().getClassLoader().getResource("tessdata").toURI());
            instance.setDatapath(tessdata.getAbsolutePath());
            instance.setTessVariable("tessedit_char_whitelist", "ABCDEFGHIKLMNOPQRSTUVWXYZabcdefghiklmnopqrstuvwxyz.:-0123456789");
        }
        catch(URISyntaxException e) {
            e.printStackTrace();
            logger.error(e.getMessage());
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

    /**
     * Invoked when File Open Image clicked
     */
    public void onFileOpen(ActionEvent event) {

        FileChooser fileChooser=new FileChooser();
        fileChooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("Image files (.jpg,.png,.jpeg)", "*.jpg", "*.png", "*.jpeg"));
        fileChooser .setTitle("Open Image File");
        String lastDirectory=PropertiesService.getLastOpenedDirectory();
        if(lastDirectory!=null)
            fileChooser.setInitialDirectory(new File(lastDirectory));
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
                FontIcon icon=new FontIcon(FILE_IMAGE_O);
                icon.setIconSize(16);
                fileItem.setGraphic(icon);
                recentlyMenu.getItems().add(fileItem);
            }
        );
    }

    public void setStage(Stage primaryStage) {
        stage=primaryStage;
    }

    public void onMenuFileClose(ActionEvent actionEvent) {
        System.exit(0);
    }

    public void onPropertiesOpen(MouseEvent event) {
        System.out.println("OPEN PROPERTIES");
    }

    /** Commit text recognition of all selections*/
    public void onAnalyzeAll(ActionEvent event) {
        for(RubberBandSelection selection : selections)
            analyze(selection.getBounds());
    }

    //TODO DELET ALL SELECTIONS MENUITEM
    /** Delete active selection if Edit->Delete selection or DEL pressed*/
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
    private void removeSelection(int idx){
        RubberBandSelection sel=selections.get(idx);
        sel.reset();
        selections.remove(sel);
        logger.info("Deleted selection "+idx);
    }
}
