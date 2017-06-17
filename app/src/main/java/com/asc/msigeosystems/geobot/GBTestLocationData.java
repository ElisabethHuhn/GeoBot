package com.asc.msigeosystems.geobot;

/**
 * Created by Elisabeth Huhn on 12/7/2016.
 * Holds test locations for Collect Points
 */

class GBTestLocationData {
    private int    mLatitudeDegrees;
    private int    mLatitudeMinutes;
    private double mLatitudeSeconds;
    private int    mLongitudeDegrees;
    private int    mLongitudeMinutes;
    private double mLongitudeSeconds;
    private double mGeoid;
    private double mNorthing;
    private double mEasting;
    private double mElevation;



    /*-**********************************/
    /*-******* Static Methods   *********/
    /*-**********************************/



    /*-**********************************/
    /*-******* Constructors     *********/
    /*-**********************************/
    GBTestLocationData() {
        initializeDefaultData();

    }


    /*-**********************************/
    /*-******* Setters/Getters  *********/
    /*-**********************************/

    int    getLatitudeDegrees()                  { return mLatitudeDegrees; }
    void   setLatitudeDegrees(int    latitude)   { mLatitudeDegrees = latitude; }

    int    getLatitudeMinutes()                  { return mLatitudeMinutes; }
    void   setLatitudeMinutes(int    latitude)   { mLatitudeMinutes = latitude; }

    double getLatitudeSeconds()                  { return mLatitudeSeconds; }
    void   setLatitudeSeconds(double latitude)   { mLatitudeSeconds = latitude; }

    int    getLongitudeDegrees()                 { return mLongitudeDegrees; }
    void   setLongitudeDegrees(int    longitude) { mLongitudeDegrees = longitude; }

    int    getLongitudeMinutes()                 { return mLongitudeMinutes; }
    void   setLongitudeMinutes(int    longitude) { mLongitudeMinutes = longitude; }

    double getLongitudeSeconds()                 { return mLongitudeSeconds; }
    void   setLongitudeSeconds(double longitude) { mLongitudeSeconds = longitude; }

    double getGeoid()                     {   return mGeoid;    }
    void   setGeoid(double geoid)         {     mGeoid = geoid;    }

    double getNorthing()                  { return mNorthing; }
    void   setNorthing(double northing)   { mNorthing = northing; }

    double getEasting()                   { return mEasting; }
    void   setEasting(double easting)     { mEasting = easting; }

    double getElevation()                 { return mElevation; }
    void setElevation(double elevation)   { mElevation = elevation; }


    //I know this isn't necessary, but it never hurts to make things explicit
    private void initializeDefaultData(){
        mLatitudeDegrees  = 0;
        mLatitudeMinutes  = 0;
        mLatitudeSeconds  = 0d;
        mLongitudeDegrees = 0;
        mLongitudeMinutes = 0;
        mLongitudeSeconds = 0d;
        mNorthing         = 0d;
        mEasting          = 0d;
        mElevation        = 0d;
    }

}
