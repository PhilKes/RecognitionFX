package config;

import org.junit.Before;
import org.junit.Test;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class JUnitTests {

    private JAXBContext context;

    @Before
    public void init() throws JAXBException {
        this.context=JAXBContext.newInstance(Project.class);
    }
    @Test
    public void xmlSerialize() throws JAXBException {
        Marshaller marshaller=this.context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        ArrayList<RectangleXML> selections= new ArrayList<>();
        selections.addAll(Arrays.asList(new RectangleXML("rect1",10,10,100,100),
                new RectangleXML("rect2",100,20,20,50)));
        marshaller.marshal(new Project("default",selections,
                        "C:\\\\Users\\\\Phil\\\\Pictures\\\\20170907_170145_small.JPG","projectTest")
                            ,new File("projectTest.xml"));
        Unmarshaller unmarshaller=this.context.createUnmarshaller();
        Project unmarshalled=(Project)unmarshaller.unmarshal(new File("projectTest.xml"));
        System.out.println(unmarshalled);
    }

}
