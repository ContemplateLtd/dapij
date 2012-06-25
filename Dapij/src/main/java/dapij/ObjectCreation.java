/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dapij;

/**
 *
 * @author Marcin Szymczak <mpszymczak@gmail.com>
 */
public class ObjectCreation {
    
    private Class clazz;
    private String method;
    private int offset;
    private long threadId;
    
    public void ObjectCreation(Class clazz, String method, int offset, long threadId) {
        this.clazz = clazz;
        this.method = method;
        this.offset = offset;
        this.threadId = threadId;
    }
    
    @Override
    public String toString() {
        return clazz.getName();
    }
    
}
