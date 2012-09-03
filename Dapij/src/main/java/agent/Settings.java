package agent;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A singleton containing agent settings.
 *
 * @author Nikolay Pulev <N.Pulev@sms.ed.ac.uk>
 */
public final class Settings {

    public static final Settings INSTANCE = new Settings();

    /* Constant keys for (internal) settings of the Settings Singleton */

    /** Verbose setting name. */
    public static final String SETT_VERBOSE = "verbose";

    /** Current working directory setting name. */
    public static final String SETT_CWD = "cwd";

    /** User network setting name. */
    public static final String SETT_USE_NET = "useNet";

    /** Agent's event server port setting name. */
    public static final String SETT_EVS_PORT = "EventSrvPort";

    /** Agent's test event client host setting name. */
    public static final String SETT_CLI_HOST = "EventCliHost";  // TODO: review this setting's name

    /** A HashMap<String, String> structure that allows storing String settings. */
    private  ConcurrentHashMap<String, String> settings;

    private Boolean verbose;

    private Settings() {
        settings = new ConcurrentHashMap<String, String>();

        /* Set some internal settings dependent on runtime. */
        /* set default root path to current working directory */
        try {
            set(Settings.SETT_CWD, new File(".").getCanonicalPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        loadSettings(); /* load the rest of the settings */
    }

    /**
     * Retrieves a setting given its name.
     *
     * @param key
     *            The name of the setting to retrieve.
     * @return The value of the setting (a String) or null if it doesn't exist.
     */
    public String get(String key) {
        return settings.get(key);
    }

    /**
     * Records or overwrites settings.
     *
     * @param key
     *            A String representing the name of the setting.
     * @param val
     *            A String representing the value of the setting.
     * @returns The previous value for this settings, if any.
     */
    public String set(String key, String val) {
        return settings.put(key, val);
    }

    /**
     * Removes a settings given its name.
     *
     * @param key
     *            The name of the setting to remove.
     * @return Returns the setting removed or null if no such setting.
     */
    public String rm(String key) {
        return settings.remove(key);
    }

    public boolean isSet(String key) {
        return settings.containsKey(key);
    }

    /* TODO: Implement config file loading. Hardcode settings for now. */
    private void loadSettings() {
        set(SETT_EVS_PORT, "7836");
        set(SETT_CLI_HOST, "localhost");
    }

    /* TODO: Use logger & remove this. */
    public void println(String msg) {

        /* Set field only once. */
        if (verbose == null) {
            String q = get(Settings.SETT_VERBOSE);
            verbose = Boolean.valueOf((q != null && q.equals("true")) ? true : false);
        }
        if (verbose) {
            System.out.println(Thread.currentThread().getName() + ": " + msg);
        }
    }
}
