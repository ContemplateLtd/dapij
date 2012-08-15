package agent;

import comms.AgentSrv;
import comms.CommsProto;
import java.io.IOException;
import java.lang.instrument.Instrumentation;

/**
 * An agent that instruments user programs for the purpose of collecting runtime
 * data & sending it to external clients for further processing.
 *
 * @author Nikolay Pulev
 */
public final class Agent {

    private Agent() {}

    public static void premain(String argString, Instrumentation inst) throws IOException {

        /* Wait for clients conns 3 times for 5 sec. */ // TODO: load from Settings
        final AgentSrv server = AgentSrv.blockingConnect(CommsProto.HOST, CommsProto.PORT, 3, 5);
        server.setDaemon(true);

        RuntmEventSrc.INSTANCE.getAccsEventSrc().addListener(new AccsEventNetSndr(server));
        RuntmEventSrc.INSTANCE.getCreatEventSrc().addListener(new CreatEventNetSndr(server));

        /* For graceful shutdown upon user program termination. */
        Runtime.getRuntime().addShutdownHook(new Thread() {

            @Override
            public void run() {
                server.shutdown();
                try {
                    server.join();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        server.start(); /* Start server. */
        inst.addTransformer(new transform.Transfmr());
    }
}
