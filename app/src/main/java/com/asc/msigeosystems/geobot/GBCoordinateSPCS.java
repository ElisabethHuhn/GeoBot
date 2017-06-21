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


    static final double E0 = 2000000.0000; //meters Easting of projection and grid origion
    static final double E0feet = 6561666.667; //E0 in feet
    static final double Nb = 500000.0000;  //meters northing of the grid base
    static final double Nbfeet = 1640416.667; //feet





    private static final double L1 = 110950.2019;
    private static final double L2 = 9.25072;
    private static final double L3 = 5.64572;
    private static final double L4 = 0.017374;
    private static final double L5 = 0.0;

    private static final double n = (
                                    (GBUtilities.sSemiMajorRadius - GBUtilities.sSemiMinorRadius) /
                                    (GBUtilities.sSemiMajorRadius + GBUtilities.sSemiMinorRadius));

    private static final double radiusOfRectifyingSphere = (
                                                    GBUtilities.sSemiMajorRadius *
                                                    (1 - n) *
                                                    (1 - (n*n)) *
                                                    (1 + ((9*(n*n))/4) + ((225*(n*n*n*n))/64) ));

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

    GBCoordinateSPCS(GBCoordinateWGS84 wsg84Coordinate, int zone){
        // TODO: 12/14/2016 complete conversion from WSG84 to SPCS

        initializeDefaultVariables();

        GBSpcsConstants constants = new GBSpcsConstants(zone);
        int spcsZone = constants.getZone();
        if (spcsZone == (int)GBUtilities.ID_DOES_NOT_EXIST) return;

        super.setZone(spcsZone);
        setState(constants.getState());

        if (spcsZone == GBSpcsConstants.sLAMBERT){
            convertWithLambert(wsg84Coordinate, constants);
        } else {
            convertWithMercator(wsg84Coordinate, constants);
        }
    }

