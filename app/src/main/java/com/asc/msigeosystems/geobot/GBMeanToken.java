package com.asc.msigeosystems.geobot;

import android.os.Bundle;

import java.util.ArrayList;

/**
 * Created by Elisabeth Huhn on 6/17/2017.
 * These are values returned from each intermediate step of meaning GPS data
 */


class GBMeanToken {

    // ******************************************************** //
    // **************    Static Constants    ****************** //
    // ******************************************************** //
    //Tags for storing a token in arguments
    private static final String sTokenIDTag = "tokenID";
    //Meaning Variables
    //Flags 0 = false, 1 = true
    private static final String IS_MEANING        = "IsMeaning";
    private static final String IS_FIRST          = "IsFirst";
    private static final String IS_LAST           = "IsLast";
    private static final String START_MEAN_TIME   = "StartMeanTime";
    private static final String CURRENT_MEAN_TIME = "CurrentMeanTime";
    private static final String FIXED_READING     = "FixedReading";
    private static final String RAW_READING       = "RawReading";



    /* **************************************************************/
    /*               Static Methods                                 */
    /* **************************************************************/

    static Bundle putTokenInArguments(Bundle args, long tokenID) {

        if (tokenID == GBUtilities.ID_DOES_NOT_EXIST){
            //And error, but what to do about it here??
            args.putLong(GBMeanToken.sTokenIDTag, GBUtilities.ID_DOES_NOT_EXIST);
        } else {
            args.putLong(GBMeanToken.sTokenIDTag, tokenID);
        }
        //all other attributes are stored in the DB via the TokenManager
        return args;
    }


    static long getTokenFromArguments(Bundle args) {

        return args.getLong (GBMeanToken.sTokenIDTag);

    }






    // ******************************************************** //
    // **************** Member Variables ********************** //
    // ******************************************************** //

    //variables for the meaning process
    private long    mMeanTokenID;
    private long    mProjectID;
    private long    mPointID;
    private boolean mIsMeanInProgress = false;
    private boolean mIsFirstPointInMean = false;
    private boolean mIsLastPointInMean = false;
    private double  mStartMeanTime;
    private double  mEndMeanTime;
    private double  mCurrentMeanTime;
    private int     mFixedReadings;
    private int     mRawReadings;
    private GBCoordinateMean             mMeanCoordinate;//The results of the mean so far
    private ArrayList<GBCoordinateWGS84> mMeanedCoordinates;

    // ******************************************************** //
    // ********************* Constructor ********************** //
    // ******************************************************** //


    GBMeanToken() {
        initializeDefaultValues();
    }

    GBMeanToken(Bundle savedInstanceState){
        //0 = false, 1 = true
        setMeanInProgress(false);
        int tempBoolean = savedInstanceState.getInt(IS_MEANING, 0);
        if (tempBoolean == 1)setMeanInProgress(true);

        setFirstPointInMean(false);
        tempBoolean = savedInstanceState.getInt(IS_FIRST, 0);
        if (tempBoolean == 1)setFirstPointInMean(true);

        setLastPointInMean(false);
        tempBoolean = savedInstanceState.getInt(IS_LAST, 0);
        if (tempBoolean == 1)setLastPointInMean(true);

        setStartMeanTime  (savedInstanceState.getDouble(START_MEAN_TIME,   0d));
        setCurrentMeanTime(savedInstanceState.getDouble(CURRENT_MEAN_TIME, 0d));
        setFixedReadings  (savedInstanceState.getInt   (FIXED_READING, 0));
        setRawReadings    (savedInstanceState.getInt   (RAW_READING,   0));

        // TODO: 6/18/2017 have to save the GBCoordinateMean in the savedInstanceState


    }


    private void initializeDefaultValues(){
        mMeanTokenID        = GBUtilities.ID_DOES_NOT_EXIST;
        mProjectID          = GBUtilities.ID_DOES_NOT_EXIST;
        mIsMeanInProgress   = false;
        mIsFirstPointInMean = false;
        mIsLastPointInMean  = false;
        mStartMeanTime      = 0d;
        mCurrentMeanTime    = 0d;
        mFixedReadings      = 0;
        mRawReadings        = 0;
        mMeanCoordinate = null;

    }


    // ******************************************************** //
    // ***************  Setters & Getters     ***************** //
    // ******************************************************** //


    long getMeanTokenID() { return mMeanTokenID;}
    void setMeanTokenID(long meanTokenID) {
        mMeanTokenID = meanTokenID;}

    long getProjectID() { return mProjectID;}
    void setProjectID(long projectID) {
        mProjectID = projectID;}

    long getPointID() { return mPointID;}
    void setPointID(long pointID) {
        mPointID = pointID;}

    boolean isMeanInProgress() {return mIsMeanInProgress;}
    void setMeanInProgress(boolean meanInProgress) {mIsMeanInProgress = meanInProgress;}

    boolean isFirstPointInMean() {return mIsFirstPointInMean;}
    void setFirstPointInMean(boolean firstPointInMean) {mIsFirstPointInMean = firstPointInMean;}

    boolean isLastPointInMean() {return mIsLastPointInMean;}
    void setLastPointInMean(boolean lastPointInMean) {mIsLastPointInMean = lastPointInMean;}

    double getStartMeanTime() {return mStartMeanTime;}
    void   setStartMeanTime(double meanTime) {mStartMeanTime = meanTime;}

    double getEndMeanTime() {return mEndMeanTime;}
    void   setEndMeanTime(double meanTime) {mEndMeanTime = meanTime;}

    double getCurrentMeanTime() {return mCurrentMeanTime;}
    void   setCurrentMeanTime(double meanTime) {mCurrentMeanTime = meanTime;}

