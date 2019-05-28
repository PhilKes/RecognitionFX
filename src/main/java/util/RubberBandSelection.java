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
 * Rectangle Selection with Mouse Dragging
 */
public class RubberBandSelection {
    private static final PseudoClass CSS_CLASS=
            PseudoClass.getPseudoClass("rubber-band");
    private static final int DEFAULT_RADIUS=5,
                            DEFAULT_WIDTH=20,
                            DEFAULT_HEIGHT=20;
    /**
     * Group used for ZoomableScrollPane*/
    private final Group rootGroup;
    private final EventHandler<MouseEvent> releaseEvent,dragEvent;

    /**
     * Stores which Item is being dragged (-1: none, 0-3: corners, 5: rectangle */
    private int draggedItem=-1;
    final RubberBandSelection.DragContext dragContext = new RubberBandSelection.DragContext();
    private final main.Controller controller;

    /**
     * Rectangle Selection Object */
    private Rectangle rect = new Rectangle();

    /**
     * Array of Rectangle Corners*/
    private Circle[] corners;
    private Group group;
    private double imageWidth=0;
    private double imageHeight=0;
    private String name="";
    private int idx=-1;
    /**
     * Stores if current Selection is active/selected*/
    private SimpleBooleanProperty active=new SimpleBooleanProperty(false);

    public RubberBandSelection(Controller controller, Group rootGroup, double x, double y, double imgWidth, double imgHeight, int idx, DoubleProperty scaleProperty, String name) {
        this.rootGroup=rootGroup;
        this.group = new Group();
        this.rootGroup.getChildren().add(group);
        this.controller=controller;
        this.idx=idx;
        this.name=name;
        imageWidth=imgWidth;
        imageHeight=imgHeight;
        rect = new Rectangle( x,y,DEFAULT_WIDTH,DEFAULT_HEIGHT);
        rect.getStyleClass().add("rubber-band");
        rect.strokeWidthProperty().bind(Bindings.divide(1,scaleProperty));
        setIdx(idx);
        this.group.getChildren().add(rect);
        /** Init draggable Corners with bindings to rect corners **/
        corners=new Circle[4];
        for(int i=0; i<corners.length; i++) {
            corners[i]=new Circle(DEFAULT_RADIUS);
            /** CSS styling + scale sizes according to Zoom **/
            corners[i].getStyleClass().add("rubber-corner");
            corners[i].radiusProperty().bind(Bindings.divide(7,scaleProperty));
            corners[i].strokeWidthProperty().bind(Bindings.divide(1,scaleProperty));
            /** Show corners if controller has selected its index **/
            corners[i].disableProperty().bind(Bindings.not(active));
            corners[i].visibleProperty().bind(active);
            group.getChildren().add(corners[i]);
        }
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

    /**
     * Binds Corners to Rectangle's properties (initial)*/
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
     * Show Corners if Rectangle clicked*/
    public void onMousePressed(MouseEvent event) {
        if(event.isSecondaryButtonDown() || event.isMiddleButtonDown())
            return;
        controller.setScrollPanePannable(false);
        if(rect.contains(new Point2D(event.getX(),event.getY()))){
            dragContext.mouseAnchorX=event.getX()-rect.getX();
            dragContext.mouseAnchorY=event.getY()-rect.getY();
            controller.setActiveSelection(idx);
        }
    }

    /**
     * Expand new Selection while Dragging*/
    public void onMouseDragged(MouseEvent event) {
        if(event.isSecondaryButtonDown() || event.isMiddleButtonDown())
            return;
        /**
         * Check Corner Drag or start new Drag **/
        Point2D mouse=new Point2D(event.getX(), event.getY());
        for(int i=0; i<corners.length; i++) {
            /**
             * Check if new corner should be dragged or if current Corner drag shall be continued **/
            if((corners[i].contains(mouse)&& draggedItem==-1) || draggedItem==i) {
                draggedItem=i;
                dragRectangleSelection(i, new Point2D(event.getX(), event.getY()));
                controller.refreshSelectedIteminList();
                return;
            }
        }
        /**
         * Check if entire rectangle selection is being dragged**/
        if(rect.contains(mouse) || draggedItem==5){
            draggedItem=5;
            rect.setX(event.getX()-dragContext.mouseAnchorX);
            rect.setY(event.getY()-dragContext.mouseAnchorY);
            checkBounds();
            controller.refreshSelectedIteminList();
            return;
        }
    }

    /**
     * Resets newDrag,draggedItem flags after dragging*/
    public void onMouseReleased(MouseEvent event) {
        if(!active.get())
            return;
        controller.setScrollPanePannable(true);
        if( event.isSecondaryButtonDown())
            return;
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
        controller.refreshSelectedIteminList();
    }

    /**
     * Checks if Selection is inside Image*/
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
    public Bounds getBounds() {
        return rect.getBoundsInParent();
    }
    public Rectangle getRect(){
        return rect;
    }
    /**
     * Resets selection and removes it from rootGroup*/
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
    public void setX(double x){
        rect.setX(x);
        checkBounds();
    }
    public void setY(double y){
        rect.setY(y);
        checkBounds();
    }
    public void setWidth(double w){
        rect.setWidth(w);
        checkBounds();
    }
    public void setHeight(double h){
        rect.setHeight(h);
        checkBounds();
    }
    public void setImageWidth(double imageWidth) {
        this.imageWidth=imageWidth;
    }
    public void setImageHeight(double imageHeight) {
        this.imageHeight=imageHeight;
    }
    /**
     * Update Index inside of selections-ArrayList */
    public void setIdx(int i) {
        idx=i;
        active.unbind();
        /** Bind to controller selection **/
        active.bind(Bindings.equal(controller.activeSelectionProperty(),idx));
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name=name;
    }

    @Override
    public String toString() {
        return "Selection: \""+name+"\" X:"+rect.getX()+" Y:"+rect.getY()+" W:"+rect.getWidth()+" H:"+rect.getHeight();
    }

    /**
     * Helper Class for Mouse Dragging
     */
    private final class DragContext {
        public double mouseAnchorX;
        public double mouseAnchorY;
    }
}