/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package transform;

import comms.EventRecord;

/**
 *
 * @author Marcin Szymczak <mpszymczak@gmail.com>
 */
public class InstAccsData implements EventRecord {

    private int objId;
    private long thdId;

    public InstAccsData(int objId, long thdId) {
        this.objId = objId;
        this.thdId = thdId;
    }

    /**
     * @return the objId
     */
    public int getObjId() {
        return objId;
    }

    /**
     * @return the thdId
     */
    public long getThdId() {
        return thdId;
    }

    @Override
    public String toString() {
        return "[ACC: id:" + String.valueOf(objId) + ", thd:"
               + String.valueOf(thdId) + "]";
    }
}
