/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package agent;

import comms.AgentEventServer;
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
    
    /* Constant keys for (internal) settings of the Settings Singleton */
    /** Key for absolute path to the XML log file setting. */
    public static final String SETT_XML_OUT = "xmlOutFile";
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
    private AgentEventServer eventServer;
    
    private Settings() {
        // TODO: support loading settings from a settings file
        settings = new HashMap<String, String>();
        breakpts = new HashMap<Integer, HashMap<String, Breakpoint>>();
        
        initInternalSettings();
        loadSettings();
    }
    
    /**
     * Retrieves a setting given its name.
     * 
     * @key The name of the setting to retrieve.
     * @return The value of the setting (a String) or null if it doesn't exist.
     */
    public final String getSett(String key) {
        return (settings.containsKey(key)) ? settings.get(key) : null;
    }

    /**
     * Records or overwrites settings.
     * 
     * @param key A String representing the name of the setting.
     * @param val A String representing the value of the setting.
     */
    public final void setSett(String key, String val) {
        settings.put(key, val);
    }
    
    /**
     * Unsets a settings given its name.
     * 
     * @key The name of the setting to remove.
     * @return Returns true if setting removed and false otherwise.
     */
    public boolean unsetSett(String key) {
        if (settings.containsKey(key)) {
            settings.remove(key);
            return true;
        }
        return false;
    }
    
    public final boolean isSetSett(String key) {
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
    
    public void setEventServer(AgentEventServer es) {
        this.eventServer = es;
    }
    
    public AgentEventServer getEventServer() {
        return this.eventServer;
    }

    private void initInternalSettings() {
        /* set dweafult root path to current working directory */
        try {
            setSett(Settings.SETT_CWD, new File(".").getCanonicalPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void loadSettings() {
        // TODO: load network port from config or chose on the fly
        setSett(SETT_EVS_PORT, "7836");
        setSett(SETT_CLI_HOST, "127.0.0.1");
        
        /* use curr working dir to set a default xml output full path */
        setSett(Settings.SETT_XML_OUT,
                getSett(Settings.SETT_CWD) + "/output.xml");
    }
}
