package agent;

import comms.AgentEventSrv;
import comms.CommsProto;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * An agent that instruments user programs for the purpose of collecting
 * runtime data & sending it to external clients for further processing.
 * 
 * @author Nikolay Pulev
 */
public class Agent {
    
    public static void premain(String argString, Instrumentation inst)
            throws IOException {
        handleArgs(argString);  /* format, read & processs arguments */
        final AgentEventSrv aes = setupEventSrv();
        
        RuntmEventSrc.INSTANCE.getAccsEventSrc().addListener(
                new AccsEventNetSndr(aes));
        RuntmEventSrc.INSTANCE.getCreatEventSrc().addListener(
                new CreatEventNetSndr(aes));
        
        RuntmEventSrc.INSTANCE.getCreatEventSrc().addListener(
                new InstCreatTracker());
        
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

    private static void handleArgs(String argString) {
        if (argString != null) {

            /* Split arglsit on one or more whitespaces */
            String[] args = argString.split("\\s+", 0);
            int i = 0;
            while (i < args.length) {
                
                /* If needed, add arguments here. */
                //if (args[i].equals(...)) {
                //...
                //}
                i++;
            }
        }
    }
    
    /**
     * Blocks until a client connects & starts, in a different thread, a server 
     * passing to it the created sockets.
     */
    public static AgentEventSrv setupEventSrv() {
        ServerSocket srvSock = null;
        Socket conn = null;
        String nm = AgentEventSrv.nm;
        /* Attempt to connect 3 times */ // TODO: load from config
        for (int i = 1; i <= 3; i++) {
            try {
                System.out.println(nm + ": [" + i + "] AgentEventServer: " +
                        "Binding on port '" + CommsProto.port + "'.");
                srvSock = new ServerSocket(CommsProto.port);
                System.out.println(nm + ": AgentEventServer: Done.");
                System.out.println(nm + ": AgentEventServer: Listening" +
                        " for clients ...");
                conn = srvSock.accept();
                System.out.println(nm + ": AgentEventServer: Client [" +
                            conn.getRemoteSocketAddress() + "] connected ...");
                break;
            } catch (IOException ex) {
                System.out.println(nm + ": AgentEventServer: Could not " +
                        "connect, trying again ...");
                continue;
            }
        }
        
        if (srvSock == null || conn == null) {
            throw new RuntimeException(nm + ": AgentEventServer: Could" +
                    " not start! Execution abroted.\n");
        }
        
        AgentEventSrv aes = new AgentEventSrv(srvSock, conn);
        aes.setDaemon(true);
        
        return aes;
    }
}