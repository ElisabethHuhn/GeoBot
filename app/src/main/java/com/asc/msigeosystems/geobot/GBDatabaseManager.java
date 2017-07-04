package com.asc.msigeosystems.geobot;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by Elisabeth Huhn on 6/10/17. Adapted from MedMinder and Prism4D
 * This manager hides the CRUD routines of the DB.
 * Originally this is a pass through layer, but if background threads are required to get
 * IO off the UI thread, this manager will maintain them.
 * This manager is a singleton that holds the one connection to the DB for the app.
 * This connection is opened when the app is first initialized, and never closed
 */
class GBDatabaseManager {
    private static final String TAG = "GBDatabaseManager";
    static final long sDB_ERROR_CODE = -1;


    // ***********************************************/
    /*         static variables                     */
    // ***********************************************/

    private static GBDatabaseManager sManagerInstance;

    private static String sNotInitializedException =
            "Attempt to access the database before it has been initialized";


    // ***********************************************/
    /*         Instance variables                   */
    // ***********************************************/

    private GBDatabaseSqliteHelper mDatabaseHelper;
    private SQLiteDatabase mDatabase;



    // ***********************************************/
    /*         static methods                       */
    // ***********************************************/


    /*********************
     * This method initializes the singleton Database Manager
     *
     * The database Manager holds onto a single instance of the helper connection
     *    to the database.
     *
     * The purpose of a singleton connection is to keep the app threadsafe
     *    in the case of attempted concurrent access to the database.
     *
     * There can be no concurrent access to the database from multiple threads,
     *    as there is only one connection, it can be accessed serially,
     *    from only one thread at a time.
     *
     * Thie lifetime of this singleton is the execution lifetime of the App,
     *    thus the application context is passed, not the activity context
     *
     * synchronized method to ensure only 1 instance exists
     *
     * @param context               The application context
     * @throws RuntimeException     Thrown if there is no context passed
     *
     * USAGE
     * GBDatabaseManager.initializeInstance(getApplicationContext());
     */
    private static synchronized GBDatabaseManager initializeInstance(Context context)
            throws RuntimeException {

        //Note the hard coded strings here.
        // todo: figure out how to access the string resources from the DatabaseManager without a context
        String sNoContextException = "Can not create database without a context";

        if (sManagerInstance == null) {
            try {
                sManagerInstance = new GBDatabaseManager();
            } catch (Exception e) {
                Log.e(TAG, Log.getStackTraceString(e));
            }
        }
        if (sManagerInstance.getDatabaseHelper() == null) {
            if (context == null) throw new RuntimeException(sNoContextException);

            try {
                //all the constructor does is save the context
                sManagerInstance.setDatabaseHelper(new GBDatabaseSqliteHelper(context));
            } catch (Exception e) {
                Log.e(TAG, Log.getStackTraceString(e));
            }
        }
        if (sManagerInstance.getDatabase() == null) {
            if (context == null) throw new RuntimeException(sNoContextException);

            try {
                sManagerInstance.setDatabase(sManagerInstance.getDatabaseHelper().getWritableDatabase());
            } catch (Exception e) {
                Log.e(TAG, Log.getStackTraceString(e));
            }
        }

        return sManagerInstance;
    }


    //returns null if the Database Manager has not yet bee initialized
    //in that case, initializeInstance() must be called first
    // We can't just fix the problem, as we need the application context
    //because this is an error condition. Treat it as an error
    public static synchronized GBDatabaseManager getInstance() throws RuntimeException {
        //The reason we can't just initialize it now is because we need a context to initialize
        if (sManagerInstance == null) {
            throw new RuntimeException(sNotInitializedException);
        }
        return sManagerInstance;
    }

    //If we do happen to have a context, we can initialize
    public static synchronized GBDatabaseManager getInstance(Context context)
            throws RuntimeException {
        //The reason we can't just initialize it now is because we need a context to initialize
        if (sManagerInstance == null) {
            if (context == null) throw new RuntimeException(sNotInitializedException);
            GBDatabaseManager.initializeInstance(context);
        }
        return sManagerInstance;
    }


    // ***********************************************/
    /*         constructor                          */
    // ***********************************************/
    //null constructor. It should never be called.
    //    initializeInstance() is the proper protocol
    private GBDatabaseManager() {
    }




    // ***********************************************/
    /*         setters & getters                    */
    // ***********************************************/

    //mDatabaseHelper
    private void setDatabaseHelper(GBDatabaseSqliteHelper mDatabaseHelper) {
        this.mDatabaseHelper = mDatabaseHelper;
    }

    //Return null if the field has not yet been initialized
    private synchronized GBDatabaseSqliteHelper getDatabaseHelper() {
        return mDatabaseHelper;
    }


    //mDatabase
    private void setDatabase(SQLiteDatabase mDatabase) {
        this.mDatabase = mDatabase;
    }

