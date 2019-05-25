package util;

import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.css.PseudoClass;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import main.Controller;

/**
 * Drag rectangle with mouse cursor in order to get selection bounds
 */
public class RubberBandSelection {
    private static final PseudoClass CSS_CLASS=
            PseudoClass.getPseudoClass("rubber-band");
    private boolean newDrag=true;
    private int cornerDrag=-1;
    final RubberBandSelection.DragContext dragContext = new RubberBandSelection.DragContext();
    private final main.Controller controller;
    Rectangle rect = new Rectangle();
    Circle[] corners;
    Group group;
    private double imageWidth=0;
    private double imageHeight=0;


    public Bounds getBounds() {
        return rect.getBoundsInParent();
    }

    public RubberBandSelection(Controller controller, Group group, DoubleProperty scaleProperty) {

        this.group = group;
        this.controller=controller;
        rect = new Rectangle( 0,0,0,0);
        rect.getStyleClass().add("rubber-band");
        rect.strokeWidthProperty().bind(Bindings.divide(1,scaleProperty));

        /** Init draggable Corners with bindings to rect corners **/
        corners=new Circle[4];
        for(int i=0; i<corners.length; i++) {
            corners[i]=new Circle(5);
            /** CSS styling + scale sizes according to Zoom **/
            corners[i].getStyleClass().add("rubber-corner");
            corners[i].radiusProperty().bind(Bindings.divide(7,scaleProperty));
            corners[i].strokeWidthProperty().bind(Bindings.divide(1,scaleProperty));
            //TODO USE BINDING TO RECT-PROPERTY INSTEAD
            corners[i].setDisable(true);
            corners[i].setVisible(false);
           /* corners[i].disableProperty().bind(rect.focusedProperty());
            corners[i].visibleProperty().bind(rect.focusedProperty());*/
            group.getChildren().add(corners[i]);
        }
        bindCorners();

        group.addEventHandler(MouseEvent.MOUSE_PRESSED, onMousePressedEventHandler);
        //group.addEventHandler(MouseEvent.MOUSE_DRAGGED, onMouseEntryDraggedEventHandler);
        group.addEventHandler(MouseEvent.MOUSE_DRAGGED, onMouseDraggedEventHandler);
        group.addEventHandler(MouseEvent.MOUSE_RELEASED, onMouseReleasedEventHandler);

    }

    private void bindCorners() {
        if(corners.length<4)
            return;
        corners[0].centerXProperty().bind(rect.xProperty());
        corners[0].centerYProperty().bind(rect.yProperty());
        corners[1].centerXProperty().bind(Bindings.add(rect.xProperty(),rect.widthProperty()));
        corners[1].centerYProperty().bind(Bindings.add(rect.yProperty(),0));
        corners[2].centerXProperty().bind(Bindings.add(rect.xProperty(),rect.widthProperty()));
        corners[2].centerYProperty().bind(Bindings.add(rect.yProperty(),rect.heightProperty()));
        corners[3].centerXProperty().bind(Bindings.add(rect.xProperty(),0));
        corners[3].centerYProperty().bind(Bindings.add(rect.yProperty(),rect.heightProperty()));
    }
    private void dragRectangleSelection(int corner, Point2D mouse) {
        Rectangle oldRect=new Rectangle(rect.getX(),rect.getY(),rect.getWidth(),rect.getHeight());
        Point2D corner2D=new Point2D(corners[corner].getCenterX(),corners[corner].getCenterY());
        double diffX=corner2D.getX()-mouse.getX();
        double diffY=corner2D.getY()-mouse.getY();
        switch(corner){
            /** Top left **/
            case 0:
                if(!(rect.getWidth()+diffX<=0)) {
                    rect.setX(mouse.getX());
                    rect.setWidth(rect.getWidth() + diffX);
                }
                if(!(rect.getHeight()+diffY<=0)) {
                    rect.setY(mouse.getY());
                    rect.setHeight(rect.getHeight() + diffY);
                }
                break;
            /** Top right **/
            case 1:
                rect.setWidth(rect.getWidth()-diffX);
                if(!(rect.getHeight()+diffY<=0)) {
                    rect.setHeight(rect.getHeight() + diffY);
                    rect.setY(mouse.getY());
                }
               /* System.out.println("X:\t"+rect.getX()+"\tY:\t"+rect.getY());
                System.out.println("W:\t"+rect.getWidth()+"\tH:\t"+rect.getHeight());*/
                break;
            /** Bottom right **/
            case 2:
                rect.setWidth(rect.getWidth()-diffX);
                rect.setHeight(rect.getHeight()-diffY);
                break;
            /** Bottom left **/
            case 3:
                rect.setHeight(rect.getHeight()-diffY);
                if(!(rect.getWidth()+diffX<=0)) {
                    rect.setWidth(rect.getWidth() + diffX);
                    rect.setX(mouse.getX());
                }
                break;
            default:
                return;
        }
        if(!checkBounds()){
            rect.setX(oldRect.getX());
            rect.setY(oldRect.getY());
            rect.setWidth(oldRect.getWidth());
            rect.setHeight(oldRect.getHeight());
        }
    }

