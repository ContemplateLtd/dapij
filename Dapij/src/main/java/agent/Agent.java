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

/**
 * An agent that instruments user programs for the purpose of collecting runtime
 * data & sending it to external clients for further processing.
 *
 * @author Nikolay Pulev <N.Pulev@sms.ed.ac.uk>
 */
public final class Agent {

    private Agent() {}

    public static void premain(String argString, Instrumentation inst) throws IOException {

        /* Initialise singletons before transformation starts. */
        InstanceIdentifier.INSTANCE.hashCode(); /* TODO: Remove. Partially solves issue '002'. */

        handleArgs(argString);
        if (Settings.INSTANCE.isSet(Settings.SETT_USE_NET)
                && Settings.INSTANCE.get(Settings.SETT_USE_NET).equals("true")) {

            /*
             * Wait for client conns for 15 sec (3 * 5). Bind attempt interval -
             * 3 sec. TODO: load timeout args from Settings.
             */
            final AgentServer server = AgentServer
                    .blockingConnect(CommsProtocol.HOST, CommsProtocol.PORT, 5000, 3000, 3);
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
        inst.addTransformer(new transform.Transformer());
    }

    private static void handleArgs(String argString) {
        if (argString != null) {

            /* Split argString on one or more occurrences of whitespace characters. */
            String[] args = argString.split("\\s+", 0);
            int i = 0;
            while (i < args.length) {

                /* Controls whether a network server is used for data streaming. */
                if (args[i].equals("--server") || args[i].equals("-s")) {
                    Settings.INSTANCE.set(Settings.SETT_USE_NET, "true");

                /* Controls whether a verbose output is produced. */
                } else if (args[i].equals("--verbose") || args[i].equals("-v")) {
                    Settings.INSTANCE.set(Settings.SETT_VERBOSE, "true");
                }
                i++;
            }
        }
    }
}
