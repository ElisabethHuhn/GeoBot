package com.asc.msigeosystems.geobot;

/**
 * Created by Elisabeth Huhn on 5/23/2016.
 *
 * This coordinate extends the basic capabilities of Prism4DCoordinateLL
 * and adds:
 *
 * A) returns its type from getCoordinateType as "Pris4DCoordinateNAD83"
 * B) remembers its coordinate system specific constants, e.g. Dataum
 *
 */


class GBCoordinateNAD83 extends GBCoordinateLL {


    //WGS84 Constants
    //static final double sSemiMajorAxis      = 6378137.0;    // a meters
    //static final double sFlattening         = 298.257223563; // 1/f unitless

    //NAD83 Constants
    //Get these from GBUtilities
    //static final double sSemiMajorAxis = 6378137.;      // a meters
    //static final double sFlattening    = 298.257222101; // 1/f unitless

    //Consider an ellipsoid of revolution with:
    // a = equatorial radius, aka semi major axis
    // b = polar radius, aka polar semi axis
    // f = flattening = (a - b) / a
    // e = eccentricity =  sqrt[f * (2.-f)]
    // n = third flattening = (a-b)/(a+b) = f / (2.-f)


    //From the Ellipsoid
    //Get these constants from GBUtilities
    //static final double sEquatorialRadiusA = 6378137.0;      //equatorial radius in meters
    //static final double sPolarRadiusB = -sEquatorialRadiusA * (sFlattening-1.0);


    private CharSequence mThisCoordinateType  = GBCoordinate.sCoordinateTypeNAD83;
    private CharSequence mThisCoordinateClass = GBCoordinate.sCoordinateTypeClassNAD83;
    static final String sDatum = "NAD83";

    /* ******
     *
     * Constructors
     *
     **********/

    GBCoordinateNAD83(){
        super.initializeDefaultVariables();
    }

    GBCoordinateNAD83(GBCoordinateWGS84 wsg84Coordinate) {

        // TODO: 12/14/2016 complete conversion from WSG84 to NAD83
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
        this.mThisCoordinateType = GBCoordinate.sCoordinateTypeNAD83;
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
        //initialize all variables from this level
        mThisCoordinateType  = sCoordinateTypeNAD83;
        mThisCoordinateClass = sCoordinateTypeClassNAD83;

        mDatum               = sDatum; //eg WGS84

    }


}
