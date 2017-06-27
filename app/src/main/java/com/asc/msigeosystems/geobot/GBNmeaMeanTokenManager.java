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
 */
class GBNmeaMeanTokenManager {

    /* **********************************/
    /* ******* Static Constants *********/
    /* **********************************/


    /* **********************************/
    /* ******* Static Variables *********/
    /* **********************************/
    private static GBNmeaMeanTokenManager ourInstance ;

    /* **********************************/
    /* ******* Member Variables *********/
    /* **********************************/


    /* **********************************/
    /* ******* Static Methods   *********/
    /* **********************************/
    public static GBNmeaMeanTokenManager getInstance() {
        if (ourInstance == null){
            ourInstance = new GBNmeaMeanTokenManager();

        }
        return ourInstance;
    }

    /* **********************************/
    /* ******* Constructors     *********/
    /* **********************************/
    private GBNmeaMeanTokenManager() {

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

    long addMeanToDB(GBNmeaMeanToken token){
        //add Mean to DB
        GBDatabaseManager databaseManager = GBDatabaseManager.getInstance();
        if (databaseManager.addToken(token) == GBDatabaseManager.sDB_ERROR_CODE) {
            return GBUtilities.ID_DOES_NOT_EXIST;
        }

        //Raw Data Coordinates
        //
        if (token.areCoordinatesInMemory()){
            //only bother writing them out if they already exist in memory,
            //don't get into the race condition of reading them in just to write them out again
            int last = token.getCoordinateSize();
            if (last == 0)return token.getMeanTokenID();

            //don't read them back in from the DB if they aren't alread in memory
            ArrayList<GBCoordinateWGS84> rawCoordinates = token.getCoordinates();

            int position = 0;
            GBCoordinateWGS84 coordinateWGS84;
            GBCoordinateManager coordinateManager = GBCoordinateManager.getInstance();

            while (position < last){
                coordinateWGS84 = rawCoordinates.get(position);
                coordinateManager.addCoordinate(coordinateWGS84);

                long coordinateID = coordinateWGS84.getCoordinateID();

                ContentValues cv = getCVFromTokenReading(token, position);

                coordinateManager.addCoordinateToReading(cv, coordinateID);

                position++;
            }
        }
        return token.getMeanTokenID();
    }


    //* ****************  READ *******************************************


    //* ****************  UPDATE *******************************************






    //* ****************  DELETE *******************************************



    /* ******************************************/
    /* ******* Translation Utility Methods  *****/
    /* ******************************************/

    //returns the ContentValues object needed to add/update the MEAN_TOKEN to/in the DB
    ContentValues getCVFromToken(GBNmeaMeanToken token){
        //convert the GBMeanToken object into a ContentValues object containing a token
        ContentValues cvToken = new ContentValues();
        //put(columnName, value);
        cvToken.put(GBDatabaseSqliteHelper.MEAN_TOKEN_ID,       token.getMeanTokenID());
        cvToken.put(GBDatabaseSqliteHelper.MEAN_TOKEN_PROJECT_ID, token.getProjectID());

        int progress = 0; //false
        boolean isProgress = token.isMeanInProgress();
        if (isProgress)progress = 1;//true;
        cvToken.put(GBDatabaseSqliteHelper.MEAN_TOKEN_IN_PROGRESS, progress);

        int first = 0; //false
        boolean isFirst = token.isFirstPointInMean();
        if (isFirst)first = 1;//true;
        cvToken.put(GBDatabaseSqliteHelper.MEAN_TOKEN_FIRST, first);

        int lastPoint = 0; //false
        boolean isLast = token.isLastPointInMean();
        if (isLast)lastPoint = 1;//true;
        cvToken.put(GBDatabaseSqliteHelper.MEAN_TOKEN_LAST, lastPoint);


        cvToken.put(GBDatabaseSqliteHelper.MEAN_TOKEN_START,   token.getStartMeanTime());
        cvToken.put(GBDatabaseSqliteHelper.MEAN_TOKEN_END,     token.getEndMeanTime());
        cvToken.put(GBDatabaseSqliteHelper.MEAN_TOKEN_CURRENT, token.getCurrentMeanTime());
        cvToken.put(GBDatabaseSqliteHelper.MEAN_TOKEN_FIXED,   token.getFixedReadings());
        cvToken.put(GBDatabaseSqliteHelper.MEAN_TOKEN_RAW,     token.getRawReadings());

        //MeanCoordinate does not get saved to the DB
        //MeanCoordinates must be saved individually by the caller
         return cvToken;
    }

    //returns the ContentValues object needed to add/update the MEAN_TOKEN_READING to/in the DB
    ContentValues getCVFromTokenReading(GBNmeaMeanToken token, int position){

        ContentValues cvTokenReading = new ContentValues();

        GBCoordinateWGS84 coordinateWGS84 = token.getCoordinateAt(position);
        long coordinateID = coordinateWGS84.getCoordinateID();


        //put(columnName, value);
        cvTokenReading.put(GBDatabaseSqliteHelper.MEAN_TOKEN_READING_MEAN_ID,  token.getMeanTokenID());
        cvTokenReading.put(GBDatabaseSqliteHelper.MEAN_TOKEN_READING_COORDINATE_ID, coordinateID);

        return cvTokenReading;
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
    GBNmeaMeanToken getMeanTokenFromCursor(Cursor cursor, int position){

        int last = cursor.getCount();
        if (position >= last) return null;

        GBNmeaMeanToken meanToken = new GBNmeaMeanToken();

        cursor.moveToPosition(position);

        long tokenID = cursor.getLong(cursor.getColumnIndex(GBDatabaseSqliteHelper.MEAN_TOKEN_ID));
        meanToken.setMeanTokenID(tokenID);
        long projectID = cursor.getLong(
                            cursor.getColumnIndex(GBDatabaseSqliteHelper.MEAN_TOKEN_PROJECT_ID));
        meanToken.setMeanTokenID(projectID);

        int inProgress = cursor.getInt(
                            cursor.getColumnIndex(GBDatabaseSqliteHelper.MEAN_TOKEN_IN_PROGRESS));
        if (inProgress == 0) {
            meanToken.setMeanInProgress(false);
        }else{
            meanToken.setMeanInProgress(true);
        }

        int first = cursor.getInt(cursor.getColumnIndex(GBDatabaseSqliteHelper.MEAN_TOKEN_FIRST));
        if (first == 0) {
            meanToken.setFirstPointInMean(false);
        }else{
            meanToken.setFirstPointInMean(true);
        }


        int lastFlag = cursor.getInt(cursor.getColumnIndex(GBDatabaseSqliteHelper.MEAN_TOKEN_LAST));
        if (lastFlag == 0) {
            meanToken.setLastPointInMean(false);
        }else{
            meanToken.setLastPointInMean(true);
        }

        long start = cursor.getLong(cursor.getColumnIndex(GBDatabaseSqliteHelper.MEAN_TOKEN_START));
        meanToken.setStartMeanTime(start);

        long end = cursor.getLong(cursor.getColumnIndex(GBDatabaseSqliteHelper.MEAN_TOKEN_END));
        meanToken.setEndMeanTime(end);

        long current = cursor.getLong(
                            cursor.getColumnIndex(GBDatabaseSqliteHelper.MEAN_TOKEN_CURRENT));
        meanToken.setCurrentMeanTime(current);

        int fixed = cursor.getInt(cursor.getColumnIndex(GBDatabaseSqliteHelper.MEAN_TOKEN_FIXED));
        meanToken.setFixedReadings(fixed);

        int raw = cursor.getInt(cursor.getColumnIndex(GBDatabaseSqliteHelper.MEAN_TOKEN_RAW));
        meanToken.setRawReadings(raw);

        long meanID = cursor.getLong(
                            cursor.getColumnIndex(GBDatabaseSqliteHelper.MEAN_TOKEN_MEAN_COORD_ID));
        GBCoordinateMean coordinateMean = GBCoordinateManager.getInstance().getCoordinateMeanFromDB(meanID);
        meanToken.setMeanCoordinate(coordinateMean);

        return meanToken;
    }

    //The cursor input contains all the NmeaMeanTokenReadings that link our
    // MeanToken to GBCoordinateWgs84 instances included in the mean covered by the token
    /*+****************************************
     *  There isn't a GB object corresponding to a TokenReading. It exists only in the DB
     *  as a 1 to many relationship between a GBMeanToken and the GBCoordinateWgs84
     *  coordinates that are included in the mean result corresponding to the Token
     */
    GBCoordinateWGS84 getCoordinateFromMeanTokenReadingCursor(Cursor cursor, int cursorPosition){
        int last = cursor.getCount();
        if (cursorPosition >= last) return null;



        cursor.moveToPosition(cursorPosition);

        long coordinateID = cursor.getLong(
                cursor.getColumnIndex(GBDatabaseSqliteHelper.MEAN_TOKEN_READING_COORDINATE_ID));

        GBCoordinateManager coordinateManager = GBCoordinateManager.getInstance();
        return coordinateManager.getCoordinateWgs84(coordinateID);
    }







    /* ******************************************/
    /* ******* General Utility Methods      *****/
    /* ******************************************/


}