    //return null if the field has not yet been initialized
    private synchronized SQLiteDatabase getDatabase() {
        return mDatabase;
    }


    // ***********************************************/
    /*         Instance methods                     */
    // ***********************************************/
    //The CRUD routines:


    // ***********************************************/
    /*         Project CRUD methods                 */
    // ***********************************************/


    /// ********************************** CREATE ********************************
    //add or update a single project. No cascade add at this level
    long addProject(GBProject project) {
        long returnCode = sDB_ERROR_CODE;
        GBProjectManager projectManager = GBProjectManager.getInstance();
        returnCode = mDatabaseHelper.add(mDatabase,                //database to add to
                            GBDatabaseSqliteHelper.TABLE_PROJECT,   //table to add to
                            projectManager.getCVFromProject(project),//Content Values of the project
                            getProjectWhereClause(project.getProjectID()),
                            GBDatabaseSqliteHelper.PROJECT_ID);
        if (returnCode == sDB_ERROR_CODE) return returnCode;

        //Cascade add happens at the manager level

        if (project.getProjectID() == GBUtilities.ID_DOES_NOT_EXIST) {
            //update the in-memory object with the ID just assigned
            project.setProjectID(returnCode);
        }
        //new DB was created. Return the DB ID to the caller
        return returnCode;
    }

    long addProjectSettingsToDB(GBProject project) {

        long returnCode = sDB_ERROR_CODE;

        GBProjectSettings projectSettings = project.getSettings();
        if (projectSettings == null) {
            //Settings had not yet been created. Do so now, with default values
            projectSettings = new GBProjectSettings();//defaults
            projectSettings.setProjectID(project.getProjectID());
            project.setSettings(projectSettings);
        }
        GBProjectManager projectManager = GBProjectManager.getInstance();
        //store the settings in the DB with the project
        returnCode = mDatabaseHelper.add(mDatabase,
                GBDatabaseSqliteHelper.TABLE_PROJECT_SETTINGS,
                projectManager.getCVFromProjectSettings(project),
                getProjectSettingsWhereClause(project.getProjectID()),
                GBDatabaseSqliteHelper.PROJECT_SETTINGS_ID);
        if (returnCode == sDB_ERROR_CODE) return returnCode;
        //return the new ID to the caller
        projectSettings.setProjectSettingsID(returnCode);
        return returnCode;
    }

    /// ********************************** READ ********************************
    /* *****************************
     Cursor query (  String table,           //Table Name
                     String[] columns,       //Columns to return, null for all columns
                     String where_clause,
                     String[] selectionArgs, //replaces ? in the where_clause with these arguments
                     String groupBy,         //null meanas no grouping
                     String having,          //row grouping
                     String orderBy)         //null means the default sort order
     *********************************/
    //get the cursor containing rows representing all the projets in the DB
    Cursor getAllProjectsCursor() {
        return mDatabaseHelper.getObject(mDatabase,
                GBDatabaseSqliteHelper.TABLE_PROJECT,
                null,    //get the whole project
                null,    //get all projects.
                null, null, null, null);

    }

    //returns the number of Projects read in from the DB
    //side effect is to load all projects into memory
    //  along with all the associated project settings
    int getAllProjects() {

        Cursor cursor = getAllProjectsCursor();

        //create the project objects from the Cursor object
        GBProjectManager projectManager = GBProjectManager.getInstance();

        int position = 0;
        int last = cursor.getCount();
        GBProject project;
        while (position < last) {
            project = projectManager.getProjectFromCursor(cursor, position);
            // TODO: 12/21/2016 Need to let someone know that something is wrong here if null
            if (project != null) {

                //Add the DB project to memory
                boolean addToDBToo = false;
                boolean cascadeFlag = false;
                projectManager.addProject(project, addToDBToo, cascadeFlag);

                //cascading objects are not pulled from the DB until they are explicitly needed
                //Whenever project.getPoints,getSettings,getPictures is called
                // and the array list is empty, the DB is queried
            }

            position++;
        }
        cursor.close();
        return last;

    }

    Cursor getProjectCursor(long projectID) {
        //get the project row from the DB
        return mDatabaseHelper.getObject(mDatabase,      //the db to access
                GBDatabaseSqliteHelper.TABLE_PROJECT,//table name
                null,           //get the whole project
                getProjectWhereClause(projectID),   //where clause
                null, null, null, null); //args,group,row grouping,order

    }

    //NOTE this routine does NOT add the project to the RAM list maintained by ProjectManager
    GBProject getProject(long projectID) {
        //get the project row from the DB
        Cursor cursor = getProjectCursor(projectID);

        //create a project object from the Cursor object
        GBProjectManager projectManager = GBProjectManager.getInstance();
        //get the first row in the cursor
        int position = 0;//so we don't have a magic number in the last parameter
        GBProject project = projectManager.getProjectFromCursor(cursor, position);
        cursor.close();

        //Cascading objects are not pulled from the DB until explicitly requested by calling
        //project.getPoints, getSettngs, getPictures
        return project;
    }


