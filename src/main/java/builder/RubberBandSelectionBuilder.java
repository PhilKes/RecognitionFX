package builder;

import javafx.beans.property.DoubleProperty;
import javafx.scene.Group;
import main.Controller;
import util.RubberBandSelection;

public class RubberBandSelectionBuilder {
    private final static Controller controller=Controller.CONTROLLER;
    private Group rootGroup;
    private double x=0;
    private double y=0;
    private double imgWidth=0;
    private double imgHeight=0;
    private int idx=-1;
    private DoubleProperty scaleProperty;
    private String name="Selection";

/*
    public RubberBandSelectionBuilder setController(Controller controller) {
        this.controller=controller;
        return this;
    }
*/

    public RubberBandSelectionBuilder setRootGroup(Group rootGroup) {
        this.rootGroup=rootGroup;
        return this;
    }

    public RubberBandSelectionBuilder setX(double x) {
        this.x=x;
        return this;
    }

    public RubberBandSelectionBuilder setY(double y) {
        this.y=y;
        return this;
    }

    public RubberBandSelectionBuilder setImgWidth(double imgWidth) {
        this.imgWidth=imgWidth;
        return this;
    }

    public RubberBandSelectionBuilder setImgHeight(double imgHeight) {
        this.imgHeight=imgHeight;
        return this;
    }

    public RubberBandSelectionBuilder setIdx(int idx) {
        this.idx=idx;
        return this;
    }

    public RubberBandSelectionBuilder setScaleProperty(DoubleProperty scaleProperty) {
        this.scaleProperty=scaleProperty;
        return this;
    }

    public RubberBandSelectionBuilder setName(String name) {
        this.name=name;
        return this;
    }

    public RubberBandSelection createRubberBandSelection() {
        return new RubberBandSelection(controller, rootGroup, x, y, imgWidth, imgHeight, idx, scaleProperty, name);
    }
}