/*
 * TODO: doc comment
 */
package transform;

/**
 * Container for info stored upon detection of newly created instances during
 * execution of instrumented client programs.
 * 
 * @author Marcin Szymczak <mpszymczak@gmail.com>
 */
public class InstCreatStats {
    
    private Class clazz;
    private String method;
    private int offset;
    private long threadId;
    
    public InstCreatStats(Class clazz, String method, int offset,
            long threadId) {
        this.clazz = clazz;
        this.method = method;
        this.offset = offset;
        this.threadId = threadId;
    }
    
    @Override
    public String toString() {
        return ("[cls: " + clazz.getName() + "; mtd: " + method + "; ofs: " +
                String.valueOf(offset) + "; tId: " + String.valueOf(threadId) +
                "]");
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