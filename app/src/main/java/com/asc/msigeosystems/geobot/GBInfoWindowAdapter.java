package com.asc.msigeosystems.geobot;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.model.Marker;


/**
 * Created by Elisabeth Huhn on 12/11/2016.
 *
 * Prism4D will use Info Windows on markers to give the user feedback about
 * a meaning process that is in progress / has finished at this location
 */

class GBInfoWindowAdapter implements InfoWindowAdapter {
    private View       mInfoWindowView;
    private GBActivity mActivity;
    private int        mPrecisionDigits;

    GBInfoWindowAdapter(GBActivity activity){
        mActivity = activity;

        //Get an inflater
        LayoutInflater layoutInflater =  LayoutInflater.from(activity);
        //LayoutInflater inflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );

        mInfoWindowView = layoutInflater.inflate(R.layout.info_window_points_collect, null);
    }

    @Override
    public View getInfoContents(Marker marker) {
        CharSequence msg;
        TextView     iwTextField;
        GBCoordinateMean meanCoordinate = (GBCoordinateMean) marker.getTag();
        if (meanCoordinate == null)meanCoordinate = new GBCoordinateMean();

        GBProject openProject = GBUtilities.getInstance().getOpenProject(mActivity);



        boolean isDD      = GBGeneralSettings.isLocDD(mActivity);
        boolean isDir     = GBGeneralSettings.isDir(mActivity);
        boolean isPM      = GBGeneralSettings.isPM(mActivity);
        boolean isLngLat  = GBGeneralSettings.isLngLat(mActivity);
        boolean isEN      = GBGeneralSettings.isEN(mActivity);

        int coordType    = GBUtilities.getCoordinateTypeFromProject(openProject);
        int distUnits    = openProject.getDistanceUnits();
        int locDigOfPrecision = GBGeneralSettings.getLocPrecision(mActivity);


        //Point ID
        iwTextField = ((TextView) mInfoWindowView.findViewById(R.id.iwPointIDOutput));
        long pointID = meanCoordinate.getPointID();
        if (pointID != GBUtilities.ID_DOES_NOT_EXIST){
            int pointNumber = GBPointManager.getInstance().getPoint(openProject.getProjectID(), pointID).getPointNumber();
            msg = String.valueOf(pointNumber);

        } else {
            msg = "Point not yet created";
        }
        iwTextField.setText(msg);

        //# Raw Readings
        iwTextField = ((TextView) mInfoWindowView.findViewById(R.id.iwNumRawReadingsOutput));
        iwTextField.setText(String.valueOf(meanCoordinate.getRawReadings()));

        //# Meaned Readings
        iwTextField = ((TextView) mInfoWindowView.findViewById(R.id.iwNumMeanedReadingsOutput));
        iwTextField.setText(String.valueOf(meanCoordinate.getMeanedReadings()));

        //# Fixed Readings
        iwTextField = ((TextView) mInfoWindowView.findViewById(R.id.iwNumFixedReadingsOutput));
        iwTextField.setText(String.valueOf(meanCoordinate.getFixedReadings()));

        //# Satellites
        iwTextField = ((TextView) mInfoWindowView.findViewById(R.id.iwNumSatellitesOutput));
        iwTextField.setText(String.valueOf(meanCoordinate.getSatellites()));


        //Latitude
        TextView latLabelTV = ((TextView) mInfoWindowView.findViewById(R.id.iwLatLabel));
        TextView latDirTV  = ((TextView) mInfoWindowView.findViewById(R.id.iwLatDir));
        TextView latDDTV   = ((TextView) mInfoWindowView.findViewById(R.id.iwLatDD));
        TextView latDegTV  = ((TextView) mInfoWindowView.findViewById(R.id.iwLatDeg));
        TextView latMinTV  = ((TextView) mInfoWindowView.findViewById(R.id.iwLatMin));
        TextView latSecTV  = ((TextView) mInfoWindowView.findViewById(R.id.iwLatSec));



        //Longitude
        TextView lngLabelTV = ((TextView) mInfoWindowView.findViewById(R.id.iwLngLabel));
        TextView lngDirTV  = ((TextView) mInfoWindowView.findViewById(R.id.iwLngDir));
        TextView lngDDTV   = ((TextView) mInfoWindowView.findViewById(R.id.iwLngDD));
        TextView lngDegTV  = ((TextView) mInfoWindowView.findViewById(R.id.iwLngDeg));
        TextView lngMinTV  = ((TextView) mInfoWindowView.findViewById(R.id.iwLngMin));
        TextView lngSecTV  = ((TextView) mInfoWindowView.findViewById(R.id.iwLngSec));



        if (((coordType == GBCoordinate.sLLWidgets) && isLngLat) ||
            ((coordType == GBCoordinate.sNEWidgets)  && isEN)){
            //Switch the order of the fields on the UI

            //Latitude
            latLabelTV = ((TextView) mInfoWindowView.findViewById(R.id.iwLngLabel));
            latDirTV  = ((TextView) mInfoWindowView.findViewById(R.id.iwLngDir));
            latDDTV   = ((TextView) mInfoWindowView.findViewById(R.id.iwLngDD));
            latDegTV  = ((TextView) mInfoWindowView.findViewById(R.id.iwLngDeg));
            latMinTV  = ((TextView) mInfoWindowView.findViewById(R.id.iwLngMin));
            latSecTV  = ((TextView) mInfoWindowView.findViewById(R.id.iwLngSec));



            //Longitude
            lngLabelTV = ((TextView) mInfoWindowView.findViewById(R.id.iwLatLabel));
            lngDirTV  = ((TextView) mInfoWindowView.findViewById(R.id.iwLatDir));
            lngDDTV   = ((TextView) mInfoWindowView.findViewById(R.id.iwLatDD));
            lngDegTV  = ((TextView) mInfoWindowView.findViewById(R.id.iwLatDeg));
            lngMinTV  = ((TextView) mInfoWindowView.findViewById(R.id.iwLatMin));
            lngSecTV  = ((TextView) mInfoWindowView.findViewById(R.id.iwLatSec));

        }


        //Elevation
        TextView elevationTV = ((TextView) mInfoWindowView.findViewById(R.id.iwElevationOutput));


        if (coordType == GBCoordinate.sLLWidgets) {
            latLabelTV.setText(mActivity.getString(R.string.iwcp_latitude_label));
            lngLabelTV.setText(mActivity.getString(R.string.iwcp_longitude_label));

            if (isPM){
                latDirTV.setVisibility(View.GONE);
                lngDirTV.setVisibility(View.GONE);
            } else {
                latDirTV.setVisibility(View.VISIBLE);
                lngDirTV.setVisibility(View.VISIBLE);
            }
            if (isDD) {
                double latitude = meanCoordinate.getLatitude();
                double longitude = meanCoordinate.getLongitude();
                int posHemi = R.string.hemisphere_N;
                int negHemi = R.string.hemisphere_S;
                GBUtilities.locDD(mActivity, latitude, locDigOfPrecision,
                        isDir, posHemi, negHemi,
                        latDirTV, latDDTV);

                posHemi = R.string.hemisphere_E;
                negHemi = R.string.hemisphere_W;
                GBUtilities.locDD(mActivity, longitude, locDigOfPrecision,
                        isDir, posHemi, negHemi,
                        lngDirTV, lngDDTV);

                latDDTV.setVisibility(View.VISIBLE);
                lngDDTV.setVisibility(View.VISIBLE);

                latDegTV.setVisibility(View.GONE);
                latMinTV.setVisibility(View.GONE);
                latDegTV.setVisibility(View.GONE);
                lngDegTV.setVisibility(View.GONE);
                lngMinTV.setVisibility(View.GONE);
                lngDegTV.setVisibility(View.GONE);

            } else {
                int deg = meanCoordinate.getLatitudeDegree();
                int min = meanCoordinate.getLatitudeMinute();
                double sec = meanCoordinate.getLatitudeSecond();
                int posHemi = R.string.hemisphere_N;
                int negHemi = R.string.hemisphere_S;

                GBUtilities.locDMS(mActivity, deg, min, sec, locDigOfPrecision,
                                    isDir, posHemi, negHemi, latDirTV,
                                    latDegTV, latMinTV, latSecTV);

                deg = meanCoordinate.getLongitudeDegree();
                min = meanCoordinate.getLongitudeMinute();
                sec = meanCoordinate.getLongitudeSecond();
                posHemi = R.string.hemisphere_E;
                negHemi = R.string.hemisphere_W;

                GBUtilities.locDMS(mActivity, deg, min, sec, locDigOfPrecision,
                        isDir, posHemi, negHemi, lngDirTV,
                        lngDegTV, lngMinTV, lngSecTV);

                latDDTV.setVisibility(View.GONE);
                lngDDTV.setVisibility(View.GONE);

                latDegTV.setVisibility(View.VISIBLE);
                latMinTV.setVisibility(View.VISIBLE);
                latDegTV.setVisibility(View.VISIBLE);
                lngDegTV.setVisibility(View.VISIBLE);
                lngMinTV.setVisibility(View.VISIBLE);
                lngDegTV.setVisibility(View.VISIBLE);

            }
            double elevation = meanCoordinate.getElevation();
            GBUtilities.locDistance(mActivity, elevation, elevationTV);

        }else {
            //Easting / Northing coordinates
            latLabelTV.setText(mActivity.getString(R.string.iwcp_northing_label));
            lngLabelTV.setText(mActivity.getString(R.string.iwcp_easting_label));

            double easting   = meanCoordinate.getEasting();
            double northing  = meanCoordinate.getNorthing();
            double elevation = meanCoordinate.getElevation();

            GBUtilities.locDistance(mActivity, northing,  latDDTV);
            GBUtilities.locDistance(mActivity, easting,   lngDDTV);
            GBUtilities.locDistance(mActivity, elevation, elevationTV);

            latDirTV.setVisibility(View.GONE);
            lngDirTV.setVisibility(View.GONE);

            latDDTV.setVisibility(View.VISIBLE);
            lngDDTV.setVisibility(View.VISIBLE);

            latDegTV.setVisibility(View.GONE);
            latMinTV.setVisibility(View.GONE);
            latDegTV.setVisibility(View.GONE);
            lngDegTV.setVisibility(View.GONE);
            lngMinTV.setVisibility(View.GONE);
            lngDegTV.setVisibility(View.GONE);
        }


        //Default is RMS in the layout
        int hLabel = R.string.hrms_label;
        int vLabel = R.string.vrms_label;
        int eLabel = R.string.ele_rms_label;

        if (GBGeneralSettings.isStdDev(mActivity)) {

            //Standard Deviation

            if (coordType == GBCoordinate.sNEWidgets) {
                hLabel = R.string.ning_sigma_label;
                vLabel = R.string.eing_sigma_label;
                eLabel = R.string.elev_sigma_label;

            } else {
                hLabel = R.string.lng_sigma_label;
                vLabel = R.string.lat_sigma_label;
                eLabel = R.string.elev_sigma_label;
            }

        }

        iwTextField = ((TextView) mInfoWindowView.findViewById(R.id.iwSdLatNorLabel));
        iwTextField.setText(mActivity.getString(hLabel));
        iwTextField = ((TextView) mInfoWindowView.findViewById(R.id.iwSdLongEastLabel));
        iwTextField.setText(mActivity.getString(vLabel));
        iwTextField = ((TextView) mInfoWindowView.findViewById(R.id.iwSdElevationLabel));
        iwTextField.setText(mActivity.getString(eLabel));

        locDigOfPrecision = GBGeneralSettings.getStdDevPrecision(mActivity);

        //Standard Deviation Latitude
        iwTextField = ((TextView) mInfoWindowView.findViewById(R.id.iwSdLatNorOutput));
        iwTextField.setText(truncatePrecisionString(meanCoordinate.getLatitudeStdDev(), locDigOfPrecision));

        //Standard Deviation Longitude
        iwTextField = ((TextView) mInfoWindowView.findViewById(R.id.iwSdLongEastOutput));
        iwTextField.setText(truncatePrecisionString(meanCoordinate.getLongitudeStdDev(), locDigOfPrecision));

        //Standard Deviation Elevation
        iwTextField = ((TextView) mInfoWindowView.findViewById(R.id.iwSdElevationOutput));
        iwTextField.setText(truncatePrecisionString(meanCoordinate.getElevationStdDev(), locDigOfPrecision));


        return mInfoWindowView;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        // TODO Write method if ever needed
        return null;
    }

    private String truncatePrecisionString(double reading, int digitsOfPrecision) {
        return GBUtilities.truncatePrecisionString(reading, digitsOfPrecision);
    }

}
