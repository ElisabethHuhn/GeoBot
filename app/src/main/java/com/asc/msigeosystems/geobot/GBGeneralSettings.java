package com.asc.msigeosystems.geobot;

import android.content.Context;
import android.content.SharedPreferences;

import static android.R.attr.defaultValue;

/**
 * Created by Elisabeth Huhn on 7/29/17
 *
 * General Settings
 * Values saved in Shared Preferences, not the DB
 *
 */
public class GBGeneralSettings {
    //************************************/
    /*    Static (class) Constants       */
    //************************************/


    private static final String sAutosaveTag  = "AUTOSAVE";
    private static final String sRmsVStdTag   = "RMS_V_STD";
    private static final String sUIOrderTag   = "UI_ORDER";
    private static final String sLocDDvDMSTag = "LOC_DD_V_DMS";
    private static final String sCADDvDMSTag  = "CA_DD_V_DMS";
    private static final String sDirVPMTag    = "DIR_V_PM";

    private static final String sLocPrcTag    = "LOC_PRC";
    private static final String sStdPrcTag    = "STD_PRC";
    private static final String sSfPrcTag     = "SF_PRC";
    private static final String sCAPrcTag     = "CA_PRC";
    private static final String sExLocPrcTag    = "EXLOC_PRC";
    private static final String sExStdPrcTag    = "EXSTD_PRC";
    private static final String sExSfPrcTag     = "EXSF_PRC";
    private static final String sExCAPrcTag     = "EXCA_PRC";

    static final int sLocPrc    = 0;
    static final int sStdPrc    = 1;
    static final int sSfPrc     = 2;
    static final int sCAPrc     = 3;

    private static final boolean sAutosaveDefault = true;
    private static final boolean sRmsDefault      = true;
    private static final boolean sStdDefault      = false;
    private static final boolean sUIOrderDefault  = true;
    private static final boolean sLatLngDefault   = true;
    private static final boolean sLngLatDefault   = false;
    private static final boolean sNEDefault       = true;
    private static final boolean sENDefault       = true;

    private static final boolean sLocDDDefault    = true;
    private static final boolean sLocDMSDefault   = false;
    private static final boolean sCADDDefault     = true;
    private static final boolean sCADMSDefault    = false;
    private static final boolean sDirDefault      = true;
    private static final boolean sPMDefault       = false;

    private static final int sLocPrcDefault    = 6;
    private static final int sStdPrcDefault    = 6;
    private static final int sSfPrcDefault     = 6;
    private static final int sCAPrcDefault     = 6;
    static final int sExLocPrcDefault    = 6;
    static final int sExStdPrcDefault    = 6;
    static final int sExSfPrcDefault     = 6;
    static final int sExCAPrcDefault     = 6;

    //************************************/
    /*          Static Methods           */
    //************************************/


    static boolean getBooleanSetting (GBActivity activity, String tag, boolean initialValue) {
        if (activity == null){
            return false;
        }
        SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
        return sharedPref.getBoolean(tag, initialValue);
    }
    static void    setBooleanSetting (GBActivity activity, String tag, boolean boolValue) {
        if (activity == null){
            return;
        }

        SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(tag, boolValue);
        editor.apply();
    }


    static int  getIntSetting (GBActivity activity, String tag, int initialValue) {

        if (activity == null){
            return defaultValue;
        }
        SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);

