package com.asc.msigeosystems.geobot;

/**
 * Created by Elisabeth Huhn on 6/17/2016.
 *
 * This class is NOT part of the Coordinate's hierarchy
 * But it is only meant as a temporary
 * holder of statistical data such as standard deviation
 *
 * NOTE all fields are double, even Degrees and Minutes
 *
 */


class GBCoordinateMean {
    //

    /* ***************************************************/
    /* ******    Attributes                      *********/
    /* ***************************************************/

    //ID is necessary for the DB
    private long mCoordinateID;

    //Project of the meaning process
    private long mProjectID;
    private long mPointID;

    //total number of readings since meaning started
    private int mRawReadings;

    //number of readings that have been used in the mean
    private int mMeanedReadings;

    //number of the readings included in the mean that are of quality fixed
    // (per Project Settings Quality definition)
    private int mFixedReadings;


    //Latitude in DD and DMS formats
    private double mLatitude;
    private double mLatitudeDegree;
    private double mLatitudeMinute;
    private double mLatitudeSecond;
    private double mLatitudeStdDev;


    //Longitude in DD and DMS formats
    private double mLongitude;
    private double mLongitudeDegree;
    private double mLongitudeMinute;
    private double mLongitudeSecond;
    private double mLongitudeStdDev;

    private double mElevation; //Orthometric Elevation in Meters
    private double mElevationStdDev;

    private double mGeoid;     //Mean Sea Level in Meters

    private int    mSatellites; //Number of satellites in the fix

    private boolean mValidCoordinate = true;
    private boolean mIsFixed = true;
    private boolean mType = true; //true = Easting/Northing, false = Latitude/Longitude


    /* ***************************************************/
    /* ******    Static Conversion Utilities     *********/
    /* ***************************************************/


     /* ******
     *
     * Setters and Getters
     *
     **********/

    long getCoordinateID() {  return mCoordinateID; }
    void setCoordinateID(long coordinateID) {  mCoordinateID = coordinateID; }


    long getProjectID()                              { return mProjectID;  }
    void setProjectID(long projectID)                { mProjectID = projectID; }

    long  getPointID()                               { return mPointID; }
    void setPointID(long pointID)                    {  mPointID = pointID; }


    int getRawReadings()                           {return mRawReadings;}
    void   setRawReadings(int size)                 {mRawReadings = size;}

    int getMeanedReadings()                         {return mMeanedReadings;}
    void   setMeanedReadings(int size)              {mMeanedReadings = size;}

    int getFixedReadings()                          { return mFixedReadings; }
    void   setFixedReadings(int fixedReadings)      {  mFixedReadings = fixedReadings; }

    double getLatitude()                            { return mLatitude;        }
    void   setLatitude(double latitude)             { mLatitude = latitude; }

    double getLatitudeDegree()                      { return mLatitudeDegree;  }
    void   setLatitudeDegree(double latitudeDegree) { mLatitudeDegree = latitudeDegree; }

    double getLatitudeMinute()                      { return mLatitudeMinute;  }
    void   setLatitudeMinute(double latitudeMinute) { mLatitudeMinute = latitudeMinute; }

    double getLatitudeSecond()                      { return mLatitudeSecond;  }
    void   setLatitudeSecond(double latitudeSecond) { mLatitudeSecond = latitudeSecond; }

    double getLatitudeStdDev()                      { return mLatitudeStdDev;        }
    void   setLatitudeStdDev(double latitudeStdDev) { mLatitudeStdDev = latitudeStdDev; }

    double getLongitude()                           { return mLongitude;       }
    void   setLongitude(double longitude)           { mLongitude = longitude; }

    double getLongitudeDegree()                     { return mLongitudeDegree; }
    void   setLongitudeDegree(double longitudeDegree) { mLongitudeDegree = longitudeDegree; }

    double getLongitudeMinute()                     { return mLongitudeMinute; }
    void   setLongitudeMinute(double longitudeMinute) { mLongitudeMinute = longitudeMinute; }

    double getLongitudeSecond()                     { return mLongitudeSecond; }
    void   setLongitudeSecond(double longitudeSecond) {mLongitudeSecond = longitudeSecond; }

