/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dapij;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 *
 * @author Marcin Szymczak <mpszymczak@gmail.com>
 */
public class XMLWriter {
    
    /*
     * Writes the object creation info to an XML file
     */
    
    public static void writeDataToXml(String filename) throws IOException {
        
        /* Initialize the file writer */
        
        FileWriter fileWriter = new FileWriter(filename, false);
        PrintWriter filePrinter = new PrintWriter(fileWriter);
        
        /* Write the XML header */
        
        filePrinter.println("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\" ?>");
        
        /* Write the breakpoint details */
        
        filePrinter.println("<SourceFile>" + InstanceCreationVisitor.targetFile +"</SourceFile>");
        filePrinter.println("<Line>" + InstanceCreationVisitor.targetLine + "</Line>");
        
        
        /* Write the opening tag */
        
        filePrinter.println("<Elements>");
        
        /* Now write the object creation data for all objects on the map */
        
        String tab = "    ";
        String twoTabs = tab + tab;
        
        for(InstanceCreationStats info : InstanceCreationTracker.INSTANCE.getValues()) {
            
            /* Write the data for the given object */
            
            filePrinter.println(tab + "<Element>");
            filePrinter.println(twoTabs + "<Class>" + info.getClazz().getName() +"</Class>");
            filePrinter.println(twoTabs + "<Method>" + info.getMethod() +"</Method>");
            filePrinter.println(twoTabs + "<Offset>" + info.getOffset() +"</Offset>");
            filePrinter.println(twoTabs + "<ThreadId>" + info.getThreadId() +"</ThreadId>");
            filePrinter.println(tab + "</Element>");
        }
        
        
        /* Write the closing tag */
        
        filePrinter.println("</Elements>");
        
        /* Close the file writer */
        
        filePrinter.close();
        
    }
    
}
