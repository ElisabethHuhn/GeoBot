package com.asc.msigeosystems.geobot;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Elisabeth Huhn on 6/13/2016.
 *
 * This is a holder for constants and utilities that are not data type specific
 * enough to live in another class
 */
public class GBUtilities {

    //***********************************/
    //******** Static Constants *********/
    //***********************************/

    //Use this before an object gets saved to the DB
    public static final long ID_DOES_NOT_EXIST = -1;


    static final boolean BUTTON_DISABLE = false;
    static final boolean BUTTON_ENABLE  = true;

    static final int sHundredthsDigitsOfPrecision = 2;
    static final int sMicrometerDigitsOfPrecision = 6;
    static final int sNanometerDigitsOfPrecision = 9;
    static final int sPicometerDigitsOfPrecision = 12;


    //************************************************************************************

    // TODO: 7/1/2017 These constants need to be moved to the GBCoordConstants object
    static final double sEquatorialRadiusA = 6378137.0; //equatorial radius in meters
    static final double sSemiMajorRadius   = sEquatorialRadiusA;
    static final double sPolarRadiusB      = 6356752.314245; //polar semi axis
    static final double sSemiMinorRadius   = sPolarRadiusB;


    //flatteningF = (equatorialRadiusA-polarRadiusB)/equatorialRadiusA;
    static final double sFlattening        = 0.0033528106811837;

    static final double sGravitationalConstant = 3.986004418e14; // cubic meters/square seconds
    static final double sMeanAngularVelocity   = 7.292115e-5;    // rads / s


    //used in calculating Moments of Inertia, based on EGM2008
    static final double sDynSecDegZonal     = -4.84165143790815e-4; //
    static final double sSectorialHarmonics = 2.43938357328313e-6; //
    

    //Constants for transforming WGS84 to NAD83
    static final double sTx19972011 = 0.99343; // meters
    static final double sTx1997PS11 = 0.9080;
    static final double sTx1997MA11 = 0.9080;
    
    static final double sTy19972011 = -1.90331; //meters
    static final double sTy1997PA11 = -2.0161;
    static final double sTy1997MA11 = -2.0161;

    static final double sTz19972011 = -0.52655; //meters
    static final double sTz1997PA11 = -0.5653;
    static final double sTz1997MA11 = -0.5653;

    static final double sWx19972011 = 125.63787; //nanoradians
    static final double sWx1997PA11 = 134.49216;
    static final double sWx1997MA11 = 140.45537;

    static final double sWy19972011 = 45.70072; //nanoradians
    static final double sWy1997PA11 = 65.29956;
    static final double sWy1997MA11 = 50.51759;

    static final double sWz19972011 = 56.23524; //nanoradians
    static final double sWz1997PA11 = 13.14815;
    static final double sWz1997MA11 = 43.28416;
    
    static final double sS19972011  = 1.71504; //parts per billion
    static final double sS1997PA11  = 1.10;
    static final double sS1997MA11  = 1.10;

    static final double sdTx19972011 = 0.00079; // meters/yr
    static final double sdTx1997PS11 = 0.0001;
    static final double sdTx1997MA11 = 0.0001;

    static final double sdTy19972011 = -0.00060; //meters/yr
    static final double sdTy1997PA11 = 0.0001;
    static final double sdTy1997MA11 = 0.0001;

    static final double sdTz19972011 = -0.00134; //meters/yr
    static final double sdTz1997PA11 = -0.0018;
    static final double sdTz1997MA11 = -0.0018;

    static final double sdWx19972011 = 0.32322; //nanoradians/yr
    static final double sdWx1997PA11 = -1.86168;
    static final double sdWx1997MA11 = -0.09696;

    static final double sdWy19972011 = -3.67217; //nanoradians/yr
    static final double sdWy1997PA11 = 4.88207;
    static final double sdWy1997MA11 = 0.50905;

    static final double sdWz19972011 = -0.24886; //nanoradians/yr
    static final double sdWz1997PA11 = -10.59803;
    static final double sdWz1997MA11 = -1.68230;

