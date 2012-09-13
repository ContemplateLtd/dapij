package agent;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A singleton managing agent settings. Settings can be added and default values
 * can be overridden using a config file with the name 'config'. The file must
 * be present in the current working directory. It can only contain lines
 * matching one of the criteria listed below. Lines not matching the below
 * criteria are considered invalid and prevent execution. Command line arguments
 * that change the values of some of these settings have precedence over both
 * default presets and over values provided in the config file.
 * <ul>
 * <li>An empty line.</li>
 * <li>A comment line.</li>
 * <li>A setting line with format '[a-zA-Z][a-zA-Z0-9_]*=.*'.</li>
 * </ul>
 *
 * Settings from the config have precedence over preset defaults.
 *
 * @author Nikolay Pulev <N.Pulev@sms.ed.ac.uk>
 */
public final class Settings {

    public static final Settings INSTANCE = new Settings();

    /** The config file's filename. */
    public static final String CFG_FNAME = "config";

    /**
     * Key for the instanceCreation' setting for turning on/off
     * instrumentation. By default, this option is set to 'true'.
     */
    public static final String SETT_INST_CREAT = "instanceCreation";

    /**
     * Key for the 'fieldAccess' setting for turning on/off instrumentation. By
     * default, this option is set to 'true'.
     */
    public static final String SETT_FLD_ACCS = "fieldAccess";

    /**
     * Key for the 'methodAccess' setting for turning on/off instrumentation. By
     * default, this option is set to 'true'.
     */
    public static final String SETT_MTD_ACCS = "methodAccess";

    /**
     * Key for the 'verbose' setting for producing verbose output. By default,
     * this option is set to 'false'.
     */
    public static final String SETT_VERBOSE = "verbose";

    /**
     * Key for the 'cwd' setting that provides the current working directory. By
     * default this is set to the current working directory.
     */
    public static final String SETT_CWD = "cwd";

    /**
     * Key for the 'useNet' network setting for turning on/off the agent's
     * network server. By default, this setting is set to 'false';
     */
    public static final String SETT_USE_NET = "useNet";

    /**
     * Key for the the 'agentNetPort' setting that configures the port on which
     * the agent's network server operates. By default, this setting is set to
     * '7836'.
     */
    public static final String SETT_NET_PORT = "agentNetPort";

    /**
     * Key for the 'agentNetHost' setting configuring the host on which the
     * agent's test client and production server operate. By default, this
     * setting is set to 'localhost'.
     */
    public static final String SETT_NET_HOST = "agentNetHost";

    /** HashMap<String, String> structure that allows storing String settings. */
    private ConcurrentHashMap<String, String> settings;
    private Boolean verbose;

    private Settings() {
        settings = new ConcurrentHashMap<String, String>();

        /* Set some internal settings. */
        /* Set default root path to current working directory. */
        try {
            set(Settings.SETT_CWD, new File(".").getCanonicalPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        /* Turn on all instrumentation by default. */
        set(Settings.SETT_INST_CREAT, "true");
        set(Settings.SETT_MTD_ACCS, "true");
        set(Settings.SETT_FLD_ACCS, "true");

        /*
         * Set default values for host and port settings for the production
         * server and testing client.
         */
        set(SETT_NET_PORT, "7836");
        set(SETT_NET_HOST, "localhost");

        /* Output not verbose by default. */
        set(Settings.SETT_VERBOSE, "false");

        /* load the rest of the settings from config file. */
        if (!loadSettings()) {
            System.out.println("Exitting ...");
            System.exit(1);
        }
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

    /**
     * Loads settings from a config file line by line.
     *
     * @returns true if settings correctly loaded and false otherwise.
     */
    private boolean loadSettings() {
        File config = new File(get(SETT_CWD), CFG_FNAME);
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(config);
        } catch (FileNotFoundException e) {
            println("Could not load settings. \"config\" file does not exist in the current "
                    + "working directory.");
        }
        DataInputStream dis = new DataInputStream(fis);
        BufferedReader br = new BufferedReader(new InputStreamReader(dis));

        /* Compile a regular expression. */
        Pattern settingPattern = Pattern.compile("([a-zA-Z][a-zA-Z0-9_]*)=(.*)");
        String line;
        int lineCount = 1;
        try {
            while ((line = br.readLine()) != null) {

                /* If a setting line. */
                Matcher matcher = settingPattern.matcher(line);
                if (matcher.find() && matcher.group(1) != null && matcher.group(2) != null) {
                        set(matcher.group(1), matcher.group(2)); /* Load extracted setting. */

                /* If not a comment or an empty line, consider invalid. */
                } else if (!line.startsWith("#") && !line.equals("")) {
                    System.out.println("Invalid line in config file: "
                            + config.getPath() + ":" + lineCount);

                    return false;
                }
                lineCount++;
            }

            return true;
        } catch (IOException e) {
            System.out.println("Could not read settings file: '" + config.getPath() + "'.");

            return false;
        } finally {
            try {
                fis.close();
                dis.close();
                br.close();
            } catch (IOException e) {} /* Ignore. */
        }
    }

    /* TODO: Use logger & remove this. */
    public void println(String msg) {

        /* Set verbose field only once. */
        if (verbose == null) {
            String q = get(Settings.SETT_VERBOSE);
            verbose = Boolean.valueOf((q != null && q.equals("true")) ? true : false);
        }
        if (verbose) {
            System.out.println(Thread.currentThread().getName() + ": " + msg);
        }
    }

    /** Outputs all settings to standard output. */
    public void printSettings() {
        System.out.println("### DAPIJ SETTINGS ###");
        for (String key : settings.keySet()) {
            System.out.println(key + "=" + settings.get(key) + ";");
        }
        System.out.println("######################");
    }
}
