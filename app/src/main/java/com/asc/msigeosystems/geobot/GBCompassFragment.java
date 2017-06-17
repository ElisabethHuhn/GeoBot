package com.asc.msigeosystems.geobot;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;

/**
 * Created by Elisabeth Huhn on 11/19/2016.
 *
 * Code from internet that's been modified for GB
 */

public class GBCompassFragment extends Fragment
                                            implements SensorEventListener, LocationListener {

    /* ***********************************************************************/
    /*                Static Variables                                       */
    /* ***********************************************************************/
    public static final String NA = "N/A";
    public static final String FIXED = "FIXED";
    // location min time
    private static final int LOCATION_MIN_TIME = 30 * 1000;
    // location min distance
    private static final int LOCATION_MIN_DISTANCE = 10;


    /* ***********************************************************************/
    /*                Member Variables                                       */
    /* ***********************************************************************/

    // Gravity for accelerometer data
    private float[] mGravity = new float[3];
    // magnetic data
    private float[] mGeomagnetic = new float[3];
    // Rotation data
    private float[] mRotation = new float[9];
    // mOrientation (azimuth, pitch, roll)
    private float[] mOrientation = new float[3];
    // mSmoothed values
    private float[] mSmoothed = new float[3];

    // sensor manager
    private SensorManager mSensorManager;
    // sensor mGravity
    private Sensor mSensorGravity;
    private Sensor mSensorMagnetic;

    private LocationManager mLocationManager;
    private Location mCurrentLocation;

    private GeomagneticField mGeomagneticField;
    private double mBearing = 0;

    private TextView mTextDirection, mTextLat, mTextLong;

    //The Custom View for the compass itself
    private GBCompassRoseView mCompassView;


    /* ***********************************************************************/
    /*                Lifecycle Methods                                      */
    /* ***********************************************************************/

    @Override
    public View onCreateView(LayoutInflater inflater,
                                ViewGroup container,
                                Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        //Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_compass_rose, container, false);


        //Wire up the UI widgets so they can handle events later
        wireWidgets(v);


        //set the title bar subtitle
        setSubtitle();


        // keep screen light on (wake lock light)
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


        return v;
    }


    @Override
    public void onResume() {
        super.onResume();
        setSubtitle();
    }



    private void wireWidgets(View v){
        //wire up the widgets

        mTextLat = (TextView) v.findViewById(R.id.compass_latitude);
        mTextLong = (TextView) v.findViewById(R.id.compass_longitude);
        mTextDirection = (TextView) v.findViewById(R.id.compass_label);
        mCompassView = (GBCompassRoseView) v.findViewById(R.id.compass_rose);

    }

    private void setSubtitle(){
        ((GBActivity) getActivity()).setSubtitle(R.string.subtitle_compass);
    }

    @Override
    public void onStart() {
        super.onStart();

        //set up the sensors
        mSensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        mSensorGravity = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorMagnetic = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        // listen to these sensors
        mSensorManager.registerListener(this, mSensorGravity,SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mSensorMagnetic,SensorManager.SENSOR_DELAY_NORMAL);

        //Make sure we have the proper GPS permissions before starting
        //If we don't currently have permission, bail
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){
            getFalseLocation();
            return;
        }

        //get the location manager
        mLocationManager = (LocationManager)getActivity().getSystemService(Context.LOCATION_SERVICE);
        //request the location data
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                                                                LOCATION_MIN_TIME,
                                                                LOCATION_MIN_DISTANCE,
                                                                this);

        // get last known position from gps
        Location gpsLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        //if we can't get the location from gps, least try the network position
        // if we can't even get a network location, put in an obviously false position
        if (gpsLocation != null) {
            //the gps location is good
            mCurrentLocation = gpsLocation;
        } else {
            // try with network provider
            Location networkLocation =
                    mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

            if (networkLocation != null) {
                mCurrentLocation = networkLocation;
            } else {
                // Fix a position
                getFalseLocation();
            }

            // set current location
            onLocationChanged(mCurrentLocation);
        }
    }

    private Location getFalseLocation() {
        Location location = new Location(FIXED);
        location.setAltitude(1);
        location.setLatitude(0);
        location.setLongitude(0);
        return location;
    }


    @Override
    public void onStop() {
        super.onStop();
        // remove listeners
        mSensorManager.unregisterListener(this, mSensorGravity);
        mSensorManager.unregisterListener(this, mSensorMagnetic);

        //We can't even stop the updates if we don't have the proper permissions

        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED){return;}
        mLocationManager.removeUpdates(this);
    }


    /* ***********************************************************************/
    /*               Sensor Callbacks                                        */
    /* ***********************************************************************/
    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        // used to update location info on screen
        updateLocation(location);
        mGeomagneticField = new GeomagneticField((float) mCurrentLocation.getLatitude(),
                                                (float) mCurrentLocation.getLongitude(),
                                                (float) mCurrentLocation.getAltitude(),
                                                System.currentTimeMillis());
    }

    private void updateLocation(Location location) {
        if (FIXED.equals(location.getProvider())) {
            mTextLat.setText(NA);
            mTextLong.setText(NA);
        } else {

            // better => make this creation outside method
            DecimalFormatSymbols dfs = new DecimalFormatSymbols();
            dfs.setDecimalSeparator('.');
            NumberFormat formatter = new DecimalFormat("#0.00", dfs);
            CharSequence temp = "Lat : " + formatter.format(location.getLatitude());
            mTextLat.setText(temp);
            temp = "Long : " + formatter.format(location.getLongitude());
            mTextLong.setText(temp);
        }
    }

    /* ***********************************************************************/
    /*          GPS Status Callbacks stubs only                              */
    /* ***********************************************************************/
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }


    /* ***********************************************************************/
    /*          Magnetic or Accelerometer Sensor Callback                    */
    /* ***********************************************************************/
    /*         When the magnetic sensor changes, rotate the rose             */
    /* ***********************************************************************/
    @Override
    public void onSensorChanged(SensorEvent event) {
        /*
        if (evt.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            gravSensorVals = lowPass(evt.values.clone(), gravSensorVals);
        } else if (evt.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            magSensorVals = lowPass(evt.values.clone(), magSensorVals);
        }

        if (gravSensorVals != null && magSensorVals != null) {
            SensorManager.getRotationMatrix(RTmp, I, gravSensorVals, magSensorVals);
            int rotation = Compatibility.getRotation(this);
            if (rotation == 1) {
                SensorManager.remapCoordinateSystem(RTmp, SensorManager.AXIS_X, SensorManager.AXIS_MINUS_Z, Rot);
            } else {
                SensorManager.remapCoordinateSystem(RTmp, SensorManager.AXIS_Y, SensorManager.AXIS_MINUS_Z, Rot);
            }

            SensorManager.getOrientation(Rot, results);
            UIARView.azimuth = (float)(((results[0]*180)/Math.PI)+180);
            UIARView.pitch = (float)(((results[1]*180/Math.PI))+90);
            UIARView.roll = (float)(((results[2]*180/Math.PI)));
            radarMarkerView.postInvalidate();
        }
        */

        boolean accelOrMagnetic = false;

        // get accelerometer data
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            // we need to use a low pass lowPassFilter to make data mSmoothed
            mSmoothed = lowPass(event.values, mGravity);
            mGravity[0] = mSmoothed[0];
            mGravity[1] = mSmoothed[1];
            mGravity[2] = mSmoothed[2];
            accelOrMagnetic = true;

        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            mSmoothed = lowPass(event.values, mGeomagnetic);
            mGeomagnetic[0] = mSmoothed[0];
            mGeomagnetic[1] = mSmoothed[1];
            mGeomagnetic[2] = mSmoothed[2];
            accelOrMagnetic = true;

        }

        // get mRotation matrix to get mGravity and magnetic data
        SensorManager.getRotationMatrix(mRotation, null, mGravity, mGeomagnetic);
        // get mBearing to target
        SensorManager.getOrientation(mRotation, mOrientation);
        // east degrees of true North
        mBearing = mOrientation[0];
        // convert from radians to degrees
        mBearing = Math.toDegrees(mBearing);

        // fix difference between true North and magnetic North
        if (mGeomagneticField != null) {
            mBearing += mGeomagneticField.getDeclination();
        }

        // mBearing must be in 0-360
        if (mBearing < 0) {
            mBearing += 360;
        }

        // update compass view
        mCompassView.setBearing((float) mBearing);

        if (accelOrMagnetic) {
            mCompassView.postInvalidate();
        }

        updateTextDirection(mBearing); // display text direction on screen
    }

    /* ***********************************************************************/
    /*          Utilities                                                    */
    /* ***********************************************************************/
    static final float ALPHA = 0.15f;

    protected float[] lowPass( float[] input, float[] output ) {
        if ( output == null ) return input;
        for ( int i=0; i<input.length; i++ ) {
            output[i] = output[i] + ALPHA * (input[i] - output[i]);
        }
        return output;
    }

    //This is written for the accelerometer, but the algorithm is the same for the compass
    private float[] lowPassFilter(float[] eventValues, float[] pastValues ) {
        // alpha is calculated as t / (t + dT)
        // with t, the low-pass filter's time-constant
        // and dT, the event delivery rate

        final float alpha = 0.8f;
        float [] linear_acceleration = new float[3];

        pastValues[0] = alpha * pastValues[0] + (1 - alpha) * eventValues[0];
        pastValues[1] = alpha * pastValues[1] + (1 - alpha) * eventValues[1];
        pastValues[2] = alpha * pastValues[2] + (1 - alpha) * eventValues[2];

        linear_acceleration[0] = eventValues[0] - pastValues[0];
        linear_acceleration[1] = eventValues[1] - pastValues[1];
        linear_acceleration[2] = eventValues[2] - pastValues[2];

        return linear_acceleration;
    }

    private void updateTextDirection(double bearing) {
        int range = (int) (bearing / (360f / 16f));
        String dirTxt = "";

        if (range == 15 || range == 0)
            dirTxt = "N";
        if (range == 1 || range == 2)
            dirTxt = "NE";
        if (range == 3 || range == 4)
            dirTxt = "E";
        if (range == 5 || range == 6)
            dirTxt = "SE";
        if (range == 7 || range == 8)
            dirTxt = "S";
        if (range == 9 || range == 10)
            dirTxt = "SW";
        if (range == 11 || range == 12)
            dirTxt = "W";
        if (range == 13 || range == 14)
            dirTxt = "NW";

        mTextDirection.setText("" + ((int) bearing) + ((char) 176) + " "
                + dirTxt); // char 176 ) = degrees ...
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        if (sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD
                && accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE) {
            // manage fact that compass data are unreliable ...
            // toast ? display on screen ?
            Toast.makeText(getActivity(), "Compass data is unreliable", Toast.LENGTH_SHORT).show();
        }
    }
}

