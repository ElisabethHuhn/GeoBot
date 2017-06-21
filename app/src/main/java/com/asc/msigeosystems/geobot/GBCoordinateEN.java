package com.asc.msigeosystems.geobot;

/**
 * Created by Elisabeth Huhn on 5/25/2016.
 *
 * This class is part of the Coordinate's hierarchy
 * This is the super class of any Coordinate expressed with
 * Easting/Northing rectangular coordinate systems
 *
 * The class knows how to:
 *   A) store all attributes required of Easting/Northing coordinate systems.
 *   B) convert from any other coordinate system to this one
 *
 */
abstract class GBCoordinateEN extends GBCoordinate {

    static final String sDatum = "EN";

    /* ***************************************************/
    /* ******    Attributes stored in the DB     *********/
    /* ***************************************************/



    protected   double       mEasting;
    protected   double       mNorthing;
    protected   double       mElevation;
    protected   int          mZone;        //1-60
    protected CharSequence mDatum = "WGS84"; //eg WGS84
    protected   double       mConvergence; //
    protected   double       mScale;





    /* ************************************************************
     * Setters and getters
     *
     *
     ****************************************************************/


    double getEasting()     { return mEasting;  }
    double getNorthing()    { return mNorthing; }
    double getElevation()   { return mElevation; }

    double getEastingFeet() { return GBUtilities.convertMetersToFeet(mEasting);  }
    double getNorthingFeet(){ return GBUtilities.convertMetersToFeet(mNorthing); }
    double getElevationFeet(){return GBUtilities.convertMetersToFeet(mElevation);}

    int    getZone()        { return mZone;     }
    CharSequence getDatum() { return mDatum;    }
    double getConvergence() { return mConvergence; }
    double getScale()       { return mScale;    }


    void setEasting(double easting)   { mEasting = easting;  }
    void setNorthing(double northing) { mNorthing = northing;}
    void setElevation(double elevation) { mElevation = elevation; }
    void setZone(int zone)              { mZone = zone;   }
    void setDatum(CharSequence datum)   { mDatum = datum;  }
    void setConvergence(double convergence) { mConvergence = convergence; }
    void setScale(double scale)         { mScale = scale; }



    //default constructor
    /* ************************************************************/
    /* ************************************************************/
    /*       Vanilla constructor, default values only             */
    /*                                                            */
    /* ************************************************************/
    /* ************************************************************/
    GBCoordinateEN(){
            initializeDefaultVariables();
        }

    protected void initializeDefaultVariables(){
        //set all variables with defaults, so that none are null
        //I know that one does not have to initialize int's etc, but
        //to be explicit about the initialization, do it anyway

        //initialize all variables common to all coordinates
        super.initializeDefaultVariables();

        //initialize all variables common to EN coordinates


        mEasting     = 0d;
        mNorthing    = 0d;
        mZone        = 0;        //1-60
        //mHemisphere  = 'N';  //N or S
        //mLatBand     = 'A';
        mValidCoordinate = false;
        mDatum       = sDatum; //eg WGS84
        mConvergence = 1d; //
        mScale       = 1d;

    }



}
