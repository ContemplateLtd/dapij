package dapij;

import comms.CommsProto;
import comms.EventClient;
import comms.EventServer;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.ProtectionDomain;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

/**
 *
 * @author Nikolay Pulev
 */
public class Dapij implements ClassFileTransformer {
    
    @Override
    public byte[] transform(ClassLoader loader, String className,
            Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
            byte[] classfileBuffer) throws IllegalClassFormatException {
        
        /* Do not instrument agent classes */
        // TODO: should remove all other classes from this blacklist!
        // Unfortunately, code breaks when any of these is removed.
        if (    className.startsWith("dapij/") ||
                className.startsWith("comms/") ||
                className.startsWith("com/google/common/collect/") ||
                
                /*
                 * TODO: FIX: the lines below need to be ucommented. They
                 * throw the same error. Further investigation needed.
                 */
                className.startsWith("java/io/") ||
                className.startsWith("sun/net/") ||
                className.startsWith("java/util/")) {
            System.out.println("Did not instument " + className + "!");
            return classfileBuffer;
        }
        
        System.out.println("Instrumenting " + className + " ...");
        return transformClass(classfileBuffer);
    }

    /**
     * Reads and instruments class bytecode using a StatsCollector visitor.
     */
    public static byte[] transformClass(byte[] classfileBuffer) {
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        /*
         * Uncomment lines below and pass tcv to the constructor of sc_visitor
         * to print the instrumented bytecode on System.out.
         */
        //TraceClassVisitor tcv = new TraceClassVisitor(writer,
        //        new PrintWriter(System.out));
        
        ClassVisitor sc_visitor = new StatsCollector(writer);
        new ClassReader(classfileBuffer).accept(sc_visitor, 0);
        return writer.toByteArray();
        //return classfileBuffer;//writer.toByteArray();
    }

    public static void setupEventServer() {
        ServerSocket srvSock = null;
        Socket conn = null;
        
        /* Attempt to connect 3 times */
        for (int i = 0; i <= 3; i++) {
            try {
                srvSock = new ServerSocket(CommsProto.port);
                conn = srvSock.accept();
            } catch (IOException ex) {
                continue;
            }
        }
        
        if (srvSock == null || conn == null) {
            System.out.println("Could not start event server! " +
                    "Execution abroted.");
            System.exit(1);
        }
        
        final EventServer es = new EventServer(srvSock, conn);
        es.setDaemon(true);
        Settings.INSTANCE.setEventServer(es);
        
        /* For gracefully shutdown when user program ends. */
        Thread sh = new Thread() {
            @Override
            public void run() {
                es.shutdown();
            }
        };
        Runtime.getRuntime().addShutdownHook(sh);

        es.start(); /* Start server. */
    }
    
    public static void setupEventClient() {
        final EventClient ec = new EventClient(CommsProto.host,
                CommsProto.port);
        ec.setDaemon(true);
        
        /* For gracefully shutdown when user program ends. */
        Thread sh = new Thread() {
            @Override
            public void run() {
                ec.shutdown();
            }
        };
        Runtime.getRuntime().addShutdownHook(sh);
        
        ec.start(); /* Start client. */
    }
    
    public static void premain(String argString, Instrumentation inst)
            throws IOException {
        System.out.println("PREMAIN");
        int i = 0;
        
        /* Split arglsit on one or more whitespaces */
        if (argString != null) {
                String[] args = argString.split("\\s+", 0);

            while (i < args.length) {
                
                /* If output XML filename passed. */
                if (args[i].equals("-o") && i + 1 < args.length) {
                    Settings.INSTANCE.setProp(Settings.XML_OUT_SETT, args[++i]);
                }
                
                /* Add more arguments here if needed */
                //else if (args[i].equals(...)) {
                //...
                //}
                i++;
            }
        }
        
        // TODO: remove, load breakpoints from config
        Settings.INSTANCE.addBreakpt(
                new Breakpoint("HelloAzura.java", 38, true));
        
        /* Start a client to receive and process events. */
        /* Start a server for receiving & forwarding events to one client. */
        setupEventClient(); // waits for server to start // TODO: start client independently?
        setupEventServer(); // waits for client to connect
        
        inst.addTransformer(new Dapij());
        System.out.println("ENDPREMAIN");
    }
}