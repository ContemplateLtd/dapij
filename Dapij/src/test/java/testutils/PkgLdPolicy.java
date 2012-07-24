/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package testutils;

import java.io.File;

/**
 * This is a container for pkg settings regarding loading and instrumenting
 * rules. The settings include pkg full path, whether the class is loaded in a
 * child-first or in the parent (or system) class loader, and whether the class
 * should be instrumented.
 * 
 * @author Nikolay Pulev <N.Pulev@sms.ed.ac.uk>
 */
public class PkgLdPolicy {
    private String pkgName;
    private File fullPath;
    private boolean ldInChild;
    private boolean isInstrumented;

    /**
     * 
     * @param fullPath A File object that contains the full path of the
     * package.
     * @param ldInChild A boolean that indicates whether to load in a
     * child-first or parent (or system if no parent) class loader.
     * @param instrument A boolean that indicates whether to instrument or not.
     */
    public PkgLdPolicy(String pkgName, File fullPath, boolean ldInChild,
            boolean isInstrumented) {
        this.pkgName = pkgName;
        this.fullPath = fullPath;       /* File obj containing pkg full path */
        this.ldInChild = ldInChild;     /* true - child; false - parent/sys */
        
        /* true - instrument; false - don't */
        this.isInstrumented = isInstrumented;
    }

    /**
     * @return the boolean flag denoting whether to load classes from
     * this package in child or parent (or system if no parent) class loader
     */
    public boolean isChildFirst() {
        return ldInChild;
    }

    /**
     * @param ldInChild a boolean flag denoting whether to load classes from
     * this package in child or parent (or system if no parent) class loader
     */
    public void setChildFirst(boolean ldInChild) {
        this.ldInChild = ldInChild;
    }

    /**
     * @return the boolean flag denoting whether to instrument classes from
     * this package or not
     */
    public boolean isInstrumented() {
        return isInstrumented;
    }

    /**
     * @param isInstrumented the boolean flag denoting whether to instrument
     * classes from this package or not
     */
    public void setInstrument(boolean isInstrumented) {
        this.isInstrumented = isInstrumented;
    }

    /**
     * @return the full path to the package as a File
     */
    public File getPkgPath() {
        return fullPath;
    }

    /**
     * @param fullPath the full path to the package as a File
     */
    public void setPkgPath(File fullPath) {
        this.fullPath = fullPath;
    }

    /**
     * @return the name of the package as a String
     */
    public String getName() {
        return pkgName;
    }

    /**
     * @param name the name of the package as a String
     */
    public void setName(String name) {
        this.pkgName = name;
    }
}
