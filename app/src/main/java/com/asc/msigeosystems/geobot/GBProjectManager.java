package com.asc.msigeosystems.geobot;

import android.content.ContentValues;
import android.database.Cursor;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Elisabeth Huhn on 5/18/2016.
 *
 * This manager hides the fact that the Picture objects are mirrored in a DB
 * If a Project is not found in memory, the DB is queried for it.
 * If a Project is added to memory,     it will also be added to the DB
 * If a Project is updated in memory,   it is also updated in teh DB
 * If a Project is deleted from memory, it is also deleted from the DB
 */
class GBProjectManager {

    /* **********************************/
    /* ******* Static Constants *********/
    /* **********************************/
    private static final int PROJECT_NOT_FOUND = -1;


    /* **********************************/
    /* ******* Static Variables *********/
    /* **********************************/
    private static GBProjectManager ourInstance ;

    /* **********************************/
    /* ******* Member Variables *********/
    /* **********************************/
    private ArrayList<GBProject> mProjectList;


    /* **********************************/
    /* ******* Static Methods   *********/
    /* **********************************/
    public static GBProjectManager getInstance() {
        if (ourInstance == null){
            ourInstance = new GBProjectManager();

        }
        return ourInstance;
    }




    /* **********************************/
    /* ******* Constructors     *********/
    /* **********************************/
    private GBProjectManager() {

        mProjectList = new ArrayList<>();

        //The DB isn't read in until the first time a project is accessed

    }


    /* **********************************/
    /* ******* Setters/Getters  *********/
    /* **********************************/

    /* *****************************************/
    /* *******     CRUD Methods        *********/
    /* *****************************************/


    //* ****************  CREATE *******************************************

    //The routine that actually adds the instance to in memory list and
    // potentially (third boolean parameter) to the DB
    //This is the only project routine that does a cascade add of contained objects
    boolean addProject(GBProject newProject, boolean addToDBToo, boolean cascadeFlag){
        //Add project to memory list
        //determine whether the project already exists
        int position = findProjectPosition(newProject.getProjectID());
        if (position == PROJECT_NOT_FOUND) {
            mProjectList.add(newProject);
            //Not going to pull cascading objects from the DB until they are explicitly
            // requested with a call to project.getPoints
        } else {
            mProjectList.add(position, newProject);
        }

        if (addToDBToo){
            //add project to DB
            GBDatabaseManager databaseManager = GBDatabaseManager.getInstance();
            if (databaseManager.addProject(newProject) == GBDatabaseManager.sDB_ERROR_CODE) return false;


            if (cascadeFlag) {
                //Points
                // TODO: 6/14/2017 revisit the decision not to cascade points from the project level
                //cascadePoints(newProject);
                //Pictures
                ArrayList<GBPicture> pictures = newProject.getPictures();
                if (pictures != null) {
                    databaseManager.addProjectPicturesToDB(newProject);
                }

                if (!newProject.areSettingsInMemory()) {
                    databaseManager.addProjectSettingsToDB(newProject);
                }
            }
        }

        //deal with any pictures on the project

        return true;
    }



    //Updates the Project Object in the DB and the Picture Object in the DB
    //Assumes all subordinate objects are correct, and don't need updating
    boolean addPictureToDB(GBPicture picture){

        long projectID = picture.getProjectID();
        if (projectID == GBUtilities.ID_DOES_NOT_EXIST){
            return false;
        }

        //GBProjectManager projectManager = GBProjectManager.getInstance();
        //GBProject project = projectManager.getProject(projectID);
        //not necessary to update the project,
        // as the picture relationship isn't stored on the project in the DB
        //databaseManager.updateProjectOnly(project);

        GBDatabaseManager databaseManager = GBDatabaseManager.getInstance();
        String whereClause = databaseManager.getPictureWhereClause(projectID);
        return databaseManager.addPicture(picture, whereClause);

    }



    //* ****************  READ *******************************************

