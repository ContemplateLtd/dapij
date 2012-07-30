/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package agent;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

/**
 * A singleton class containing the settings of the agent.
 * 
 * @author Nikolay Pulev <N.Pulev@sms.ed.ac.uk>
 */
public class Settings {
    
    public static final Settings INSTANCE = new Settings();
    
    /* Constant keys for (internal) settings of the Settings Singleton */
    /** Key for current working dir setting. */
    public static final String SETT_CWD = "cwd";
    
    /** Key for agent's event server port setting. */
    public static final String SETT_EVS_PORT = "EventSrvPort";
    
    /** Key for agent's event test client port setting. */
    public static final String SETT_CLI_HOST = "EventCliHost";
    
    /**
     * A HashMap<String, String> structure that allows storing String
     * settings.
     */
    private HashMap<String, String> settings;
    
    private Settings() {
        settings = new HashMap<String, String>();
        
        /* Set some internal settings dependent on runtime. */
        /* set default root path to current working directory */
        try {
            set(Settings.SETT_CWD, new File(".").getCanonicalPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        
        //TODO: support loading from config
        loadSettings(); /* load the rest of the settings */
    }
    
    /**
     * Retrieves a setting given its name.
     * 
     * @key The name of the setting to retrieve.
     * @return The value of the setting (a String) or null if it doesn't exist.
     */
    public final String get(String key) {
        return (settings.containsKey(key)) ? settings.get(key) : null;
    }

    /**
     * Records or overwrites settings.
     * 
     * @param key A String representing the name of the setting.
     * @param val A String representing the value of the setting.
     */
    public final void set(String key, String val) {
        settings.put(key, val);
    }
    
    /**
     * Removes a settings given its name.
     * 
     * @key The name of the setting to remove.
     * @return Returns true if setting removed and false otherwise.
     */
    public boolean rm(String key) {
        if (settings.containsKey(key)) {
            settings.remove(key);
            return true;
        }
        return false;
    }
    
    public final boolean isSet(String key) {
        return settings.containsKey(key);
    }
    
    /* Hardcoded settings for now */
    private void loadSettings() {
        set(SETT_EVS_PORT, "7836");
        set(SETT_CLI_HOST, "127.0.0.1");
    }
}
