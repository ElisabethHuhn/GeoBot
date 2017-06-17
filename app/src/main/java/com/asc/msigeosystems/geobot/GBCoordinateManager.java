package com.asc.msigeosystems.geobot;

import android.content.ContentValues;
import android.database.Cursor;



import java.util.ArrayList;

/* 
 * Created by Elisabeth Huhn on 11/25/2016.
 *
 * This manager hides the fact that the Coordinate objects are mirrored in a DB
 * If a Coordinate is not found in memory, the DB is queried for it.
 * If a Coordinate is added to memory,     it will also be added to the DB
 * If a Coordinate is updated in memory,   it is also updated in teh DB
 * If a Coordinate is deleted from memory, it is also deleted from the DB
 *
 * This manager is a bit different from the other object managers of GB in that
 * it manages more than one type of coordinate. It manages all the subclasses of
 * GBCoordinate. The subclasses are in different tables, depending on whether
 * they are principally Lat/Long coordinate systems or Easting/Northing coordinate systems.
 */
class GBCoordinateManager {

    /* **********************************/
    /* ******* Static Constants *********/
    /* **********************************/
    //public static final int COORDINATE_NOT_FOUND = -1;


    /* **********************************/
    /* ******* Static Variables *********/
    /* **********************************/
    private static GBCoordinateManager ourInstance ;

    /* **********************************/
    /* ******* Member Variables *********/
    /* **********************************/


    /* **********************************/
    /* ******* Static Methods   *********/
    /* **********************************/
    public static GBCoordinateManager getInstance() {
        if (ourInstance == null){
            ourInstance = new GBCoordinateManager();

        }
        return ourInstance;
    }

    /* **********************************/
    /* ******* Constructors     *********/
    /* **********************************/
    private GBCoordinateManager() {

        //mCoordinateList = new ArrayList<>();

        //The DB isn't read in until the first time a coordinate is accessed
    }


    /* **********************************/
    /* ******* Setters/Getters  *********/
    /* **********************************/

    /* *****************************************/
    /* *******     CRUD Methods        *********/
    /* *****************************************/


    //* ****************  CREATE *******************************************

   public long addCoordinateMean(GBCoordinateMean coordinateMean){
       GBDatabaseManager databaseManager = GBDatabaseManager.getInstance();
       return databaseManager.addCoordinateMean(coordinateMean);
   }

    //* ****************  READ *******************************************

    public ArrayList<GBCoordinateMean> getAllCoordinateMeanFromDB (int projectID){
        GBDatabaseManager databaseManager = GBDatabaseManager.getInstance();
        return databaseManager.getAllCoordinateMeanFromDB(projectID);
    }

    public GBCoordinateMean getCoordinateMeanFromDB(int coordinateMeanID, int projectID){
        GBDatabaseManager databaseManager = GBDatabaseManager.getInstance();
        return databaseManager.getCoordinateMeanFromDB(coordinateMeanID, projectID);
    }

    //* ****************  UPDATE *******************************************






    //* ****************  DELETE *******************************************


    //The return code indicates how many rows affected
    public int removeCoordinateMean(int coordinateMeanID, int projectID){
        GBDatabaseManager databaseManager = GBDatabaseManager.getInstance();
        return databaseManager.removeCoordinateMean(coordinateMeanID, projectID);
    }

    //The return code indicates how many rows affected
    public int removeProjectCoordinatesMean(int projectID){
        GBDatabaseManager databaseManager = GBDatabaseManager.getInstance();
        return databaseManager.removeProjectCoordinatesMean(projectID);
    }



    /* ******************************************/
    /* ******* Translation Utility Methods  *****/
    /* ******************************************/