    ArrayList<GBProject> getProjectList() {
        if (mProjectList == null){
            mProjectList = new ArrayList<>();
        }
        if (mProjectList.size() == 0){
            //get the Projects from the DB
            GBDatabaseManager databaseManager = GBDatabaseManager.getInstance();
            databaseManager.getAllProjects();
        }
        return mProjectList;
    }


    //Get the project matching the passed project ID
    //from the projects stored in the database
    GBProject getProject(long projectID) {
        int atPosition = findProjectPosition(projectID);

        if (atPosition == PROJECT_NOT_FOUND) {

            //attempt to read the DB before giving up
            GBProject project = getProjectFromDB(projectID);

            if (project != null) {
                //if a matching project was in the DB, add it to RAM
                mProjectList.add(project);

                //cascading objects are not pulled from the DB until explicitly requested by
                //calling project.getPoints, project.getPictures or project.getSettings
            }
            return project;
        }
        return (mProjectList.get(atPosition));
    }

    //Returns null if it's not in the DB
    GBProject getProjectFromDB (long projectID){
        //Ignore the in memory list, just go straight to the DB
        GBDatabaseManager databaseManager = GBDatabaseManager.getInstance();
        return databaseManager.getProject(projectID);
    }


    //returns the position of the project instance that matches the argument project
    //returns constant = PROJECT_NOT_FOUND if the project is not in the list
    //NOTE it is a RunTimeException to call this routine if the list is null or empty
    private int findProjectPosition(long projectID){
        GBProject project;
        int position        = 0;
        int last            = mProjectList.size();

        //Determine whether an instance of the project is already in the list
        //NOTE that if list is empty, while doesn't loop even once
        while (position < last){
            project = mProjectList.get(position);

            if (project.getProjectID() == projectID){
                //Found the project in the list at this position
                return position;
            }
            position++;
        }
        return PROJECT_NOT_FOUND;
    }


    //* ****************  UPDATE *******************************************




    //replace the project in memory at position, and update the DB version of this project
    private void updateProject(GBProject project, int position){
        //replace the project at position in mProjectList
        mProjectList.add(position, project);

        // update the project and all subordinate objects in the DB
        GBDatabaseManager databaseManager = GBDatabaseManager.getInstance();
        databaseManager.addProject(project);

    }

    //Updates the Project Object in the DB and the Picture Object in the DB
    //No cascade of contained objects
    boolean updateSinglePictureInDB(GBPicture picture){

        long projectID = picture.getProjectID();
        if (projectID == GBUtilities.ID_DOES_NOT_EXIST){
            return false;
        }

        //GBProjectManager projectManager = GBProjectManager.getInstance();
        //GBProject project = projectManager.getProject(projectID);
        //not necessary to update the project,
        // as the picture relationship isn't stored on the project in the DB
        //databaseManager.updateProjectOnly(project);

        GBDatabaseManager databaseManager = GBDatabaseManager.getInstance();
        String whereClause = databaseManager.getPictureWhereClause(projectID);
        return databaseManager.addPicture(picture, whereClause);

    }

    //Updates the Project Object in the DB and the Picture Object in the DB
    //Assumes all subordinate objects are correct, and don't need updating
    void updateSingleProjectInDB(GBProject project){

        GBDatabaseManager databaseManager = GBDatabaseManager.getInstance();
        databaseManager.addProject(project);

    }


    //* ****************  DELETE *******************************************


    //This routine not only removes from the in-memory list, but also from the DB
    //This routine removes all traces of a project: from menory and the DB
    //It takes care of the project, its points, and the points coordinates
    //return code only indicates whether the project existed in the first place, not
    //if it was removed successfully. That is rather rashly assumed
    boolean removeProject(int position) {
        if (position > mProjectList.size()) {
            //Can't remove a position that the list isn't long enough for
            return false;
        }
        GBProject project = mProjectList.get(position);
        //get rid of all project objects in the DB
        removeProjectFromDB(project);

        //Garbage collection would probably do this, but.... do it anyway
        //Clear all points from memory
        ArrayList<GBPoint> points = project.getPoints();
        points.clear();

        //Garbage collection would probably do this, but.... do it anyway
        //Clear all pictures from memory
        ArrayList<GBPicture> pictures = project.getPictures();
        pictures.clear();

        //remove the project itself from management
        mProjectList.remove(position);
        return true;
    }//end public remove position