    //get project settings from the DB, analogous to getProject(projectID)
    GBProjectSettings getProjectSettings(long projectID) {

        Cursor cursor = mDatabaseHelper.getObject(mDatabase,      //the db to access
                GBDatabaseSqliteHelper.TABLE_PROJECT_SETTINGS,//table
                null,           //get the whole project
                getProjectSettingsWhereClause(projectID), //where clause
                null, null, null, null); //args,group,row grouping,order

        //create a project object from the Cursor object
        GBProjectManager projectManager = GBProjectManager.getInstance();
        int position = 0;

        GBProjectSettings projectSettings =
                projectManager.getProjectSettingsFromCursor(cursor, position);
        cursor.close();
        return projectSettings;

    }

    /// ********************************** UPDATE ********************************
    //Update is now part of ADD
    //Cascade of add/update happens at the Manager level


    //* ********************************* DELETE ********************************

    //The return code indicates how many rows affected
    int removeProject(long projectID) {

        removeProjectSettings(projectID);

        return mDatabaseHelper.remove(mDatabase,
                GBDatabaseSqliteHelper.TABLE_PROJECT,
                getProjectWhereClause(projectID),
                null);  //values that replace ? in where clause
    }

    //The return code indicates how many rows affected
    void removeProjectSettings(long projectID) {

        mDatabaseHelper.remove(mDatabase,
                GBDatabaseSqliteHelper.TABLE_PROJECT_SETTINGS,
                getProjectSettingsWhereClause(projectID),
                null);  //values that replace ? in where clause
    }


    // ***********************************************/
    /*        Project specific CRUD  utility         */
    // ***********************************************/
    private String getProjectWhereClause(long projectID) {
        return GBDatabaseSqliteHelper.PROJECT_ID + " = " + String.valueOf(projectID);
    }


    private String getProjectSettingsWhereClause(long projectID) {
        return GBDatabaseSqliteHelper.PROJECT_SETTINGS_ID + " = " + String.valueOf(projectID);
    }


    // ***********************************************/
    /*        Point CRUD methods               */
    // ***********************************************/


    /// *****************************    Create    ***********************
    //At the helper level, the helper examins the project ID.
    // If the ID has not been assigned, the object is added to the DB
    // Else the object is assumed to already exist in the DB so an UPDATE is attempted

    //No Cascade adds of contained objects, that happens at the Manager level, not here
    long addPoint(GBPoint point) {
        long returnCode = sDB_ERROR_CODE;

        //first add/update the point
        String whereClause = getPointWhereClause(point.getForProjectID(), point.getPointID());
        GBPointManager pointManager = GBPointManager.getInstance();
        returnCode = mDatabaseHelper.add(mDatabase,
                                         GBDatabaseSqliteHelper.TABLE_POINT,
                                         pointManager.getCVFromPoint(point),
                                         whereClause,
                                         GBDatabaseSqliteHelper.POINT_ID);
        if (returnCode == sDB_ERROR_CODE) return returnCode;

        //Cascade add happens at the manager level, not here

        //If the point was newly added to the DB, the ID was just assigned
        //If that is the case, update the pointID on the in-memory object
        if (point.getPointID() == GBUtilities.ID_DOES_NOT_EXIST) {
            point.setPointID(returnCode);
        }
        return returnCode;
    }


    //* *********************  Read **********************************
    //Reads the Points in from the DB,
    //     and adds them to the project instance passed as an argument


    ArrayList<GBPoint> getPointsForProjectFromDB(long projectID) {

        //This list will be added to the project after the points are read in
        ArrayList<GBPoint> pointsList = new ArrayList<>();

        if (projectID == GBUtilities.ID_DOES_NOT_EXIST) return pointsList;

        //read in all the points that belong to the project from the DB
        Cursor cursor = mDatabaseHelper.getObject(mDatabase,
                GBDatabaseSqliteHelper.TABLE_POINT,
                null,    //get the whole point
                //Only get the points for this project
                getPointWhereClause(projectID),
                null, null, null, null);

        //need the pointManager to convert a point in the Cursor to a point object
        GBPointManager pointManager = GBPointManager.getInstance();

        //iterate over all the rows in the cursor
        GBPoint point;
        int last = cursor.getCount();
        int position = 0;
        while (position < last) {
            point = pointManager.getPointFromCursor(cursor, position);
            //add the point to the project
            pointsList.add(point);

            //cascading objects are not pulled from the DB until explicitly requested

            position++;
        }

        cursor.close();

        return pointsList;
    }


    //* ******************************    Update   *************************


    //* *******************************     Delete    ***************************
    //The return code indicates how many rows affected
    int removePoint(long pointID, long projectID) {

        return mDatabaseHelper.remove(
                mDatabase,
                GBDatabaseSqliteHelper.TABLE_POINT,
                getPointWhereClause(pointID, projectID),
                null);  //values that replace ? in where clause
    }