    //returns the ContentValues object needed to add/update the COORDINATE to/in the DB
    ContentValues getCVFromCoordinate(GBCoordinate coordinate){
        //convert the GBCoordinate object into a ContentValues object containing a coordinate
        ContentValues cvCoordinate = new ContentValues();
        //put(columnName, value);
        cvCoordinate.put(GBDatabaseSqliteHelper.COORDINATE_ID,       coordinate.getCoordinateID());
        cvCoordinate.put(GBDatabaseSqliteHelper.COORDINATE_PROJECT_ID,  coordinate.getProjectID());
        cvCoordinate.put(GBDatabaseSqliteHelper.COORDINATE_POINT_ID,     coordinate.getPointID());

        CharSequence coordinateType = coordinate.getCoordinateType();
        if (coordinateType.equals(GBCoordinate.sCoordinateTypeWGS84)){
            cvCoordinate.put(GBDatabaseSqliteHelper.COORDINATE_TYPE,
                             GBCoordinate.sCoordinateDBTypeWGS84);

        } else if (coordinateType.equals(GBCoordinate.sCoordinateTypeNAD83)){
            cvCoordinate.put(GBDatabaseSqliteHelper.COORDINATE_TYPE,
                             GBCoordinate.sCoordinateDBTypeNAD83);

        } else if (coordinateType.equals(GBCoordinate.sCoordinateTypeUTM)){
            cvCoordinate.put(GBDatabaseSqliteHelper.COORDINATE_TYPE,
                             GBCoordinate.sCoordinateDBTypeUTM);

        } else if (coordinateType.equals(GBCoordinate.sCoordinateTypeSPCS)){
            cvCoordinate.put(GBDatabaseSqliteHelper.COORDINATE_TYPE,
                             GBCoordinate.sCoordinateDBTypeSPCS);
        } else {
            cvCoordinate.put(GBDatabaseSqliteHelper.COORDINATE_TYPE,
                             GBCoordinate.sCoordinateDBTypeUnknown);
        }

        int valCoord = 0; //false
        boolean validCoordinate = coordinate.isValidCoordinate();
        if (validCoordinate)valCoord = 1;//true;

        cvCoordinate.put(GBDatabaseSqliteHelper.COORDINATE_VALID_COORD, valCoord);


        //The rest of the attributes depend upon the specific subtype

        if ((coordinateType.equals(GBCoordinate.sCoordinateTypeWGS84))||
            (coordinateType.equals(GBCoordinate.sCoordinateTypeNAD83))    ){

            cvCoordinate.put(GBDatabaseSqliteHelper.COORDINATE_LL_TIME,
                                            ((GBCoordinateLL)coordinate).getTime());
            cvCoordinate.put(GBDatabaseSqliteHelper.COORDINATE_LL_LATITUDE,
                                            ((GBCoordinateLL)coordinate).getLatitude());
 /*
            cvCoordinate.put(GBDatabaseSqliteHelper.COORDINATE_LL_LATITUDE_DEGREE,
                                            ((GBCoordinateLL)coordinate).getLatitudeDegree());
            cvCoordinate.put(GBDatabaseSqliteHelper.COORDINATE_LL_LATITUDE_MINUTE,
                                            ((GBCoordinateLL)coordinate).getLatitudeMinute());
            cvCoordinate.put(GBDatabaseSqliteHelper.COORDINATE_LL_LATITUDE_SECOND,
                                            ((GBCoordinateLL)coordinate).getLatitudeSecond());
   */
            cvCoordinate.put(GBDatabaseSqliteHelper.COORDINATE_LL_LONGITUDE,
                                            ((GBCoordinateLL)coordinate).getLongitude());
/*
            cvCoordinate.put(GBDatabaseSqliteHelper.COORDINATE_LL_LONGITUDE_DEGREE,
                                            ((GBCoordinateLL)coordinate).getLongitudeDegree());
            cvCoordinate.put(GBDatabaseSqliteHelper.COORDINATE_LL_LONGITUDE_MINUTE,
                                            ((GBCoordinateLL)coordinate).getLongitudeMinute());
            cvCoordinate.put(GBDatabaseSqliteHelper.COORDINATE_LL_LONGITUDE_SECOND,
                                            ((GBCoordinateLL)coordinate).getLongitudeSecond());
 */
            cvCoordinate.put(GBDatabaseSqliteHelper.COORDINATE_LL_ELEVATION,
                                            ((GBCoordinateLL)coordinate).getElevation());
            cvCoordinate.put(GBDatabaseSqliteHelper.COORDINATE_LL_GEOID,
                                            ((GBCoordinateLL)coordinate).getGeoid());


        } else {
            cvCoordinate.put(GBDatabaseSqliteHelper.COORDINATE_EN_EASTING,
                                            ((GBCoordinateEN)coordinate).getEasting());
            cvCoordinate.put(GBDatabaseSqliteHelper.COORDINATE_EN_NORTHING,
                                            ((GBCoordinateEN)coordinate).getNorthing());
            cvCoordinate.put(GBDatabaseSqliteHelper.COORDINATE_EN_ELEVATION,
                                            ((GBCoordinateEN)coordinate).getElevation());
            cvCoordinate.put(GBDatabaseSqliteHelper.COORDINATE_EN_ZONE,
                                            ((GBCoordinateEN)coordinate).getZone());
            cvCoordinate.put(GBDatabaseSqliteHelper.COORDINATE_EN_HEMISPHERE,
                                String.valueOf(((GBCoordinateEN)coordinate).getHemisphere()));
            cvCoordinate.put(GBDatabaseSqliteHelper.COORDINATE_EN_LATBAND,
                                String.valueOf(((GBCoordinateEN)coordinate).getLatBand()));
            cvCoordinate.put(GBDatabaseSqliteHelper.COORDINATE_EN_DATUM,
                                            (String) ((GBCoordinateEN)coordinate).getDatum());
            cvCoordinate.put(GBDatabaseSqliteHelper.COORDINATE_EN_CONVERGENCE,
                                            ((GBCoordinateEN)coordinate).getConvergence());
            cvCoordinate.put(GBDatabaseSqliteHelper.COORDINATE_EN_SCALE,
                                            ((GBCoordinateEN)coordinate).getScale());

        }
        return cvCoordinate;
    }


