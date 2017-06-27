package com.asc.msigeosystems.geobot;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static com.asc.msigeosystems.geobot.GBDatabaseManager.sDB_ERROR_CODE;

/**
 * Created by Elisabeth Huhn on 7/9/2016.
 * This class makes all the actual calls to the DB
 * Thus, if there is a need to put such calls on a background thread, that
 * can be managed by the DB Manager.
 * But if it touches the DB directly, this class does it
 */
class GBDatabaseSqliteHelper extends SQLiteOpenHelper {
    //logcat Tag
    //private static final String TAG = "Prism4DSqliteOpenHelper";

    /* ***************************************************/
    /* ***************************************************/
    /* ***************************************************/

    //Database Version
    private static final int DATABASE_VERSION = 1;

    //Database Name
    private static final String DATABASE_NAME = "Prism4D";


    /* ***************************************************/
    /* ***************************************************/
    /* ***************************************************/

    //Global Column Names
    static final String KEY_ID = "id";
    static final String KEY_CREATED_AT = "created_at";


    /* ***************************************************/
    /* ********  Tables          *************************/
    /* ***************************************************/

    //Table Names
    static final String TABLE_PROJECT =          "Project";
    static final String TABLE_PROJECT_SETTINGS = "ProjectSettings";
    static final String TABLE_POINT =            "Point";
    static final String TABLE_COORDINATE_EN =    "CoordinateEN";
    static final String TABLE_COORDINATE_LL =    "CoordinateLL";
    static final String TABLE_COORDINATE_MEAN  = "CoordinateMean";
    static final String TABLE_MEAN_TOKEN       = "MeanToken";
    static final String TABLE_MEAN_TOKEN_READINGS = "MeanTokenReadings";

    static final String TABLE_PICTURE =          "Picture";



    /* ***************************************************/
    /* ********** Project       **************************/
    /* ***************************************************/

    //Project Column Names
    static final String PROJECT_ID =              "proj_id";
    static final String PROJECT_NAME =            "proj_name";
    static final String PROJECT_CREATED =         "proj_created";
    static final String PROJECT_LAST_MAINTAINED = "proj_last_maintained";
    static final String PROJECT_DESCRIPTION =     "proj_description";
    static final String PROJECT_COORDINATE_TYPE = "proj_coordinate_type";


    //create project table
    //NOTE: Dates are stored as long, NOT as a Date.
    //      The conversion is done when CV is created and when Cursor is translated
    private static final String CREATE_TABLE_PROJECT = "CREATE TABLE " +
            TABLE_PROJECT           + "("            +
            KEY_ID                  + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            PROJECT_ID              + " INTEGER, "   +
            PROJECT_NAME            + " TEXT, "      +
            PROJECT_CREATED         + " TEXT, "      +
            PROJECT_LAST_MAINTAINED + " TEXT, "      +
            PROJECT_DESCRIPTION     + " TEXT, "      +
            PROJECT_COORDINATE_TYPE + " TEXT, "      +
            KEY_CREATED_AT          + " DATETIME"    + ")";

    /* ***************************************************/
    /* ******  Project Settings   ************************/
    /* ***************************************************/