    double getLongitudeStdDev()                      { return mLongitudeStdDev;        }
    void   setLongitudeStdDev(double longitudeStdDev) { mLongitudeStdDev = longitudeStdDev; }

    double getElevation()                           {  return mElevation;   }
    void   setElevation(double elevation)           { mElevation = elevation;   }
    double getElevationFeet() {return GBUtilities.convertMetersToFeet(mElevation); }

    double getElevationStdDev()                      {return mElevationStdDev;}
    void   setElevationStdDev(double elevationStdDev){mElevationStdDev = elevationStdDev;}

    double getGeoid()                               {  return mGeoid; }
    double getGeoidFeet() { return GBUtilities.convertMetersToFeet(mGeoid);}
    void   setGeoid(double geoid)                   { mGeoid = geoid;  }

    //RMS is just another term for Std Deviation
    double getHrms()                                {return mLongitudeStdDev; }
      void setHrms(double hrms)                     { mLongitudeStdDev = hrms; }

    double getVrms()                                { return mLatitudeStdDev; }
    void   setVrms(double vrms)                     { mLatitudeStdDev = vrms; }

    int getSatellites()                             {  return mSatellites; }
    void setSatellites(int satellites)              { mSatellites = satellites; }

    double getEasting()                            { return mLongitude;        }
    void   setEasting(double easting)              { mLongitude = easting; }

    double getEastingStdDev()                      { return mLongitudeStdDev;        }
    void   setEastingStdDev(double eastingStdDev)  { mLongitudeStdDev = eastingStdDev; }

    double getNorthing()                           { return mLatitude;       }
    void   setNorthing(double northing)            { mLatitude = northing; }

    double getNorthingStdDev()                     { return mLatitudeStdDev;       }
    void   setNorthingStdDev(double northingStdDev){ mLatitudeStdDev = northingStdDev; }

    void setValidCoordinate(boolean validCoordinate){this.mValidCoordinate = validCoordinate;}
    boolean isValidCoordinate() {
        return mValidCoordinate;
    }

    void    setIsFixed (boolean isFixed){this.mIsFixed = isFixed;}
    boolean isFixed()                   {return mIsFixed;}

    //type = true if Easting/Northing and = false if Latitude / Longitude
    static final boolean EASTING_NORTHING   = true;
    static final boolean LATITUDE_LONGITUDE = false;
    void setType(boolean type)                       {this.mType = type;}
    boolean isEastingNorthing()                      { return mType; }
    boolean isLatitudeLongitude()                    {
        return (!mType);
        /*
        if (mType) return false;
        return true;
         */

    }

    /* ******
     *
     * Static functions
     *
     **********/



    /* ******
     *
     * Constructors
     *
     **********/
    /* ************************************************************/
    /* ************************************************************/
    /*       Vanilla constructor, default values only             */
    /*                                                            */
    /* ************************************************************/
    /* ************************************************************/
    GBCoordinateMean() {
        //set all variables to their defaults
        initializeDefaultVariables();
    }

    private void initializeDefaultVariables() {
        //I know that one does not have to initialize int's etc, but
        //to be explicit about the initialization, do it anyway
        mPointID        = GBUtilities.ID_DOES_NOT_EXIST;

        mRawReadings    = 0;
        mMeanedReadings = 0;
        mFixedReadings  = 0;

        //Latitude in DD and DMS formats
        mLatitude       = 0d;

        mLatitudeDegree = 0;
        mLatitudeMinute = 0;
        mLatitudeSecond = 0d;
        mLatitudeStdDev = 0.d;


        //Longitude in DD and DMS formats
        mLongitude      = 0d;

        mLongitudeDegree = 0;
        mLongitudeMinute = 0;
        mLongitudeSecond = 0d;
        mLongitudeStdDev = 0d;

        mElevation       = 0d; //Orthometric Elevation in Meters
        mElevationStdDev = 0d;
        mGeoid           = 0d;     //Mean Sea Level in Meters

        mValidCoordinate = false;
    }



}