    //returns the GBCoordinate characterized by the position within the Cursor
    //returns null if the position is larger than the size of the Cursor
    //NOTE    this routine does NOT add the coordinate to the list maintained by this CoordinateManager
    //        The caller of this routine is responsible for that.
    //        This is only a translation utility
    //WARNING As the app is not multi-threaded, this routine is not synchronized.
    //        If the app becomes multi-threaded, this routine must be made thread safe
    //WARNING The cursor is NOT closed by this routine. It assumes the caller will close the
    //         cursor when it is done with it
    GBCoordinate getCoordinateFromCursor(Cursor cursor, int position){

        int last = cursor.getCount();
        if (position >= last) return null;

        cursor.moveToPosition(position);

        int coordinateType = cursor.getInt(
                                    cursor.getColumnIndex(GBDatabaseSqliteHelper.COORDINATE_TYPE));

        if (coordinateType == GBCoordinate.sCoordinateDBTypeWGS84) {
            GBCoordinateWGS84 coordinate = new GBCoordinateWGS84();
            coordinate = (GBCoordinateWGS84) getLLCoordinateFromCursor(coordinate, cursor);

            coordinate.setCoordinateType();
            return coordinate;

        } else if (coordinateType == GBCoordinate.sCoordinateDBTypeNAD83) {
                GBCoordinateNAD83 coordinate = new GBCoordinateNAD83();
                getLLCoordinateFromCursor( coordinate, cursor);
                coordinate = (GBCoordinateNAD83) getLLCoordinateFromCursor(coordinate, cursor);

            coordinate.setCoordinateType();
            return coordinate;


        } else if (coordinateType == GBCoordinate.sCoordinateDBTypeUTM) {
            GBCoordinateUTM coordinate = new GBCoordinateUTM();
            getENCoordinateFromCursor( coordinate, cursor);
            coordinate = (GBCoordinateUTM)getENCoordinateFromCursor(coordinate, cursor);

            coordinate.setCoordinateType();
            return coordinate;

        } else if (coordinateType == GBCoordinate.sCoordinateDBTypeSPCS) {
            GBCoordinateSPCS coordinate = new GBCoordinateSPCS();
            getENCoordinateFromCursor( coordinate, cursor);
            coordinate = (GBCoordinateSPCS) getENCoordinateFromCursor(coordinate, cursor);

            coordinate.setCoordinateType();
            return coordinate;

        }
        return null;
    }