    //The return code indicates how many rows affected
    int removeProjectPoints(long projectID) {

        return mDatabaseHelper.remove(
                mDatabase,
                GBDatabaseSqliteHelper.TABLE_POINT,
                getPointWhereClause(projectID),
                null);  //values that replace ? in where clause
    }


    // ***********************************************/
    /*        Point specific CRUD  utility         */
    // ***********************************************/
    //This only gets the one point related to this project
    private String getPointWhereClause(long pointID, long projectID) {
        return GBDatabaseSqliteHelper.POINT_ID + " = '" + String.valueOf(pointID) + "' AND " +
               GBDatabaseSqliteHelper.POINT_FOR_PROJECT_ID + " = '" +
                                                          String.valueOf(projectID) + "'";

    }

    //This gets all the points related to this project
    private String getPointWhereClause(long projectID) {
        return GBDatabaseSqliteHelper.POINT_FOR_PROJECT_ID + " = '" +
                String.valueOf(projectID) + "'";
    }


    // ***********************************************/
    /*         Coordinate CRUD methods              */
    // ***********************************************/

    //* ****************************    Create    ***********************
    long addCoordinate(GBCoordinate coordinate) {
        if (coordinate == null) return sDB_ERROR_CODE;

        long projectID = coordinate.getProjectID();
        if (projectID == GBUtilities.ID_DOES_NOT_EXIST) return sDB_ERROR_CODE;

        GBProjectManager projectManager = GBProjectManager.getInstance();
        GBProject project = projectManager.getProject(projectID);
        if (project == null) return sDB_ERROR_CODE;

        String table = getCoordinateTypeTable(project);

        GBCoordinateManager coordinateManager = GBCoordinateManager.getInstance();
        ContentValues cv = coordinateManager.getCVFromCoordinate(coordinate);

        long returnCode = sDB_ERROR_CODE;

        //first add/update the coordinate
        String whereClause =
                getCoordinateWhereClause(coordinate.getCoordinateID(), coordinate.getProjectID());
        returnCode = mDatabaseHelper.add(mDatabase,
                                         table,
                                         cv,
                                         whereClause,
                                         GBDatabaseSqliteHelper.COORDINATE_ID);
        if (returnCode == sDB_ERROR_CODE) return returnCode;

        //Cascade add happens at the manager level, not here

        //If the coordinate was newly added to the DB, the ID was just assigned
        //If that is the case, update the coordinateID on the in-memory object
        if (coordinate.getCoordinateID() == GBUtilities.ID_DOES_NOT_EXIST) {
            coordinate.setCoordinateID(returnCode);
        }
        return returnCode;

    }


    //* *********************  Read **********************************

    //NOTE this routine does NOT add the coordinate to the Project where
    GBCoordinate getCoordinateFromDB(long coordinateID, long projectID) {
        if (coordinateID == GBUtilities.ID_DOES_NOT_EXIST) return null;
        if (projectID == GBUtilities.ID_DOES_NOT_EXIST) return null;

        String table = getCoordinateTypeTable(projectID);

        //get the coordinate row from the DB
        Cursor cursor = mDatabaseHelper.getObject(
                mDatabase,     //the db to access
                table,  //table name
                null,          //get the whole coordinate
                getCoordinateWhereClause(coordinateID, projectID), //where clause
                null, null, null, null);//args, group, row grouping, order

        //create a coordinate object from the Cursor object
        GBCoordinateManager coordinateManager = GBCoordinateManager.getInstance();

        //get the first row in the cursor
        GBCoordinate coordinate = coordinateManager.getCoordinateFromCursor(cursor, 0);
        cursor.close();
        return coordinate;
    }

    //NOTE this routine does NOT add the coordinate to the Project where
    GBCoordinate getCoordinateFromDB(long coordinateID, String table) {
        if (coordinateID == GBUtilities.ID_DOES_NOT_EXIST) return null;

        //get the coordinate row from the DB
        Cursor cursor = mDatabaseHelper.getObject(
                mDatabase,     //the db to access
                table,  //table name
                null,          //get the whole coordinate
                getCoordinateIDWhereClause(coordinateID), //where clause
                null, null, null, null);//args, group, row grouping, order

        //create a coordinate object from the Cursor object
        GBCoordinateManager coordinateManager = GBCoordinateManager.getInstance();

        //get the first row in the cursor
        GBCoordinate coordinate = coordinateManager.getCoordinateFromCursor(cursor, 0);
        cursor.close();
        return coordinate;
    }

    //* ******************************    Update   *************************


