package com.asc.msigeosystems.geobot;


import android.content.ContentValues;
import android.database.Cursor;

import java.util.ArrayList;

/**
 * Created by Elisabeth Huhn on 5/18/2016.
 *
 *
 * The class in charge of maintaining the set of instances of Point
 *  both in-memory and in the DB
 *
 */
public class GBPointManager {
    //***********************************/
    //******** Static Constants  ********/
    //***********************************/

   private static final int POINT_NOT_FOUND = -1;


    //***********************************/
    //******** Static Variables  ********/
    //***********************************/
   private static GBPointManager ourInstance ;


    //*************************************/
    //******** Member Variables   *********/
    //*************************************/
    //The point lists exist on the Projects, rather than on a list here
    //private ArrayList<GBPoint> mPointList;


    //***********************************/
    //******** Static Methods   *********/
    //***********************************/
    public static GBPointManager getInstance() {
        if (ourInstance == null){
            ourInstance = new GBPointManager();
        }
        return ourInstance;
    }





    //***********************************/
    //******** Constructors     *********/
    //***********************************/
    private GBPointManager() {

        //The point list already exists on the Project Instance
        //mPointList = new ArrayList<>();

        //Points are contained in lists on the Project instances
        //ie Points are project specific.
        // So this is not the place to gin up dummy data
    }


    //******************************************/
    //******** Public Member Methods   *********/
    //******************************************/



    //******************************************/
    //********     CRUD Methods        *********/
    //******************************************/

    //***********************  SIZE **************************************

    //returns the number of points of the indicated project
    int getSize(long projectID){

        ArrayList<GBPoint> pointList = getProjectPointsList(projectID);

        return pointList.size();
    }

    //***********************  CREATE **************************************
    //This routine not only adds to the in memory list,
    // but has an argument, that if true,  also adds to the DB
    //returns FALSE if for any reason the point can not be added
    //ALSO deals with the coordinate on the point
    //Use this if you already have the Project object in hand
    boolean addPointsToProject(GBProject project, GBPoint newPoint, boolean addToDB){
        boolean returnCode = false;
        //  Can not add a point to a project that does not exist
        if ((project == null) || (newPoint == null)) return returnCode;

        //Find the project the point is for
        long medProjectID = newPoint.getForProjectID();
        long projectID    = project.getProjectID();
        //The point and the project must point at each other
        if (medProjectID != projectID) return returnCode;

        //determine if the point already is associated with this project
        ArrayList<GBPoint> pointList = project.getPoints();

        //Assert that the project has a points list, which is done when project is created
        if (pointList == null){
            pointList = new ArrayList<>();
            project.setPoints(pointList);
        }

        //Get the DB Manager to help with DB operations
        GBDatabaseManager databaseManager = GBDatabaseManager.getInstance();


        int atPosition = findPointPosition(pointList, newPoint.getPointID());
        if (atPosition == POINT_NOT_FOUND){
            //The point does not already exist. Add it
            pointList.add(newPoint);
        } else {
            //overwrite the existing point
            pointList.add(atPosition, newPoint);
        }
        if (addToDB) {
            //  Add the point and it's coordinate to the DB
            if  (databaseManager.addPoint(newPoint) == GBDatabaseManager.sDB_ERROR_CODE) return returnCode;

            if (!databaseManager.addPointPicturesToDB(newPoint)) return returnCode;
            if ((databaseManager.addCoordinate(newPoint.getCoordinate()) ==
                                                                GBDatabaseManager.sDB_ERROR_CODE)){
                return returnCode;
            }
        }
        returnCode = true;
        return returnCode;
    }



    //***********************  READ **************************************

    //returns a list of points for just this project
    ArrayList<GBPoint> getProjectPointsList(long projectID){
        GBProjectManager projectManager = GBProjectManager.getInstance();
        GBProject        project        = projectManager.getProject(projectID);

        ArrayList<GBPoint> projectPointsList;
        if (project != null){
            projectPointsList = project.getPoints();
        } else {
            throw new RuntimeException("Project Should Exist by now");

        }
        return projectPointsList;
    }