    //Project Settings Column Names
    static final String PROJECT_SETTINGS_ID                = "projset_id";
    /*
    static final String PROJECT_SETTINGS_DISTANCE_UNITS    = "projset_distance_units";
    static final String PROJECT_SETTINGS_DECIMAL_DISPLAY   = "projset_decimal_display";
    static final String PROJECT_SETTINGS_ANGLE_UNITS       = "projset_angle_units";
    static final String PROJECT_SETTINGS_ANGLE_DISPLAY     = "projset_angle_display";
    static final String PROJECT_SETTINGS_GRID_DIRECTION    = "projset_grid_direction";
    static final String PROJECT_SETTINGS_SCALE_FACTOR      = "projset_scale";
    static final String PROJECT_SETTINGS_SEA_LEVEL         = "projset_sealevel";
    static final String PROJECT_SETTINGS_REFRACTION        = "projset_refraction";
    static final String PROJECT_SETTINGS_DATUM             = "projset_datum";
    static final String PROJECT_SETTINGS_PROJECTION        = "projset_projection";
    static final String PROJECT_SETTINGS_ZONE              = "projset_zone";
    static final String PROJECT_SETTINGS_COORDINATE_DISPLAY= "projset_coord_display";
    static final String PROJECT_SETTINGS_SPC_SCALE_FACTOR  = "projset_spc_scale";
    static final String PROJECT_SETTINGS_GEOID_MODEL       = "projset_geoid_model";
    static final String PROJECT_SETTINGS_STARTING_POINT_ID = "projset_starting_point_id";
    static final String PROJECT_SETTINGS_ALPHANUMERIC      = "projset_alphanumeric";
    static final String PROJECT_SETTINGS_FEATURE_CODES     = "projset_feature_codes";
    static final String PROJECT_SETTINGS_FC_CONTROL_FILE   = "projset_fc_control_file";
    static final String PROJECT_SETTINGS_FC_TIMESTAMP      = "projset_fc_timestamp";
    */
    static final String PROJECT_SETTINGS_MEANING_METHOD    = "projset_meaning_method";
    static final String PROJECT_SETTINGS_MEANING_NUMBER    = "projset_meaning_number";
    static final String PROJECT_SETTINGS_LOCATION_PRECISION = "projset_location_precision";
    static final String PROJECT_SETTINGS_STDDEV_PRECISION  = "projset_stddev_precision";
    static final String PROJECT_SETTINGS_RMS_V_STDDEV      = "projset_rms_v_stddev";



    //create project settings table
    private static final String CREATE_TABLE_PROJECT_SETTINGS = "CREATE TABLE " +
            TABLE_PROJECT_SETTINGS           + "("           +
            KEY_ID                           + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            PROJECT_SETTINGS_ID              + " INTEGER, "  +
            /*
            PROJECT_SETTINGS_DISTANCE_UNITS  + " TEXT, "     +
            PROJECT_SETTINGS_DECIMAL_DISPLAY + " TEXT, "     +
            PROJECT_SETTINGS_ANGLE_UNITS     + " TEXT, "     +
            PROJECT_SETTINGS_GRID_DIRECTION  + " TEXT, "     +
            PROJECT_SETTINGS_SCALE_FACTOR    + " REAL, "     +
            PROJECT_SETTINGS_SEA_LEVEL       + " INTEGER, "  + //BOOLEAN  no-0/1-yes
            PROJECT_SETTINGS_REFRACTION      + " INTEGER, "  + //BOOLEAN  0/1
            PROJECT_SETTINGS_DATUM           + " TEXT, "     +
            PROJECT_SETTINGS_PROJECTION      + " TEXT, "     +
            PROJECT_SETTINGS_ZONE            + " TEXT, "     +
            PROJECT_SETTINGS_COORDINATE_DISPLAY + " TEXT, "  +
            PROJECT_SETTINGS_SPC_SCALE_FACTOR + " REAL, "    +
            PROJECT_SETTINGS_GEOID_MODEL     + " TEXT, "     +
            PROJECT_SETTINGS_STARTING_POINT_ID + " INTEGER, "+
            PROJECT_SETTINGS_ALPHANUMERIC    + " INTEGER, "  + //BOOLEAN  no-0/1-yes
            PROJECT_SETTINGS_FEATURE_CODES   + " TEXT, "     +
            PROJECT_SETTINGS_FC_CONTROL_FILE + " TEXT, "     +
            PROJECT_SETTINGS_FC_TIMESTAMP    + " INTEGER, "  + //BOOLEAN  no-0/1-yes
            */
            PROJECT_SETTINGS_MEANING_METHOD      + " INTEGER, "  +
            PROJECT_SETTINGS_MEANING_NUMBER      + " INTEGER, "  +
            PROJECT_SETTINGS_LOCATION_PRECISION  + " INTEGER, "  +
            PROJECT_SETTINGS_STDDEV_PRECISION    + " INTEGER, "  +
            PROJECT_SETTINGS_RMS_V_STDDEV        + " INTEGER, "  +