    /** Checks if Selection is inside Image **/
    private boolean checkBounds() {
       /* return !(rect.getX()<0 || rect.getX()>imageWidth
                || rect.getY()<0 ||rect.getY()> imageHeight
                || (rect.getX()+rect.getWidth())>imageWidth
                || (rect.getY()+rect.getHeight())>imageHeight
                || rect.getWidth()<=0
                || rect.getHeight()<=0);*/

        if(rect.getX()<0) {
            rect.setWidth(rect.getWidth()+rect.getX());
            rect.setX(0);
        }
        else if(rect.getX()>imageWidth)
            rect.setX(imageWidth-1);
        if(rect.getY()<0) {
            rect.setHeight(rect.getHeight() + rect.getY());
            rect.setY(0);
        }
        else if(rect.getY()>imageHeight)
            rect.setY(imageHeight-1);
        if(rect.getWidth()<=0)
            rect.setWidth(1);
        else if((rect.getX()+rect.getWidth())>imageWidth)
            rect.setWidth(imageWidth-rect.getX());
        if(rect.getHeight()<=0)
            rect.setHeight(1);
        else if((rect.getY()+rect.getHeight())>imageHeight)
            rect.setHeight(imageHeight-rect.getY());
        return true;
    }

    /** Show Corners if Rectangle clicked **/
    EventHandler<MouseEvent> onMousePressedEventHandler = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent event) {
            controller.setScrollPanePannable(false);
            if(rect.contains(new Point2D(event.getX(),event.getY()))){
                for(int i=0; i<corners.length; i++) {
                    //TODO USE BINDING TO RECT PROPERTY INSTEAD
                    corners[i].setDisable(false);
                    corners[i].setVisible(true);
                }
            }
        }
    };

/*
    */
/** Init new Rectangle Selection if started drag **//*

    EventHandler<MouseEvent> onMouseEntryDraggedEventHandler = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent event) {
            if( !newDrag || event.isSecondaryButtonDown() || event.isMiddleButtonDown())
                return;
            for(int i=0; i<corners.length; i++) {
                if(corners[i].contains(new Point2D(event.getX(),event.getY()))){
                    */
/*corners[i].setCenterX(event.getX());
                    corners[i].setCenterY(event.getY());*//*

                    dragRectangleSelection(i,new Point2D(event.getX(),event.getY()));
                    return;
                }
            }
            // remove old rect
            rect.setX(0);
            rect.setY(0);
            rect.setWidth(0);
            rect.setHeight(0);
            group.getChildren().remove(rect);

            // prepare new drag operation
            dragContext.mouseAnchorX=event.getX();
            dragContext.mouseAnchorY=event.getY();

            rect.setX(dragContext.mouseAnchorX);
            rect.setY(dragContext.mouseAnchorY);
            rect.setWidth(0);
            rect.setHeight(0);

            group.getChildren().add(rect);
            for(int i=0; i<corners.length; i++) {
                corners[i].toFront();
                //TODO USE BINDING TO RECT PROPERTY INSTEAD
                corners[i].setDisable(true);
                corners[i].setVisible(false);
            }

        }
    };

*/


    /** Expand new Selection while Dragging **/
    EventHandler<MouseEvent> onMouseDraggedEventHandler = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent event) {
           /* System.out.println("X:"+event.getX());
            System.out.println("Y:"+event.getY());*/
            if(event.isSecondaryButtonDown() || event.isMiddleButtonDown())
                return;
            if(newDrag) {
                for(int i=0; i<corners.length; i++) {
                    /** Check if new corner should be dragged or if current Corner drag shall be continued **/
                    if((corners[i].contains(new Point2D(event.getX(), event.getY()))&& cornerDrag==-1) ||cornerDrag==i) {
                        /*corners[i].setCenterX(event.getX());
                        corners[i].setCenterY(event.getY());*/
                        dragRectangleSelection(i, new Point2D(event.getX(), event.getY()));
                        cornerDrag=i;
                        return;
                    }
                }
                // remove old rect
                rect.setX(0);
                rect.setY(0);
                rect.setWidth(0);
                rect.setHeight(0);
                group.getChildren().remove(rect);

                // prepare new drag operation
                dragContext.mouseAnchorX=event.getX();
                dragContext.mouseAnchorY=event.getY();

                rect.setX(dragContext.mouseAnchorX);
                rect.setY(dragContext.mouseAnchorY);
                rect.setWidth(0);
                rect.setHeight(0);

                group.getChildren().add(rect);
                for(int i=0; i<corners.length; i++) {
                    corners[i].toFront();
                    //TODO USE BINDING TO RECT PROPERTY INSTEAD
                    corners[i].setDisable(true);
                    corners[i].setVisible(false);
                }
                newDrag=false;
            }
            else {
                /** Check Image Borders are not overlapped **/
                double x=event.getX();
                //x=x<0 ? 0 : (x>imageWidth ? imageWidth : x);
                double y=event.getY();
                //y=y<0 ? 0 : (y>imageHeight ? imageHeight : y);

                double offsetX=x - dragContext.mouseAnchorX;
                double offsetY=y - dragContext.mouseAnchorY;

                if(offsetX>0)
                    rect.setWidth(offsetX);
                else {
                    rect.setX(x);
                    rect.setWidth(dragContext.mouseAnchorX - rect.getX());
                }

                if(offsetY>0) {
                    rect.setHeight(offsetY);
                }
                else {
                    rect.setY(y);
                    rect.setHeight(dragContext.mouseAnchorY - rect.getY());
                }
                checkBounds();
            }
        }
    };

    EventHandler<MouseEvent> onMouseReleasedEventHandler = new EventHandler<MouseEvent>() {

        @Override
        public void handle(MouseEvent event) {
            controller.setScrollPanePannable(true);
            if( event.isSecondaryButtonDown())
                return;
            newDrag=true;
            cornerDrag=-1;
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

    public void reset(){
        // remove old rect
        rect.setX(0);
        rect.setY(0);
        rect.setWidth(0);
        rect.setHeight(0);
        //TODO USE BINDING TO RECT PROPERTY INSTEAD
        for(int i=0; i<corners.length; i++) {
            //TODO USE BINDING TO RECT PROPERTY INSTEAD
            corners[i].setDisable(true);
            corners[i].setVisible(false);
        }
        group.getChildren().remove(rect);
    }
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