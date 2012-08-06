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
    private boolean mainChldFst; /* Main class package load policy. */
    private boolean tstChldFst; /* Test class package load policy. */
    private boolean tstIsInstrum; /* Test class package instrumentation policy */

    /**
     * @param mainChldFst
     *            A boolean indicating whether to load pkg main classes in a
     *            child or parent (or system if no parent) class loader.
     * @param tstChldFst
     *            A boolean indicating whether to load pkg test classes in a
     *            child or parent (or system if no parent) class loader.
     * @param tstIsInstrum
     *            A boolean that indicates whether to instrument package test
     *            classes or not.
     */
    public PkgLdPolicy(boolean mainChldFst, boolean tstChldFst, boolean tstIsInstrum) {
        this.mainChldFst = mainChldFst;
        this.tstChldFst = tstChldFst;
        this.tstIsInstrum = tstIsInstrum;
    }

    /**
     * @return The boolean flag denoting whether to load main classes from this
     *         package in child or parent (or system if no parent) class loader.
     */
    public boolean isMainChldFst() {
        return mainChldFst;
    }

    /**
     * @param mainChldFst
     *            Sets the boolean flag denoting whether to load main classes
     *            from this package in child or parent (or system if no parent)
     *            class loader.
     */
    public void setMainChldFst(boolean mainChldFst) {
        this.mainChldFst = mainChldFst;
    }

    /**
     * @return The boolean flag denoting whether to load test classes from this
     *         package in child or parent (or system if no parent) class loader.
     */
    public boolean isTstChldFst() {
        return tstChldFst;
    }

    /**
     * @param tstChldFst
     *            Sets the boolean flag denoting whether to load main classes
     *            from this package in child or parent (or system if no parent)
     *            class loader.
     */
    public void setTstChldFst(boolean tstChldFst) {
        this.tstChldFst = tstChldFst;
    }

    /**
     * @return The value of the flag determining whether to instrument or not.
     */
    public boolean isTstInstr() {
        return (tstIsInstrum);
    }

    /**
     * @param tstIsInstrum
     *            The boolean flag denoting whether to instrument test classes
     *            from this package or not.
     */
    public void isTstInstr(boolean tstIsInstrum) {
        this.tstIsInstrum = tstIsInstrum;
    }
}