    //* *******************************     Delete    ***************************
    //The return code indicates how many rows affected
    int removeCoordinate(long coordinateID, long projectID) {
        if (coordinateID == GBUtilities.ID_DOES_NOT_EXIST) return 0;
        if (projectID == GBUtilities.ID_DOES_NOT_EXIST) return 0;


        String table = getCoordinateTypeTable(projectID);

        return mDatabaseHelper.remove(mDatabase,
                table,
                getCoordinateWhereClause(coordinateID, projectID),
                null);  //values that replace ? in where clause
    }

    //The return code indicates how many rows affected
    int removeProjectCoordinates(long projectID) {
        if (projectID == GBUtilities.ID_DOES_NOT_EXIST) return 0;

        String table = getCoordinateTypeTable(projectID);

        return mDatabaseHelper.remove(mDatabase,
                table,
                getCoordinateWhereClause(projectID),
                null);  //values that replace ? in where clause
    }


    // ***********************************************/
    /*        Coordinate specific CRUD  utility         */
    // ***********************************************/
    //This only gets the one coordinate related to this project
    private String getCoordinateWhereClause(long coordinateID, long projectID) {
        return GBDatabaseSqliteHelper.COORDINATE_ID + " = '" +
                                                        String.valueOf(coordinateID) + "' AND " +
                GBDatabaseSqliteHelper.COORDINATE_PROJECT_ID + " = '" +
                                                        String.valueOf(projectID) + "'";

    }
    //This only gets the one coordinate related to this project
    private String getCoordinateIDWhereClause(long coordinateID) {
        return GBDatabaseSqliteHelper.COORDINATE_ID + " = '" + String.valueOf(coordinateID) +  "'";
    }

    //This gets all the coordinates related to this project
    private String getCoordinateWhereClause(long projectID) {
        return GBDatabaseSqliteHelper.COORDINATE_PROJECT_ID +
                " = '" + String.valueOf(projectID) + "'";
    }


    private String getCoordinateTypeTable(GBProject project) {
        //Get the table to read from from the Project
        CharSequence coordinateType = project.getProjectCoordinateType();
        String table = GBDatabaseSqliteHelper.TABLE_COORDINATE_LL; //assume a default
        if ((coordinateType == GBCoordinate.sCoordinateTypeUTM) ||
            (coordinateType == GBCoordinate.sCoordinateTypeSPCS)) {
            table = GBDatabaseSqliteHelper.TABLE_COORDINATE_EN;
        }
        return table;
    }

    private String getCoordinateTypeTable(long projectID) {
        GBProjectManager projectManager = GBProjectManager.getInstance();
        GBProject project = projectManager.getProject(projectID);
        return getCoordinateTypeTable(project);
    }


    // ***********************************************/
    /*         CoordinateMean CRUD methods              */
    // ***********************************************/


    //* ****************************    Create    ***********************
    long addCoordinateMean(GBCoordinateMean coordinate) {
        if (coordinate == null) return GBUtilities.ID_DOES_NOT_EXIST;

        long projectID = coordinate.getProjectID();
        if (projectID == GBUtilities.ID_DOES_NOT_EXIST) return GBUtilities.ID_DOES_NOT_EXIST;

        GBProjectManager projectManager = GBProjectManager.getInstance();
        GBProject project = projectManager.getProject(projectID);
        if (project == null) return GBUtilities.ID_DOES_NOT_EXIST;

        GBCoordinateManager coordinateManager = GBCoordinateManager.getInstance();
        ContentValues cv = coordinateManager.getCVFromCoordinateMean(coordinate);

        long returncode = mDatabaseHelper.add(mDatabase,
                GBDatabaseSqliteHelper.TABLE_COORDINATE_MEAN,
                cv,
                getCoordinateWhereClause(coordinate.getCoordinateID(), coordinate.getProjectID()),
                GBDatabaseSqliteHelper.COORDINATE_ID);

        if (returncode == sDB_ERROR_CODE) return sDB_ERROR_CODE;
        if (coordinate.getCoordinateID() == GBUtilities.ID_DOES_NOT_EXIST) {
            coordinate.setCoordinateID(returncode);
        }
        return returncode;

    }


    //* *********************  Read **********************************

    //just returns the single coordinateMean object that corresponds to the coordinateID
    GBCoordinateMean getCoordinateMeanFromDB(long coordinateID) {
        if (coordinateID == GBUtilities.ID_DOES_NOT_EXIST) return null;


        //get the coordinate row from the DB
        Cursor cursor = mDatabaseHelper.getObject(
                                        mDatabase,     //the db to access
                                        GBDatabaseSqliteHelper.TABLE_COORDINATE_MEAN,  //table name
                                        null,          //get the whole coordinate
                                        getCoordinateMeanIDWhereClause(coordinateID), //where clause
                                        null, null, null, null);//args, group, row grouping, order

        //create a coordinate object from the Cursor object
        GBCoordinateManager coordinateManager = GBCoordinateManager.getInstance();

        //get the first row in the cursor
        GBCoordinateMean coordinate = coordinateManager.getCoordinateMeanFromCursor(cursor, 0);
        cursor.close();
        return coordinate;
    }


