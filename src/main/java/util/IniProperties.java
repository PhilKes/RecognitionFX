package util;

import main.Controller;
import options.TesseractConstants;
import org.ini4j.Ini;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

//TODO HANDLE ALL AS TASKs
public class IniProperties {
    public static class Tesseract {
        public static String TESSERACT_FILE="configs/tesseract.ini";

        public static HashMap<String, String> getDefaultSettings() {
            HashMap<String, String> options=new HashMap<>();
            Ini ini=getIni(TESSERACT_FILE);
            Ini.Section defaultSection=ini.get("default");
            if(defaultSection==null) {
                saveDefault(TesseractConstants.DEFAULTS.getAll());
                Controller.getLogger().warn("Created default settings in " + TESSERACT_FILE);
                return getDefaultSettings();
            }
            options.putAll(defaultSection);
            return options;
        }

        public static HashMap<String, String> getProfile(String profile) {
            HashMap<String, String> options=new HashMap<>();
            Ini ini=getIni(TESSERACT_FILE);
            Ini.Section section=ini.get(profile);
            if(section==null) {
                Controller.getLogger().error("Profile \"" + profile + "\" not found in " + TESSERACT_FILE + ", loading default profile");
                options=getDefaultSettings();
                return options;
            }
            options.putAll(section);
            return options;
        }

        public static ArrayList<String> getProfiles() {
            Ini ini=getIni(TESSERACT_FILE);
            return new ArrayList<>(ini.keySet());
        }

        public static void saveProfile(String profileName, HashMap<String, String> options) {
            Ini ini=getIni(TESSERACT_FILE);
            Ini.Section section=ini.get(profileName);
            if(section==null) {
                section=ini.add(profileName);
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
            saveProfile("default", options);
        }

        public static void renameProfile(String profile, String profileNew, HashMap<String, String> options) {
            Ini ini=getIni(TESSERACT_FILE);
            ini.remove(profile);
            try {
                ini.store();
            }
            catch(IOException e) {
                e.printStackTrace();
                Controller.getLogger().error("Could not remove \"" + profile + "\" in " + TESSERACT_FILE);
            }
            saveProfile(profileNew, options);
        }
    }
    public static class Program{
        public static String CONFIG_FILE="configs/config.ini",
                            LASTOPENEDIMG="filechooser.lastimagedirectory",
                            LASTOPENEDPROJECT="filechooser.lastprojectdirectory",
                            RECENTIMAGES="files.recentlyopened",
                            RECENTPROJECTS="files.recentprojects",
                            THEME="style.theme";

        public static String getLastOpenedDirectory(){
            Ini ini=getIni(CONFIG_FILE);
            Ini.Section section=ini.get("settings");
            if(section==null) {
               /* saveDefault(TesseractConstants.DEFAULTS.getAll());
                Controller.getLogger().warn("Created default settings in " + CONFIG_FILE);
                return getDefaultSettings();*/
            }
            return section.get(LASTOPENEDIMG);
        }
        public static void saveLastOpenedDirectory(File openedFile) {
            Ini ini=getIni(CONFIG_FILE);
            Ini.Section section=ini.get("settings");
            section.put(LASTOPENEDIMG,openedFile.getParent());
            //ini.put(profileName,section);
            try {
                ini.store();
            }
            catch(IOException e) {
                e.printStackTrace();
                Controller.getLogger().error("Could not save LastOpenedImageDirectory into " + CONFIG_FILE);
            }
            Controller.getLogger().info("Saved LastOpenedImageDirectory");
        }

        public static String getLastOpenedProjectDirectory(){
            Ini ini=getIni(CONFIG_FILE);
            Ini.Section section=ini.get("settings");
            if(section==null) {
               /* saveDefault(TesseractConstants.DEFAULTS.getAll());
                Controller.getLogger().warn("Created default settings in " + CONFIG_FILE);
                return getDefaultSettings();*/
            }
            return section.get(LASTOPENEDPROJECT);
        }

        public static void saveLastOpenedProjectDirectory(File openedFile) {
            Ini ini=getIni(CONFIG_FILE);
            Ini.Section section=ini.get("settings");
            section.put(LASTOPENEDPROJECT,openedFile.getParent());
            //ini.put(profileName,section);
            try {
                ini.store();
            }
            catch(IOException e) {
                e.printStackTrace();
                Controller.getLogger().error("Could not save LastOpenedProjectDirectory into " + CONFIG_FILE);
            }
            Controller.getLogger().info("Saved LastOpenedProjectDirectory");
        }
        public static List<String> getRecentlyOpenedImages(){
            Ini ini=getIni(CONFIG_FILE);
            Ini.Section section=ini.get("settings");
            List<String> list=section.getAll(RECENTIMAGES);
            if(list==null){
                return new ArrayList<>();
            }
            return list;
        }

