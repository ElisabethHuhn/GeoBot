package com.asc.msigeosystems.geobot;

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
    static final String sProjectTag       = "PROJECT_OBJECT";
    static final String sProjectNameTag   = "PROJECT_NAME";
    static final String sProjectIDTag     = "PROJECT_ID";
    static final String sProjectCreateTag = "PROJECT_CREATION";
    static final String sProjectMaintTag  = "PROJECT_MAINTAINED";
    static final String sProjectDescTag   = "PROJECT_DESCRIPTION";
    static final String sProjectCoordType = "PROJECT_COORDINATE_TYPE";
    //static final String sProjectSettingsTag = "PROJECT_SETTINGS";
    //static final String sProjectPointsTag = "PROJECT_POINTS";

   // static final int    sProjectDefaultsID   = -1;
    static final String sProjectDefaultName  = "Project ";
    /*static final String sProjectDefaultsDesc =
            "This project represents the defaults that all other projects start with";
     */

    static final long   sProjectNewID   = -2;
    //static final String sProjectNewName = "Create Project";
    //static final String sProjectNewDesc = "";

    static final long   sFirstPointID   = 10001;

    static final int    sMeters         = 0;//values for Distance Units
    static final int    sFeet           = 1;
    static final String sMetersString = "Meters";
    static final String sFeetString   = "Feet";


    static final int    sEN             = 0;//values for EN v NE
    static final int    sNE             = 1;
    static final String sENString       = "Easting / Northing";
    static final String sNEString       = "Northing / Easting";


    static final int    sLatLng         = 0;//values for LatLng v LngLat
    static final int    sLngLat         = 1;
    static final String sLatLngString   = "Latitude / Longitude";
    static final String sLngLatString   = "Longitude / Latitude";


    static final int    sRMS            = 0;//values for RMS v StdDev
    static final int    sStdDev         = 1;
    static final String sRMSString      = "RMS";
    static final String sStdDevString   = "Standard Deviation";


    static final int    sDD             = 0;//values for DD v DMS
    static final int    sDMS            = 1;
    static final String sDDString       = "Digital Degrees";
    static final String sDMSString      = "Degrees Minutes Seconds";


    static final int    sDirections         = 0;//values for Directions v PlusMinus
    static final int    sPlusMinus          = 1;
    static final String sDirectionsString   = "Directions (N/S and E/W";
    static final String sPlusMinusString    = "Plus for N & E, Minus for S & W";

    //************************************/
    /*    Static (class) Variables       */
    //************************************/


    //************************************/
    /*    Member (instance) Variables    */
    //************************************/

    // Project Data
    private long         mProjectID;
    private CharSequence mName;
    private long         mDateCreated;//milliseconds since 1/1/70
    private long         mLastModified;
    private CharSequence mDescription;
    //Settings are linked to the project with an attribute on the project settings
    //which is the project ID the settings belong to
    private GBProjectSettings mSettings;

    //The coordinate type governs the type for all the points in the project
    //Once the first point is saved, this can no longer be saved.
    private int          mNextPointNumber; //next available point number within the project
    private int          mNumMean;      // number of raw points in a mean calculation
    private CharSequence mCoordinateType;
    private int          mDistanceUnits; //0= Meters, 1=Feet
    private int          mEnVNe;        //order of presentation on a screen
    private int          mRMSvStD;      //Does the user prefer RMS notation or Standard Deviation
    private int          mDDvDMS;       //Digital Degrees vs Degrees Minutes Seconds
    private int          mDIRvPlusMinus;//Are Lat/Lng  in directions or plus/minus
    private int          mLocPrecision; //number of digits of precision in UI
    private int          mStdDevPrecision;
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
        this.mDescription  = "";
        this.mDateCreated  = new Date().getTime();
        this.mLastModified = new Date().getTime();
        this.mCoordinateType = GBCoordinate.sCoordinateTypeClassWGS84;
        // TODO: 12/23/2016 Should we add in a default settings object here? 
        //this.mSettings     = new GBProjectSettings();
        this.mLocPrecision = GBUtilities.sHundredthsDigitsOfPrecision;
        this.mStdDevPrecision = GBUtilities.sHundredthsDigitsOfPrecision;
        this.mPoints       = new ArrayList<>();
        this.mPictures     = new ArrayList<>();

    }


    //*********************************************/
    //Setters and Getters
    //*********************************************/



    CharSequence getProjectName() {  return mName;    }
    void         setProjectName(CharSequence name) {
        this.mName = name;
    }

    long  getProjectID()        { return mProjectID; }
    void  setProjectID(long id) {
        this.mProjectID = id;
        GBProjectSettings projectSettings = this.getSettings();
        if (projectSettings != null) {
            projectSettings.setProjectID(id);
        }
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

    CharSequence getProjectCoordinateType() { return mCoordinateType;   }
    void setProjectCoordinateType(CharSequence coordinateType){mCoordinateType = coordinateType;}


    int getDistanceUnits() {
        return mDistanceUnits;
    }
    void setDistanceUnits(int distanceUnits) {
        mDistanceUnits = distanceUnits;
    }

    int getNumMean() {
        return mNumMean;
    }
    void setNumMean(int numMean) {
        mNumMean = numMean;
    }

    int getEnVNe() {
        return mEnVNe;
    }
    void setEnVNe(int enVNe) {
        mEnVNe = enVNe;
    }

    int getNextPointNumber() {
        return mNextPointNumber;
    }
    void setNextPointNumber(int nextPointNumber) {
        mNextPointNumber = nextPointNumber;
    }

    int getLocPrecision() {
        return mLocPrecision;
    }
    void setLocPrecision(int locPrecision) {
        mLocPrecision = locPrecision;
    }

    int getStdDevPrecision() {
        return mStdDevPrecision;
    }
    void setStdDevPrecision(int stdDevPrecision) {
        mStdDevPrecision = stdDevPrecision;
    }

    int getRMSvStD() {
        return mRMSvStD;
    }
    void setRMSvStD(int RMSvStD) {
        mRMSvStD = RMSvStD;
    }

    int getDDvDMS() {
        return mDDvDMS;
    }
    void setDDvDMS(int DDvDMS) {
        mDDvDMS = DDvDMS;
    }

    int getDIRvPlusMinus() {
        return mDIRvPlusMinus;
    }
    void setDIRvPlusMinus(int dIRvPlusMinus) {
        mDIRvPlusMinus = dIRvPlusMinus;
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

        return getPoints().size();
    }

    GBProjectSettings getSettings(){
        if (!areSettingsInMemory()) {
            GBDatabaseManager databaseManager = GBDatabaseManager.getInstance();
            mSettings = databaseManager.getProjectSettings(mProjectID);
        }

        if (mSettings == null){
            mSettings = new GBProjectSettings();
        }
        return mSettings;
    }
    boolean areSettingsInMemory(){
        return (!(mSettings == null));

    }
    void              setSettings(GBProjectSettings settings) {mSettings = settings;}

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
    /*       methods dealing with determining the next project ID         */
    //**********************************************************************/
    static long getNextProjectID(GBActivity activity){
        GBGlobalSettings globalSettings = GBGlobalSettings.getInstance();

        return globalSettings.getNextProjectID(activity);
    }

    static long getPotentialNextID(GBActivity activity){

        return GBGlobalSettings.getInstance().getPotentialNextProjectID(activity);

    }

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