        return sharedPref.getInt(tag, initialValue);
    }
    static void setIntSetting (GBActivity activity, String tag, int intValue) {
        if (activity == null){
            return;
        }

        SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(tag, intValue);
        editor.apply();
    }




    //************************************/
    /*    Static (class) Variables       */
    //************************************/


    //************************************/
    /*    Member (instance) Variables    */
    //************************************/

    //All values are stored in shared preferences




    //************************************************************
    // *             Constructor
    //*************************************************************/

    //Default constructor, A dummy project that is not in memory list or in the DB
    //Every field is initialized. No Nulls Allowed
    public GBGeneralSettings() {

    }



    //*********************************************/
    //Setters and Getters
    //*********************************************/

    static boolean isAutosave  (GBActivity activity)   {
        return getBooleanSetting(activity, sAutosaveTag, sAutosaveDefault);
    }
    static void    setAutosave(GBActivity activity) { setBooleanSetting(activity, sAutosaveTag, true);}
    static boolean isManualSave(GBActivity activity) { return !isAutosave(activity);}
    static void    setManualSave(GBActivity activity){ setBooleanSetting(activity, sAutosaveTag, false);}


    static boolean isRms    (GBActivity activity)   {
        return getBooleanSetting(activity, sRmsVStdTag, sRmsDefault);
    }
    static void    setRms   (GBActivity activity) { setBooleanSetting(activity, sRmsVStdTag, true);}
    static boolean isStdDev (GBActivity activity) { return !isRms(activity);}
    static void    setStdDev(GBActivity activity){ setBooleanSetting(activity, sRmsVStdTag, false);}


    static boolean isLatLng  (GBActivity activity)   {
        return getBooleanSetting(activity, sUIOrderTag, sLatLngDefault);
    }
    static void    setLatLng(GBActivity activity) { setBooleanSetting(activity, sUIOrderTag, true);}
    static boolean isLngLat(GBActivity activity) { return !isLatLng(activity);}
    static void    setLngLat(GBActivity activity){ setBooleanSetting(activity, sUIOrderTag, false);}


    static boolean isNE    (GBActivity activity)   {
        return getBooleanSetting(activity, sUIOrderTag, sNEDefault);
    }
    static void    setNE   (GBActivity activity) { setBooleanSetting(activity, sUIOrderTag, true);}
    static boolean isEN (GBActivity activity) { return !isNE(activity);}
    static void    setEN(GBActivity activity){ setBooleanSetting(activity, sUIOrderTag, false);}


    static boolean isLocDD    (GBActivity activity)   {
        return getBooleanSetting(activity, sLocDDvDMSTag, sLocDDDefault);
    }
    static void    setLocDD   (GBActivity activity) { setBooleanSetting(activity, sLocDDvDMSTag, true);}
    static boolean isLocDMS (GBActivity activity) { return !isLocDD(activity);}
    static void    setLocDMS(GBActivity activity){ setBooleanSetting(activity, sLocDDvDMSTag, false);}


    static boolean isCADD    (GBActivity activity)   {
        return getBooleanSetting(activity, sCADDvDMSTag, sCADDDefault);
    }
    static void    setCADD   (GBActivity activity) { setBooleanSetting(activity, sCADDvDMSTag, true);}
    static boolean isCADMS (GBActivity activity) { return !isCADD(activity);}
    static void    setCADMS(GBActivity activity){ setBooleanSetting(activity, sCADDvDMSTag, false);}


    static boolean isDir  (GBActivity activity)   {
        return getBooleanSetting(activity, sDirVPMTag, sDirDefault);
    }
    static void    setDir (GBActivity activity) { setBooleanSetting(activity, sDirVPMTag, true);}
    static boolean isPM   (GBActivity activity) { return !isDir(activity);}
    static void    setPM  (GBActivity activity) { setBooleanSetting(activity, sDirVPMTag, false);}





    static int  getLocPrecision(GBActivity activity) {
        return getIntSetting(activity, sLocPrcTag, sLocPrcDefault);
    }
    static void setLocPrecision(GBActivity activity, int locPrecision) {
        setIntSetting(activity, sLocPrcTag, locPrecision);
    }

    static int  getStdDevPrecision(GBActivity activity) {
        return getIntSetting(activity, sStdPrcTag, sStdPrcDefault);
    }
    static void setStdDevPrecision(GBActivity activity, int stdDevPrecision) {
        setIntSetting(activity, sStdPrcTag, stdDevPrecision);
    }


    static int  getSfPrecision(GBActivity activity) {
        return getIntSetting(activity, sSfPrcTag, sSfPrcDefault);
    }
    static void setSfPrecision(GBActivity activity, int sfPrecision) {
        setIntSetting(activity, sSfPrcTag, sfPrecision);
    }

    static int  getCAPrecision(GBActivity activity) {
        return getIntSetting(activity, sCAPrcTag, sCAPrcDefault);
    }
    static void setCAPrecision(GBActivity activity, int caPrecision) {
        setIntSetting(activity, sCAPrcTag, caPrecision);
    }



    static int  getExLocPrecision(GBActivity activity) {
        return getIntSetting(activity, sExLocPrcTag, sExLocPrcDefault);
    }
    static void setExLocPrecision(GBActivity activity, int locPrecision) {
        setIntSetting(activity, sExLocPrcTag, locPrecision);
    }

    static int  getExStdDevPrecision(GBActivity activity) {
        return getIntSetting(activity, sExStdPrcTag, sExStdPrcDefault);
    }
    static void setExStdDevPrecision(GBActivity activity, int stdDevPrecision) {
        setIntSetting(activity, sExStdPrcTag, stdDevPrecision);
    }


    static int  getExSfPrecision(GBActivity activity) {
        return getIntSetting(activity, sExSfPrcTag, sExSfPrcDefault);
    }
    static void setExSfPrecision(GBActivity activity, int sfPrecision) {
        setIntSetting(activity, sExSfPrcTag, sfPrecision);
    }

    static int  getExCAPrecision(GBActivity activity) {
        return getIntSetting(activity, sExCAPrcTag, sExCAPrcDefault);
    }
    static void setExCAPrecision(GBActivity activity, int caPrecision) {
        setIntSetting(activity, sExCAPrcTag, caPrecision);
    }



}