    //Returns null if it's not in the DB
    void removeProjectFromDB (GBProject project){
        long projectID = project.getProjectID();
        // TODO: 1/10/2017 remove any coordinateMean objects from the DB 

        //remove any pictures on points of the project
        removePicturesFromProjectPoints(project);

        //remove the coordinates and the points themselves
        GBPointManager pointManager = GBPointManager.getInstance();
        pointManager.removeProjectPointsFromDB(project);

        //remove any and all Pictures from the project (this will also remove point pictures)
        GBDatabaseManager databaseManager = GBDatabaseManager.getInstance();
        databaseManager.removeProjectPictures(projectID);

        //remove project settings
        databaseManager.removeProjectSettings(projectID);

        //finally, remove the project itself
        databaseManager.removeProject(projectID);
    }

    void removeProjectPicture(String pictureID, GBProject project){

        GBDatabaseManager databaseManager = GBDatabaseManager.getInstance();

        //remove the picture from the DB
        databaseManager.removeProjectPicture(pictureID, project.getProjectID());
    }

    void removePointPicture(String pictureID, long projectID, GBPoint point){
        GBDatabaseManager databaseManager = GBDatabaseManager.getInstance();

        //remove the picture from the DB
        databaseManager.removePointPicture(pictureID, projectID, point.getPointID());
    }

    private void removePicturesFromProjectPoints(GBProject project){

        ArrayList<GBPoint>   points   = project.getPoints();
         GBPoint              point;
        int last;
        int position;
        if (points != null){
            last = points.size();
            position = 0;
            while (position < last){
                point = points.get(position);
                removePicturesFromPoint(point);
                position++;
            }
        }

    }

    void removePicturesFromPoint(GBPoint point){
        GBDatabaseManager databaseManager = GBDatabaseManager.getInstance();
        ArrayList<GBPicture> pictures = point.getPictures();

        if (pictures != null) {
            //from the DB
            databaseManager.removePicturesFromPoint(point);
            //from the Point Object
            pictures.clear();
        }

    }


    /* ******************************************/
    /* ******* Translation Utility Methods  *****/
    /* ******************************************/