    private GBCoordinateLL getLLCoordinateFromCursor(GBCoordinateLL coordinate,
                                                           Cursor cursor){
        coordinate.setCoordinateID(cursor.getInt  (
                        cursor.getColumnIndex(GBDatabaseSqliteHelper.COORDINATE_ID)));
        coordinate.setProjectID(cursor.getInt  (
                        cursor.getColumnIndex(GBDatabaseSqliteHelper.COORDINATE_PROJECT_ID)));
        coordinate.setPointID(cursor.getInt  (
                        cursor.getColumnIndex(GBDatabaseSqliteHelper.COORDINATE_POINT_ID)));

        //type is set in calling routine


        int valid = cursor.getInt(
                cursor.getColumnIndex(GBDatabaseSqliteHelper.COORDINATE_VALID_COORD));
        if (valid == 0) {
            coordinate.setValidCoordinate(false);
        }else{
            coordinate.setValidCoordinate(true);
        }


        int isFixed = cursor.getInt(
                cursor.getColumnIndex(GBDatabaseSqliteHelper.COORDINATE_IS_FIXED));
        if (isFixed == 0) {
            coordinate.setIsFixed(false);
        }else{
            coordinate.setIsFixed(true);
        }


        coordinate.setTime(cursor.getLong (
                        cursor.getColumnIndex(GBDatabaseSqliteHelper.COORDINATE_LL_TIME)));

        coordinate.setLatitude(cursor.getDouble(
                        cursor.getColumnIndex(GBDatabaseSqliteHelper.COORDINATE_LL_LATITUDE)));
  /*
        coordinate.setLatitudeDegree(cursor.getInt(
                        cursor.getColumnIndex(GBDatabaseSqliteHelper.COORDINATE_LL_LATITUDE_DEGREE)));
        coordinate.setLatitudeMinute(cursor.getInt(
                        cursor.getColumnIndex(GBDatabaseSqliteHelper.COORDINATE_LL_LATITUDE_MINUTE)));
        coordinate.setLatitudeSecond(cursor.getDouble(
                        cursor.getColumnIndex(GBDatabaseSqliteHelper.COORDINATE_LL_LATITUDE_SECOND)));
*/

        coordinate.setLongitude(cursor.getDouble(
                        cursor.getColumnIndex(GBDatabaseSqliteHelper.COORDINATE_LL_LONGITUDE)));
 /*
        coordinate.setLongitudeDegree(cursor.getInt(
                        cursor.getColumnIndex(GBDatabaseSqliteHelper.COORDINATE_LL_LONGITUDE_DEGREE)));
        coordinate.setLongitudeMinute(cursor.getInt(
                        cursor.getColumnIndex(GBDatabaseSqliteHelper.COORDINATE_LL_LONGITUDE_MINUTE)));
        coordinate.setLongitudeSecond(cursor.getDouble(
                        cursor.getColumnIndex(GBDatabaseSqliteHelper.COORDINATE_LL_LONGITUDE_SECOND)));
*/

        coordinate.setElevation(cursor.getDouble(
                        cursor.getColumnIndex(GBDatabaseSqliteHelper.COORDINATE_LL_ELEVATION)));
        coordinate.setGeoid(cursor.getDouble(
                        cursor.getColumnIndex(GBDatabaseSqliteHelper.COORDINATE_LL_GEOID)));

        //Degrees, minutes, seconds are not stored in the DB
        coordinate.convertDDToDMS();

        return coordinate;
    }



    private GBCoordinateEN getENCoordinateFromCursor(GBCoordinateEN coordinate,
                                                          Cursor cursor){
        coordinate.setCoordinateID(cursor.getInt  (
                        cursor.getColumnIndex(GBDatabaseSqliteHelper.COORDINATE_ID)));
        coordinate.setProjectID(cursor.getInt  (
                        cursor.getColumnIndex(GBDatabaseSqliteHelper.COORDINATE_PROJECT_ID)));
        coordinate.setPointID(cursor.getInt  (
                        cursor.getColumnIndex(GBDatabaseSqliteHelper.COORDINATE_POINT_ID)));

        //type set in caller of this routine


        int valid = cursor.getInt(
                cursor.getColumnIndex(GBDatabaseSqliteHelper.COORDINATE_VALID_COORD));
        if (valid == 0) {
            coordinate.setValidCoordinate(false);
        }else{
            coordinate.setValidCoordinate(true);
        }

        int isFixed = cursor.getInt(
                cursor.getColumnIndex(GBDatabaseSqliteHelper.COORDINATE_IS_FIXED));
        if (isFixed == 0) {
            coordinate.setIsFixed(false);
        }else{
            coordinate.setIsFixed(true);
        }


        coordinate.setEasting(cursor.getDouble(
                        cursor.getColumnIndex(GBDatabaseSqliteHelper.COORDINATE_EN_EASTING)));
        coordinate.setNorthing(cursor.getDouble(
                        cursor.getColumnIndex(GBDatabaseSqliteHelper.COORDINATE_EN_NORTHING)));
        coordinate.setElevation(cursor.getDouble(
                        cursor.getColumnIndex(GBDatabaseSqliteHelper.COORDINATE_EN_ELEVATION)));

        coordinate.setZone(cursor.getInt(
                        cursor.getColumnIndex(GBDatabaseSqliteHelper.COORDINATE_EN_ZONE)));


        String latBand = cursor.getString(
                cursor.getColumnIndex(GBDatabaseSqliteHelper.COORDINATE_EN_LATBAND));
        coordinate.setLatBand(latBand.charAt(0));

        String hemisphere = cursor.getString(
                cursor.getColumnIndex(GBDatabaseSqliteHelper.COORDINATE_EN_HEMISPHERE));
        coordinate.setHemisphere(hemisphere.charAt(0)  ) ;


        coordinate.setDatum(cursor.getString(
                        cursor.getColumnIndex(GBDatabaseSqliteHelper.COORDINATE_EN_DATUM)));
        coordinate.setConvergence(cursor.getDouble(
                        cursor.getColumnIndex(GBDatabaseSqliteHelper.COORDINATE_EN_CONVERGENCE)));
        coordinate.setScale(cursor.getDouble(
                        cursor.getColumnIndex(GBDatabaseSqliteHelper.COORDINATE_LL_ELEVATION)));


        return coordinate;
    }




