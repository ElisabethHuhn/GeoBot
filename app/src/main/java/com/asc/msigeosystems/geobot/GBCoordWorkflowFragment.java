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
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * The Collect Fragment is the UI
 * when the workflow from WGS84 GPS to NAD83 to UTM/State Plane Coordinates
 * Created by Elisabeth Huhn on 6/15/2016.
 */
public class GBCoordWorkflowFragment extends Fragment implements GpsStatus.Listener, LocationListener, GpsStatus.NmeaListener {

    private static GBNmeaParser mNmeaParser = new GBNmeaParser();
    private LocationManager     mLocationManager;
    private GBNmea              mNmeaData; //latest nmea sentence received
    private GpsStatus           mGpsStatus = null;
    private Location            mCurLocation;

    //Data that must survive a reconfigure
            int     counter = 0;

    private boolean isMeanInProgress   = false;
    private boolean isFirstPointInMean = false;
    private boolean isLastPointInMean  = false;
    private boolean isGpsOn            = true;

    private GBCoordinateWGS84 mMeanCoordinateWGS84;

    private List<GBCoordinateWGS84> mMeanWgs84List;






    private GBCoordinateWGS84 mWSG84Coordinate;

    /*  Not used, but maybe they should be
    private double mConvergence;
    private double mScale;
    */


    public GBCoordWorkflowFragment() {
        //for now, we don't need to initialize anything when the fragment
        //  is first created
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_coord_workflow, container, false);

        wireWidgets(v);

        if (ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){return v;}

        mLocationManager = (LocationManager)getActivity().getSystemService(Context.LOCATION_SERVICE);




