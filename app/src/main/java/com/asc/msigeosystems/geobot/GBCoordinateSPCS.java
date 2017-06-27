package com.asc.msigeosystems.geobot;

/**
 * This is coordinate in the State Plane Coordinate System
 * Created by Elisabeth Huhn on 11/11/2016.
 *
 * * This coordinate extends the basic capabilities of Prism4DCoordinateEN
 * and adds:
 *
 * A) returns its type from getCoordinateType as "Pris4DCoordinateSPCS"
 * B) remembers its coordinate system specific constants, e.g. Dataum
 *

 */
class GBCoordinateSPCS extends GBCoordinateEN {

    //+*******************************************************************/
    //+**********      Static Constants                         **********/
    //+*******************************************************************/

    static final String sDatum = "SPCS";

    private CharSequence mThisCoordinateType  = GBCoordinate.sCoordinateTypeSPCS;
    private CharSequence mThisCoordinateClass = GBCoordinate.sCoordinateTypeClassSPCS;


    static final double E0     = 2000000.0000; //meters Easting of projection and grid origion
    static final double E0feet = 6561666.667; //E0 in feet
    static final double Nb     = 500000.0000;  //meters northing of the grid base
    static final double Nbfeet = 1640416.667; //feet





    //+*******************************************************************/
    //+**********Instance Variables, Setters & Getters          **********/
    //+*******************************************************************/

    private CharSequence mState;

    CharSequence getState() {
        return mState;
    }
    void setState(CharSequence state) {
        mState = state;
    }

    //This method returns the type of the instance actually instantiated
    @Override
    CharSequence getCoordinateType() { return mThisCoordinateType; }
    void         setCoordinateType(){
        this.mThisCoordinateType = GBCoordinate.sCoordinateTypeSPCS;
    }


    //This method returns the type of the instance as a string for UI display
    @Override
    CharSequence getCoordinateClass(){ return mThisCoordinateClass; }



    //+*******************************************************************/
    //+**********     Constructors                              **********/
    //+*******************************************************************/

    GBCoordinateSPCS() {super.initializeDefaultVariables(); }

    GBCoordinateSPCS(String zoneString,
                     String stateString,
                     String eastingString,
                     String eastingFString,
                     String northingString,
                     String northingFString,
                     String elevationString,
                     String elevationFString,
                     String geoidString,
                     String geoidFFString,
                     String convergenceString,
                     String scaleString){
        initializeDefaultVariables();

        setEasting  (getMeters(eastingString,   eastingFString));
        setNorthing (getMeters(northingString,  northingFString));
        setElevation(getMeters(elevationString, elevationFString));
        setGeoid    (getMeters(geoidString    , geoidFFString));
        setConvergenceAngle(Double.valueOf(convergenceString));
        setScaleFactor(Double.valueOf(scaleString));
    }





    GBCoordinateSPCS(GBCoordinateWGS84 wsg84Coordinate, int zone){

        initializeDefaultVariables();

        GBCoordinateConstants constants = new GBCoordinateConstants(zone);
        int spcsZone = constants.getZone();
        if (spcsZone == (int)GBUtilities.ID_DOES_NOT_EXIST) return;

        super.setZone(spcsZone);
        setState(constants.getState());

        if (constants.getProjection() == GBCoordinateConstants.sLAMBERT){
            convertWithLambert(wsg84Coordinate, constants);
        } else {
            convertWithMercator(wsg84Coordinate, constants);
        }

        setElevation(wsg84Coordinate.getElevation());
        setGeoid(wsg84Coordinate.getGeoid());
    }


    protected void initializeDefaultVariables(){
        //set all variables with defaults, so that none are null
        //I know that one does not have to initialize int's etc, but
        //to be explicit about the initialization, do it anyway

        //initialize all variables common to EN coordinates
        super.initializeDefaultVariables();

        //initialize all variables from this level
        mThisCoordinateType  = sCoordinateTypeSPCS;
        mThisCoordinateClass = sCoordinateTypeClassSPCS;
        mDatum               = sDatum; //eg WGS84


    }



    //+*******************************************************************/
    //+**********     Instance Methods                          **********/
    //+*******************************************************************/

    private void convertWithLambert(GBCoordinateWGS84 coordinateWGS84,
                                    GBCoordinateConstants constants){
        //Convert degrees to radians (radians = degrees * pi/180)
        double latAts  = coordinateWGS84.getLatitude() ;
        double latAtcp = constants.getCentralParallel();

        double lngAts  = coordinateWGS84.getLongitude();
        double lngAtcm = constants.getCentralMeridian();

        double deltaLatitude = latAts - latAtcp;
        double deltaLatitude2 = deltaLatitude * deltaLatitude;
        double deltaLatitude3 = deltaLatitude * deltaLatitude2;
        double deltaLatitude4 = deltaLatitude * deltaLatitude3;
        double deltaLatitude5 = deltaLatitude * deltaLatitude4;

        double u = constants.getL1() * deltaLatitude +
                   constants.getL2() * deltaLatitude2 +
                   constants.getL3() * deltaLatitude3 +
                   constants.getL4() * deltaLatitude4 +
                   constants.getL5() * deltaLatitude5;

        double RMappingRadiusAtStation = constants.getMappingRadiusAtLat() - u;

        double deltaLng = lngAtcm - lngAts;
        double sincp    = Math.sin(Math.toRadians(latAtcp));

        double convergenceAngle = deltaLng * sincp;

        double sinCA = Math.sin(Math.toRadians(convergenceAngle));

        double E1 = RMappingRadiusAtStation * sinCA;

        double easting = E1 + constants.getFalseEasting();

        double tanCA = Math.tan(Math.toRadians(convergenceAngle / 2.));
        double N1 = u + (E1 * tanCA);

        double northing = N1 + constants.getFalseNorthing();

        double F1 = constants.getF1();
        double F2 = constants.getF2();
        double F3 = constants.getF3();

        double u2 = u*u;
        double u3 = u*u2;
        double scaleFactor = F1 + (F2 * u2) + (F3 * u3);

        super.setNorthing(northing);
        super.setEasting(easting);
        super.setScaleFactor(scaleFactor);
        super.setConvergenceAngle(convergenceAngle);



    }


