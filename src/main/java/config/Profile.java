package config;

import options.TesseractConstants;

import java.util.HashMap;

public class Profile {
    private HashMap<String,String> options;
    private String name;

    public Profile(String name, HashMap<String, String> profile) {
        this.name=name;
        options=new HashMap<>(profile);
    }

    public Profile(String name) {
        this.name=name;
        options=new HashMap<>(TesseractConstants.DEFAULTS.getAll());
    }

    public HashMap<String, String> getOptions() {
        return options;
    }

    public void setOptions(HashMap<String, String> options) {
        this.options=new HashMap<>(options);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name=name;
    }
}
