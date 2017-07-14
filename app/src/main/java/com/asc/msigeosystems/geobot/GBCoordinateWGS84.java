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


    static final String sDatum = "WGS84";



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
        initializeDefaultVariables();
    }

    GBCoordinateWGS84(GBCoordinateUTM coordinateUTM){
        // TODO: 12/27/2016 finish the conversion from UTM to WGS
        initializeDefaultVariables();
    }

    GBCoordinateWGS84(GBCoordinateNAD83 coordinateNAD83){
        // TODO: 12/27/2016 finish conversion from NAD to WGS
        initializeDefaultVariables();
    }

    GBCoordinateWGS84(GBCoordinateSPCS coordinateSPCS){

        initializeDefaultVariables();
        int zone = coordinateSPCS.getZone();
        GBCoordinateConstants constants = new GBCoordinateConstants(zone);
        int spcsZone = constants.getZone();
        if (spcsZone == (int)GBUtilities.ID_DOES_NOT_EXIST) {
            setValidCoordinate(false);
            return;
        }



        if (constants.getProjection() == GBCoordinateConstants.sLAMBERT){
            convertInverseLambert(coordinateSPCS, constants);
        } else {
            convertInverseMercator(coordinateSPCS, constants);
        }
    }

    GBCoordinateWGS84(GBActivity activity, GBNmea nmeaData){
        //GBCoordinateWGS84 wgsCoordinate = new GBCoordinateWGS84(nmeaData.getLatitude(),
                                                                //nmeaData.getLongitude());
        initializeDefaultVariables();
        latLongDD(nmeaData.getLatitude(), nmeaData.getLongitude());

        setElevation(nmeaData.getOrthometricElevation());
        setGeoid(nmeaData.getGeoid());

        setProjectID(GBUtilities.getInstance().getOpenProjectID(activity));
        //setTime(System.currentTimeMillis());

        setTime(nmeaData.getTimeStamp());
    }

    GBCoordinateWGS84(GBActivity activity, GBCoordinateMean meanCoordinate){
       // GBCoordinateWGS84 wgs84Coordinate = new GBCoordinateWGS84(meanCoordinate.getLatitude(),
                                                                  //meanCoordinate.getLongitude());
        initializeDefaultVariables();
        latLongDD(meanCoordinate.getLatitude(), meanCoordinate.getLongitude());


        setElevation(meanCoordinate.getElevation());
        setGeoid    (meanCoordinate.getGeoid());

        setProjectID(GBUtilities.getInstance().getOpenProjectID(activity));
        setTime(System.currentTimeMillis());
    }

    GBCoordinateWGS84(double latitude, double longitude) {

        //initialize all variables to their defaults
        initializeDefaultVariables();

        latLongDD(latitude, longitude);
    }


    GBCoordinateWGS84(int latitudeDegree, int latitudeMinute, double latitudeSecond,
                      int longitudeDegree, int longitudeMinute, double longitudeSecond){

        //initialize all variables to their defaults
        initializeDefaultVariables();

        latLongDMS( latitudeDegree,  latitudeMinute,  latitudeSecond,
                    longitudeDegree, longitudeMinute, longitudeSecond);
    }

    GBCoordinateWGS84(CharSequence latitudeString, CharSequence longitudeString) {

        //initialize all variables to their defaults
        initializeDefaultVariables();

        super.latLongDDStrings(latitudeString, longitudeString);
    }




    GBCoordinateWGS84(   long   timestamp,
                         String latitudeString,
                         String latitudeDegreeString,
                         String latitudeMinuteString,
                         String latitudeSecondString,
                         String longitudeString,
                         String longitudeDegreeString,
                         String longitudeMinuteString,
                         String longitudeSecondString,
                         String elevationString,
                         String elevationFString,
                         String geoidString,
                         String geoidFString) {

        //initialize all variables to their defaults
        initializeDefaultVariables();

        setTime(timestamp);

        if (latitudeString.isEmpty()) {
            latitudeString = "0";
        }
        if (latitudeDegreeString.isEmpty()) {
            latitudeDegreeString = "0";
        }
        if (latitudeMinuteString.isEmpty()){
            latitudeMinuteString = "0";
        }
        if (latitudeSecondString.isEmpty() ){
            latitudeSecondString = "0.0";
        }


        if (longitudeString.isEmpty()){
            longitudeString = "0";
        }
        if (longitudeDegreeString.isEmpty()){
            longitudeDegreeString = "0";
        }
        if (longitudeMinuteString.isEmpty() ){
            longitudeMinuteString = "0";
        }
        if (longitudeSecondString.isEmpty()) {
            longitudeSecondString = "0.0";
        }


        this.mLatitude       = Double.parseDouble(latitudeString);
        this.mLatitudeDegree = Integer.valueOf   (latitudeDegreeString);
        this.mLatitudeMinute = Integer.valueOf   (latitudeMinuteString);
        this.mLatitudeSecond = Double.parseDouble(latitudeSecondString);

        this.mLongitude       = Double.parseDouble(longitudeString);
        this.mLongitudeDegree = Integer.valueOf   (longitudeDegreeString);
        this.mLongitudeMinute = Integer.valueOf   (longitudeMinuteString);
        this.mLongitudeSecond = Double.parseDouble(longitudeSecondString);

        boolean latReturnCode = true;
        boolean lngReturnCode = true;

        if ((mLatitude != 0.) && (mLongitude != 0.)){
            latReturnCode = latLongDD(mLatitude, mLongitude);
        } else if ((mLatitude == 0.) &&
                  ((mLatitudeDegree != 0) || (mLatitudeMinute != 0) || mLatitudeSecond != 0.)){
            latReturnCode = convertLatDMSToDD();
            if (mLongitude != 0) {
                lngReturnCode = convertLngDDToDMS();
            } else if ((mLongitude == 0) &&
                    ((mLongitudeDegree != 0) || (mLongitudeMinute != 0) || (mLongitudeSecond != 0.))){
                lngReturnCode = convertLngDMSToDD();
            } else {
                lngReturnCode = false;
            }

        } else {
            //everything is zero. Special casee, but must handle it
            latReturnCode = latLongDD(mLatitude, mLongitude);
        }
        setValidCoordinate(latReturnCode && lngReturnCode);

        if (!isValidCoordinate())return;

        setElevation(getMeters(elevationString, elevationFString));
        setGeoid    (getMeters(geoidString    , geoidFString));
    }


    GBCoordinateWGS84(   CharSequence latitudeDegreeString,
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

        mCoordinateDBType = GBCoordinate.sCoordinateDBTypeWGS84;

    }


    private void convertInverseLambert(GBCoordinateSPCS coordinateSPCS,
                                       GBCoordinateConstants constants){

        //Equ A) 	N1 	=  N – No
        double N1  = coordinateSPCS.getNorthing() - constants.getFalseNorthing2();

        //Equ B) 	E1	=  E – Eo
        double E1  = coordinateSPCS.getEasting()  - constants.getFalseEasting();

        //Equ C) 	R1   	=  Ro – N1
        double R1  = constants.getRoMappingRadiusAtLat() - N1;

        double radER = Math.toRadians(E1 / R1);

        //Equ D) 	Y 	=  tan-1 (E1/R1)
        double convergenceAngle = Math.atan(radER);

        double radB0 = Math.toRadians(constants.getCentralParallel());
        double sinB0 = Math.sin(radB0);

        //Equ E) 	λ 	=  λo (Lo) – Y/sin Bo
        // TODO: 6/30/2017 This is not implemented properly. Get the correct interpretation from Robert
        double longitude = constants.getCentralMeridian() - (convergenceAngle / sinB0);

        double tanCA = Math.tan(Math.toRadians(convergenceAngle / 2.));

        //Equ F) 	u	=  N1 – E1 tan (Y/2)
        double u = N1 - (E1 * tanCA);
        double u2 = u*u;
        double u3 = u*u2;

        double G1 = constants.getG1();
        double G2 = constants.getG2();
        double G3 = constants.getG3();
        double G4 = constants.getG4();
        double G5 = constants.getG5();


        //Equ G) 	Δ Φ 	=  u[G1 + uG2 + uG3 + uG4 +G5u]
        double deltaLat = (G1*u) + (G2*u2) + (G3 *u2) + (G4*u2) + (G5*u2);

        //Equ H) 	Φ 	=  Bo + Δ Φ
        double latitude = constants.getCentralParallel() + deltaLat;

        double F1 = constants.getF1();
        double F2 = constants.getF2();
        double F3 = constants.getF3();

        //Equ I) 	k 	=   F1  +  F2u2  +  F3u3
        double scaleFactor = F1 + (F2 * u2) + (F3 * u3);


        super.setLatitude(latitude);
        super.setLongitude(longitude);
        super.setScaleFactor(scaleFactor);
        super.setConvergenceAngle(convergenceAngle);

    }



    private void convertInverseMercator(GBCoordinateSPCS coordinateSPCS,
                                       GBCoordinateConstants constants){

        double latitude = 0;
        double longitude = 0;
        double scaleFactor = 0;
        double convergenceAngle = 0;

        setValidCoordinate(false);


        super.setLatitude(latitude);
        super.setLongitude(longitude);
        super.setScaleFactor(scaleFactor);
        super.setConvergenceAngle(convergenceAngle);

    }


}
