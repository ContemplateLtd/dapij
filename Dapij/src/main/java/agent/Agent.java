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
        handleArgs(argString);
        //setupEventServer(); /* forwards runtime events to a network client */
        inst.addTransformer(new transform.Transformer());
    }

    private static void handleArgs(String argString) {
                /* Split arglsit on one or more whitespaces */
        if (argString != null) {

            /* Split arglsit on one or more whitespaces */
            String[] args = argString.split("\\s+", 0);
            int i = 0;
            while (i < args.length) {
                
                /* Set output XML filename if provided. */
                if (args[i].equals("-o") && i + 1 < args.length) {
                    Settings.INSTANCE.setSett(Settings.SETT_XML_OUT, args[++i]);
                }
                
                /* Add more arguments here if needed */
                //else if (args[i].equals(...)) {
                //...
                //}
                i++;
            }
        }
    }
    
    public static void setupEventServer() {
        ServerSocket srvSock = null;
        Socket conn = null;
        
        /* Attempt to connect 3 times */ // TODO: load from config
        for (int i = 1; i <= 3; i++) {
            try {
                System.out.println("premain: [" + i + "] AgentEventServer: " +
                        "Binding on port '" + CommsProto.port + "'.");
                srvSock = new ServerSocket(CommsProto.port);
                System.out.println("premain: AgentEventServer: Done.");
                System.out.println("premain: AgentEventServer: Listening for" +
                        " clients ...");
                conn = srvSock.accept();
                System.out.println("premain: AgentEventServer: Client [" +
                            conn.getRemoteSocketAddress() + "] connected ...");
                break;
            } catch (IOException ex) {
                System.out.println("premain: AgentEventServer: Could not " +
                        "connect, trying again ...");
                continue;
            }
        }
        
        if (srvSock == null || conn == null) {
            throw new RuntimeException("premain: AgentEventServer: Could not" +
                    "start! Execution abroted.");
        }
        
        final AgentEventServer aes = new AgentEventServer(srvSock, conn);
        aes.setDaemon(true);
        Settings.INSTANCE.setEventServer(aes);

        /* For gracefully shutdown when user program ends. */
        Thread sh = new Thread() {
            @Override
            public void run() {
                aes.shutdown();
            }
        };
        Runtime.getRuntime().addShutdownHook(sh);
        aes.start(); /* Start server. */
    }
}