package com.asc.msigeosystems.geobot;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.math.BigDecimal;
import java.math.RoundingMode;
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


    public static final boolean BUTTON_DISABLE = false;
    public static final boolean BUTTON_ENABLE  = true;

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

    static final double feetPerMeter   = 3.280833333;
    static final double inchesPerMeter = feetPerMeter*12.;   //39.37
    static final double cmPerInch      = 100. / (feetPerMeter * 12.); //2.54



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
        //truncate to a reasonable number of decimal digits
        double feet = meters * GBUtilities.feetPerMeter;
        BigDecimal bd = new BigDecimal(feet).setScale(sMicrometerDigitsOfPrecision, RoundingMode.HALF_UP);
        feet = bd.doubleValue();

        return (feet);  // official conversion rate of Meters to Feet

    }

    static double convertFeetToMeters(double feet){
        double meters = feet / GBUtilities.feetPerMeter;
        //truncate to a reasonable number of decimal digits
        BigDecimal bd = new BigDecimal(meters).setScale(sMicrometerDigitsOfPrecision, RoundingMode.HALF_UP);
        meters = bd.doubleValue();

        return (meters);
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

        feet = meters * GBUtilities.feetPerMeter;
        BigDecimal bd = new BigDecimal(feet).setScale(sMicrometerDigitsOfPrecision, RoundingMode.HALF_UP);
        feet = bd.doubleValue();

        feetWidget.setText(String.valueOf(feet));

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
        //truncate to a reasonable number of decimal digits
        BigDecimal bd = new BigDecimal(meters).setScale(sMicrometerDigitsOfPrecision, RoundingMode.HALF_UP);
        meters = bd.doubleValue();

        metersWidget.setText(String.valueOf(meters));


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
    static double getMetersPerPixel(double latitude, float zoomLevel){
        return (getCircumferenceInMetersAtLatitude(latitude) /
                getCircumferenceInPixelsAtLatitude(latitude, zoomLevel));
        /*
        double metersPerPixelZoomZero  = 156543.03392; //by definition
        double metersPerPixel = metersPerPixelZoomZero *
                                Math.cos(latitude * Math.PI / 180) / Math.pow(2, zoomLevel);

        return metersPerPixel;
        */
    }


    static double getCircumferenceInMetersAtLatitude (double latitude){
        double radius = getRadiusInMetersAtLatitude(latitude);
        double circumfrence = 2 * Math.PI * radius;
        return circumfrence;
    }


    static double getRadiusInMetersAtLatitude(double latitude) {
        //latitude in radians
        double latRad = latitude * Math.PI / 180.;
        double equRSqr = sEquatorialRadiusA * sEquatorialRadiusA;//in meters
        double polRSqr = sPolarRadiusB * sPolarRadiusB;
        double cosLat = Math.cos(latRad);

        double numerator = ((equRSqr * cosLat) * (equRSqr * cosLat)) +
                           ((polRSqr * cosLat) * (polRSqr * cosLat));
        double denominator = ((sEquatorialRadiusA * cosLat) * (sEquatorialRadiusA * cosLat)) +
                             ((sPolarRadiusB      * cosLat) * (sPolarRadiusB      * cosLat));

        double radius = Math.sqrt(numerator / denominator);
        return radius;
    }

    static double getCircumferenceInPixelsAtLatitude (double latitude, float zoomLevel){

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


    static boolean isEmpty(CharSequence str) {
        return (str == null || str.length() == 0);
    }

    DecimalFormat df = new DecimalFormat("#.##");
    private String convertToFormat(double value){

        return df.format(value);
    }


    public static String getDateTimeString(long milliSeconds){
        Date date = new Date(milliSeconds);
        return DateFormat.getDateTimeInstance().format(date);
    }

    public static long getDateTimeFromString(Context activity, String timeString){
        Date date;
        try {
            SimpleDateFormat format = new SimpleDateFormat("MMM d, yyyy hh:mm:ss", Locale.getDefault());
            date = format.parse(timeString);
        } catch ( ParseException e){
            GBUtilities.getInstance().errorHandler(activity, e.getMessage());
            return 0;
        }
        return date.getTime();
    }


    //***********************************/
    //******** Constructors     *********/
    //***********************************/
    private GBUtilities() {

    }


    //***********************************/
    //******** Setters/Getters  *********/
    //***********************************/


    GBProject getOpenProject(GBActivity activity) {
        long openProjectID = getOpenProjectID(activity);
        if (openProjectID == GBUtilities.ID_DOES_NOT_EXIST)return null;

        return GBProjectManager.getInstance().getProject(openProjectID);
    }

    void  setOpenProject(GBActivity activity, GBProject openProject) {
        if (activity == null){
            return;
        }
        //Store the PersonID for the next time
        SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putLong(GBActivity.sOpenProjectIDTag, openProject.getProjectID());
        editor.apply();

    }

    long  getOpenProjectID(GBActivity activity) {
        if (activity == null){
            return GBUtilities.ID_DOES_NOT_EXIST;
        }
        SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
        long defaultValue = GBUtilities.ID_DOES_NOT_EXIST;
        return sharedPref.getLong(GBActivity.sOpenProjectIDTag, defaultValue);
    }

    String getOpenProjectIDMessage(GBActivity activity){
        if (activity == null){
            return "Programming Error, Activity is null";
        }
        GBProject openProject = getOpenProject(activity);
        if (openProject != null){
            //A project is open
            return activity.getString(R.string.project_opened_1) + " " +
                    String.valueOf(openProject.getProjectID())  + " " +
                    openProject.getProjectName()                + " " +
                    activity.getString(R.string.project_opened_2);
        } else {
            return activity.getString(R.string.no_project_open);
        }

    }

    GBPoint getOpenPoint(GBActivity activity) {
        long openPointID = getOpenPointID(activity);
        if (openPointID == GBUtilities.ID_DOES_NOT_EXIST)return null;

        return GBPointManager.getInstance().getPoint(getOpenProjectID(activity), openPointID);
    }

    void  setOpenPoint(GBActivity activity, GBPoint openPoint) {
        if (activity == null){
            return;
        }
        //Store the PersonID for the next time
        SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putLong(GBActivity.sOpenPointIDTag, openPoint.getPointID());
        editor.apply();

    }

    long  getOpenPointID(GBActivity activity) {
        if (activity == null){
            return GBUtilities.ID_DOES_NOT_EXIST;
        }
        SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
        long defaultValue = GBUtilities.ID_DOES_NOT_EXIST;
        return sharedPref.getLong(GBActivity.sOpenPointIDTag, defaultValue);
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

    public int getOrientation(Context activity){
        int orientation = Configuration.ORIENTATION_PORTRAIT;
        if (activity.getResources().getDisplayMetrics().widthPixels >
            activity.getResources().getDisplayMetrics().heightPixels) {

            orientation = Configuration.ORIENTATION_LANDSCAPE;
        }
        return orientation;
    }


    //************************************/
    /*         Widget Utilities          */
    //************************************/
    public  void enableButton(Context context, Button button, boolean enable){
        button.setEnabled(enable);
        if (enable == BUTTON_ENABLE) {
            button.setTextColor(ContextCompat.getColor(context, R.color.colorTextBlack));
        } else {
            button.setTextColor(ContextCompat.getColor(context, R.color.colorGray));
        }
    }

    public  void showSoftKeyboard(FragmentActivity context, EditText textField){
        //Give the view the focus, then show the keyboard

        textField.requestFocus();
        InputMethodManager imm =
                (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
        //second parameter is flags. We don't need any of them
        imm.showSoftInput(textField, InputMethodManager.SHOW_FORCED);

    }

    public  void hideSoftKeyboard(FragmentActivity context){
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

    public  void toggleSoftKeyboard(FragmentActivity context){
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


    public  void clearFocus(FragmentActivity context){
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