    GBProject deepCopyProject(GBActivity activity, GBProject fromProject, boolean assignNextID){

        //This method of deep copy only works if the most current copy of the
        //project exists in the DB.
        //This assumption is often valid, as in ListProjects. The only way a project
        //  can appear in the UI List is if is the most current copy of the object

        //do a deep copy of the project by reading in the
        //DB version of the project
        //This also brings in the ProjectSettings associated with the project
        GBProject toProject = getProjectFromDB(fromProject.getProjectID());

        long toProjectID;
        if (assignNextID) {
            //set the new project ID on the toProject and on its Settings
            toProjectID = GBProject.getNextProjectID(activity);
            toProject.setProjectID(toProjectID);
            toProject.getSettings().setProjectID(toProjectID);
        } else {
            toProjectID = fromProject.getProjectID();
        }

           //update the date created and date last maintained
        toProject.setProjectDateCreated(new Date().getTime());
        toProject.setProjectLastModified(new Date().getTime());

        //the coordinate type is OK as read in from the DB, it does not need to be touched

        //do a deep copy of the points on this project is to read them in from the DB
        //GBPointManager pointManager = GBPointManager.getInstance();

        //This not only reads in the points from the DB, it adds those points to
        //    the pointsList on the project
        //The return value is not what is important here, it's the side effect of reading
        //    all the points in from the DB and adding them to memory
        toProject.getPoints();

        //change the pointIDs and the projectID's on the points of the project
        GBPoint toPoint;
        ArrayList<GBPoint> toPointList   = toProject.getPoints();

        //initialize the first point ID
        //The ID is assigned and stored the first time the object is added to the DB
        toProject.setProjectID(GBUtilities.ID_DOES_NOT_EXIST);

        //iterate through the point list, changing the IDs
        int last = toPointList.size();
        for (int position = 0; position < last; position++) {

            //set the forProjectID on the point to the new projectID
            toPoint = toPointList.get(position);
            toPoint.setForProjectID(toProjectID);

            //and assign a new point id to this point
            //ID will be assigned when the point is first saved to the DB
            toPoint.setPointID(GBUtilities.ID_DOES_NOT_EXIST);

            //add the new point to the list on the project AND to the db
            //pointManager.addPointToProject(toProject, toPoint, true);

            //  give the coordinate on the point a new ID
            int toCoordinateID = GBCoordinate.getNextCoordinateID();
            toPoint.getCoordinate().setCoordinateID(toCoordinateID);
            toPoint.setHasACoordinateID(toCoordinateID);

        }
        
        //Picture ID's are the timestamp of when they were taken, so will not change
        //The project they are in makes the ID unique, so update the picture's project ID
        //iterate through the picture list, changing the project IDs
        ArrayList<GBPicture> toPictureList   = toProject.getPictures();
        GBPicture toPicture;
        last = toPictureList.size();
        for (int position = 0; position < last; position++) {

            //set the forProjectID on the picture to the new projectID
            toPicture = toPictureList.get(position);
            toPicture.setProjectID(toProjectID);
            
            //leave the PictureID alone, it is the timestamp of when the picture was taken
            
            //add the new picture to the list on the project AND to the db
            // TODO: 12/29/2016 make sure the project has already added pictures in the getP'rojectFromDB() 
            //pictureManager.addPointToProject(toProject, toPicture, true);
            
        }

        return toProject;
    }


    //returns the ContentValues object needed to add/update the PROJECT to/in the DB
    ContentValues getCVFromProject(GBProject project){
        //convert the GBProject object into a ContentValues object containing a project
        ContentValues cvProject = new ContentValues();
        //put(columnName, value);
        cvProject.put(GBDatabaseSqliteHelper.PROJECT_ID,project.getProjectID());
        cvProject.put(GBDatabaseSqliteHelper.PROJECT_NAME,project.getProjectName().toString());
        //CONVERT the dates to strings in the DB.
        // They will be converted back to milliseconds in memory
        cvProject.put(GBDatabaseSqliteHelper.PROJECT_CREATED,
                                                String.valueOf(project.getProjectDateCreated()));
        cvProject.put(GBDatabaseSqliteHelper.PROJECT_LAST_MAINTAINED,
                                                String.valueOf(project.getProjectLastModified()));
        cvProject.put(GBDatabaseSqliteHelper.PROJECT_DESCRIPTION,
                                                project.getProjectDescription().toString());
        cvProject.put(GBDatabaseSqliteHelper.PROJECT_COORDINATE_TYPE,
                                                project.getProjectCoordinateType().toString());

        cvProject.put(GBDatabaseSqliteHelper.PROJECT_DIST_UNITS, project.getDistanceUnits());
        cvProject.put(GBDatabaseSqliteHelper.PROJECT_NUM_MEAN,   project.getNumMean());
        cvProject.put(GBDatabaseSqliteHelper.PROJECT_EN_V_NE,    project.getEnVNe());
        cvProject.put(GBDatabaseSqliteHelper.PROJECT_NXT_POINT_NUM, project.getNextPointNumber());
        cvProject.put(GBDatabaseSqliteHelper.PROJECT_LOCATION_PRECISION, project.getLocPrecision());
        cvProject.put(GBDatabaseSqliteHelper.PROJECT_STDDEV_PRECISION, project.getStdDevPrecision());
        cvProject.put(GBDatabaseSqliteHelper.PROJECT_RMS_V_STDDEV, project.getRMSvStD());

        return cvProject;
    }