    //Get all the CoordinateMean objects for this project that are in the DB
    ArrayList<GBCoordinateMean> getAllCoordinateMeanFromDB(long projectID) {

        if (projectID == GBUtilities.ID_DOES_NOT_EXIST) return null;

        //get the coordinate row from the DB
        Cursor cursor = mDatabaseHelper.getObject(
                mDatabase,     //the db to access
                GBDatabaseSqliteHelper.TABLE_COORDINATE_MEAN,  //table name
                null,          //get the whole coordinate
                getCoordinateWhereClause(projectID), //where clause
                null, null, null, null);//args, group, row grouping, order

        //create a coordinate object from the Cursor object
        GBCoordinateManager coordinateManager = GBCoordinateManager.getInstance();

        GBCoordinateMean coordinateMean;
        ArrayList<GBCoordinateMean> coordinateMeans = new ArrayList<>();

        int position = 0;
        int last = cursor.getCount();

        while (position < last) {
            coordinateMean = coordinateManager.getCoordinateMeanFromCursor(cursor, position);
            if (coordinateMean != null) {
                // TODO: 12/21/2016 Need to let someone know that something is wrong here if null
                //Add the coordinateMean object to the list
                coordinateMeans.add(coordinateMean);
            }

            position++;
        }
        cursor.close();
        return coordinateMeans;

    }


    //* ******************************    Update   *************************
    //update is done by add


    //* *******************************     Delete    ***************************
    //The return code indicates how many rows affected
    int removeCoordinateMean(long coordinateID, long projectID) {
        if (coordinateID == GBUtilities.ID_DOES_NOT_EXIST) return 0;
        if (projectID == GBUtilities.ID_DOES_NOT_EXIST) return 0;

        return mDatabaseHelper.remove   (mDatabase,
                                        GBDatabaseSqliteHelper.TABLE_COORDINATE_MEAN,
                                        getCoordinateMeanWhereClause(coordinateID, projectID),
                                        null);  //values that replace ? in where clause
    }

    //The return code indicates how many rows affected
    int removeProjectCoordinatesMean(long projectID) {
        if (projectID == GBUtilities.ID_DOES_NOT_EXIST) return 0;

        return mDatabaseHelper.remove(mDatabase,
                GBDatabaseSqliteHelper.TABLE_COORDINATE_MEAN,
                getCoordinateMeanWhereClause(projectID),
                null);  //values that replace ? in where clause
    }


    // ***********************************************/
    /*        Coordinate specific CRUD  utility         */
    // ***********************************************/
    //This only gets the one coordinate related to this project
    private String getCoordinateMeanWhereClause(long coordinateID, long projectID) {
        return GBDatabaseSqliteHelper.COORDINATE_MEAN_ID + " = '" +
                                                        String.valueOf(coordinateID) + "' AND " +
               GBDatabaseSqliteHelper.COORDINATE_MEAN_PROJECT_ID + " = '" +
                                                        String.valueOf(projectID) + "'";

    }

    private String getCoordinateMeanIDWhereClause(long coordinateID) {
        return GBDatabaseSqliteHelper.COORDINATE_MEAN_ID + " = '" +
                                                                String.valueOf(coordinateID) +  "'";

    }

    //This gets all the coordinates related to this project
    private String getCoordinateMeanWhereClause(long projectID) {
        return GBDatabaseSqliteHelper.COORDINATE_MEAN_PROJECT_ID + " = '" +
                                                                    String.valueOf(projectID) + "'";
    }






    // ***********************************************/
    /*         MeanToken CRUD methods                */
    // ***********************************************/
    long addToken(GBMeanToken token){
        if (token == null) return sDB_ERROR_CODE;

        long projectID = token.getProjectID();
        if (projectID == GBUtilities.ID_DOES_NOT_EXIST) return sDB_ERROR_CODE;

        GBProjectManager projectManager = GBProjectManager.getInstance();
        GBProject project = projectManager.getProject(projectID);
        if (project == null) return sDB_ERROR_CODE;


        GBMeanTokenManager tokenManager = GBMeanTokenManager.getInstance();
        ContentValues cv = tokenManager.getCVFromToken(token);

        long returnCode = sDB_ERROR_CODE;

        //first add/update the coordinate
        String whereClause = getTokenWhereClause(token.getMeanTokenID());
        returnCode = mDatabaseHelper.add(mDatabase,
                                         GBDatabaseSqliteHelper.TABLE_MEAN_TOKEN,
                                         cv,
                                         whereClause,
                                         GBDatabaseSqliteHelper.MEAN_TOKEN_ID);
        if (returnCode == sDB_ERROR_CODE) return returnCode;

        //Cascade add happens at the manager level, not here

        //If the coordinate was newly added to the DB, the ID was just assigned
        //If that is the case, update the coordinateID on the in-memory object
        if (token.getMeanTokenID() == GBUtilities.ID_DOES_NOT_EXIST) {
            token.setMeanTokenID(returnCode);
        }
        return returnCode;
    }


