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
import android.widget.EditText;
import android.widget.TextView;

import java.util.Locale;

/**
 * The Collect Fragment is the UI
 * when the workflow from WGS84 GPS to NAD83 to UTM/State Plane Coordinates
 * Created by Elisabeth Huhn on 6/15/2016.
 */
public class GBCoordWorkflowFragment extends Fragment implements GpsStatus.Listener, LocationListener, GpsStatus.NmeaListener {

    private static GBNmeaParser mNmeaParser = GBNmeaParser.getInstance();
    private LocationManager     mLocationManager;
    private GBNmea              mNmeaData; //latest nmea sentence received
    private GpsStatus           mGpsStatus = null;
    private Location            mCurLocation;

    //Data that must survive a reconfigure
            int     counter = 0;

    //Contains all raw data and current results of meaning such data
    private GBMeanToken mMeanToken;


     private boolean isGpsOn            = true;




    /*  Not used, but maybe they should be
    private double mConvergenceAngle;
    private double mScaleFactor;
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


                GBUtilities.getInstance().showStatus(getActivity(), R.string.start_gps_button_label);

                startGps();
                isGpsOn = true;

            }//End on Click
        });

        //Stop GPS Button
        Button stopGpsButton = (Button) v.findViewById(R.id.stopGPSButton);
        stopGpsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){

                GBUtilities.getInstance().showStatus(getActivity(), R.string.stop_gps_button_label);

                stopGps();
                isGpsOn = false;

            }//End on Click
        });

        //Start Mean Button
        Button startMeanButton = (Button) v.findViewById(R.id.startMeanButton);
        startMeanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){

                GBUtilities.getInstance().showStatus(getActivity(), R.string.start_mean_button_label);

                //set flags to start taking mean
                initializeMeanToken();
                mMeanToken.setFirstPointInMean(true);
                mMeanToken.setMeanInProgress(true);

            }//End on Click
        });

        //Stop Mean Button
        Button stopMeanButton = (Button) v.findViewById(R.id.stopMeanButton);
        stopMeanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){

                GBUtilities.getInstance().showStatus(getActivity(), R.string.stop_mean_button_label);
                //set flags that mean is done
                mMeanToken.setMeanInProgress(false);
                mMeanToken.setEndMeanTime(mNmeaData.getTime());
                mMeanToken.setLastPointInMean(true);

            }//End on Click
        });





        //Conversion Button
        Button convertButton = (Button) v.findViewById(R.id.convertButton);
        convertButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){

                //performConversion();

                if (mMeanToken == null)return;
                GBUtilities.getInstance().showStatus(getActivity(), R.string.convert_success);

                //The UTM constructor performs the conversion from WGS84
                //make sure the mean is up to date
                GBCoordinateWGS84 meanedWGS84 = new GBCoordinateWGS84((GBActivity)getActivity(),
                                                            mMeanToken.getMeanCoordinate(true));
                GBCoordinateUTM utmCoordinate = new GBCoordinateUTM(meanedWGS84);
                updateUtmUI(utmCoordinate);

                updateSPCSUI(meanedWGS84);

                // TODO: 6/18/2017 add in here the other conversions as they come on line

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
            if (mNmeaParser == null)mNmeaParser = GBNmeaParser.getInstance();
            GBNmea nmeaData = mNmeaParser.parse(nmea);
            if (nmeaData == null) return;

            nmeaData = refineNmeaData(nmeaData);
            if (nmeaData == null)return;

            //so we know it's a good point
            mNmeaData = nmeaData;


            GBCoordinateWGS84 coordinateWGS84;
            if (isMeanInProgress()){
                if (mMeanToken == null)initializeMeanToken();

                //Fold the new nmea sentence into the ongoing mean
                GBCoordinateMean meanCoordinate = mMeanToken.updateMean((GBActivity)getActivity(),
                                                                         mNmeaData);
                if (meanCoordinate != null) {
                    //Is this the first point we have processed?
                    if (isFirstMeanPoint()) {
                        mMeanToken.setStartMeanTime(mNmeaData.getTime());
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
                coordinateWGS84 = new GBCoordinateWGS84((GBActivity)getActivity(), nmeaData);

                //update the UI from the Nmea Sentence
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

    private boolean isMeanInProgress(){
        return ((mMeanToken != null) && mMeanToken.isMeanInProgress());
    }
    private boolean isMeanStopped(){return !isMeanInProgress();}
    private boolean isFirstMeanPoint(){
        return mMeanToken.isFirstPointInMean();
    }

    private void initializeMeanToken(){
        if (mMeanToken == null)mMeanToken = new GBMeanToken();
        long openProjectID = GBUtilities.getInstance().getOpenProjectID((GBActivity)getActivity());
        mMeanToken.setProjectID(openProjectID);
        mMeanToken.setMeanInProgress(false);
        mMeanToken.setFirstPointInMean(false);
        mMeanToken.setLastPointInMean(false);
        mMeanToken.resetCoordinates();

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



        if (!((type.contains("GGA")) || (type.contains("GNS"))) )return null;


        if ((nmeaData.getLatitude() == 0.0) && (nmeaData.getLongitude() == 0.0)) {
            return null;
        }
        return nmeaData;
    }

    private void updateNmeaUI(GBCoordinateWGS84 coordinateWGS84, GBNmea nmeaData){
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

        String nmeaTimeString = String.format(Locale.getDefault(), "%.3f", nmeaData.getTime());
        //String wgsTimeString  = String.format(Locale.getDefault(), "%.0f", coordinateWGS84.getTime());
        gpsWgs84TimeOutput.setText(nmeaTimeString);
                //todo fix time between nmea and coordinate WGS84
                //setText(Double.toString(coordinateWGS84.getTime()));
                //setText(Double.toString(nmeaData.getTime()));

        GBActivity myActivity = (GBActivity)getActivity();
        int locPrecision = GBGeneralSettings.getLocPrecision(myActivity);

        gpsWgs84LatitudeInput  .setText(truncatePrecisionString(locPrecision, coordinateWGS84.getLatitude()));

        gpsWgs84LongitudeInput  .setText(truncatePrecisionString(locPrecision, coordinateWGS84.getLongitude()));

        gpsWgs84ElevationMetersInput  .setText(truncatePrecisionString(locPrecision, coordinateWGS84.getElevation()));
        gpsWgs84GeoidHeightMetersInput.setText(truncatePrecisionString(locPrecision, coordinateWGS84.getGeoid()));
        gpsWgs84ElevationFeetInput.setText(truncatePrecisionString(locPrecision, coordinateWGS84.getElevationFeet()));
        gpsWgs84GeoidHeightFeetInput.setText(truncatePrecisionString(locPrecision, coordinateWGS84.getGeoidFeet()));


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


    }

    private void updateMeanUI(GBCoordinateMean meanCoordinate, GBMeanToken meanToken){


        View v = getView();
        if (v == null)return;


        //Mean Parameters
        TextView meanWgs84PointsInMeanOutput = (TextView)v.findViewById(R.id.meanWgs84PointsInMeanOutput);
        TextView meanWgs84StartTimeOutput = (TextView) v.findViewById(R.id.meanWgs84StartTimeOutput);
        TextView meanWgs84EndTimeOutput   = (TextView) v.findViewById(R.id.meanWgs84EndTimeOutput);

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
        TextView meanWgs84LongSigmaOutput= (TextView)v.findViewById(R.id.meanWgs84LngSigmaOutput);
        TextView meanWgs84ElevSigmaOutput= (TextView)v.findViewById(R.id.meanWgs84ElevSigmaOutput);





        //show the mean and standard deviation on the screen
        meanWgs84PointsInMeanOutput.setText(String.valueOf(meanCoordinate.getMeanedReadings()));
        meanWgs84StartTimeOutput.setText(String.valueOf(meanToken.getStartMeanTime()));
        meanWgs84EndTimeOutput.setText(String.valueOf(meanToken.getEndMeanTime()));

        GBActivity myActivity = (GBActivity)getActivity();
        int locPrecision = GBGeneralSettings.getLocPrecision(myActivity);
        int stdPrecision  = GBGeneralSettings.getStdDevPrecision(myActivity);

        meanWgs84LatitudeInput   .setText(truncatePrecisionString(locPrecision, meanCoordinate.getLatitude()));
        meanWgs84LongitudeInput  .setText(truncatePrecisionString(locPrecision, meanCoordinate.getLongitude()));

        meanWgs84ElevationMetersInput  .setText(truncatePrecisionString(locPrecision, meanCoordinate.getElevation()));
        meanWgs84ElevationFeetInput    .setText(truncatePrecisionString(locPrecision, meanCoordinate.getElevationFeet()));
        meanWgs84GeoidHeightMetersInput.setText(truncatePrecisionString(locPrecision, meanCoordinate.getGeoid()));
        meanWgs84GeoidHeightFeetInput  .setText(truncatePrecisionString(locPrecision, meanCoordinate.getGeoidFeet()));

        meanWgs84LatSigmaOutput .setText(truncatePrecisionString(stdPrecision, meanCoordinate.getLatitudeStdDev()));
        meanWgs84LongSigmaOutput.setText(truncatePrecisionString(stdPrecision, meanCoordinate.getLongitudeStdDev()));
        meanWgs84ElevSigmaOutput.setText(truncatePrecisionString(stdPrecision, meanCoordinate.getElevationStdDev()));

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
         int tudeColor= R.color.colorPosNumber;
         if (tude < 0) {
             //tude is negative, remember this and work with the absolute value
             tude = Math.abs(tude);
             isTudePos = false;
             tudeColor = R.color.colorNegNumber;
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


         GBActivity myActivity = (GBActivity)getActivity();
         int locPrecision = GBGeneralSettings.getLocPrecision(myActivity);
         int caPrecision  = GBGeneralSettings.getCAPrecision(myActivity);
         int sfPrecision  = GBGeneralSettings.getSfPrecision(myActivity);
         int stdPrecision = GBGeneralSettings.getStdDevPrecision(myActivity);


         //show the user the result
         tudeDDInput.setText(truncatePrecisionString(locPrecision, tude));
         tudeDInput.setText(String.valueOf(tudeDegree));
         tudeMInput.setText(String.valueOf(tudeMinute));
         tudeSInput.setText(truncatePrecisionString(locPrecision, tudeSecond));

         tudeDDInput.setTextColor(ContextCompat.getColor(context, tudeColor));
         tudeDInput .setTextColor(ContextCompat.getColor(context, tudeColor));
         tudeMInput .setTextColor(ContextCompat.getColor(context, tudeColor));
         tudeSInput .setTextColor(ContextCompat.getColor(context, tudeColor));

         return true;
    }




    private boolean updateUtmUI(GBCoordinateUTM utmCoordinate){
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

            //Also output the result in separate fields
            utmZoneOutput        .setText(String.valueOf(utmCoordinate.getZone()));
            utmHemisphereOutput  .setText(String.valueOf(utmCoordinate.getHemisphere()));
            utmLatbandOutput     .setText(String.valueOf(utmCoordinate.getLatBand()));
            utmEastingMetersOutput.setText(String.valueOf(utmCoordinate.getEasting()));
            utmNorthingMetersOutput.setText(String.valueOf(utmCoordinate.getNorthing()));
            utmEastingFeetOutput .setText(String.valueOf(utmCoordinate.getEastingFeet()));
            utmNorthingFeetOutput.setText(String.valueOf(utmCoordinate.getNorthingFeet()));
            utmConvergenceOutput .setText(String.valueOf(utmCoordinate.getConvergenceAngle()));
            utmScaleFactorOutput .setText(String.valueOf(utmCoordinate.getScaleFactor()));
            return true;

        } catch (IllegalArgumentException exc) {
            //input parameters were not within range
            utmEastingMetersOutput.setText(R.string.input_wrong_range_error);
            utmNorthingMetersOutput.setText(R.string.input_wrong_range_error);
            return false;
        }
    }

    private void updateSPCSUI(GBCoordinateWGS84 coordinateWgs){
        View v = getView();
        if (v == null)return;
        //need to ask for zone, then convert based on the zone
        EditText spcZoneInput = (EditText)v.findViewById(R.id.spcZoneOutput);
        String zoneString = spcZoneInput.getText().toString();
        int zone = Integer.valueOf(zoneString);

        GBCoordinateSPCS spcsCoordinate = new GBCoordinateSPCS(coordinateWgs, zone);

        TextView spcStateOutput          =  (TextView) v.findViewById(R.id.spcStateOutput);

        if ((spcsCoordinate.getZone() == (int)GBUtilities.ID_DOES_NOT_EXIST) ||
            (spcsCoordinate.getZone() != zone))    {
            clearSPCSUI(v);
            spcZoneInput            .setText(String.valueOf(spcsCoordinate.getZone()));
            spcStateOutput         .setText(getString(R.string.spc_zone_error));
            return;
        }


        //SPC
        TextView spcEastingMetersOutput  =  (TextView) v.findViewById(R.id.spcEastingMetersOutput);
        TextView spcNorthingMetersOutput =  (TextView) v.findViewById(R.id.spcNorthingMetersOutput);
        TextView spcEastingFeetOutput    =  (TextView) v.findViewById(R.id.spcEastingFeetOutput);
        TextView spcNorthingFeetOutput   =  (TextView) v.findViewById(R.id.spcNorthingFeetOutput);

        TextView spcConvergenceOutput    =  (TextView) v.findViewById(R.id.spcConvergenceOutput);
        TextView spcScaleFactorOutput    =  (TextView) v.findViewById(R.id.spcScaleFactorOutput);

        GBActivity myActivity = (GBActivity)getActivity();
        int locPrecision = GBGeneralSettings.getLocPrecision(myActivity);
        int caPrecision  = GBGeneralSettings.getCAPrecision(myActivity);
        int sfPrecision  = GBGeneralSettings.getSfPrecision(myActivity);

        spcZoneInput           .setText(String.valueOf(spcsCoordinate.getZone()));
        spcStateOutput         .setText(spcsCoordinate.getState());
        spcEastingMetersOutput .setText(truncatePrecisionString(locPrecision, spcsCoordinate.getEasting()));

        spcNorthingMetersOutput.setText(truncatePrecisionString(locPrecision, spcsCoordinate.getNorthing()));
        spcEastingFeetOutput   .setText(truncatePrecisionString(locPrecision, spcsCoordinate.getEastingFeet()));
        spcNorthingFeetOutput  .setText(truncatePrecisionString(locPrecision, spcsCoordinate.getNorthingFeet()));
        spcConvergenceOutput   .setText(truncatePrecisionString(caPrecision, spcsCoordinate.getConvergenceAngle()));

        spcScaleFactorOutput   .setText(truncatePrecisionString(sfPrecision, spcsCoordinate.getScaleFactor()));


    }



    //truncate digits of precision
    private String truncatePrecisionString(int digitsOfPrecision, double reading) {
        return GBUtilities.truncatePrecisionString(reading, digitsOfPrecision);
    }





    private void clearForm() {
        View v = getView();
        if (v == null)return;

        clearGPSUI(v);

        clearMeanUI(v);

        clearSPCSUI(v);

        clearUtmUI(v);

        clearNadUI(v);


    }

    private void clearMeanUI(View v){

        //Mean Parameters
        TextView meanWgs84StartTimeOutput    = (TextView)v.findViewById(R.id.meanWgs84StartTimeOutput);
        TextView meanWgs84EndTimeOutput      = (TextView)v.findViewById(R.id.meanWgs84EndTimeOutput);
        TextView meanWgs84PointsInMeanOutput = (TextView)v.findViewById(R.id.meanWgs84PointsInMeanOutput);


        //Mean Standard Deviations
        TextView meanWgs84LatSigmaOutput = (TextView)v.findViewById(R.id.meanWgs84LatSigmaOutput);
        TextView meanWgs84LongSigmaOutput= (TextView)v.findViewById(R.id.meanWgs84LngSigmaOutput);
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
        TextView utmZoneOutput           =  (TextView) v.findViewById(R.id.utmZoneOutput);
        TextView utmLatbandOutput        =  (TextView) v.findViewById(R.id.utmLatbandOutput);
        TextView utmHemisphereOutput     =  (TextView) v.findViewById(R.id.utmHemisphereOutput);
        TextView utmEastingMetersOutput  =  (TextView) v.findViewById(R.id.utmEastingMetersOutput);
        TextView utmNorthingMetersOutput =  (TextView) v.findViewById(R.id.utmNorthingMetersOutput);
        TextView utmEastingFeetOutput    =  (TextView) v.findViewById(R.id.utmEastingFeetOutput);
        TextView utmNorthingFeetOutput   =  (TextView) v.findViewById(R.id.utmNorthingFeetOutput);

        TextView utmConvergenceOutput    =  (TextView) v.findViewById(R.id.utmConvergence);
        TextView utmScaleFactorOutput    =  (TextView) v.findViewById(R.id.utmScaleFactor);


        utmZoneOutput.          setText("");
        utmLatbandOutput.       setText("");
        utmHemisphereOutput.    setText("");
        utmEastingMetersOutput. setText("");
        utmNorthingMetersOutput.setText("");
        utmEastingFeetOutput.   setText("");
        utmNorthingFeetOutput.  setText("");

        utmConvergenceOutput.   setText("");
        utmScaleFactorOutput.   setText("");

    }

    private void clearNadUI(View v){
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
        TextView nad83ElevationFeetInput   = (TextView) v.findViewById(R.id.nad83ElevationFeetInput);
        TextView nad83GeoidHeightMetersInput = (TextView) v.findViewById(R.id.nad83GeoidHeightMetersInput);
        TextView nad83GeoidHeightFeetInput = (TextView) v.findViewById(R.id.nad83GeoidHeightFeetInput);

        nad83LatitudeInput  .setText("");
        nad83LatDegreesInput.setText("");
        nad83LatMinutesInput.setText("");
        nad83LatSecondsInput.setText("");

        nad83LongitudeInput  .setText("");
        nad83LongDegreesInput.setText("");
        nad83LongMinutesInput.setText("");
        nad83LongSecondsInput.setText("");

        nad83ElevationMetersInput  .setText("");
        nad83ElevationFeetInput    .setText("");
        nad83GeoidHeightMetersInput.setText("");
        nad83GeoidHeightFeetInput  .setText("");



    }

    private void clearSPCSUI(View v){



        //SPC
        TextView spcZoneOutput           =  (TextView) v.findViewById(R.id.spcZoneOutput);
        TextView spcStateOutput          =  (TextView) v.findViewById(R.id.spcStateOutput);

        TextView spcEastingMetersOutput  =  (TextView) v.findViewById(R.id.spcEastingMetersOutput);
        TextView spcNorthingMetersOutput =  (TextView) v.findViewById(R.id.spcNorthingMetersOutput);
        TextView spcEastingFeetOutput    =  (TextView) v.findViewById(R.id.spcEastingFeetOutput);
        TextView spcNorthingFeetOutput   =  (TextView) v.findViewById(R.id.spcNorthingFeetOutput);

        TextView spcConvergenceOutput    =  (TextView) v.findViewById(R.id.spcConvergenceOutput);
        TextView spcScaleFactorOutput    =  (TextView) v.findViewById(R.id.spcScaleFactorOutput);


        spcZoneOutput          .setText("");
        spcStateOutput         .setText("");
        spcEastingMetersOutput .setText("");

        spcNorthingMetersOutput.setText("");
        spcEastingFeetOutput   .setText("");
        spcNorthingFeetOutput  .setText("");
        spcConvergenceOutput   .setText("");

        spcScaleFactorOutput   .setText("");


    }



}


