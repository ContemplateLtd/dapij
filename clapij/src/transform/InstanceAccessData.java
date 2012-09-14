/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package transform;

/**
 * A container class for data collected upon access to instances.
 *
 * @author Marcin Szymczak <mpszymczak@gmail.com>
 */
public class InstanceAccessData implements InstanceEventData {

    private long objId;
    private long thdId;

    public InstanceAccessData(long objId, long thdId) {
        this.objId = objId;
        this.thdId = thdId;
    }

    public long getObjId() {
        return objId;
    }

    @Override
    public long getThdId() {
        return thdId;
    }

    @Override
    public String toString() {
        return "[ACC: id:" + String.valueOf(objId) + ", thd:"
               + String.valueOf(thdId) + "]";
    }
}
