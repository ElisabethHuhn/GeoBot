package com.asc.msigeosystems.geobot;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.model.Marker;

import java.math.BigDecimal;
import java.math.RoundingMode;


/**
 * Created by Elisabeth Huhn on 12/11/2016.
 *
 * Prism4D will use Info Windows on markers to give the user feedback about
 * a meaning process that is in progress / has finished at this location
 */

class GBInfoWindowAdapter implements InfoWindowAdapter {
    private View       mInfoWindowView;
    private GBActivity mActivity;

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

        //project has to be open
        int precisionDigits =
                GBUtilities.getInstance().getOpenProject(mActivity).getSettings().getLocationPrecision();


        //Point ID
        iwTextField = ((TextView) mInfoWindowView.findViewById(R.id.iwPointIDOutput));
        long pointID = meanCoordinate.getPointID();
        if (pointID >= 1){
            msg = String.valueOf(pointID);
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
        double locationTemp = meanCoordinate.getLatitude();
        BigDecimal bdTemp   =
                       new BigDecimal(locationTemp).setScale(precisionDigits, RoundingMode.HALF_UP);
        locationTemp        = bdTemp.doubleValue();
        TextView latitudeTV = ((TextView) mInfoWindowView.findViewById(R.id.iwLatNorOutput));
        latitudeTV.setText(String.valueOf(locationTemp));

        //Longitude
        locationTemp = meanCoordinate.getLongitude();
        bdTemp       = new BigDecimal(locationTemp).setScale(precisionDigits, RoundingMode.HALF_UP);
        locationTemp = bdTemp.doubleValue();
        TextView longitudeTV = ((TextView) mInfoWindowView.findViewById(R.id.iwLongEastOutput));
        longitudeTV.setText(String.valueOf(locationTemp));

        //Elevation
        locationTemp = meanCoordinate.getElevation();
        bdTemp       = new BigDecimal(locationTemp).setScale(precisionDigits, RoundingMode.HALF_UP);
        locationTemp = bdTemp.doubleValue();
        TextView elevationTV = ((TextView) mInfoWindowView.findViewById(R.id.iwElevationOutput));
        elevationTV.setText(String.valueOf(locationTemp));


        precisionDigits = GBUtilities.getInstance()
                                       .getOpenProject(mActivity).getSettings().getStdDevPrecision();


        //Standard Deviation Latitude
        locationTemp = meanCoordinate.getLatitudeStdDev();
        bdTemp       = new BigDecimal(locationTemp).setScale(precisionDigits, RoundingMode.HALF_UP);
        locationTemp = bdTemp.doubleValue();
        iwTextField = ((TextView) mInfoWindowView.findViewById(R.id.iwSdLatNorOutput));
        iwTextField.setText(String.valueOf(locationTemp));

        //Standard Deviation Longitude
        locationTemp = meanCoordinate.getLongitudeStdDev();
        bdTemp       = new BigDecimal(locationTemp).setScale(precisionDigits, RoundingMode.HALF_UP);
        locationTemp = bdTemp.doubleValue();
        iwTextField = ((TextView) mInfoWindowView.findViewById(R.id.iwSdLongEastOutput));
        iwTextField.setText(String.valueOf(locationTemp));

        //Standard Deviation Elevation
        locationTemp = meanCoordinate.getElevationStdDev();
        bdTemp       = new BigDecimal(locationTemp).setScale(precisionDigits, RoundingMode.HALF_UP);
        locationTemp = bdTemp.doubleValue();
        iwTextField = ((TextView) mInfoWindowView.findViewById(R.id.iwSdElevationOutput));
        iwTextField.setText(String.valueOf(locationTemp));

        //HRMS
        locationTemp = meanCoordinate.getHrms();
        bdTemp       = new BigDecimal(locationTemp).setScale(precisionDigits, RoundingMode.HALF_UP);
        locationTemp = bdTemp.doubleValue();
        iwTextField =  ((TextView) mInfoWindowView.findViewById(R.id.iwHrmsOutput));
        iwTextField.setText(String.valueOf(locationTemp));

        //VRMS
        locationTemp = meanCoordinate.getVrms();
        bdTemp       = new BigDecimal(locationTemp).setScale(precisionDigits, RoundingMode.HALF_UP);
        locationTemp = bdTemp.doubleValue();
        iwTextField = ((TextView) mInfoWindowView.findViewById(R.id.iwVrmsOutput));
        iwTextField.setText(String.valueOf(locationTemp));

        return mInfoWindowView;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        // TODO Write method if ever needed
        return null;
    }


}