    //returns the ContentValues object needed to add/update the PROJECT SETTINGS to/in the DB
    ContentValues getCVFromProjectSettings(GBProject project){
        GBProjectSettings projectSettings = project.getSettings();
        if (projectSettings == null){
            //use default settings if there are none
            projectSettings = new GBProjectSettings();
            //the two objects must point at each other
            projectSettings.setProjectID(project.getProjectID());
            project.setSettings(projectSettings);
        }
        //convert the GBProject object into a ContentValues object containing a project
        ContentValues cvProjectSettings = new ContentValues();
        //put(columnName, value);
        cvProjectSettings.put(GBDatabaseSqliteHelper.PROJECT_SETTINGS_ID,
                                                    projectSettings.getProjectID());
        /*
        cvProjectSettings.put(GBDatabaseSqliteHelper.PROJECT_SETTINGS_DISTANCE_UNITS,
                                                    projectSettings.getDistanceUnits().toString());
        cvProjectSettings.put(GBDatabaseSqliteHelper.PROJECT_SETTINGS_DECIMAL_DISPLAY,
                                                    projectSettings.getDecimalDisplay().toString());
        cvProjectSettings.put(GBDatabaseSqliteHelper.PROJECT_SETTINGS_ANGLE_UNITS,
                                                    projectSettings.getAngleUnits().toString());
        cvProjectSettings.put(GBDatabaseSqliteHelper.PROJECT_SETTINGS_ANGLE_DISPLAY,
                                                    projectSettings.getAngleDisplay().toString());
        cvProjectSettings.put(GBDatabaseSqliteHelper.PROJECT_SETTINGS_GRID_DIRECTION,
                                                    projectSettings.getGridDirection().toString());
        cvProjectSettings.put(GBDatabaseSqliteHelper.PROJECT_SETTINGS_SCALE_FACTOR,
                                                    projectSettings.getScaleFactor());
        int temp = 0;//set default to no
        if (projectSettings.isSeaLevel())temp = 1;
        cvProjectSettings.put(GBDatabaseSqliteHelper.PROJECT_SETTINGS_SEA_LEVEL, temp);

        temp = 0;//set default to no
        if (projectSettings.isRefraction())temp = 1;
        cvProjectSettings.put(GBDatabaseSqliteHelper.PROJECT_SETTINGS_REFRACTION,temp);

        cvProjectSettings.put(GBDatabaseSqliteHelper.PROJECT_SETTINGS_DATUM,
                                                    projectSettings.getDatum().toString());
        cvProjectSettings.put(GBDatabaseSqliteHelper.PROJECT_SETTINGS_PROJECTION,
                                                    projectSettings.getProjection().toString());
        cvProjectSettings.put(GBDatabaseSqliteHelper.PROJECT_SETTINGS_ZONE,
                                                    projectSettings.getZone().toString());
        cvProjectSettings.put(GBDatabaseSqliteHelper.PROJECT_SETTINGS_COORDINATE_DISPLAY,
                                                    projectSettings.getCoordinateDisplay().toString());
        cvProjectSettings.put(GBDatabaseSqliteHelper.PROJECT_SETTINGS_GEOID_MODEL,
                                                    projectSettings.getGeoidModel().toString());
        cvProjectSettings.put(GBDatabaseSqliteHelper.PROJECT_SETTINGS_STARTING_POINT_ID,
                                                    projectSettings.getStartingPointID().toString());
        temp = 0;//set default to no
        if (projectSettings.isAlphanumericID())temp = 1;
        cvProjectSettings.put(GBDatabaseSqliteHelper.PROJECT_SETTINGS_ALPHANUMERIC, temp);

        cvProjectSettings.put(GBDatabaseSqliteHelper.PROJECT_SETTINGS_FEATURE_CODES,
                                                    projectSettings.getFeatureCodes().toString());
        cvProjectSettings.put(GBDatabaseSqliteHelper.PROJECT_SETTINGS_FC_CONTROL_FILE,
                                                    projectSettings.getFCControlFile().toString());
        temp = 0;//set default to no
        if (projectSettings.isFCTimeStamp())temp = 1;
        cvProjectSettings.put(GBDatabaseSqliteHelper.PROJECT_SETTINGS_FC_TIMESTAMP,temp);
*/
        cvProjectSettings.put(GBDatabaseSqliteHelper.PROJECT_SETTINGS_MEANING_METHOD,
                projectSettings.getProjectID());

        cvProjectSettings.put(GBDatabaseSqliteHelper.PROJECT_SETTINGS_MEANING_NUMBER,
                projectSettings.getProjectID());

        cvProjectSettings.put(GBDatabaseSqliteHelper.PROJECT_SETTINGS_MEANING_METHOD,
                projectSettings.getProjectID());

        cvProjectSettings.put(GBDatabaseSqliteHelper.PROJECT_SETTINGS_MEANING_METHOD,
                projectSettings.getProjectID());

        cvProjectSettings.put(GBDatabaseSqliteHelper.PROJECT_SETTINGS_RMS_V_STDDEV,
                projectSettings.getRMSvsStdDev());


        return cvProjectSettings;
    }



