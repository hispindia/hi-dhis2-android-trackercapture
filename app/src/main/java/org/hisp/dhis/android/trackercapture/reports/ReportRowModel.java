package org.hisp.dhis.android.trackercapture.reports;

/**
 * Created by xelvias on 11/22/17.
 */

public class ReportRowModel {
    long trackedEntityInstanceIdLocal;
    String sin;
    boolean formB;
    boolean formC;
    boolean formD;
    boolean formE;
    boolean formF;
    boolean formG1;
    boolean formG2;

    public String getSin() {
        return sin;
    }

    public void setSin(String sin) {
        this.sin = sin;
    }

    public boolean isFormC() {
        return formC;
    }

    public void setFormC(boolean formC) {
        this.formC = formC;
    }

    public boolean isFormD() {
        return formD;
    }

    public void setFormD(boolean formD) {
        this.formD = formD;
    }

    public boolean isFormE() {
        return formE;
    }

    public void setFormE(boolean formE) {
        this.formE = formE;
    }

    public boolean isFormF() {
        return formF;
    }

    public void setFormF(boolean formF) {
        this.formF = formF;
    }

    public boolean isFormB() {
        return formB;
    }

    public void setFormB(boolean formB) {
        this.formB = formB;
    }

    public boolean isFormG1() {
        return formG1;
    }

    public void setFormG1(boolean formG1) {
        this.formG1 = formG1;
    }

    public boolean isFormG2() {
        return formG2;
    }

    public void setFormG2(boolean formG2) {
        this.formG2 = formG2;
    }

    public long getTrackedEntityInstanceIdLocal() {
        return trackedEntityInstanceIdLocal;
    }

    public void setTrackedEntityInstanceIdLocal(long trackedEntityInstanceIdLocal) {
        this.trackedEntityInstanceIdLocal = trackedEntityInstanceIdLocal;
    }
}