            KEY_CREATED_AT                   + " INTEGER "  + ")";


    /* ***************************************************/
    /* *********    Point           **********************/
    /* ***************************************************/

    //Point Column Names
    static final String POINT_ID             = "point_id";
    static final String POINT_FOR_PROJECT_ID = "point_project_id";
    static final String POINT_ISA_COORDINATE_ID =  "point_coordinate_ID";
    static final String POINT_OFFSET_DISTANCE =  "point_offset_distance";
    static final String POINT_OFFSET_HEADING =  "point_offset_heading";
    static final String POINT_OFFSET_ELEVATION =  "point_offset_elevation";
    static final String POINT_HEIGHT         =  "point_height";
    static final String POINT_FEATURE_CODE   = "point_feature_code";
    static final String POINT_NOTES          = "point_notes";
    static final String POINT_HDOP           = "point_hdop";
    static final String POINT_VDOP           = "point_vdop";
    static final String POINT_TDOP           = "point_tdop";
    static final String POINT_PDOP           = "point_pdop";
    static final String POINT_HRMS           = "point_hrms";
    static final String POINT_VRMS           = "point_vrms";



    //create point table
    private static final String CREATE_TABLE_POINT = "CREATE TABLE " +
            TABLE_POINT             + "("          +
            KEY_ID                  + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            POINT_ID                + " INTEGER, " +
            POINT_FOR_PROJECT_ID    + " INTEGER, " +
            POINT_ISA_COORDINATE_ID + " INTEGER, " +
            POINT_OFFSET_DISTANCE   + " REAL, "    +
            POINT_OFFSET_HEADING    + " REAL, "    +
            POINT_OFFSET_ELEVATION  + " REAL, "    +
            POINT_HEIGHT            + " REAL, "    +
            POINT_FEATURE_CODE      + " TEXT, "    +
            POINT_NOTES             + " TEXT, "    +
            POINT_HDOP              + " REAL, "    +
            POINT_VDOP              + " REAL, "    +
            POINT_TDOP              + " REAL, "    +
            POINT_PDOP              + " REAL, "    +
            POINT_HRMS              + " REAL, "    +
            POINT_VRMS              + " REAL, "    +
            KEY_CREATED_AT          + " INTEGER"   + ")";



    /* ***************************************************/
    /* ******   EN Coordinates        ********************/
    /* ***************************************************/

    //Coordinate EN column names
    static final String COORDINATE_ID            = "coord_id";
    static final String COORDINATE_PROJECT_ID    = "coord_project_id";
    static final String COORDINATE_POINT_ID      = "coord_point_id";
    static final String COORDINATE_TYPE          = "coord_type";

    static final String COORDINATE_TIME          = "coord_time";

    static final String COORDINATE_ELEVATION     = "coord_elevation";
    static final String COORDINATE_GEOID         = "coord_geoid";
    static final String COORDINATE_CONVERGENCE   = "coord_convergence" ; //
    static final String COORDINATE_SCALE         = "coord_scale";

    static final String COORDINATE_VALID_COORD   = "coord_valid_coord";//BOOLEAN  no-0/1-yes
    static final String COORDINATE_IS_FIXED      = "coord_is_fixed";   //0 = false, 1 = true
    static final String COORDINATE_DATUM         = "coord_datum";       //eg WGS84

    static final String COORDINATE_EN_EASTING    = "coord_en_easting";
    static final String COORDINATE_EN_NORTHING   = "coord_en_northing";
    static final String COORDINATE_EN_ZONE       = "coord_en_zone";        //1-60

    static final String COORDINATE_UTM_HEMISPHERE = "coord_utm_hemisphere";  //N or S
    static final String COORDINATE_UTM_LATBAND    = "coord_utm_latband";

    static final String COORDINATE_SPC_STATE      = "coord_stc_state";


