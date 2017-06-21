package com.asc.msigeosystems.geobot;

/**
 * Created by Elisabeth Huhn on 5/7/2016.
 * Settings that govern how the App behaves
 */
class GBProjectSettings {

    static final int sMeanByTime = 0;
    static final int sMeanByNumber = 1;


    //Constants for enumerated types
    static final CharSequence sUIDistanceUSFeet  = "US Survey Feet";
    static final CharSequence sUIDistanceIntFeet = "International Feet";
    static final CharSequence sUIDistanceMeters  = "Meters";
    static final int sDBDistanceMeters  = 0;
    static final int sDBDistanceUSFeet  = 1;
    static final int sDBDistanceIntFeet = 2;

    static final CharSequence sUIDecimalDisplayCommas   = "123,456,789.00";
    static final CharSequence sUIDecimalDisplayNoCommas = "123456789.00";
    static final int sDBDecimalDisplayCommas = 0;
    static final int sDBDecimalDisplayNoCommas = 1;

    static final CharSequence sUIAngleUnitsDeg = "Degrees";
    static final CharSequence sUIAngleUnitsRad = "Radians";
    static final int sDBAngleUnitsDeg = 0;
    static final int sDBAngleUnitsRad = 1;

    static final CharSequence sUIAngleDisplayDD   = "123.456789";//Decimal Degrees
    static final CharSequence sUIAngleDisplayDM   = "123 45.11'";//Degrees Minutes
    static final CharSequence sUIAngleDisplayDMS  = "123 45' 11\"";//Degrees Minutes Seconds
    static final CharSequence sUIAngleDisplayGONS = "Gons????";
    static final CharSequence sUIAngleDisplayMils = "MILs?????";
    static final int sDBAngleDisplayDD   = 0;
    static final int sDBAngleDisplayDM   = 1;//Degrees Minutes
    static final int sDBAngleDisplayDMS  = 2;
    static final int sDBAngleDisplayGONS = 3;
    static final int sDBAngleDisplayMils = 4;


    static final CharSequence sUIGridDirectionN = "North Azimuth";
    static final CharSequence sUIGridDirectionS = "South Azimuth";
    static final CharSequence sUIGridDirectionE = "East Azimuth";
    static final CharSequence sUIGridDirectionW = "West Azimuth";
    static final int sDBGridDirectionN = 0;
    static final int sDBGridDirectionS = 1;
    static final int sDBGridDirectionE = 2;
    static final int sDBGridDirectionW = 3;

    //scale factor is double

    static final CharSequence sUISeaLevelTrue  = "True";
    static final CharSequence sUISeaLevelFalse = "False";
    static final int sDBSeaLevelTrue  = 0;
    static final int sDBSeaLevelFalse = 1;

    static final CharSequence sUIRefractionTrue  = "True";
    static final CharSequence sUIRefractionFalse = "False";
    static final int sDBRefractionTrue  = 0;
    static final int sDBRefractionFalse = 1;


    static final CharSequence sUIDatumWGS = GBCoordinate.sCoordinateTypeWGS84;
    static final CharSequence sUIDatumNAD = GBCoordinate.sCoordinateTypeNAD83;
    static final int sDBDatumWGS  = 0;
    static final int sDBDatumNAD = 1;


    static final CharSequence sUIProjectionNone = "No Projection";
    static final CharSequence sUIProjectionUTM = GBCoordinate.sCoordinateTypeUTM;
    static final CharSequence sUIProjectionSPC = GBCoordinate.sCoordinateTypeClassSPCS;
    static final int sDBProjectionNone = 0;
    static final int sDBProjectionUTM  = 1;
    static final int sDBProjectionSPC  = 2;


    static final CharSequence sUIZoneNone = "Not SPCS";
    static final CharSequence sUIZoneAl  = "Alabama";
    static final CharSequence sUIZoneGaW = "Georgia West (1002)";
    static final int sDBZoneNone = 0;
    static final int sDBZoneAl   = 1;
    static final int sDBZoneGaW   = 2;

    //spc scale factor is double value

    static final CharSequence sUICoordinateDisplayNE = "Northing/Easting";//Northing first
    static final CharSequence sUICoordinateDisplayEN = "Easting/Northing";//Easting first
    static final int sDBCoordinateDisplayNE = 0;
    static final int sDBCoordinateDisplayEN = 1;

    static final CharSequence sUIGeoidModelNone = "None";
    static final CharSequence sUIGeoidModel99  = "GEOID99";
    static final int sDBGeoidModelNone = 0;
    static final int sDBGeoidModel99   = 1;


    static final CharSequence sUIAlphanumericIDTrue  = "True";
    static final CharSequence sUIAlphanumericIDFalse = "False";
    static final int sDBAlphanumericIDTrue  = 0;
    static final int sDBAlphanumericIDFalse = 1;


    static final CharSequence sUITimeStampTrue  = "True";
    static final CharSequence sUITimeStampFalse = "False";
    static final int sDBTimeStampTrue  = 0;
    static final int sDBTimeStampFalse = 1;