    //returns the ContentValues object needed to add/update the COORDINATE to/in the DB
    ContentValues getCVFromCoordinateMean(GBCoordinateMean coordinate){

        ContentValues cvCoordinate = new ContentValues();
        //put(columnName, value);
        cvCoordinate.put(GBDatabaseSqliteHelper.COORDINATE_MEAN_ID, coordinate.getCoordinateID());
        cvCoordinate.put(GBDatabaseSqliteHelper.COORDINATE_MEAN_PROJECT_ID,coordinate.getProjectID());
        cvCoordinate.put(GBDatabaseSqliteHelper.COORDINATE_MEAN_POINT_ID,coordinate.getPointID());

        //Easting northing vs Latitude longitude
        int coorTyp = 0; //false or Latitude/Longitude
        boolean eastingNorthing = coordinate.isEastingNorthing();
        if (eastingNorthing)coorTyp = 1;//true; Easting/Northing
        cvCoordinate.put(GBDatabaseSqliteHelper.COORDINATE_MEAN_TYPE, coorTyp);

        //are the latitude longitude values valid on Earth?
        int valCoord = 0; //false
        boolean validCoordinate = coordinate.isValidCoordinate();
        if (validCoordinate)valCoord = 1;//true;
        cvCoordinate.put(GBDatabaseSqliteHelper.COORDINATE_MEAN_VALID_COORD, valCoord);

        int isFixedInt = 0; //false
        boolean isFixed = coordinate.isFixed();
        if (isFixed)isFixedInt = 1;//true;
        cvCoordinate.put(GBDatabaseSqliteHelper.COORDINATE_MEAN_IS_FIXED, isFixedInt);

        cvCoordinate.put(GBDatabaseSqliteHelper.COORDINATE_MEAN_RAW,coordinate.getRawReadings());
        cvCoordinate.put(GBDatabaseSqliteHelper.COORDINATE_MEAN_MEANED,coordinate.getMeanedReadings());
        cvCoordinate.put(GBDatabaseSqliteHelper.COORDINATE_MEAN_FIXED,coordinate.getFixedReadings());

        cvCoordinate.put(GBDatabaseSqliteHelper.COORDINATE_MEAN_LATITUDE,coordinate.getLatitude());
        cvCoordinate.put(GBDatabaseSqliteHelper.COORDINATE_MEAN_LATITUDE_STD,coordinate.getLatitudeStdDev());
        cvCoordinate.put(GBDatabaseSqliteHelper.COORDINATE_MEAN_LONGITUDE,coordinate.getLongitude());
        cvCoordinate.put(GBDatabaseSqliteHelper.COORDINATE_MEAN_LONGITUDE_STD,coordinate.getLongitudeStdDev());
        cvCoordinate.put(GBDatabaseSqliteHelper.COORDINATE_MEAN_ELEVATION,coordinate.getElevation());
        cvCoordinate.put(GBDatabaseSqliteHelper.COORDINATE_MEAN_ELEVATION_STD,coordinate.getElevationStdDev());
        cvCoordinate.put(GBDatabaseSqliteHelper.COORDINATE_MEAN_GEOID,coordinate.getGeoid());
        cvCoordinate.put(GBDatabaseSqliteHelper.COORDINATE_MEAN_SATELLITES,coordinate.getSatellites());


        return cvCoordinate;
    }


