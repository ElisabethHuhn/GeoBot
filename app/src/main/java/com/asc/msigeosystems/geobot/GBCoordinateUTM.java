package com.asc.msigeosystems.geobot;

/**
 * Created by Elisabeth Huhn on 5/25/2016.
 * This coordinate extends the basic capabilities of Prism4DCoordinateEN
 * and adds:
 *
 * A) returns its type from getCoordinateType as "Pris4DCoordinateUTM"
 * B) remembers its coordinate system specific constants, e.g. Dataum
 *
 */

class GBCoordinateUTM extends GBCoordinateEN {

    //variables and setters/getters at super level
    private CharSequence mThisCoordinateType  = GBCoordinate.sCoordinateTypeUTM;
    private CharSequence mThisCoordinateClass = GBCoordinate.sCoordinateTypeClassUTM;


    /* ******
     *
     * Constructors
     *
     **********/


    GBCoordinateUTM(){super.initializeDefaultVariables();}

    GBCoordinateUTM(GBCoordinateWGS84 coordinate) {
        //initialize all variables to their defaults
        super.initializeDefaultVariables();
        convertWGStoUTM(coordinate.getLatitude(), coordinate.getLongitude());
        mDatum = "WGS84"; //eg WGS84
    }

    GBCoordinateUTM(GBCoordinateNAD83 coordinate) {
        //initialize all variables to their defaults
        super.initializeDefaultVariables();
        convertNADtoUTM(coordinate.getLatitude(), coordinate.getLongitude());
        mDatum = "NAD83"; //eg NAD83
    }

    private void convertWGStoUTM (double lat, double longi) throws IllegalArgumentException {
        setWgsConstants(); //use the WGS constants for the conversion
        convertLLtoUTM(lat, longi);
    }

    private void convertNADtoUTM (double lat, double longi) throws IllegalArgumentException {
        setNadConstants(); //use the NAD constants for the conversion
        convertLLtoUTM(lat, longi); //on superclass
    }


    /* ******
     *
     * Setters and Getters
     *
     **********/

    //This method returns the type of the instance actually instantiated
    @Override
    CharSequence getCoordinateType() { return mThisCoordinateType; }
    void         setCoordinateType(){
        this.mThisCoordinateType = GBCoordinate.sCoordinateTypeUTM;
    }


    //This method returns the type of the instance as a string for UI display
    @Override
    CharSequence getCoordinateClass(){ return mThisCoordinateClass; }


    /* ******
     *
     * Static methods
     *
     **********/


    /* ******
     *
     * Member methods
     *
     **********/


    protected void initializeDefaultVariables(){
        //set all variables with defaults, so that none are null
        //I know that one does not have to initialize int's etc, but
        //to be explicit about the initialization, do it anyway

        //initialize all variables common to EN coordinates
        super.initializeDefaultVariables();

        //initialize all variables from this level

    }


    private void setWgsConstants() {
        //mEquatorialRadiusA = GBCoordinateWGS84.sEquatorialRadiusA;
        //mPolarRadiusB      = GBCoordinateWGS84.sPolarRadiusB;
    }

    private void setNadConstants() {
        //mEquatorialRadiusA = GBCoordinateNAD83.sEquatorialRadiusA;
        //mPolarRadiusB      = GBCoordinateNAD83.sPolarRadiusB;
    }


}
