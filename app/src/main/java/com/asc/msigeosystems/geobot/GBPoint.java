package com.asc.msigeosystems.geobot;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import java.util.ArrayList;

/**
 * Created by Elisabeth Huhn on 5/15/2016.
 *
 * One of the principal Data Classes of the model
 * A Point is contained within one and only one project
 */
class GBPoint {

    /* ***************************************************/
    /* ******    Static constants                *********/
    /* ***************************************************/

    //Tags for fragment arguments
    static final String sPointProjectIDTag  = "PROJECT_ID";
    static final String sPointTag           = "POINT_OBJECT";
    static final String sPointNameTag       = "POINT_NAME";
    static final String sPointIDTag         = "POINT_ID";
    static final String sPointEastingTag    = "POINT_EASTING";
    static final String sPointNorthingTag   = "POINT_NORTHING";
    static final String sPointElevationTag  = "POINT_ELEVATION";
    static final String sPointFCTag         = "POINT_FEATURE_CODE";
    static final String sPointNotesTag      = "POINT_NOTES";

    static final int    sPointDefaultsID   = -1;
    static final String sPointDefaultsDesc =
            "This point represents the defaults that all other projects start with";

    static final int    sPointNewID   = -2;
    static final String sPointNewDesc = "";


    //Tags for shared pref settings for whether the property was exported last time
    static final String sPointProjectIDExportTag  = "POINT_PROJECT_ID_EXPORT";
    static final String sPointNameExportTag       = "POINT_NAME_EXPORT";
    static final String sPointIsMeanedExportTag   = "POINT_IS_MEANED_EXPORT";
    static final String sPointCoordinateExportTag = "POINT_COORD_EXPORT";
    static final String sPointNumberExportTag    = "POINT_NUMBER_EXPORT";
    static final String sPointHeightExportTag    = "POINT_HEIGHT_EXPORT";
    static final String sPointFCExportTag        = "POINT_FC_EXPORT";
    static final String sPointNotesExportTag     = "POINT_NOTES_EXPORT";
    static final String sPointTokenExportTag     = "POINT_TOKEN_EXPORT";
    static final String sPointOffDistExportTag   = "POINT_OFF_DIST_EXPORT";
    static final String sPointOffHeadExportTag   = "POINT_OFF_HEAD_EXPORT";
    static final String sPointOffEleExportTag    = "POINT_OFF_ELE_EXPORT";
    static final String sPointHdopExportTag      = "POINT_HDOP_EXPORT";
    static final String sPointVdopExportTag      = "POINT_VDOP_EXPORT";
    static final String sPointPdopExportTag      = "POINT_PDOP_EXPORT";
    static final String sPointTdopExportTag      = "POINT_TDOP_EXPORT";
    static final String sPointHrmsExportTag      = "POINT_HRMS_EXPORT";
    static final String sPointVrmsExportTag      = "POINT_VRMS_EXPORT";



    static boolean getPointExport (GBActivity activity, String tag) {
        if (activity == null){
            return false;
        }
        SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
        boolean defaultValue = true;
        return sharedPref.getBoolean(tag, defaultValue);
    }
    static void    setPointExport  (GBActivity activity, String tag, boolean isExported) {
        if (activity == null){
            return;
        }
        //Store the PersonID for the next time
        SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(tag, isExported);
        editor.apply();
    }



    /* ***********************************/
    /*    Static (class) Variables       */
    /* ***********************************/


    /* ***********************************/
    /*    Member (instance) Variables    */
    /* ***********************************/

    /* ***************************************************/
    /* ******    Attributes stored in the DB     *********/
    /* ***************************************************/

    private long         mPointID;
    private long         mForProjectID;

    //Actual location of point is given by Coordinate
    private long         mHasACoordinateID;

    private int          mPointNumber;
    private long         mMeanTokenID;

    //The original offset. The Coordinate has this offset calculated into it
    private double       mOffsetDistance;
    private double       mOffsetHeading;
    private double       mOffsetElevation;

    //Original height due to artificial means, e.g. tripod
    private double       mHeight;

    private CharSequence mPointFeatureCode;
    private CharSequence mPointNotes;


    //Quality fields
    private double      mHdop;
    private double      mVdop;
    private double      mTdop;
    private double      mPdop;
    private double      mHrms;
    private double      mVrms;


    private ArrayList<GBPicture> mPictures;




    /* **************************************************************/
    /*               Static Methods                                 */
    /* **************************************************************/

