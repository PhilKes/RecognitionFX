package util;

import main.Controller;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.File;

public class XMLService {
    static JAXBContext context;

    static {
        try {
            context=JAXBContext.newInstance(Project.class);
        }
        catch(JAXBException e) {
            e.printStackTrace();
        }
    }

    public static Project parseProject(File file){
        try {
            Unmarshaller unmarshaller=context.createUnmarshaller();
            Project unmarshalled=(Project)unmarshaller.unmarshal(file);
            unmarshalled.setPath(file.getAbsolutePath());
            return unmarshalled;
        }
        catch(JAXBException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void saveProject(Project project,String path){
        try {
            Marshaller marshaller=context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(project,new File(path));
            Controller.getLogger().info("Saved project "+project.getName());
        }
        catch(JAXBException e) {
            e.printStackTrace();
        }
    }
}