/*-**************************************************************

 //Conversion Routines are called by the constructors
    Prism4DCoordinateSPCS(Prism4DCoordinateWGS84 coordinate) {
        //initialize all variables to their defaults
        super.initializeDefaultVariables();
        convertWGSToSPCS(coordinate.getLatitude(), coordinate.getLongitude());
        mDatum = "WGS84"; //eg WGS84
    }

    Prism4DCoordinateSPCS(Prism4DCoordinateNAD83 coordinate) {
        //initialize all variables to their defaults
        super.initializeDefaultVariables();
        convertNADtoSPCS(coordinate.getLatitude(), coordinate.getLongitude());
        mDatum = "NAD83"; //eg NAD83
    }

    private void convertWGStoUTM (double lat, double longi) throws IllegalArgumentException {
        setWgsConstants(); //use the WGS constants for the conversion
        convertLLtoSPCS(lat, longi);
    }

    private void convertNADtoSPCS (double lat, double longi) throws IllegalArgumentException {
        setNadConstants(); //use the NAD constants for the conversion
        convertLLtoSPCS(lat, longi); //on superclass
    }

 ****************************************************************/



    protected void initializeDefaultVariables(){
        //set all variables with defaults, so that none are null
        //I know that one does not have to initialize int's etc, but
        //to be explicit about the initialization, do it anyway

        //initialize all variables common to EN coordinates
        super.initializeDefaultVariables();

        //initialize all variables from this level
        mThisCoordinateType  = sCoordinateTypeSPCS;
        mThisCoordinateClass = sCoordinateTypeClassSPCS;


    }



    //+*******************************************************************/
    //+**********     Instance Methods                          **********/
    //+*******************************************************************/



    private void someFunctions(double B, double L){ //B = Latitude, L = Longitude


        double a = 6378137.;       //Semimajor axis of geodetic ellipsoid - equitorial radius
        double b = 6356752.314245; //Semiminor axis of the geodetic ellipsoid - polar radius
        double f = (a - b) / a;    // f = flattening
        double e = Math.sqrt((2*f) - (f*f)); //First eccentricity of geodetic ellipsoid sqrt (2f-f squared)
        double e_sq      = e*e;

        double K0 = 0.9996;       // scale on the central meridian

        //B = Parallel of geodetic latitude, North is positive
         double Bs; //Southern standard parallel
        // TODO: 6/17/2017 remove this hack put in to get rid of errors in the short term

        Bs = 1.;

        double sin_Bs    = Math.sin(Bs);
        double sin_Bs_sq = sin_Bs * sin_Bs;
        double cos_Bs    = Math.cos(Bs);

        double Bn; //Northern standarad parallel
        // TODO: 6/17/2017 Remove hack to surpress errors in the short term

        Bn = 1.;

        double sin_Bn    = Math.sin(Bn);
        double sin_Bn_sq = sin_Bn * sin_Bn;
        double cos_Bn    = Math.cos(Bn);

        double Bb; //Latitude of the grid origin
        // TODO: 6/17/2017 remove hack

        Bb = 1.;
        double sin_Bb    = Math.sin(Bb);

        double Bo; //Central parallel, the latitude of the true projection origin
        //double sin_Bo    = Math.sin(Bo);
        // TODO: 6/17/2017 hack

        Bo = 1.;

        //L = Meridian of geodetic longitude, West is positive
        double Lo; //Central meridian. Longitude of the true and grid origin
        // TODO: 6/17/2017 hack

        Lo = 1.;


        /*-****************************************************************/
        /*                Zone Constants                                  */
        /*-****************************************************************/

        double Qs_Term1 = Math.log(     (1.+ sin_Bs)   /   (1.-sin_Bs)    );
        double Qs_Term2 = Math.log(   (1.+ (e*sin_Bs)) /(1.- (e*sin_Bs))  );
        double Qs = (Qs_Term1 - (e*Qs_Term2))/2.;

        double Qn_Term1 = Math.log(     (1.+ sin_Bn)   /   (1.-sin_Bn)    );
        double Qn_Term2 = Math.log(   (1.+ (e*sin_Bn)) /(1.- (e*sin_Bn))  );
        double Qn       = (Qn_Term1 - (e*Qn_Term2))/2.;

        double Qb_Term1 = Math.log(     (1.+ sin_Bb)   /   (1.-sin_Bb)    );
        double Qb_Term2 = Math.log(   (1.+ (e*sin_Bb)) /(1.- (e*sin_Bb))  );
        double Qb       = (Qb_Term1 - (e*Qb_Term2))/2.;


        double Ws       = Math.sqrt(1. - (e_sq * sin_Bs_sq));
        double Wn       = Math.sqrt(1. - (e_sq * sin_Bn_sq));


        double sin_Bo  = Math.log( (Wn * cos_Bs) / (Ws * cos_Bn) ) / (Qn - Qs);
        double sin_Bo_sq = sin_Bo * sin_Bo;

        double Qo_Term1 = Math.log(     (1.+ sin_Bo)   /   (1.-sin_Bo)    );
        double Qo_Term2 = Math.log(   (1.+ (e*sin_Bo)) /(1.- (e*sin_Bo))  );
        double Qo       = (Qo_Term1 - (e*Qo_Term2))/2.;

        double Wo       = Math.sqrt(1. - (e_sq * sin_Bo_sq));

        //K = Mapping radius at the equator
        double K_term1 = (a * cos_Bs * Math.exp(Qs * sin_Bo)) / (Ws * sin_Bo);
        double K_term2 = (a * cos_Bn * Math.exp(Qn * sin_Bo)) / (Wn * sin_Bo);
        double K       = K_term1 - K_term2;

        // R = Mapping radius at indicated latitude B
        double Rb = K / (Math.exp(Qb * sin_Bo));
        double Ro = K / (Math.exp(Qo * sin_Bo));

        //k = Grid scale factor at general point
        double ko = (Wo * Math.tan(Bo * Ro)) / a;

        double No = Rb + Nb - Ro;
        double Eo;
        // TODO: 6/17/2017 hack

        Eo = 1.;



        /*-****************************************************************/
        /*               Direct Conversion Computation                    */
        /*-****************************************************************/

        /*-****************************************************************
         *   Conversion:
         *   Inputs:  (B, L) - geodetic coordinates(latitude, longitude) to
         *   Outputs: (N, E) - Lambert Grid Coordinates
         *   with convergence angle C
         *   and  grid scale factor k
         ******************************************************************/

        double sin_B = Math.sin(B);
        double sin_B_sq = sin_B * sin_B;
        double cos_B = Math.cos(B);

        double Q_Term1 = Math.log(     (1.+ sin_B)   /   (1.-sin_B)    );
        double Q_Term2 = Math.log(   (1.+ (e*sin_B)) /(1.- (e*sin_B))  );
        double Q = (Q_Term1 - (e*Q_Term2))/2.;

        // R = Mapping radius at indicated latitude B
        double R = K / (Math.exp(Q * sin_B));

        //C = Convergence angle
        double C = (Lo - L) * sin_Bo;
        double cos_C = Math.cos(C);
        double sin_C = Math.sin(C);

        //Northing Coordinate
        double N = Rb + Nb - (R * cos_C);

        //Easting Coordinate
        double E = Eo + (R * sin_C);


        //k = Grid scale factor
        double k_term1 = Math.sqrt(1. - (e_sq * sin_B_sq));
        double k_term2 = (R * sin_Bo) / (a * cos_B);
        double k = k_term1 * k_term2;



        /*-****************************************************************
         *   Inverse Conversion
         *   Inputs:  (N, E) - Lambert Grid Coordinates   to
         *   Outputs: (B, L) - geodetic coordinates(latitude, longitude)
         *   with convergence angle C
         *   and  grid scale factor k
         ******************************************************************/



    }

    private void convertWithLambert(GBCoordinateWGS84 coordinateWGS84,
                                    GBSpcsConstants   constants){
        //Convert degrees to radians (radians = degrees * pi/180)
        double latAts  = coordinateWGS84.getLatitude()  * (Math.PI / 180.);
        double latAtcp = constants.getCentralParallel() * (Math.PI / 180.);

        double lngAts  = coordinateWGS84.getLongitude() * (Math.PI / 180.);
        double lngAtcm = constants.getCentralMeridian() * (Math.PI / 180.);

        double deltaLatitude = latAts - latAtcp;

        double u = L1 * deltaLatitude +
                   L2 * deltaLatitude * deltaLatitude +
                   L3 * deltaLatitude * deltaLatitude * deltaLatitude +
                   L4 * deltaLatitude * deltaLatitude * deltaLatitude * deltaLatitude;

        double RMappingRadiusAtStation = constants.getMappingRadiusAtLat() - u;

        double deltaLng = lngAtcm - lngAts;
        double sincp    = Math.sin(latAtcp);

        double convergenceAngle = deltaLng * sincp;

        double sinCA = Math.sin(convergenceAngle);
        double E1 = RMappingRadiusAtStation * sinCA;

        double easting = E1 + constants.getFalseEasting();

        double tanCA = Math.tan(convergenceAngle / 2.);
        double N1 = u + (E1 * tanCA);

        double northing = N1 + constants.getFalseNorthing();

        double F1 = .999948401424;
        double F2 = 1.23188E-14;
        // TODO: 6/20/2017 I have no idea what F3 is, it was wrong in the spreadsheet and not mentioned in the document
        double F3 = 4.54E-22;

        double u2 = u*u;
        double u3 = u*u2;
        double scaleFactor = F1 + (F2 * (u*u)) + (F3 * u3);

        super.setNorthing(northing);
        super.setEasting(easting);
        super.setScale(scaleFactor);
        super.setConvergence(convergenceAngle);

    }


    private void convertWithMercator(GBCoordinateWGS84 coordinateWGS84,
                                    GBSpcsConstants constants){

        //Convert degrees to radians (radians = degrees * pi/180)
        double latAts  = coordinateWGS84.getLatitude()  * (Math.PI / 180);
        double latAtcp = constants.getCentralParallel() * (Math.PI / 180);

        double lngAts  = coordinateWGS84.getLongitude() * (Math.PI / 180);
        double lngAtcm = constants.getCentralMeridian() * (Math.PI / 180);

        double rectifyingLatitudeAtCP = rectifyingLatitude(latAtcp, constants);

        double rectifyingLatitudeAtst = rectifyingLatitude(latAts,  constants);

        double cosLat = Math.cos(latAts);
        double L = (lngAts - lngAtcm) * cosLat;

        double meridionalDistanceCp = meridionalDistance(rectifyingLatitudeAtCP, constants);
        double meridionalDistanceSt = meridionalDistance(rectifyingLatitudeAtst, constants);

        double sinLat  = Math.sin(latAts);
        double sin2Lat = sinLat * sinLat;
        double mappingRadiusSt = ((constants.getGridScaleFactor() * GBUtilities.sSemiMajorRadius) /
                                  ( Math.sqrt(1 - (constants.getEccentricity2() * sin2Lat))));

        double tanLat = Math.tan(latAts);
        double tan2Lat = tanLat * tanLat;
        double tan4Lat = (tan2Lat * tan2Lat);
        double tan6Lat = tan4Lat * tan2Lat;
        double n2      = n*n;
        double n4      = n2 * n2;

        //A4 does not look right. I bet it is 4*tan2Lat NOT 4*n2
        double A1 = -mappingRadiusSt;
        double A2 = (1./2.)    * ( mappingRadiusSt * tanLat);
        double A3 = (1./6.)    * ( 1. -    tan2Lat       +            n2 );
        double A4 = (1./12.)   * ( 5. -     tan2Lat      +           (n2 * (9. +   (4.*n2))));
        double A5 = (1./120.)  * ( 5. -  (18. * tan2Lat) + tan4Lat + (n2 * (14. -  (58. * tan2Lat))));
        double A6 = (1./360.)  * ((61. - (58. * tan2Lat) + tan4Lat + (n2 * (270. - (330. * tan2Lat)))));
        double A7 = (1./5040.) * (61. - (479. * tan2Lat) + (179. * tan4Lat) + tan6Lat);

        double L2 = L * L;

        double northing = ( (meridionalDistanceSt - meridionalDistanceCp) +
                            (constants.getFalseNorthing())                +
                            (A2 * L2 * ( 1. + (L2 *(A4 + (A6*L2))) )));

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
        super.setScale(gridScaleFactorAtSt);
        super.setConvergence(convergenceAngle);

    }

    private double rectifyingLatitude(double lat, GBSpcsConstants constants){
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

    private double meridionalDistance(double rectifyingLatitude, GBSpcsConstants constants){
        return constants.getGridScaleFactor() * rectifyingLatitude * radiusOfRectifyingSphere;
    }


}
