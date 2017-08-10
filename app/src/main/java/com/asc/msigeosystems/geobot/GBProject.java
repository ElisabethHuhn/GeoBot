package com.asc.msigeosystems.geobot;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Elisabeth Huhn on 5/7/2016.
 *
 * This is the Principal Data Model Object.
 * A Project contains points
 *
 */
public class GBProject {
    //************************************/
    /*    Static (class) Constants       */
    //************************************/



    //Tags for fragment arguments
    //private static final String sProjectTag       = "PROJECT_OBJECT";
    private static final String sProjectNameTag   = "PROJECT_NAME";
    private static final String sProjectIDTag     = "PROJECT_ID";
    private static final String sProjectCreateTag = "PROJECT_CREATION";
    private static final String sProjectMaintTag  = "PROJECT_MAINTAINED";
    private static final String sProjectDescTag   = "PROJECT_DESCRIPTION";
    private static final String sProjectCoordType = "PROJECT_COORDINATE_TYPE";
    private static final String sProjectNxtPointNumbTag = "PROJECT_NXT_POINT_NUM";

    //private static final String sProjectPointsTag = "PROJECT_POINTS";

    private static final String sProjectDefaultName  = "Project ";

    //Tags for shared pref settings for whether the property was exported last time
    static final String sProjectReadabilityExportTag = "PROJECT_READABILITY_EXPORT";
    static final String sProjectHeadersExportTag   = "PROJECT_HEADERS_EXPORT";
    static final String sProjectIDExportTag        = "PROJECT_ID_EXPORT";
    static final String sProjectNameExportTag      = "PROJECT_NAME_EXPORT";
    static final String sProjectCreateExportTag    = "PROJECT_CREATION_EXPORT";
    static final String sProjectLastMaintExportTag = "PROJECT_MAINTAINED_EXPORT";
    static final String sProjectDescExportTag      = "PROJECT_DESCRIPTION_EXPORT";
    static final String sProjectHeightExportTag    = "PROJECT_HEIGHT_EXPORT";
    static final String sProjectCoordTypeExportTag = "PROJECT_COORDINATE_TYPE_EXPORT";
    static final String sProjectNbMeanExportTag    = "PROJECT_NB_MEAN_EXPORT";
    static final String sProjectZoneExportTag      = "PROJECT_ZONE_EXPORT";
    static final String sProjectDistUnitsExportTag = "PROJECT_DIST_UNITS_EXPORT";
    static final String sProjectAutosaveExportTag  = "PROJECT_AUTOSAVE_EXPORT";
    static final String sProjectRmsVStdExportTag   = "PROJECT_RMS_V_STD_EXPORT";
    static final String sProjectUIOrderExportTag   = "PROJECT_UI_ORDER_EXPORT";
    static final String sProjectDDvDMSExportTag    = "PROJECT_DD_V_DMS_EXPORT";
    static final String sProjectDirVPMExportTag    = "PROJECT_DIR_V_PM_EXPORT";
    static final String sProjectDataSrcExportTag   = "PROJECT_DATA_SRC_EXPORT";
    static final String sProjectLocPrcExportTag    = "PROJECT_LOC_PRC_EXPORT";
    static final String sProjectStdPrcExportTag    = "PROJECT_STD_PRC_EXPORT";





    //These constants define the order of the corresponding spinner positions
    static final int    sMeters         = 0;//values for Distance Units
    static final int    sFeet           = 1;
    static final int    sIntFeet        = 2; //international feet
    static final String sMetersString   = "Meters";
    static final String sFeetString     = "Feet";
    static final String sIntFeetString  = "International Feet";



    static final int sDataSourceNoneSelected            = 0;
    static final int sDataSourceWGSManual               = 1;
    static final int sDataSourceSPCSManual              = 2;
    static final int sDataSourceUTMManual               = 3;
    static final int sDataSourcePhoneGps                = 4;
    static final int sDataSourceExternalGps             = 5;
    static final int sDataSourceCellTowerTriangulation  = 6;



    //************************************/
    /*    Static (class) Variables       */
    //************************************/


    //************************************/
    /*    Member (instance) Variables    */
    //************************************/

