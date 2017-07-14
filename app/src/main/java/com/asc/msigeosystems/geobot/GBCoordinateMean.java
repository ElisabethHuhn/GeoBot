package com.asc.msigeosystems.geobot;

import android.os.Bundle;

/**
 * Created by Elisabeth Huhn on 6/17/2016.
 *
 * This class is NOT part of the Coordinate's hierarchy
 * But it is only meant as a summary of the meaning process
 *  The raw data and meaning process flags are kept on an instance of GBMeanToken
 *
 * NOTE all fields are double, even Degrees and Minutes
 *
 */


class GBCoordinateMean {
    //

    /* ***************************************************/
    /* ******     Static Constants               *********/
    /* ***************************************************/
    public static final String MEAN_COORDINATE_ID   = "meanCoordinateID";
    public static final String MEAN_PROJECT_ID      = "meanProjectID";
    public static final String MEAN_POINT_ID        = "meanPointID";
    public static final String MEAN_RAW_READINGS    = "meanRaw";
    public static final String MEAN_FIXED_READINGS  = "meanFixed";
    public static final String MEAN_MEANED_READINGS = "meanMeaned";
    public static final String MEAN_LATITUDE        = "meanLatitude";
    public static final String MEAN_LATITUDE_DEGREE = "meanLatitudeDeg";
    public static final String MEAN_LATITUDE_MINUTE = "meanLatitudeMin";
    public static final String MEAN_LATITUDE_STDDEV = "meanLatitudeStD";
    public static final String MEAN_LATITUDE_SECOND = "meanLatitudeSec";
    public static final String MEAN_LONGITUDE        = "meanLongitude";
    public static final String MEAN_LONGITUDE_DEGREE = "meanLongitudeDeg";
    public static final String MEAN_LONGITUDE_MINUTE = "meanLongitudeMin";
    public static final String MEAN_LONGITUDE_SECOND = "meanLongitudeSec";
    public static final String MEAN_LONGITUDE_STDDEV = "meanLongitudeStD";
    public static final String MEAN_ELEVATION        = "meanElevation";
    public static final String MEAN_ELEVATION_STDDEV = "meanElevationStD";
    public static final String MEAN_GEOID            = "meanGeoid";
    public static final String MEAN_SATELLITES       = "meanSatellites";
    public static final String MEAN_VALID            = "meanValid";
    public static final String MEAN_ISFIXED          = "meanIsFixed";
    public static final String MEAN_TYPE             = "meanType";


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
    private int    mLatitudeDegree;
    private int    mLatitudeMinute;
    private double mLatitudeSecond;
    private double mLatitudeStdDev;  //RMS


    //Longitude in DD and DMS formats
    private double mLongitude;
    private int    mLongitudeDegree;
    private int    mLongitudeMinute;
    private double mLongitudeSecond;
    private double mLongitudeStdDev;  //RMS

    private double mElevation;        //Orthometric Elevation in Meters
    private double mElevationStdDev;  //RMS

    private double mGeoid;            //Mean Sea Level in Meters

    private int    mSatellites;       //Number of satellites in the fix

    private boolean mValidCoordinate = true;
    private boolean mIsFixed = true;
    private boolean mType = true; //true = Easting/Northing, false = Latitude/Longitude

    // TODO: 6/19/2017 maybe need some quality indicators also (DOP)

    /* ***************************************************/
    /* ******    Static Conversion Utilities     *********/
    /* ***************************************************/


    /* ******
     *
     * Constructors
     *
     **********/

    /* ************************************************************/
    /*       Vanilla constructor, default values only             */
    /* ************************************************************/
    GBCoordinateMean() {
        //set all variables to their defaults
        initializeDefaultVariables();
    }

    // TODO: 6/18/2017 write a constructor from the Bundle saveInstanceState


    GBCoordinateMean (GBNmea nmeaData){

            setMeanedReadings (1);//we are only dealing with a single point
            int fixed = 0;
            if (nmeaData.isFixed())fixed++;
            setFixedReadings  (fixed);
            setLatitude       (nmeaData.getLatitude());
            setLongitude      (nmeaData.getLongitude());
            setElevation      (nmeaData.getOrthometricElevation());
            setLatitudeStdDev (0);
            setLongitudeStdDev(0);
            setElevationStdDev(0);
            setHrms           (1);
            setVrms           (1);

    }