    //just returns the single coordinateMean object that corresponds to the coordinateID
    GBMeanToken getMeanTokenFromDB(long tokenID) {
        if (tokenID == GBUtilities.ID_DOES_NOT_EXIST) return null;

        //get the coordinate row from the DB
        Cursor cursor = mDatabaseHelper.getObject(mDatabase,     //the db to access
                                    GBDatabaseSqliteHelper.TABLE_MEAN_TOKEN,  //table name
                                    null,          //get the whole coordinate
                                    getTokenWhereClause(tokenID), //where clause
                                    null, null, null, null);//args, group, row grouping, order

        //create a coordinate object from the Cursor object
        GBMeanTokenManager meanTokenManager = GBMeanTokenManager.getInstance();

        //get the first row in the cursor
        GBMeanToken token = meanTokenManager.getMeanTokenFromCursor(cursor, 0);
        cursor.close();
        return token;
    }


    //This gets all the pictures related to this project
    String getTokenWhereClause(long tokenID) {
        return GBDatabaseSqliteHelper.MEAN_TOKEN_ID + " = '" + String.valueOf(tokenID) + "'";
    }


    // ***********************************************/
    /*      MeanTokenReadings CRUD methods           */
    // ***********************************************/

    Cursor getTokenReadings(long tokenID){
        if (tokenID == GBUtilities.ID_DOES_NOT_EXIST) return null;

        return mDatabaseHelper.getObject(
                                    mDatabase,     //the db to access
                                    GBDatabaseSqliteHelper.TABLE_MEAN_TOKEN_READINGS,  //table name
                                    null,          //get the whole object
                                    getMeanTokenIDWhereClause(tokenID), //where clause
                                    null, null, null, null);//args, group, row grouping, order


    }


    // ***********************************************/
    /*        MeanToken specific CRUD  utility         */
    // ***********************************************/
    //This only gets the one coordinate related to this project
    private String getMeanTokenIDWhereClause(long tokenID) {
        return GBDatabaseSqliteHelper.MEAN_TOKEN_READING_MEAN_ID + " = '" +
                                                                     String.valueOf(tokenID) +  "'";
    }



    // ***********************************************/
    /*         TokenReading CRUD methods             */
    // ***********************************************/
    long  addCoordinateToReading(ContentValues cv, long coordinateID){

        long returncode = mDatabaseHelper.add(mDatabase,
                GBDatabaseSqliteHelper.TABLE_MEAN_TOKEN_READINGS,
                cv,
                getReadingWhereClause(coordinateID),
                GBDatabaseSqliteHelper.COORDINATE_ID);

        if (returncode == sDB_ERROR_CODE) return sDB_ERROR_CODE;
        //Readings do not exist in GeoBot, only in the DB, so no need to worry about the ID
        return returncode;

    }

    private String getReadingWhereClause(long coordinateID) {
        return
              GBDatabaseSqliteHelper.MEAN_TOKEN_READING_COORDINATE_ID + " = '" + coordinateID + "'";

    }


    // ***********************************************/
    /*         Picture CRUD methods             */
    // ***********************************************/

    //* *******************    Create    ***********************


    boolean addProjectPicturesToDB(GBProject project) {
        ArrayList<GBPicture> pictures = project.getPictures();
        if (pictures == null) {
            pictures = new ArrayList<>();
            project.setPictures(pictures);
        }

        String whereClause = getPictureWhereClause(project.getProjectID());

        return addPicturesToDB(pictures, whereClause);
    }


    boolean addPointPicturesToDB(GBPoint point) {
        ArrayList<GBPicture> pictures = point.getPictures();
        if ((pictures == null) || (pictures.size() == 0)) return true;

        String whereClause = getPictureWhereClause(point.getForProjectID(), point.getPointID());

        return addPicturesToDB(pictures, whereClause);
    }


    private boolean addPicturesToDB(ArrayList<GBPicture> pictures, String whereClause) {
        GBProjectManager projectManager = GBProjectManager.getInstance();

        int position = 0;
        int last = pictures.size();
        GBPicture picture;
        boolean returnCode = true;
        while (position < last) {
            picture = pictures.get(position);
            returnCode = returnCode & addPicture(picture, whereClause);
            position++;
        }
        return returnCode;
    }

    boolean addPicture(GBPicture picture, String whereClause) {
        GBProjectManager projectManager = GBProjectManager.getInstance();

        long ret = mDatabaseHelper.addPicture(mDatabase,
                                              projectManager.getCVFromPicture(picture),
                                              whereClause);

        return (!(ret == sDB_ERROR_CODE));
    }


    //* *********************  Read **********************************


