/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dapij;

/**
 *
 * @author Nikolay Pulev <N.Pulev@sms.ed.ac.uk>
 */
public class Breakpoint {
    private int line;
    private String sourceFile;
    private boolean visited;
    private boolean writeToXML;   /* XML log */

    public Breakpoint(String filename, int line, boolean visited,
            boolean toXml) {
        this.sourceFile = filename;
        this.line = line;
        this.visited = visited;
        this.writeToXML = toXml;
    }
    
    public Breakpoint(String filename, int line, boolean toXml) {
        this(filename, line, false, toXml);
    }

    /**
     * @return the line
     */
    public int getLine() {
        return line;
    }

    /**
     * @param line the line to set
     */
    public void setLine(int line) {
        this.line = line;
    }

    /**
     * @return the sourceFile
     */
    public String getSourceFile() {
        return sourceFile;
    }

    /**
     * @param sourceFile the sourceFile to set
     */
    public void setSourceFile(String sourceFile) {
        this.sourceFile = sourceFile;
    }

    /**
     * @return the visited
     */
    public boolean isVisited() {
        return visited;
    }

    /**
     * @param visited the visited to set
     */
    public void setVisited(boolean visited) {
        this.visited = visited;
    }

    /**
     * @return the writeToXML
     */
    public boolean isWriteToXML() {
        return writeToXML;
    }

    /**
     * @param writeToXML the writeToXML to set
     */
    public void setWriteToXML(boolean writeToXML) {
        this.writeToXML = writeToXML;
    }
}