    //create  table
    private static final String CREATE_TABLE_COORDINATE_EN = "CREATE TABLE " +
            TABLE_COORDINATE_EN             + "("            +
            KEY_ID                          + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COORDINATE_ID                   + " INTEGER, "   +
            COORDINATE_PROJECT_ID           + " INTEGER, "   +
            COORDINATE_POINT_ID             + " INTEGER, "   +
            COORDINATE_TYPE                 + " INTEGER, "   +

            COORDINATE_TIME                 + " REAL, "      +

            COORDINATE_ELEVATION            + " REAL, "      +
            COORDINATE_GEOID                + " REAL, "      +
            COORDINATE_CONVERGENCE          + " REAL, "      +
            COORDINATE_SCALE                + " REAL, "      +

            COORDINATE_VALID_COORD          + " INTEGER, "   +
            COORDINATE_IS_FIXED             + " INTEGER, "   + //0 = FALSE, 1 = TRUE
            COORDINATE_DATUM                + " TEXT, "      +

            COORDINATE_EN_EASTING           + " REAL, "      +
            COORDINATE_EN_NORTHING          + " REAL, "      +
            COORDINATE_EN_ZONE              + " INTEGER, "   +
            COORDINATE_UTM_HEMISPHERE       + " TEXT, "      +
            COORDINATE_UTM_LATBAND          + " TEXT, "      +
            COORDINATE_SPC_STATE            + " TEXT, "      +
             KEY_CREATED_AT                 + " DATETIME"  + ")";



    /* ***************************************************/
    /* ******    LL Coordinates        *******************/
    /* ***************************************************/

    //Coordinate LL column names

    //static final String COORDINATE_ID                 = "coord_id";
    //static final String COORDINATE_PROJECT_ID         = "coord_project_id";
    //static final String COORDINATE_POINT_ID           = "coord_point_id";
    //static final String COORDINATE_TYPE               = "coord_type";

    //static final String COORDINATE_ELEVATION          = "coord_elevation";
    //static final String COORDINATE_CONVERGENCE        = "coord_convergence" ; //
    //static final String COORDINATE_SCALE              = "coord_scale";

    //static final String COORDINATE_VALID_COORD        = "coord_valid_coord";//BOOLEAN  no-0/1-yes
    //static final String COORDINATE_IS_FIXED           = "coord_is_fixed";   //0=false, 1=true
    //static final String COORDINATE_DATUM              = "coord_datum";       //eg WGS84

    static final String COORDINATE_LL_LATITUDE          = "coord_ll_latitude";
    //static final String COORDINATE_LL_LATITUDE_DEGREE   = "coord_ll_latitude_degree";
    //static final String COORDINATE_LL_LATITUDE_MINUTE   = "coord_ll_latitude_minute";
    //static final String COORDINATE_LL_LATITUDE_SECOND   = "coord_ll_latitude_second";
    static final String COORDINATE_LL_LONGITUDE         = "coord_ll_longitude";
    //static final String COORDINATE_LL_LONGITUDE_DEGREE  = "coord_ll_longitude_degree";
    //static final String COORDINATE_LL_LONGITUDE_MINUTE  = "coord_ll_longitude_minute";
    //static final String COORDINATE_LL_LONGITUDE_SECOND  = "coord_ll_longitude_second";


    //create  table
    private static final String CREATE_TABLE_COORDINATE_LL = "CREATE TABLE " +
            TABLE_COORDINATE_LL             + "("          +
            KEY_ID                          + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COORDINATE_ID                   + " INTEGER, " +
            COORDINATE_PROJECT_ID           + " INTEGER, " +
            COORDINATE_POINT_ID             + " INTEGER, " +
            COORDINATE_TYPE                 + " INTEGER, " +

            COORDINATE_TIME                 + " REAL, "    +

            COORDINATE_ELEVATION            + " REAL, "    +
            COORDINATE_GEOID                + " REAL, "    +
            COORDINATE_CONVERGENCE          + " REAL, "    +
            COORDINATE_SCALE                + " REAL, "    +

