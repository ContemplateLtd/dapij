package utils;

import agent.Settings;

/**
 * A class containing functionality for processing command line input.
 *
 * @author Nikolay Pulev <N.Pulev@sms.ed.ac.uk>
 *
 */
public class CMDHelper {

    /* Agent command line settings' names. */
    private static String helpShort = "-h";                 /* Help setting. */
    private static String helpLong = "--help";
    private static String serverShort = "-s";               /* Server setting. */
    private static String serverLong = "--server";
    private static String verboseShort = "-v";              /* Verbosity setting. */
    private static String verboseLong = "--verbose";
    private static String noCreatDetectShort = "-nc";       /* No creation detection setting. */
    private static String noCreatDetectLong = "--no-creat";
    private static String noFldAccsDetectShort = "-nf";     /* No fld access detection setting. */
    private static String noFldAccsDetectLong = "--no-fld";
    private static String noMthdAccsDetectShort = "-nm";    /* No mthd access detection setting. */
    private static String noMthdAccsDetectLong = "--no-mthd";
    private static String portShort = "-p";                 /* Comms protocol port setting. */
    private static String portLong = "--port";
    private static String helpMessage = "\n"
            + "Command line arguments are passed to the java agent via the agent argument\n"
            + "string appended to the end of the agent's jar path. For example:\n\n"
            + "USAGE: java -javaagent:/full/path/to/dapij.jar=\"[" + helpShort + "|" + helpLong
                    + "] [" + verboseShort + "|" + verboseLong + "]\n"
            + "\t[" + serverShort + "|" + serverLong + "] ["
                    + noCreatDetectShort + "|" + noCreatDetectLong + "] ["
                    + noFldAccsDetectShort + "|" + noFldAccsDetectLong + "] ["
                    + noMthdAccsDetectShort + "|" + noMthdAccsDetectLong + "]\" MainClass\n"
            + "\n"
            + "\t" + verboseShort + "|" + verboseLong + "\tDisplay verbose output.\n"
            + "\t" + serverShort + "|" + serverLong + "\tUse a communications server.\n"
            + "\t" + noCreatDetectShort + "|" + noCreatDetectLong + "\tDon't detect & "
                    + "generate events on instance creations.\n"
            + "\t" + noFldAccsDetectShort + "|" + noFldAccsDetectLong + "\tDon't detect & "
                    + "generate events on instance field accesses.\n"
            + "\t" + noMthdAccsDetectShort + "|" + noMthdAccsDetectLong + "\tDon't detect &"
                    + " generate events on instance method accesses.\n";
    private boolean shouldTerminate;
    private String argString;

    public CMDHelper(String argString) {
        this.argString = argString;
        shouldTerminate = false;
    }

    /**
     * Processes command line arguments passed via the -javaagent JVM parameter.
     *
     * @param argString the argument string.
     * @return true if program should terminate and false otherwise.
     */
    public void processCmdArgs() {
        if (argString != null) {

            /* Split argString on one or more occurrences of whitespace characters. */
            String[] args = argString.split("\\s+", 0);
            int i = 0;
            while (i < args.length) {

                /* Display help & terminate execution. */
                if (args[i].equals(helpShort) || args[i].equals(helpLong)) {
                    System.out.println(helpMessage); /* Display help message & terminate. */
                    shouldTerminate = true;

                /* Controls whether a network server is used for data streaming. */
                } else if (args[i].equals(serverShort) || args[i].equals(serverLong)) {
                    Settings.INSTANCE.set(Settings.SETT_USE_NET, "true");

                /* Controls whether a verbose output is produced. */
                } else if (args[i].equals(verboseShort) || args[i].equals(verboseLong)) {
                    Settings.INSTANCE.set(Settings.SETT_VERBOSE, "true");

                /* Turns off instance creation detection. */
                } else if (args[i].equals(noCreatDetectShort)
                        || args[i].equals(noCreatDetectLong)) {
                    Settings.INSTANCE.set(Settings.SETT_INST_CREAT, "false");

                /* Turns off instance field access detection. */
                } else if (args[i].equals(noFldAccsDetectShort)
                        || args[i].equals(noFldAccsDetectLong)) {
                    Settings.INSTANCE.set(Settings.SETT_FLD_ACCS, "false");

                /* Turns off instance method access detection. */
                } else if (args[i].equals(noMthdAccsDetectShort)
                        || args[i].equals(noMthdAccsDetectLong)) {
                    Settings.INSTANCE.set(Settings.SETT_MTD_ACCS, "false");

                /* Sets a network port for the communications protocol. */
                } else if (args[i].equals(portShort) || args[i].equals(portLong)) {
                    try {
                        Integer.valueOf(args[++i]); /* Throws if number can't be parsed.*/
                        Settings.INSTANCE.set(Settings.SETT_NET_PORT, args[i]);
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid port. Expected a number, but got: " + args[i]);
                        shouldTerminate = true;
                    }
                } else {
                    System.out.println("Invalid argument: " + args[i]);
                    shouldTerminate = true;
                }
                i++;
            }
        }
    }

    public boolean shouldTerminate() {
        return shouldTerminate;
    }
}
