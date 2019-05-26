package util;

import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
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
 * Create Rectangle Selection with Mouse Dragging
 * Rectangle has draggable Corners to expand Selection
 */
public class RubberBandSelection {
    private static final PseudoClass CSS_CLASS=
            PseudoClass.getPseudoClass("rubber-band");
    private final Group rootGroup;
    private final EventHandler<MouseEvent> releaseEvent,dragEvent;
    /** Internal value to determine if new drag is due in Mouse Handler */
    private boolean newDrag=true;

    /** Stores which Item is being dragged (-1: none, 0-3: corners, 5: rectangle */
    private int draggedItem=-1;
    final RubberBandSelection.DragContext dragContext = new RubberBandSelection.DragContext();
    private final main.Controller controller;

    /** Rectangle Selection Object */
    Rectangle rect = new Rectangle();

    /** Array of Rectangle Corners*/
    Circle[] corners;
    Group group;
    private double imageWidth=0;
    private double imageHeight=0;
    private int idx=-1;
    private SimpleBooleanProperty active=new SimpleBooleanProperty(false);
    /**
     * @return Bounds of Rectangle Selection
     */
    public Bounds getBounds() {
        return rect.getBoundsInParent();
    }

    //TODO BUILDER PATTERN
    public RubberBandSelection(Controller controller, Group rootGroup, double x, double y, double imgWidth, double imgHeight, int idx, DoubleProperty scaleProperty) {
        this.rootGroup=rootGroup;
        this.group = new Group();
        this.rootGroup.getChildren().add(group);
        this.controller=controller;
        this.idx=idx;
        imageWidth=imgWidth;
        imageHeight=imgHeight;
        rect = new Rectangle( x,y,20,20);
        rect.getStyleClass().add("rubber-band");
        rect.strokeWidthProperty().bind(Bindings.divide(1,scaleProperty));
        setIdx(idx);
        this.group.getChildren().add(rect);
        /** Init draggable Corners with bindings to rect corners **/
        corners=new Circle[4];
        for(int i=0; i<corners.length; i++) {
            corners[i]=new Circle(5);
            /** CSS styling + scale sizes according to Zoom **/
            corners[i].getStyleClass().add("rubber-corner");
            corners[i].radiusProperty().bind(Bindings.divide(7,scaleProperty));
            corners[i].strokeWidthProperty().bind(Bindings.divide(1,scaleProperty));
            /** Show corners if controller has selected its index **/
            corners[i].disableProperty().bind(Bindings.not(active));
            corners[i].visibleProperty().bind(active);
           /* corners[i].disableProperty().bind(rect.focusedProperty());
            corners[i].visibleProperty().bind(rect.focusedProperty());*/
            group.getChildren().add(corners[i]);
        }
        //showCorners(true);
        bindCorners();

        dragContext.mouseAnchorX=x;
        dragContext.mouseAnchorY=y;

        this.rootGroup.addEventHandler(MouseEvent.MOUSE_PRESSED, this::onMousePressed);
        //group.addEventHandler(MouseEvent.MOUSE_DRAGGED, onMouseEntryDraggedEventHandler);
        dragEvent=event->{
            /** Only react if selection is active */
            if(!active.get()) {
                event.consume();
                return;
            }
            this.onMouseDragged(event);
        };
        this.rootGroup.addEventHandler(MouseEvent.MOUSE_DRAGGED, dragEvent);
        releaseEvent=event->{
            /** Only react if selection is active */
            if(!active.get()) {
                event.consume();
                return;
            }
            this.onMouseReleased(event);
        };
        this.rootGroup.addEventHandler(MouseEvent.MOUSE_RELEASED, releaseEvent);
    }

    @Override
    public String toString() {
        return "Selection: X:\t"+rect.getX()+"\tY:"+rect.getY()+"\tW:"+rect.getWidth()+"\tH:"+rect.getHeight();
    }

    /**
     * Binds Corners to Rectangle's properties
     */
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