    GBCoordinateMean getMeanCoordinate() {
        //if the meanCoordinate is null, we might be recovering from a configuration change
        //  and need to recalculate
        if (mMeanCoordinate == null){
            //recalculate the mean from the raw coordinates if we don't have a copy of the results
            // TODO: 6/26/2017 figure out what to do about # satellites
            recalculateMean();
        }
        return mMeanCoordinate;
    }
    GBCoordinateMean getMeanCoordinate(boolean calculateMean){

        if (mMeanCoordinate == null){
            setMeanCoordinate(new GBCoordinateMean());
        }
        if (calculateMean) {
            mMeanCoordinate.updateMean(0, this);
        }
        return mMeanCoordinate;
    }
    void setMeanCoordinate(GBCoordinateMean meanCoordinate) {mMeanCoordinate = meanCoordinate;}
    void resetMeanCoordinate(){mMeanCoordinate = null;}

    int  getFixedReadings() {return mFixedReadings;}
    void incFixedReadings() {       mFixedReadings++;}
    void setFixedReadings(int fixedReadings) {mFixedReadings = fixedReadings;}

    int  getRawReadings() {return mRawReadings;}
    void incRawReadings() {       mRawReadings++;}
    void setRawReadings(int rawReadings) {mRawReadings = rawReadings;}

    //These are the raw coordinates that have contributed to the mean
    ArrayList<GBCoordinateWGS84> getCoordinates() {
        if (!areCoordinatesInMemory()) {
            GBCoordinateManager coordinateManager = GBCoordinateManager.getInstance();
            mMeanedCoordinates = coordinateManager.getCoordinatesForToken(getMeanTokenID());
        }
        return mMeanedCoordinates;
    }
    boolean areCoordinatesInMemory(){
        return (!((mMeanedCoordinates == null) || (mMeanedCoordinates.size()==0))) ;
    }
    void    setCoordinates(ArrayList<GBCoordinateWGS84> meanedCoordinates) {
        mMeanedCoordinates = meanedCoordinates;
    }

    int getCoordinateSize() {
        if (areCoordinatesInMemory()) {
            ArrayList<GBCoordinateWGS84> coordinates = getCoordinates();
            if (coordinates == null) return 0;
            return getCoordinates().size();
        } else {
            GBCoordinateManager coordinateManager = GBCoordinateManager.getInstance();
            return coordinateManager.getRawCoordinatesCount(getMeanTokenID());
        }
    }
    GBCoordinateWGS84 getCoordinateAt(int position){
        if (position < 0)return null;
        return getCoordinates().get(position);
    }
    GBCoordinateWGS84 getLastCoordinate(){
        int position = getCoordinateSize()-1;
        if (position < 0)return null;
        return getCoordinateAt(position);
    }
    boolean addCoordinate(GBCoordinateWGS84 newCoordinate){
        ArrayList<GBCoordinateWGS84> coordinates = getCoordinates();
        if (coordinates == null){
            mMeanedCoordinates = new ArrayList<>();
            coordinates = mMeanedCoordinates;
        }

        return coordinates.add(newCoordinate);
    }

    void    resetCoordinates(){
        if (mMeanedCoordinates == null)return;
        mMeanedCoordinates = new ArrayList<>();
    }
    boolean anyCoordinates(){
        return (!((mMeanedCoordinates == null) || (mMeanedCoordinates.size() == 0)));
    }



    GBCoordinateMean updateMean(GBActivity activity, GBNmea nmeaData){
        //increment the number of raw readings in the mean
        incRawReadings();
        //The reading must be fixed to be used in the meaning
        if (!nmeaData.isFixed()) {
            return null;
        } else {
            incFixedReadings();
        }

        //create a new coordinate for this nmeaData, then
        // add it to the list of locations being meaned.
        GBCoordinateWGS84 wgsCoordinate = new GBCoordinateWGS84(activity, nmeaData);
        addCoordinate(wgsCoordinate);

        //there have to be enough values accumulated to perform the mean
        if (getCoordinateSize() < 2) return null;

        //calculate the mean using the coordinates and flags in the meanToken structure
        GBCoordinateMean meanCoordinate = getMeanCoordinate();
        if (meanCoordinate == null){
            meanCoordinate = new GBCoordinateMean();
            setMeanCoordinate(meanCoordinate);
        }
        meanCoordinate.updateMean(nmeaData.getSatellites(), this);


        return meanCoordinate;
    }

    GBCoordinateMean recalculateMean(){
        GBCoordinateMean coordinateMean = new GBCoordinateMean();
        setMeanCoordinate(coordinateMean);
        mMeanCoordinate.updateMean(0, this);
        return coordinateMean;
    }


    Bundle saveState(Bundle outState){
        //0 = false, 1 = true
        int tempBoolean = 0; //default = false
        if (isMeanInProgress())tempBoolean = 1;
        outState.putInt(IS_MEANING, tempBoolean);

        tempBoolean = 0;
        if (isFirstPointInMean())tempBoolean = 1;
        outState.putInt(IS_FIRST, tempBoolean);

        tempBoolean = 0;
        if (isLastPointInMean()) tempBoolean = 1;
        outState.putInt(IS_LAST, tempBoolean);

        outState.putDouble(CURRENT_MEAN_TIME,getCurrentMeanTime());
        outState.putDouble(START_MEAN_TIME,  getStartMeanTime());
        outState.putInt   (FIXED_READING,    getFixedReadings());
        outState.putInt   (RAW_READING,      getRawReadings());

        getMeanCoordinate(false).saveState(outState);
        return outState;
    }

}
