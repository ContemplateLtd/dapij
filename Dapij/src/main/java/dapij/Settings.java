/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dapij;

import java.util.HashMap;

/**
 * A singleton class containing the settings of the agent application.
 * 
 * @author Nikolay Pulev <N.Pulev@sms.ed.ac.uk>
 */
public class Settings {
    
    public static final Settings INSTANCE = new Settings();
    
    /* Settings */
    /**
     * The key for the setting representing the absolute path to the XML
     * log file (for recording snapshots).
     */
    public static final String XML_OUT_SETT = "xmlOutFile";
    
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
    
    private Settings() {
        // TODO: Read settings from a file
        settings = new HashMap<String, String>();
        breakpts = new HashMap<Integer, HashMap<String, Breakpoint>>();
    }
    
    /**
     * Returns the value of a setting given its name or null if the setting
     * id not set.
     * 
     * @key The name of the setting to look for.
     * @return the xmlOutFile
     */
    public String get(String key) {
        return (settings.containsKey(key)) ? settings.get(key) : null;
    }

    /**
     * Records or overwrites settings.
     * 
     * @param key A String representing the name of the setting.
     * @param val A String representing the value of the setting.
     */
    public void set(String key, String val) {
        settings.put(key, val);
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
}