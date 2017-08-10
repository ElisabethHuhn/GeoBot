package com.asc.msigeosystems.geobot;

/**
 * Created by Elisabeth Huhn on 11/20/2016.
 *
 * This is the superclass of all coordinate types
 * It doesn't do much, as it will be subtypes that are actually instantiated.
 * This level just provides the capability for the subtypes to
 * identify the type of the instance that is actually instantiated
 */

abstract class GBCoordinate {

    static final String sCoordinateTypeUnknown = "Unknown";
    static final String sCoordinateTypeWGS84 = "WGS84 G1762";
    static final String sCoordinateTypeSPCS  = "US State Plane Coordinates";
    static final String sCoordinateTypeUTM   = "UTM";
    static final String sCoordinateTypeNAD83 = "NAD83 2011";

    //This is the order of the items in the Project UI spinner
    //Unfortunately, this is a hard coded order.
    // The ProjectEditFragment spinner must be coordinated with any changes
    static final int sCoordinateDBTypeUnknown = -1;
    static final int sCoordinateDBTypeWGS84 = 0;
    static final int sCoordinateDBTypeSPCS  = 1;
    static final int sCoordinateDBTypeUTM   = 2;
    static final int sCoordinateDBTypeNAD83 = 3;

    static final String sCoordinateTypeClassUnknown = "GBCoordinate";
    static final String sCoordinateTypeClassWGS84 = "GBCoordinateWGS84";
    static final String sCoordinateTypeClassSPCS  = "GBCoordinateSPCS";
    static final String sCoordinateTypeClassUTM   = "GBCoordinateUTM";
    static final String sCoordinateTypeClassNAD83 = "GBCoordinateNAD83";

    static final int sUNKWidgets = 0;
    static final int sLLWidgets = 1;
    static final int sNEWidgets = 2;


    //Tags for shared pref settings for whether the property was exported last time
    static final String sCoordinateTimeExportTag        = "COORD_TIME_EXPORT";
    static final String sCoordinateNorthingExportTag    = "COORD_NORTHING_EXPORT";
    static final String sCoordinateEastingExportTag     = "COORD_EASTING_EXPORT";
    static final String sCoordinateElevationExportTag   = "COORD_ELE_EXPORT";
    static final String sCoordinateGeoidExportTag       = "COORD_GEOID_EXPORT";
    static final String sCoordinateScaleFactorExportTag = "COORD_SF_EXPORT";
    static final String sCoordinateCAngleExportTag      = "COORD_CA_EXPORT";
    static final String sCoordinateLatExportTag         = "COORD_LAT_EXPORT";
    static final String sCoordinateLngExportTag         = "COORD_LNG_EXPORT";


    /* *********************************************************/
    /* ***    Variables common to ALL coordinates        *******/
    /* *********************************************************/
    private long    mCoordinateID; //All coordinates have a DB ID
    private long    mProjectID; //May or may not describe a point
    private long    mPointID;   //These will be null if not describing a point
    private int     mCoordinateDBType;

    private long    mTime; //time coordinate taken in milliseconds

    private double  mElevation; //Orthometric Elevation in Meters
    private double  mGeoid;     //Mean Sea Level in Meters

    private double  mScaleFactor;
    private double  mConvergenceAngle;

    private boolean mValidCoordinate = true;
    private boolean mIsFixed         = true;

    private CharSequence mDatum = ""; //eg WGS84


    /* *********************************************************/
    /* ***    Static methods                             *******/
    /* *********************************************************/
    static int getNextCoordinateID(){
        return (int) (System.currentTimeMillis() & 0xfffffff);
    }

    /* *****************************************************************/
    /*      Return codes are static int's:                             */
    /*                       GBCoordinate.sUNKWidgets                  */
    /*                       GBCoordinate.sLLWidgets                   */
    /*                       GBCoordinate.sNEWidgets                   */
    /* *****************************************************************/
    static int getCoordinateCategoryFromProjectID(long projectID){
        if (projectID == GBUtilities.ID_DOES_NOT_EXIST)return GBCoordinate.sUNKWidgets;
        int returnCode = GBCoordinate.sUNKWidgets;


        GBProject project = GBProjectManager.getInstance().getProject(projectID);

        CharSequence coordinateType;
        if (project != null) {
            coordinateType = project.getProjectCoordinateType();
        } else {
            coordinateType = null;
        }


        if (!GBUtilities.isEmpty(coordinateType)){
            if (coordinateType.equals(GBCoordinate.sCoordinateTypeWGS84) ||
                coordinateType.equals(GBCoordinate.sCoordinateTypeNAD83) ){

                returnCode = GBCoordinate.sLLWidgets;

            } else if (coordinateType.equals(GBCoordinate.sCoordinateTypeUTM) ||
                       coordinateType.equals(GBCoordinate.sCoordinateTypeSPCS) ){

                returnCode = GBCoordinate.sNEWidgets;
            }
        }
        return returnCode;
    }




