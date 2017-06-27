package com.asc.msigeosystems.geobot;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;

import static com.asc.msigeosystems.geobot.R.color.colorNegNumber;
import static com.asc.msigeosystems.geobot.R.color.colorPosNumber;

/**
 * The Collect Fragment is the UI
 * when the workflow from WGS84 GPS to NAD83 to UTM/State Plane Coordinates
 * Created by Elisabeth Huhn on 6/15/2016.
 */
public class GBCoordConvertFragment extends Fragment implements GpsStatus.Listener, LocationListener, GpsStatus.NmeaListener {

    //These must be in the same order as the items are
    // added to the spinner in wireDataSourceSpinner{}
    private static final int dataSourceNoneSelected           = 0;
    private static final int dataSourceWGSManual              = 1;
    private static final int dataSourceSPCSManual             = 2;
    private static final int dataSourceUTMManual              = 3;
    private static final int dataSourcePhoneGps               = 4;
    private static final int dataSourceExternalGps            = 5;
    private static final int dataSourceCellTowerTriangulation = 6;
    
    private static final boolean sENABLE = true;
    private static final boolean sDISABLE = false;


    private GBNmeaParser mNmeaParser = new GBNmeaParser();
    private LocationManager locationManager;
    private GBNmea              mNmeaData; //latest nmea sentence received
    private GpsStatus           mGpsStatus = null;
    private Location            mCurLocation;

    //Data that must survive a reconfigure
            int     counter = 0;

    //Contains all raw data and current results of meaning such data
    private GBNmeaMeanToken mMeanToken;


     private boolean isGpsOn            = true;


    //**********************************************************/
    //*****  DataSource types for Spinner Widgets     **********/
    //**********************************************************/
    String[] mDataSourceTypes;
    private Spinner  mSpinner;


    private int    mCurDataSource;


    public GBCoordConvertFragment() {
        //for now, we don't need to initialize anything when the fragment
        //  is first created
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_convert, container, false);

        wireWidgets(v);
        wireDataSourceSpinner(v);

        updateMeanProgressUI(v);