    //return the pictures for this project (that are currently in the DB)
    ArrayList<GBPicture> getProjectPicturesFromDB(long projectID) {
        if (projectID == GBUtilities.ID_DOES_NOT_EXIST) return null;
        String whereClause = getPictureWhereClause(projectID);
        return getPicturesFromDB(whereClause);

    }


    //return the pictures for this point (that are currently in the DB)
    ArrayList<GBPicture> getPointPicturesFromDB(long projectID, long pointID) {
        if ((projectID == GBUtilities.ID_DOES_NOT_EXIST) ||
                (pointID == GBUtilities.ID_DOES_NOT_EXIST)) return null;
        String whereClause = getPictureWhereClause(projectID, pointID);
        return getPicturesFromDB(whereClause);

    }

    //actually get the pictures from the DB, regardless of where they are to be stored
    ArrayList<GBPicture> getPicturesFromDB(String whereClause) {

        //get the pictures for this project from the DB
        Cursor cursor = mDatabaseHelper.getObject(
                mDatabase,     //the db to access
                GBDatabaseSqliteHelper.TABLE_PICTURE,  //table name
                null,          //get the whole picture
                whereClause, //where clause
                null, null, null, null);//args, group, row grouping, order

        //create the picture objects from the Cursor object
        GBProjectManager projectManager = GBProjectManager.getInstance();

        //and store the pictures in a list
        ArrayList<GBPicture> pictures = new ArrayList<>();

        int position = 0;
        int last = cursor.getCount();
        GBPicture picture;
        while (position < last) {
            picture = projectManager.getPictureFromCursor(cursor, position);
            pictures.add(picture);
            position++;
        }
        cursor.close();

        return pictures;

    }

    //* *********************  Update   *************************
    //use addPicture(), it automatically updates if the add fails


    //* ******************     Delete    ***************************

    //The return code indicates how many rows affected
    //Removes all pictures, both at the project level and the point level
    int removeProjectPictures(long projectID) {

        return mDatabaseHelper.remove(mDatabase,
                GBDatabaseSqliteHelper.TABLE_PICTURE,
                getPictureWhereClause(projectID),
                null);  //values that replace ? in where clause
    }


    //The return code indicates how many rows affected
    int removePicturesFromPoint(GBPoint point) {
        String whereClause = getPictureWhereClause(point.getForProjectID(), point.getPointID());

        return mDatabaseHelper.remove(mDatabase,
                GBDatabaseSqliteHelper.TABLE_PICTURE,
                whereClause,
                null);  //values that replace ? in where clause
    }


    //The return code indicates how many rows affected
    //Removes only one picture from the project level
    int removeProjectPicture(String pictureID, long projectID) {

        return mDatabaseHelper.remove(mDatabase,
                GBDatabaseSqliteHelper.TABLE_PICTURE,
                getPictureWhereClause(pictureID, projectID),
                null);  //values that replace ? in where clause
    }


    //The return code indicates how many rows affected
    //Removes only one picture from the project level
    int removePointPicture(String pictureID, long projectID, long pointID) {
        String whereClause = getPictureWhereClause(pictureID, projectID, pointID);

        return mDatabaseHelper.remove(mDatabase,
                GBDatabaseSqliteHelper.TABLE_PICTURE,
                whereClause,
                null);  //values that replace ? in where clause
    }


    // ***********************************************/
    /*        Picture specific CRUD  utility         */
    // ***********************************************/

    //This only gets the one picture related to this project
    private String getPictureWhereClause(String pictureID, long projectID) {
        return
                GBDatabaseSqliteHelper.PICTURE_ID + " = '" + pictureID + "' AND " +
                        GBDatabaseSqliteHelper.PICTURE_PROJECT_ID + " = '" + String.valueOf(projectID) + "'";

    }

    //This only gets the one picture from the point
    private String getPictureWhereClause(String pictureID, long projectID, long pointID) {
        return
                GBDatabaseSqliteHelper.PICTURE_ID + " = '" + pictureID + "' AND " +
                        GBDatabaseSqliteHelper.PICTURE_PROJECT_ID + " = '" + String.valueOf(projectID) + "' AND " +
                        GBDatabaseSqliteHelper.PICTURE_POINT_ID + " = '" + String.valueOf(pointID) + "' ";

    }


    //This gets all the pictures related to this project
    String getPictureWhereClause(long projectID) {
        return
                GBDatabaseSqliteHelper.PICTURE_PROJECT_ID + " = '" + String.valueOf(projectID) + "'";
    }


    //This gets all pictures on this point of this project
    private String getPictureWhereClause(long projectID, long pointID) {
        return
         GBDatabaseSqliteHelper.PICTURE_PROJECT_ID + " = '" + String.valueOf(projectID) + "' AND " +
         GBDatabaseSqliteHelper.PICTURE_POINT_ID   + " = '" + String.valueOf(pointID)   + "' ";


    }
}