    static Bundle putPointInArguments(Bundle args, GBPoint point) {

        if (point == null){
            //This  happens when the point is being created by this fragment on save
            args.putLong(GBPoint.sPointIDTag,GBUtilities.ID_DOES_NOT_EXIST);
        } else {
            args.putLong(GBPoint.sPointIDTag, point.getPointID());
        }
        //assume all other attributes exist on the point being managed by the PointManager
        return args;
    }


    static GBPoint getPointFromArguments(GBActivity activity, Bundle args) {
        GBPoint point = null;
        long pointID   = args.getLong (GBPoint.sPointIDTag);

        long openProjectID = GBUtilities.getInstance().getOpenProjectID(activity);

        //If we are on the Create Path, we won't have a point here
        if (pointID != GBUtilities.ID_DOES_NOT_EXIST) {
            point = GBPointManager.getInstance().getPoint(openProjectID, pointID);

        } else {
            point = initializePoint(activity);
        }

        return point;
    }

    private static GBPoint initializePoint(GBActivity activity){
        //A project must be open for this to work
        GBProject openProject = GBUtilities.getInstance().getOpenProject(activity);
        if (openProject == null)return null;

        GBPoint point = new GBPoint();
        point.setPointID(GBUtilities.ID_DOES_NOT_EXIST);

        point.setForProjectID(openProject.getProjectID());
        point.setHeight(openProject.getHeight());
        point.setPointNumber(openProject.getNextPointNumber(activity));
        //the point number is not incremented until the point is saved for the first time
        //The SQL Helper is in charge of assigning both
        // the DB ID and then incrementing the point number
        //openProject.incrementPointNumber(activity);
        return point;
    }


    /* ***********************************/
    /*         CONSTRUCTORS              */
    /* ***********************************/

    /* ***************************************************/
    /* ******    Constructors                    *********/
    /* ***************************************************/
    //Need to know what project the new point will be in
    GBPoint(GBProject project) {

        initializeDefaultVariables();
        //initialize all variables so we are assured that none are null
        //that way we never have to check for null later
        this.mForProjectID = project.getProjectID();
        this.mPointID      = GBUtilities.ID_DOES_NOT_EXIST;
    }


    //a coule of special case points flag "create new point" and "open a point" path
    GBPoint(int specialPointID) {
        initializeDefaultVariables();

        this.mForProjectID     = specialPointID;
        this.mPointID          = specialPointID ;
        this.mPointFeatureCode = "A special point";
    }

    GBPoint() {
        initializeDefaultVariables();
    }



    /* ***************************************************/
    /* ******    Setters and Getters             *********/
    /* ***************************************************/



    long getForProjectID()                  {  return mForProjectID;    }
    void setForProjectID(long forProjectID) {  this.mForProjectID = forProjectID;  }

    long getPointID()             {  return mPointID;    }
    void setPointID(long pointID) {  this.mPointID = pointID;  }

    long getHasACoordinateID()                    {return mHasACoordinateID; }
    void setHasACoordinateID(long isACoordinateID) { mHasACoordinateID = isACoordinateID; }

    GBCoordinate getCoordinate()                {

        GBDatabaseManager databaseManager = GBDatabaseManager.getInstance();
        return databaseManager.getCoordinateFromDB(getHasACoordinateID());
    }
    long setCoordinate(GBCoordinate coordinate) {
        if (coordinate != null){

            GBCoordinateManager coordinateManager = GBCoordinateManager.getInstance();
            long coordinateID = coordinateManager.addCoordinate(coordinate);
            // TODO: 7/9/2017 assure the ID coming back from teh DB is the same as recorded on the coordinate instance 
            coordinateID = coordinate.getCoordinateID();
            setHasACoordinateID(coordinateID);
            return coordinateID;
        }
        return GBUtilities.ID_DOES_NOT_EXIST;
     }

    int getPointNumber() {  return mPointNumber;   }
    void setPointNumber(int pointNumber) {  mPointNumber = pointNumber; }

    long getMeanTokenID() {  return mMeanTokenID;   }
    void setMeanTokenID(long meanTokenID) {  mMeanTokenID = meanTokenID; }

    GBMeanToken getMeanToken()                {
        GBMeanTokenManager tokenManager = GBMeanTokenManager.getInstance();
        return tokenManager.getMeanTokenFromDB(getMeanTokenID());
    }
    long setMeanToken(GBMeanToken token) {
        if (token != null) {
            GBMeanTokenManager tokenManager = GBMeanTokenManager.getInstance();
            tokenManager.addMeanToDB(token);
            long tokenID = token.getMeanTokenID();
            setMeanTokenID(tokenID);
            return  tokenID;
        }
        return GBUtilities.ID_DOES_NOT_EXIST;
    }

