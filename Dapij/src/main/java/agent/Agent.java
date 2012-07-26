package agent;

import comms.AgentEventServer;
import comms.CommsProto;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 * @author Nikolay Pulev
 */
public class Agent {
    
    public static void premain(String argString, Instrumentation inst)
            throws IOException {
        handleArgs(argString);  /* format, read & processs arguments */
        final AgentEventServer aes = setupEventServer();
        /* For gracefully shutdown when user program ends. */
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                aes.shutdown();
            }
        });
        aes.start(); /* Start server. */
        inst.addTransformer(new transform.Transformer());
    }

    private static void handleArgs(String argString) {
        if (argString != null) {

            /* Split arglsit on one or more whitespaces */
            String[] args = argString.split("\\s+", 0);
            int i = 0;
            while (i < args.length) {
                
                /* Set output XML filename if provided. */
                if (args[i].equals("-o") && i + 1 < args.length) {
                    Settings.INSTANCE.setSett(Settings.SETT_XML_OUT, args[++i]);
                }
                
                /* If needed, add more arguments here. */
                //else if (args[i].equals(...)) {
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
    public static AgentEventServer setupEventServer() {
        ServerSocket srvSock = null;
        Socket conn = null;
        String nm = AgentEventServer.nm;
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
        
        AgentEventServer aes = new AgentEventServer(srvSock, conn);
        aes.setDaemon(true);
        Settings.INSTANCE.setEventServer(aes);
        
        return aes;
    }
}