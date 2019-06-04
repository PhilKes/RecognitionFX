package config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name="rectangle")
public class RectangleXML {
    public RectangleXML() {
    }

    public RectangleXML(String name,double x, double y, double width, double height) {
        this.name=name;
        this.x=x;
        this.y=y;
        this.height=height;
        this.width=width;
    }

    @XmlAttribute(name="name")
    private String name;
    @XmlAttribute(name="x")
    private double x;

    @XmlAttribute(name="y")
    private double y;

    @XmlAttribute(name="height")
    private double height;

    @XmlAttribute(name="width")
    private double width;

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getHeight() {
        return height;
    }

    public double getWidth() {
        return width;
    }

    public String getName() {
        return name;
    }
}