    GBCoordinateMean getCoordinateMeanFromCursor(Cursor cursor, int position){

        int last = cursor.getCount();
        if (position >= last) return null;

        cursor.moveToPosition(position);

        GBCoordinateMean coordinate = new GBCoordinateMean();

        coordinate.setCoordinateID(cursor.getInt  (
                cursor.getColumnIndex(GBDatabaseSqliteHelper.COORDINATE_MEAN_ID)));
        coordinate.setProjectID(cursor.getInt  (
                cursor.getColumnIndex(GBDatabaseSqliteHelper.COORDINATE_MEAN_PROJECT_ID)));
        coordinate.setPointID(cursor.getInt  (
                cursor.getColumnIndex(GBDatabaseSqliteHelper.COORDINATE_MEAN_POINT_ID)));

        //type is set in calling routine


        int type = cursor.getInt(cursor.getColumnIndex(GBDatabaseSqliteHelper.COORDINATE_MEAN_TYPE));
        if (type == 0) { //0 = Latitude/Longitude, 1 = Easting/Northing
            coordinate.setType(GBCoordinateMean.LATITUDE_LONGITUDE);
        }else{
            coordinate.setType(GBCoordinateMean.EASTING_NORTHING);
        }


        int valid = cursor.getInt(
                cursor.getColumnIndex(GBDatabaseSqliteHelper.COORDINATE_MEAN_VALID_COORD));
        if (valid == 0) {
            coordinate.setValidCoordinate(false);
        }else{
            coordinate.setValidCoordinate(true);
        }

        int isFixed = cursor.getInt(
                cursor.getColumnIndex(GBDatabaseSqliteHelper.COORDINATE_MEAN_IS_FIXED));
        if (isFixed == 0) {
            coordinate.setIsFixed(false);
        }else{
            coordinate.setIsFixed(true);
        }

        coordinate.setRawReadings(cursor.getInt (
                cursor.getColumnIndex(GBDatabaseSqliteHelper.COORDINATE_MEAN_RAW)));

        coordinate.setMeanedReadings(cursor.getInt (
                cursor.getColumnIndex(GBDatabaseSqliteHelper.COORDINATE_MEAN_MEANED)));

        coordinate.setFixedReadings(cursor.getInt (
                cursor.getColumnIndex(GBDatabaseSqliteHelper.COORDINATE_MEAN_FIXED)));

        coordinate.setLatitude(cursor.getDouble(
                cursor.getColumnIndex(GBDatabaseSqliteHelper.COORDINATE_MEAN_LATITUDE)));

        coordinate.setLongitude(cursor.getDouble(
                cursor.getColumnIndex(GBDatabaseSqliteHelper.COORDINATE_MEAN_LONGITUDE)));


        //convert DD to DMS by creating a new coordinateWGS
        GBCoordinateWGS84 coordinateWGS84 =
                new GBCoordinateWGS84(coordinate.getLatitude(), coordinate.getLongitude());
        coordinate.setLatitudeDegree(coordinateWGS84.getLatitudeDegree());
        coordinate.setLatitudeMinute(coordinateWGS84.getLatitudeMinute());
        coordinate.setLatitudeSecond(coordinateWGS84.getLatitudeSecond());

        coordinate.setLongitudeDegree(coordinateWGS84.getLongitudeDegree());
        coordinate.setLongitudeMinute(coordinateWGS84.getLongitudeMinute());
        coordinate.setLongitudeSecond(coordinateWGS84.getLongitudeSecond());

        coordinate.setLatitudeStdDev(cursor.getDouble(
                cursor.getColumnIndex(GBDatabaseSqliteHelper.COORDINATE_MEAN_LATITUDE_STD)));

        coordinate.setLongitudeStdDev(cursor.getDouble(
                cursor.getColumnIndex(GBDatabaseSqliteHelper.COORDINATE_MEAN_LONGITUDE_STD)));



        coordinate.setElevation(cursor.getDouble(
                cursor.getColumnIndex(GBDatabaseSqliteHelper.COORDINATE_MEAN_ELEVATION)));
        coordinate.setElevationStdDev(cursor.getDouble(
                cursor.getColumnIndex(GBDatabaseSqliteHelper.COORDINATE_MEAN_ELEVATION_STD)));
        coordinate.setGeoid(cursor.getDouble(
                cursor.getColumnIndex(GBDatabaseSqliteHelper.COORDINATE_MEAN_GEOID)));


        coordinate.setSatellites(cursor.getInt(
                cursor.getColumnIndex(GBDatabaseSqliteHelper.COORDINATE_MEAN_SATELLITES)));



        return coordinate;

    }


    /* ******************************************/
    /* ******* General Utility Methods      *****/
    /* ******************************************/


}