    GBPoint getPoint(long projectID, long pointID) {
        ArrayList<GBPoint> pointsList = getProjectPointsList(projectID);

        for (GBPoint point : pointsList){
            if (point.getForProjectID() == projectID){
                if (point.getPointID() == pointID) {
                    return point;
                }
            }
        }
        return null;
    }





    //***********************  UPDATE **************************************




    //***********************  DELETE **************************************

    //Because the list is on one project instance, we must also have the project ID
    //of the instance being manipulated
    //This routine not only removes from the in-memory list, but also from the DB
    boolean removePoint(long projectID, GBPoint point) {
        //Now find that project using the ProjectManager
        GBProjectManager projectManager = GBProjectManager.getInstance();
        GBProject        project        = projectManager.getProject(projectID);

        //If project not found, return false.
        //  Can not add a point to a project who does not exist
        if (project == null) return false;

        //determine if the point already is associated with this project
        //get the list of points contained in this project
        ArrayList<GBPoint> pointList = project.getPoints();

        //if not, create one
        if (pointList == null){
            pointList = new ArrayList<>();
            project.setPoints(pointList);
        }

        //Make sure it's in the memory list before removing it from the DB
        boolean returnCode = pointList.remove(point);

        if (returnCode){
            GBDatabaseManager databaseManager = GBDatabaseManager.getInstance();
            //first get rid of any coordinate on the point
            long coordinateID = point.getHasACoordinateID();
            databaseManager.removeCoordinate(coordinateID, projectID);
            // then get rid of the point itself
            // TODO: 6/15/2017 Aren't including DB in returnCode, should it?
            databaseManager.removePoint(point.getPointID(), projectID);
        }

        return returnCode;
    }//end public remove position



    void removeProjectPointsFromDB(GBProject project) {
        GBDatabaseManager databaseManager = GBDatabaseManager.getInstance();
        databaseManager.removeProjectCoordinates(project.getProjectID());
        databaseManager.removeProjectPoints(project.getProjectID());
    }//end public remove position





    //*******************************************/
    //********  Member Methods   *********/
    //*******************************************/

    //Find the position of the point instance
    //     that matches the argument pointID
    //     within the argument list pointList
    //returns constant = POINT_NOT_FOUND if the point is not in the list
    //NOTE it is a RunTimeException to call this routine if the list is null or empty
    private int findPointPosition(ArrayList<GBPoint> pointList, long pointID){
        GBPoint point;
        int          position = 0;
        int          last     = pointList.size();

        //Determine whether an instance of the point is already in the list
        //NOTE that if list is empty, while doesn't loop even once
        while (position < last){
            point = pointList.get(position);

            if (point.getPointID() == pointID){
                //Found the point in the list at this position
                return position;
            }
            position++;
        }
        return POINT_NOT_FOUND;
    }



    //***********************  COPY **************************************


    //*******************************************/
    //***  Translation utility Methods   ********/
    //*******************************************/

    ContentValues getCVFromPoint(GBPoint point){
        ContentValues values = new ContentValues();
        values.put(GBDatabaseSqliteHelper.POINT_ID,               point.getPointID());
        values.put(GBDatabaseSqliteHelper.POINT_FOR_PROJECT_ID,   point.getForProjectID());
        values.put(GBDatabaseSqliteHelper.POINT_ISA_COORDINATE_ID,point.getHasACoordinateID());
        values.put(GBDatabaseSqliteHelper.POINT_NUMBER,           point.getPointNumber());
        values.put(GBDatabaseSqliteHelper.POINT_OFFSET_DISTANCE,  point.getOffsetDistance());
        values.put(GBDatabaseSqliteHelper.POINT_OFFSET_HEADING,   point.getOffsetHeading());
        values.put(GBDatabaseSqliteHelper.POINT_OFFSET_ELEVATION, point.getOffsetElevation());
        values.put(GBDatabaseSqliteHelper.POINT_HEIGHT,           point.getHeight());
        values.put(GBDatabaseSqliteHelper.POINT_FEATURE_CODE,     point.getPointFeatureCode().toString());
        values.put(GBDatabaseSqliteHelper.POINT_NOTES,            point.getPointNotes().toString());
        values.put(GBDatabaseSqliteHelper.POINT_HDOP ,            point.getHdop());
        values.put(GBDatabaseSqliteHelper.POINT_VDOP ,            point.getVdop());
        values.put(GBDatabaseSqliteHelper.POINT_TDOP ,            point.getTdop());
        values.put(GBDatabaseSqliteHelper.POINT_PDOP ,            point.getPdop());
        values.put(GBDatabaseSqliteHelper.POINT_HRMS ,            point.getHrms());
        values.put(GBDatabaseSqliteHelper.POINT_VRMS ,            point.getVrms());
        //Doesn't insert coordinate object here, just the ID above

        return values;
    }