            COORDINATE_VALID_COORD          + " INTEGER, " +
            COORDINATE_IS_FIXED             + " INTEGER, " +//0 = FALSE, 1 = TRUE

             COORDINATE_LL_LATITUDE          + " REAL, "    +
     /*
            COORDINATE_LL_LATITUDE_DEGREE   + " INTEGER, " +
            COORDINATE_LL_LATITUDE_MINUTE   + " INTEGER, " +
            COORDINATE_LL_LATITUDE_SECOND   + " REAL, "    +
    */
            COORDINATE_LL_LONGITUDE         + " REAL, "    +
    /*
            COORDINATE_LL_LONGITUDE_DEGREE  + " INTEGER, " +
            COORDINATE_LL_LONGITUDE_MINUTE  + " INTEGER, " +
            COORDINATE_LL_LONGITUDE_SECOND  + " REAL, "    +
    */
            KEY_CREATED_AT                  + " DATETIME " + ")";


    /* ***************************************************/
    /* ******   Mean Coordinates       *******************/
    /* ***************************************************/

    //column names
    static final String COORDINATE_MEAN_ID            = "coord_mean_id";
    static final String COORDINATE_MEAN_PROJECT_ID    = "coord_mean_project_id";
    static final String COORDINATE_MEAN_POINT_ID      = "coord_mean_point_id";
    static final String COORDINATE_MEAN_TYPE          = "coord_mean_type";
    static final String COORDINATE_MEAN_VALID_COORD   = "coord_mean_valid_coord";//BOOLEAN  no-0/1-yes
    static final String COORDINATE_MEAN_IS_FIXED      = "coord_mean_is_fixed";//0=false, 1=true
    static final String COORDINATE_MEAN_RAW           = "coord_mean_mean_easting";
    static final String COORDINATE_MEAN_MEANED        = "coord_mean_mean_northing";
    static final String COORDINATE_MEAN_FIXED         = "coord_mean_mean_elevation";
    static final String COORDINATE_MEAN_LATITUDE            = "coord_mean_mean_lat";
    static final String COORDINATE_MEAN_LATITUDE_STD        = "coord_mean_mean_lat_std";
    //static final String COORDINATE_MEAN_LATITUDE_DEGREE   = "coord_mean_mean_lat_degree";
    //static final String COORDINATE_MEAN_LATITUDE_MINUTE   = "coord_mean_mean_lat_minute";
    //static final String COORDINATE_MEAN_LATITUDE_SECOND   = "coord_mean_mean_lat_second";
    static final String COORDINATE_MEAN_LONGITUDE           = "coord_mean_mean_lng";
    static final String COORDINATE_MEAN_LONGITUDE_STD       = "coord_mean_mean_lng_std";
    //static final String COORDINATE_MEAN_LONGITUDE_DEGREE  = "coord_mean_mean_lng_degree";
    //static final String COORDINATE_MEAN_LONGITUDE_MINUTE  = "coord_mean_mean_lng_minute";
    //static final String COORDINATE_MEAN_LONGITUDE_SECOND  = "coord_mean_mean_lng_second";
    static final String COORDINATE_MEAN_ELEVATION           = "coord_mean_mean_elev" ;
    static final String COORDINATE_MEAN_ELEVATION_STD       = "coord_mean_mean_elev_std" ;
    static final String COORDINATE_MEAN_GEOID               = "coord_mean_mean_geoid";
    static final String COORDINATE_MEAN_SATELLITES          = "coord_mean_mean_sat";