    //returns the ContentValues object needed to add/update the Picture to/in the DB
    ContentValues getCVFromPicture(GBPicture picture){
        //convert the GBPicture object into a ContentValues object containing a picture
        ContentValues cvPicture = new ContentValues();
        //put(columnName, value);
        cvPicture.put(GBDatabaseSqliteHelper.PICTURE_ID,picture.getPictureID());
        cvPicture.put(GBDatabaseSqliteHelper.PICTURE_PROJECT_ID,picture.getProjectID());
        //CONVERT the dates to strings in the DB.
        // They will be converted back to milliseconds in memory
        cvPicture.put(GBDatabaseSqliteHelper.PICTURE_POINT_ID,picture.getPointID());

        String temp = picture.getPathName();
        String pathName = temp.replace(' ','<');//can not have blanks in the string or worn't store in DB
        temp = picture.getFileName();
        String fileName = temp.replace(' ','<');
        cvPicture.put(GBDatabaseSqliteHelper.PICTURE_PATH_NAME,pathName);
        cvPicture.put(GBDatabaseSqliteHelper.PICTURE_FILE_NAME,fileName);

        return cvPicture;
    }




    //returns the GBProject characterized by the position within the Cursor
    //returns null if the position is larger than the size of the Cursor
    //NOTE    this routine does NOT add the project to the list maintained by this ProjectManager
    //        The caller of this routine is responsible for that.
    //        This is only a translation utility
    //WARNING As the app is not multi-threaded, this routine is not synchronized.
    //        If the app becomes multi-threaded, this routine must be made thread safe
    //WARNING The cursor is NOT closed by this routine. It assumes the caller will close the
    //         cursor when it is done with it
    GBProject getProjectFromCursor(Cursor cursor, int position){

        int last = cursor.getCount();
        if (position >= last) return null;

        GBProject project = new GBProject(); //filled with defaults, no ID is assigned

        cursor.moveToPosition(position);
        project.setProjectID(
                cursor.getInt  (cursor.getColumnIndex(GBDatabaseSqliteHelper.PROJECT_ID)));
        project.setProjectName(
                cursor.getString(cursor.getColumnIndex(GBDatabaseSqliteHelper.PROJECT_NAME)));
        project.setProjectDateCreated(Long.parseLong(cursor.getString(
                cursor.getColumnIndex(GBDatabaseSqliteHelper.PROJECT_CREATED))));
        project.setProjectLastModified(Long.parseLong(cursor.getString(
                cursor.getColumnIndex(GBDatabaseSqliteHelper.PROJECT_LAST_MAINTAINED))));
        project.setProjectDescription(cursor.getString(
                cursor.getColumnIndex(GBDatabaseSqliteHelper.PROJECT_DESCRIPTION)));
        project.setProjectCoordinateType(cursor.getString(
                cursor.getColumnIndex(GBDatabaseSqliteHelper.PROJECT_COORDINATE_TYPE)));

        project.setDistanceUnits(cursor.getInt(
                cursor.getColumnIndex(GBDatabaseSqliteHelper.PROJECT_DIST_UNITS)));
        project.setNumMean(cursor.getInt(
                cursor.getColumnIndex(GBDatabaseSqliteHelper.PROJECT_NUM_MEAN)));
        project.setNextPointNumber(cursor.getInt(
                cursor.getColumnIndex(GBDatabaseSqliteHelper.PROJECT_NXT_POINT_NUM)));
        project.setNumMean(cursor.getInt(
                cursor.getColumnIndex(GBDatabaseSqliteHelper.PROJECT_NUM_MEAN)));
        project.setLocPrecision(cursor.getInt(
                cursor.getColumnIndex(GBDatabaseSqliteHelper.PROJECT_LOCATION_PRECISION)));
        project.setStdDevPrecision(cursor.getInt(
                cursor.getColumnIndex(GBDatabaseSqliteHelper.PROJECT_STDDEV_PRECISION)));
        project.setRMSvStD(cursor.getInt(
                cursor.getColumnIndex(GBDatabaseSqliteHelper.PROJECT_RMS_V_STDDEV)));

        return project;
    }

