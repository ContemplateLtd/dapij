/*
 * TODO: doc comment.
 */
package testutils;

/**
 * This is a container for package settings regarding loading and instrumenting
 * policies (per package). The settings include two booleans that determine
 * whether classes from package should be loaded using a child-first or in
 * parent-first methods, and whether the class should be instrumented or not.
 * 
 * @author Nikolay Pulev <N.Pulev@sms.ed.ac.uk>
 */
public class PkgLdPolicy {
    private boolean isChldFst;
    private boolean isInstrum;
    
    /**
     * @param ldInChild A boolean that indicates whether to load in a
     * child-first or parent (or system if no parent) class loader.
     * @param instrument A boolean that indicates whether to instrument or not.
     */
    public PkgLdPolicy(boolean ldInChild, boolean isInstrum) {
        this.isChldFst = ldInChild;     /* true - child; false - parent/sys */
        this.isInstrum = isInstrum;     /* true - instrument; false - don't */
    }

    /**
     * @return the boolean flag denoting whether to load classes from
     * this package in child or parent (or system if no parent) class loader
     */
    public boolean isChildFirst() {
        return isChldFst;
    }

    /**
     * @param ldInChild a boolean flag denoting whether to load classes from
     * this package in child or parent (or system if no parent) class loader
     */
    public void setChildFirst(boolean ldInChild) {
        this.isChldFst = ldInChild;
    }

    /**
     * @return the boolean flag denoting whether to instrument classes from
     * this package or not
     */
    public boolean isInstrumented() {
        return isInstrum;
    }

    /**
     * @param isInstrumented the boolean flag denoting whether to instrument
     * classes from this package or not
     */
    public void setInstrument(boolean isInstrumented) {
        this.isInstrum = isInstrumented;
    }
}