package util;

import main.Controller;
import options.TesseractConstants;
import org.ini4j.Ini;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class IniProperties {
    public static String TESSERACT_FILE="tesseract.ini";

    public static HashMap<String,String> getDefaultSettings(){
        HashMap<String,String> options=new HashMap<>();
        Ini ini=getIni();
        Ini.Section defaultSection= ini.get("default");
        if(defaultSection==null){
            saveDefault(TesseractConstants.DEFAULTS.getAll());
            Controller.getLogger().warn("Created default settings in "+TESSERACT_FILE);
            return getDefaultSettings();
        }
        options.putAll(defaultSection);
        return options;
    }
    public static HashMap<String,String> getProfile(String profile) {
        HashMap<String, String> options=new HashMap<>();
        Ini ini=getIni();
        Ini.Section section=ini.get(profile);
        if(section==null) {
            Controller.getLogger().error("Profile \"" + profile + "\" not found in " + TESSERACT_FILE+", loading default profile");
            options=getDefaultSettings();
            return options;
        }
        options.putAll(section);
        return options;
    }
    public static ArrayList<String> getProfiles(){
        Ini ini=getIni();
        return new ArrayList<>(ini.keySet());
    }

    public static void saveProfile(String profileName,HashMap<String,String> options){
        Ini ini=getIni();
        Ini.Section section=ini.get(profileName);
        if(section==null){
           section= ini.add(profileName);
        }
        section.putAll(options);
        //ini.put(profileName,section);
        try {
            ini.store();
        }
        catch(IOException e) {
            e.printStackTrace();
            Controller.getLogger().error("Could not save \"" + profileName + "\" into " + TESSERACT_FILE);
        }
        Controller.getLogger().info("Saved \"" + profileName + "\" profile");
    }
    public static void saveDefault(HashMap<String, String> options) {
        saveProfile("default",options);
    }


    private static Ini getIni() {
        Ini ini =new Ini();
        try{
            File file=new File(IniProperties.class.getResource("/"+TESSERACT_FILE).getFile());
            ini.load(new FileReader(file));
            ini.setFile(file);
        }
        catch(IOException e) {
            e.printStackTrace();
            Controller.getLogger().error(".ini settings not found:  "+TESSERACT_FILE);
        }
        return ini;
    }

    public static void renameProfile(String profile, String profileNew, HashMap<String, String> options) {
        Ini ini=getIni();
        ini.remove(profile);
        try {
            ini.store();
        }
        catch(IOException e) {
            e.printStackTrace();
            Controller.getLogger().error("Could not remove \"" + profile + "\" in " + TESSERACT_FILE);
        }
        saveProfile(profileNew,options);
    }
}
