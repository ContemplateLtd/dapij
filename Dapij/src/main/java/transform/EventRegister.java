/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package transform;

/**
 *
 * @author Nikolay Pulev <N.Pulev@sms.ed.ac.uk>
 */
public interface EventRegister {
    
    public void regCreation(Object key, Class clazz, String method, int offset,
            long threadId);
    
    public void regAccess(Object ref, long threadId);
    
    public void regBreakpt();
}
