package com.asc.msigeosystems.geobot;

import android.os.Bundle;

/**
 * Created by Elisabeth Huhn on 6/18/2017.
 * Repository of information gathered to create a new point
 *
 */

public class GBPointQualityToken {
    //*+****************************************/
    //*+*        Static constants           ****/
    //*+****************************************/
    //Quality Variables
    private static final String IN_FIX     = "InFix";
    private static final String HDOP_VALUE = "HdopVal";
    private static final String VDOP_VALUE = "VdopVal";
    private static final String TDOP_VALUE = "TdopVal";
    private static final String PDOP_VALUE = "PdopVal";
    private static final String HRMS_VALUE = "HrmsVal";
    private static final String VRMS_VALUE = "VrmsVal";


    //*+****************************************/
    //*+*        Instance variables         ****/
    //*+****************************************/

    private int          mInFix = 0;
    private double       mHdop = 0d;
    private double       mVdop = 0d;
    private double       mTdop = 0d;
    private double       mPdop = 0d;
    private double       mHrms = 0d;
    private double       mVrms = 0d;


    //*+****************************************/
    //*+*       Constructor                 ****/
    //*+****************************************/

    public GBPointQualityToken() {
        initializeVariables();
    }

    public GBPointQualityToken (Bundle savedInstanceState){
        mInFix = savedInstanceState.getInt(IN_FIX);
        mHdop  = savedInstanceState.getDouble(HDOP_VALUE);
        mVdop  = savedInstanceState.getDouble(VDOP_VALUE);
        mTdop  = savedInstanceState.getDouble(TDOP_VALUE);
        mPdop  = savedInstanceState.getDouble(PDOP_VALUE);
        mHrms  = savedInstanceState.getDouble(HRMS_VALUE);
        mVrms  = savedInstanceState.getDouble(VRMS_VALUE);
    }

    private void initializeVariables(){
        mInFix = 0;
        mHdop  = 0d;
        mVdop  = 0d;
        mTdop  = 0d;
        mPdop  = 0d;
        mHrms  = 0d;
        mVrms  = 0d;
    }

    //*+****************************************/
    //*+*       Setters and Getters        ****/
    //*+****************************************/

    public int getInFix() {
        return mInFix;
    }
    public void setInFix(int inFix) {
        mInFix = inFix;
    }

    public double getHdop() {
        return mHdop;
    }
    public void setHdop(double hdop) {
        mHdop = hdop;
    }

    public double getVdop() {
        return mVdop;
    }
    public void setVdop(double vdop) {
        mVdop = vdop;
    }

    public double getTdop() {
        return mTdop;
    }
    public void setTdop(double tdop) {
        mTdop = tdop;
    }

    public double getPdop() {
        return mPdop;
    }
    public void setPdop(double pdop) {
        mPdop = pdop;
    }

    public double getHrms() {
        return mHrms;
    }
    public void setHrms(double hrms) {
        mHrms = hrms;
    }

    public double getVrms() {
        return mVrms;
    }
    public void setVrms(double vrms) {
        mVrms = vrms;
    }


    Bundle saveState(Bundle outState){
        outState.putInt   (IN_FIX, mInFix);
        outState.putDouble(HDOP_VALUE, mHdop);
        outState.putDouble(VDOP_VALUE, mVdop);
        outState.putDouble(TDOP_VALUE, mTdop);
        outState.putDouble(PDOP_VALUE, mPdop);
        outState.putDouble(HRMS_VALUE, mHrms);
        outState.putDouble(VRMS_VALUE, mVrms);
        return outState;
    }
}