    // Project Data
    private long         mProjectID;
    //private long         mProjectNumber;  // TODO: 7/5/2017 Do we need a Project Number
    private CharSequence mName;
    private long         mDateCreated;//milliseconds since 1/1/70
    private long         mLastModified;
    private CharSequence mDescription;
    private double       mHeight;       //device height added to all point elevations

    //The coordinate type governs the type for all the points in the project
    //Once the first point is saved, this can no longer be saved.
    //private CharSequence mCoordinateType;
    private int          mCoordinateDBType;
    private int          mNumMean;       // number of raw points in a mean calculation
    private int          mZone;          //spc Zone
    private int          mDistanceUnits; //0= Meters, 1=Feet
    private int          mDataSource;    //eg phone gps vs external gps vs etc.

    private ArrayList<GBPoint>   mPoints;
    private ArrayList<GBPicture> mPictures;




    //**********************************************************************/
    /*  This methods dealing with an argument bundle adn a project         */
    //**********************************************************************/

    static Bundle putProjectInArguments(Bundle args, GBProject project) {


        //It will be some work to make all of the data model serializable
        //so for now, just pass the project values
        if (project == null){
            //we are creating the project for the first time, so just store that flag
            args.putLong(GBProject.sProjectIDTag,    GBUtilities.ID_DOES_NOT_EXIST);
        } else {
            args.putLong        (GBProject.sProjectIDTag,    project.getProjectID());
            args.putCharSequence(GBProject.sProjectNameTag,  project.getProjectName());
            args.putLong        (GBProject.sProjectCreateTag,project.getProjectDateCreated());
            args.putLong        (GBProject.sProjectMaintTag, project.getProjectLastModified());
            args.putCharSequence(GBProject.sProjectDescTag,  project.getProjectDescription());
            args.putCharSequence(GBProject.sProjectCoordType,project.getProjectCoordinateType());
        }
        return args;
    }

    static GBProject getProjectFromArguments(Bundle args){

        GBProject project;

        long projectID       = args.getLong(GBProject.sProjectIDTag);

        if (projectID == GBUtilities.ID_DOES_NOT_EXIST){
            //the project is being created for the first time,
            // just pass a default project that isn't in the DB
            project = new GBProject();
        } else {
            //go find the project in either memory or the DB
            GBProjectManager projectManager = GBProjectManager.getInstance();
            project = projectManager.getProject(projectID);

            if (project == null){
                project = new GBProject();
            }
        }

        return project;
    }



    //************************************************************
    // *             Constructor
    //*************************************************************/

    //Default constructor, A dummy project that is not in memory list or in the DB
    //Every field is initialized. No Nulls Allowed
    public GBProject() {
        initializeNoID();
    }
    
    
    public GBProject(GBActivity activity, CharSequence projectName, long projectID) {
        initializeNoID();
        this.mName         = projectName;
        if (projectID == GBUtilities.ID_DOES_NOT_EXIST){
            GBGlobalSettings globalSettings = GBGlobalSettings.getInstance();

            this.mProjectID = globalSettings.getNextProjectID(activity);
        } else {
            this.mProjectID = projectID;
        }
    }

    public GBProject(GBActivity activity, CharSequence projectName){
        initializeDefaultVariables(activity);
        this.mName = projectName;
    }


    private void initializeDefaultVariables(GBActivity activity){
        this.mProjectID    = GBUtilities.ID_DOES_NOT_EXIST;
        initializeNoID();
        //mSettings.setProjectID(mProjectID);
    }

    private void initializeNoID(){
        //defaults for the project
        this.mName         = sProjectDefaultName;
        this.mDateCreated  = new Date().getTime();
        this.mLastModified = new Date().getTime();
        this.mDescription  = "";
        this.mHeight       = 0d;
        this.mCoordinateDBType = GBCoordinate.sCoordinateDBTypeWGS84;
        this.mNumMean       = 10;
        this.mZone          = 0;
        this.mDistanceUnits = sMeters;
        this.mDataSource    = sDataSourcePhoneGps;

        this.mPoints       = new ArrayList<>();
        this.mPictures     = new ArrayList<>();

    }


    //*********************************************/
    //Setters and Getters
    //*********************************************/


    long  getProjectID()        { return mProjectID; }
    void  setProjectID(long id) {
        this.mProjectID = id;

    }


    CharSequence getProjectName() {  return mName;    }
    void         setProjectName(CharSequence name) {
        this.mName = name;
    }