    private void convertWithMercator(GBCoordinateWGS84 coordinateWGS84,
                                    GBCoordinateConstants constants){

        double e2 = constants.getEccentricity2();
        double major = constants.getSemiMajorAxis();
        double minor = constants.getSemiMinorAxis();

        double n = ((major - minor) / (major + minor));
        double n2 = n*n;
        double n3 = n*n2;
        double n4 = n*n3;

        double radiusOfRectifyingSphere = ( major *
                                            (1. - n) *
                                            (1. - n2) *
                                            (1. + ((9.*n2)/4.) + ((225.*n4)/64.) ));


        //Convert degrees to radians (radians = degrees * pi/180)
        double latAts  = Math.toRadians(coordinateWGS84.getLatitude());
        double latAtcp = Math.toRadians(constants.getCentralParallel());

        double lngAts  = Math.toRadians(coordinateWGS84.getLongitude());
        double lngAtcm = Math.toRadians(constants.getCentralMeridian());

        double rectifyingLatitudeAtCP = rectifyingLatitude(latAtcp, constants);

        double rectifyingLatitudeAtst = rectifyingLatitude(latAts,  constants);

        double cosLat = Math.cos(latAts);
        double L = (lngAts - lngAtcm) * cosLat;




        double meridionalDistanceCp = constants.getGridScaleFactor() *
                                        rectifyingLatitudeAtCP *
                                        radiusOfRectifyingSphere;
        double meridionalDistanceSt = constants.getGridScaleFactor() *
                                        rectifyingLatitudeAtst *
                                        radiusOfRectifyingSphere;

        double sinLat  = Math.sin(latAts);
        double sin2Lat = sinLat * sinLat;
        double RMappingRadiusSt = ((constants.getGridScaleFactor() * major) /
                                  ( Math.sqrt(1 - (constants.getEccentricity2() * sin2Lat))));

        double tanLat = Math.tan(latAts);
        double tan2Lat = tanLat * tanLat;
        double tan4Lat = (tan2Lat * tan2Lat);
        double tan6Lat = tan4Lat * tan2Lat;


        //A4 does not look right. I bet it is 4*tan2Lat NOT 4*n2
        double A1 = -RMappingRadiusSt;
        double A2 = (1./2.)    * ( RMappingRadiusSt * tanLat);
        double A3 = (1./6.)    * ( 1. -    tan2Lat       +            n2 );
        double A4 = (1./12.)   * ( 5. -     tan2Lat      +           (n2 * (9. +   (4.*n2))));
        double A5 = (1./120.)  * ( 5. -  (18. * tan2Lat) + tan4Lat + (n2 * (14. -  (58. * tan2Lat))));
        double A6 = (1./360.)  * ((61. - (58. * tan2Lat) + tan4Lat + (n2 * (270. - (330. * tan2Lat)))));
        double A7 = (1./5040.) * (61. - (479. * tan2Lat) + (179. * tan4Lat) + tan6Lat);

        double L2 = L * L;

        double northing = ( (meridionalDistanceSt - meridionalDistanceCp) +
                            (constants.getFalseNorthing())                +
                            ((A2 * L2) * ( 1. + (L2 *(A4 + (A6*L2))) )));

        double easting = ( constants.getFalseEasting() + A1 +
                        (L * (1. + (L2 * (A3 + (L2 * (A5 + (A7 * L2))))))));

        double F2 = n2 / 2.;
        // TODO: 6/20/2017 I am just guessing what F4 is, it is not in the documentation
        double F4 = F2 * F2;
        double gridScaleFactorAtSt = (constants.getGridScaleFactor() *
                                                                    (1. + ((F2 * L2)* (1. + F4*L2) )));

        double C1 = -tanLat;
        double C3 = (1. + (3.*n2) + ((2./3.)*n4));
        double C5 = 2. - (tan2Lat/15.);
        double convergenceAngle = (C1*L) * (1. + (L2*(C3 + (C5*L2))));

        super.setNorthing(northing);
        super.setEasting(easting);
        super.setScaleFactor(gridScaleFactorAtSt);
        super.setConvergenceAngle(convergenceAngle);

    }

    private double rectifyingLatitude(double lat, GBCoordinateConstants constants){
        double sinLat = Math.sin(lat);
        double cosLat = Math.cos(lat);
        double cos2Lat = cosLat * cosLat;
        double cos4Lat = cos2Lat * cos2Lat;
        double cos6Lat = cos2Lat * cos4Lat;

        return (lat + ((sinLat*cosLat) * (constants.getGPCuo()              +
                                         (constants.getGPCu2() * cos2Lat)   +
                                         (constants.getGPCu4() * cos4Lat)   +
                                         (constants.getGPCu6() * cos6Lat))));

    }



}
