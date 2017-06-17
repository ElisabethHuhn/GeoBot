package com.asc.msigeosystems.geobot;

/**
 * Created by Elisabeth Huhn on 5/23/2016.
 *
 * * This coordinate extends the basic capabilities of Prism4DCoordinateLL
 * and adds:
 *
 * A) returns its type from getCoordinateType as "Pris4DCoordinateWGS84"
 * B) remembers its coordinate system specific constants, e.g. Dataum
 *

 */


class GBCoordinateWGS84 extends GBCoordinateLL {

    //WGS84 Datum constants
    //Karney 2010 page 5
    //From the Ellipsoid
    //Get these constants from GBUtilities
    //static final double sEquatorialRadiusA = 6378137.0;      //equatorial radius in meters
    //static final double sPolarRadiusB      = 6356752.314245; //polar semi axis

    //WGS84 Constants
    //static final double sSemiMajorAxis      = 6378137.0;    // a meters
    //static final double sFlattening         = 298.257223563; // 1/f unitless

    private CharSequence mThisCoordinateType  = GBCoordinate.sCoordinateTypeWGS84;
    private CharSequence mThisCoordinateClass = GBCoordinate.sCoordinateTypeClassWGS84;



    /* ******
     *
     * Setters and Getters
     *
     **********/


    /* ******
     *
     * Static functions
     *
     **********/



    /* ******
     *
     * Constructors
     *
     **********/

    GBCoordinateWGS84(){
        super.initializeDefaultVariables();
    }

    GBCoordinateWGS84(GBCoordinateUTM coordinateUTM){
        // TODO: 12/27/2016 finish the conversion from UTM to WGS
        super.initializeDefaultVariables();
    }

    GBCoordinateWGS84(GBCoordinateNAD83 coordinateNAD83){
        // TODO: 12/27/2016 finish conversion from NAD to WGS
        super.initializeDefaultVariables();
    }

    GBCoordinateWGS84(GBCoordinateSPCS coordinateSPCS){
        // TODO: 12/27/2016 finish conversion from SPCS to WGS 
        super.initializeDefaultVariables();
    }

    GBCoordinateWGS84(double latitude, double longitude) {

        //initialize all variables to their defaults
        initializeDefaultVariables();

        latLongDD(latitude, longitude);
    }


    GBCoordinateWGS84(int latitudeDegree, int latitudeMinute, double latitudeSecond,
                             int longitudeDegree, int longitudeMinute, double longitudeSecond){

        //initialize all variables to their defaults
        super.initializeDefaultVariables();

        latLongDMS( latitudeDegree,  latitudeMinute,  latitudeSecond,
                    longitudeDegree, longitudeMinute, longitudeSecond);
    }

    GBCoordinateWGS84(CharSequence latitudeString, CharSequence longitudeString) {

        //initialize all variables to their defaults
        super.initializeDefaultVariables();

        super.latLongDDStrings(latitudeString, longitudeString);
    }





    GBCoordinateWGS84(CharSequence latitudeDegreeString,
                             CharSequence latitudeMinuteString,
                             CharSequence latitudeSecondString,
                             CharSequence longitudeDegreeString,
                             CharSequence longitudeMinuteString,
                             CharSequence longitudeSecondString) {

        //initialize all variables to their defaults
        initializeDefaultVariables();

        latLongDMSStrings(latitudeDegreeString,
                          latitudeMinuteString,
                          latitudeSecondString,
                          longitudeDegreeString,
                          longitudeMinuteString,
                          longitudeSecondString);
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
        this.mThisCoordinateType = GBCoordinate.sCoordinateTypeWGS84;
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


}