    static final double sdS19972011  = -0.10201; //parts per billion/yr
    static final double sdS1997PA11  = 0.08;
    static final double sdS1997MA11  = 0.08;


    //Constants used in conversion from NAD83 to State Plane Coordinates
    static final double E0     = 2000000.0000; //meters Easting of projection and grid origion
    static final double E0feet = 6561666.667; //E0 in feet
    static final double Nb     = 500000.0000;  //meters northing of the grid base
    static final double Nbfeet = 1640416.667; //feet


    //********************************************************************************

    // The U.S. Metric Law of 1866 provided the relationship that
    // one meter is equal to 39.37 inches, exactly

    //one yard equal to 0.9144 meters, exactly.
    // From that,  one foot is equal to one-third of that constant, or 0.3048 meters.
    // This is also equivalent to 2.54 centimeters equal to 1 inch,

    //3.28083989501 International Feet are equal to one meter

    static final double feetPerMeter   = 3.280833333;
    static final double inchesPerMeter = feetPerMeter*12.;   //39.37
    static final double cmPerInch      = 100. / (feetPerMeter * 12.); //2.54

    static final double ifeetPerMeter  = 3.28083989501;



    //***********************************/
    //******** Static Variables *********/
    //***********************************/
    private static GBUtilities ourInstance ;


    //***********************************/
    //******** Member Variables *********/
    //***********************************/



    //***********************************/
    //******** Static Methods   *********/
    //***********************************/
    static GBUtilities getInstance() {
        if (ourInstance == null){
            ourInstance = new GBUtilities();

        }
        return ourInstance;
    }



    static double convertMetersToFeet(double meters) {
        //function converts Feet to Meters.

        return meters * GBUtilities.feetPerMeter;
    }

    static double convertMetersToIFeet(double meters) {
        //function converts Meters to Ifeet.
        return meters * GBUtilities.ifeetPerMeter;
    }

    static double convertFeetToMeters(double feet){
        return feet / GBUtilities.feetPerMeter;
    }

    static double convertIFeetMeters(double iFeet) {
        //function converts IFeet to Meters.
        return iFeet / GBUtilities.ifeetPerMeter;
    }

    static boolean convertMetersToFeet(Context context,
                                      EditText metersWidget,
                                      EditText feetWidget){
        double meters, feet;
        String meterString; //, feetString;

        try {
            //The inputs are limited to digital numbers by the xml, so don't need to check
            meterString = metersWidget.getText().toString();
            meters = Double.parseDouble(meterString);
        } catch  (NumberFormatException e) {
            return false;
        }

        int locPrecision = GBGeneralSettings.getLocPrecision((GBActivity)context);
        feet = meters * GBUtilities.feetPerMeter;
        feetWidget.setText(truncatePrecisionString(feet, locPrecision));

        if (meters < 0){
            metersWidget.setTextColor(ContextCompat.getColor(context, R.color.colorNegNumber));
            feetWidget  .setTextColor(ContextCompat.getColor(context, R.color.colorNegNumber));
        } else {
            metersWidget.setTextColor(ContextCompat.getColor(context, R.color.colorPosNumber));
            feetWidget  .setTextColor(ContextCompat.getColor(context, R.color.colorPosNumber));
        }

        return true;

    }

    static boolean convertFeetToMeters(Context context,
                                      EditText metersWidget,
                                      EditText feetWidget){
        double meters, feet;
        String feetString; //meterString too if needed

        try {
            //The inputs are limited to digital numbers by the xml, so don't need to check
            feetString = feetWidget.getText().toString();
            feet = Double.parseDouble(feetString);

        } catch  (NumberFormatException e) {
            return false;
        }

        meters = feet  / GBUtilities.feetPerMeter;
        int locPrecision = GBGeneralSettings.getLocPrecision((GBActivity)context);
        metersWidget.setText(truncatePrecisionString(meters, locPrecision));


        if (feet < 0){
            metersWidget.setTextColor(ContextCompat.getColor(context, R.color.colorNegNumber));
            feetWidget  .setTextColor(ContextCompat.getColor(context, R.color.colorNegNumber));
        } else {
            metersWidget.setTextColor(ContextCompat.getColor(context, R.color.colorPosNumber));
            feetWidget  .setTextColor(ContextCompat.getColor(context, R.color.colorPosNumber));
        }

        return true;
    }