    GBCoordinateMean (GBCoordinateWGS84 coordinateWGS84){

        setProjectID      (coordinateWGS84.getProjectID());
        setPointID        (coordinateWGS84.getPointID());

        setRawReadings    (1);
        setMeanedReadings (1);//we are only dealing with a single point
        setFixedReadings  (1);

        setLatitude       (coordinateWGS84.getLatitude());
        setLatitudeDegree (coordinateWGS84.getLatitudeDegree());
        setLatitudeMinute (coordinateWGS84.getLatitudeMinute());
        setLatitudeSecond (coordinateWGS84.getLatitudeSecond());

        setLongitude      (coordinateWGS84.getLongitude());
        setLongitudeDegree(coordinateWGS84.getLongitudeDegree());
        setLongitudeMinute(coordinateWGS84.getLongitudeMinute());
        setLongitudeSecond(coordinateWGS84.getLongitudeSecond());
        setElevation      (coordinateWGS84.getElevation());

        setLatitudeStdDev (0);
        setLongitudeStdDev(0);
        setElevationStdDev(0);
        setHrms           (1);
        setVrms           (1);

        setValidCoordinate(coordinateWGS84.isValidCoordinate());
        setIsFixed        (coordinateWGS84.isFixed());

        setType           (LATITUDE_LONGITUDE);//because it is coming from WGS84 coordinate

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


    int    getRawReadings()                         {return mRawReadings;}
    void   setRawReadings(int size)                 {mRawReadings = size;}

    int    getMeanedReadings()                      {return mMeanedReadings;}
    void   setMeanedReadings(int size)              {mMeanedReadings = size;}

    int    getFixedReadings()                       { return mFixedReadings; }
    void   setFixedReadings(int fixedReadings)      {  mFixedReadings = fixedReadings; }

    double getLatitude()                            { return mLatitude;        }
    void   setLatitude(double latitude)             { mLatitude = latitude; }

    int    getLatitudeDegree()                      { return mLatitudeDegree;  }
    void   setLatitudeDegree(int latitudeDegree) { mLatitudeDegree = latitudeDegree; }

    int    getLatitudeMinute()                      { return mLatitudeMinute;  }
    void   setLatitudeMinute(int latitudeMinute) { mLatitudeMinute = latitudeMinute; }

    double getLatitudeSecond()                      { return mLatitudeSecond;  }
    void   setLatitudeSecond(double latitudeSecond) { mLatitudeSecond = latitudeSecond; }

    double getLatitudeStdDev()                      { return mLatitudeStdDev;        }
    void   setLatitudeStdDev(double latitudeStdDev) { mLatitudeStdDev = latitudeStdDev; }

    double getLongitude()                           { return mLongitude;       }
    void   setLongitude(double longitude)           { mLongitude = longitude; }

    int    getLongitudeDegree()                     { return mLongitudeDegree; }
    void   setLongitudeDegree(int longitudeDegree) { mLongitudeDegree = longitudeDegree; }

    int    getLongitudeMinute()                     { return mLongitudeMinute; }
    void   setLongitudeMinute(int longitudeMinute) { mLongitudeMinute = longitudeMinute; }

    double getLongitudeSecond()                     { return mLongitudeSecond; }
    void   setLongitudeSecond(double longitudeSecond) {mLongitudeSecond = longitudeSecond; }

    double getLongitudeStdDev()                      { return mLongitudeStdDev;        }
    void   setLongitudeStdDev(double longitudeStdDev) { mLongitudeStdDev = longitudeStdDev; }

    double getElevation()                           {  return mElevation;   }
    void   setElevation(double elevation)           { mElevation = elevation;   }
    double getElevationFeet() {return GBUtilities.convertMetersToFeet(mElevation); }
    double getElevationIFeet() {return GBUtilities.convertMetersToIFeet(mElevation);}

    double getElevationStdDev()                      {return mElevationStdDev;}
    void   setElevationStdDev(double elevationStdDev){mElevationStdDev = elevationStdDev;}

    double getGeoid()                               {  return mGeoid; }
    double getGeoidFeet() { return GBUtilities.convertMetersToFeet(mGeoid);}
    double getGeoidIFeet(){ return GBUtilities.convertMetersToIFeet(mGeoid);}
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
    boolean isEastingNorthing()                      { return   mType; }
    boolean isLatitudeLongitude()                    { return (!mType);    }





    /* ************************************************************/
    /*                   Calculate the mean                       */
    /* ************************************************************/
    //called from the nmea event handler when mean is in progress
    void updateMean(int numSatellites,  GBMeanToken meanValues){

        // TODO: 1/21/2017 update the meanCoordinate with quality / fixed data from the nmeaData

        //Some temporary spots to hold intermediate results
        double tempMeanD;
        //A mean coordinate for the Standard Deviation/RMS
        //GBCoordinateMean residuals = new GBCoordinateMean();
        double residualLat  = 0d;
        double residualLong = 0d;
        double residualEle  = 0d;
        double residualGeoid= 0d;

        //assure that we have enough readings to calculate mean
        int last = meanValues.getCoordinateSize();
        if (last < 2) return;

        //update the mean coordinate with satellites and readings numbers

        setSatellites    (numSatellites);
        setRawReadings   (meanValues.getRawReadings());
        setFixedReadings (meanValues.getFixedReadings());
        setMeanedReadings(last);

        // TODO: 6/19/2017 get rid of the debug statements
        //debug variables
        double meanedLat   = 0;
        double readingLat  = 0;
        double meanedLong  = 0;
        double readingLong = 0;
        double meanedEle   = 0;
        double readingEle  = 0;
        double meanedGeo   = 0;
        double readingGeo  = 0;

        //Start from zero
        setLatitude (0d);
        setLongitude(0d);
        setElevation(0d);
        setGeoid    (0d);

        setLatitudeStdDev(0d);
        setLongitudeStdDev(0d);
        setElevationStdDev(0d);


        //calculate the mean
        //Step one - Sum of each member in the list
        int position = 0;

        while (position < last){

            meanedLat  = getLatitude();
            readingLat = meanValues.getCoordinateAt(position).getLatitude();
            tempMeanD  = meanedLat  + readingLat;
            setLatitude(tempMeanD);
            meanedLat  = getLatitude();

            meanedLong  = getLongitude();
            readingLong = meanValues.getCoordinateAt(position).getLongitude();
            tempMeanD   = meanedLong + readingLong;
            setLongitude(tempMeanD);
            meanedLong  = getLongitude();

            meanedEle  = getElevation();
            readingEle = meanValues.getCoordinateAt(position).getElevation();
            tempMeanD  = meanedEle + readingEle;
            setElevation(tempMeanD);
            meanedEle  = getElevation();

            meanedGeo  = getGeoid();
            readingGeo = meanValues.getCoordinateAt(position).getGeoid();
            tempMeanD  = meanedGeo + readingGeo;
            setGeoid(tempMeanD);
            meanedGeo  = getGeoid();

            position++;

        }
        //Step two: divide by the number in the list
        double sizeD = (double)last;//convert to double as Java is a strongly typed language

        meanedLat = meanedLat / sizeD;
        setLatitude (getLatitude()  / sizeD);

        meanedLong = meanedLong / sizeD;
        setLongitude(getLongitude() / sizeD);

        meanedEle = meanedEle / sizeD;
        setElevation(getElevation() / sizeD);

        meanedGeo = meanedGeo / sizeD;
        setGeoid    (getGeoid()     / sizeD);

        //calculate the variance of the squared residuals
        //Step three: residual = sum( (mean - reading) squared  )
        position = 0;
        while(position < last){

            //subtract the mean from the reading
            tempMeanD = getLatitude()  - meanValues.getCoordinateAt(position).getLatitude();
            //and square the result
            tempMeanD = tempMeanD * tempMeanD;
            residualLat = (residualLat      + tempMeanD);

            tempMeanD = getLongitude() - meanValues.getCoordinateAt(position).getLongitude();
            tempMeanD = tempMeanD * tempMeanD;
            residualLong = (residualLong    + tempMeanD);

            tempMeanD = getElevation() - meanValues.getCoordinateAt(position).getElevation();
            tempMeanD = tempMeanD * tempMeanD;
            residualEle = (residualEle      + tempMeanD);

            tempMeanD = getGeoid()     - meanValues.getCoordinateAt(position).getGeoid();
            tempMeanD = tempMeanD * tempMeanD;
            residualGeoid = (residualGeoid  + tempMeanD);

            position++;
        }


        //step 3.5 mean of residuals = sum / # of readings
        //Step 4: sqrt of (mean of the residuals)

        //Treat readings as sample of a larger population
        //so take sample mean (i.e. divide by size - 1)
        sizeD = sizeD - 1.0;
        setLatitudeStdDev  (Math.sqrt(residualLat  / sizeD));
        setLongitudeStdDev (Math.sqrt(residualLong / sizeD));
        setElevationStdDev (Math.sqrt(residualEle  / sizeD));

        //convert DecimalDegrees to Degrees, Minutes, Seconds is done when output to screen

        //if we've gotten this far, it's valid
        setValidCoordinate(true);
    }


    Bundle saveState(Bundle outState){

        //ID is necessary for the DB
        outState.putLong(MEAN_COORDINATE_ID, getCoordinateID());

        //Project of the meaning process
        outState.putLong(MEAN_PROJECT_ID, getProjectID());
        outState.putLong(MEAN_POINT_ID,   getPointID());

        //total number of readings since meaning started
        outState.getInt(MEAN_RAW_READINGS, getRawReadings());

        //number of readings that have been used in the mean
        outState.getInt(MEAN_MEANED_READINGS, getMeanedReadings());

        //number of the readings included in the mean that are of quality fixed
        // (per Project Settings Quality definition)
        outState.getInt(MEAN_FIXED_READINGS, getFixedReadings());



        //Latitude in DD and DMS formats
        outState.getDouble(MEAN_LATITUDE,        getLatitude());
        outState.getDouble(MEAN_LATITUDE_DEGREE, getLatitudeDegree());
        outState.getDouble(MEAN_LATITUDE_MINUTE, getLatitudeMinute());
        outState.getDouble(MEAN_LATITUDE_SECOND, getLatitudeSecond());
        outState.getDouble(MEAN_LATITUDE_STDDEV, getLatitudeStdDev());


        //Longitude in DD and DMS formats
        outState.getDouble(MEAN_LONGITUDE,        getLongitude());
        outState.getDouble(MEAN_LONGITUDE_DEGREE, getLongitudeDegree());
        outState.getDouble(MEAN_LONGITUDE_MINUTE, getLongitudeMinute());
        outState.getDouble(MEAN_LONGITUDE_SECOND, getLongitudeSecond());
        outState.getDouble(MEAN_LONGITUDE_STDDEV, getLongitudeStdDev());


        outState.getDouble(MEAN_ELEVATION,        getElevation());
        outState.getDouble(MEAN_ELEVATION_STDDEV, getElevationStdDev());
        outState.getDouble(MEAN_GEOID,            getGeoid());

        outState.getInt   (MEAN_SATELLITES,       getSatellites());

        //0 = false, 1 = true
        int tempBoolean = 0; //default = false
        if (isValidCoordinate())tempBoolean = 1;
        outState.putInt(MEAN_VALID, tempBoolean);

        //0 = false, 1 = true
        tempBoolean = 0; //default = false
        if (isFixed())tempBoolean = 1;
        outState.putInt(MEAN_ISFIXED, tempBoolean);

        //0 = false, 1 = true
        tempBoolean = 0; //default = false
        if (isEastingNorthing())tempBoolean = 1;
        outState.putInt(MEAN_TYPE, tempBoolean);

        return outState;
    }

    /* ******
     *
     * Static functions
     *
     **********/





}
