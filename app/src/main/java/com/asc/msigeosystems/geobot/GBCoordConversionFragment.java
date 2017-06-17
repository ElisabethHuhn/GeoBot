package com.asc.msigeosystems.geobot;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * The Collect Fragment is the UI
 * when the user is making point measurements in the field
 * Created by Elisabeth Huhn on 4/13/2016.
 */
public class GBCoordConversionFragment extends Fragment {

    /**
     * Create variables for all the widgets
     *  although in the mockup, most will be statically defined in the xml
     */




    private GBCoordinateWGS84 mCoordinateWGS84;

    //private double mConvergence;
    //private double mScale;



    public GBCoordConversionFragment() {
        //for now, we don't need to initialize anything when the fragment
        //  is first created
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_coord_conversion, container, false);

        //Wire up the UI widgets so they can handle events later
        wireWidgets(v);

        return v;
    }


    @Override
    public void onResume() {
        super.onResume();
        setSubtitle();
    }

    private void setSubtitle(){
        ((GBActivity)getActivity())
                .setSubtitle(R.string.subtitle_coordinate_conversion);
    }


    private void wireWidgets(View v){


        //Conversion Button
        Button convertButton = (Button) v.findViewById(R.id.convertButton);
        convertButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){

                performConversion();

                Toast.makeText(getActivity(),
                        R.string.conversion_stub,
                        Toast.LENGTH_SHORT).show();

            }//End on Click
        });

        //Clear Form Button
        Button clearButton = (Button) v.findViewById(R.id.clearFormButton);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                clearForm();
            }
        });



    }

    private boolean convertInputs() {
        View v = getView();
        if (v == null)return false;

        //Latitude
        EditText latDigDegInput = (EditText) v.findViewById(R.id.latitudeInput);
        EditText latDegInput    = (EditText) v.findViewById(R.id.latDegreesInput);
        EditText latMinInput    = (EditText) v.findViewById(R.id.latMinutesInput);
        EditText latSecInput    = (EditText) v.findViewById(R.id.latSecondsInput);

        //Longitude
        EditText longDigDegInput = (EditText) v.findViewById(R.id.longitudeInput);
        EditText longDegInput    = (EditText) v.findViewById(R.id.longDegreesInput);
        EditText longMinInput    = (EditText) v.findViewById(R.id.longMinutesInput);
        EditText longSecInput    = (EditText) v.findViewById(R.id.longSecondsInput);

        mCoordinateWGS84 = new GBCoordinateWGS84(latDigDegInput.getText().toString(),
                                                      longDigDegInput.getText().toString());

        if (!mCoordinateWGS84.isValidCoordinate()) {
            mCoordinateWGS84 = new GBCoordinateWGS84(latDegInput.getText(),
                                                          latMinInput.getText(),
                                                          latSecInput.getText(),
                                                          longDegInput.getText(),
                                                          longMinInput.getText(),
                                                          longSecInput.getText());
            if (!mCoordinateWGS84.isValidCoordinate()){
                Toast.makeText(getActivity(),
                        R.string.coordinate_try_again,
                        Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        //display the coordinate values in the UI
        latDigDegInput.setText(String.valueOf(mCoordinateWGS84.getLatitude()));
        latDegInput   .setText(String.valueOf(mCoordinateWGS84.getLatitudeDegree()));
        latMinInput   .setText(String.valueOf(mCoordinateWGS84.getLatitudeMinute()));
        latSecInput   .setText(String.valueOf(mCoordinateWGS84.getLatitudeSecond()));

        longDigDegInput.setText(String.valueOf(mCoordinateWGS84.getLongitude()));
        longDegInput   .setText(String.valueOf(mCoordinateWGS84.getLongitudeDegree()));
        longMinInput   .setText(String.valueOf(mCoordinateWGS84.getLongitudeMinute()));
        longSecInput   .setText(String.valueOf(mCoordinateWGS84.getLongitudeSecond()));

        setLatColor();
        setLongColor();

        return true;

    }

    private void performConversion() {


        /*
         * Compare three conversion routines:
         * 1) IBM
         * 2) Stack Overflow
         * 3) GB developed from scratch, based on Karney (2010)
         *
         *
         *
         */

        //"Legal ranges: latitude [-90,90], longitude [-180,180).");
        boolean inputsValid = convertInputs() ;

        //only attempt the conversions if the inputs are valid
         if (inputsValid) {
            //The IBM code:
             inputsValid = convertIBM();
        }

        if (inputsValid) {
            //The stack overflow conversion code:
            inputsValid = convertStackOverflow();

        }
        if (inputsValid){
            //Create the UTM coordinate based on the WSG coordinate from the user
            // The GB conversion
            // algorithm based on Kearny (2010)
            // supposed nanometer accuracy
            inputsValid = convertKarney();
        }

    }

    private boolean convertIBM() {
        View v = getView();
        if (v == null)return false;
        TextView utmIntegerOutput = (TextView) v.findViewById(R.id.utmIntgerOutput);

        try {
            //an actual conversion
            IBMCoordinateConversion coordinateConversion = new IBMCoordinateConversion();

            //integer precision (meter)
            String utmStringCoordinates =
                    coordinateConversion.latLon2UTM(mCoordinateWGS84.getLatitude(),
                            mCoordinateWGS84.getLongitude());
            utmIntegerOutput.setText(utmStringCoordinates);
            return true;

        } catch (IllegalArgumentException exc) {
            //input parameters were not within range
            EditText latDigDegInput = (EditText) v.findViewById(R.id.latitudeInput);
            EditText longDigDegInput = (EditText) v.findViewById(R.id.longitudeInput);

            latDigDegInput.setText(R.string.input_wrong_range_error);
            longDigDegInput.setText(R.string.input_wrong_range_error);
            return false;
        }
    }

    private boolean convertStackOverflow() {
        View v = getView();
        if (v == null)return false;
        TextView utmSOOutput =      (TextView) v.findViewById(R.id.utmSOOutput);


        try {
            //create a new WGS84 object from the Lat / Long coordinates
            GBCoordinateWGS84 latlong = new GBCoordinateWGS84(
                                                                    mCoordinateWGS84.getLatitude(),
                                                                    mCoordinateWGS84.getLongitude());

            //Now convert the coordinates into UTM
            //There is not very robust exception handling in this class
            GBCoordinateUTM utmCoordinates = new GBCoordinateUTM(latlong);

            utmSOOutput.setText(utmCoordinates.toString());
            return true;
        } catch (IllegalArgumentException exc) {
            //input parameters were not within range
            EditText latDigDegInput = (EditText) v.findViewById(R.id.latitudeInput);
            EditText longDigDegInput = (EditText) v.findViewById(R.id.longitudeInput);

            latDigDegInput.setText(R.string.input_wrong_range_error);
            longDigDegInput.setText(R.string.input_wrong_range_error);
            return false;
        }
    }

    private boolean convertKarney(){
        View v = getView();
        if (v == null)return false;

        TextView utmNmZoneOutput       =  (TextView) v.findViewById(R.id.utm_zone);
        TextView utmNmLatBandOutput    =  (TextView) v.findViewById(R.id.utm_latband);
        TextView utmNmHemisphereOutput =  (TextView) v.findViewById(R.id.utm_hemisphere);
        TextView utmNmEastingMOutput   =  (TextView) v.findViewById(R.id.utm_easting_meters);
        TextView utmNmNorthingMOutput  =  (TextView) v.findViewById(R.id.utm_northing_meters);
        TextView utmNmEastingFOutput   =  (TextView) v.findViewById(R.id.utm_easting_feet);
        TextView utmNmNorthingFOutput  =  (TextView) v.findViewById(R.id.utm_northing_feet);


        try{
            //The UTM constructor performs the conversion from WGS84
            GBCoordinateUTM utmCoordinate = new GBCoordinateUTM(mCoordinateWGS84);

            //Also output the result in separate fields
            utmNmZoneOutput      .setText(String.valueOf(utmCoordinate.getZone()));
            utmNmHemisphereOutput.setText(String.valueOf(utmCoordinate.getHemisphere()));
            utmNmLatBandOutput   .setText(String.valueOf(utmCoordinate.getLatBand()));
            utmNmEastingMOutput  .setText(String.valueOf(utmCoordinate.getEasting()));
            utmNmNorthingMOutput .setText(String.valueOf(utmCoordinate.getNorthing()));

            //convert meters to feet
            double temp = utmCoordinate.getEastingFeet();
            //and round to a reasonable precision
            BigDecimal bdTemp = new BigDecimal(temp).setScale(6, RoundingMode.HALF_UP);
            temp = bdTemp.doubleValue();
            utmNmEastingFOutput.setText(String.valueOf(temp));

            temp = utmCoordinate.getNorthingFeet();
            bdTemp = new BigDecimal(temp).setScale(GBUtilities.sMicrometerDigitsOfPrecision,
                                                   RoundingMode.HALF_UP);
            temp = bdTemp.doubleValue();

            utmNmNorthingFOutput.setText(String.valueOf(temp ));
            return true;

        } catch (IllegalArgumentException exc) {
            //input parameters were not within range
            EditText latDigDegInput = (EditText) v.findViewById(R.id.latitudeInput);
            EditText longDigDegInput = (EditText) v.findViewById(R.id.longitudeInput);

            latDigDegInput.setText(R.string.input_wrong_range_error);
            longDigDegInput.setText(R.string.input_wrong_range_error);
            return false;
        }
    }


    private void clearForm() {
        View v = getView();
        if (v == null)return;
        //Latitude
        EditText latDigDegInput = (EditText) v.findViewById(R.id.latitudeInput);
        EditText latDegInput    = (EditText) v.findViewById(R.id.latDegreesInput);
        EditText latMinInput    = (EditText) v.findViewById(R.id.latMinutesInput);
        EditText latSecInput    = (EditText) v.findViewById(R.id.latSecondsInput);

        //Longitude
        EditText longDigDegInput = (EditText) v.findViewById(R.id.longitudeInput);
        EditText longDegInput    = (EditText) v.findViewById(R.id.longDegreesInput);
        EditText longMinInput    = (EditText) v.findViewById(R.id.longMinutesInput);
        EditText longSecInput    = (EditText) v.findViewById(R.id.longSecondsInput);

        //UTM
        TextView utmIntegerOutput = (TextView) v.findViewById(R.id.utmIntgerOutput);
        TextView utmSOOutput =      (TextView) v.findViewById(R.id.utmSOOutput);

        TextView utmNmZoneOutput       =  (TextView) v.findViewById(R.id.utm_zone);
        TextView utmNmLatBandOutput    =  (TextView) v.findViewById(R.id.utm_latband);
        TextView utmNmHemisphereOutput =  (TextView) v.findViewById(R.id.utm_hemisphere);
        TextView utmNmEastingMOutput   =  (TextView) v.findViewById(R.id.utm_easting_meters);
        TextView utmNmNorthingMOutput  =  (TextView) v.findViewById(R.id.utm_northing_meters);
        TextView utmNmEastingFOutput   =  (TextView) v.findViewById(R.id.utm_easting_feet);
        TextView utmNmNorthingFOutput  =  (TextView) v.findViewById(R.id.utm_northing_feet);



        latDigDegInput.setText("");
        latDegInput   .setText("");
        latMinInput   .setText("");
        latSecInput   .setText("");

        longDigDegInput.setText("");
        longDegInput   .setText("");
        longMinInput   .setText("");
        longSecInput   .setText("");

        utmIntegerOutput.setText("");
        utmSOOutput.     setText("");

        utmNmZoneOutput.      setText(R.string.utm_zone_label);
        utmNmLatBandOutput.   setText(R.string.utm_latband_label);
        utmNmHemisphereOutput.setText(R.string.utm_hemisphere_label);
        utmNmEastingMOutput.  setText(R.string.utm_easting_label);
        utmNmNorthingMOutput. setText(R.string.utm_northing_label);
        utmNmEastingFOutput.  setText(R.string.utm_easting_label);
        utmNmNorthingFOutput. setText(R.string.utm_northing_label);
        setLatColorPos();
        setLongColorPos();
    }

    private void setLatColor(){
        if (mCoordinateWGS84.getLatitude() >= 0.0) {
            setLatColorPos();

        } else {
            setLatColorNeg();
        }
    }

    private void setLongColor(){
        if (mCoordinateWGS84.getLongitude() >= 0.0) {
            setLongColorPos();

        } else {
            setLongColorNeg();

        }
    }

    private void setLatColorNeg(){
        View v = getView();
        if (v == null)return;

        //Latitude
        EditText latDigDegInput = (EditText) v.findViewById(R.id.latitudeInput);
        EditText latDegInput    = (EditText) v.findViewById(R.id.latDegreesInput);
        EditText latMinInput    = (EditText) v.findViewById(R.id.latMinutesInput);
        EditText latSecInput    = (EditText) v.findViewById(R.id.latSecondsInput);


        latDigDegInput.setTextColor(
                ContextCompat.getColor(getActivity(),R.color.colorNegNumber));
        latDegInput.setTextColor(
                ContextCompat.getColor(getActivity(),R.color.colorNegNumber));
        latMinInput.setTextColor(
                ContextCompat.getColor(getActivity(),R.color.colorNegNumber));
        latSecInput.setTextColor(
                ContextCompat.getColor(getActivity(),R.color.colorNegNumber));

    }

    private void setLatColorPos(){
        View v = getView();
        if (v == null)return;

        //Latitude
        EditText latDigDegInput = (EditText) v.findViewById(R.id.latitudeInput);
        EditText latDegInput    = (EditText) v.findViewById(R.id.latDegreesInput);
        EditText latMinInput    = (EditText) v.findViewById(R.id.latMinutesInput);
        EditText latSecInput    = (EditText) v.findViewById(R.id.latSecondsInput);


        latDigDegInput.setTextColor(
                ContextCompat.getColor(getActivity(),R.color.colorPosNumber));
        latDegInput.setTextColor(
                ContextCompat.getColor(getActivity(),R.color.colorPosNumber));
        latMinInput.setTextColor(
                ContextCompat.getColor(getActivity(),R.color.colorPosNumber));
        latSecInput.setTextColor(
                ContextCompat.getColor(getActivity(),R.color.colorPosNumber));

    }

    private void setLongColorNeg(){
        View v = getView();
        if (v == null)return;

         //Longitude
        EditText longDigDegInput = (EditText) v.findViewById(R.id.longitudeInput);
        EditText longDegInput    = (EditText) v.findViewById(R.id.longDegreesInput);
        EditText longMinInput    = (EditText) v.findViewById(R.id.longMinutesInput);
        EditText longSecInput    = (EditText) v.findViewById(R.id.longSecondsInput);


        longDigDegInput.setTextColor(
                ContextCompat.getColor(getActivity(),R.color.colorNegNumber));
        longDegInput.setTextColor(
                ContextCompat.getColor(getActivity(),R.color.colorNegNumber));
        longMinInput.setTextColor(
                ContextCompat.getColor(getActivity(),R.color.colorNegNumber));
        longSecInput.setTextColor(
                ContextCompat.getColor(getActivity(),R.color.colorNegNumber));

    }

    private void setLongColorPos(){
        View v = getView();
        if (v == null)return;

        //Longitude
        EditText longDigDegInput = (EditText) v.findViewById(R.id.longitudeInput);
        EditText longDegInput    = (EditText) v.findViewById(R.id.longDegreesInput);
        EditText longMinInput    = (EditText) v.findViewById(R.id.longMinutesInput);
        EditText longSecInput    = (EditText) v.findViewById(R.id.longSecondsInput);

        longDigDegInput.setTextColor(
                ContextCompat.getColor(getActivity(),R.color.colorPosNumber));
        longDegInput.setTextColor(
                ContextCompat.getColor(getActivity(),R.color.colorPosNumber));
        longMinInput.setTextColor(
                ContextCompat.getColor(getActivity(),R.color.colorPosNumber));
        longSecInput.setTextColor(
                ContextCompat.getColor(getActivity(),R.color.colorPosNumber));

    }

}


