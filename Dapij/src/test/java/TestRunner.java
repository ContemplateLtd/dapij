/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

/**
 *
 * @author Nikolay Pulev <N.Pulev@sms.ed.ac.uk>
 */
public class TestRunner {
    
    public static void main(String args[]) {
        Result result = JUnitCore.runClasses(SingleThreadTest.class);
        for (Failure failure : result.getFailures()) {
                System.out.println(failure.toString());
        }
    }       
}
