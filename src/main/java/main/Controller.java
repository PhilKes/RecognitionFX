package main;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.FileChooserBuilder;
import javafx.stage.Stage;
import logging.Log;
import logging.LogView;
import logging.Logger;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import util.PropertiesService;
import util.RubberBandSelection;
import util.ZoomableScrollPane;

import java.io.File;
import java.net.URISyntaxException;
import java.util.List;

public class Controller {

    private Stage stage;
    @FXML
    private BorderPane rootPane;

    private ImageView imgView;
    @FXML
    private StackPane imgStack;

    @FXML
    private ScrollPane imgScroll;
    private RubberBandSelection rubberBandSelection;
    private File file;

    @FXML
    private Menu recentlyMenu;

    @FXML
    private VBox vboxBottom;
    private Logger logger;

    @FXML
    public void initialize(){

        /** Init StackPane with ZoomableScrollPane with ImageView and RubberbandSelction inside**/
        imgView=new ImageView();
        Group selectionLayer=new Group();
        selectionLayer.setAutoSizeChildren(false);
        selectionLayer.getChildren().add(imgView);

        imgScroll=new ZoomableScrollPane(selectionLayer);
        imgScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        imgScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);

        imgStack.getChildren().add(imgScroll);
        imgStack.setOnMousePressed(event -> {
            if(event.getButton() == MouseButton.MIDDLE)
                event.consume();
        });
        imgStack.setOnKeyPressed(event -> {
                if(event.getCode()==KeyCode.ESCAPE)
                    rubberBandSelection.reset();
            }
        );
        rootPane.setCenter(imgStack);

        rubberBandSelection = new RubberBandSelection(this,selectionLayer);
        /** Show Context menu when left clicked inside StackPane **/
        final ContextMenu contextMenu = new ContextMenu();
        MenuItem cropMenuItem = new MenuItem("Analyze");
        cropMenuItem.setOnAction(e -> {
            Bounds selectionBounds = rubberBandSelection.getBounds();
            System.out.println( "Selected area: " + selectionBounds);
            analyze( selectionBounds);
        });
        contextMenu.getItems().add( cropMenuItem);
        selectionLayer.setOnMousePressed(event -> {
            if (event.isSecondaryButtonDown()) {
                contextMenu.show(selectionLayer, event.getScreenX(), event.getScreenY());
            }
            else if(event.isPrimaryButtonDown()){
                contextMenu.hide();
            }
        });
        loadRecentlyOpenedFiles();
        initLog();
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
    /** Use Tesseract to recognize text in selected Rectangle **/
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
     * Invoked when File->Open Image clicked
     * @param event
     */
    public void onFileOpen(ActionEvent event) {

        FileChooser fileChooser=FileChooserBuilder.create()
                .extensionFilters(new FileChooser.ExtensionFilter("Image files (.jpg,.png,.jpeg)", "*.jpg", "*.png", "*.jpeg"))
                .title("Open Image File")
                .build();
        String lastDirectory=PropertiesService.getLastOpenedDirectory();
        if(lastDirectory!=null)
            fileChooser.setInitialDirectory(new File(lastDirectory));
        File file=fileChooser.showOpenDialog(stage);
        if(file!=null)
            openImage(file.getAbsolutePath());
       /* if (file != null) {
            rubberBandSelection.reset();
            System.out.println(file.getAbsolutePath()+" opened");
            PropertiesService.saveLastOpenedDirectory(file);
            Image img=new Image(file.toURI().toString());

            imgView.setImage(img);
            imgView.setFitWidth(img.getWidth());
            imgView.setFitHeight(img.getHeight());
        }
        else
            System.err.println("ERROR: Could not open Image \""+file.getAbsolutePath()+"\"");*/
    }
    public boolean openImage(String filePath){
        File oldFile=file;
        file=new File(filePath);
        if (file.exists()) {
            rubberBandSelection.reset();
            System.out.println(file.getAbsolutePath()+" opened");
            logger.info(file.getAbsolutePath()+" opened");
            PropertiesService.saveLastOpenedDirectory(file);
            /** Open Image File **/
            Image img=new Image(file.toURI().toString());
            imgView.setImage(img);
            imgView.setFitWidth(img.getWidth());
            imgView.setFitHeight(img.getHeight());
            PropertiesService.saveRecentlyOpened(file.getAbsolutePath());
            loadRecentlyOpenedFiles();
            rubberBandSelection.setImageHeight(img.getHeight());
            rubberBandSelection.setImageWidth(img.getWidth());
            return true;
        }
        else {
            System.err.println("ERROR: Could not open Image \"" + file.getAbsolutePath() + "\"");
            logger.error("Could not open Image \"" + file.getAbsolutePath());
            file=oldFile;
            return false;
        }
    }

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
                recentlyMenu.getItems().add(fileItem);
            }
        );
    }


    public void setStage(Stage primaryStage) {
        stage=primaryStage;
    }


}
