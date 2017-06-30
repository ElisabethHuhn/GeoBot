package com.asc.msigeosystems.geobot;

import android.content.Context;
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
    // TODO: 6/10/2017 Coordinate should not be stored on Point, only the CoordinateID. But have to know the type as well as the ID
    // TODO: 6/11/2017 The coordinate type is on the project, is this sufficient??? 
    private GBCoordinate mCoordinate;

    private int          mPointNumber;

    //The original offset. The Coordinate has this offset calculated into it
    private double       mOffsetDistance;
    private double       mOffsetHeading;
    private double       mOffsetElevation;

    //Original height due to artificial means, e.g. tripod
    private double       mHeight;

    private CharSequence mPointFeatureCode;
    private CharSequence mPointNotes;


    private ArrayList<GBPicture> mPictures;

    //Quality fields
    private double      mHdop;
    private double      mVdop;
    private double      mTdop;
    private double      mPdop;
    private double      mGdop;
    private double      mHrms;
    private double      mVrms;





    /* **************************************************************/
    /*               Static Methods                                 */
    /* **************************************************************/

    static Bundle putPointInArguments(Bundle args, long projectID, GBPoint point) {

        args.putLong         (GBPoint.sPointProjectIDTag, projectID);
        if (point == null){
            //This  happens when the point is being created by this fragment on save
            args.putLong(GBPoint.sPointIDTag,0);
        } else {
            args.putLong(GBPoint.sPointIDTag, point.getPointID());
        }
        //assume all other attributes exist on the point being managed by the PointManager
        return args;
    }


    static GBPoint getPointFromArguments(Context activity, Bundle args) {
        GBPoint point = null;
        long projectID = args.getLong (GBPoint.sPointProjectIDTag);
        long pointID   = args.getLong (GBPoint.sPointIDTag);

        //If we are on the Create Path, we won't have a point here
        if (pointID != GBUtilities.ID_DOES_NOT_EXIST) {

            point = GBPointManager.getInstance().getPoint(projectID, pointID);


        } else {
            //there is no existing point, it is being created

            //Start with an empty point with default values
            point = new GBPoint();
            //set the bogus point ID
            point.setPointID(GBUtilities.ID_DOES_NOT_EXIST);
            //save the project ID
            point.setForProjectID(projectID);
        }

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

    GBCoordinate getCoordinate()                { return mCoordinate;  }
    void setCoordinate(GBCoordinate coordinate) { mCoordinate = coordinate; }

    int getPointNumber() {  return mPointNumber;   }
    void setPointNumber(int pointNumber) {  mPointNumber = pointNumber; }

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

    // TODO: 6/13/2017 This is probably not saved in the DB
    double getGdop() {
        return mGdop;
    }
    void   setGdop(double gdop) {
        mGdop = gdop;
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
        this.mCoordinate       = null;

        this.mOffsetDistance   = 0d;
        this.mOffsetHeading    = 0d;
        this.mOffsetElevation  = 0d;

        this.mHeight           = 0d;

        this.mPointFeatureCode = "";
        this.mPointNotes       = "";

          this.mPictures         = new ArrayList<>();
        this.mHdop = 0d;
        this.mVdop = 0d;
        this.mTdop = 0d;
        this.mPdop = 0d;
        this.mGdop = 0d;
        this.mHrms = 0d;
        this.mVrms = 0d;

    }


    //Convert point to comma delimited file for exchange
    String convertToCDF() {
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