    //this tells you how many screen inches are in a meter displayed on the map
    static double getMetersPerScreenInch(double latitude, float zoomLevel){
        return (160. * getMetersPerPixel(latitude, zoomLevel));
    }



    //This is meters per dp, NOT physical pixel
    //There are 160 dp in an inch, so 160 dpPerInch
    //         mapMeters per screenInch = getMetersPerPixel() X PixelsPerInch
    private static double getMetersPerPixel(double latitude, float zoomLevel){
        return (getCircumferenceInMetersAtLatitude(latitude) /
                getCircumferenceInPixelsAtLatitude(latitude, zoomLevel));
        /*
        double metersPerPixelZoomZero  = 156543.03392; //by definition
        double metersPerPixel = metersPerPixelZoomZero *
                                Math.cos(latitude * Math.PI / 180) / Math.pow(2, zoomLevel);

        return metersPerPixel;
        */
    }


    private static double getCircumferenceInMetersAtLatitude(double latitude){
        double radius = getRadiusInMetersAtLatitude(latitude);
        return 2 * Math.PI * radius;
    }


    private static double getRadiusInMetersAtLatitude(double latitude) {
        //latitude in radians
        double latRad = latitude * Math.PI / 180.;
        double equRSqr = sEquatorialRadiusA * sEquatorialRadiusA;//in meters
        double polRSqr = sPolarRadiusB * sPolarRadiusB;
        double cosLat = Math.cos(latRad);

        double numerator = ((equRSqr * cosLat) * (equRSqr * cosLat)) +
                           ((polRSqr * cosLat) * (polRSqr * cosLat));
        double denominator = ((sEquatorialRadiusA * cosLat) * (sEquatorialRadiusA * cosLat)) +
                             ((sPolarRadiusB      * cosLat) * (sPolarRadiusB      * cosLat));

        return Math.sqrt(numerator / denominator);
    }

    private static double getCircumferenceInPixelsAtLatitude(double latitude, float zoomLevel){

        //at zoom level N, the circumference at the equator is approximately 256 * (2 to the N) dp
        double nPlus8 = (double)zoomLevel + 8.;
        double pixelsAtEquator = Math.pow(2, nPlus8);

        //Unit at Latitude = (Cosine of Latitude in Radians) X (Unit at Equator)
        double latRad = latitude * Math.PI / 180.;
        return Math.cos(latRad) * pixelsAtEquator;
    }



    static double getFeetPerPixel(double latitude, float zoomLevel){
        return convertMetersToFeet(getMetersPerPixel(latitude, zoomLevel));
    }



    private static int getPixelDensityHeight(Context context){
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        return dm.heightPixels;

    }
    private static int getPixelDensityWidth(Context context){
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        return  dm.widthPixels;
    }