    /* *********************************************************/
    /* ***    Setters and Getters for common variables   *******/
    /* *********************************************************/



     long getCoordinateID(){return mCoordinateID;}
     void setCoordinateID(long coordinateID){this.mCoordinateID = coordinateID;}


    long  getProjectID()              { return mProjectID; }
    void setProjectID(long projectID) { mProjectID = projectID; }

    long  getPointID()            { return mPointID; }
    void setPointID(long pointID) { mPointID = pointID; }


    int  getCoordinateDBType()            { return mCoordinateDBType; }
    void setCoordinateDBType(int coordinateDBType) { mCoordinateDBType = coordinateDBType; }

    CharSequence getCoordinateType(){

        CharSequence returnCode = GBCoordinate.sCoordinateTypeUnknown;

        switch (getCoordinateDBType()) {
            case GBCoordinate.sCoordinateDBTypeWGS84:
                returnCode = GBCoordinate.sCoordinateTypeWGS84;
                break;
            case GBCoordinate.sCoordinateDBTypeSPCS:
                returnCode = GBCoordinate.sCoordinateTypeSPCS;
                break;
            case GBCoordinate.sCoordinateDBTypeUTM:
                returnCode = GBCoordinate.sCoordinateTypeUTM;
                break;
            case GBCoordinate.sCoordinateDBTypeNAD83:
                returnCode = GBCoordinate.sCoordinateTypeNAD83;
                break;
        }
        return returnCode;
    }


    long getTime()              {  return mTime;    }
    void setTime(long time)     {  mTime = time;  }


    double getElevation()       {  return mElevation;   }
    void setElevation(double elevation) { mElevation = elevation;   }
    double getElevationFeet() {return GBUtilities.convertMetersToFeet(mElevation); }
    double getElevationIFeet() {return GBUtilities.convertMetersToIFeet(mElevation);}

    double getGeoid()           {  return mGeoid; }
    double getGeoidFeet() { return GBUtilities.convertMetersToFeet(mGeoid);}
    double getGeoidIFeet() {return GBUtilities.convertMetersToIFeet(mGeoid);}
    void setGeoid(double geoid) { mGeoid = geoid;  }

    double getScaleFactor()       { return mScaleFactor;       }
    void setScaleFactor(double scaleFactor) { mScaleFactor = scaleFactor; }

    double getConvergenceAngle()       { return mConvergenceAngle;       }
    int    getConvergenceAngleDegree()  { return GBUtilities.getDegrees(mConvergenceAngle);  }
    int    getConvergenceAngleMinute()  { return GBUtilities.getMinutes(mConvergenceAngle);  }
    double getConvergenceAngleSecond()  { return GBUtilities.getSeconds(mConvergenceAngle);  }
    void setConvergenceAngle(double convergenceAngle) { mConvergenceAngle = convergenceAngle; }


    void    setValidCoordinate(boolean validCoordinate){ this.mValidCoordinate = validCoordinate;}
    boolean isValidCoordinate() {
        return mValidCoordinate;
    }

    void    setIsFixed(boolean isFixed){this.mIsFixed = isFixed;}
    boolean isFixed()                  {return mIsFixed;}

    CharSequence getDatum() { return mDatum;    }
    void setDatum(CharSequence datum)   { mDatum = datum;  }


    /* *********************************************************/
    /* ***     Methods common to ALL coordinates         *******/
    /* *********************************************************/

    protected void initializeDefaultVariables() {
        //set all variables with defaults, so that none are null
        //I know that one does not have to initialize int's etc, but
        //to be explicit about the initialization, do it anyway

        mCoordinateID     = GBUtilities.ID_DOES_NOT_EXIST;

        mProjectID        = GBUtilities.ID_DOES_NOT_EXIST; //assume does not describe a point
        mPointID          = GBUtilities.ID_DOES_NOT_EXIST;
        mCoordinateDBType = sCoordinateDBTypeUnknown;



        mTime             = 0; //time coordinate taken
        mValidCoordinate  = false;

        mElevation        = 0d;
        mGeoid            = 0d;
        mConvergenceAngle = 1d; //
        mScaleFactor      = 1d;


    }

    protected double getMeters(String distString, int distUnits){
        //The distString is in the units defined by the UI setting in the project
        //Must convert to meters to store in the object or the DB
        double distValue;
        // TODO: 7/21/2017 need to get rid of commas, and be able to differentiate between 6,0 AND 6.0
        if (GBUtilities.isEmpty(distString)) return 0.d;

        distValue = Double.valueOf(distString);
        //if (distUnits == GBProject.sMeters)//do nothing, the value is already in meters

        if (distUnits == GBProject.sFeet){
            distValue = GBUtilities.convertFeetToMeters(distValue);
        }
        if (distUnits == GBProject.sIntFeet){
            distValue = GBUtilities.convertFeetToMeters(distValue);
        }

        return distValue;

    }



}