    static final CharSequence sUIFeatureCodeTrue  = "True";
    static final CharSequence sUIFeatureCodeFalse = "False";
    static final int sDBFeatureCodeTrue  = 0;
    static final int sDBFeatureCodeFalse = 1;

    //Standard Deviation vs RMS
    static final CharSequence sRMS    = "RMS";
    static final CharSequence sStdDev = "Standard Deviation";
    static final int sRMSCode    = 0;
    static final int sStdDevCode = 1;



    //starting point ID is integer


    //If Project ID = -1, these are the global defaults for Project Settings
    private long         mProjectSettingsID;
    private long         mProjectID; //The project these settings belong to
    private int          mDistanceUnits ;
    private int          mDecimalDisplay ;
    private int          mAngleUnits ;
    private int          mAngleDisplay ;
    private int          mGridDirection  ;
    private double       mScaleFactor  ;
    private boolean      mSeaLevel  ;
    private boolean      mRefraction ;
    private int          mDatum ;
    private int          mProjection ;
    private int          mZone  ;
    private double       mSpcScaleFactor  ;
    private int          mCoordinateDisplay;
    private int          mGeoidModel ;
    private long         mStartingPointID ;
    private boolean      mAlphanumericID;
    private boolean      mFeatureCodes;
    //private CharSequence mFCControlFile;
    private boolean      mFCTimeStamp;

    private int          mMeaningMethod = sMeanByNumber;
    private int          mMeaningNumber; //either the number of readings or the number of seconds
    //Digits of precision are for the UI only. Underlying numbers are not truncated
    // TODO: 12/13/2016 Should underlying values be truncated to precision digits? 
    private int          mLocationPrecision; //# digits of precision for locations eg Lat/Long or N/E
    private int          mElevationPrecision;
    private int          mStdDevPrecision;//precision of standard deviations

    private int          mRMSvsStdDev;

    /********
     *
     * Setters and Getters
     *
     ********/
    long getProjectSettingsID() {
        return mProjectSettingsID;
    }
    void setProjectSettingsID(long projectSettingsID) {
        this.mProjectSettingsID = projectSettingsID;
    }

    long getProjectID() {
        return mProjectID;
    }
    void setProjectID(long projectID) {
        this.mProjectID = projectID;
    }

    int getDistanceUnits() {
        return mDistanceUnits;
    }
    void setDistanceUnits(int distanceUnits) {this.mDistanceUnits = distanceUnits;}

    int getDecimalDisplay() {
        return mDecimalDisplay;
    }
    void setDecimalDisplay(int decimalDisplay) {mDecimalDisplay = decimalDisplay; }

    int getAngleUnits() {
        return mAngleUnits;
    }
    void setAngleUnits(int angleUnits) { mAngleUnits = angleUnits; }

    int getAngleDisplay() {
        return mAngleDisplay;
    }
    void setAngleDisplay(int angleDisplay) { mAngleDisplay = angleDisplay; }

    int getGridDirection() {
        return mGridDirection;
    }
    void setGridDirection(int gridDirection) { mGridDirection = gridDirection; }

    double getScaleFactor() {
        return mScaleFactor;
    }
    void setScaleFactor(double scaleFactor) {mScaleFactor = scaleFactor;   }

    boolean isSeaLevel() {
        return mSeaLevel;
    }
    void setSeaLevel(boolean seaLevel) { mSeaLevel = seaLevel;  }

    boolean isRefraction() {
        return mRefraction;
    }
    void setRefraction(boolean refraction) { mRefraction = refraction;}

    int getDatum() {
        return mDatum;
    }
    void setDatum(int datum) { mDatum = datum; }

    int getProjection() {
        return mProjection;
    }
    void setProjection(int projection) { mProjection = projection; }

    int getZone() {
        return mZone;
    }
    void setZone(int zone) {  mZone = zone; }

    double getSpcScaleFactor() {return mSpcScaleFactor;}
    void   setSpcScaleFactor(double scaleFactor) {mSpcScaleFactor = scaleFactor;}

    int getCoordinateDisplay() {
        return mCoordinateDisplay;
    }
    void setCoordinateDisplay(int coordinateDisplay) {
        mCoordinateDisplay = coordinateDisplay;
    }

    int getGeoidModel() {
        return mGeoidModel;
    }
    void setGeoidModel(int geoidModel) { mGeoidModel = geoidModel; }

    long getStartingPointID() {
        return mStartingPointID;
    }
    void setStartingPointID(long startingPointID) {
        mStartingPointID = startingPointID;
    }

    boolean isAlphanumericID() {
        return mAlphanumericID;
    }
    void setAlphanumericID(boolean alphanumericID) { mAlphanumericID = alphanumericID; }

    boolean isFeatureCodes() {
        return mFeatureCodes;
    }
    void setFeatureCodes(boolean featureCodes) { mFeatureCodes = featureCodes; }

/*
    CharSequence getFCControlFile() { return mFCControlFile; }
    void setFCControlFile(CharSequence fcControlFile){mFCControlFile = fcControlFile;}
 */