    /**
     * Expands selection Rectangle corresponding to Mouse Dragging
     * @param corner which Corner has been dragged
     * @param mouse Mouse position
     */
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
       /* if(!checkBounds()){
            rect.setX(oldRect.getX());
            rect.setY(oldRect.getY());
            rect.setWidth(oldRect.getWidth());
            rect.setHeight(oldRect.getHeight());
        }*/
       checkBounds();
    }

    /**
     * Checks if Selection is inside Image
     */
    private void checkBounds() {
       /* return !(rect.getX()<0 || rect.getX()>imageWidth
                || rect.getY()<0 ||rect.getY()> imageHeight
                || (rect.getX()+rect.getWidth())>imageWidth
                || (rect.getY()+rect.getHeight())>imageHeight
                || rect.getWidth()<=0
                || rect.getHeight()<=0);*/
        /** Correct Position and Size of Rectangle if necessary,
         *  draggedItem=5 -> Entire Rectangle is being dragged
         */
        if(rect.getX()<0) {
            if(draggedItem!=5)
                rect.setWidth(rect.getWidth()+rect.getX());
            rect.setX(0);
        }
        else if(rect.getX()>imageWidth)
            rect.setX(imageWidth-1);
        if(rect.getY()<0) {
            if(draggedItem!=5)
                rect.setHeight(rect.getHeight() + rect.getY());
            rect.setY(0);
        }
        else if(rect.getY()>imageHeight)
            rect.setY(imageHeight-1);
        if(rect.getWidth()<=0)
            rect.setWidth(1);

        else if((rect.getX()+rect.getWidth())>imageWidth) {
            if(draggedItem!=5)
                rect.setWidth(imageWidth - rect.getX());
            else
                rect.setX(imageWidth-rect.getWidth());
        }
        if(rect.getHeight()<=0) {
            rect.setHeight(1);
        }
        else if((rect.getY()+rect.getHeight())>imageHeight) {
            if(draggedItem!=5)
                rect.setHeight(imageHeight - rect.getY());
            else
                rect.setY(imageHeight-rect.getHeight());
        }
    }

    /** Show Corners if Rectangle clicked **/
    public void onMousePressed(MouseEvent event) {
        controller.setScrollPanePannable(false);
       /* if(!active.get())
            return;*/
        if(rect.contains(new Point2D(event.getX(),event.getY()))){
            /*for(int i=0; i<corners.length; i++) {
                //TODO USE BINDING TO RECT PROPERTY INSTEAD
                corners[i].setDisable(false);
                corners[i].setVisible(true);
            }*/
            controller.setActiveSelection(idx);
        }
    }

    /** Expand new Selection while Dragging **/
    public void onMouseDragged(MouseEvent event) {
       /* System.out.println("X:"+event.getX());
        System.out.println("Y:"+event.getY());*/
        if(event.isSecondaryButtonDown() || event.isMiddleButtonDown())
            return;
        /** Check Corner Drag or start new Drag **/
        //if(newDrag) {
        Point2D mouse=new Point2D(event.getX(), event.getY());
        for(int i=0; i<corners.length; i++) {
            /** Check if new corner should be dragged or if current Corner drag shall be continued **/
            if((corners[i].contains(mouse)&& draggedItem==-1) || draggedItem==i) {
                    /*corners[i].setCenterX(event.getX());
                    corners[i].setCenterY(event.getY());*/
                draggedItem=i;
                dragRectangleSelection(i, new Point2D(event.getX(), event.getY()));
                return;
            }
        }
        /** Check if entire rectangle selection is being dragged**/
        if(rect.contains(mouse) || draggedItem==5){
            draggedItem=5;
            rect.setX(mouse.getX()-rect.getWidth()/2);
            rect.setY(mouse.getY()-rect.getHeight()/2);
            checkBounds();
            return;
        }
        /** Otherwise new selection is being drawn **/
            /*else {
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
        }
        *//** Expand Selection with Dragg **//*
        else {
            *//** Check Image Borders are not overlapped **//*
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
        }*/
    }

    /** Resets newDrag,draggedItem flags after dragging*/
    public void onMouseReleased(MouseEvent event) {
        if(!active.get())
            return;
        controller.setScrollPanePannable(true);
        if( event.isSecondaryButtonDown())
            return;
        newDrag=true;
        draggedItem=-1;
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

    /** Resets selection to empty rectangle*/
    public void reset(){
        // remove old rect
        rect.setX(0);
        rect.setY(0);
        rect.setWidth(0);
        rect.setHeight(0);
        for(int i=0; i<corners.length; i++)
            group.getChildren().remove(corners[i]);
        corners=null;
        group.getChildren().remove(rect);
        this.rootGroup.removeEventHandler(MouseEvent.MOUSE_DRAGGED, dragEvent);
        this.rootGroup.removeEventHandler(MouseEvent.MOUSE_RELEASED, releaseEvent);
        this.rootGroup.getChildren().remove(group);
    }

    public void setImageWidth(double imageWidth) {
        this.imageWidth=imageWidth;
    }
    public void setImageHeight(double imageHeight) {
        this.imageHeight=imageHeight;
    }

    /** Update Index inside of selections-ArrayList */
    public void setIdx(int i) {
        idx=i;
        active.unbind();
        /** Bind to controller selection **/
        active.bind(Bindings.equal(controller.activeSelectionProperty(),idx));
    }


    /**
     * Helper Class for Mouse Dragging
     */
    private final class DragContext {
        public double mouseAnchorX;
        public double mouseAnchorY;

    }
}