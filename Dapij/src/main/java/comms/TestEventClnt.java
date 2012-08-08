/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package comms;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

import agent.Settings;

/**
 * A network client used for testing the agent's network server.
 *
 * @author Nikolay Pulev <N.Pulev@sms.ed.ac.uk>
 */
public class TestEventClnt extends Thread {

    private static final String NM = "TEC";
    private static boolean quiet = shouldBeQuiet();
    private Socket conn;
    private String host;
    private int port;
    private boolean allowedToRun;
    private boolean stopped;
    private DataInputStream inFromServer;
    private ArrayList<EventRecord> eventLog = new ArrayList<EventRecord>();

    /* private DataOutputStream outToServer; */

    public TestEventClnt(String host, int port) {
        this.host = host;
        this.port = port;
        this.allowedToRun = true;
        this.stopped = false;
        setName("test-event-cli");
    }

    public EventRecord[] getEventLog() {
        return eventLog.toArray(new EventRecord[eventLog.size()]);
    }

    /**
     * Attempts to reconnect if forced and only resets inFromServer otherwise.
     *
     * @param force
     *            A boolean value for enforce connection closure.
     */
    private void connect(boolean force) {
        closeConn(force); /* close conn if forced or if not set */
        while (allowedToRun) {
            try {
                if (conn == null || !conn.isConnected()) {
                    stdoutPrintln(NM + ": Connecting to server [" + host + ":" + port
                            + "] ...");
                    conn = new Socket(host, port);
                }
                inFromServer = new DataInputStream(conn.getInputStream());
                stdoutPrintln(NM + ": Done.");
                break;
            } catch (IOException e) {
                stdoutPrintln(NM + ": Could not connect to '" + host + ":" + port + "' ..."
                        + "\n" + NM + ": Attempting again ...");
                try {
                    Thread.sleep(10);
                } catch (Exception ex) {
                    /* Ignore */
                }
                continue;
            }
        }
        stopped = true;
    }

    @Override
    public void run() {
        connect(true);
        eventLog.clear();
        EventRecord event = null;
        while (allowedToRun) {
            try {
                // TODO: remove this workaround and use nonblocking sockets
                if (inFromServer.available() > 5) {
                    event = readEvent();
                } else {
                    Thread.sleep(5);
                    continue;
                }
            } catch (Exception e) {
                throw new RuntimeException(e); // TODO: ignore?
            }
            if (event != null) {
                stdoutPrintln(NM + ": RCV: " + event);
                eventLog.add(event);
            }
            event = null;
        }
        stopped = true;
    }

    public void shutdown() {
        allowedToRun = false; /* Stop main loop. */

        /* Now wait until stopped. */
        while (!stopped) {
            yield();
        }

        /* Shutdown. */
        EventRecord event = null;
        stdoutPrintln(NM + ": Shutting down, fetching last messages ...");
        while (true) {
            try {
                // TODO: remove this workaround and use nonblocking sockets
                if (inFromServer.available() > 5) {
                    event = readEvent();
                    if (event != null) {
                        stdoutPrintln(NM + ": RCV: " + event);
                        eventLog.add(event);
                        event = null;
                    }
                } else {
                    break;
                }
            } catch (IOException e) {
                stdoutPrintln(NM + ": Could not read during shutdown ...");
                throw new RuntimeException(e);
            }
        }
        stdoutPrintln(NM + ": Done.");
    }

    /**
     * Closes connection if not usable or forced. Guarantees input stream to
     * server is always closed after call.
     *
     * @param force
     *            A boolean value for enforce connection closure.
     */
    private void closeConn(boolean force) {
        if (conn != null && (!conn.isConnected() || force)) {
            try {
                conn.close();
            } catch (IOException e) {
                /* Ignore. */
            } finally {
                conn = null;
            }
        }

        /* Always close inFromServer */
        if (inFromServer != null) {
            try {
                inFromServer.close();
            } catch (IOException e) {
                /* Ignore. */
            } finally {
                inFromServer = null;
            }
        }

        /* Uncomment when sending msgs to server needed/supported */
        //if (outToServer != null) {
        //  try {
        //      outToServer.flush();
        //      outToServer.close();
        //  } catch (IOException e) {
        //      // Ignore.
        //  } finally {
        //      outToServer = null;
        //  }
        //}
    }

    private EventRecord readEvent() {
        EventRecord event = null;
        try {
            byte type = inFromServer.readByte();
            int rest = inFromServer.readInt();
            byte[] msg = new byte[rest];
            inFromServer.readFully(msg);
            if (type == CommsProto.TYP_CRT) {
                event = CommsProto.deconstCreatMsg(msg);
            } else if (type == CommsProto.TYP_ACC) {
                event = CommsProto.deconstAccsMsg(msg);
            }
            return event;
        } catch (Exception e) {
            return event;
        }
    }

    private static boolean shouldBeQuiet() {
        String q = Settings.INSTANCE.get(Settings.SETT_QUIET_NET);

        /* By default no messages are sent to stdout. */
        return (q == null || (q != null && !q.equals("false"))) ? true : false;
    }

    private static void stdoutPrintln(String msg) {
        if (!quiet) {
            System.out.println(msg);
        }
    }
}
