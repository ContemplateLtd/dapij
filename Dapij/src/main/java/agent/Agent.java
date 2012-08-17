package agent;

import comms.AgentSrv;
import comms.proto.AccsMsg;
import comms.proto.CommsProto;
import comms.proto.CreatMsg;

import java.io.IOException;
import java.lang.instrument.Instrumentation;

import transform.AccsEvent;
import transform.AccsEventLisnr;
import transform.CreatEvent;
import transform.CreatEventLisnr;
import transform.InstAccsData;

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

        /*
         * An access event listener that converts events to a format suitable for
         * network transfer & sends them to external subscribers via the agent's event
         * server.
         */
        RuntmEventSrc.INSTANCE.getAccsEventSrc().addListener(new AccsEventLisnr() {

            @Override
            public void handleAccessEvent(AccsEvent e) {
                server.sendMsg(AccsMsg.construct(new InstAccsData(e.getObjId(), e.getThreadId())));
            }
        });

        /*
         * An creation event listener that converts events to a format suitable for
         * network transfer & sends them to external subscribers via the agent's event
         * server.
         */
        RuntmEventSrc.INSTANCE.getCreatEventSrc().addListener(new CreatEventLisnr() {

            @Override
            public void handleCreationEvent(CreatEvent e) {
                server.sendMsg(CreatMsg.construct(e.getObjData()));
            }
        });

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
