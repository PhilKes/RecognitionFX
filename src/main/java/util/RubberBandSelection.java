package util;

import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineCap;

/**
 * Drag rectangle with mouse cursor in order to get selection bounds
 */
public class RubberBandSelection {

    final RubberBandSelection.DragContext dragContext = new RubberBandSelection.DragContext();
    private final main.Controller controller;
    Rectangle rect = new Rectangle();
    Group group;
    private double imageWidth=0;
    private double imageHeight=0;


    public Bounds getBounds() {
        return rect.getBoundsInParent();
    }

    public RubberBandSelection(main.Controller controller, Group group) {

        this.group = group;
        this.controller=controller;
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
            if( event.isSecondaryButtonDown() || event.isMiddleButtonDown())
                return;
            controller.setScrollPanePannable(false);

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

    public void reset(){

        // remove old rect
        rect.setX(0);
        rect.setY(0);
        rect.setWidth(0);
        rect.setHeight(0);

        group.getChildren().remove( rect);
    }
    EventHandler<MouseEvent> onMouseDraggedEventHandler = new EventHandler<MouseEvent>() {

        @Override
        public void handle(MouseEvent event) {

            if( event.isSecondaryButtonDown() || event.isMiddleButtonDown())
                return;
            /** Check Image Borders are not overlapped **/
            double x= event.getX();
            x= x<0? 0 : (x>imageWidth? imageWidth : x);
            double y= event.getY();
            y= y<0? 0 : (y>imageHeight? imageHeight : y);

            double offsetX = x - dragContext.mouseAnchorX;
            double offsetY = y - dragContext.mouseAnchorY;

            if( offsetX > 0)
                rect.setWidth( offsetX);
            else {
                rect.setX(x);
                rect.setWidth(dragContext.mouseAnchorX - rect.getX());
            }

            if( offsetY > 0) {
                rect.setHeight( offsetY);
            } else {
                rect.setY(y);
                rect.setHeight(dragContext.mouseAnchorY - rect.getY());
            }
        }
    };


    EventHandler<MouseEvent> onMouseReleasedEventHandler = new EventHandler<MouseEvent>() {

        @Override
        public void handle(MouseEvent event) {
            controller.setScrollPanePannable(true);
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

    public double getImageWidth() {
        return imageWidth;
    }

    public void setImageWidth(double imageWidth) {
        this.imageWidth=imageWidth;
    }

    public double getImageHeight() {
        return imageHeight;
    }

    public void setImageHeight(double imageHeight) {
        this.imageHeight=imageHeight;
    }

    private final class DragContext {

        public double mouseAnchorX;
        public double mouseAnchorY;

    }
}