    //totally analogous to same function for project.
    // Whenever project is fetched from DB, so is Project Settings
    GBProjectSettings getProjectSettingsFromCursor(Cursor cursor, int position){

        int last = cursor.getCount();
        if (position >= last) return null;

        //filled with defaults, no ID is assigned
        GBProjectSettings projectSettings = new GBProjectSettings();

        cursor.moveToPosition(position);
        projectSettings.setProjectSettingsID(cursor.getInt(
                cursor.getColumnIndex(GBDatabaseSqliteHelper.PROJECT_SETTINGS_ID)));
/*
        projectSettings.setDistanceUnits (cursor.getString (
                cursor.getColumnIndex(GBDatabaseSqliteHelper.PROJECT_SETTINGS_DISTANCE_UNITS)));
        projectSettings.setDecimalDisplay (cursor.getString (
                cursor.getColumnIndex(GBDatabaseSqliteHelper.PROJECT_SETTINGS_DECIMAL_DISPLAY)));
        projectSettings.setAngleUnits (cursor.getString (
                cursor.getColumnIndex(GBDatabaseSqliteHelper.PROJECT_SETTINGS_ANGLE_UNITS)));
        projectSettings.setAngleDisplay (cursor.getString (
                cursor.getColumnIndex(GBDatabaseSqliteHelper.PROJECT_SETTINGS_ANGLE_DISPLAY)));
        projectSettings.setGridDirection (cursor.getString (
                cursor.getColumnIndex(GBDatabaseSqliteHelper.PROJECT_SETTINGS_GRID_DIRECTION)));

        projectSettings.setScaleFactor (cursor.getDouble(
                cursor.getColumnIndex(GBDatabaseSqliteHelper.PROJECT_SETTINGS_SCALE_FACTOR)));


        int temp = cursor.getInt(
                cursor.getColumnIndex(GBDatabaseSqliteHelper.PROJECT_SETTINGS_SEA_LEVEL));
        boolean tempBoolean = false;
        if (temp == 1)tempBoolean = true;
        projectSettings.setSeaLevel(tempBoolean);

        temp = cursor.getInt(
                cursor.getColumnIndex(GBDatabaseSqliteHelper.PROJECT_SETTINGS_REFRACTION));
        tempBoolean = false;
        if (temp == 1)tempBoolean = true;
        projectSettings.setRefraction(tempBoolean);

        projectSettings.setDatum (cursor.getString (
                cursor.getColumnIndex(GBDatabaseSqliteHelper.PROJECT_SETTINGS_DATUM)));
        projectSettings.setProjection (cursor.getString (
                cursor.getColumnIndex(GBDatabaseSqliteHelper.PROJECT_SETTINGS_PROJECTION)));
        projectSettings.setZone (cursor.getString (
                cursor.getColumnIndex(GBDatabaseSqliteHelper.PROJECT_SETTINGS_ZONE)));
        projectSettings.setCoordinateDisplay (cursor.getString (
                cursor.getColumnIndex(GBDatabaseSqliteHelper.PROJECT_SETTINGS_COORDINATE_DISPLAY)));
        projectSettings.setGeoidModel (cursor.getString (
                cursor.getColumnIndex(GBDatabaseSqliteHelper.PROJECT_SETTINGS_GEOID_MODEL)));
        projectSettings.setStartingPointID (cursor.getString (
                cursor.getColumnIndex(GBDatabaseSqliteHelper.PROJECT_SETTINGS_STARTING_POINT_ID)));


        temp = cursor.getInt(
                cursor.getColumnIndex(GBDatabaseSqliteHelper.PROJECT_SETTINGS_ALPHANUMERIC));
        tempBoolean = false;
        if (temp == 1)tempBoolean = true;
        projectSettings.setAlphanumericID(tempBoolean);

        projectSettings.setFeatureCodes (cursor.getString (
                cursor.getColumnIndex(GBDatabaseSqliteHelper.PROJECT_SETTINGS_FEATURE_CODES)));
        projectSettings.setFCControlFile (cursor.getString (
                cursor.getColumnIndex(GBDatabaseSqliteHelper.PROJECT_SETTINGS_FC_CONTROL_FILE)));


        temp = cursor.getInt(
                cursor.getColumnIndex(GBDatabaseSqliteHelper.PROJECT_SETTINGS_FC_TIMESTAMP));
        tempBoolean = false;
        if (temp == 1)tempBoolean = true;
        projectSettings.setFCTimeStamp(tempBoolean);
*/
        projectSettings.setMeaningMethod(cursor.getInt(
                cursor.getColumnIndex(GBDatabaseSqliteHelper.PROJECT_SETTINGS_MEANING_METHOD)));


        projectSettings.setMeaningNumber(cursor.getInt(
                cursor.getColumnIndex(GBDatabaseSqliteHelper.PROJECT_SETTINGS_MEANING_NUMBER)));


        projectSettings.setLocationPrecision(cursor.getInt(
                cursor.getColumnIndex(GBDatabaseSqliteHelper.PROJECT_SETTINGS_LOCATION_PRECISION)));


        projectSettings.setStdDevPrecision(cursor.getInt(
                cursor.getColumnIndex(GBDatabaseSqliteHelper.PROJECT_SETTINGS_STDDEV_PRECISION)));

        projectSettings.setRMSStdDev(cursor.getInt(
                cursor.getColumnIndex(GBDatabaseSqliteHelper.PROJECT_SETTINGS_RMS_V_STDDEV)));



        return projectSettings;
    }