    boolean isFCTimeStamp() {
        return mFCTimeStamp;
    }
    void setFCTimeStamp(boolean fcTimeStamp) {mFCTimeStamp = fcTimeStamp;}

    int getMeaningMethod() {  return mMeaningMethod; }
    boolean isMeanByNumber () {return (getMeaningMethod() == sMeanByNumber);}
    boolean isMeanByTime   () {return (getMeaningMethod() == sMeanByTime);}
    void setMeaningMethod(int meaningMethod) { mMeaningMethod = meaningMethod; }

    int getMeaningNumber() { return mMeaningNumber;}
    void setMeaningNumber(int meaningNumber) {  mMeaningNumber = meaningNumber; }

    int getLocationPrecision() {       return mLocationPrecision; }
    void setLocationPrecision(int locationPrecision) {mLocationPrecision = locationPrecision; }

    int getElevationPrecision() { return mElevationPrecision;}
    void setElevationPrecision(int elevationPrecision) {mElevationPrecision = elevationPrecision;}

    int getStdDevPrecision() {       return mStdDevPrecision; }
    void setStdDevPrecision(int stdDevPrecision) {mStdDevPrecision = stdDevPrecision; }

    int getRMSvsStdDev()                  {  return mRMSvsStdDev; }
    void setRMSStdDev(int rmsVSstdCode)   {mStdDevPrecision = rmsVSstdCode; }

    /*
     *
     * Constructor
     *
     */

    //this creates a new instance with values defined by global defaults
    GBProjectSettings() {
        setDefaults();
    }

    GBProjectSettings(long projectID){
        setDefaults();
        mProjectID = projectID;
    }

    /*
     *
     * Other Methods of the class
     *
     */
    void setDefaults() {


        //Restore default values
        mProjectSettingsID = GBUtilities.ID_DOES_NOT_EXIST;
        mProjectID      = GBUtilities.ID_DOES_NOT_EXIST; //by default, the default settings
        mDistanceUnits  = sDBDistanceMeters;
        mDecimalDisplay = sDBDecimalDisplayCommas;
        mAngleUnits     = sDBAngleUnitsDeg;
        mAngleDisplay   = sDBAngleDisplayDD;
        mGridDirection  = sDBGridDirectionN;
        mScaleFactor    = .99982410;
        mSeaLevel       = false;
        mRefraction     = false;
        mDatum          = sDBDatumWGS;
        mProjection     = sDBProjectionSPC;
        mZone           = sDBZoneGaW;
        mCoordinateDisplay = sDBCoordinateDisplayEN;
        mGeoidModel      = sDBGeoidModel99;
        mStartingPointID = 1001;
        mAlphanumericID  = false;
        mFeatureCodes    = true;
        //mFCControlFile   = "CP2";
        mFCTimeStamp     = false;

        mMeaningMethod   = sMeanByNumber;
        mMeaningNumber   = 180;
        mLocationPrecision  = GBUtilities.sMicrometerDigitsOfPrecision;
        mElevationPrecision = GBUtilities.sHundredthsDigitsOfPrecision;
        mStdDevPrecision    = GBUtilities.sMicrometerDigitsOfPrecision;
    }




    String convertToCDF(){

        String msg =
                String.valueOf(this.getProjectID()) + ", " +
                this.getDistanceUnits()             + ", " +
                this.getDecimalDisplay()            + ", " +
                this.getAngleUnits()                + ", " +
                this.getAngleDisplay()              + ", " +
                this.getGridDirection()             + ", " +
                String.valueOf(this.getScaleFactor())  + ", ";

                if (isSeaLevel()){        msg = msg + "true, "; }
                else {                    msg = msg + "false, ";}
                if (isRefraction()){      msg = msg + "true, "; }
                else {                    msg = msg + "false, ";}

        msg = msg +
                this.getDatum()                     + ", " +
                this.getProjection()                + ", " +
                this.getZone()                      + ", " +
                this.getCoordinateDisplay()         + ", " +
                this.getGeoidModel()                + ", " +
                this.getStartingPointID()           + ", " ;

                if (isAlphanumericID()){    msg = msg + "true, "; }
                else {                    msg = msg + "false, ";}

                if (isFeatureCodes()){    msg = msg + "true, "; }
                else {                    msg = msg + "false, ";}

                //this.getFCControlFile()             + ", " ;

                if (isFCTimeStamp()){     msg = msg + "true, "; }
                else {                    msg = msg + "false, ";}
        msg = msg +
                String.valueOf(this.getMeaningMethod() )      + ", " +
                String.valueOf(this.getMeaningNumber() )      + ", " +
                String.valueOf(this.getLocationPrecision() )  + ", " +
                String.valueOf(this.getElevationPrecision())  + ", " +
                String.valueOf(this.getStdDevPrecision() )    + ", " ;

                System.getProperty("line.separator");
        return msg;
    }

}