        public static void saveRecentImage(String filePath) {
            Ini ini=getIni(CONFIG_FILE);
            Ini.Section section=ini.get("settings");
            List<String> recentlyOpened=section.getAll(RECENTIMAGES);
            if(recentlyOpened==null) {
                section.add(RECENTIMAGES, filePath);
            }
            else {
                if(recentlyOpened.contains(filePath)) {
                    int idx=recentlyOpened.indexOf(filePath);
                    String oldTop=recentlyOpened.get(0);
                    recentlyOpened.set(0, filePath);
                    recentlyOpened.set(idx, oldTop);
                }
                else {
                    //filePath=filePath.replace("\\","\\\\");
                    recentlyOpened.add(0, filePath);
                    if(recentlyOpened.size()>5)
                        recentlyOpened.remove(recentlyOpened.size() - 1);
                }
                section.putAll(RECENTIMAGES, recentlyOpened);
            }
            try {
                ini.store();
            }
            catch(IOException e) {
                e.printStackTrace();
                Controller.getLogger().error("Could not save RecentlyOpenedFiles into " + CONFIG_FILE);
            }
            Controller.getLogger().info("Saved RecentlyOpenedFiles");
        }

        public static void saveRecentProject(String filePath) {
            Ini ini=getIni(CONFIG_FILE);
            Ini.Section section=ini.get("settings");
            List<String> recentlyOpened=section.getAll(RECENTPROJECTS);
            if(recentlyOpened==null) {
                section.add(RECENTPROJECTS, filePath);
            }
            else {
                if(recentlyOpened.contains(filePath)) {
                    int idx=recentlyOpened.indexOf(filePath);
                    String oldTop=recentlyOpened.get(0);
                    recentlyOpened.set(0, filePath);
                    recentlyOpened.set(idx, oldTop);
                }
                else {
                    //filePath=filePath.replace("\\","\\\\");
                    recentlyOpened.add(0, filePath);
                    if(recentlyOpened.size()>5)
                        recentlyOpened.remove(recentlyOpened.size() - 1);
                }
                section.putAll(RECENTPROJECTS, recentlyOpened);
            }
            try {
                ini.store();
            }
            catch(IOException e) {
                e.printStackTrace();
                Controller.getLogger().error("Could not save RecentProjects into " + CONFIG_FILE);
            }
            Controller.getLogger().info("Saved RecentProjects");
        }

        public static List<String> getRecentProjects(){
            Ini ini=getIni(CONFIG_FILE);
            Ini.Section section=ini.get("settings");
            List<String> list=section.getAll(RECENTPROJECTS);
            if(list==null){
                return new ArrayList<>();
            }
            return list;
        }

        public static void removeInvalidRecentlyOpened(String filePath) {
            Ini ini=getIni(CONFIG_FILE);
            Ini.Section section=ini.get("settings");
            List<String> recentlyOpened= section.getAll(RECENTIMAGES);
            recentlyOpened.remove(filePath);
            section.putAll(RECENTIMAGES,recentlyOpened);
            try {
                ini.store();
            }
            catch(IOException e) {
                e.printStackTrace();
                Controller.getLogger().error("Could not remove RecentlyOpenedFile from " + CONFIG_FILE);
            }
            Controller.getLogger().info("Removed invalid RecentlyOpenedFile");
        }

        public static void removeInvalidRecentProject(String filePath) {
            Ini ini=getIni(CONFIG_FILE);
            Ini.Section section=ini.get("settings");
            List<String> recentlyOpened= section.getAll(RECENTPROJECTS);
            recentlyOpened.remove(filePath);
            section.putAll(RECENTPROJECTS,recentlyOpened);
            try {
                ini.store();
            }
            catch(IOException e) {
                e.printStackTrace();
                Controller.getLogger().error("Could not remove RecentProjects from " + CONFIG_FILE);
            }
            Controller.getLogger().info("Removed invalid RecentProjects");
        }
        public static void saveTheme(int theme) {
            Ini ini=getIni(CONFIG_FILE);
            Ini.Section section=ini.get("settings");
            section.put(THEME,theme);
            try {
                ini.store();
            }
            catch(IOException e) {
                e.printStackTrace();
                Controller.getLogger().error("Could not save Theme into " + CONFIG_FILE);
            }
            Controller.getLogger().info("Saved Theme");

        }

        public static int getTheme(){
            Ini ini=getIni(CONFIG_FILE);
            Ini.Section section=ini.get("settings");
            return Integer.parseInt(section.get(THEME));
        }

        private static HashMap<String,String> getAllProperties(){
            Ini ini=getIni(CONFIG_FILE);
            Ini.Section section=ini.get("settings");
            return new HashMap<>(section);
        }

    }
    private static Ini getIni(String configFile) {
        Ini ini=new Ini();
        try {
            File file=new File(IniProperties.class.getResource("/" + configFile).getFile());
            ini.load(new FileReader(file));
            ini.setFile(file);
        }
        catch(IOException e) {
            e.printStackTrace();
            Controller.getLogger().error(".ini settings not found:  " + configFile);
        }
        return ini;
    }
}