    //create  table
    private static final String CREATE_TABLE_COORDINATE_MEAN = "CREATE TABLE " +
            TABLE_COORDINATE_MEAN                + "("            +
            KEY_ID                               + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COORDINATE_MEAN_ID                   + " INTEGER, "   +
            COORDINATE_MEAN_PROJECT_ID           + " INTEGER, "   +
            COORDINATE_MEAN_POINT_ID             + " INTEGER, "   +
            COORDINATE_MEAN_TYPE                 + " INTEGER, "   +
            COORDINATE_MEAN_VALID_COORD          + " INTEGER, "   +
            COORDINATE_MEAN_IS_FIXED             + " INTEGER, "   +
            COORDINATE_MEAN_RAW                  + " INTEGER, "   +
            COORDINATE_MEAN_MEANED               + " INTEGER, "   +
            COORDINATE_MEAN_FIXED                + " INTEGER, "   +
            COORDINATE_MEAN_LATITUDE             + " REAL, "      +
            COORDINATE_MEAN_LATITUDE_STD         + " REAL, "      +
            COORDINATE_MEAN_LONGITUDE            + " REAL, "      +
            COORDINATE_MEAN_LONGITUDE_STD        + " REAL, "      +
            COORDINATE_MEAN_ELEVATION            + " REAL, "      +
            COORDINATE_MEAN_ELEVATION_STD        + " REAL, "      +
            COORDINATE_MEAN_GEOID                + " REAL, "      +
            COORDINATE_MEAN_SATELLITES           + " INTEGER, "   +
            KEY_CREATED_AT                       + " DATETIME"  + ")";



    /* ***************************************************/
    /* ******      Mean Token              ***************/
    /* ***************************************************/

    //column names
    static final String MEAN_TOKEN_ID           = "mean_token_id";
    static final String MEAN_TOKEN_PROJECT_ID   = "mean_token_project_id";
    static final String MEAN_TOKEN_IN_PROGRESS  = "mean_token_progress";
    static final String MEAN_TOKEN_FIRST        = "mean_token_first";//0 = FALSE, 1 = TRUE
    static final String MEAN_TOKEN_LAST         = "mean_token_last"; //0 = FALSE, 1 = TRUE
    static final String MEAN_TOKEN_START        = "mean_token_start";
    static final String MEAN_TOKEN_END          = "mean_token_end";
    static final String MEAN_TOKEN_CURRENT      = "mean_token_current";
    static final String MEAN_TOKEN_FIXED        = "mean_token_fixed";
    static final String MEAN_TOKEN_RAW          = "mean_token_raw";
    static final String MEAN_TOKEN_MEAN_COORD_ID = "mean_token_mean_coord_id";





    //create  table
    private static final String CREATE_TABLE_MEAN_TOKEN = "CREATE TABLE " +
            TABLE_MEAN_TOKEN         + "("            +
            KEY_ID                   + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            MEAN_TOKEN_ID            + " INTEGER, "   +
            MEAN_TOKEN_PROJECT_ID    + " INTEGER, "   +
            MEAN_TOKEN_IN_PROGRESS   + " INTEGER, "   +
            MEAN_TOKEN_FIRST         + " INTEGER, "   + //0 = FALSE, 1 = TRUE
            MEAN_TOKEN_LAST          + " INTEGER, "   + //0 = FALSE, 1 = TRUE
            MEAN_TOKEN_START         + " INTEGER, "   +
            MEAN_TOKEN_END           + " INTEGER, "   +
            MEAN_TOKEN_CURRENT       + " INTEGER, "   +
            MEAN_TOKEN_FIXED         + " INTEGER, "   +
            MEAN_TOKEN_RAW           + " INTEGER, "   +
            MEAN_TOKEN_MEAN_COORD_ID + " INTEGER, "   +

            KEY_CREATED_AT                        + " DATETIME"  + ")";



    /* ***************************************************/
    /* ******      Mean Token Readings     ***************/
    /* ***************************************************/

    //column names
    static final String MEAN_TOKEN_READING_ID            = "coord_mean_read_id";
    static final String MEAN_TOKEN_READING_MEAN_ID       = "coord_mean_read_mean_id";
    static final String MEAN_TOKEN_READING_COORDINATE_ID = "coord_mean_read_coord_id";