        return v;
    }


    //Ask for location events to start
    @Override
    public void onResume() {
        super.onResume();

        startGps();
        setSubtitle();
    }

    private void setSubtitle() {
        ((GBActivity) getActivity()).setSubtitle(R.string.subtitle_workflow);
    }

    @Override
    public void onStop(){
        super.onStop();
    }

    //Ask for location events to stop
    @Override
    public void onPause() {
        super.onPause();
        //If we don't currently have permission, bail
       stopGps();
    }

    private void startGps(){
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                         != PackageManager.PERMISSION_GRANTED){return;}

        //ask the Location Manager to start sending us updates
        mLocationManager.requestLocationUpdates("gps", 0, 0.0f, this);
        //mLocationManager.addGpsStatusListener(this);
        mLocationManager.addNmeaListener(this);

        setGpsStatus();
    }

    private void stopGps() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED){return;}

        mLocationManager.removeUpdates(this);
        //mLocationManager.removeGpsStatusListener(this);
        mLocationManager.removeNmeaListener(this);
    }

    private void wireWidgets(View v) {
        //Wire up the UI widgets so they can handle events later
        //For now ignore the text view widgets, as this is just a mockup
        //      for the real screen we'll have to actually fill the fields

        //TextView's and EditText's do not have any Listeners, so they are not needed here
        //They will be fetched from getView() "just in time" for when they are needed
        //But keep this list for convenience, as the entire View Hierarchy is listed in one spot
        /* **************************************************************************************
        //Time
        TextView gpsWgs84TimeOutput   = (TextView) v.findViewById(R.id.gpsWgs84TimeOutput);
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

        //Mean Parameters
        TextView meanWgs84StartTimeOutput    = (TextView)v.findViewById(R.id.meanWgs84StartTimeOutput);
        TextView meanWgs84EndTimeOutput      = (TextView)v.findViewById(R.id.meanWgs84EndTimeOutput);
        TextView meanWgs84PointsInMeanOutput = (TextView)v.findViewById(R.id.meanWgs84PointsInMeanOutput);


        //Mean Standard Deviations
        TextView meanWgs84LatSigmaOutput = (TextView)v.findViewById(R.id.meanWgs84LatSigmaOutput);
        TextView meanWgs84LongSigmaOutput= (TextView)v.findViewById(R.id.meanWgs84LongSigmaOutput);
        TextView meanWgs84ElevSigmaOutput= (TextView)v.findViewById(R.id.meanWgs84ElevSigmaOutput);


        //NAD83 Latitude
        TextView nad83LatitudeInput   = (TextView) v.findViewById(R.id.nad83LatitudeInput);
        TextView nad83LatDegreesInput = (TextView) v.findViewById(R.id.nad83LatDegreesInput);
        TextView nad83LatMinutesInput = (TextView) v.findViewById(R.id.nad83LatMinutesInput);
        TextView nad83LatSecondsInput = (TextView) v.findViewById(R.id.nad83LatSecondsInput);

        //NAD83 Longitude
        TextView nad83LongitudeInput   = (TextView) v.findViewById(R.id.nad83LongitudeInput);
        TextView nad83LongDegreesInput = (TextView) v.findViewById(R.id.nad83LongDegreesInput);
        TextView nad83LongMinutesInput = (TextView) v.findViewById(R.id.nad83LongMinutesInput);
        TextView nad83LongSecondsInput = (TextView) v.findViewById(R.id.nad83LongSecondsInput);

        //Elevation
        TextView nad83ElevationMetersInput = (TextView) v.findViewById(R.id.nad83ElevationMetersInput);
        TextView nad83ElevationFeetInput = (TextView) v.findViewById(R.id.nad83ElevationFeetInput);
        TextView nad83GeoidHeightMetersInput = (TextView) v.findViewById(R.id.nad83GeoidHeightMetersInput);
        TextView nad83GeoidHeightFeetInput = (TextView) v.findViewById(R.id.nad83GeoidHeightFeetInput);

        //SPC
        TextView SpcZoneOutput           =  (TextView) v.findViewById(R.id.spcZoneOutput);
        TextView SpcLatbandOutput        =  (TextView) v.findViewById(R.id.spcLatbandOutput);
        TextView SpcHemisphereOutput     =  (TextView) v.findViewById(R.id.spcHemisphereOutput);
        TextView SpcEastingMetersOutput  =  (TextView) v.findViewById(R.id.spcEastingMetersOutput);
        TextView SpcNorthingMetersOutput =  (TextView) v.findViewById(R.id.spcNorthingMetersOutput);
        TextView SpcEastingFeetOutput    =  (TextView) v.findViewById(R.id.spcEastingFeetOutput);
        TextView SpcNorthingFeetOutput   =  (TextView) v.findViewById(R.id.spcNorthingFeetOutput);

        TextView SpcConvergenceOutput    =  (TextView) v.findViewById(R.id.spcConvergenceOutput);
        TextView SpcScaleFactorOutput    =  (TextView) v.findViewById(R.id.spcScaleFactorOutput);

        //UTM
        TextView utmZoneOutput           =  (TextView) v.findViewById(R.id.utmZoneOutput);
        TextView utmLatbandOutput        =  (TextView) v.findViewById(R.id.utmLatbandOutput);
        TextView utmHemisphereOutput     =  (TextView) v.findViewById(R.id.utmHemisphereOutput);
        TextView utmEastingMetersOutput  =  (TextView) v.findViewById(R.id.utmEastingMetersOutput);
        TextView utmNorthingMetersOutput =  (TextView) v.findViewById(R.id.utmNorthingMetersOutput);
        TextView utmEastingFeetOutput    =  (TextView) v.findViewById(R.id.utmEastingFeetOutput);
        TextView utmNorthingFeetOutput   =  (TextView) v.findViewById(R.id.utmNorthingFeetOutput);

        TextView utmConvergenceOutput    =  (TextView) v.findViewById(R.id.utmConvergence);
        TextView utmScaleFactorOutput    =  (TextView) v.findViewById(R.id.utmScaleFactor);

        *************************************************************************************/

        //Start GPS Button
        Button startGpsButton = (Button) v.findViewById(R.id.startGpsButton);
        startGpsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){


                Toast.makeText(getActivity(),
                        R.string.start_gps_button_label,
                        Toast.LENGTH_SHORT).show();

                startGps();
                isGpsOn = true;

            }//End on Click
        });

        //Stop GPS Button
        Button stopGpsButton = (Button) v.findViewById(R.id.stopGPSButton);
        stopGpsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){

                Toast.makeText(getActivity(),
                        R.string.stop_gps_button_label,
                        Toast.LENGTH_SHORT).show();

                stopGps();
                isGpsOn = false;

            }//End on Click
        });

        //Start Mean Button
        Button startMeanButton = (Button) v.findViewById(R.id.startMeanButton);
        startMeanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){

                Toast.makeText(getActivity(),
                        R.string.start_mean_button_label,
                        Toast.LENGTH_SHORT).show();

                //set flags to start taking mean
                mMeanWgs84List = new ArrayList<>();
                isFirstPointInMean = true;
                isMeanInProgress = true;



            }//End on Click
        });

        //Stop Mean Button
        Button stopMeanButton = (Button) v.findViewById(R.id.stopMeanButton);
        stopMeanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){

                Toast.makeText(getActivity(),
                        R.string.stop_mean_button_label,
                        Toast.LENGTH_SHORT).show();
                //set flags that mean is done
                isLastPointInMean = true;

            }//End on Click
        });





        //Conversion Button
        Button convertButton = (Button) v.findViewById(R.id.convertButton);
        convertButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){

                //performConversion();

                Toast.makeText(getActivity(),
                        R.string.conversion_stub,
                        Toast.LENGTH_SHORT).show();

                convertKarney();

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
        setGpsStatus();
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
            setGpsStatus();
        }
    }

    // called when the GPS provider is turned on
    // (i.e. user turning on the GPS on the phone)
    @Override
    public void onProviderEnabled(String provider) {
        if (LocationManager.GPS_PROVIDER.equals(provider)){
            setGpsStatus();
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
            setGpsStatus();


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
        //todo maybe need to do something with the timestamp
        try {
            //create an object with all the fields from the string
            mNmeaData = mNmeaParser.parse(nmea);

            if (!(mNmeaData == null)){
                //update the UI
                updateNmeaUI(mNmeaData);

                //save the raw data
                //get the nmea container
                GBNmeaManager nmeaManager = GBNmeaManager.getInstance();
                nmeaManager.add(mNmeaData);
            }


        } catch (RuntimeException e){
            //there was an exception processing the NMEA Sentence
            Toast toast = Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT);
            toast.show();
            //throw new RuntimeException(e);
        }
    }


    //******************** Callback Utilities *****************//
    //        Called by OS when a Listener condition is met       //
    //************************************************************//


    //Update the UI with satellite status info from GPS
    protected void setGpsStatus(){
        if (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            //location manager is enabled,
            //update the UI with info from GPS
            counter++;
            //mLocalizationOutput.setText(

            View v = getView();
            if (v == null)return;
            TextView nad83LatitudeInput   = (TextView) v.findViewById(R.id.nad83LatitudeInput);
            nad83LatitudeInput.setText(
                    "Set GPS is called for the "+(Integer.toString(counter))+"th time.");
        }

    }


    private void updateNmeaUI(GBNmea nmeaData){
        View v = getView();
        if (v == null)return;
        TextView meanWgs84StartTimeOutput    = (TextView)v.findViewById(R.id.meanWgs84StartTimeOutput);
        TextView meanWgs84EndTimeOutput      = (TextView)v.findViewById(R.id.meanWgs84EndTimeOutput);


        if (nmeaData != null){
            //Which fields have meaning depend upon the type of the sentence
            String type = nmeaData.getNmeaType().toString();
            if (!(type.isEmpty())){
                GBCoordinateWGS84 coordinateWGS84;

                if ((type.contains("GGA")) || (type.contains("GNS"))) {
                    coordinateWGS84 = new GBCoordinateWGS84(nmeaData.getLatitude(),
                                                                 nmeaData.getLongitude());
                    if (coordinateWGS84.isValidCoordinate()){
                        //create the WGS coordinate with NMEA data
                        // TODO: 11/26/2016  time attribute is broken
                        //NMEA time is HHMMSS
                        //Coordinate time is milliseconds since 1970
                        //coordinateWGS84.setTime(nmeaData.getTime());
                        coordinateWGS84.setElevation(nmeaData.getOrthometricElevation());
                        coordinateWGS84.setGeoid(nmeaData.getGeoid());
                        if (isMeanInProgress){
                            mMeanWgs84List.add(coordinateWGS84);
                            //calculate mean, and update screen
                            updateMeanWGS();
                            updateMeanedWGS();
                            if (isFirstPointInMean){
                                clearMeanUI(v);
                                meanWgs84StartTimeOutput.setText(
                                        //String.valueOf(coordinateWGS84.getTime()));
                                        String.valueOf(nmeaData.getTime()));
                                isFirstPointInMean = false;
                            }
                            if (isLastPointInMean){
                                meanWgs84EndTimeOutput.setText(
                                        //todo fix time between nmea and wgs84 (see below too)
                                        //String.valueOf(coordinateWGS84.getTime()));
                                        String.valueOf(nmeaData.getTime()));
                                isMeanInProgress = false;



                                //with an abundance of caution, set these too
                                isLastPointInMean = false;
                                isFirstPointInMean = false;

                                //then store the readings to permanent storage
                                storeRawReadings();
                            }
                        }
                        //update the screen with the latest NMEA update
                        updateUIWithNmea(coordinateWGS84, nmeaData);
                    }
                }

           /* ***
                else if (type.contains("GSV")) {
                    //this is better shown graphically
                    mNmeaSentenceOutput.setText(nmeaData.getNmeaSentence());
                    mSatellitesOutput.setText(Integer.toString(nmeaData.getSatellites()));
                    //TODO rest of satellite data
                } else if (type.contains("GSA")) {
                    mNmeaSentenceOutput.setText(nmeaData.getNmeaSentence());
                    mSatellitesOutput.setText(Integer.toString(nmeaData.getSatellites()));
                    mPDopOutput.setText(doubleToUI(nmeaData.getPdop()));
                    mHDopOutput.setText(doubleToUI(nmeaData.getHdop()));
                    mVDopOutput.setText(doubleToUI(nmeaData.getVdop()));
                }

  *********************/
            } else {
                //there was an exception processing the NMEA Sentence
                Toast toast = Toast.makeText(getActivity(), "Null type found", Toast.LENGTH_SHORT);
                toast.show();
            }

        } else {
            //there was an exception processing the NMEA Sentence
            Toast toast = Toast.makeText(getActivity(), "Null NMEA found", Toast.LENGTH_SHORT);
            toast.show();
        }

    }


    private void updateUIWithNmea(GBCoordinateWGS84 coordinateWGS84, GBNmea nmeaData){
        View v = getView();
        if (v == null)return;

        //Time
        TextView gpsWgs84TimeOutput   = (TextView) v.findViewById(R.id.gpsWgs84TimeOutput);
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

        String nmeaTimeString = String.format(Locale.getDefault(), "%.0f", nmeaData.getTime());
        //String wgsTimeString  = String.format(Locale.getDefault(), "%.0f", coordinateWGS84.getTime());
        gpsWgs84TimeOutput.setText(nmeaTimeString);
                //todo fix time between nmea and coordinate WGS84
                //setText(Double.toString(coordinateWGS84.getTime()));
                //setText(Double.toString(nmeaData.getTime()));

        gpsWgs84LatitudeInput.
                setText(doubleToUI(coordinateWGS84.getLatitude()));
        gpsWgs84LatDegreesInput.
                setText(doubleToUI(coordinateWGS84.getLatitudeDegree()));
        gpsWgs84LatMinutesInput.
                setText(doubleToUI(coordinateWGS84.getLatitudeMinute()));
        gpsWgs84LatSecondsInput.
                setText(doubleToUI(coordinateWGS84.getLatitudeSecond()));

        gpsWgs84LongitudeInput.
                setText(doubleToUI(coordinateWGS84.getLongitude()));
        gpsWgs84LongDegreesInput.
                setText(doubleToUI(coordinateWGS84.getLongitudeDegree()));
        gpsWgs84LongMinutesInput.
                setText(doubleToUI(coordinateWGS84.getLongitudeMinute()));
        gpsWgs84LongSecondsInput.
                setText(doubleToUI(coordinateWGS84.getLongitudeSecond()));

        gpsWgs84ElevationMetersInput.
                setText(doubleToUI(coordinateWGS84.getElevation()));
        gpsWgs84GeoidHeightMetersInput.
                setText(doubleToUI(coordinateWGS84.getGeoid()));
        gpsWgs84ElevationFeetInput.
                setText(doubleToUI(coordinateWGS84.getElevationFeet()));
        gpsWgs84GeoidHeightFeetInput.
                setText(doubleToUI(coordinateWGS84.getGeoidFeet()));
        //fixed or quality

    }

    private void updateMeanWGS(){
        int size= mMeanWgs84List.size();


        double meanLat = 0.;
        double meanLatDeg = 0.;
        double meanLatMin = 0.;
        double meanLatSec = 0.;

        double meanLong = 0.;
        double meanLongDeg = 0.;
        double meanLongMin = 0.;
        double meanLongSec = 0.;

        double meanEle = 0.;
        double meanGeoid = 0.;

        double r = 0;

        double rLat = 0.;
        double rLatDeg = 0.;
        double rLatMin = 0.;
        double rLatSec = 0.;

        double rLng = 0.;
        double rLngDeg = 0.;
        double rLngMin = 0.;
        double rLngSec = 0.;

        double rEle = 0.;
        double rGeoid = 0.;

        double sigmaLat = 0.;
        /* *****
        double sigmaLatDeg = 0.;
        double sigmaLatMin = 0.;
        double sigmaLatSec = 0.;
         ***/

        double sigmaLong = 0.;
        /* *****
        double sigmaLongDeg = 0.;
        double sigmaLongMin = 0.;
        double sigmaLongSec = 0.;
         ****/

        double sigmaEle = 0.;
        double sigmaGeoid = 0.;

        //assure that we have enough readings to calculate mean
        if (size > 1){
            //calculate the mean
            for (int i = 0; i<size; i++){
                meanLat    = meanLat    + mMeanWgs84List.get(i).getLatitude();
                meanLatDeg = meanLatDeg + mMeanWgs84List.get(i).getLatitudeDegree();
                meanLatMin = meanLatMin + mMeanWgs84List.get(i).getLatitudeMinute();
                meanLatSec = meanLatSec + mMeanWgs84List.get(i).getLatitudeSecond();

                meanLong    = meanLong    + mMeanWgs84List.get(i).getLongitude();
                meanLongDeg = meanLongDeg + mMeanWgs84List.get(i).getLongitudeDegree();
                meanLongMin = meanLongMin + mMeanWgs84List.get(i).getLongitudeMinute();
                meanLongSec = meanLongSec + mMeanWgs84List.get(i).getLongitudeSecond();

                meanEle   = meanEle   + mMeanWgs84List.get(i).getElevation();
                meanGeoid = meanGeoid + mMeanWgs84List.get(i).getGeoid();
            }
            double sizeD = (double)size;

            meanLat    = meanLat / sizeD;
            meanLatDeg = meanLatDeg / sizeD;
            meanLatMin = meanLatMin / sizeD;
            meanLatSec = meanLatSec / sizeD;

            meanLong    = meanLong / sizeD;
            meanLongDeg = meanLongDeg / sizeD;
            meanLongMin = meanLongMin / sizeD;
            meanLongSec = meanLongSec / sizeD;

            meanEle   = meanEle / sizeD;
            meanGeoid = meanGeoid / sizeD;

            //calculate the variance of the squared residuals
            for (int i = 0; i<size; i++){

                rLat    = rLat    +
                        ((meanLat - mMeanWgs84List.get(i).getLatitude())*
                         (meanLat - mMeanWgs84List.get(i).getLatitude()));
                rLatDeg = rLatDeg +
                        ((meanLatDeg - mMeanWgs84List.get(i).getLatitudeDegree()) *
                         (meanLatDeg - mMeanWgs84List.get(i).getLatitudeDegree()));
                rLatMin = rLatMin +
                        ((meanLatMin - mMeanWgs84List.get(i).getLatitudeMinute()) *
                         (meanLatMin - mMeanWgs84List.get(i).getLatitudeMinute()));
                rLatSec = rLatSec +
                        ((meanLatSec - mMeanWgs84List.get(i).getLatitudeSecond()) *
                         (meanLatSec - mMeanWgs84List.get(i).getLatitudeSecond()));

                rLng    = rLng    + (
                        (meanLong    - mMeanWgs84List.get(i).getLongitude())*
                        (meanLong    - mMeanWgs84List.get(i).getLongitude()));
                rLngDeg = rLngDeg +
                        ((meanLongDeg - mMeanWgs84List.get(i).getLongitudeDegree())*
                         (meanLongDeg - mMeanWgs84List.get(i).getLongitudeDegree()));
                rLngMin = rLngMin +
                        ((meanLongMin - mMeanWgs84List.get(i).getLongitudeMinute())*
                         (meanLongMin - mMeanWgs84List.get(i).getLongitudeMinute()));
                rLngSec = rLngSec +
                        ((meanLongSec - mMeanWgs84List.get(i).getLongitudeSecond())*
                         (meanLongSec - mMeanWgs84List.get(i).getLongitudeSecond()));

                rEle = rEle +
                        ((meanEle - mMeanWgs84List.get(i).getElevation())*
                         (meanEle - mMeanWgs84List.get(i).getElevation()));
                rGeoid = rGeoid +
                        ((meanGeoid - mMeanWgs84List.get(i).getGeoid())*
                         (meanGeoid - mMeanWgs84List.get(i).getGeoid()));
            }

            //Treat readings as sample of a larger population
            //so take sample mean (i.e. divide by size - 1
            sizeD = sizeD - 1.0;
            sigmaLat    = Math.sqrt(rLat     / sizeD);
            /* **
            sigmaLatDeg = Math.sqrt(rLatDeg  / sizeD);
            sigmaLatMin = Math.sqrt(rLatMin  / sizeD);
            sigmaLatSec = Math.sqrt(rLatSec  / sizeD);
             ***/

            sigmaLong    = Math.sqrt(rLng    / sizeD);
            /* **
            sigmaLongDeg = Math.sqrt(rLngDeg / sizeD);
            sigmaLongMin = Math.sqrt(rLngMin / sizeD);
            sigmaLongSec = Math.sqrt(rLngSec / sizeD);
             ***/

            sigmaEle     = Math.sqrt(rEle     / sizeD);
            //sigmaGeoid   = Math.sqrt(rGeoid   / sizeD);

            View v = getView();
            if (v == null)return;


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

            //Mean Parameters
            TextView meanWgs84PointsInMeanOutput =
                                        (TextView)v.findViewById(R.id.meanWgs84PointsInMeanOutput);


            //Mean Standard Deviations
            TextView meanWgs84LatSigmaOutput = (TextView)v.findViewById(R.id.meanWgs84LatSigmaOutput);
            TextView meanWgs84LongSigmaOutput= (TextView)v.findViewById(R.id.meanWgs84LongSigmaOutput);
            TextView meanWgs84ElevSigmaOutput= (TextView)v.findViewById(R.id.meanWgs84ElevSigmaOutput);


            //show the mean and standard deviation on the screen
            meanWgs84LatitudeInput  .setText(doubleToUI(meanLat));
            meanWgs84LatDegreesInput.setText(doubleToUI(meanLatDeg));
            meanWgs84LatMinutesInput.setText(doubleToUI(meanLatMin));
            meanWgs84LatSecondsInput.setText(doubleToUI(meanLatSec));

            meanWgs84LongitudeInput  .setText(doubleToUI(meanLong));
            meanWgs84LongDegreesInput.setText(doubleToUI(meanLongDeg));
            meanWgs84LongMinutesInput.setText(doubleToUI(meanLongMin));
            meanWgs84LongSecondsInput.setText(doubleToUI(meanLongSec));

            meanWgs84ElevationMetersInput.setText(doubleToUI(meanEle));
            double feetReading = GBUtilities.convertMetersToFeet(truncatePrecision(meanEle));
            meanWgs84ElevationFeetInput.setText(doubleToUI(feetReading));
            meanWgs84GeoidHeightMetersInput.setText(doubleToUI(meanGeoid));
            feetReading = GBUtilities.convertMetersToFeet(truncatePrecision(meanGeoid));
            meanWgs84GeoidHeightFeetInput.setText(doubleToUI(feetReading));

            meanWgs84PointsInMeanOutput.setText(String.valueOf(size));

            meanWgs84LatSigmaOutput.setText(doubleToUI(sigmaLat));
            meanWgs84LongSigmaOutput.setText(doubleToUI(sigmaLong));
            meanWgs84ElevSigmaOutput.setText(doubleToUI(sigmaEle));

            if (isLastPointInMean){
                //create the coordinate object with the mean
                mMeanCoordinateWGS84 = new GBCoordinateWGS84(meanLat, meanLong);
                mMeanCoordinateWGS84.setElevation(meanEle);
                mMeanCoordinateWGS84.setGeoid(meanGeoid);
            }

        }
    }

    private void updateMeanedWGS(){
        int size= mMeanWgs84List.size();
        GBCoordinateMean meanCoordinate     = new GBCoordinateMean();
        GBCoordinateMean residuals          = new GBCoordinateMean();
        //GBCoordinateMean sigma              = new GBCoordinateMean();
        double tempMeanD;


        //assure that we have enough readings to calculate mean
        if (size > 1){
            //calculate the mean
            //Step one - Sum of each member in the list
            for (int i = 0; i<size; i++){
                tempMeanD = meanCoordinate.getLatitude() + mMeanWgs84List.get(i).getLatitude();
                meanCoordinate.setLatitude(tempMeanD);
                /*
                meanTempI = meanCoordinate.getLatitudeDegree() + mMeanWgs84List.get(i).getLatitudeDegree();
                meanCoordinate.setLatitudeDegree(meanTempI);
                meanTempI = meanCoordinate.getLatitudeMinute() + mMeanWgs84List.get(i).getLatitudeMinute();
                meanCoordinate.setLatitudeMinute(meanTempI);
                tempMeanD = meanCoordinate.getLatitudeSecond() + mMeanWgs84List.get(i).getLatitudeSecond();
                meanCoordinate.setLatitudeSecond(tempMeanD);
*/
                tempMeanD = meanCoordinate.getLongitude() + mMeanWgs84List.get(i).getLongitude();
                meanCoordinate.setLongitude(tempMeanD);
    /*
                meanTempI = meanCoordinate.getLongitudeDegree() + mMeanWgs84List.get(i).getLongitudeDegree();
                meanCoordinate.setLongitudeDegree(meanTempI);
                meanTempI = meanCoordinate.getLongitudeMinute() + mMeanWgs84List.get(i).getLongitudeMinute();
                meanCoordinate.setLongitudeMinute(meanTempI);
                tempMeanD = meanCoordinate.getLongitudeSecond() + mMeanWgs84List.get(i).getLongitudeSecond();
                meanCoordinate.setLongitudeSecond(tempMeanD);
*/
                tempMeanD = meanCoordinate.getElevation() + mMeanWgs84List.get(i).getElevation();
                meanCoordinate.setElevation(tempMeanD);
                tempMeanD = meanCoordinate.getGeoid() + mMeanWgs84List.get(i).getGeoid();
                meanCoordinate.setGeoid(tempMeanD);

            }
            //Step two: devide by the number in the list
            double sizeD = (double)size;

            tempMeanD = meanCoordinate.getLatitude() / sizeD;
            meanCoordinate.setLatitude(tempMeanD);
/*
            meanTempI = meanCoordinate.getLatitudeDegree() / size;
            meanCoordinate.setLatitudeDegree(meanTempI);
            meanTempI = meanCoordinate.getLatitudeMinute() / size;
            meanCoordinate.setLatitudeMinute(meanTempI);
            tempMeanD = meanCoordinate.getLatitudeSecond() / sizeD;
            meanCoordinate.setLatitudeSecond(tempMeanD);
*/

            meanCoordinate.setLongitude((meanCoordinate.getLongitude() / sizeD));
/*
            meanTempI = meanCoordinate.getLongitudeDegree() / size;

            meanCoordinate.setLongitudeDegree(meanTempI);
            meanTempI = meanCoordinate.getLongitudeMinute() / size;
            meanCoordinate.setLongitudeMinute(meanTempI);
            tempMeanD = meanCoordinate.getLongitudeSecond() / sizeD;
            meanCoordinate.setLongitudeSecond(tempMeanD);
*/
            tempMeanD = meanCoordinate.getElevation() / sizeD;
            meanCoordinate.setElevation(tempMeanD);
            tempMeanD = meanCoordinate.getGeoid() / sizeD;
            meanCoordinate.setGeoid(tempMeanD);

            //calculate the variance of the squared residuals
            //Step three: sum( (mean - reading) squared  )
            for (int i = 0; i<size; i++){

                tempMeanD = meanCoordinate.getLatitude()      - mMeanWgs84List.get(i).getLatitude();
                tempMeanD = tempMeanD * tempMeanD;
                residuals.setLatitude      (residuals.getLatitude()+ tempMeanD);
/*
                tempMeanD = meanCoordinate.getLatitudeDegree() - mMeanWgs84List.get(i).getLatitudeDegree();
                tempMeanD = tempMeanD * tempMeanD;
                residuals.setLatitudeDegree(residuals.getLatitudeDegree() + tempMeanD);


                tempMeanD = meanCoordinate.getLatitudeMinute() - mMeanWgs84List.get(i).getLatitudeMinute();
                tempMeanD = tempMeanD * tempMeanD;
                residuals.setLatitudeMinute(residuals.getLatitudeMinute()+ tempMeanD);


                tempMeanD = meanCoordinate.getLatitudeSecond() - mMeanWgs84List.get(i).getLatitudeSecond();
                tempMeanD = tempMeanD * tempMeanD;
                residuals.setLatitudeSecond(residuals.getLatitudeSecond()+ tempMeanD);
*/


                tempMeanD = meanCoordinate.getLongitude() - mMeanWgs84List.get(i).getLongitude();
                tempMeanD = tempMeanD * tempMeanD;
                residuals.setLongitude(residuals.getLongitude() + tempMeanD);
/*
                tempMeanD = meanCoordinate.getLongitudeDegree() - mMeanWgs84List.get(i).getLongitudeDegree();
                tempMeanD = tempMeanD * tempMeanD;
                residuals.setLongitudeDegree(residuals.getLongitudeDegree()+ tempMeanD);

                tempMeanD = meanCoordinate.getLongitudeMinute() - mMeanWgs84List.get(i).getLongitudeMinute();
                tempMeanD = tempMeanD * tempMeanD;
                residuals.setLongitudeMinute(residuals.getLongitudeMinute()+ tempMeanD);

                tempMeanD = meanCoordinate.getLongitudeSecond() - mMeanWgs84List.get(i).getLongitudeSecond();
                tempMeanD = tempMeanD * tempMeanD;
                residuals.setLongitudeSecond(residuals.getLongitudeSecond()+ tempMeanD);
*/
                tempMeanD = meanCoordinate.getElevation() - mMeanWgs84List.get(i).getElevation();
                tempMeanD = tempMeanD * tempMeanD;
                residuals.setElevation(residuals.getElevation() + tempMeanD);


                tempMeanD = meanCoordinate.getGeoid() - mMeanWgs84List.get(i).getGeoid();
                tempMeanD = tempMeanD * tempMeanD;
                residuals.setGeoid(residuals.getGeoid()+ tempMeanD);
            }

            //Step 3: residual = sum of [(mean - reading) squared]
            //step 3.5 mean of residuals = sum / # of readings
            //Step 4: sqrt of (mean of the residuals)

            //Treat readings as sample of a larger population
            //so take sample mean (i.e. divide by size - 1
            sizeD = sizeD - 1.0;
            meanCoordinate.setLatitudeStdDev(Math.sqrt(residuals.getLatitude() / sizeD));

            meanCoordinate.setLongitudeStdDev(Math.sqrt(residuals.getLongitude()/ sizeD));

            meanCoordinate.setElevationStdDev(Math.sqrt(residuals.getElevation()/ sizeD));
            //sigma.setGeoid          (Math.sqrt(residuals.getGeoid()           / sizeD));

            updateUIWithMean(meanCoordinate);

            if (isLastPointInMean){
                //create the coordinate object with the mean
                mMeanCoordinateWGS84 = new GBCoordinateWGS84(meanCoordinate.getLatitude(),
                                                                  meanCoordinate.getLongitude());
                mMeanCoordinateWGS84.setElevation(meanCoordinate.getElevation());
                mMeanCoordinateWGS84.setGeoid(meanCoordinate.getGeoid());
            }

        }
    }


    private void updateUIWithMean(GBCoordinateMean meanCoordinate){
        View v = getView();
        if (v == null)return;


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

        //Mean Parameters
        TextView meanWgs84PointsInMeanOutput = (TextView)v.findViewById(R.id.meanWgs84PointsInMeanOutput);


        meanWgs84LongitudeInput  .setText(doubleToUI(meanCoordinate.getLongitude()));
        meanWgs84LongDegreesInput.setText(doubleToUI(meanCoordinate.getLongitudeDegree()));
        meanWgs84LongMinutesInput.setText(doubleToUI(meanCoordinate.getLongitudeMinute()));
        meanWgs84LongSecondsInput.setText(doubleToUI(meanCoordinate.getLongitudeSecond()));

        meanWgs84ElevationMetersInput.setText(doubleToUI(meanCoordinate.getElevation()));
        double feetReading = GBUtilities.convertMetersToFeet
                                                (truncatePrecision(meanCoordinate.getElevation()));
        meanWgs84ElevationFeetInput.setText(doubleToUI(feetReading));
        meanWgs84GeoidHeightMetersInput.setText(doubleToUI(meanCoordinate.getGeoid()));
        feetReading = GBUtilities.convertMetersToFeet
                                                    (truncatePrecision(meanCoordinate.getGeoid()));
        meanWgs84GeoidHeightFeetInput.setText(doubleToUI(feetReading));

        int size = meanCoordinate.getMeanedReadings();
        meanWgs84PointsInMeanOutput.setText(String.valueOf(size));


    }



    private boolean convertKarney(){
        View v = getView();
        if (v == null  )return false;

        TextView utmZoneOutput           =  (TextView) v.findViewById(R.id.utmZoneOutput);
        TextView utmLatbandOutput        =  (TextView) v.findViewById(R.id.utmLatbandOutput);
        TextView utmHemisphereOutput     =  (TextView) v.findViewById(R.id.utmHemisphereOutput);
        TextView utmEastingMetersOutput  =  (TextView) v.findViewById(R.id.utmEastingMetersOutput);
        TextView utmNorthingMetersOutput =  (TextView) v.findViewById(R.id.utmNorthingMetersOutput);
        TextView utmEastingFeetOutput    =  (TextView) v.findViewById(R.id.utmEastingFeetOutput);
        TextView utmNorthingFeetOutput   =  (TextView) v.findViewById(R.id.utmNorthingFeetOutput);

        TextView utmConvergenceOutput    =  (TextView) v.findViewById(R.id.utmConvergence);
        TextView utmScaleFactorOutput    =  (TextView) v.findViewById(R.id.utmScaleFactor);



        try{
            //The UTM constructor performs the conversion from WGS84
            GBCoordinateUTM utmCoordinate = new GBCoordinateUTM(mMeanCoordinateWGS84);

            //Also output the result in separate fields
            utmZoneOutput        .setText(String.valueOf(utmCoordinate.getZone()));
            utmHemisphereOutput  .setText(String.valueOf(utmCoordinate.getHemisphere()));
            utmLatbandOutput     .setText(String.valueOf(utmCoordinate.getLatBand()));
            utmEastingMetersOutput.setText(String.valueOf(utmCoordinate.getEasting()));
            utmNorthingMetersOutput.setText(String.valueOf(utmCoordinate.getNorthing()));
            utmEastingFeetOutput .setText(String.valueOf(utmCoordinate.getEastingFeet()));
            utmNorthingFeetOutput.setText(String.valueOf(utmCoordinate.getNorthingFeet()));
            utmConvergenceOutput .setText(String.valueOf(utmCoordinate.getConvergence()));
            utmScaleFactorOutput .setText(String.valueOf(utmCoordinate.getScale()));
            return true;

        } catch (IllegalArgumentException exc) {
            //input parameters were not within range
            utmEastingMetersOutput.setText(R.string.input_wrong_range_error);
            utmNorthingMetersOutput.setText(R.string.input_wrong_range_error);
            return false;
        }
    }


    private String doubleToUI(double reading){
        return String.valueOf(truncatePrecision(reading));
    }

    //truncate digits of precision
    private double truncatePrecision(double reading) {

        BigDecimal bd = new BigDecimal(reading).
                setScale(GBUtilities.sMicrometerDigitsOfPrecision, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }


    private void storeRawReadings() {
        //Store off the list of raw gps readings to permanent storage
        //mMeanWgs84List contains the raw gps readings in the form of coordinateWgs84 objects
    }

    private boolean convertInputs() {
        View v = getView();
        if (v == null)return false;

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


        mWSG84Coordinate = new GBCoordinateWGS84(gpsWgs84LatitudeInput.getText(),
                                                      gpsWgs84LongitudeInput.getText());
        if (!mWSG84Coordinate.isValidCoordinate()) {
            mWSG84Coordinate = new GBCoordinateWGS84(
                    gpsWgs84LatDegreesInput.getText(),
                    gpsWgs84LatMinutesInput.getText(),
                    gpsWgs84LatSecondsInput.getText(),
                    gpsWgs84LongDegreesInput.getText(),
                    gpsWgs84LongMinutesInput.getText(),
                    gpsWgs84LongSecondsInput.getText());
            if (!mWSG84Coordinate.isValidCoordinate()){
                Toast.makeText(getActivity(),
                        R.string.coordinate_try_again,
                        Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        //display the coordinate values in the UI
        gpsWgs84LatitudeInput.setText(doubleToUI(mWSG84Coordinate.getLatitude()));
        gpsWgs84LatDegreesInput   .setText(doubleToUI(mWSG84Coordinate.getLatitudeDegree()));
        gpsWgs84LatMinutesInput   .setText(doubleToUI(mWSG84Coordinate.getLatitudeMinute()));
        gpsWgs84LatSecondsInput   .setText(doubleToUI(mWSG84Coordinate.getLatitudeSecond()));

        gpsWgs84LongitudeInput.setText(doubleToUI(mWSG84Coordinate.getLongitude()));
        gpsWgs84LongDegreesInput   .setText(doubleToUI(mWSG84Coordinate.getLongitudeDegree()));
        gpsWgs84LongMinutesInput   .setText(doubleToUI(mWSG84Coordinate.getLongitudeMinute()));
        gpsWgs84LongSecondsInput   .setText(doubleToUI(mWSG84Coordinate.getLongitudeSecond()));

        setLatColor();
        setLongColor();

        return true;

    }

    //Replaced by convertKarney
    private void performConversion() {
        View v = getView();
        if (v == null)return;

        /*
         * Compare three conversion routines:
         * 1) WGS84 to NAD83
         * 2) NAD86 to State Plane Coordinates
         * 3) GB developed from scratch, based on Karney (2010)
         *
         *
         *
         */

        //"Legal ranges: latitude [-90,90], longitude [-180,180).");
        boolean inputsValid = convertInputs() ;

        //only attempt the conversions if the inputs are valid

        if (inputsValid){

            //Create the UTM coordinate based on the WSG coordinate from the user
            // The GB conversion
            // algorithm based on Kearny (2010)
            // supposed nanometer accuracy

            try{
                GBCoordinateUTM utmCoordinate = new GBCoordinateUTM(mWSG84Coordinate);

                TextView utmZoneOutput           =  (TextView) v.findViewById(R.id.utmZoneOutput);
                TextView utmLatbandOutput        =  (TextView) v.findViewById(R.id.utmLatbandOutput);
                TextView utmHemisphereOutput     =  (TextView) v.findViewById(R.id.utmHemisphereOutput);
                TextView utmEastingMetersOutput  =  (TextView) v.findViewById(R.id.utmEastingMetersOutput);
                TextView utmNorthingMetersOutput =  (TextView) v.findViewById(R.id.utmNorthingMetersOutput);
                TextView utmEastingFeetOutput    =  (TextView) v.findViewById(R.id.utmEastingFeetOutput);
                TextView utmNorthingFeetOutput   =  (TextView) v.findViewById(R.id.utmNorthingFeetOutput);


                //Also output the result in separate fields
                utmZoneOutput          .setText(doubleToUI(utmCoordinate.getZone()));
                utmHemisphereOutput    .setText(doubleToUI(utmCoordinate.getHemisphere()));
                utmLatbandOutput       .setText(doubleToUI(utmCoordinate.getLatBand()));
                utmEastingMetersOutput .setText(doubleToUI(utmCoordinate.getEasting()));
                utmNorthingMetersOutput.setText(doubleToUI(utmCoordinate.getNorthing()));

                utmEastingFeetOutput   .setText(doubleToUI(utmCoordinate.getEastingFeet()));
                utmNorthingFeetOutput  .setText(doubleToUI(utmCoordinate.getNorthingFeet()));

            } catch (IllegalArgumentException exc) {
                TextView gpsWgs84LatitudeInput   = (TextView) v.findViewById(R.id.gpsWgs84LatitudeInput);
                TextView gpsWgs84LongitudeInput   = (TextView) v.findViewById(R.id.gpsWgs84LongitudeInput);

                //input parameters were not within range
                gpsWgs84LatitudeInput.setText(R.string.input_wrong_range_error);
                gpsWgs84LongitudeInput.setText(R.string.input_wrong_range_error);
                //inputsValid = false;
            }
        }

    }




    private void clearForm() {
        View v = getView();
        if (v == null)return;

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


        gpsWgs84LatitudeInput.setText("");
        gpsWgs84LatDegreesInput   .setText("");
        gpsWgs84LatMinutesInput   .setText("");
        gpsWgs84LatSecondsInput   .setText("");

        gpsWgs84LongitudeInput.setText("");
        gpsWgs84LongDegreesInput   .setText("");
        gpsWgs84LongMinutesInput   .setText("");
        gpsWgs84LongSecondsInput   .setText("");

        clearMeanUI(v);

        TextView utmZoneOutput           =  (TextView) v.findViewById(R.id.utmZoneOutput);
        TextView utmLatbandOutput        =  (TextView) v.findViewById(R.id.utmLatbandOutput);
        TextView utmHemisphereOutput     =  (TextView) v.findViewById(R.id.utmHemisphereOutput);
        TextView utmEastingMetersOutput  =  (TextView) v.findViewById(R.id.utmEastingMetersOutput);
        TextView utmNorthingMetersOutput =  (TextView) v.findViewById(R.id.utmNorthingMetersOutput);
        TextView utmEastingFeetOutput    =  (TextView) v.findViewById(R.id.utmEastingFeetOutput);
        TextView utmNorthingFeetOutput   =  (TextView) v.findViewById(R.id.utmNorthingFeetOutput);


        utmZoneOutput.          setText(R.string.utm_zone_label);
        utmLatbandOutput.       setText(R.string.utm_latband_label);
        utmHemisphereOutput.    setText(R.string.utm_hemisphere_label);
        utmEastingMetersOutput. setText(R.string.utm_easting_label);
        utmNorthingMetersOutput.setText(R.string.utm_northing_label);
        utmEastingFeetOutput.   setText(R.string.utm_easting_label);
        utmNorthingFeetOutput.  setText(R.string.utm_northing_label);
        setLatColorPos();
        setLongColorPos();
    }

    private void clearMeanUI(View v){

        //Mean Parameters
        TextView meanWgs84StartTimeOutput    = (TextView)v.findViewById(R.id.meanWgs84StartTimeOutput);
        TextView meanWgs84EndTimeOutput      = (TextView)v.findViewById(R.id.meanWgs84EndTimeOutput);
        TextView meanWgs84PointsInMeanOutput = (TextView)v.findViewById(R.id.meanWgs84PointsInMeanOutput);


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



        meanWgs84StartTimeOutput   .setText("");
        meanWgs84EndTimeOutput     .setText("");
        meanWgs84PointsInMeanOutput.setText("");

        meanWgs84LatitudeInput     .setText("");
        meanWgs84LatDegreesInput   .setText("");
        meanWgs84LatMinutesInput   .setText("");
        meanWgs84LatSecondsInput   .setText("");

        meanWgs84LongitudeInput     .setText("");
        meanWgs84LongDegreesInput   .setText("");
        meanWgs84LongMinutesInput   .setText("");
        meanWgs84LongSecondsInput   .setText("");

        meanWgs84ElevationMetersInput.setText("");
        meanWgs84ElevationFeetInput .setText("");
        meanWgs84GeoidHeightMetersInput   .setText("");
        meanWgs84GeoidHeightFeetInput.setText("");

        meanWgs84LatSigmaOutput     .setText("");
        meanWgs84LongSigmaOutput    .setText("");

        meanWgs84ElevSigmaOutput    .setText("");

    }


    private void setLatColor(){
        if (mWSG84Coordinate.getLatitude() >= 0.0) {
            setLatColorPos();

        } else {
            setLatColorNeg();
        }
    }

    private void setLongColor(){
        if (mWSG84Coordinate.getLongitude() >= 0.0) {
            setLongColorPos();

        } else {
            setLongColorNeg();

        }
    }

    private void setLatColorNeg(){
        View v = getView();
        if (v == null)return;

        //GPS Latitude
        TextView gpsWgs84LatitudeInput   = (TextView) v.findViewById(R.id.gpsWgs84LatitudeInput);
        TextView gpsWgs84LatDegreesInput = (TextView) v.findViewById(R.id.gpsWgs84LatDegreesInput);
        TextView gpsWgs84LatMinutesInput = (TextView) v.findViewById(R.id.gpsWgs84LatMinutesInput);
        TextView gpsWgs84LatSecondsInput = (TextView) v.findViewById(R.id.gpsWgs84LatSecondsInput);


        gpsWgs84LatitudeInput.setTextColor(
                ContextCompat.getColor(getActivity(),R.color.colorNegNumber));
        gpsWgs84LatDegreesInput.setTextColor(
                ContextCompat.getColor(getActivity(),R.color.colorNegNumber));
        gpsWgs84LatMinutesInput.setTextColor(
                ContextCompat.getColor(getActivity(),R.color.colorNegNumber));
        gpsWgs84LatSecondsInput.setTextColor(
                ContextCompat.getColor(getActivity(),R.color.colorNegNumber));

    }

    private void setLatColorPos(){
        View v = getView();
        if (v == null)return;

        //GPS Latitude
        TextView gpsWgs84LatitudeInput   = (TextView) v.findViewById(R.id.gpsWgs84LatitudeInput);
        TextView gpsWgs84LatDegreesInput = (TextView) v.findViewById(R.id.gpsWgs84LatDegreesInput);
        TextView gpsWgs84LatMinutesInput = (TextView) v.findViewById(R.id.gpsWgs84LatMinutesInput);
        TextView gpsWgs84LatSecondsInput = (TextView) v.findViewById(R.id.gpsWgs84LatSecondsInput);



        gpsWgs84LatitudeInput.setTextColor(
                ContextCompat.getColor(getActivity(),R.color.colorPosNumber));
        gpsWgs84LatDegreesInput.setTextColor(
                ContextCompat.getColor(getActivity(),R.color.colorPosNumber));
        gpsWgs84LatMinutesInput.setTextColor(
                ContextCompat.getColor(getActivity(),R.color.colorPosNumber));
        gpsWgs84LatSecondsInput.setTextColor(
                ContextCompat.getColor(getActivity(),R.color.colorPosNumber));

    }

    private void setLongColorNeg(){
        View v = getView();
        if (v == null)return;


        //GPS Longitude
        TextView gpsWgs84LongitudeInput   = (TextView) v.findViewById(R.id.gpsWgs84LongitudeInput);
        TextView gpsWgs84LongDegreesInput = (TextView) v.findViewById(R.id.gpsWgs84LongDegreesInput);
        TextView gpsWgs84LongMinutesInput = (TextView) v.findViewById(R.id.gpsWgs84LongMinutesInput);
        TextView gpsWgs84LongSecondsInput = (TextView) v.findViewById(R.id.gpsWgs84LongSecondsInput);


        gpsWgs84LongitudeInput.setTextColor(
                ContextCompat.getColor(getActivity(),R.color.colorNegNumber));
        gpsWgs84LongDegreesInput.setTextColor(
                ContextCompat.getColor(getActivity(),R.color.colorNegNumber));
        gpsWgs84LongMinutesInput.setTextColor(
                ContextCompat.getColor(getActivity(),R.color.colorNegNumber));
        gpsWgs84LongSecondsInput.setTextColor(
                ContextCompat.getColor(getActivity(),R.color.colorNegNumber));

    }

    private void setLongColorPos(){
        View v = getView();
        if (v == null)return;

        //GPS Longitude
        TextView gpsWgs84LongitudeInput   = (TextView) v.findViewById(R.id.gpsWgs84LongitudeInput);
        TextView gpsWgs84LongDegreesInput = (TextView) v.findViewById(R.id.gpsWgs84LongDegreesInput);
        TextView gpsWgs84LongMinutesInput = (TextView) v.findViewById(R.id.gpsWgs84LongMinutesInput);
        TextView gpsWgs84LongSecondsInput = (TextView) v.findViewById(R.id.gpsWgs84LongSecondsInput);


        gpsWgs84LongitudeInput.setTextColor(
                ContextCompat.getColor(getActivity(),R.color.colorPosNumber));
        gpsWgs84LongDegreesInput.setTextColor(
                ContextCompat.getColor(getActivity(),R.color.colorPosNumber));
        gpsWgs84LongMinutesInput.setTextColor(
                ContextCompat.getColor(getActivity(),R.color.colorPosNumber));
        gpsWgs84LongSecondsInput.setTextColor(
                ContextCompat.getColor(getActivity(),R.color.colorPosNumber));

    }

}