    double getOffsetDistance() {  return mOffsetDistance;   }
    void setOffsetDistance(double offsetDistance) {  mOffsetDistance = offsetDistance; }

    double getOffsetHeading() {   return mOffsetHeading;}
    void setOffsetHeading(double offsetHeading) { mOffsetHeading = offsetHeading;   }

    double getOffsetElevation() {  return mOffsetElevation;  }
    void setOffsetElevation(double offsetElevation) { mOffsetElevation = offsetElevation; }


    double getHeight()              {  return mHeight; }
    void   setHeight(double height) { mHeight = height; }

    CharSequence getPointFeatureCode() { return mPointFeatureCode;  }
    void setPointFeatureCode(CharSequence description) { mPointFeatureCode = description;  }

    CharSequence getPointNotes() {  return mPointNotes;   }
    void setPointNotes(CharSequence notes) { mPointNotes = notes;   }

    double getHdop() {
        return mHdop;
    }
    void   setHdop(double hdop) {
        mHdop = hdop;
    }

    double getVdop() {
        return mVdop;
    }
    void   setVdop(double vdop) {
        mVdop = vdop;
    }

    double getTdop() {
        return mTdop;
    }
    void   setTdop(double tdop) {
        mTdop = tdop;
    }

    double getPdop() {
        return mPdop;
    }
    void   setPdop(double pdop) {
        mPdop = pdop;
    }

    double getHrms() {
        return mHrms;
    }
    void   setHrms(double hrms) {
        mHrms = hrms;
    }

    double getVrms() {
        return mVrms;
    }
    void   setVrms(double vrms) {
        mVrms = vrms;
    }

    //Cascading objects are not pulled from the DB until
    // explicitly reqested by a call to getPictures();
    ArrayList<GBPicture> getPictures(){
        if (!isPicturesChanged()) {
            GBDatabaseManager databaseManager = GBDatabaseManager.getInstance();
            mPictures = databaseManager.getPointPicturesFromDB(getForProjectID(), getPointID());
        }
        return mPictures;
    }
    boolean isPicturesChanged(){
        return (!((mPictures == null) || (mPictures.size()==0)));
    }
    void   setPictures(ArrayList<GBPicture> pictures) {  mPictures = pictures; }

    boolean addPicture(GBPicture picture){
        return getPictures().add(picture);
    }



    /* ***************************************************/
    /* ******    Private Member Methods          *********/
    /* ***************************************************/

    void initializeDefaultVariables(){
        this.mForProjectID     = GBUtilities.ID_DOES_NOT_EXIST;
        this.mPointID          = GBUtilities.ID_DOES_NOT_EXIST;
        this.mHasACoordinateID = GBUtilities.ID_DOES_NOT_EXIST;
        this.mPointNumber      = 0;
        this.mMeanTokenID      = GBUtilities.ID_DOES_NOT_EXIST;

        this.mOffsetDistance   = 0d;
        this.mOffsetHeading    = 0d;
        this.mOffsetElevation  = 0d;

        this.mHeight           = 0d;

        this.mPointFeatureCode = "";
        this.mPointNotes       = "";

        this.mHdop = 0d;
        this.mVdop = 0d;
        this.mTdop = 0d;
        this.mPdop = 0d;
        this.mHrms = 0d;
        this.mVrms = 0d;

        this.mPictures         = new ArrayList<>();

    }


    //Convert point to comma delimited file for exchange
    String convertToCDF() {
        // TODO: 7/2/2017 routine needs to be updated
        return String.valueOf(this.getPointID()) + ", " +
                this.getPointFeatureCode()       + ", " +
                this.getPointNotes()             + ", " +
                this.getHdop()                   + ", " +
                this.getVdop()                   + ", " +
                this.getTdop()                   + ", " +
                this.getPdop()                   + ", " +
                this.getHrms()                   + ", " +
                this.getVrms()                   + ", " +
                this.getOffsetDistance()         + ", " +
                this.getOffsetHeading()          + ", " +
                this.getOffsetElevation()        + ", " +
                this.getHeight()                 + ", " +
                //todo 12/13/2016 have to add in coordinates here
                "plus coordinate positions "      +
                System.getProperty("line.separator");
    }


}
