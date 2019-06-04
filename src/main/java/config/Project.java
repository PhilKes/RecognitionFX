package config;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name= "project")
@XmlAccessorType(XmlAccessType.FIELD)
public class Project {

    public Project() {
    }

    public Project(String profile, List<RectangleXML> selections, String imagePath, String name) {
        this.profile=profile;
        this.name=name;
        this.selections=selections;
        this.imagePath=imagePath;
    }
    private String path;
    @XmlAttribute(name="name")
    private String name;

    @XmlAttribute(name="profile")
    private String profile;

    @XmlElementWrapper(name = "selections")
    @XmlElement(name="rectangle")
    private List<RectangleXML> selections;
    @XmlAttribute(name="img")
    private String imagePath;

    public Project(String name) {
        this.name=name;
        selections=new ArrayList<>();
        profile="default";
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder=new StringBuilder("Project:\n");
        stringBuilder.append("Profile:"+profile+"\n");
        stringBuilder.append("Img:"+imagePath+"\n");
        stringBuilder.append("Selections:\n");
        for(RectangleXML rect: selections)
            stringBuilder.append("X:"+rect.getX()+" Y:"+rect.getY()+" W:"+rect.getWidth()+" H:"+rect.getHeight()+"\n");
        return stringBuilder.toString();
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile=profile;
    }

    public List<RectangleXML> getSelections() {
        return selections;
    }

    public void setSelections(List<RectangleXML> selections) {
        this.selections=selections;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath=imagePath;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name=name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path=path;
    }
}