    long getProjectDateCreated()                  {return mDateCreated; }
    void setProjectDateCreated(long dateCreated) { this.mDateCreated = dateCreated;  }

    long getProjectLastModified() { return mLastModified;    }
    void setProjectLastModified(long lastModified) { this.mLastModified = lastModified; }

    CharSequence getProjectDescription() {
        return mDescription;
    }
    void setProjectDescription(CharSequence description) {
        this.mDescription = description;
    }


    private String getNextPtNbTag (){
        return sProjectNxtPointNumbTag + "|" + String.valueOf(getProjectID());
    }
    int  getNextPointNumber(GBActivity activity) {
        if (activity == null){
            return (int)GBUtilities.ID_DOES_NOT_EXIST;
        }
        String nextPtNumTag = getNextPtNbTag();
        SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
        int defaultValue = 100;
        return sharedPref.getInt(nextPtNumTag, defaultValue);
    }
    void setNextPointNumber(GBActivity activity, int nextPointNumber) {
        if (activity == null){
            return ;
        }

        String nextPtNumTag = getNextPtNbTag();
        //Store the next point number for the next time
        SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(nextPtNumTag, nextPointNumber);
        editor.apply();

    }
    void incrementPointNumber(GBActivity activity) {
        if (activity == null){
            return ;
        }
        int nextPointNumber = getNextPointNumber(activity);
        nextPointNumber++;
        setNextPointNumber(activity, nextPointNumber);
    }


    double getHeight() {
        return mHeight;
    }
    void   setHeight(double height) {
        mHeight = height;
    }



    CharSequence getProjectCoordinateType() {

        String type;

        switch(mCoordinateDBType) {
            case GBCoordinate.sCoordinateDBTypeWGS84:
                type = GBCoordinate.sCoordinateTypeWGS84;
                break;

            case GBCoordinate.sCoordinateDBTypeSPCS:
                type = GBCoordinate.sCoordinateTypeSPCS;
                break;

            case GBCoordinate.sCoordinateDBTypeUTM:
                type = GBCoordinate.sCoordinateTypeUTM;
                break;

            case GBCoordinate.sCoordinateDBTypeNAD83:
                type = GBCoordinate.sCoordinateTypeNAD83;
                break;

            default:
                type = GBCoordinate.sCoordinateTypeUnknown;
        }
            return type;
    }
    void setProjectCoordinateType(CharSequence coordinateType){
        int type;

        if (coordinateType.equals(GBCoordinate.sCoordinateTypeWGS84)){
            type = GBCoordinate.sCoordinateDBTypeWGS84;
        } else if (coordinateType.equals(GBCoordinate.sCoordinateTypeSPCS)){
            type = GBCoordinate.sCoordinateDBTypeSPCS;
        } else if (coordinateType.equals(GBCoordinate.sCoordinateTypeUTM)){
            type = GBCoordinate.sCoordinateDBTypeUTM;
        } else if (coordinateType.equals(GBCoordinate.sCoordinateTypeNAD83)){
            type = GBCoordinate.sCoordinateDBTypeNAD83;
        } else {
            type = GBCoordinate.sCoordinateDBTypeWGS84;
        }
        setCoordinateType(type);
    }

    int  getCoordinateType()                  { return mCoordinateDBType;   }
    void setCoordinateType(int coordinateType){mCoordinateDBType = coordinateType;}


    int  getNumMean() {
        return mNumMean;
    }
    void setNumMean(int numMean) {
        mNumMean = numMean;
    }


    int  getZone() {
        return mZone;
    }
    void setZone(int zone) {
        mZone = zone;
    }

    int  getDistanceUnits() {
        return mDistanceUnits;
    }
    String getDistUnitString() {
        CharSequence duString;

        if (getDistanceUnits() == GBProject.sMeters){
            duString = sMetersString;
        } else if (getDistanceUnits() == GBProject.sFeet){
            duString = sFeetString;
        } else {//international feet
            duString = sIntFeetString;
        }
        return duString.toString();

    }
    void setDistanceUnits(int distanceUnits) {
        mDistanceUnits = distanceUnits;
    }



    int  getDataSource() {
        return mDataSource;
    }
    void setDataSource(int dataSource) {
        mDataSource = dataSource;
    }


