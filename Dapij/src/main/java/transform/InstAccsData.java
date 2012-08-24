/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package transform;


/**
 *
 * @author Marcin Szymczak <mpszymczak@gmail.com>
 */
public class InstAccsData implements InstEventData {

    private long objId;
    private long thdId;

    public InstAccsData(long objId, long thdId) {
        this.objId = objId;
        this.thdId = thdId;
    }

    /**
     * @return the objId
     */
    public long getObjId() {
        return objId;
    }

    /**
     * @return the thdId
     */
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
