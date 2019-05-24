import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineCap;
import javafx.stage.FileChooser;
import javafx.stage.FileChooserBuilder;
import javafx.stage.Stage;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import util.ZoomableScrollPane;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

public class Controller {

    private Stage stage;
    @FXML
    private BorderPane rootPane;

    private ImageView imgView;
    @FXML
    private StackPane imgStack;

    private ScrollPane imgScroll;
    private RubberBandSelection rubberBandSelection;
    private File file;

    @FXML
    public void initialize(){

        imgView=new ImageView();
        //imgView.fitWidthProperty().bind(stage.widthProperty());
        Group selectionLayer=new Group();
        selectionLayer.setAutoSizeChildren(false);
        selectionLayer.getChildren().add(imgView);
        imgScroll=new ZoomableScrollPane(selectionLayer);
        imgScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        imgScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        imgStack.getChildren().add(imgScroll);

        rootPane.setCenter(imgScroll);

        rubberBandSelection = new RubberBandSelection(selectionLayer);
        // set context menu on image layer
        selectionLayer.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (event.isSecondaryButtonDown()) {
                    System.out.println("Left click");
                }
            }
        });
        // create context menu and menu items
        final ContextMenu contextMenu = new ContextMenu();

        MenuItem cropMenuItem = new MenuItem("Analyze");
        cropMenuItem.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {

                // get bounds for image crop
                Bounds selectionBounds = rubberBandSelection.getBounds();

                // show bounds info
                System.out.println( "Selected area: " + selectionBounds);

                // crop the image
               recognize( selectionBounds);

            }
        });
        contextMenu.getItems().add( cropMenuItem);
        // set context menu on image layer
        selectionLayer.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (event.isSecondaryButtonDown()) {
                    contextMenu.show(selectionLayer, event.getScreenX(), event.getScreenY());
                }
            }
        });
        //imgScroll.setContent(imgView);
    }

    private void recognize(Bounds selectionBounds) {
        //final File imageFile = new File(getClass().getResource("testmessung.jpeg").toURI());
        File imageFile=file;
        final ITesseract instance = new Tesseract();
        //System.out.println("E:\\TEMP\\tessdata");
        instance.setDatapath("F:\\IntellijProjects\\NumberRecognition\\Tess4J\\tessdata");
        instance.setTessVariable("tessedit_char_whitelist", "ABCDEFGHIKLMNOPQRSTUVWXYZabcdefghiklmnopqrstuvwxyz.:-0123456789");

        //Rectangle rect = new Rectangle(25, 3, 83, 27);
       // java.awt.Rectangle rect = new java.awt.Rectangle(506, 93, 93, 23);
        Bounds rec=rubberBandSelection.getBounds();
        final String result;
        try {
            result=instance.doOCR(imageFile,new java.awt.Rectangle((int)rec.getMinX(),(int)rec.getMinY(),(int)rec.getWidth(),(int)rec.getHeight()));
            System.out.println(result);
        }
        catch(TesseractException e) {
            e.printStackTrace();
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
        file=fileChooser.showOpenDialog(stage);
        if (file != null) {
            System.out.println(file.getAbsolutePath()+" opened");
            Image img=new Image(file.toURI().toString());

            imgView.setImage(img);
            imgView.setFitWidth(img.getWidth());
            imgView.setFitHeight(img.getHeight());
        }
        else
            System.err.println("ERROR: Could not open Image \""+file.getAbsolutePath()+"\"");
    }

    public void setStage(Stage primaryStage) {
        stage=primaryStage;
    }

    /**
     * Drag rectangle with mouse cursor in order to get selection bounds
     */
    public static class RubberBandSelection {

        final RubberBandSelection.DragContext dragContext = new RubberBandSelection.DragContext();
        Rectangle rect = new Rectangle();

        Group group;


        public Bounds getBounds() {
            return rect.getBoundsInParent();
        }

        public RubberBandSelection( Group group) {

            this.group = group;

            rect = new Rectangle( 0,0,0,0);
            rect.setStroke(Color.BLUE);
            rect.setStrokeWidth(1);
            rect.setStrokeLineCap(StrokeLineCap.ROUND);
            rect.setFill(Color.LIGHTBLUE.deriveColor(0, 1.2, 1, 0.6));

            group.addEventHandler(MouseEvent.MOUSE_PRESSED, onMousePressedEventHandler);
            group.addEventHandler(MouseEvent.MOUSE_DRAGGED, onMouseDraggedEventHandler);
            group.addEventHandler(MouseEvent.MOUSE_RELEASED, onMouseReleasedEventHandler);

        }

        EventHandler<MouseEvent> onMousePressedEventHandler = new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent event) {

                if( event.isSecondaryButtonDown())
                    return;

                // remove old rect
                rect.setX(0);
                rect.setY(0);
                rect.setWidth(0);
                rect.setHeight(0);

                group.getChildren().remove( rect);


                // prepare new drag operation
                dragContext.mouseAnchorX = event.getX();
                dragContext.mouseAnchorY = event.getY();

                rect.setX(dragContext.mouseAnchorX);
                rect.setY(dragContext.mouseAnchorY);
                rect.setWidth(0);
                rect.setHeight(0);

                group.getChildren().add( rect);

            }
        };

        EventHandler<MouseEvent> onMouseDraggedEventHandler = new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent event) {

                if( event.isSecondaryButtonDown())
                    return;

                double offsetX = event.getX() - dragContext.mouseAnchorX;
                double offsetY = event.getY() - dragContext.mouseAnchorY;

                if( offsetX > 0)
                    rect.setWidth( offsetX);
                else {
                    rect.setX(event.getX());
                    rect.setWidth(dragContext.mouseAnchorX - rect.getX());
                }

                if( offsetY > 0) {
                    rect.setHeight( offsetY);
                } else {
                    rect.setY(event.getY());
                    rect.setHeight(dragContext.mouseAnchorY - rect.getY());
                }
            }
        };


        EventHandler<MouseEvent> onMouseReleasedEventHandler = new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent event) {

                if( event.isSecondaryButtonDown())
                    return;

                // remove rectangle
                // note: we want to keep the ruuberband selection for the cropping => code is just commented out
                /*
                rect.setX(0);
                rect.setY(0);
                rect.setWidth(0);
                rect.setHeight(0);
                group.getChildren().remove( rect);
                */

            }
        };
        private static final class DragContext {

            public double mouseAnchorX;
            public double mouseAnchorY;

        }
    }
}