        return v;
    }


    //Ask for location events to start
    @Override
    public void onResume() {
        super.onResume();

        if ((mCurDataSource == dataSourcePhoneGps) && (isGpsOn == true)) {
            startGps();
        }
        setSubtitle();
    }

    private void setSubtitle() {
        ((GBActivity) getActivity()).setSubtitle(R.string.subtitle_convert);
    }

    @Override
    public void onStop(){
        super.onStop();
    }

    //Ask for location events to stop
    @Override
    public void onPause() {
        super.onPause();

        stopGps();
    }


   //+****************************************************


    //+**********************************************


    private void wireWidgets(View v) {

        //Save Button
        Button saveButton = (Button) v.findViewById(R.id.saveButton);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){

                GBUtilities.getInstance().showStatus(getActivity(), R.string.save_label);

                onSave();
            }//End on Click
        });

        //Start/Stop Data  Button
        Button startStopDataButton = (Button) v.findViewById(R.id.startStopDataButton);
        startStopDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){

                // TODO: 6/22/2017 this is only if the data source is phone gps
                int msg = 0;
                switch(mCurDataSource){
                    case dataSourceNoneSelected:
                        msg = R.string.select_data_source;
                        enableManualWgsInput(sDISABLE);
                        enableManualSpcsInput(sDISABLE);
                        enableManualUtmInput(sDISABLE);
                        stopGps();
                        break;
                    case dataSourceWGSManual: //manual
                        msg = R.string.manual_wgs_data_source;
                        stopGps();
                        enableManualWgsInput(sENABLE);
                        enableManualSpcsInput(sDISABLE);
                        enableManualUtmInput(sDISABLE);

                        break;
                    case dataSourceSPCSManual: //manual
                        msg = R.string.manual_spcs_data_source;
                        stopGps();
                        enableManualWgsInput(sDISABLE);
                        enableManualSpcsInput(sENABLE);
                        enableManualUtmInput(sDISABLE);

                        break;
                    case dataSourceUTMManual: //manual
                        msg = R.string.manual_utm_data_source;
                        stopGps();
                        enableManualWgsInput(sDISABLE);
                        enableManualSpcsInput(sDISABLE);
                        enableManualUtmInput(sENABLE);

                        break;
                    case dataSourcePhoneGps://pnone GPS
                        enableManualWgsInput(sDISABLE);
                        enableManualSpcsInput(sDISABLE);
                        enableManualUtmInput(sDISABLE);
                        msg = R.string.phone_gps;
                        if (isGpsOn){
                            stopGps();
                            isGpsOn = false;
                            msg = R.string.stop_gps_button_label;
                        } else {
                            startGps();
                            isGpsOn = true;
                            msg = R.string.start_gps_button_label;
                        }

                        break;
                    case dataSourceExternalGps://external gps
                        enableManualWgsInput(sDISABLE);
                        enableManualSpcsInput(sDISABLE);
                        enableManualUtmInput(sDISABLE);
                        msg = R.string.external_gps;
                        msg = R.string.external_gps_not_available;
                        stopGps();
                        break;
                    case dataSourceCellTowerTriangulation: //cell tower triangulation
                        enableManualWgsInput(sDISABLE);
                        enableManualSpcsInput(sDISABLE);
                        enableManualUtmInput(sDISABLE);
                        msg = R.string.cell_tower_triangulation;
                        msg = R.string.cell_tower_triangu_not_available;
                        stopGps();

                        break;
                    default:
                        enableManualWgsInput(sDISABLE);
                        enableManualSpcsInput(sDISABLE);
                        enableManualUtmInput(sDISABLE);
                        stopGps();
                        msg = R.string.select_data_source;
                }

                 GBUtilities.getInstance().showStatus(getActivity(), msg);

            }//End on Click
        });


        //Start / stop Mean Button
        Button startMeanButton = (Button) v.findViewById(R.id.startMeanButton);
        startMeanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){

                int message;
                if (isMeanInProgress()){

                    message =  R.string.stop_mean_button_label;
                    //set flags that mean is done
                    mMeanToken.setMeanInProgress(false);
                    mMeanToken.setEndMeanTime(mNmeaData.getTimeStamp());
                    mMeanToken.setLastPointInMean(true);
                } else if (mCurDataSource == dataSourceNoneSelected){
                    message = R.string.select_data_source;
                } else if ((mCurDataSource == dataSourceWGSManual) ||
                           (mCurDataSource == dataSourceSPCSManual)||
                           (mCurDataSource == dataSourceUTMManual))  {
                    // TODO: 6/22/2017 think about how to lift limitation on meaning manual data
                    message = R.string.can_not_mean_manual;
                } else {
                    message = R.string.start_mean_button_label;

                    //set flags to start taking mean
                    initializeMeanToken();
                    mMeanToken.setFirstPointInMean(true);
                    mMeanToken.setMeanInProgress(true);
                }
                updateMeanProgressUI();



                GBUtilities.getInstance().showStatus(getActivity(), message);


            }//End on Click
        });






        //Conversion Button
        Button convertButton = (Button) v.findViewById(R.id.convertButton);
        convertButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){

                int message = R.string.select_data_source;
                if (mCurDataSource == dataSourceNoneSelected){
                    message = R.string.select_data_source;
                } else if (!isMeanInProgress() ) {
                    message = R.string.coordinate_conversion;
                    onConvert();
                } else {
                    message = R.string.can_not_convert_during_mean;
                }
                GBUtilities.getInstance().showStatus(getActivity(), message);

            }//End on Click
        });

        //Clear Form Button
        Button clearButton = (Button) v.findViewById(R.id.clearFormButton);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                //Can always clear the form, even if during mean. It will just come right back
                clearForm();
            }
        });

    }


    private void wireDataSourceSpinner(View v){

        //Create the array of spinner choices from the Types of Coordinates defined
        mDataSourceTypes = new String[]{getString(R.string.select_data_source),
                                        getString(R.string.manual_wgs_data_source),
                                        getString(R.string.manual_spcs_data_source),
                                        getString(R.string.manual_utm_data_source),
                                        getString(R.string.phone_gps),
                                        getString(R.string.external_gps),
                                        getString(R.string.cell_tower_triangulation)};

        //Then initialize the spinner itself
        mSpinner = (Spinner) v.findViewById(R.id.data_source_spinner);

        // Create an ArrayAdapter using the Activities context AND
        // the string array and a default spinner layout
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_spinner_item,
                mDataSourceTypes);

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        mSpinner.setAdapter(adapter);

        //attach the listener to the spinner
        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mCurDataSource = position;

                int msg = 0;
                switch(mCurDataSource){
                    case dataSourceNoneSelected:
                        enableManualWgsInput(sDISABLE);
                        enableManualSpcsInput(sDISABLE);
                        enableManualUtmInput(sDISABLE);
                        stopGps();
                        msg = R.string.select_data_source;
                        break;
                    case dataSourceWGSManual:
                        msg = R.string.manual_wgs_data_source;
                        enableManualWgsInput(sENABLE);
                        enableManualSpcsInput(sDISABLE);
                        enableManualUtmInput(sDISABLE);
                        stopGps();

                        break;
                    case dataSourceSPCSManual:
                        msg = R.string.manual_spcs_data_source;
                        enableManualWgsInput(sDISABLE);
                        enableManualSpcsInput(sENABLE);
                        enableManualUtmInput(sDISABLE);
                        stopGps();

                        break;
                    case dataSourceUTMManual:
                        msg = R.string.manual_utm_data_source;
                        enableManualWgsInput(sDISABLE);
                        enableManualSpcsInput(sDISABLE);
                        enableManualUtmInput(sENABLE);
                        stopGps();

                        break;
                    case dataSourcePhoneGps:
                        // TODO: 6/22/2017 need to check that GPS is supported on this device
                        initializeGPS();
                        startGps();
                        enableManualWgsInput(sDISABLE);
                        enableManualSpcsInput(sDISABLE);
                        enableManualUtmInput(sDISABLE);
                        msg = R.string.phone_gps;

                        break;
                    case dataSourceExternalGps:
                        msg = R.string.external_gps;
                        msg = R.string.external_gps_not_available;
                        enableManualWgsInput(sDISABLE);
                        enableManualSpcsInput(sDISABLE);
                        enableManualUtmInput(sDISABLE);
                        stopGps();
                        break;
                    case dataSourceCellTowerTriangulation:
                        msg = R.string.cell_tower_triangulation;
                        msg = R.string.cell_tower_triangu_not_available;
                        enableManualWgsInput(sDISABLE);
                        enableManualSpcsInput(sDISABLE);
                        enableManualUtmInput(sDISABLE);
                        stopGps();

                        break;
                    default:
                        enableManualWgsInput(sDISABLE);
                        enableManualSpcsInput(sDISABLE);
                        enableManualUtmInput(sDISABLE);
                        stopGps();
                        msg = R.string.select_data_source;
                }

                GBUtilities.getInstance().showStatus(getActivity(), msg);



            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //for now, do nothing
            }
        });

    }




    //******************************************************************//
    //            Button Handlers                                       //
    //******************************************************************//
    private void onSave(){
        GBUtilities.getInstance().showStatus(getActivity(), R.string.save_not_available);


    }


    //******************************************************************//
    //            Process the received NMEA sentence                    //
    //******************************************************************//
    void handleNmeaReceived(long timestamp, String nmea) {
        //todo maybe need to do something with the timestamp
        try {
            //create an object with all the fields from the string
            if (mNmeaParser == null)mNmeaParser = new GBNmeaParser();
            GBNmea nmeaData = mNmeaParser.parse(nmea);
            if (nmeaData == null) return;

            nmeaData.setTimeStamp(timestamp);

            nmeaData = refineNmeaData(nmeaData);
            if (nmeaData == null)return;

            //so we know it's a good point
            mNmeaData = nmeaData;


            GBCoordinateWGS84 coordinateWGS84;
            if (isMeanInProgress()){
                if (mMeanToken == null){
                    initializeMeanToken();
                    updateMeanProgressUI();
                }

                //Fold the new nmea sentence into the ongoing mean
                GBCoordinateMean meanCoordinate = mMeanToken.updateMean(mNmeaData);
                if (meanCoordinate != null) {
                    //Is this the first point we have processed?
                    if (isFirstMeanPoint()) {
                        mMeanToken.setStartMeanTime(mNmeaData.getTimeStamp());
                        mMeanToken.setFirstPointInMean(false);
                    }
                    // TODO: 6/19/2017 remember debug change. Remove the 0
                    updateNmeaUI(mMeanToken.getLastCoordinate(), nmeaData);
                    //updateNmeaUI(mMeanToken.getCoordinateAt(0), nmeaData);
                    updateMeanUI(meanCoordinate, mMeanToken);
                }
            } else {
                if ((mMeanToken != null) && (mMeanToken.isLastPointInMean())){
                    //no need to recalcuclate the mean.
                    updateMeanUI(mMeanToken.getMeanCoordinate(false), mMeanToken);
                    mMeanToken.setLastPointInMean(false);
                }
                coordinateWGS84 = new GBCoordinateWGS84(nmeaData);

                //update the UI from the coordinate
                updateNmeaUI(coordinateWGS84, nmeaData);
            }

            //save the raw data
            //get the nmea container
            GBNmeaManager nmeaManager = GBNmeaManager.getInstance();
            //nmeaManager.add(mNmeaData);



        } catch (RuntimeException e){
            //there was an exception processing the NMEA Sentence
            GBUtilities.getInstance().showStatus(getActivity(), e.getMessage());
            //throw new RuntimeException(e);
        }
    }

    private GBNmea refineNmeaData(GBNmea nmeaData){
        if (nmeaData == null) {
            GBUtilities.getInstance().showStatus(getActivity(), R.string.null_type_found);
            return  null;
        }


        //Which fields have meaning depend upon the type of the sentence
        String type = nmeaData.getNmeaType().toString();
        if (GBUtilities.isEmpty(type)) {
            GBUtilities.getInstance().showStatus(getActivity(), R.string.null_type_found);
            return  null;
        }



        if (!type.contains("GGA")) {
            if (!type.contains("GNS")){
                return null;
            }
        }


        if ((nmeaData.getLatitude() == 0.0) && (nmeaData.getLongitude() == 0.0)) {
            return null;
        }
        return nmeaData;
    }



    private boolean isMeanInProgress(){
        return ((mMeanToken != null) && mMeanToken.isMeanInProgress());
    }
    private boolean isMeanStopped(){return !isMeanInProgress();}
    private boolean isFirstMeanPoint(){
        return mMeanToken.isFirstPointInMean();
    }

    private void initializeMeanToken(){
        if (mMeanToken == null)mMeanToken = new GBNmeaMeanToken();
        mMeanToken.setMeanInProgress(false);
        mMeanToken.setFirstPointInMean(false);
        mMeanToken.setLastPointInMean(false);
        mMeanToken.resetCoordinates();
        updateMeanProgressUI();
    }


    private boolean updateNmeaUI(GBCoordinateWGS84 coordinateWGS84, GBNmea nmeaData){
        View v = getView();
        if (v == null)return false;

        //Time
        //TextView gpsWgs84TimeOutput   = (TextView) v.findViewById(R.id.gpsWgs84TimeOutput);
        TextView gpsWgs84TimestampOutput = (TextView)v.findViewById(R.id.gpsWgs84TimestampOutput);
        //GPS Latitude
        TextView gpsWgs84LatitudeInput   = (TextView) v.findViewById(R.id.gpsWgs84LatitudeInput);
        TextView gpsWgs84LatDegreesInput = (TextView) v.findViewById(R.id.gpsWgs84LatDegreesInput);
        TextView gpsWgs84LatMinutesInput = (TextView) v.findViewById(R.id.gpsWgs84LatMinutesInput);
        TextView gpsWgs84LatSecondsInput = (TextView) v.findViewById(R.id.gpsWgs84LatSecondsInput);

        //GPS Longitude
        TextView gpsWgs84LongitudeInput   = (TextView) v.findViewById(R.id.gpsWgs84LongitudeInput);
        TextView gpsWgs84LongDegreesInput = (TextView) v.findViewById(R.id.gpsWgs84LongDegreesInput);
        TextView gpsWgs84LongMinutesInput = (TextView) v.findViewById(R.id.gpsWgs84LongMinutesInput);
        TextView gpsWgs84LongSecondsInput = (TextView) v.findViewById(R.id.gpsWgs84LongSecondsInput);

        //Elevation
        TextView gpsWgs84ElevationMetersInput   = (TextView) v.findViewById(R.id.gpsWgs84ElevationMetersInput);
        TextView gpsWgs84GeoidHeightMetersInput = (TextView) v.findViewById(R.id.gpsWgs84GeoidHeightMetersInput);
        TextView gpsWgs84ElevationFeetInput     = (TextView) v.findViewById(R.id.gpsWgs84ElevationFeetInput);
        TextView gpsWgs84GeoidHeightFeetInput   = (TextView) v.findViewById(R.id.gpsWgs84GeoidHeightFeetInput);

        TextView gpsWgs84ConvergenceAngleInput  = (TextView) v.findViewById(R.id.gpsWgs84ConvergenceOutput);
        TextView gpsWgs84ScaleFactorInput       = (TextView) v.findViewById(R.id.gpsWgs84ScaleFactorOutput);

        String nmeaTimeString = String.format(Locale.getDefault(), "%.0f", nmeaData.getTime());
        //String wgsTimeString  = String.format(Locale.getDefault(), "%.0f", coordinateWGS84.getTime());
        //gpsWgs84TimeOutput.setText(nmeaTimeString);
                //todo fix time between nmea and coordinate WGS84
                //setText(Double.toString(coordinateWGS84.getTime()));
                //setText(Double.toString(nmeaData.getTime()));

        //String timestampString = GBUtilities.getDateTimeString((long)nmeaData.getTimeStamp());
        String timestampString = GBUtilities.getDateTimeString(coordinateWGS84.getTime());
        gpsWgs84TimestampOutput.setText(timestampString);

        gpsWgs84LatitudeInput  .setText(doubleToUI(coordinateWGS84.getLatitude()));

        gpsWgs84LongitudeInput  .setText(doubleToUI(coordinateWGS84.getLongitude()));

        gpsWgs84ElevationMetersInput  .setText(doubleToUI(coordinateWGS84.getElevation()));
        gpsWgs84GeoidHeightMetersInput.setText(doubleToUI(coordinateWGS84.getGeoid()));
        gpsWgs84ElevationFeetInput.setText(doubleToUI(coordinateWGS84.getElevationFeet()));
        gpsWgs84GeoidHeightFeetInput.setText(doubleToUI(coordinateWGS84.getGeoidFeet()));


        boolean isLatitude = true;
        convertDDtoDMS(getActivity(),
                        gpsWgs84LatitudeInput,
                        gpsWgs84LatDegreesInput,
                        gpsWgs84LatMinutesInput,
                        gpsWgs84LatSecondsInput,
                        isLatitude);

        isLatitude = false;
        convertDDtoDMS(getActivity(),
                        gpsWgs84LongitudeInput,
                        gpsWgs84LongDegreesInput,
                        gpsWgs84LongMinutesInput,
                        gpsWgs84LongSecondsInput,
                        isLatitude);

        gpsWgs84ConvergenceAngleInput.setText(doubleToUI(coordinateWGS84.getConvergenceAngle()));
        gpsWgs84ScaleFactorInput.setText(doubleToUI(coordinateWGS84.getScaleFactor()));

        return true;
    }

    private boolean updateMeanUI(GBCoordinateMean meanCoordinate, GBNmeaMeanToken meanToken){


        View v = getView();
        if (v == null)return false;


        //Mean Parameters
        TextView meanWgs84PointsInMeanOutput = (TextView)v.findViewById(R.id.meanWgs84PointsInMeanOutput);
        //TextView meanWgs84StartTimeOutput = (TextView) v.findViewById(R.id.meanWgs84StartTimeOutput);
        //TextView meanWgs84EndTimeOutput   = (TextView) v.findViewById(R.id.meanWgs84EndTimeOutput);
        TextView meanWgs84StartTimestampOutput = (TextView) v.findViewById(R.id.meanWgs84StartTimestampOutput);
        TextView meanWgs84EndTimestampOutput   = (TextView) v.findViewById(R.id.meanWgs84EndTimestampOutput);

        //Mean Latitude
        TextView meanWgs84LatitudeInput   = (TextView) v.findViewById(R.id.meanWgs84LatitudeInput);
        TextView meanWgs84LatDegreesInput = (TextView) v.findViewById(R.id.meanWgs84LatDegreesInput);
        TextView meanWgs84LatMinutesInput = (TextView) v.findViewById(R.id.meanWgs84LatMinutesInput);
        TextView meanWgs84LatSecondsInput = (TextView) v.findViewById(R.id.meanWgs84LatSecondsInput);

        //Mean Longitude
        TextView meanWgs84LongitudeInput   = (TextView) v.findViewById(R.id.meanWgs84LongitudeInput);
        TextView meanWgs84LongDegreesInput = (TextView) v.findViewById(R.id.meanWgs84LongDegreesInput);
        TextView meanWgs84LongMinutesInput = (TextView) v.findViewById(R.id.meanWgs84LongMinutesInput);
        TextView meanWgs84LongSecondsInput = (TextView) v.findViewById(R.id.meanWgs84LongSecondsInput);

        //Elevation
        TextView meanWgs84ElevationMetersInput   = (TextView) v.findViewById(
                                                            R.id.meanWgs84ElevationMetersInput);
        TextView meanWgs84ElevationFeetInput     = (TextView) v.findViewById(
                                                            R.id.meanWgs84ElevationFeetInput);
        TextView meanWgs84GeoidHeightMetersInput = (TextView) v.findViewById(
                                                         R.id.meanWgs84GeoidHeightMetersInput);
        TextView meanWgs84GeoidHeightFeetInput   = (TextView) v.findViewById(
                                                            R.id.meanWgs84GeoidHeightFeetInput);

        //Mean Standard Deviations
        TextView meanWgs84LatSigmaOutput = (TextView)v.findViewById(R.id.meanWgs84LatSigmaOutput);
        TextView meanWgs84LongSigmaOutput= (TextView)v.findViewById(R.id.meanWgs84LongSigmaOutput);
        TextView meanWgs84ElevSigmaOutput= (TextView)v.findViewById(R.id.meanWgs84ElevSigmaOutput);





        //show the mean and standard deviation on the screen
        meanWgs84PointsInMeanOutput.setText(String.valueOf(meanCoordinate.getMeanedReadings()));
        //meanWgs84StartTimeOutput.setText(String.valueOf(meanToken.getStartMeanTime()));
        //meanWgs84EndTimeOutput.setText(String.valueOf(meanToken.getEndMeanTime()));

        long startTimestamp = (long)meanToken.getStartMeanTime();
        String startTimeStampString = GBUtilities.getDateTimeString(startTimestamp);

        long endTimestamp   = (long)meanToken.getEndMeanTime();
        String endTimestampString = GBUtilities.getDateTimeString(endTimestamp);

        meanWgs84StartTimestampOutput.setText(String.valueOf(startTimeStampString));

        if (endTimestamp == 0){
            endTimestampString = "0";
        }
        meanWgs84EndTimestampOutput.setText(String.valueOf(endTimestampString));


        meanWgs84LatitudeInput  .setText(doubleToUI(meanCoordinate.getLatitude()));
        meanWgs84LongitudeInput  .setText(doubleToUI(meanCoordinate.getLongitude()));

        meanWgs84ElevationMetersInput  .setText(doubleToUI(meanCoordinate.getElevation()));
        meanWgs84ElevationFeetInput    .setText(doubleToUI(meanCoordinate.getElevationFeet()));
        meanWgs84GeoidHeightMetersInput.setText(doubleToUI(meanCoordinate.getGeoid()));
        meanWgs84GeoidHeightFeetInput  .setText(doubleToUI(meanCoordinate.getGeoidFeet()));

        meanWgs84LatSigmaOutput .setText(doubleToUI(meanCoordinate.getLatitudeStdDev()));
        meanWgs84LongSigmaOutput.setText(doubleToUI(meanCoordinate.getLongitudeStdDev()));
        meanWgs84ElevSigmaOutput.setText(doubleToUI(meanCoordinate.getElevationStdDev()));

        boolean isLatitude = true;
        convertDDtoDMS(getActivity(),
                        meanWgs84LatitudeInput,
                        meanWgs84LatDegreesInput,
                        meanWgs84LatMinutesInput,
                        meanWgs84LatSecondsInput,
                        isLatitude);

        isLatitude = false;
        convertDDtoDMS(getActivity(),
                        meanWgs84LongitudeInput,
                        meanWgs84LongDegreesInput,
                        meanWgs84LongMinutesInput,
                        meanWgs84LongSecondsInput,
                        isLatitude);

        return true;
    }

    private boolean updateGpsUI(boolean isGpsOn) {
        View v = getView();
        if (v == null) return false;

        //Time
        TextView gpsOnOffOutput = (TextView) v.findViewById(R.id.gps_on_off_output);
        int message;
        if (isGpsOn){
            message = R.string.gps_on;
        } else {
            message = R.string.gps_off;
        }
        gpsOnOffOutput.setText(getString(message));

        return true;
    }


    private boolean updateMeanProgressUI(View v) {

        //Time
        TextView meanOnOffOutput = (TextView) v.findViewById(R.id.gpsMeanInProgressOutput);
        int message;
        if (isMeanInProgress()){
            message = R.string.mean_in_progress_string;
        } else {
            message = R.string.mean_not_in_progress_string;
        }
        meanOnOffOutput.setText(getString(message));

        return true;
    }

    private boolean updateMeanProgressUI() {
        View v = getView();
        if (v == null) return false;

       return updateMeanProgressUI(v);
    }


    private boolean updateWgsUI(GBCoordinateWGS84 coordinateWGS84){
        View v = getView();
        if (v == null)return false;

        //Time
        //TextView gpsWgs84TimeOutput   = (TextView) v.findViewById(gpsWgs84TimeOutput);
        TextView gpsWgs84TimestampOutput = (TextView)v.findViewById(R.id.gpsWgs84TimestampOutput);
        //GPS Latitude
        TextView gpsWgs84LatitudeInput   = (TextView) v.findViewById(R.id.gpsWgs84LatitudeInput);
        TextView gpsWgs84LatDegreesInput = (TextView) v.findViewById(R.id.gpsWgs84LatDegreesInput);
        TextView gpsWgs84LatMinutesInput = (TextView) v.findViewById(R.id.gpsWgs84LatMinutesInput);
        TextView gpsWgs84LatSecondsInput = (TextView) v.findViewById(R.id.gpsWgs84LatSecondsInput);

        //GPS Longitude
        TextView gpsWgs84LongitudeInput   = (TextView) v.findViewById(R.id.gpsWgs84LongitudeInput);
        TextView gpsWgs84LongDegreesInput = (TextView) v.findViewById(R.id.gpsWgs84LongDegreesInput);
        TextView gpsWgs84LongMinutesInput = (TextView) v.findViewById(R.id.gpsWgs84LongMinutesInput);
        TextView gpsWgs84LongSecondsInput = (TextView) v.findViewById(R.id.gpsWgs84LongSecondsInput);

        //Elevation
        TextView gpsWgs84ElevationMetersInput   = (TextView) v.findViewById(R.id.gpsWgs84ElevationMetersInput);
        TextView gpsWgs84GeoidHeightMetersInput = (TextView) v.findViewById(R.id.gpsWgs84GeoidHeightMetersInput);
        TextView gpsWgs84ElevationFeetInput     = (TextView) v.findViewById(R.id.gpsWgs84ElevationFeetInput);
        TextView gpsWgs84GeoidHeightFeetInput   = (TextView) v.findViewById(R.id.gpsWgs84GeoidHeightFeetInput);

        TextView gpsWgs84ConvergenceAngleInput  = (TextView) v.findViewById(R.id.gpsWgs84ConvergenceOutput);
        TextView gpsWgs84ScaleFactorInput       = (TextView) v.findViewById(R.id.gpsWgs84ScaleFactorOutput);

        long timeStamp = coordinateWGS84.getTime();
        String timestampString = GBUtilities.getDateTimeString(timeStamp);

        String timeString = String.format(Locale.getDefault(), "%d", coordinateWGS84.getTime());
        //gpsWgs84TimeOutput.setText(timeString);

        gpsWgs84TimestampOutput.setText(timestampString);

        gpsWgs84LatitudeInput  .setText(doubleToUI(coordinateWGS84.getLatitude()));

        gpsWgs84LongitudeInput  .setText(doubleToUI(coordinateWGS84.getLongitude()));

        gpsWgs84ElevationMetersInput  .setText(doubleToUI(coordinateWGS84.getElevation()));
        gpsWgs84GeoidHeightMetersInput.setText(doubleToUI(coordinateWGS84.getGeoid()));
        gpsWgs84ElevationFeetInput.setText(doubleToUI(coordinateWGS84.getElevationFeet()));
        gpsWgs84GeoidHeightFeetInput.setText(doubleToUI(coordinateWGS84.getGeoidFeet()));


        boolean isLatitude = true;
        convertDDtoDMS(getActivity(),
                        gpsWgs84LatitudeInput,
                        gpsWgs84LatDegreesInput,
                        gpsWgs84LatMinutesInput,
                        gpsWgs84LatSecondsInput,
                        isLatitude);

        isLatitude = false;
        convertDDtoDMS(getActivity(),
                        gpsWgs84LongitudeInput,
                        gpsWgs84LongDegreesInput,
                        gpsWgs84LongMinutesInput,
                        gpsWgs84LongSecondsInput,
                        isLatitude);

        gpsWgs84ConvergenceAngleInput.setText(doubleToUI(coordinateWGS84.getConvergenceAngle()));
        gpsWgs84ScaleFactorInput     .setText(doubleToUI(coordinateWGS84.getScaleFactor()));


        return true;
    }

    private boolean updateUtmUI(GBCoordinateWGS84 coordinateWGS84){


        GBCoordinateUTM utmCoordinate = new GBCoordinateUTM(coordinateWGS84);
        return updateUtmUI(utmCoordinate);
    }

    private boolean updateUtmUI(GBCoordinateUTM coordinateUTM){
        View v = getView();
        if (v == null  )return false;

        if (!coordinateUTM.isValidCoordinate()) return false;


        TextView utmZoneOutput           = (TextView) v.findViewById(R.id.utmZoneOutput);
        TextView utmLatbandOutput        = (TextView) v.findViewById(R.id.utmLatbandOutput);
        TextView utmHemisphereOutput     = (TextView) v.findViewById(R.id.utmHemisphereOutput);
        TextView utmEastingMetersOutput  = (TextView) v.findViewById(R.id.utmEastingMetersOutput);
        TextView utmNorthingMetersOutput = (TextView) v.findViewById(R.id.utmNorthingMetersOutput);
        TextView utmEastingFeetOutput    = (TextView) v.findViewById(R.id.utmEastingFeetOutput);
        TextView utmNorthingFeetOutput   = (TextView) v.findViewById(R.id.utmNorthingFeetOutput);

        TextView utmElevationOutput       = (TextView) v.findViewById(R.id.utmElevationMetersInput) ;
        TextView utmElevationFeetOutput   = (TextView) v.findViewById(R.id.utmElevationFeetInput) ;
        TextView utmGeoidOutput           = (TextView) v.findViewById(R.id.utmGeoidHeightMetersInput) ;
        TextView utmGeoidFeetOutput       = (TextView) v.findViewById(R.id.utmGeoidHeightFeetInput) ;

        TextView utmConvergenceOutput    = (TextView) v.findViewById(R.id.utmConvergence);
        TextView utmScaleFactorOutput    = (TextView) v.findViewById(R.id.utmScaleFactor);

        //Also output the result in separate fields
        utmZoneOutput        .setText(String.valueOf(coordinateUTM.getZone()));
        utmHemisphereOutput  .setText(String.valueOf(coordinateUTM.getHemisphere()));
        utmLatbandOutput     .setText(String.valueOf(coordinateUTM.getLatBand()));

        utmEastingMetersOutput.setText(String.valueOf(coordinateUTM.getEasting()));
        utmNorthingMetersOutput.setText(String.valueOf(coordinateUTM.getNorthing()));
        utmEastingFeetOutput .setText(String.valueOf(coordinateUTM.getEastingFeet()));
        utmNorthingFeetOutput.setText(String.valueOf(coordinateUTM.getNorthingFeet()));

        utmElevationOutput    .setText(String.valueOf(coordinateUTM.getElevation()));
        utmElevationFeetOutput.setText(String.valueOf(coordinateUTM.getElevationFeet()));
        utmGeoidOutput        .setText(String.valueOf(coordinateUTM.getGeoid()));
        utmGeoidFeetOutput    .setText(String.valueOf(coordinateUTM.getGeoidFeet()));

        utmConvergenceOutput .setText(String.valueOf(coordinateUTM.getConvergenceAngle()));
        utmScaleFactorOutput .setText(String.valueOf(coordinateUTM.getScaleFactor()));
        return true;


    }

    private boolean updateSpcsUI(GBCoordinateWGS84 coordinateWgs){
        View v = getView();
        if (v == null)return false;
        //need to ask for zone, then convert based on the zone
        EditText spcZoneInput = (EditText)v.findViewById(R.id.spcZoneOutput);
        TextView spcStateOutput  = (TextView) v.findViewById(R.id.spcStateOutput);
        String zoneString = spcZoneInput.getText().toString();
        if (GBUtilities.isEmpty(zoneString)){
            spcStateOutput.setText(getString(R.string.spc_zone_error));
            return false;
        }
        int zone = Integer.valueOf(zoneString);

        GBCoordinateSPCS coordinateSPCS = new GBCoordinateSPCS(coordinateWgs, zone);

        if ((coordinateSPCS.getZone() == (int)GBUtilities.ID_DOES_NOT_EXIST) ||
            (coordinateSPCS.getZone() != zone))    {
            clearSPCSUI(v);
            spcZoneInput  .setText(String.valueOf(coordinateSPCS.getZone()));
            spcStateOutput.setText(getString(R.string.spc_zone_error));
            return false;
        }

        return updateSpcsUI(coordinateSPCS);


    }

    private boolean updateSpcsUI(GBCoordinateSPCS coordinateSPCS){
        View v = getView();
        if (v == null)return false;


        EditText spcZoneInput            = (EditText)v.findViewById(R.id.spcZoneOutput);
        TextView spcStateOutput          = (TextView) v.findViewById(R.id.spcStateOutput);

        //SPC
        TextView spcEastingMetersOutput  = (TextView) v.findViewById(R.id.spcEastingMetersOutput);
        TextView spcNorthingMetersOutput = (TextView) v.findViewById(R.id.spcNorthingMetersOutput);
        TextView spcEastingFeetOutput    = (TextView) v.findViewById(R.id.spcEastingFeetOutput);
        TextView spcNorthingFeetOutput   = (TextView) v.findViewById(R.id.spcNorthingFeetOutput);

        //Elevation
        TextView spcsElevationMetersInput   = (TextView) v.findViewById(R.id.spcsElevationMetersInput);
        TextView spcsGeoidHeightMetersInput = (TextView) v.findViewById(R.id.spcsGeoidHeightMetersInput);
        TextView spcsElevationFeetInput     = (TextView) v.findViewById(R.id.spcsElevationFeetInput);
        TextView spcsGeoidHeightFeetInput   = (TextView) v.findViewById(R.id.spcsGeoidHeightFeetInput);

        TextView spcConvergenceOutput    = (TextView) v.findViewById(R.id.spcConvergenceOutput);
        TextView spcScaleFactorOutput    = (TextView) v.findViewById(R.id.spcScaleFactorOutput);


        spcZoneInput           .setText(String.valueOf(coordinateSPCS.getZone()));
        spcStateOutput         .setText(coordinateSPCS.getState());

        spcEastingMetersOutput .setText(String.valueOf(doubleToUI(coordinateSPCS.getEasting())));
        spcNorthingMetersOutput.setText(String.valueOf(doubleToUI(coordinateSPCS.getNorthing())));
        spcEastingFeetOutput   .setText(String.valueOf(doubleToUI(coordinateSPCS.getEastingFeet())));
        spcNorthingFeetOutput  .setText(String.valueOf(doubleToUI(coordinateSPCS.getNorthingFeet())));

        spcsElevationMetersInput  .setText(String.valueOf(doubleToUI(coordinateSPCS.getElevation())));
        spcsElevationFeetInput    .setText(String.valueOf(doubleToUI(coordinateSPCS.getElevationFeet())));
        spcsGeoidHeightMetersInput.setText(String.valueOf(doubleToUI(coordinateSPCS.getGeoid())));
        spcsGeoidHeightFeetInput  .setText(String.valueOf(doubleToUI(coordinateSPCS.getGeoidFeet())));

        spcConvergenceOutput   .setText(String.valueOf(doubleToUI(coordinateSPCS.getConvergenceAngle())));
        spcScaleFactorOutput   .setText(String.valueOf(doubleToUI(coordinateSPCS.getScaleFactor())));

        return true;

    }



    private String doubleToUI(double reading){
        return String.valueOf(truncatePrecision(reading));
    }
    private String intToUI   (int reading)    {return String.valueOf(reading);}

    //truncate digits of precision
    private double truncatePrecision(double reading) {

        BigDecimal bd = new BigDecimal(reading).
                setScale(GBUtilities.sMicrometerDigitsOfPrecision, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    // TODO: 6/19/2017 figrue out how to use the one on GBCoordinateLL
    //Conversion for UI fields
    //last parameter indicates whether latitude (true) or longitude (false)
    boolean convertDDtoDMS(Context  context,
                           TextView tudeDDInput,
                           TextView tudeDInput,
                           TextView tudeMInput,
                           TextView tudeSInput,
                           boolean  isLatitude) {

        String tudeString = tudeDDInput.getText().toString().trim();
        if (tudeString.isEmpty()) {
            tudeString = context.getString(R.string.zero_decimal_string);
            tudeDDInput.setText(tudeString);
        }

        double tude = Double.parseDouble(tudeString);

        //The user inputs have to be within range to be
        if (   (isLatitude   && ((tude < -90.0) || (tude >= 90.0)))  || //Latitude
                ((!isLatitude)  && ((tude < -180.) || (tude >= 180.)))) {  //Longitude

            tudeDInput.setText(R.string.zero_decimal_string);
            tudeMInput.setText(R.string.zero_decimal_string);
            tudeSInput.setText(R.string.zero_decimal_string);
            return false;
        }

        //check sign of tude
        boolean isTudePos = true;
        int tudeColor= colorPosNumber;
        if (tude < 0) {
            //tude is negative, remember this and work with the absolute value
            tude = Math.abs(tude);
            isTudePos = false;
            tudeColor = colorNegNumber;
        }

        //strip out the decimal parts of tude
        int tudeDegree = (int) tude;

        double degree = tudeDegree;

        //digital degrees minus degrees will be decimal minutes plus seconds
        //converting to int strips out the seconds
        double minuteSec = tude - degree;
        double minutes = minuteSec * 60.;
        int tudeMinute = (int) minutes;
        double minuteOnly = (double) tudeMinute;

        //start with the DD, subtract out Degrees, subtract out Minutes
        //convert the remainder into whole seconds
        double tudeSecond = (tude - degree - (minuteOnly / 60.)) * (60. * 60.);
        //tudeSecond = (tude - minutes) * (60. *60.);

        //If tude was negative before, restore it to negative
        if (!isTudePos) {
            //tude       = 0. - tude;
            tudeDegree = 0 - tudeDegree;
            tudeMinute = 0 - tudeMinute;
            tudeSecond = 0. - tudeSecond;
        }

        //truncate to a reasonable number of decimal digits
        BigDecimal bd = new BigDecimal(tudeSecond).setScale(GBUtilities.sMicrometerDigitsOfPrecision,
                RoundingMode.HALF_UP);
        tudeSecond = bd.doubleValue();

        //show the user the result
        tudeDInput.setText(String.valueOf(tudeDegree));
        tudeMInput.setText(String.valueOf(tudeMinute));
        tudeSInput.setText(String.valueOf(tudeSecond));

        tudeDDInput.setTextColor(ContextCompat.getColor(context, tudeColor));
        tudeDInput .setTextColor(ContextCompat.getColor(context, tudeColor));
        tudeMInput .setTextColor(ContextCompat.getColor(context, tudeColor));
        tudeSInput .setTextColor(ContextCompat.getColor(context, tudeColor));

        return true;
    }



    //******************************************************************//
    //            Convert input WGS84 fields into a CoordinateWGS84     //
    //******************************************************************//
    private void onConvert(){
        if (isMeanInProgress())return;

        GBUtilities.getInstance().showStatus(getActivity(), R.string.conversion_stub);

        GBCoordinateWGS84 coordinateWGS84 = null;
        switch(mCurDataSource){

            case dataSourceWGSManual:
                coordinateWGS84 = convertWgsInputs();
                if ((coordinateWGS84 == null) || !coordinateWGS84.isValidCoordinate()){
                    GBUtilities.getInstance().showStatus(getActivity(), R.string.conversion_failed);
                    return;
                }

                //Convert the WGS84 to UTM
                updateUtmUI(coordinateWGS84);

                //Convert to State Plane
                updateSpcsUI(coordinateWGS84);

                break;

            case dataSourceSPCSManual:
                GBCoordinateSPCS coordinateSPCS = convertSpcsInputs();
                if ((coordinateSPCS == null) || !coordinateSPCS.isValidCoordinate()){
                    GBUtilities.getInstance().showStatus(getActivity(), R.string.conversion_failed);
                    return;
                }
                coordinateWGS84 = new GBCoordinateWGS84(coordinateSPCS);
                if ((coordinateWGS84 == null) || !coordinateWGS84.isValidCoordinate()){
                    GBUtilities.getInstance().showStatus(getActivity(), R.string.conversion_failed);
                    return;
                }

                updateWgsUI(coordinateWGS84);
                updateSpcsUI(coordinateSPCS);
                updateUtmUI(coordinateWGS84);
                break;

            case dataSourceUTMManual:
                GBCoordinateUTM coordinateUTM = convertUtmInputs();
                if ((coordinateUTM == null) || !coordinateUTM.isValidCoordinate()){
                    GBUtilities.getInstance().showStatus(getActivity(), R.string.conversion_failed);
                    return;
                }
                coordinateWGS84 = new GBCoordinateWGS84(coordinateUTM);
                if ((coordinateWGS84 == null) || !coordinateWGS84.isValidCoordinate()){
                    GBUtilities.getInstance().showStatus(getActivity(), R.string.conversion_failed);
                    return;
                }

                //display WGS Coordinate
                updateWgsUI(coordinateWGS84);
                //Convert to UTM Coordinate and display it
                updateSpcsUI(coordinateWGS84);
                //display UTM Coordinate
                updateUtmUI(coordinateUTM);
                break;

            case dataSourcePhoneGps:
            case dataSourceExternalGps:
                stopGps();
                coordinateWGS84 = convertMeanedOrRaw();
                if ((coordinateWGS84 == null) || (!coordinateWGS84.isValidCoordinate())){
                    GBUtilities.getInstance().showStatus(getActivity(), R.string.conversion_failed);
                    return;
                }

                updateWgsUI(coordinateWGS84);

                //Convert the WGS84 to UTM, and display the UTM coordinate
                updateUtmUI(coordinateWGS84);

                //Convert to State Plane and display the SPCS coordinate
                updateSpcsUI(coordinateWGS84);

                break;


            case dataSourceCellTowerTriangulation:
                //for now, cell tower conversion is not supported

                break;
            default:
        }

    }

    private GBCoordinateWGS84 convertMeanedOrRaw(){
        GBCoordinateWGS84 coordinateWGS84;

        //even though the mean is not in progress, it might not have been run yet
        //or it might have been reset
        if ((mMeanToken == null) || (mMeanToken.getCoordinateSize() == 0)) {
            coordinateWGS84 = convertWgsInputs();
        } else if (isMeanInProgress()){
            return null;
        } else {
            //convert the meaned coordinate
            coordinateWGS84 = new GBCoordinateWGS84(mMeanToken.getMeanCoordinate(true));
        }
        return coordinateWGS84;
    }

    private GBCoordinateWGS84 convertWgsInputs() {
        View v = getView();
        if (v == null)return null;

        //Time
        //EditText gpsWgs84TimeOutput      = (EditText) v.findViewById(gpsWgs84TimeOutput);
        TextView gpsWgs84TimeStampOutput = (TextView) v.findViewById(R.id.gpsWgs84TimestampOutput);
        //GPS Latitude
        EditText gpsWgs84LatitudeInput   = (EditText) v.findViewById(R.id.gpsWgs84LatitudeInput);
        EditText gpsWgs84LatDegreesInput = (EditText) v.findViewById(R.id.gpsWgs84LatDegreesInput);
        EditText gpsWgs84LatMinutesInput = (EditText) v.findViewById(R.id.gpsWgs84LatMinutesInput);
        EditText gpsWgs84LatSecondsInput = (EditText) v.findViewById(R.id.gpsWgs84LatSecondsInput);

        //GPS Longitude
        EditText gpsWgs84LongitudeInput   = (EditText) v.findViewById(R.id.gpsWgs84LongitudeInput);
        EditText gpsWgs84LongDegreesInput = (EditText) v.findViewById(R.id.gpsWgs84LongDegreesInput);
        EditText gpsWgs84LongMinutesInput = (EditText) v.findViewById(R.id.gpsWgs84LongMinutesInput);
        EditText gpsWgs84LongSecondsInput = (EditText) v.findViewById(R.id.gpsWgs84LongSecondsInput);

        //Elevation
        EditText gpsWgs84ElevationMetersInput   = (EditText) v.findViewById(R.id.gpsWgs84ElevationMetersInput);
        EditText gpsWgs84GeoidHeightMetersInput = (EditText) v.findViewById(R.id.gpsWgs84GeoidHeightMetersInput);
        EditText gpsWgs84ElevationFeetInput     = (EditText) v.findViewById(R.id.gpsWgs84ElevationFeetInput);
        EditText gpsWgs84GeoidHeightFeetInput   = (EditText) v.findViewById(R.id.gpsWgs84GeoidHeightFeetInput);


        TextView gpsWgs84ConvergenceAngleInput  = (TextView) v.findViewById(R.id.gpsWgs84ConvergenceOutput);
        TextView gpsWgs84ScaleFactorInput       = (TextView) v.findViewById(R.id.gpsWgs84ScaleFactorOutput);


        long timeStamp = GBUtilities.getDateTimeFromString(getActivity(),
                gpsWgs84TimeStampOutput.getText().toString());
        GBCoordinateWGS84 coordinateWGS84 = new GBCoordinateWGS84(
                timeStamp,
                gpsWgs84LatitudeInput.getText().toString(),
                gpsWgs84LatDegreesInput.getText().toString(),
                gpsWgs84LatMinutesInput.getText().toString(),
                gpsWgs84LatSecondsInput.getText().toString(),
                gpsWgs84LongitudeInput.getText().toString(),
                gpsWgs84LongDegreesInput.getText().toString(),
                gpsWgs84LongMinutesInput.getText().toString(),
                gpsWgs84LongSecondsInput.getText().toString(),
                gpsWgs84ElevationMetersInput.getText().toString(),
                gpsWgs84ElevationFeetInput.getText().toString(),
                gpsWgs84GeoidHeightMetersInput.getText().toString(),
                gpsWgs84GeoidHeightFeetInput.getText().toString());
        if (!coordinateWGS84.isValidCoordinate()){

            GBUtilities.getInstance().showStatus(getActivity(), R.string.coordinate_try_again);
            return null;
        }


        String timeStampString = GBUtilities.getDateTimeString(coordinateWGS84.getTime());
        gpsWgs84TimeStampOutput.setText(timeStampString);
        //gpsWgs84TimeOutput.setText(String.valueOf(timeStamp));
        //display the coordinate values in the UI
        gpsWgs84LatitudeInput  .setText(String.valueOf(coordinateWGS84.getLatitude()));
        gpsWgs84LatDegreesInput.setText(String.valueOf(coordinateWGS84.getLatitudeDegree()));
        gpsWgs84LatMinutesInput.setText(String.valueOf(coordinateWGS84.getLatitudeMinute()));
        gpsWgs84LatSecondsInput.setText(String.valueOf(coordinateWGS84.getLatitudeSecond()));

        gpsWgs84LongitudeInput  .setText(String.valueOf(coordinateWGS84.getLongitude()));
        gpsWgs84LongDegreesInput.setText(String.valueOf(coordinateWGS84.getLongitudeDegree()));
        gpsWgs84LongMinutesInput.setText(String.valueOf(coordinateWGS84.getLongitudeMinute()));
        gpsWgs84LongSecondsInput.setText(String.valueOf(coordinateWGS84.getLongitudeSecond()));

        gpsWgs84ElevationMetersInput.setText(String.valueOf(coordinateWGS84.getElevation()));
        gpsWgs84ElevationFeetInput  .setText(String.valueOf(coordinateWGS84.getElevationFeet()));
        gpsWgs84GeoidHeightMetersInput.setText(String.valueOf(coordinateWGS84.getGeoid()));
        gpsWgs84GeoidHeightFeetInput  .setText(String.valueOf(coordinateWGS84.getGeoidFeet()));


        gpsWgs84ConvergenceAngleInput.setText(String.valueOf(coordinateWGS84.getConvergenceAngle()));
        gpsWgs84ScaleFactorInput     .setText(String.valueOf(coordinateWGS84.getScaleFactor()));

        setColor(coordinateWGS84);

        return coordinateWGS84;

    }

    private GBCoordinateSPCS  convertSpcsInputs() {
        View v = getView();
        if (v == null)return null;

        EditText spcZoneInput            = (EditText)v.findViewById(R.id.spcZoneOutput);
        TextView spcStateOutput          = (TextView) v.findViewById(R.id.spcStateOutput);

        //SPC
        TextView spcEastingMetersOutput  = (TextView) v.findViewById(R.id.spcEastingMetersOutput);
        TextView spcNorthingMetersOutput = (TextView) v.findViewById(R.id.spcNorthingMetersOutput);
        TextView spcEastingFeetOutput    = (TextView) v.findViewById(R.id.spcEastingFeetOutput);
        TextView spcNorthingFeetOutput   = (TextView) v.findViewById(R.id.spcNorthingFeetOutput);

        //Elevation
        TextView spcsElevationMetersInput   = (TextView) v.findViewById(R.id.spcsElevationMetersInput);
        TextView spcsGeoidHeightMetersInput = (TextView) v.findViewById(R.id.spcsGeoidHeightMetersInput);
        TextView spcsElevationFeetInput     = (TextView) v.findViewById(R.id.spcsElevationFeetInput);
        TextView spcsGeoidHeightFeetInput   = (TextView) v.findViewById(R.id.spcsGeoidHeightFeetInput);

        TextView spcConvergenceOutput    = (TextView) v.findViewById(R.id.spcConvergenceOutput);
        TextView spcScaleFactorOutput    = (TextView) v.findViewById(R.id.spcScaleFactorOutput);



        GBCoordinateSPCS coordinateSPCS = new GBCoordinateSPCS(
                spcZoneInput.getText().toString(),
                spcStateOutput.getText().toString(),
                spcEastingMetersOutput.getText().toString(),
                spcEastingFeetOutput.getText().toString(),
                spcNorthingMetersOutput.getText().toString(),
                spcNorthingFeetOutput.getText().toString(),
                spcsElevationMetersInput.getText().toString(),
                spcsElevationFeetInput.getText().toString(),
                spcsGeoidHeightMetersInput.getText().toString(),
                spcsGeoidHeightFeetInput.getText().toString(),
                spcConvergenceOutput.getText().toString(),
                spcScaleFactorOutput.getText().toString());
        if (!coordinateSPCS.isValidCoordinate()){

            GBUtilities.getInstance().showStatus(getActivity(), R.string.coordinate_try_again);
            return null;
        }


        //display the coordinate values in the UI
        spcZoneInput  .setText(String.valueOf(coordinateSPCS.getZone()));
        spcStateOutput.setText(coordinateSPCS.getState());

        spcEastingMetersOutput.setText(String.valueOf(coordinateSPCS.getEasting()));
        spcEastingFeetOutput.setText(String.valueOf(coordinateSPCS.getEastingFeet()));

        spcNorthingMetersOutput  .setText(String.valueOf(coordinateSPCS.getNorthing()));
        spcNorthingFeetOutput.setText(String.valueOf(coordinateSPCS.getNorthingFeet()));

        spcsElevationMetersInput.setText(String.valueOf(coordinateSPCS.getElevation()));
        spcsElevationMetersInput.setText(String.valueOf(coordinateSPCS.getElevationFeet()));

        spcsGeoidHeightMetersInput  .setText(String.valueOf(coordinateSPCS.getGeoid()));
        spcsGeoidHeightFeetInput.setText(String.valueOf(coordinateSPCS.getGeoidFeet()));

        spcConvergenceOutput.setText(String.valueOf(coordinateSPCS.getConvergenceAngle()));
        spcScaleFactorOutput.setText(String.valueOf(coordinateSPCS.getScaleFactor()));



        setColor(coordinateSPCS);

        return coordinateSPCS;

    }

    private GBCoordinateUTM   convertUtmInputs() {
        View v = getView();
        if (v == null)return null;


        TextView utmZoneOutput           = (TextView) v.findViewById(R.id.utmZoneOutput);
        TextView utmLatbandOutput        = (TextView) v.findViewById(R.id.utmLatbandOutput);
        TextView utmHemisphereOutput     = (TextView) v.findViewById(R.id.utmHemisphereOutput);

        TextView utmEastingMetersOutput  = (TextView) v.findViewById(R.id.utmEastingMetersOutput);
        TextView utmNorthingMetersOutput = (TextView) v.findViewById(R.id.utmNorthingMetersOutput);
        TextView utmEastingFeetOutput    = (TextView) v.findViewById(R.id.utmEastingFeetOutput);
        TextView utmNorthingFeetOutput   = (TextView) v.findViewById(R.id.utmNorthingFeetOutput);

        //Elevation
        TextView utmElevationMetersInput   = (TextView) v.findViewById(R.id.utmElevationMetersInput);
        TextView utmGeoidHeightMetersInput = (TextView) v.findViewById(R.id.utmGeoidHeightMetersInput);
        TextView utmElevationFeetInput     = (TextView) v.findViewById(R.id.utmElevationFeetInput);
        TextView utmGeoidHeightFeetInput   = (TextView) v.findViewById(R.id.utmGeoidHeightFeetInput);

        TextView utmConvergenceOutput    = (TextView) v.findViewById(R.id.utmConvergence);
        TextView utmScaleFactorOutput    = (TextView) v.findViewById(R.id.utmScaleFactor);


        GBCoordinateUTM coordinateUTM = new GBCoordinateUTM(
                utmZoneOutput.getText().toString(),
                utmLatbandOutput.getText().toString(),
                utmHemisphereOutput.getText().toString(),
                utmEastingMetersOutput.getText().toString(),
                utmEastingFeetOutput.getText().toString(),
                utmNorthingMetersOutput.getText().toString(),
                utmNorthingFeetOutput.getText().toString(),
                utmElevationMetersInput.getText().toString(),
                utmElevationFeetInput.getText().toString(),
                utmGeoidHeightMetersInput.getText().toString(),
                utmGeoidHeightFeetInput.getText().toString(),
                utmConvergenceOutput.getText().toString(),
                utmScaleFactorOutput.getText().toString());
        if (!coordinateUTM.isValidCoordinate()){

            GBUtilities.getInstance().showStatus(getActivity(), R.string.coordinate_try_again);
            return null;
        }


        //display the coordinate values in the UI
        utmZoneOutput      .setText(String.valueOf(coordinateUTM.getZone()));
        utmLatbandOutput   .setText(String.valueOf(coordinateUTM.getLatBand()));
        utmHemisphereOutput.setText(String.valueOf(coordinateUTM.getHemisphere()));

        utmEastingMetersOutput.setText(String.valueOf(coordinateUTM.getEasting()));
        utmEastingFeetOutput  .setText(String.valueOf(coordinateUTM.getEastingFeet()));
        utmNorthingMetersOutput.setText(String.valueOf(coordinateUTM.getNorthing()));
        utmNorthingFeetOutput.setText(String.valueOf(coordinateUTM.getNorthingFeet()));

        utmElevationMetersInput.setText(String.valueOf(coordinateUTM.getElevation()));
        utmElevationFeetInput  .setText(String.valueOf(coordinateUTM.getElevationFeet()));
        utmGeoidHeightMetersInput.setText(String.valueOf(coordinateUTM.getGeoid()));
        utmGeoidHeightMetersInput.setText(String.valueOf(coordinateUTM.getGeoidFeet()));

        utmConvergenceOutput.setText(String.valueOf(coordinateUTM.getConvergenceAngle()));
        utmScaleFactorOutput.setText(String.valueOf(coordinateUTM.getScaleFactor()));



        setColor(coordinateUTM);

        return coordinateUTM;

    }



    //******************************************************************//
    //            Screen UI stuff                                       //
    //******************************************************************//

    private void enableManualWgsInput(boolean enableFlag){
        View v = getView();
        if (v == null)return;

        //Time
        // EditText gpsWgs84TimeOutput   = (EditText) v.findViewById(gpsWgs84TimeOutput);
        TextView gpsWgs84TimeStampOutput = (TextView) v.findViewById(R.id.gpsWgs84TimestampOutput);
        //GPS Latitude
        EditText gpsWgs84LatitudeInput   = (EditText) v.findViewById(R.id.gpsWgs84LatitudeInput);
        EditText gpsWgs84LatDegreesInput = (EditText) v.findViewById(R.id.gpsWgs84LatDegreesInput);
        EditText gpsWgs84LatMinutesInput = (EditText) v.findViewById(R.id.gpsWgs84LatMinutesInput);
        EditText gpsWgs84LatSecondsInput = (EditText) v.findViewById(R.id.gpsWgs84LatSecondsInput);

        //GPS Longitude
        EditText gpsWgs84LongitudeInput   = (EditText) v.findViewById(R.id.gpsWgs84LongitudeInput);
        EditText gpsWgs84LongDegreesInput = (EditText) v.findViewById(R.id.gpsWgs84LongDegreesInput);
        EditText gpsWgs84LongMinutesInput = (EditText) v.findViewById(R.id.gpsWgs84LongMinutesInput);
        EditText gpsWgs84LongSecondsInput = (EditText) v.findViewById(R.id.gpsWgs84LongSecondsInput);

        //Elevation
        EditText gpsWgs84ElevationMetersInput   = (EditText) v.findViewById(R.id.gpsWgs84ElevationMetersInput);
        EditText gpsWgs84GeoidHeightMetersInput = (EditText) v.findViewById(R.id.gpsWgs84GeoidHeightMetersInput);
        EditText gpsWgs84ElevationFeetInput     = (EditText) v.findViewById(R.id.gpsWgs84ElevationFeetInput);
        EditText gpsWgs84GeoidHeightFeetInput   = (EditText) v.findViewById(R.id.gpsWgs84GeoidHeightFeetInput);


        TextView gpsWgs84ConvergenceAngleInput  = (TextView) v.findViewById(R.id.gpsWgs84ConvergenceOutput);
        TextView gpsWgs84ScaleFactorInput       = (TextView) v.findViewById(R.id.gpsWgs84ScaleFactorOutput);

        //gpsWgs84TimeOutput     .setEnabled(enableFlag);
        gpsWgs84TimeStampOutput.setEnabled(enableFlag);

        gpsWgs84LatitudeInput  .setEnabled(enableFlag);
        gpsWgs84LatDegreesInput.setEnabled(enableFlag);
        gpsWgs84LatMinutesInput.setEnabled(enableFlag);
        gpsWgs84LatSecondsInput.setEnabled(enableFlag);

        gpsWgs84LongitudeInput  .setEnabled(enableFlag);
        gpsWgs84LongDegreesInput.setEnabled(enableFlag);
        gpsWgs84LongMinutesInput.setEnabled(enableFlag);
        gpsWgs84LongSecondsInput.setEnabled(enableFlag);

        gpsWgs84ElevationMetersInput  .setEnabled(enableFlag);
        gpsWgs84ElevationFeetInput    .setEnabled(enableFlag);
        gpsWgs84GeoidHeightMetersInput.setEnabled(enableFlag);
        gpsWgs84GeoidHeightFeetInput  .setEnabled(enableFlag);

        gpsWgs84ConvergenceAngleInput.setEnabled(enableFlag);
        gpsWgs84ScaleFactorInput.setEnabled(enableFlag);


    }

    private void enableManualSpcsInput(boolean enableFlag){
        View v = getView();
        if (v == null)return;

        EditText spcZoneInput            = (EditText)v.findViewById(R.id.spcZoneOutput);
        TextView spcStateOutput          = (TextView) v.findViewById(R.id.spcStateOutput);

        //SPC
        TextView spcEastingMetersOutput  = (TextView) v.findViewById(R.id.spcEastingMetersOutput);
        TextView spcNorthingMetersOutput = (TextView) v.findViewById(R.id.spcNorthingMetersOutput);
        TextView spcEastingFeetOutput    = (TextView) v.findViewById(R.id.spcEastingFeetOutput);
        TextView spcNorthingFeetOutput   = (TextView) v.findViewById(R.id.spcNorthingFeetOutput);

        //Elevation
        TextView spcsElevationMetersInput   = (TextView) v.findViewById(R.id.spcsElevationMetersInput);
        TextView spcsGeoidHeightMetersInput = (TextView) v.findViewById(R.id.spcsGeoidHeightMetersInput);
        TextView spcsElevationFeetInput     = (TextView) v.findViewById(R.id.spcsElevationFeetInput);
        TextView spcsGeoidHeightFeetInput   = (TextView) v.findViewById(R.id.spcsGeoidHeightFeetInput);

        TextView spcConvergenceOutput    = (TextView) v.findViewById(R.id.spcConvergenceOutput);
        TextView spcScaleFactorOutput    = (TextView) v.findViewById(R.id.spcScaleFactorOutput);

        spcZoneInput           .setEnabled(true);//always enabled for input, regardless

        spcStateOutput         .setEnabled(enableFlag);
        spcEastingMetersOutput .setEnabled(enableFlag);
        spcNorthingMetersOutput.setEnabled(enableFlag);
        spcEastingFeetOutput   .setEnabled(enableFlag);
        spcNorthingFeetOutput  .setEnabled(enableFlag);

        spcConvergenceOutput.setEnabled(enableFlag);
        spcScaleFactorOutput.setEnabled(enableFlag);

    }

    private void enableManualUtmInput(boolean enableFlag){
        View v = getView();
        if (v == null)return;

        TextView utmZoneOutput           = (TextView) v.findViewById(R.id.utmZoneOutput);
        TextView utmLatbandOutput        = (TextView) v.findViewById(R.id.utmLatbandOutput);
        TextView utmHemisphereOutput     = (TextView) v.findViewById(R.id.utmHemisphereOutput);
        TextView utmEastingMetersOutput  = (TextView) v.findViewById(R.id.utmEastingMetersOutput);
        TextView utmNorthingMetersOutput = (TextView) v.findViewById(R.id.utmNorthingMetersOutput);
        TextView utmEastingFeetOutput    = (TextView) v.findViewById(R.id.utmEastingFeetOutput);
        TextView utmNorthingFeetOutput   = (TextView) v.findViewById(R.id.utmNorthingFeetOutput);

        TextView utmConvergenceOutput    = (TextView) v.findViewById(R.id.utmConvergence);
        TextView utmScaleFactorOutput    = (TextView) v.findViewById(R.id.utmScaleFactor);

        utmZoneOutput          .setEnabled(enableFlag);

        utmLatbandOutput       .setEnabled(enableFlag);
        utmHemisphereOutput    .setEnabled(enableFlag);
        utmEastingMetersOutput .setEnabled(enableFlag);
        utmNorthingMetersOutput.setEnabled(enableFlag);

        utmEastingFeetOutput   .setEnabled(enableFlag);
        utmNorthingFeetOutput  .setEnabled(enableFlag);
        utmConvergenceOutput   .setEnabled(enableFlag);
        utmScaleFactorOutput   .setEnabled(enableFlag);

    }


    private void clearForm() {
        View v = getView();
        if (v == null)return;

        clearGPSUI(v);

        clearMeanUI(v);

        clearSPCSUI(v);

        clearUtmUI(v);

        //clearNadUI(v);


    }

    private void clearMeanUI(View v){

        //Mean Parameters
        //TextView meanWgs84StartTimeOutput    = (TextView)v.findViewById(meanWgs84StartTimeOutput);
        //TextView meanWgs84EndTimeOutput      = (TextView)v.findViewById(meanWgs84EndTimeOutput);
        TextView meanWgs84PointsInMeanOutput = (TextView)v.findViewById(R.id.meanWgs84PointsInMeanOutput);
        TextView meanWgs84StartTimestampOutput = (TextView) v.findViewById(R.id.meanWgs84StartTimestampOutput);
        TextView meanWgs84EndTimestampOutput   = (TextView) v.findViewById(R.id.meanWgs84EndTimestampOutput);


        //Mean Standard Deviations
        TextView meanWgs84LatSigmaOutput = (TextView)v.findViewById(R.id.meanWgs84LatSigmaOutput);
        TextView meanWgs84LongSigmaOutput= (TextView)v.findViewById(R.id.meanWgs84LongSigmaOutput);
        TextView meanWgs84ElevSigmaOutput= (TextView)v.findViewById(R.id.meanWgs84ElevSigmaOutput);

        //Mean Latitude
        TextView meanWgs84LatitudeInput   = (TextView) v.findViewById(R.id.meanWgs84LatitudeInput);
        TextView meanWgs84LatDegreesInput = (TextView) v.findViewById(R.id.meanWgs84LatDegreesInput);
        TextView meanWgs84LatMinutesInput = (TextView) v.findViewById(R.id.meanWgs84LatMinutesInput);
        TextView meanWgs84LatSecondsInput = (TextView) v.findViewById(R.id.meanWgs84LatSecondsInput);

        TextView meanWgs84LongitudeInput   = (TextView) v.findViewById(R.id.meanWgs84LongitudeInput);
        TextView meanWgs84LongDegreesInput = (TextView) v.findViewById(R.id.meanWgs84LongDegreesInput);
        TextView meanWgs84LongMinutesInput = (TextView) v.findViewById(R.id.meanWgs84LongMinutesInput);
        TextView meanWgs84LongSecondsInput = (TextView) v.findViewById(R.id.meanWgs84LongSecondsInput);

        //Elevation
        TextView meanWgs84ElevationMetersInput   = (TextView) v.findViewById(
                                                                R.id.meanWgs84ElevationMetersInput);
        TextView meanWgs84ElevationFeetInput     = (TextView) v.findViewById(
                                                                R.id.meanWgs84ElevationFeetInput);
        TextView meanWgs84GeoidHeightMetersInput = (TextView) v.findViewById(
                                                                R.id.meanWgs84GeoidHeightMetersInput);
        TextView meanWgs84GeoidHeightFeetInput   = (TextView) v.findViewById(
                                                                R.id.meanWgs84GeoidHeightFeetInput);



        //meanWgs84StartTimeOutput   .setText("");
        //meanWgs84EndTimeOutput     .setText("");
        meanWgs84StartTimestampOutput.setText("");
        meanWgs84EndTimestampOutput.setText("");
        meanWgs84PointsInMeanOutput.setText("");

        meanWgs84LatSigmaOutput     .setText("");
        meanWgs84LongSigmaOutput    .setText("");
        meanWgs84ElevSigmaOutput    .setText("");


        meanWgs84LatitudeInput     .setText("");
        meanWgs84LatDegreesInput   .setText("");
        meanWgs84LatMinutesInput   .setText("");
        meanWgs84LatSecondsInput   .setText("");

        meanWgs84LongitudeInput     .setText("");
        meanWgs84LongDegreesInput   .setText("");
        meanWgs84LongMinutesInput   .setText("");
        meanWgs84LongSecondsInput   .setText("");

        meanWgs84ElevationMetersInput  .setText("");
        meanWgs84ElevationFeetInput    .setText("");
        meanWgs84GeoidHeightMetersInput.setText("");
        meanWgs84GeoidHeightFeetInput  .setText("");


    }

    private void clearGPSUI(View v){

        //GPS Latitude
        TextView gpsWgs84LatitudeInput   = (TextView) v.findViewById(R.id.gpsWgs84LatitudeInput);
        TextView gpsWgs84LatDegreesInput = (TextView) v.findViewById(R.id.gpsWgs84LatDegreesInput);
        TextView gpsWgs84LatMinutesInput = (TextView) v.findViewById(R.id.gpsWgs84LatMinutesInput);
        TextView gpsWgs84LatSecondsInput = (TextView) v.findViewById(R.id.gpsWgs84LatSecondsInput);

        //GPS Longitude
        TextView gpsWgs84LongitudeInput   = (TextView) v.findViewById(R.id.gpsWgs84LongitudeInput);
        TextView gpsWgs84LongDegreesInput = (TextView) v.findViewById(R.id.gpsWgs84LongDegreesInput);
        TextView gpsWgs84LongMinutesInput = (TextView) v.findViewById(R.id.gpsWgs84LongMinutesInput);
        TextView gpsWgs84LongSecondsInput = (TextView) v.findViewById(R.id.gpsWgs84LongSecondsInput);

        //Elevation
        TextView gpsWgs84ElevationMetersInput   = (TextView) v.findViewById(R.id.gpsWgs84ElevationMetersInput);
        TextView gpsWgs84GeoidHeightMetersInput = (TextView) v.findViewById(R.id.gpsWgs84GeoidHeightMetersInput);
        TextView gpsWgs84ElevationFeetInput     = (TextView) v.findViewById(R.id.gpsWgs84ElevationFeetInput);
        TextView gpsWgs84GeoidHeightFeetInput   = (TextView) v.findViewById(R.id.gpsWgs84GeoidHeightFeetInput);


        gpsWgs84LatitudeInput     .setText("");
        gpsWgs84LatDegreesInput   .setText("");
        gpsWgs84LatMinutesInput   .setText("");
        gpsWgs84LatSecondsInput   .setText("");

        gpsWgs84LongitudeInput     .setText("");
        gpsWgs84LongDegreesInput   .setText("");
        gpsWgs84LongMinutesInput   .setText("");
        gpsWgs84LongSecondsInput   .setText("");

        gpsWgs84ElevationMetersInput  .setText("");
        gpsWgs84ElevationFeetInput    .setText("");
        gpsWgs84GeoidHeightMetersInput.setText("");
        gpsWgs84GeoidHeightFeetInput  .setText("");

    }

    private void clearUtmUI(View v){
        TextView utmZoneOutput           = (TextView) v.findViewById(R.id.utmZoneOutput);

        TextView utmLatbandOutput        = (TextView) v.findViewById(R.id.utmLatbandOutput);
        TextView utmHemisphereOutput     = (TextView) v.findViewById(R.id.utmHemisphereOutput);

        TextView utmEastingMetersOutput  = (TextView) v.findViewById(R.id.utmEastingMetersOutput);
        TextView utmNorthingMetersOutput = (TextView) v.findViewById(R.id.utmNorthingMetersOutput);
        TextView utmEastingFeetOutput    = (TextView) v.findViewById(R.id.utmEastingFeetOutput);
        TextView utmNorthingFeetOutput   = (TextView) v.findViewById(R.id.utmNorthingFeetOutput);

        //Elevation
        TextView utmElevationMetersInput   = (TextView) v.findViewById(R.id.utmElevationMetersInput);
        TextView utmGeoidHeightMetersInput = (TextView) v.findViewById(R.id.utmGeoidHeightMetersInput);
        TextView utmElevationFeetInput     = (TextView) v.findViewById(R.id.utmElevationFeetInput);
        TextView utmGeoidHeightFeetInput   = (TextView) v.findViewById(R.id.utmGeoidHeightFeetInput);


        TextView utmConvergenceOutput    = (TextView) v.findViewById(R.id.utmConvergence);
        TextView utmScaleFactorOutput    = (TextView) v.findViewById(R.id.utmScaleFactor);


        utmZoneOutput.          setText("");
        utmLatbandOutput.       setText("");
        utmHemisphereOutput.    setText("");

        utmEastingMetersOutput. setText("");
        utmNorthingMetersOutput.setText("");
        utmEastingFeetOutput.   setText("");
        utmNorthingFeetOutput.  setText("");

        utmElevationMetersInput. setText("");
        utmElevationFeetInput   .setText("");
        utmGeoidHeightMetersInput.setText("");
        utmGeoidHeightFeetInput. setText("");

        utmConvergenceOutput.   setText("");
        utmScaleFactorOutput.   setText("");

    }


    private void clearSPCSUI(View v){

        //SPC
        TextView spcZoneOutput           = (TextView) v.findViewById(R.id.spcZoneOutput);
        TextView spcStateOutput          = (TextView) v.findViewById(R.id.spcStateOutput);

        TextView spcEastingMetersOutput  = (TextView) v.findViewById(R.id.spcEastingMetersOutput);
        TextView spcNorthingMetersOutput = (TextView) v.findViewById(R.id.spcNorthingMetersOutput);
        TextView spcEastingFeetOutput    = (TextView) v.findViewById(R.id.spcEastingFeetOutput);
        TextView spcNorthingFeetOutput   = (TextView) v.findViewById(R.id.spcNorthingFeetOutput);

        //Elevation
        TextView spcsElevationMetersInput   = (TextView) v.findViewById(R.id.spcsElevationMetersInput);
        TextView spcsGeoidHeightMetersInput = (TextView) v.findViewById(R.id.spcsGeoidHeightMetersInput);
        TextView spcsElevationFeetInput     = (TextView) v.findViewById(R.id.spcsElevationFeetInput);
        TextView spcsGeoidHeightFeetInput   = (TextView) v.findViewById(R.id.spcsGeoidHeightFeetInput);

        TextView spcConvergenceOutput    = (TextView) v.findViewById(R.id.spcConvergenceOutput);
        TextView spcScaleFactorOutput    = (TextView) v.findViewById(R.id.spcScaleFactorOutput);


        spcZoneOutput          .setText("");
        spcStateOutput         .setText("");

        spcEastingMetersOutput .setText("");
        spcNorthingMetersOutput.setText("");
        spcEastingFeetOutput   .setText("");
        spcNorthingFeetOutput  .setText("");

        spcsElevationMetersInput. setText("");
        spcsElevationFeetInput   .setText("");
        spcsGeoidHeightMetersInput.setText("");
        spcsGeoidHeightFeetInput. setText("");

        spcConvergenceOutput   .setText("");
        spcScaleFactorOutput   .setText("");


    }


    private void setColor(GBCoordinateWGS84 coordinateWGS84){
        View v = getView();
        if (v == null)return;

        //Time
        //EditText gpsWgs84TimeOutput      = (EditText) v.findViewById(gpsWgs84TimeOutput);
        TextView gpsWgs84TimeStampOutput = (TextView) v.findViewById(R.id.gpsWgs84TimestampOutput);
        //GPS Latitude
        EditText gpsWgs84LatitudeInput   = (EditText) v.findViewById(R.id.gpsWgs84LatitudeInput);
        EditText gpsWgs84LatDegreesInput = (EditText) v.findViewById(R.id.gpsWgs84LatDegreesInput);
        EditText gpsWgs84LatMinutesInput = (EditText) v.findViewById(R.id.gpsWgs84LatMinutesInput);
        EditText gpsWgs84LatSecondsInput = (EditText) v.findViewById(R.id.gpsWgs84LatSecondsInput);

        //GPS Longitude
        EditText gpsWgs84LongitudeInput   = (EditText) v.findViewById(R.id.gpsWgs84LongitudeInput);
        EditText gpsWgs84LongDegreesInput = (EditText) v.findViewById(R.id.gpsWgs84LongDegreesInput);
        EditText gpsWgs84LongMinutesInput = (EditText) v.findViewById(R.id.gpsWgs84LongMinutesInput);
        EditText gpsWgs84LongSecondsInput = (EditText) v.findViewById(R.id.gpsWgs84LongSecondsInput);

        //Elevation
        EditText gpsWgs84ElevationMetersInput   = (EditText) v.findViewById(R.id.gpsWgs84ElevationMetersInput);
        EditText gpsWgs84GeoidHeightMetersInput = (EditText) v.findViewById(R.id.gpsWgs84GeoidHeightMetersInput);
        EditText gpsWgs84ElevationFeetInput     = (EditText) v.findViewById(R.id.gpsWgs84ElevationFeetInput);
        EditText gpsWgs84GeoidHeightFeetInput   = (EditText) v.findViewById(R.id.gpsWgs84GeoidHeightFeetInput);


        TextView gpsWgs84ConvergenceAngleInput  = (TextView) v.findViewById(R.id.gpsWgs84ConvergenceOutput);
        TextView gpsWgs84ScaleFactorInput       = (TextView) v.findViewById(R.id.gpsWgs84ScaleFactorOutput);

        int uiColor;
        if (coordinateWGS84.getLatitude() >= 0.0) {
            uiColor = colorPosNumber;

        } else {
            uiColor = colorNegNumber;
        }


        gpsWgs84LatitudeInput  .setTextColor(ContextCompat.getColor(getActivity(), uiColor));
        gpsWgs84LatDegreesInput.setTextColor(ContextCompat.getColor(getActivity(), uiColor));
        gpsWgs84LatMinutesInput.setTextColor(ContextCompat.getColor(getActivity(), uiColor));
        gpsWgs84LatSecondsInput.setTextColor(ContextCompat.getColor(getActivity(), uiColor));

        if (coordinateWGS84.getLongitude() >= 0.0) {
            uiColor = colorPosNumber;

        } else {
            uiColor = colorNegNumber;
        }

        gpsWgs84LongitudeInput  .setTextColor(ContextCompat.getColor(getActivity(), uiColor));
        gpsWgs84LongDegreesInput.setTextColor(ContextCompat.getColor(getActivity(), uiColor));
        gpsWgs84LongMinutesInput.setTextColor(ContextCompat.getColor(getActivity(), uiColor));
        gpsWgs84LongSecondsInput.setTextColor(ContextCompat.getColor(getActivity(), uiColor));

        if (coordinateWGS84.getElevation() >= 0.0) {
            uiColor = colorPosNumber;

        } else {
            uiColor = colorNegNumber;
        }

        gpsWgs84ElevationMetersInput.setTextColor(ContextCompat.getColor(getActivity(), uiColor));
        gpsWgs84ElevationFeetInput  .setTextColor(ContextCompat.getColor(getActivity(), uiColor));


        if (coordinateWGS84.getGeoid() >= 0.0) {
            uiColor = colorPosNumber;

        } else {
            uiColor = colorNegNumber;
        }

        gpsWgs84GeoidHeightMetersInput.setTextColor(ContextCompat.getColor(getActivity(), uiColor));
        gpsWgs84GeoidHeightFeetInput  .setTextColor(ContextCompat.getColor(getActivity(), uiColor));

        if (coordinateWGS84.getConvergenceAngle() >= 0.0) {
            uiColor = colorPosNumber;

        } else {
            uiColor = colorNegNumber;
        }

        gpsWgs84ConvergenceAngleInput.setTextColor(ContextCompat.getColor(getActivity(),uiColor));

        if (coordinateWGS84.getScaleFactor() >= 0.0) {
            uiColor = colorPosNumber;

        } else {
            uiColor = colorNegNumber;
        }

        gpsWgs84ScaleFactorInput     .setTextColor(ContextCompat.getColor(getActivity(),uiColor));

    }

    private void setColor(GBCoordinateSPCS coordinateSPCS){
        View v = getView();
        if (v == null)return;

        //SPC
        TextView spcZoneOutput           = (TextView) v.findViewById(R.id.spcZoneOutput);
        TextView spcStateOutput          = (TextView) v.findViewById(R.id.spcStateOutput);

        TextView spcEastingMetersOutput  = (TextView) v.findViewById(R.id.spcEastingMetersOutput);
        TextView spcNorthingMetersOutput = (TextView) v.findViewById(R.id.spcNorthingMetersOutput);
        TextView spcEastingFeetOutput    = (TextView) v.findViewById(R.id.spcEastingFeetOutput);
        TextView spcNorthingFeetOutput   = (TextView) v.findViewById(R.id.spcNorthingFeetOutput);

        //Elevation
        TextView spcsElevationMetersInput   = (TextView) v.findViewById(R.id.spcsElevationMetersInput);
        TextView spcsGeoidHeightMetersInput = (TextView) v.findViewById(R.id.spcsGeoidHeightMetersInput);
        TextView spcsElevationFeetInput     = (TextView) v.findViewById(R.id.spcsElevationFeetInput);
        TextView spcsGeoidHeightFeetInput   = (TextView) v.findViewById(R.id.spcsGeoidHeightFeetInput);

        TextView spcConvergenceOutput    = (TextView) v.findViewById(R.id.spcConvergenceOutput);
        TextView spcScaleFactorOutput    = (TextView) v.findViewById(R.id.spcScaleFactorOutput);

        int uiColor;
        if (coordinateSPCS.getEasting() >= 0.0) {
            uiColor = colorPosNumber;

        } else {
            uiColor = colorNegNumber;
        }


        spcEastingMetersOutput  .setTextColor(ContextCompat.getColor(getActivity(), uiColor));
        spcEastingFeetOutput.setTextColor(ContextCompat.getColor(getActivity(), uiColor));


        if (coordinateSPCS.getNorthing() >= 0.0) {
            uiColor = colorPosNumber;

        } else {
            uiColor = colorNegNumber;
        }

        spcNorthingMetersOutput  .setTextColor(ContextCompat.getColor(getActivity(), uiColor));
        spcNorthingFeetOutput    .setTextColor(ContextCompat.getColor(getActivity(), uiColor));

        if (coordinateSPCS.getElevation() >= 0.0) {
            uiColor = colorPosNumber;

        } else {
            uiColor = colorNegNumber;
        }

        spcsElevationMetersInput.setTextColor(ContextCompat.getColor(getActivity(), uiColor));
        spcsElevationFeetInput  .setTextColor(ContextCompat.getColor(getActivity(), uiColor));


        if (coordinateSPCS.getGeoid() >= 0.0) {
            uiColor = colorPosNumber;

        } else {
            uiColor = colorNegNumber;
        }

        spcsGeoidHeightMetersInput.setTextColor(ContextCompat.getColor(getActivity(), uiColor));
        spcsGeoidHeightFeetInput  .setTextColor(ContextCompat.getColor(getActivity(), uiColor));


        if (coordinateSPCS.getConvergenceAngle() >= 0.0) {
            uiColor = colorPosNumber;

        } else {
            uiColor = colorNegNumber;
        }

        spcConvergenceOutput.setTextColor(ContextCompat.getColor(getActivity(), uiColor));

        if (coordinateSPCS.getScaleFactor() >= 0.0) {
            uiColor = colorPosNumber;

        } else {
            uiColor = colorNegNumber;
        }

        spcScaleFactorOutput.setTextColor(ContextCompat.getColor(getActivity(), uiColor));

    }

    private void setColor(GBCoordinateUTM coordinateUTM){
        View v = getView();
        if (v == null)return;


        TextView utmEastingMetersOutput  = (TextView) v.findViewById(R.id.utmEastingMetersOutput);
        TextView utmNorthingMetersOutput = (TextView) v.findViewById(R.id.utmNorthingMetersOutput);
        TextView utmEastingFeetOutput    = (TextView) v.findViewById(R.id.utmEastingFeetOutput);
        TextView utmNorthingFeetOutput   = (TextView) v.findViewById(R.id.utmNorthingFeetOutput);

        //Elevation
        TextView utmElevationMetersInput   = (TextView) v.findViewById(R.id.utmElevationMetersInput);
        TextView utmGeoidHeightMetersInput = (TextView) v.findViewById(R.id.utmGeoidHeightMetersInput);
        TextView utmElevationFeetInput     = (TextView) v.findViewById(R.id.utmElevationFeetInput);
        TextView utmGeoidHeightFeetInput   = (TextView) v.findViewById(R.id.utmGeoidHeightFeetInput);


        TextView utmConvergenceOutput    = (TextView) v.findViewById(R.id.utmConvergence);
        TextView utmScaleFactorOutput    = (TextView) v.findViewById(R.id.utmScaleFactor);

        int uiColor;

        if (coordinateUTM.getEasting() >= 0.0) {
            uiColor = colorPosNumber;

        } else {
            uiColor = colorNegNumber;
        }


        utmEastingMetersOutput  .setTextColor(ContextCompat.getColor(getActivity(), uiColor));
        utmEastingFeetOutput.setTextColor(ContextCompat.getColor(getActivity(), uiColor));


        if (coordinateUTM.getNorthing() >= 0.0) {
            uiColor = colorPosNumber;

        } else {
            uiColor = colorNegNumber;
        }

        utmNorthingMetersOutput  .setTextColor(ContextCompat.getColor(getActivity(), uiColor));
        utmNorthingFeetOutput    .setTextColor(ContextCompat.getColor(getActivity(), uiColor));

        if (coordinateUTM.getElevation() >= 0.0) {
            uiColor = colorPosNumber;

        } else {
            uiColor = colorNegNumber;
        }

        utmElevationMetersInput.setTextColor(ContextCompat.getColor(getActivity(), uiColor));
        utmElevationFeetInput  .setTextColor(ContextCompat.getColor(getActivity(), uiColor));


        if (coordinateUTM.getGeoid() >= 0.0) {
            uiColor = colorPosNumber;

        } else {
            uiColor = colorNegNumber;
        }

        utmGeoidHeightMetersInput.setTextColor(ContextCompat.getColor(getActivity(), uiColor));
        utmGeoidHeightFeetInput  .setTextColor(ContextCompat.getColor(getActivity(), uiColor));

        if (coordinateUTM.getConvergenceAngle() >= 0.0) {
            uiColor = colorPosNumber;

        } else {
            uiColor = colorNegNumber;
        }

        utmConvergenceOutput.setTextColor(ContextCompat.getColor(getActivity(), uiColor));

        if (coordinateUTM.getScaleFactor() >= 0.0) {
            uiColor = colorPosNumber;

        } else {
            uiColor = colorNegNumber;
        }

        utmScaleFactorOutput.setTextColor(ContextCompat.getColor(getActivity(), uiColor));

    }



    //+**************  GPS Stuff  ****************************
    private void initializeGPS(){
        if (ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED){return;}

        locationManager = (LocationManager)getActivity().getSystemService(Context.LOCATION_SERVICE);

    }


    private void startGps(){

        // TODO: 6/23/2017 Show gps status on UI
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED){return;}


        locationManager = (LocationManager)getActivity().getSystemService(Context.LOCATION_SERVICE);

        //ask the Location Manager to start sending us updates
        locationManager.requestLocationUpdates("gps", 0, 0.0f, this);
        //locationManager.addGpsStatusListener(this);
        locationManager.addNmeaListener(this);

        isGpsOn = true;
        updateGpsUI(isGpsOn);
    }

    private void stopGps() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED){return;}

        locationManager = (LocationManager)getActivity().getSystemService(Context.LOCATION_SERVICE);

        locationManager.removeUpdates(this);
        //locationManager.removeGpsStatusListener(this);
        locationManager.removeNmeaListener(this);

        isGpsOn = false;
        updateGpsUI(isGpsOn);
    }

    //+*******************************************

    //+**********************************************

    //******************************************************************//
    //             GPS Listener Callbacks                               //
    //            Called by the OS to handle GPS events                 //
    //******************************************************************//

    //GpsStatus.Listener Callback


    //OS calls this callback when
    // a change has been detected in GPS satellite status
    //Called to report changes in the GPS status.

    // The parameter event type is one of:

    // o GPS_EVENT_STARTED
    // o GPS_EVENT_STOPPED
    // o GPS_EVENT_FIRST_FIX
    // o GPS_EVENT_SATELLITE_STATUS

    //When this method is called,
    // the client should call getGpsStatus(GpsStatus)
    // to get additional status information.
    @Override
    public void onGpsStatusChanged(int eventType) {
        //setGpsStatus();
    }



    //******************************************************************//
    //             Location Listener Callbacks                          //
    //            Called by the OS to handle GPS events                 //
    //******************************************************************//

    // called when the GPS provider is turned off
    // (i.e. user turning off the GPS on the phone)
    // If requestLocationUpdates is called on an already disabled provider,
    // this method is called immediately.
    @Override
    public void onProviderDisabled(String provider) {
        if (LocationManager.GPS_PROVIDER.equals(provider)){
            //setGpsStatus();
        }
    }

    // called when the GPS provider is turned on
    // (i.e. user turning on the GPS on the phone)
    @Override
    public void onProviderEnabled(String provider) {
        if (LocationManager.GPS_PROVIDER.equals(provider)){
            //setGpsStatus();
        }
    }

    //Called when the provider status changes.
    // This method is called when
    // o a provider is unable to fetch a location or
    // o if the provider has recently become available
    //    after a period of unavailability.
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        if (LocationManager.GPS_PROVIDER.equals(provider)){
            //setGpsStatus();


        }

    }


    // called when the listener is notified with a location update from the GPS
    @Override
    public void onLocationChanged(Location loc) {
        mCurLocation = new Location(loc); // copy location
    }



    //******************************************************************//
    //             NMEA Listener Callbacks                              //
    //            Called by the OS to handle GPS events                 //
    //******************************************************************//
    @Override
    public void onNmeaReceived(long timestamp, String nmea) {
        handleNmeaReceived(timestamp, nmea);
    }

}


