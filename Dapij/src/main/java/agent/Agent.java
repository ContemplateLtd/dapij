package agent;

import comms.AgentServer;
import comms.CommsProtocol;
import comms.CommsProtocol.AccsMsg;
import comms.CommsProtocol.CreatMsg;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import transform.AccessEvent;
import transform.AccessEventListener;
import transform.CreationEvent;
import transform.CreationEventListener;
import utils.CMDHelper;

/**
 * An agent that instruments user programs for the purpose of collecting runtime
 * data & sending it to external clients for further processing.
 *
 * @author Nikolay Pulev <N.Pulev@sms.ed.ac.uk>
 */
public final class Agent {

    private Agent() {}

    public static void premain(String argString, Instrumentation inst) throws IOException {

        /* Initialise singleton before transformation starts. */
        InstanceIdentifier.INSTANCE.hashCode(); /* TODO: Remove. Partially solves issue '002'. */

        /* Process command line arguments. */
        CMDHelper cmdHelper = new CMDHelper(argString);
        cmdHelper.processCmdArgs();
        if (cmdHelper.shouldTerminate()) {
            System.exit(0);
        }

        if (Settings.INSTANCE.isSet(Settings.SETT_USE_NET)
                && Settings.INSTANCE.get(Settings.SETT_USE_NET).equals("true")) {
            startNetworkServer();
        }
        inst.addTransformer(new transform.Transformer());
    }

    private static void startNetworkServer() {
        long soTimeout = 0;
        long attemptInterval = 0;
        int attempts = 0;
        try {
            soTimeout = Long.valueOf(Settings.INSTANCE.get("serverSoTimeout")).longValue();
        } catch (NumberFormatException e) {
            throw new RuntimeException("Invalid serverSoTimeout setting. Expected a long, but "
                    + "got: " + Settings.INSTANCE.get("serverSoTimeout"));
        }
        try {
            attemptInterval = Long.valueOf(
                    Settings.INSTANCE.get("serverAttemptInterval")).longValue();
        } catch (NumberFormatException e) {
            throw new RuntimeException("Invalid serverAttemptInterval setting. Expected a long, but"
                    + " got: " + Settings.INSTANCE.get("serverAttemptInterval"));
        }
        try {
            attempts = Integer.valueOf(Settings.INSTANCE.get("serverWaitAttempts")).intValue();
        } catch (NumberFormatException e) {
            throw new RuntimeException("Invalid serverWaitAttempts setting. Expected an int, but "
                    + "got: " + Settings.INSTANCE.get("serverWaitAttempts"));
        }

        /*
         * Wait for client connections for soTimeout * attempts seconds with an
         * interval between accept() attempts equal to attemptInterval.
         */
        final AgentServer server = AgentServer.blockingConnect(
                CommsProtocol.HOST, CommsProtocol.PORT, soTimeout, attemptInterval, attempts);
        server.setDaemon(true);

        /*
         * An access event listener that converts events to a format
         * suitable for network transfer & sends them to external
         * subscribers via the agent's event server.
         */
        RuntimeEventSource.INSTANCE.getAccsEventSrc().addListener(new AccessEventListener() {

            @Override
            public void handleAccessEvent(AccessEvent e) {
                try {
                    server.blockSnd(AccsMsg.construct(e.getAccsData()));
                } catch (InterruptedException e1) {
                    e1.printStackTrace();   /* TODO: add logging. */
                }
            }
        });

        /*
         * An creation event listener that converts events to a format
         * suitable for network transfer & sends them to external
         * subscribers via the agent's event server.
         */
        RuntimeEventSource.INSTANCE.getCreatEventSrc().addListener(new CreationEventListener() {

            @Override
            public void handleCreationEvent(CreationEvent e) {
                try {
                    server.blockSnd(CreatMsg.construct(e.getCreatData()));
                } catch (InterruptedException e1) {
                    e1.printStackTrace();   /* TODO: add logging. */
                }
            }
        });

        /* For graceful shutdown upon user program termination. */
        Runtime.getRuntime().addShutdownHook(new Thread() {

            @Override
            public void run() {
                server.shutdown();  /* Initiate shutdown. */
                try {
                    server.join();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        server.start();
    }
}
