/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dapij;

import comms.EventServer;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

/**
 * A singleton class containing the settings of the agent application.
 * 
 * @author Nikolay Pulev <N.Pulev@sms.ed.ac.uk>
 */
public class Settings {
    
    public static final Settings INSTANCE = new Settings();
    
    /* Constant keys for (internal) properties of the Settings Singleton */
    /* abs path to the XML log file (for recording snapshots). */
    public static final String XML_OUT_SETT = "xmlOutFile";
    /* current working dir */
    public static final String CWD = "cwd";
    
    /**
     * A HashMap<String, String> structure that allows storing String
     * properties.
     */
    private HashMap<String, String> settings;
    
    /**
     * The key for a HashMap<Integer, HashMap<String, Breakpoint>>>
     * representing the the list of breakpoints to take/record (in XML)
     * snapshots at.
     */
    private HashMap<Integer, HashMap<String, Breakpoint>> breakpts;
    
    /**
     * Network server used by agent for notifying external programs for user
     * program events.
     */
    private EventServer eventServer;
    
    private Settings() {
        // TODO: support loading settings from a settings file
        settings = new HashMap<String, String>();
        breakpts = new HashMap<Integer, HashMap<String, Breakpoint>>();
        
        /* set dweafult root path to current working directory */
        try {
            setProp(Settings.CWD, new File(".").getCanonicalPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        
        /* use curr working dir to set a default xml output full path */
        // TODO: load the relative path suffix from a config file
        setProp(Settings.XML_OUT_SETT,
                getProp(Settings.CWD) + "/output.xml");
    }
    
    /**
     * Retrieves a setting given its name.
     * 
     * @key The name of the setting to retrieve.
     * @return The value of the setting (a String) or null if it doesn't exist.
     */
    public final String getProp(String key) {
        return (settings.containsKey(key)) ? settings.get(key) : null;
    }

    /**
     * Records or overwrites settings.
     * 
     * @param key A String representing the name of the setting.
     * @param val A String representing the value of the setting.
     */
    public final void setProp(String key, String val) {
        settings.put(key, val);
    }
    
    /**
     * Unsets a settings given its name.
     * 
     * @key The name of the setting to remove.
     * @return Returns true if setting removed and false otherwise.
     */
    public boolean unsetProp(String key) {
        if (settings.containsKey(key)) {
            settings.remove(key);
            return true;
        }
        return false;
    }
    
    public final boolean isSetProp(String key) {
        return settings.containsKey(key);
    }
    
    public void addBreakpt(Breakpoint b) {
        if (!breakpts.containsKey(b.getLine())) {
            breakpts.put(b.getLine(), new HashMap<String, Breakpoint>());
        }
        breakpts.get(b.getLine()).put(b.getSourceFile(), b);
    }
    
    public void addBreakpt(String filename, int line, boolean toXml) {
        addBreakpt(new Breakpoint(filename, line, toXml));
    }
    
    public HashMap<Integer, HashMap<String, Breakpoint>> getBreakpts () {
        return breakpts;
    }
    
    public void setEventServer(EventServer es) {
        this.eventServer = es;
    }
    
    public EventServer getEventServer() {
        return this.eventServer;
    }
}
