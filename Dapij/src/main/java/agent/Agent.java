package agent;

import comms.AgentEventSrv;
import comms.CommsProto;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * An agent that instruments user programs for the purpose of collecting runtime
 * data & sending it to external clients for further processing.
 *
 * @author Nikolay Pulev
 */
public final class Agent {

    private Agent() {}

    public static void premain(String argString, Instrumentation inst) throws IOException {
        final AgentEventSrv aes = setupEventSrv();

        RuntmEventSrc.INSTANCE.getAccsEventSrc().addListener(new AccsEventNetSndr(aes));
        RuntmEventSrc.INSTANCE.getCreatEventSrc().addListener(new CreatEventNetSndr(aes));

        /* For gracefully shutdown when user program ends. */
        Runtime.getRuntime().addShutdownHook(new Thread() {

            @Override
            public void run() {
                aes.shutdown();
            }
        });
        aes.start(); /* Start server. */
        inst.addTransformer(new transform.Transfmr());
    }

    /**
     * Blocks until a client connects & starts, in a different thread, a server
     * passing to it the created sockets.
     * @throws IOException
     */
    public static AgentEventSrv setupEventSrv() {
        ServerSocket srvSock;
        try {
            srvSock = new ServerSocket(CommsProto.PORT);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        /* Attempt to connect '3' times */ // TODO: load from Settings
        Socket conn = AgentEventSrv.blockingConnect(srvSock, 3, 5);
        AgentEventSrv aes = new AgentEventSrv(srvSock, conn);
        aes.setDaemon(true);

        return aes;
    }
}