    //create  table
    private static final String CREATE_TABLE_MEAN_TOKEN_READINGS = "CREATE TABLE " +
            TABLE_MEAN_TOKEN_READINGS        + "("            +
            KEY_ID                           + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            MEAN_TOKEN_READING_ID            + " INTEGER, "   +
            MEAN_TOKEN_READING_MEAN_ID       + " INTEGER, "   +
            MEAN_TOKEN_READING_COORDINATE_ID + " INTEGER, "   +
            KEY_CREATED_AT                   + " DATETIME"  + ")";




    /* ***************************************************/
    /* ********** Picture File   *************************/
    /* ***************************************************/

    // Column Names
    static final String PICTURE_ID =         "pict_id";
    static final String PICTURE_PROJECT_ID = "pict_project_id";
    static final String PICTURE_POINT_ID =   "pict_point_id";
    static final String PICTURE_PATH_NAME =  "pict_path_name";
    static final String PICTURE_FILE_NAME =  "pict_file_name";


    //create  table
    //NOTE: Dates are stored as long, NOT as a Date.
    //      The conversion is done when CV is created and when Cursor is translated
    private static final String CREATE_TABLE_PICTURE = "CREATE TABLE " +
            TABLE_PICTURE           + "("            +
            KEY_ID                  + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            PICTURE_ID              + " TEXT, "      +
            PICTURE_PROJECT_ID      + " INTEGER, "   +
            PICTURE_POINT_ID        + " INTEGER, "   +
            PICTURE_PATH_NAME       + " TEXT, "      +
            PICTURE_FILE_NAME       + " TEXT, "      +
            KEY_CREATED_AT          + " DATETIME"    + ")";


    /* ***************************************************/
    /* ***************************************************/
    /* ***************************************************/





    /* ***************************************************/
    /* ***************************************************/
    /* ***************************************************/





    /* ***************************************************/
    /* *****  Member Variables          ******************/
    /* ***************************************************/

    private Context mContext;


    /* ***************************************************/
    /* *****  Constructor               ******************/
    /* ***************************************************/

    //This should be called with the APPLICATION context
    GBDatabaseSqliteHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

