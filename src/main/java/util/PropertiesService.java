package util;

import main.Controller;

import java.io.*;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

/** Util Class to save/load properties from external .PROPERTIES **/
public class PropertiesService {
    public static String PROPERTIES_FILE="config.properties";
    public static void saveLastOpenedDirectory(File openedFile) {
            Properties prop = new Properties();
            prop.setProperty("filechooser.lastdirectory", openedFile.getParent());
            saveProperties(prop);
    }
    public static String getLastOpenedDirectory(){
        try{
            InputStream input =Controller.class.getClassLoader().getResourceAsStream(PROPERTIES_FILE);
            Properties prop = new Properties();
            // load a properties file
            prop.load(input);
            // get the property value and print it out
            System.out.println(prop.getProperty("filechooser.lastdirectory"));
            return prop.getProperty("filechooser.lastdirectory");

        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }
    public static List<String> getRecentlyOpenedFiles(){
        try{
            InputStream input =Controller.class.getClassLoader().getResourceAsStream(PROPERTIES_FILE);
            Properties prop = new Properties();
            // load a properties file
            prop.load(input);
            // get the property value and print it out
            String files=prop.getProperty("files.recentlyopened");
            return Arrays.stream(files.split(",")).collect(Collectors.toList());
        } catch (Exception ex) {
            //ex.printStackTrace();
            return new ArrayList<String>();
        }
    }

    public static void saveRecentlyOpened(String filePath) {
        List<String> recentlyOpened=getRecentlyOpenedFiles();
        /** Place file at the top if already in List **/
        if(recentlyOpened.contains(filePath)){
            int idx=recentlyOpened.indexOf(filePath);
            String oldTop=recentlyOpened.get(0);
            recentlyOpened.set(0,filePath);
            recentlyOpened.set(idx,oldTop);
        }
        /** Add File to top of List **/
        else {
            recentlyOpened.add(0, filePath);
            if(recentlyOpened.size()>5)
                recentlyOpened.remove(recentlyOpened.size() - 1);
        }
        Properties properties=new Properties();
        String files=recentlyOpened.stream().collect(Collectors.joining(","));
        properties.setProperty("files.recentlyopened",files);
        saveProperties(properties);
    }

    public static void removeInvalidRecentlyOpened(String filePath) {
        List<String> recentlyOpened=getRecentlyOpenedFiles();
        /** Remove invalid file at the top if already in List **/
        if(recentlyOpened.contains(filePath)){
            int idx=recentlyOpened.indexOf(filePath);
            recentlyOpened.remove(idx);
        }
        Properties properties=new Properties();
        String files=recentlyOpened.stream().collect(Collectors.joining(","));
        properties.setProperty("files.recentlyopened",files);
        saveProperties(properties);
    }

    public static void saveProperties(Properties properties){
        Properties currenProps=getAllProperties();
        try{
            OutputStream output = new FileOutputStream(new File(Controller.class.getClassLoader().getResource(PROPERTIES_FILE).toURI()));
            properties.stringPropertyNames().forEach(prop->{
                currenProps.setProperty(prop,properties.getProperty(prop));
            });
            currenProps.store(output, null);
            System.out.println("SAVED: "+properties);

        } catch (IOException io) {
            io.printStackTrace();
        }
        catch(URISyntaxException e) {
            e.printStackTrace();
        }
    }
    private static Properties getAllProperties(){
        try{
            InputStream input =Controller.class.getClassLoader().getResourceAsStream(PROPERTIES_FILE);
            Properties prop = new Properties();
            prop.load(input);
          return prop;
        } catch (Exception ex) {
            //ex.printStackTrace();
            return new Properties();
        }
    }


}
