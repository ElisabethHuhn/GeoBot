package com.asc.msigeosystems.geobot;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by elisabethhuhn on 5/7/2016.
 * This class is the container for any global settings of the App
 */
class GBGlobalSettings {
    //************************************/
    /*    Static (class) Constants       */
    //************************************/
    private static final String sNextProjectIDTag      = "nextProjectID";


    //************************************/
    /*    Static (class) Variables       */
    //************************************/
    private static GBGlobalSettings ourInstance = new GBGlobalSettings();

    //************************************/
    /*    Member (instance) Variables    */
    //************************************/



    //************************************/
    /*         Static Methods            */
    //************************************/
    public static GBGlobalSettings getInstance() {
        return ourInstance;
    }



    //************************************/
    /*         CONSTRUCTOR               */
    //************************************/
    private GBGlobalSettings() {

    }



    //************************************/
    /*    Member setter/getter Methods   */
    //************************************/


    //*********************************************************/
    //               Preferences setters and getters         //
    //*********************************************************/

    //Returns ID_DOES_NOT_EXIST if it can't find a value in Preferences
    //automatically increments the nextID in Preferences
    long getNextProjectID (GBActivity activity)  {
        long nextID = getPotentialNextProjectID(activity);
        if (nextID == GBUtilities.ID_DOES_NOT_EXIST) return nextID;

        setNextProjectID(activity, nextID++);
        return nextID;
    }
    long setNextProjectID (GBActivity activity, long projectID){

        //Store the ProjectID for the next time
        SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putLong(sNextProjectIDTag, projectID);
        editor.apply();

        return projectID;
    }


    //No auto-increment. Analogous to Peak at a stack
    long getPotentialNextProjectID (GBActivity activity)  {
        SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
        long defaultValue = GBUtilities.ID_DOES_NOT_EXIST;
        return sharedPref.getLong(sNextProjectIDTag, defaultValue);

    }


    // TODO: 6/13/2017 Put these Maps constants in Preferences
    int getMinZoomLevel(){
        return 8;
    }
    int getMarkerPadding(){
        return 50;
    }
    int getForceZoomAfter(){
        return 2;
    }













}
