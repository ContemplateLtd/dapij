/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dapij;

/**
 *
 * @author Marcin Szymczak <mpszymczak@gmail.com>
 */
public class InstanceCreationStats {
    
    private Class clazz;
    private String method;
    private int offset;
    private long threadId;
    
    public InstanceCreationStats(Class clazz, String method, int offset,
            long threadId) {
    //public InstanceCreationStats(int offset) {
        this.setClazz(clazz);
        this.setMethod(method);
        this.setOffset(offset);
        this.setThreadId(threadId);
    }
    
    @Override
    public String toString() {
        return ("type: " + clazz.getName() + ", creator: " + method
                + ", offset: " + String.valueOf(offset) + ", thread_id: "
                + threadId);
    }

    /**
     * @return the clazz
     */
    public Class getClazz() {
        return clazz;
    }

    /**
     * @return the method
     */
    public String getMethod() {
        return method;
    }

    /**
     * @return the offset
     */
    public int getOffset() {
        return offset;
    }

    /**
     * @return the threadId
     */
    public long getThreadId() {
        return threadId;
    }

    /**
     * @param clazz the clazz to set
     */
    public void setClazz(Class clazz) {
        this.clazz = clazz;
    }

    /**
     * @param method the method to set
     */
    public void setMethod(String method) {
        this.method = method;
    }

    /**
     * @param offset the offset to set
     */
    public void setOffset(int offset) {
        this.offset = offset;
    }

    /**
     * @param threadId the threadId to set
     */
    public void setThreadId(long threadId) {
        this.threadId = threadId;
    }
}