    //returns the GBPoint characterized by the position within the Cursor
    //returns null if the position is larger than the size of the Cursor
    //NOTE    this routine does NOT add the point to the list maintained by this PointManager
    //        The caller of this routine is responsible for that.
    //        This is only a translation utility
    //WARNING As the app is not multi-threaded, this routine is not synchronized.
    //        If the app becomes multi-threaded, this routine must be made thread safe
    //WARNING The cursor is NOT closed by this routine. It assumes the caller will close the
    //         cursor when it is done with it
    GBPoint getPointFromCursor(Cursor cursor, int position){

        int last = cursor.getCount();
        if (position >= last) return null;

        GBPoint point = new GBPoint(); //filled with defaults

        cursor.moveToPosition(position);
        point.setPointID         (cursor.getLong   (
                                  cursor.getColumnIndex(GBDatabaseSqliteHelper.POINT_ID)));
        point.setForProjectID    (cursor.getLong   (
                                  cursor.getColumnIndex(GBDatabaseSqliteHelper.POINT_FOR_PROJECT_ID)));
        point.setHasACoordinateID(cursor.getLong   (
                            cursor.getColumnIndex(GBDatabaseSqliteHelper.POINT_ISA_COORDINATE_ID)));

        point.setPointNumber     (cursor.getInt(
                            cursor.getColumnIndex(GBDatabaseSqliteHelper.POINT_NUMBER)));
        point.setOffsetDistance  (cursor.getDouble(
                            cursor.getColumnIndex(GBDatabaseSqliteHelper.POINT_OFFSET_DISTANCE)));
        point.setOffsetHeading   (cursor.getDouble(
                            cursor.getColumnIndex(GBDatabaseSqliteHelper.POINT_OFFSET_HEADING)));
        point.setOffsetElevation (cursor.getDouble(
                            cursor.getColumnIndex(GBDatabaseSqliteHelper.POINT_OFFSET_ELEVATION)));

        point.setHeight          (cursor.getDouble(
                cursor.getColumnIndex(GBDatabaseSqliteHelper.POINT_HEIGHT)));

        point.setPointFeatureCode(cursor.getString(
                                  cursor.getColumnIndex(GBDatabaseSqliteHelper.POINT_FEATURE_CODE)));
        point.setPointNotes      (cursor.getString(
                                  cursor.getColumnIndex(GBDatabaseSqliteHelper.POINT_NOTES)));

        point.setHdop            (cursor.getDouble(
                                  cursor.getColumnIndex(GBDatabaseSqliteHelper.POINT_HDOP)));
        point.setVdop            (cursor.getDouble(
                                  cursor.getColumnIndex(GBDatabaseSqliteHelper.POINT_VDOP)));
        point.setTdop            (cursor.getDouble(
                                  cursor.getColumnIndex(GBDatabaseSqliteHelper.POINT_TDOP)));
        point.setPdop            (cursor.getDouble(
                                  cursor.getColumnIndex(GBDatabaseSqliteHelper.POINT_PDOP)));
        point.setHrms            (cursor.getDouble(
                                  cursor.getColumnIndex(GBDatabaseSqliteHelper.POINT_HRMS)));
        point.setVrms            (cursor.getDouble(
                                  cursor.getColumnIndex(GBDatabaseSqliteHelper.POINT_VRMS)));


        return point;
    }

}
