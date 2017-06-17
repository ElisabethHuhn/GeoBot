package com.asc.msigeosystems.geobot;

/**
 * Created by Elisabeth Huhn on 11/20/2016.
 *
 * This is the superclass of all coordinate types
 * It doesn't do much, as it will be subtypes that are actually instantiated.
 * This level just provides the capability for the subtypes to
 * identify the type of the instance that is actually instantiated
 */

abstract class GBCoordinate {

    static final String sCoordinateTypeWGS84 = "WGS84 G1762";
    static final String sCoordinateTypeNAD83 = "NAD83 2011";
    static final String sCoordinateTypeUTM   = "UTM";
    static final String sCoordinateTypeSPCS  = "US State Plane Coordinates";

    static final int sCoordinateDBTypeUnknown = -1;
    static final int sCoordinateDBTypeWGS84 = 0;
    static final int sCoordinateDBTypeNAD83 = 1;
    static final int sCoordinateDBTypeUTM   = 2;
    static final int sCoordinateDBTypeSPCS  = 3;

    static final String sCoordinateTypeClassWGS84 = "GBCoordinateWGS84";
    static final String sCoordinateTypeClassNAD83 = "GBCoordinateNAD83";
    static final String sCoordinateTypeClassUTM   = "GBCoordinateUTM";
    static final String sCoordinateTypeClassSPCS  = "GBCoordinateSPCS";

    static final int sUNKWidgets = 0;
    static final int sLLWidgets = 1;
    static final int sENWidgets = 2;

    private CharSequence mThisCoordinateType = "Undefined";
    private CharSequence mThisCoordinateClass = "GBCoordinate";


    /* *********************************************************/
    /* ***    Variables common to ALL coordinates        *******/
    /* *********************************************************/
    private long mCoordinateID; //All coordinates have a DB ID
    private long mProjectID; //May or may not describe a point
    private long mPointID;   //These will be null if not describing a point

    protected boolean      mValidCoordinate = true;
    private boolean      mIsFixed         = true;

    /* *********************************************************/
    /* ***    Static methods                             *******/
    /* *********************************************************/
    static int getNextCoordinateID(){
        return (int) (System.currentTimeMillis() & 0xfffffff);
    }

    /* *****************************************************************/
    /*      Return codes are static int's:                             */
    /*                       GBCoordinate.sUNKWidgets                  */
    /*                       GBCoordinate.sLLWidgets                   */
    /*                       GBCoordinate.sENWidgets                   */
    /* *****************************************************************/
    static int getCoordinateTypeFromProjectID(long projectID){
        if (projectID == GBUtilities.ID_DOES_NOT_EXIST)return GBCoordinate.sUNKWidgets;
        int returnCode = GBCoordinate.sUNKWidgets;


        GBProject project = GBProjectManager.getInstance().getProject(projectID);

        CharSequence coordinateType;
        if (project != null) {
            coordinateType = project.getProjectCoordinateType();
        } else {
            coordinateType = null;
        }


        if (!GBUtilities.isEmpty(coordinateType)){
            if (coordinateType.equals(GBCoordinate.sCoordinateTypeWGS84) ||
                coordinateType.equals(GBCoordinate.sCoordinateTypeNAD83) ){

                returnCode = GBCoordinate.sLLWidgets;

            } else if (coordinateType.equals(GBCoordinate.sCoordinateTypeUTM) ||
                       coordinateType.equals(GBCoordinate.sCoordinateTypeSPCS) ){

                returnCode = GBCoordinate.sENWidgets;
            }
        }
        return returnCode;
    }




    /* *********************************************************/
    /* ***    Setters and Getters for common variables   *******/
    /* *********************************************************/


    //This method returns the type of the instance actually instantiated
    CharSequence getCoordinateType() { return mThisCoordinateType; }

    //This method returns the type of the instance as a string for UI display
    CharSequence getCoordinateClass(){ return mThisCoordinateClass; }


     long getCoordinateID(){return mCoordinateID;}
     void setCoordinateID(long coordinateID){this.mCoordinateID = coordinateID;}


    long  getProjectID()              { return mProjectID; }
    void setProjectID(long projectID) { mProjectID = projectID; }

    long  getPointID()            { return mPointID; }
    void setPointID(long pointID) { mPointID = pointID; }


    void    setValidCoordinate(boolean validCoordinate){ this.mValidCoordinate = validCoordinate;}
    boolean isValidCoordinate() {
        return mValidCoordinate;
    }

    void    setIsFixed(boolean isFixed){this.mIsFixed = isFixed;}
    boolean isFixed()                  {return mIsFixed;}


    /* *********************************************************/
    /* ***     Methods common to ALL coordinates         *******/
    /* *********************************************************/

    protected void initializeDefaultVariables() {
        //set all variables with defaults, so that none are null
        //I know that one does not have to initialize int's etc, but
        //to be explicit about the initialization, do it anyway

        mCoordinateID = GBCoordinate.getNextCoordinateID();

        mProjectID = 0; //assume does not describe a point
        mPointID = 0;
    }



}
