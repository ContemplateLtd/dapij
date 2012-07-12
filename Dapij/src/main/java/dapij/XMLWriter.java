/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dapij;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;

/**
 *
 * @author Marcin Szymczak <mpszymczak@gmail.com>
 */
public class XMLWriter {
    
    /*
     * Writes the object creation info to an XML file
     */
    public static void snapshotToXml(File outputFile, Breakpoint b)
            throws IOException {
        String tab = "    ";
        String twoTabs = tab + tab;
        
        String tagElmsOp = "<elements>";
        String tagElmsCl = "</elements>";
        String tagElemOp = "<element>";
        String tagElemCl = "</element>";
        String tagClasOp = "<class>";
        String tagClasCl = "</class>";
        String tagMetdOp = "<method>";
        String tagMetdCl = "</method>";
        String tagOfstOp = "<offset>";
        String tagOfstCl = "</offset>";
        String tagThIdOp = "<thread_id>";
        String tagThIdCl = "</thread_id>";
        
        /* Initialize a file writer */
        FileOutputStream fos = new FileOutputStream(outputFile, false);
        OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
        PrintWriter pw = new PrintWriter(osw);

        /* Write the XML header */
        pw.println("<?xml version=\"1.0\" encoding=\"UTF-8\" " +
                "standalone=\"no\" ?>");
        
        /* Write the breakpoint details */
        pw.println("<SourceFile>" + b.getSourceFile() + "</SourceFile>");
        pw.println("<Line>" + b.getLine() + "</Line>");
        
        /* Enclose data for all objects in an <Elements> tag. */
        pw.println();
        pw.println(tab + tagElmsOp);

        /* Write the data for each object */
        for(InstanceCreationStats info :
                InstanceCreationTracker.INSTANCE.getValues()) {
            pw.println(tab + tagElemOp);
            pw.println(twoTabs + tagClasOp + info.getClazz().getName() +
                    tagClasCl);
            pw.println(twoTabs + tagMetdOp + info.getMethod() + tagMetdCl);
            pw.println(twoTabs + tagOfstOp + info.getOffset() + tagOfstCl);
            pw.println(twoTabs + tagThIdOp + info.getThreadId() + tagThIdCl);
            pw.println(tab + tagElemCl);
        }
        pw.println(tagElmsCl);
        
        pw.close();
        osw.close();
        fos.close();
    }
}
