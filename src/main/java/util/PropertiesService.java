package util;

import main.Controller;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.convert.DefaultListDelimiterHandler;
import org.apache.commons.configuration2.ex.ConfigurationException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.util.*;
import java.util.stream.Collectors;
/**
 * Util Class to save/load properties from external .PROPERTIES
 */
public class PropertiesService {
    public static String CONFIG_FILE="config.properties",
                        LASTDIRECTORY="filechooser.lastdirectory",
                        RECENTLYOPENED="files.recentlyopened";

    public static String TESSERACT_FILE="tesseract.properties";
    //region config.properties
    public static void saveLastOpenedDirectory(File openedFile) {
        try {
            FileBasedConfigurationBuilder<FileBasedConfiguration> builder=getBuilder(CONFIG_FILE);
            Configuration config = builder.getConfiguration();
            config.setProperty(LASTDIRECTORY,openedFile.getParent());
            builder.save();
        }
        catch(ConfigurationException e) {
            e.printStackTrace();
			Controller.getLogger().error(e.getMessage());
        }
    }
    public static String getLastOpenedDirectory(){
        try{
            FileBasedConfigurationBuilder<FileBasedConfiguration> builder=getBuilder(CONFIG_FILE);
            Configuration config=builder.getConfiguration();
            return config.getString(LASTDIRECTORY);
        } catch (Exception ex) {
            //ex.printStackTrace();
            return null;
        }
    }
    @SuppressWarnings("all")
    public static List<String> getRecentlyOpenedFiles(){
        try{
            FileBasedConfigurationBuilder<FileBasedConfiguration> builder=getBuilder(CONFIG_FILE);
            Configuration config=builder.getConfiguration();
            // get the property value and print it out
            ArrayList<String> files=new ArrayList<>(Arrays.asList(config.getStringArray(RECENTLYOPENED)));
            return files;
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
                filePath=filePath.replace("\\","\\\\");
                recentlyOpened.add(0, filePath);
                if(recentlyOpened.size()>5)
                    recentlyOpened.remove(recentlyOpened.size() - 1);
            }
        savePropertyRecentlyOpened(recentlyOpened);
    }
    public static void removeInvalidRecentlyOpened(String filePath) {
        List<String> recentlyOpened=getRecentlyOpenedFiles();
        /** Remove invalid file at the top if already in List **/
        if(recentlyOpened.contains(filePath)){
            int idx=recentlyOpened.indexOf(filePath);
            recentlyOpened.remove(idx);
        }
        savePropertyRecentlyOpened(recentlyOpened);
    }

    private static void savePropertyRecentlyOpened(List<String> recentlyOpened) {
        try {
            FileBasedConfigurationBuilder<FileBasedConfiguration> builder=getBuilder(CONFIG_FILE);
            Configuration config = builder.getConfiguration();
            String files=recentlyOpened.stream().collect(Collectors.joining(","));
            config.setProperty(RECENTLYOPENED,files);
            builder.save();
        }
        catch(ConfigurationException e) {
            e.printStackTrace();
			Controller.getLogger().error(e.getMessage());
        }
    }


    private static Properties getAllProperties(){
        try{
            FileBasedConfigurationBuilder<FileBasedConfiguration> builder=getBuilder(CONFIG_FILE);
            Configuration config = builder.getConfiguration();
            return (Properties) config;
        } catch (Exception ex) {
            //ex.printStackTrace();
            return new Properties();
        }
    }
    @Deprecated
    public static void saveProperties(Properties properties){
        Properties currenProps=getAllProperties();
        try{
            OutputStream output = new FileOutputStream(new File(Controller.class.getClassLoader().getResource(CONFIG_FILE).toURI()));
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
			Controller.getLogger().error(e.getMessage());
        }
    }
    //endregion

    //region tesseract.properties

    public static HashMap<String,String> getAllTesseractProperties(){
        try{
            FileBasedConfigurationBuilder<FileBasedConfiguration> builder=getBuilder(TESSERACT_FILE);
            Configuration config=builder.getConfiguration();
            HashMap<String,String> options=new HashMap<>();
            for(Iterator<String> it=config.getKeys(); it.hasNext(); ) {
                String key=it.next();
                options.put(key,config.getString(key));
            }
            return options;
        } catch (Exception ex) {
            //ex.printStackTrace();
            return new HashMap<>();
        }
    }

    public static void saveTesseractProperties(HashMap<String,String> newOptions) {

        try {
            FileBasedConfigurationBuilder<FileBasedConfiguration> builder=getBuilder(TESSERACT_FILE);
            Configuration config = builder.getConfiguration();
            for(Map.Entry<String,String> option : newOptions.entrySet()){
                config.setProperty(option.getKey(),option.getValue());
            }
            builder.save();
        }
        catch(ConfigurationException e) {
            e.printStackTrace();
			Controller.getLogger().error(e.getMessage());
        }
    }

    //endregion
    private static FileBasedConfigurationBuilder<FileBasedConfiguration> getBuilder(String config){
        Parameters params = new Parameters();
        return new FileBasedConfigurationBuilder<FileBasedConfiguration>(PropertiesConfiguration.class)
                .configure(params.properties()
                        .setFileName(config)
                        .setListDelimiterHandler(new DefaultListDelimiterHandler(',')));
    }


}