    static void soundMeanComplete(GBActivity activity){
        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(activity.getApplicationContext(), notification);
            r.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static boolean isEmpty(CharSequence str) {
        return (str == null || str.length() == 0);
    }



    static String truncatePrecisionString(GBActivity activity, int precisionType, double inputValue){
        GBProject openProject = GBUtilities.getInstance().getOpenProject(activity);

        //Default is the precision for locations
        int digitsOfPrecision = GBGeneralSettings.getLocPrecision(activity);
        if (precisionType == GBGeneralSettings.sCAPrc){
            digitsOfPrecision = GBGeneralSettings.getCAPrecision(activity);

        } else if (precisionType == GBGeneralSettings.sSfPrc){
            digitsOfPrecision = GBGeneralSettings.getSfPrecision(activity);

        } else if (precisionType == GBGeneralSettings.sStdPrc){
            digitsOfPrecision = GBGeneralSettings.getStdDevPrecision(activity);
        }

        if (digitsOfPrecision == 0)digitsOfPrecision = GBUtilities.sMicrometerDigitsOfPrecision;

        return truncatePrecisionString(inputValue, digitsOfPrecision);
    }

    static String truncatePrecisionString(double inputValue, int digitsOfPrecision){
        String form = "%."+digitsOfPrecision+"f\n";
        return String.format(form, inputValue);
    }

    static String getDistanceString(GBActivity activity, int digitsOfPrecision, double distanceValue){
        GBProject openProject = GBUtilities.getInstance().getOpenProject(activity);
        int distUnits = openProject.getDistanceUnits();

        double distanceMeters = distanceValue;
        if (distUnits == GBProject.sFeet){
            distanceMeters = GBUtilities.convertFeetToMeters(distanceValue);
        }
        if (distUnits == GBProject.sIntFeet){
            distanceMeters = GBUtilities.convertIFeetMeters(distanceValue);
        }

        return GBUtilities.truncatePrecisionString(distanceMeters, digitsOfPrecision);
    }



    static double getDecimalDegrees(int degrees, int minutes, double seconds){
        double dd = degrees + ((minutes + (seconds / 60.))/60.);
        return dd;
    }

    static double getSeconds (double decimalDegrees){
        int degrees = (int)decimalDegrees;
        double remainderMin = ((decimalDegrees - degrees) * 60.);
        int minutes = (int) (remainderMin);
        return (remainderMin - minutes) * 60.; //seconds
    }

    static int getMinutes (double decimalDegrees){
        int degrees = (int)decimalDegrees;
        double remainderMin = ((decimalDegrees - degrees) * 60.);
        return (int) (remainderMin);//minutes
    }

    static int getDegrees (double decimalDegrees){
        return (int)decimalDegrees;
    }


    private String convertToFormat(double value){
        DecimalFormat df = new DecimalFormat("#.##");

        return df.format(value);
    }



    static String getDateTimeString(long milliSeconds){
        Date date = new Date(milliSeconds);
        return DateFormat.getDateTimeInstance().format(date);
    }

    static long getDateTimeFromString(Context activity, String timeString){
        Date date;
        try {
            SimpleDateFormat format = getDateTimeFormat();
            date = format.parse(timeString);
        } catch ( ParseException e){
            GBUtilities.getInstance().errorHandler(activity, e.getMessage());
            return 0;
        }
        return date.getTime();
    }

    private static SimpleDateFormat getDateTimeFormat(){
        return new SimpleDateFormat("MMM d, yyyy hh:mm:ss", Locale.getDefault());
    }


    //***********************************/
    //****   Location UI conversions ****/
    //***********************************/

    static void locDD(GBActivity activity, double tude, int locDigOfPrec,
                      boolean isDir, int posHemi, int negHemi,
                      TextView dirView, TextView tudeView){


        int tudeColor= R.color.colorPosNumber;
        if (isDir){

            if (tude < 0.){
                dirView.setText(activity.getString(negHemi));
                tude = Math.abs(tude);
            } else {
                dirView.setText(activity.getString(posHemi));
            }
            dirView.setVisibility(View.VISIBLE);
        } else {
            dirView.setVisibility(View.GONE);
            if (tude < 0.) {
                tudeColor = R.color.colorNegNumber;
            }
        }


        String tudeString = GBUtilities.truncatePrecisionString(tude, locDigOfPrec);
        tudeView.setText(tudeString);
        tudeView.setBackgroundColor(ContextCompat.getColor(activity, R.color.colorWhite));
        tudeView .setTextColor(ContextCompat.getColor(activity, tudeColor));
    }

    static void locDMS(GBActivity activity,
                       int tudeDeg, int tudeMin, double tudeSec, int locDigOfPrec,
                       boolean isDir, int posHemi, int negHemi, TextView dirView,
                       TextView tudeDegView, TextView tudeMinView, TextView tudeSecView){
        int tudeColor = R.color.colorPosNumber;

        if (isDir){

            if ((tudeDeg < 0) || (tudeMin < 0) || (tudeSec < 0.)){
                dirView.setText(activity.getString(negHemi));
                tudeDeg = Math.abs(tudeDeg);
                tudeMin = Math.abs(tudeMin);
                tudeSec = Math.abs(tudeSec);
            } else {
                dirView.setText(activity.getString(posHemi));
            }
        } else if ((tudeDeg < 0) || (tudeMin < 0) || (tudeSec < 0.)){
            tudeColor = R.color.colorNegNumber;
        }

        tudeDegView.setText(String.valueOf(tudeDeg));
        tudeDegView.setBackgroundColor(ContextCompat.getColor(activity, R.color.colorWhite));
        tudeDegView .setTextColor(ContextCompat.getColor(activity, tudeColor));


        tudeMinView.setText(String.valueOf(tudeMin));
        tudeMinView.setBackgroundColor(ContextCompat.getColor(activity, R.color.colorWhite));
        tudeMinView .setTextColor(ContextCompat.getColor(activity, tudeColor));


        String tudeSecString = GBUtilities.truncatePrecisionString(tudeSec, locDigOfPrec);
        tudeSecView.setText(tudeSecString);
        tudeSecView.setBackgroundColor(ContextCompat.getColor(activity, R.color.colorWhite));
        tudeSecView .setTextColor(ContextCompat.getColor(activity, tudeColor));
    }

    static void caDD(GBActivity activity, double ca, int locDigOfPrec, TextView caView){


        int caColor= R.color.colorPosNumber;
        if (ca < 0.){
            caColor = R.color.colorNegNumber;
        }


        String caString = GBUtilities.truncatePrecisionString(ca, locDigOfPrec);
        caView.setText(caString);
        caView.setBackgroundColor(ContextCompat.getColor(activity, R.color.colorWhite));
        caView .setTextColor(ContextCompat.getColor(activity, caColor));
    }

    static void caDMS(GBActivity activity,
                       int caDeg, int caMin, double caSec, int locDigOfPrec,
                       TextView caDegView, TextView caMinView, TextView caSecView){
        int caColor = R.color.colorPosNumber;

        if ((caDeg < 0) || (caMin < 0) || (caSec < 0.)){
            caColor = R.color.colorNegNumber;
        }

        caDegView.setText(String.valueOf(caDeg));
        caDegView.setBackgroundColor(ContextCompat.getColor(activity, R.color.colorWhite));
        caDegView .setTextColor(ContextCompat.getColor(activity, caColor));


        caMinView.setText(String.valueOf(caMin));
        caMinView.setBackgroundColor(ContextCompat.getColor(activity, R.color.colorWhite));
        caMinView .setTextColor(ContextCompat.getColor(activity, caColor));


        String caSecString = GBUtilities.truncatePrecisionString(caSec, locDigOfPrec);
        caSecView.setText(caSecString);
        caSecView.setBackgroundColor(ContextCompat.getColor(activity, R.color.colorWhite));
        caSecView .setTextColor(ContextCompat.getColor(activity, caColor));
    }

    static void locDistance(GBActivity activity, double distance,  TextView distanceView){

        GBProject openProject = GBUtilities.getInstance().getOpenProject(activity);
        int distUnits    = openProject.getDistanceUnits();
        int locDigOfPrecision = GBGeneralSettings.getLocPrecision(activity);

        if (distUnits == GBProject.sFeet){
            distance   = GBUtilities.convertMetersToFeet(distance);
        } else if (distUnits == GBProject.sIntFeet){
            distance   = GBUtilities.convertMetersToIFeet(distance);
        }
        String distanceString   = GBUtilities.truncatePrecisionString(distance, locDigOfPrecision);

        distanceView.setText(distanceString);
    }



    static int getCoordinateTypeFromProject(GBProject openProject){

        if (openProject == null)return GBCoordinate.sUNKWidgets;

        CharSequence coordinateType = openProject.getProjectCoordinateType();

        int returnCode = GBCoordinate.sUNKWidgets;

        if (!GBUtilities.isEmpty(coordinateType)){
            if (       coordinateType.equals(GBCoordinate.sCoordinateTypeWGS84) ||
                    coordinateType.equals(GBCoordinate.sCoordinateTypeNAD83) ){
                returnCode = GBCoordinate.sLLWidgets;
            } else if (coordinateType.equals(GBCoordinate.sCoordinateTypeUTM) ||
                    coordinateType.equals(GBCoordinate.sCoordinateTypeSPCS) ){
                returnCode = GBCoordinate.sNEWidgets;
            }
        }
        return returnCode;
    }


    //***********************************/
    //******** Constructors     *********/
    //***********************************/
    private GBUtilities() {

    }


    //***********************************/
    //******** Setters/Getters  *********/
    //***********************************/


    GBProject getOpenProject  (GBActivity activity) {
        long openProjectID = getOpenProjectID(activity);
        if (openProjectID == GBUtilities.ID_DOES_NOT_EXIST)return null;

        return GBProjectManager.getInstance().getProject(openProjectID);
    }
    void      setOpenProject  (GBActivity activity, GBProject openProject) {
        if (activity == null){
            return;
        }
        long openProjectID;
        if (openProject == null){
            openProjectID = GBUtilities.ID_DOES_NOT_EXIST;
        } else {
            openProjectID = openProject.getProjectID();
        }
        //Store the PersonID for the next time
        SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putLong(GBActivity.sOpenProjectIDTag, openProjectID);
        editor.apply();

    }
    void      closeOpenProject(GBActivity activity) {
        setOpenProject(activity, null);
    }

    long      getOpenProjectID       (GBActivity activity) {
        if (activity == null){
            return GBUtilities.ID_DOES_NOT_EXIST;
        }
        SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
        long defaultValue = GBUtilities.ID_DOES_NOT_EXIST;
        return sharedPref.getLong(GBActivity.sOpenProjectIDTag, defaultValue);
    }
    String    getOpenProjectIDMessage(GBActivity activity){
        if (activity == null){
            return "Programming Error, Activity is null";
        }
        GBProject openProject = getOpenProject(activity);
        if (openProject != null){
            //A project is open
            return activity.getString(R.string.project_opened) + " " +
                    String.valueOf(openProject.getProjectID())  + " " +
                    openProject.getProjectName()                ;
        } else {
            return activity.getString(R.string.no_project_open);
        }

    }




    //***********************************/
    //******** Member Methods   *********/
    //***********************************/

    //Just a stub for now, but figure out what to do
     void errorHandler(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

     void errorHandler(Context context, int messageResource) {
        errorHandler(context, context.getString(messageResource));
    }

     void showStatus(Context context, String message){
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

     void showStatus(Context context, int messageResource){
        showStatus(context, context.getString(messageResource));
    }


    //***********************************/
    //******** Member Methods   *********/
    //***********************************/

    int getOrientation(Context activity){
        int orientation = Configuration.ORIENTATION_PORTRAIT;
        if (activity.getResources().getDisplayMetrics().widthPixels >
            activity.getResources().getDisplayMetrics().heightPixels) {

            orientation = Configuration.ORIENTATION_LANDSCAPE;
        }
        return orientation;
    }


    //************************************/
    /*         Send with Intents         */
    //************************************/

    void exportEmail(Context context, String subject, String emailAddr, String body, String chooser_title){

        Intent intent2 = new Intent();
        intent2.setAction(Intent.ACTION_SEND);
        intent2.setType("message/rfc822");
        intent2.putExtra(Intent.EXTRA_EMAIL,   emailAddr);
        intent2.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent2.putExtra(Intent.EXTRA_TEXT,    chooser_title );
        context.startActivity(intent2);
    }

    void exportText(Context context,String subject,String body, String chooser_title){
        Intent exportIntent = new Intent(Intent.ACTION_SEND);
        exportIntent.setType("text/plain");

        exportIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);
        exportIntent.putExtra(android.content.Intent.EXTRA_TEXT, body);

        //always display the chooser
        if (exportIntent.resolveActivity(context.getPackageManager()) != null)
            context.startActivity(Intent.createChooser(exportIntent, chooser_title ));
        else {
            GBUtilities.getInstance().showStatus(context, R.string.export_no_app);
        }
    }

    void exportSMS(Context context, String subject, String body){
        Intent sendIntent = new Intent(Intent.ACTION_VIEW);
        sendIntent.putExtra("sms_body", body);
        sendIntent.setType("vnd.android-dir/mms-sms");
        context.startActivity(sendIntent);
    }

    //************************************/
    /*         Send Email using Intent   */
    //************************************/
    void sendEmail(Context context, String subject, String toAddress, String msg) {

        String[] TO = {toAddress};     //{"someone@gmail.com"};
        //String[] CC = {"elisabethhuhn@gmail.com"};
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setData(Uri.parse("mailto:"));
        emailIntent.setType("text/plain");


        emailIntent.putExtra(Intent.EXTRA_EMAIL, TO);
        //emailIntent.putExtra(Intent.EXTRA_CC, CC);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        emailIntent.putExtra(Intent.EXTRA_TEXT, msg);

        try {
            context.startActivity(Intent.createChooser(emailIntent, "Send mail..."));
            ((GBActivity)context).finish();

        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(context,
                    "There is no email client installed.", Toast.LENGTH_SHORT).show();
        }
    }


    //************************************/
    /*             File utilities        */
    //************************************/

    /* Checks if external storage is available for read and write */
    boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return (Environment.MEDIA_MOUNTED.equals(state)) ;
    }

    /* Checks if external storage is available to at least read */
    boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        return (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) ;
    }

