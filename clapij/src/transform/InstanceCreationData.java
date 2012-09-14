package transform;


/**
 * Container for info stored upon detection of newly created instances during
 * execution of instrumented client programs.
 *
 * @author Marcin Szymczak <mpszymczak@gmail.com>
 */
public class InstanceCreationData implements InstanceEventData {

    private long objId;
    private String className;
    private String method;
    private int offset;
    private long threadId;

    public InstanceCreationData(long objId, String className, String method, int offset,
            long threadId) {
        this.objId = objId;
        this.className = className;
        this.method = method;
        this.offset = offset;
        this.threadId = threadId;
    }

    @Override
    public String toString() {
        return ("[id: " + getObjId() + "; cls: " + className + "; mtd: " + method + "; ofs: "
                + offset + "; tId: " + threadId + "]");
    }

    /**
     * @return the objId
     */
    public long getObjId() {
        return objId;
    }

    /**
     * @param objId
     *            the objId to set
     */
    public void setObjId(long objId) {
        this.objId = objId;
    }

    /**
     * @return the className
     */
    public String getClassName() {
        return className;
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
    @Override
    public long getThdId() {
        return threadId;
    }

    /**
     * @param clazz
     *            the className to set
     */
    public void setClazz(String className) {
        this.className = className;
    }

    /**
     * @param method
     *            the method to set
     */
    public void setMethod(String method) {
        this.method = method;
    }

    /**
     * @param offset
     *            the offset to set
     */
    public void setOffset(int offset) {
        this.offset = offset;
    }

    /**
     * @param threadId
     *            the threadId to set
     */
    public void setThreadId(long threadId) {
        this.threadId = threadId;
    }
}