    //totally analogous to same function for project.
    // Whenever project is fetched from DB, so are any pictures associated with the project
    GBPicture getPictureFromCursor(Cursor cursor, int position){

        int last = cursor.getCount();
        if (position >= last) return null;

        //filled with defaults, no ID is assigned
        GBPicture picture = new GBPicture();

        cursor.moveToPosition(position);
        picture.setPictureID(
                cursor.getString(cursor.getColumnIndex(GBDatabaseSqliteHelper.PICTURE_ID)));
        picture.setProjectID(
                cursor.getLong  (cursor.getColumnIndex(GBDatabaseSqliteHelper.PICTURE_PROJECT_ID)));
        picture.setPointID(
                cursor.getLong   (cursor.getColumnIndex(GBDatabaseSqliteHelper.PICTURE_POINT_ID)));

        String temp = cursor.getString(cursor.getColumnIndex(GBDatabaseSqliteHelper.PICTURE_PATH_NAME));
        String pathName = temp.replace('<', ' ');
        picture.setPathName(pathName);

        temp = cursor.getString(cursor.getColumnIndex(GBDatabaseSqliteHelper.PICTURE_FILE_NAME));
        String fileName = temp.replace('<',' ');
        picture.setFileName(fileName);

        return picture;
    }

    /* ******************************************/
    /* ******* General Utility Methods      *****/
    /* ******************************************/



    /* ******************************************/
    /* ******* Picture Methods              *****/
    /* ******************************************/


}