        this.mContext = context;
    }


    /* ***************************************************/
    /* *****  Lifecycle Methods         ******************/
    /* ***************************************************/

    /* ****************
     * onCreate()
     * when the helper constructor is executed with a name (2nd param),
     * the platform checks if the database (second parameter) exists or not and
     * if the database exists, it gets the version information from the database file header and
     * triggers the right call back (e.g. onUpdate())
     * if the database with the name doesn't exist, the platform triggers onCreate().
     *
     * @param db The Database instance being created
     */
    @Override
    public void onCreate(SQLiteDatabase db){

        //create the tables using the pre-defined SQL
        db.execSQL(CREATE_TABLE_PROJECT);
        db.execSQL(CREATE_TABLE_PROJECT_SETTINGS);
        db.execSQL(CREATE_TABLE_POINT);

        db.execSQL(CREATE_TABLE_COORDINATE_LL);
        db.execSQL(CREATE_TABLE_COORDINATE_EN);
        db.execSQL(CREATE_TABLE_COORDINATE_MEAN);
        db.execSQL(CREATE_TABLE_MEAN_TOKEN);
        db.execSQL(CREATE_TABLE_MEAN_TOKEN_READINGS);

        db.execSQL(CREATE_TABLE_PICTURE);

    }

    /* ****************
     * This default version of the onUpgrade() method just
     * deletes any data in the database file, and recreates the
     * database from scratch.
     *
     * Obviously, in the production version, this method will have
     * to migrate data in the old version table layout
     * to the new version table layout.
     * Renaming tables,
     * creating new tables,
     * writing data from renamed table to the new table,
     * then dropping the renamed table.
     * And doing this in a cascading fashion so the tables can
     * be brought up to date over several versions.
     * @param db         The instance of the db to be upgraded
     * @param OldVersion The old version number
     * @param newVersion The new version number
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int OldVersion, int newVersion){
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PROJECT);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PROJECT_SETTINGS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_POINT);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_COORDINATE_EN);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_COORDINATE_LL);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_COORDINATE_MEAN);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MEAN_TOKEN);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MEAN_TOKEN_READINGS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PICTURE);



        //Create new tables
        onCreate(db);
    }

    @Override
    public void onOpen(SQLiteDatabase db){
        super.onOpen(db);
    }



    /* **********************************************/
    /*         Generic CRUD routines                */
    /* The same routine will do for all data types  */
    /* **********************************************/

    //* ************************** CREATE *************************************

    long add( SQLiteDatabase db,
             String         table,
             ContentValues  values,          //Column names and new values
             String         where_clause,
             String         id_key){

        long returnCode = 0;
        long returnKey  = 0;


        long id_value = (long) values.get(id_key);
        if (id_value == GBUtilities.ID_DOES_NOT_EXIST){
            //Add it to the DB
            //need to insert
            returnCode = db.insert(table, null, values);
            if (returnCode == sDB_ERROR_CODE)return sDB_ERROR_CODE;

            //get ready to pass back the new ID
            returnKey = returnCode;
            //get ready to update the DB row with the new ID
            values.put(id_key, returnKey);

            returnCode = db.update(table, values, where_clause, null);

        } else {
            //Update the existing DB row

            //get ready to pass back the instance ID
            returnKey = (long) values.get(id_key);

            //update the row in the DB
            returnCode = db.update(table, values, where_clause, null);
        }

        //db.close(); //never close the db instance. Just leave the connection open

        if (returnCode == GBDatabaseManager.sDB_ERROR_CODE)return GBDatabaseManager.sDB_ERROR_CODE;

        //return the instance/row ID
        return returnKey;
    }


    //A picture has a String ID, so it is unique among all other GeoBot data objects.
    // It's ID is not assigned by addition to the DB
    //The returnCode is the number of rows affected
    long addPicture(SQLiteDatabase db,
                             ContentValues  values,          //Column names and new values
                             String         where_clause){

        long returnCode = 0;

        //Attempt to add, if that doesn't work, try update
        returnCode = db.insert(TABLE_PICTURE, null, values);
        if (returnCode == sDB_ERROR_CODE){

            returnCode = db.update(TABLE_PICTURE, values, where_clause, null);

        }

        //db.close(); //never close the db instance. Just leave the connection open

        if (returnCode == GBDatabaseManager.sDB_ERROR_CODE)return GBDatabaseManager.sDB_ERROR_CODE;

        //return the instance/row ID
        return returnCode;
    }

    //* ************************** READ *******************************
    Cursor getObject(SQLiteDatabase db,
                            String   table,
                            String[] columns,
                            String   where_clause,
                            String[] selectionArgs,
                            String   groupBy,
                            String   having,
                            String   orderBy){
        /* *******************************
         Cursor query (String table, //Table Name
                         String[] columns,   //Columns to return, null for all columns
                         String where_clause,
                         String[] selectionArgs, //replaces ? in the where_clause with these arguments
                         String groupBy, //null meanas no grouping
                         String having,   //row grouping
                         String orderBy)  //null means the default sort order
         *********************************/
        return (db.query(table, columns, where_clause, selectionArgs, groupBy, having, orderBy));
    }

    //* ****************************************UPDATE***************************
    //use add, it attempts an insert. If that fails, it tries an update



    //* ***************************************DELETE*********************************
    //returns the number of rows affected
    int remove (SQLiteDatabase  db,
                       String         table,
                       String         where_clause,//null updates all rows
                       String[]       where_args ){ //values that replace ? in where clause

        //Actually, this is just a pass through to the delete method
        return (delete(db, table, where_clause, where_args));

    }


    //returns the number of rows affected
    int delete (SQLiteDatabase  db,
                       String         table,
                       String         where_clause,//null updates all rows
                       String[]       where_args ){ //values that replace ? in where clause

        return (db.delete(table, where_clause, where_args));
    }


    /* **********************************************/
    /*      Object Specific CRUD routines           */
    /*     Each Class has it's own routine          */
    /*     in it's manager                          */
    /* **********************************************/

}
