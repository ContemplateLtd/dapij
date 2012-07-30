/*
 * TODO: doc comment
 */
package agent;

import comms.AgentEventSrv;
import comms.CommsProto;
import java.lang.reflect.Modifier;
import transform.AccsEvent;
import transform.AccsEventLisnr;

/**
 * An access event listener that converts events to a format suitable for
 * network transfer & sends them to external subscribers via the agent's event
 * server.
 * 
 * @author Nikolay Pulev <N.Pulev@sms.ed.ac.uk>
 */
public class AccsEventNetSndr implements AccsEventLisnr {
    
    AgentEventSrv aes;
    
    public AccsEventNetSndr(AgentEventSrv aes) {
        this.aes = aes;
    }

    /* TODO: Implement, a dummy implementation for now. */
    @Override
    public void handleAccessEvent(AccsEvent e) {
        int objId;
        Object ref = e.getRef();
        long threadId = e.getThreadId();
        
        if((ref.getClass().getModifiers() & Modifier.PUBLIC)==0) {
             System.out.println("Can't access object: " + ref);
        }
        
        try {
            try {
                objId = ref.getClass().getField("__DAPIJ_ID").getInt(ref);
            } catch (IllegalAccessException ex) {
                
                /* Fields of anonymous classes cannot be accessed */
                objId = IDMap.INSTANCE.get(ref);
            }
        } catch(NoSuchFieldException ex) {
            objId = IDMap.INSTANCE.get(ref);
        }
        
        if(objId == -1) {
            System.out.println("Error: creation of " + ref + 
                    " had not been registered");
            return;
        }
        
        String msg = CommsProto.constructMsg("Object " + objId +
                " accessed from thread " + threadId);
        
        /* Send message to client if event server started */
        if (aes != null) {
            aes.sendEvent(msg);
        }
    }
}