    //************************************/
    /*         Widget Utilities          */
    //************************************/
    void enableButton(Context context, Button button, boolean enable){
        button.setEnabled(enable);
        if (enable == BUTTON_ENABLE) {
            button.setTextColor(ContextCompat.getColor(context, R.color.colorTextBlack));
        } else {
            button.setTextColor(ContextCompat.getColor(context, R.color.colorGray));
        }
    }

    void showSoftKeyboard(FragmentActivity context, EditText textField){
        //Give the view the focus, then show the keyboard

        textField.requestFocus();
        InputMethodManager imm =
                (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
        //second parameter is flags. We don't need any of them
        imm.showSoftInput(textField, InputMethodManager.SHOW_FORCED);

    }

    void hideSoftKeyboard(FragmentActivity context){
        // Check if no view has focus:
        View view = context.getCurrentFocus();
        if (view != null) {
            view.clearFocus();
            InputMethodManager imm =
                    (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
            //second parameter is flags. We don't need any of them
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);


        }
        //close the keyboard
        context.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }

    void toggleSoftKeyboard(FragmentActivity context){
        //hide the soft keyboard
        // Check if no view has focus:
        View view = context.getCurrentFocus();
        if (view != null) {
            view.clearFocus();
            InputMethodManager imm =
                    (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
            //second parameter is flags. We don't need any of them
            imm.toggleSoftInputFromWindow(view.getWindowToken(),0, 0);
        }

    }


    void clearFocus(FragmentActivity context){
        //hide the soft keyboard
        // Check if no view has focus:
        View view = context.getCurrentFocus();
        if (view != null) {
            view.clearFocus();
        }
    }


    void hideKeyboard(Activity activity) {
        if (activity != null) {
            Window window = activity.getWindow();
            if (window != null) {
                View v = window.getCurrentFocus();
                if (v != null) {
                    InputMethodManager imm = (InputMethodManager) activity.
                            getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm!=null){
                        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    }
                }
            }
        }
    }


}