     //The cascade objects aren't pulled from the DB unless they are explicityly asked for
    //Creating a Project Object from the DB is not enough to populate the cascading objects
    //They are not pulled from the DB unless explicitly asked for
    ArrayList<GBPoint> getPoints(){
        if (getProjectID() == GBUtilities.ID_DOES_NOT_EXIST)return mPoints;

        if (!arePointsInMemory()) {
            GBDatabaseManager databaseManager = GBDatabaseManager.getInstance();
            mPoints = databaseManager.getPointsForProjectFromDB(mProjectID);
        }
        return mPoints;
    }
    void               setPoints(ArrayList<GBPoint> points) {mPoints = points;  }
    boolean arePointsInMemory(){
        return (!((mPoints == null) || (mPoints.size()==0))) ;

    }
    int getSize(){

        int size;
        if (arePointsInMemory()){
            //the points have already been read in from the DB, count them
            size = getPoints().size();
        } else {
            //the points are still in the DB, count the DB rows of interest
            size = (int) GBDatabaseManager.getInstance().getProjectSize(getProjectID());
        }
        return size;
    }

    ArrayList<GBPicture> getPictures(){
        if (!isPicturesChanged()) {
            GBDatabaseManager databaseManager = GBDatabaseManager.getInstance();
            mPictures = databaseManager.getProjectPicturesFromDB(mProjectID);
        }
        return mPictures;
    }
    void setPictures(ArrayList<GBPicture> pictures) {mPictures = pictures;  }
    boolean isPicturesChanged(){
        return (!((mPictures == null) || (mPictures.size()==0)));
    }


    //************************************/
    /*          Static Methods           */
    //************************************/


    //**********************************************************************/
    /*         convert a project to exchange format                        */
    //**********************************************************************/

    String convertToCDFMillisecond(){

        return  String.valueOf(this.getProjectID()    + ", " +
                this.getProjectName()                 + ", " +
                this.getProjectDateCreated()          + ", " +
                this.getProjectLastModified()         + ", " +
                this.getProjectDescription())         + ", " +
                this.getProjectCoordinateType()       +
                System.getProperty("line.separator");
    }
    String convertToCDF(){

        return  String.valueOf(this.getProjectID()                + ", " +
                this.getProjectName()                             + ", " +
                this.getDateString(this.getProjectDateCreated())  + ", " +
                this.getDateString(this.getProjectLastModified()) + ", " +
                this.getProjectDescription())                     + ", " +
                this.getProjectCoordinateType()                   +
                System.getProperty("line.separator");
    }

    //************************************/
    /*          Member Methods           */
    //************************************/

    //returns null if the point is not found on the project
    GBPoint getPoint(long pointID){
        GBPoint point;
        int last = getPoints().size();
        for(int position = 0; position < last; position++){
            point = getPoints().get(position);
            if (point.getPointID() == pointID)return point;
        }
        return null;
    }




    String getProjectDateCreatedString() {
        return getDateString(mDateCreated);
    }
    boolean setProjectDateCreatedString(String dateString){
        long milliseconds = setDateString(dateString);
        if (milliseconds == 0){
            return false;
        } else {
            mDateCreated = milliseconds;
            return true;
        }
    }

    String getProjectLastModifiedString() {
        return getDateString(mLastModified);
    }
    boolean setProjectLastModifiedString(String dateString){
        long milliseconds = setDateString(dateString);
        if (milliseconds == 0){
            return false;
        } else {
            mLastModified = milliseconds;
            return true;
        }
    }

    // TODO: 6/10/2017 move to utilities Remember Offsets
    String getDateString(long milliseconds) {
        return DateFormat.getDateInstance().format((new Date(milliseconds)));
    }

    long setDateString(String dateString){
        try {
            DateFormat df = DateFormat.getDateInstance(DateFormat.LONG, Locale.US);
            Date myDate = df.parse(dateString);
            return myDate.getTime();
        } catch (java.text.ParseException e){
            return 0;
        }
    }

    boolean addPicture(GBPicture picture){
        return getPictures().add(picture);
    }

    boolean addPoint (GBPoint point){
        return getPoints().add(point);
    }


    boolean removePoint(GBPoint point){
        GBPointManager pointManager = GBPointManager.getInstance();
        return pointManager.removePoint(getProjectID(), point);
    }



}
