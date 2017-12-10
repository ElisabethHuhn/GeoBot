package com.asc.msigeosystems.geobot;


import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.SphericalUtil;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import static android.provider.CalendarContract.CalendarCache.URI;

/**
 * The Collect Fragment is the UI
 * when the user is making point measurements in the field
 * Created by Elisabeth Huhn on 4/13/2016.
 */
public class GBPointCollectFragment extends Fragment implements //OnMapReadyCallback,
                                                                         //GpsStatus.Listener,
        LocationListener,
                                                                         GpsStatus.NmeaListener {

    //DEFINE constants / literals
    public static final int MY_PERMISSIONS_REQUEST_CAMERA = 1;
    //public static final int MY_PERMISSIONS_REQUEST_SAVE_PICTURES = 2;

    private static final int OFFSET_NEXT_ONLY  = 0;
    private static final int OFFSET_ALL_FUTURE = 1;
    private static final int OFFSET_RESET      = 2;
    private static final int OFFSET_ID_CONSTANT = 100;

    //Set this true to use TestData rather than NMEA live
    static final boolean  DEBUG = false;

    private static final String TAG = "CollectPointsFragment";
    private static final float  markerColorProvisional = BitmapDescriptorFactory.HUE_BLUE;
    private static final float  markerColorFinal       = BitmapDescriptorFactory.HUE_RED;

    //return codes from intents
    private static final int REQUEST_CODE_IMAGE_CAPTURE = 1;

    //State Keys
    //Focus Variables
    private static final String FOCUS_LATITUDE = "FocusLat";
    private static final String FOCUS_LONGITUDE = "FocusLng";

    //Photo variables
    private static final String PHOTO_PATH = "FotoPath";
    private static final String PHOTO_FILE = "FotoFile";
    private static final String PHOTO_TIME = "FotoTime";
    private static final String NOTES_TEXT = "NotesText";

    //Offset Variables
    private static final String OFFSET_HEADING = "OffsetHeading";
    private static final String OFFSET_DISTANCE = "OffsetDistance";
    private static final String OFFSET_ELEVATION = "OffsetElev";
    private static final String OFFSET_CHECKED_ID = "OffsetID";




    //Maps variables
    private static final String IS_MAP_INIT = "IsMapInit";
    private static final String IS_AUTO_RESIZE = "IsaUTOResize";







    //* ****************************************/
    //* *     variables for processing      ****/
    //* ****************************************/

    //* *************************************************************/
    //* *     variables to be saved on configuration change      ****/
    //* *************************************************************/

    //Screen Focus
    private double mLatitudeScreenFocus;
    private double mLongitudeScreenFocus;

    //Reconfiguration in the middle of camera or notes is problematic


    //Can not take pictures or record notes while the meaning is in progress HOWEVER
    //The following variables are used in the communication
    // of one part of the picture/notes process to the next
    //Camera
    private String mCurrentPhotoPath = "";
    private String mCurrentPhotoFileName = "";
    private String mCurrentPhotoTimestamp = "";

    //Notes
    private CharSequence mNotes = "";

    //Offset Positions
    //Offsets are defined by the user, then used on subsequent point creation events. Meaned or not
    private double mOffsetDistance   = 0d;
    private double mOffsetHeading    = 0d;
    private double mOffsetElevation  = 0d;
    //The id of the radio button serves as the
    // flag for how many points the offset is to be applied to
    private int    mOffsetCheckedID  = OFFSET_NEXT_ONLY + OFFSET_ID_CONSTANT;


 


       //Token for the meaning process
    //This token exists throughout the life of a meaning cycle,
    //  being handed from step to step, butilding up the mean
    //Meaning MUST continue through reconfiguration, but it is acceptable to
    //  miss the points that were read while this App is not current
    //this means that if the mean is limited by time, there will be fewer points in
    //  the intervening time period
    private GBMeanToken mMeanToken;

 


    //These fields come from NMEA, but ultimately are saved on the point

    //Quality fields to be added to the new point
    GBPointQualityToken mPointQualityToken = new GBPointQualityToken();



    //variables for the map
    //only used at very beginning of app to mark end of initialization
    private boolean      isMapInitialized = false;
    //indicates whether map should be automatically resized when a point is added
    private boolean      isAutoResizeOn = true;



    // TODO: 1/8/2017 Think about maintaining a list of points, rather than a list of markers
    //then use the "add all points" routine to recreate the map

    // TODO: 1/15/2017 how do these lists correspond to each other?
    //private ArrayList<GBCoordinateWGS84> mMeanToken.mMeanedCoordinates()


    //Markers that are actually on the map
    private ArrayList<Marker> mMarkers     = new ArrayList<>();
    //Markers that are within the zoom boundaries
    private ArrayList<Marker> mZoomMarkers = new ArrayList<>();




    //* ***************************************************************/
    //* *     variables to be ignored on configuration change      ****/
    //* ***************************************************************/
    //JUST DUMP THE LATEST NMEA DATA BUNDLE, ANOTHER ONE WILL ARRIVE IN A SECOND
    private GBNmea         mNmeaData;     //latest nmea sentence received

    //variables needed to keep track of location
    //Set by calling initializeGPS()
    //If the code finds a void LocationManager, it is simply reInitialized
    //used as a flag to determine whether GPS initialized yet in startGPS() and stopGPS()
    private LocationManager mLocationManager;


    //needs to be removed,
    // but have to figure out how to get this info from NMEA rather than the LocationManager
    //private GpsStatus           mGpsStatus = null;

    //Test Data
    private int        mTestDataCounter = 0;
    GBTestLocationData mTestData;
    int                mTestDataMax = 13;


    //set by determineFocus()
    //the first step of picture or notes
    private boolean isFocusOnPoint = false; //if false, focus on the project
    private Marker mFocusMarker;




    //Map data that doesn't need to survive a configuration change

    //The map will need to be reinitialized after each reconfiguration change
    //initializeMap()
    private MapView      mMapView;
    private GoogleMap    mMap;

    private Marker       mLastMarkerAdded;

    //buildNewZoom(markerList, doZoom) will rebuild these from mZoomMarkers
    //boolean doZoom controls whether the map is actually updated
    private LatLngBounds.Builder mZoomBuilder;
    private LatLngBounds         mZoomBounds;


    //redrawLineBetweenMarkers() recreates these variables
    // and draws the marker line from points in mMarkers
    private Polyline mPointsLine;
    private PolylineOptions mLineOptions;




    //* *******************************************************************/
    /*                Constructor                                         */
    //* *******************************************************************/
    public GBPointCollectFragment() {
        //for now, we don't need to initialize anything when the fragment
        //  is first created
    }


    //* *******************************************************************/
    /*          Lifecycle Methods                                         */
    //* *******************************************************************/
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_points_collect_gb, container, false);

        //update the view with the mapsView
       // addMapsView(v);
        //and then initialize it
        initializeMaps(savedInstanceState, v);

        //Wire up the other UI widgets so they can handle events later
        wireWidgets(v);

        CharSequence projectName = GBUtilities.getInstance().
                                        getOpenProject((GBActivity)getActivity()).getProjectName();

        //Inform the user of the name of the open project
        TextView currentProjectField  = (TextView)v.findViewById(R.id.currentProjectField);
        String currentFileString = String.format(Locale.getDefault(),
                                                 getString(R.string.current_file_label),
                                                 projectName);
        currentProjectField.setText(currentFileString);

        initializeGPS();

        //Set the titlebar subtitle
        setSubtitle();

        initializeTestData();

        setRetainInstance(true);
        return v;
    }

    //Ask for location events to start
    @Override
    public void onStart(){
        if (mMapView != null){
            mMapView.onStart();
        }
        super.onStart();
    }

    @Override
    public void onResume() {
        if (mMapView != null){
            mMapView.onResume();
        }

        super.onResume();

        setSubtitle();

        startGps();


        if ((mLatitudeScreenFocus != 0) || (mLongitudeScreenFocus != 0)){
            handleCurrentPosition();
            //redrawMapMarkers(mMarkers, true);
        }

     }




    //Ask for location events to stop
    @Override
    public void onPause() {

        if (mMapView != null){
            mMapView.onPause();
        }
        super.onPause();



        stopGps();


    }

    @Override
    public void onStop(){
        if (mMapView != null){
            mMapView.onStop();
        }
        super.onStop();
    }


    @Override
    public void onDestroy(){
        try {
            if (mMapView != null){
                mMapView.onDestroy();
            }
        } catch (Exception e) {
            Log.e(TAG, getString(R.string.collect_points_destroy_error), e);
        }
        super.onDestroy();

    }

    @Override
    public void onLowMemory(){

        if (mMapView != null){
            mMapView.onLowMemory();
        }
        super.onLowMemory();

    }


    @Override
    public void onSaveInstanceState(Bundle outState){
        if (mMapView != null){
            mMapView.onSaveInstanceState(outState);
        }
        super.onSaveInstanceState(outState);
        //saveState(outState);
    }

    private void saveState(Bundle outState){
        //save focus variables
        outState.putDouble(FOCUS_LATITUDE,  mLatitudeScreenFocus);
        outState.putDouble(FOCUS_LONGITUDE, mLongitudeScreenFocus);

        //Camera Variables
        outState.putCharSequence(PHOTO_PATH, mCurrentPhotoPath);
        outState.putCharSequence(PHOTO_FILE, mCurrentPhotoFileName);
        outState.putCharSequence(PHOTO_TIME, mCurrentPhotoTimestamp);

        //Notes Variables
        outState.putCharSequence(NOTES_TEXT, mNotes);

        //Offset variables
        outState.putDouble(OFFSET_DISTANCE,   mOffsetDistance);
        outState.putDouble(OFFSET_HEADING,    mOffsetHeading);
        outState.putDouble(OFFSET_ELEVATION,  mOffsetElevation);
        outState.putInt   (OFFSET_CHECKED_ID, mOffsetCheckedID);

        //Meaning Variables
        if (mMeanToken != null){
            mMeanToken.saveState(outState);
        }



        //Point Quality Variables
        if (mPointQualityToken != null) {
            mPointQualityToken.saveState(outState);
        }

        //Map Variables
        //0 = false, 1 = true
        int tempBoolean = 0;
        if (isMapInitialized)tempBoolean = 1;
        outState.putInt(IS_MAP_INIT, tempBoolean);

        tempBoolean = 0;
        if (isAutoResizeOn)tempBoolean = 1;
        outState.putInt(IS_AUTO_RESIZE, tempBoolean);




    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == MY_PERMISSIONS_REQUEST_CAMERA) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                View v = getView();
                if (v != null) {
                    Button pictureButton = (Button) v.findViewById(R.id.pictureButton);
                    pictureButton.setEnabled(true);
                    GBUtilities.getInstance().showStatus(getActivity(), R.string.try_camera_again);
                }
            }
        }
    }


    //* ******************************************************//
    //* *************  GPS Routines    ***********************//
    //* ******************************************************//

    private void initializeGPS(){

        //Make sure we have the proper GPS permissions before starting
        //If we don't currently have permission, bail
        if ((ContextCompat.checkSelfPermission(getActivity(),
                                                        Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) ||
            (ContextCompat.checkSelfPermission(getActivity(),
                                                        Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED)){return;}

        mLocationManager =
                        (LocationManager)getActivity().getSystemService(Context.LOCATION_SERVICE);

        //but don't turn them on until onResume()
        //Location Manager has to be receiving updates for us to receive NMEA updates
        //mLocationManager.requestLocationUpdates("gps", 0, 0.0f, this);
        //mLocationManager.addGpsStatusListener(this);
        //mLocationManager.addNmeaListener(this);

    }

    private void startGps(){
        if (ContextCompat.checkSelfPermission(getActivity(),
                                                        Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(getActivity(),
                                                        Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){return;}

        //ask the Location Manager to start sending us updates
        if (mLocationManager == null)initializeGPS();
        //location manager has to be receiving updates for us to receive NMEA sentences
        mLocationManager.requestLocationUpdates("gps", 0, 0.0f, this);
        //mLocationManager.addGpsStatusListener(this);
        mLocationManager.addNmeaListener(this);


    }

    private void stopGps() {
        if (ContextCompat.checkSelfPermission(getActivity(),
                                                        Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(getActivity(),
                                                        Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){return;}

        if (mLocationManager == null)return;

        //Location manager has to be receiving updates for us to receive NMEA sentences
        mLocationManager.removeUpdates(this);
        //mLocationManager.removeGpsStatusListener(this);
        mLocationManager.removeNmeaListener(this);
    }


    //* ****************************************************************//
    //             GPS Listener Callbacks                               //
    //            Called by the OS to handle GPS events                 //
    //* ****************************************************************//

    //GpsStatus.Listener Callback

    /*******************************************************
     *
     *
     *OS calls this callback when
     * a change has been detected in GPS satellite status
     *Called to report changes in the GPS status.
     *
     * The parameter event type is one of:
     * o GPS_EVENT_STARTED
     * o GPS_EVENT_STOPPED
     * o GPS_EVENT_FIRST_FIX
     * o GPS_EVENT_SATELLITE_STATUS
     *
     *When this method is called,
     * the client should call getGpsStatus(GpsStatus)
     * to get additional status information.
     */
    /*
    @Override
    public void onGpsStatusChanged(int eventType) {

    }
*/


    //* ****************************************************************//
    //             Location Listener Callbacks                          //
    //            Called by the OS to handle GPS events                 //
    //* ****************************************************************//

    /* called when the GPS provider is turned off
    *  (i.e. user turning off the GPS on the phone)
    *  If requestLocationUpdates is called on an already disabled provider,
    *  this method is called immediately.
    */
    @Override
    public void onProviderDisabled(String provider) {
        if (LocationManager.GPS_PROVIDER.equals(provider)){

        }
    }

    /* called when the GPS provider is turned on
    *  (i.e. user turning on the GPS on the phone)
    */
    @Override
    public void onProviderEnabled(String provider) {
        if (LocationManager.GPS_PROVIDER.equals(provider)){

        }
    }

    /*Called when the provider status changes.
    *  This method is called when
    *  o a provider is unable to fetch a location or
    *  o if the provider has recently become available
    *     after a period of unavailability.
    */
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        if (LocationManager.GPS_PROVIDER.equals(provider)){

        }

    }


    // called when the listener is notified with a location update from the GPS
    @Override
    public void onLocationChanged(Location loc) {
        //mCurLocation = new Location(loc); // copy location
        int temp = 0;
    }



    //* ****************************************************************//
    //             NMEA Listener Callbacks                              //
    //            Called by the OS to handle GPS events                 //
    //* ****************************************************************//
    @Override
    public void onNmeaReceived(long timestamp, String nmea) {
        GBNmea nmeaData;
        //todo maybe need to do something with the timestamp

        try {
             //Parse the raw GPS data
             nmeaData = GBNmeaParser.getInstance().parse(nmea);
             if (nmeaData == null)return;

             // TODO: 12/20/2016 remove kludge for test data
            //replace the raw data with test data if we are in test mode
            if (DEBUG && isMeanInProgress()){
                if (nmeaData.getTime() <= 1)return;
                if (mMeanToken.getCurrentMeanTime() <= 1){
                    mMeanToken.setCurrentMeanTime(nmeaData.getTime());
                } else {
                    //wait at least a second between data points
                    if ((nmeaData.getTime() - mMeanToken.getCurrentMeanTime()) < 1) return;
                    mMeanToken.setCurrentMeanTime(nmeaData.getTime());
                }
                nmeaData = getNmeaFromTestData();
                if (nmeaData == null)return;
            }


// TODO: 6/18/2017 There is too much overlap between refineNmeaPosition and updateUIwNmeaPosition
            //refine the lat/long position based on nmea type and the projects coordinate type
            //truncates to number of digits we are interesed in
            //strips out some types we aren't interested in
            nmeaData = refineNmeaPosition(nmeaData);
            if (nmeaData == null)return;

            //update the UI
            //only returns nmeaData which are locations
            nmeaData = updateUIwNmeaPosition(nmeaData);
            if (nmeaData == null)return;


            //So now, store the Location
            mNmeaData = nmeaData;

            //and include it in the mean
            if (isMeanInProgress()) {
                //Fold the new nmea sentence into the ongoing mean
                GBCoordinateMean meanCoordinate = mMeanToken.updateMean((GBActivity)getActivity(),
                                                                         mNmeaData);

                //Is this the first point we have processed?
                if (mMeanToken.isFirstPointInMean()){
                    mMeanToken.setFirstPointInMean( false);
                    mMeanToken.setStartMeanTime(mNmeaData.getTime());

                    //No marker, so we have to create one
                    LatLng newLocation = new LatLng(meanCoordinate.getLatitude(), meanCoordinate.getLongitude());

                    mLastMarkerAdded   = makeNewMarker(newLocation,
                            getString(R.string.collect_points_provisional_marker),
                            markerColorProvisional);
                }

                //is meaning done?
                boolean isMeanComplete = isMeaningDone(nmeaData, mMeanToken);
                mMeanToken.setLastPointInMean(isMeanComplete);


                if (mMeanToken.isLastPointInMean()){
                    mMeanToken.setEndMeanTime(System.currentTimeMillis());
                    //the handler is smart enough to know whether to get location from nmea or mean
                    handleStorePosition(nmeaData, meanCoordinate);
                } else {
                    updateMapWMean(meanCoordinate);
                }

            }

            // the first fixed reading must initialize the map
            if ((!isMapInitialized) && (nmeaData.isFixed())){
                //put the new point on the map
                centerMap(nmeaData);
                isMapInitialized = true;
            }





        } catch (RuntimeException e){
            //there was an exception processing the NMEA Sentence
             GBUtilities.getInstance().showStatus(getActivity(), e.getMessage());

            //throw new RuntimeException(e);
        }
    }


    //* **********************************************************//
    //* ****************** Nmea Utilities ********************//
    //* **********************************************************//

    private boolean updateCoordinateLabels(){
        //*+********* Update the UI coordinate labels ******************/
        //project has to be open
        long openProjectID = GBUtilities.getInstance().getOpenProjectID((GBActivity)getActivity());
        if (openProjectID == GBUtilities.ID_DOES_NOT_EXIST) return false;

        int coordinateType = GBCoordinate.getCoordinateCategoryFromProjectID(openProjectID);
        String nLable = "N: ";
        String eLable = "E: ";

        if (coordinateType == GBCoordinate.sLLWidgets) {
            nLable = "Lat: ";
            eLable = "Long: ";
        }

        View v = getView();
        if (v != null) {
            TextView currentNorthingPositionLable = (TextView) v.findViewById(R.id.currentNorthingPositionLabel);
            TextView currentEastingPositionLabel = (TextView) v.findViewById(R.id.currentEastingPositionLabel);

            currentNorthingPositionLable.setText(nLable);
            currentEastingPositionLabel.setText(eLable);
        }
        return true;

    }

    private GBNmea refineNmeaPosition(GBNmea nmeaData){

        if (nmeaData == null)return null;

        //Get the type of NMEA data passed
        String type = nmeaData.getNmeaType().toString();
        if (GBUtilities.isEmpty(type))return null;


        //Get the coordinates type from the open project
        long openProjectID = GBUtilities.getInstance().getOpenProjectID((GBActivity)getActivity());
        if (openProjectID == GBUtilities.ID_DOES_NOT_EXIST) return null;

        int coordinateType = GBCoordinate.getCoordinateCategoryFromProjectID(openProjectID);


        //the primary location sentences
        if ((type.contains("GGA")) || (type.contains("GNS")) ){

            double nLat, eLong, ele;
            GBCoordinateUTM utmCoordinate = null;

            if (coordinateType == GBCoordinate.sLLWidgets){
                nLat =   nmeaData.getLatitude();
                eLong =  nmeaData.getLongitude();
            } else {
                // TODO: 12/7/2016 this conversion assumes wgs and utm. Bad assumption, fix
                GBCoordinateWGS84 wgsCoordinate =
                        new GBCoordinateWGS84(mNmeaData.getLatitude(), mNmeaData.getLongitude());

                //The UTM constructor performs the conversion from WGS84
                utmCoordinate = new GBCoordinateUTM(wgsCoordinate);
                if (!wgsCoordinate.isValidCoordinate()){
                    GBUtilities.getInstance().showStatus(getActivity(),R.string.coordinate_not_valid);

                    throw new RuntimeException(getString(R.string.coordinate_not_valid));
                }

                nLat =  utmCoordinate.getNorthing();
                eLong = utmCoordinate.getEasting();
            }
            ele = nmeaData.getOrthometricElevation();

            nmeaData.setLatitude(nLat);
            nmeaData.setLongitude(eLong);

            mPointQualityToken.setHdop(nmeaData.getHdop());
            return nmeaData;

        } else if (type.contains("GSA")) {

            // TODO: 1/16/2017 GSA is the sentence that gives us the number of satellites in the fix
            mPointQualityToken.setInFix( nmeaData.getSatellites());

            mPointQualityToken.setPdop(nmeaData.getPdop());
            mPointQualityToken.setHdop(nmeaData.getHdop());
            mPointQualityToken.setVdop(nmeaData.getVdop());

            return null ; //in case we are updating the UI

        }

        return null; //for all other nmea types, we aren't interested
    }





    //only returns non-null value if it's a sentence we are interested in
    // TODO: 12/24/2016 Need to add in other types of coordinates besides WGS 
    //truncates coordinate location fields as a side effect
    private GBNmea updateUIwNmeaPosition(GBNmea nmeaData){

        if (nmeaData == null){
            //there was an exception processing the NMEA Sentence
            GBUtilities.getInstance().showStatus(getActivity(),R.string.null_type_found);
            return null;
        }
        //Which fields have meaning depend upon the type of the sentence
        String type = nmeaData.getNmeaType().toString();
        if (GBUtilities.isEmpty(type))return null;



        //*+********* Update the UI   ******************/
        if (!updateCoordinateLabels())return null;


        long openProjectID = GBUtilities.getInstance().getOpenProjectID((GBActivity)getActivity());
        if (openProjectID == GBUtilities.ID_DOES_NOT_EXIST) return null;

        int coordinateType = GBCoordinate.getCoordinateCategoryFromProjectID(openProjectID);

        //the primary location sentences
        if ((type.contains("GGA")) || (type.contains("GNS")) ){
            //update the location values

            double nLat, eLong, ele;

            nLat =   nmeaData.getLatitude();
            eLong =  nmeaData.getLongitude();


            ele = nmeaData.getOrthometricElevation();

            View v = getView();
            if (v != null) {

                TextView currentNorthingPositionField = (TextView) v.findViewById(R.id.currentNorthingPositionField);
                TextView currentEastingPositionField  = (TextView) v.findViewById(R.id.currentEastingPositionField);
                TextView currentElevationField        = (TextView) v.findViewById(R.id.currentElevationField);

                GBActivity activity = (GBActivity)getActivity();

                int locPrecision = GBGeneralSettings.getLocPrecision(activity);


                String northingValue  = GBUtilities.truncatePrecisionString(nLat, locPrecision);
                String eastingValue   = GBUtilities.truncatePrecisionString(eLong, locPrecision);
                String elevationValue = GBUtilities.truncatePrecisionString(ele, locPrecision);

                currentNorthingPositionField.setText(northingValue);
                currentEastingPositionField.setText(eastingValue);
                currentElevationField.setText(elevationValue);
            }
            return nmeaData;
        }

        else if (type.contains("GSA")) {
            //update the footer values from the satellite;
            // TODO: 1/16/2017 GSA is the sentence that gives us the number of satellites in the fix
              View v = getView();
            if (v != null) {
                TextView hdopField            = (TextView)v.findViewById(R.id.hdop_field);
                TextView vdopField            = (TextView)v.findViewById(R.id.vdop_field);
                TextView pdopField            = (TextView)v.findViewById(R.id.pdop_field);

                String pdopValue = String.format(Locale.getDefault(),
                                                getString(R.string.pdop_msg), nmeaData.getPdop());
                String hdopValue = String.format(Locale.getDefault(),
                                                getString(R.string.hdop_msg), nmeaData.getHdop());
                String vdopValue = String.format(Locale.getDefault(),
                                                getString(R.string.vdop_msg), nmeaData.getVdop());

                pdopField.setText(pdopValue);
                hdopField.setText(hdopValue);
                vdopField.setText(vdopValue);
            }
            mPointQualityToken.setInFix( nmeaData.getSatellites());
            mPointQualityToken.setPdop ( nmeaData.getPdop());
            mPointQualityToken.setHdop ( nmeaData.getHdop());
            mPointQualityToken.setVdop ( nmeaData.getVdop());

            return null;
        }

        return null;

    }



    private boolean isMeaningDone(GBNmea nmeaData, GBMeanToken meanToken){
        //if there isn't a mean in progress, by definition we are done
        if (isMeanStopped()) return true;

        //Check if this is the last point to be meaned
        GBProject openProject = GBUtilities.getInstance().getOpenProject((GBActivity)getActivity());

        boolean endMeaning =  (meanToken.getFixedReadings() >= openProject.getNumMean());

        meanToken.setLastPointInMean(endMeaning);
        return endMeaning;
    }

    private void updateMapWMean(GBCoordinateMean meanCoordinate){
        Double npLong = meanCoordinate.getLongitude();
        Double npLat  = meanCoordinate.getLatitude();
        LatLng newPosition = new LatLng(npLat,npLong);


        //change the screen focus and the marker to the most current mean
        setFocus(meanCoordinate.getLatitude(), meanCoordinate.getLongitude());


        mLastMarkerAdded.setPosition(newPosition);
        //pass the info for the window to the marker
        mLastMarkerAdded.setTag(meanCoordinate);
        mLastMarkerAdded.showInfoWindow();
        zoomToFit();

    }


    //* *********************************************************//
    //* *********  Maps Callback and other Maps routines   ******//
    //* *********************************************************//

    private void initializeMaps(Bundle savedInstanceState, View v){
        // TODO: 12/27/2016 Figure out how to dynamically resize the map view 

        // Gets the MapView from the XML layout and creates it
        mMapView = (MapView) v.findViewById(R.id.map_view);
        //we need to be the one stepping the map through it's lifecycle, not the system
        //  so pass the creation event on to the map
        mMapView.onCreate(savedInstanceState);

        // Gets to GoogleMap from the MapView and does initialization stuff

        //onMapReadyCallback is triggered when the map is ready to be used
        //                   is called when the map is ready for initialization
        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mMap = googleMap;
                initializeMapsReady();
            }
        });
    }

    private void initializeMapsReady(){
        //current location control depends upon permissions
        boolean locEnabled = false;
        //This check doesn't do anything other than allow us to disable locationEnabled
        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION)   == PackageManager.PERMISSION_GRANTED) {

            //locEnabled = true;

        }

        mMap.getUiSettings().setMyLocationButtonEnabled(locEnabled);
        mMap.setMyLocationEnabled(locEnabled);

        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.setIndoorEnabled(false);
        mMap.setBuildingsEnabled(false);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.getUiSettings().setScrollGesturesEnabled(true);
        mMap.getUiSettings().setTiltGesturesEnabled(false);
        mMap.getUiSettings().setRotateGesturesEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(false);


        mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);

        int minZoomLevel = GBGlobalSettings.getInstance().getMinZoomLevel();
        //Make sure we don't zoom in too close
        mMap.setMinZoomPreference(minZoomLevel);

        mMap.setInfoWindowAdapter(new GBInfoWindowAdapter((GBActivity)getActivity()));
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {

                //Handle the map touch event
                handleMapTouch(latLng);
            }
        });


        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener()
        {

            @Override
            public boolean onMarkerClick(Marker marker) {
                GBUtilities.getInstance().showStatus(getActivity(), R.string.marker_touched);

                GBCoordinateMean pointMarker = (GBCoordinateMean) marker.getTag();
                long pointID = pointMarker.getPointID();
                //get the point corresponding to this pointID
                GBPoint point = GBUtilities.getInstance().
                                    getOpenProject((GBActivity)getActivity()).getPoint(pointID);
                if (point == null) {
                    GBUtilities.getInstance().showStatus(getActivity(),  R.string.no_point_at_marker);
                } else {
                    if (isMeanStopped()) {
                        //put up the dialog
                        askMarker(point, marker);
                    }
                }
                return false;//true suppresses default marker behavior, false tells system to respond to event also
            }
        });

        if (mMarkers == null){
            mMarkers = new ArrayList<>();
        }
        if (mZoomMarkers == null){
            mZoomMarkers = new ArrayList<>();
        }
    }



    //* *************************************************************//
    //* *******   Other Initialization Methods   ********************//
    //* *************************************************************//
    private void setSubtitle() {
        ((GBActivity)getActivity()).setSubtitle(R.string.subtitle_collect_points);
    }

    private void initializeTestData(){
        mTestDataCounter = 0;
    }

    private void wireWidgets(View v){
        //We need permissions to take pictures with the camera then store the images
        if (ContextCompat.checkSelfPermission
                (getActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

            //used to identify the request in onRequestPermissionsResult()
            int requestCode = MY_PERMISSIONS_REQUEST_CAMERA;
            ActivityCompat.requestPermissions(getActivity(),
                                              new String[] {Manifest.permission.CAMERA,
                                                       Manifest.permission.WRITE_EXTERNAL_STORAGE },
                                              requestCode);
        }


        //Current Position Block of fields and buttons
        Button currentPositionButton = (Button) v.findViewById(R.id.currentPositionButton);
        currentPositionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){

                GBUtilities.getInstance().showStatus(getActivity(), R.string.current_position_label);
                handleCurrentPosition();

            }
        });


        TextView currentPointIDField = (TextView)v.findViewById(R.id.pointIDField);

        TextView currentNorthingPositionLable = (TextView)v.findViewById(R.id.currentNorthingPositionLabel);
        TextView currentNorthingPositionField = (TextView) v.findViewById(R.id.currentNorthingPositionField);
        TextView currentEastingPositionLabel  = (TextView)v.findViewById(R.id.currentEastingPositionLabel);
        TextView currentEastingPositionField  = (TextView) v.findViewById(R.id.currentEastingPositionField);
        //TextView currentElevationLabel        = (TextView)v.findViewById(R.id.currentElevationLabel);
        TextView currentElevationField        = (EditText)v.findViewById(R.id.currentElevationField);

        TextView currentFeatureCodeField      = (EditText)v.findViewById(R.id.currentFeatureCodeField);
        TextView currentHeightField           = (EditText)v.findViewById(R.id.currentHeightField);



        //Store position button
        Button storePositionButton = (Button) v.findViewById(R.id.storePositionButton);
        storePositionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                //for now, just put up a toast that the button was pressed
                GBUtilities.getInstance().showStatus(getActivity(),  R.string.store_position_label);

                //This knows whether we are in the middle of meaning or just use current position
                GBCoordinateMean meanCoordinate =  null;
                if (isMeanInProgress()){
                    //calculate the mean using the coordinates in the list
                    // TODO: 1/21/2017 does this result in the last point being used twice???
                    meanCoordinate = mMeanToken.getMeanCoordinate();
                }
                //when meanCoordinate is null, the handler will know that
                // meaning was not in progress and use mNeamData to create a new coordinate
                handleStorePosition(mNmeaData, meanCoordinate);


            }
        });

        //Offset position Button
        Button offsetPositionButton = (Button) v.findViewById(R.id.offsetPositionButton);
        offsetPositionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                //for now, just put up a toast that the button was pressed
                GBUtilities.getInstance().showStatus(getActivity(),  R.string.offset_position_label);

                if (isMeanStopped()) {
                    if ((mOffsetDistance  != 0d) ||
                        (mOffsetHeading   != 0d) ||
                        (mOffsetElevation != 0d)){

                        GBUtilities.getInstance().showStatus(getActivity(), R.string.offsets_reset);
                        mOffsetDistance  = 0d;
                        mOffsetHeading   = 0d;
                        mOffsetElevation = 0d;
                    } else {
                        askOffsetPosition();
                    }
                }
            }
        });

        //Average Button
        Button averagePositionButton = (Button) v.findViewById(R.id.averagePositionButton);
        averagePositionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                //for now, just put up a toast that the button was pressed

                if (isMeanStopped()){
                    GBUtilities.getInstance().showStatus(getActivity(), R.string.start_mean);
                    //start the meaning process by calling the event handler
                    handleAveragePosition();
                } else {
                    GBUtilities.getInstance().showStatus(getActivity(), R.string.cancel_mean);
                    //cancel any process in progress
                    handleCancelMeaning();
                }
            }
        });


        //Average Button
        Button showAllPointsButton = (Button) v.findViewById(R.id.showAllPointsButton);
        showAllPointsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){

                GBUtilities.getInstance().showStatus(getActivity(),  R.string.show_all_points_label);

                handleShowAllPoints();

            }
        });


        //maps Button
        Button mapsButton = (Button) v.findViewById(R.id.mapsButtonCollect);
        mapsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isMeanStopped()) {
                    int mapType = mMap.getMapType();
                    if (mapType == GoogleMap.MAP_TYPE_TERRAIN) {
                        mMap.setMapType(GoogleMap.MAP_TYPE_NONE);
                    } else {
                        mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                    }
                }
            }
        });

        //picture (as in camera or video) Button
        Button pictureButton = (Button) v.findViewById(R.id.pictureButton);
        pictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){

                if (isMeanStopped()) {
                    handleTakePicture();
                }

            }
        });


        //notes Button
        Button notesButton = (Button) v.findViewById(R.id.notesButton);
        notesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                GBUtilities.getInstance().showStatus(getActivity(),  R.string.notes_button_label);

                if (isMeanStopped() ){
                    handleNotes();
                }

            }
        });


        //ZOOMIN Button
        ImageButton zoomInButton = (ImageButton) v.findViewById(R.id.zoomInButton);
        zoomInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                GBUtilities.getInstance().showStatus(getActivity(),  R.string.zoom_in_button_label);

                updateCamera(CameraUpdateFactory.zoomIn());
                isAutoResizeOn = false;

            }
        });

        //ZOOM OUT Button
        ImageButton zoomOutButton = (ImageButton) v.findViewById(R.id.zoomOutButton);
        zoomOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                GBUtilities.getInstance().showStatus(getActivity(),  R.string.zoom_out_button_label);

                updateCamera(CameraUpdateFactory.zoomOut());
                isAutoResizeOn = false;
            }
        });

        //ZOOM ext Button
        ImageButton zoomExtButton = (ImageButton) v.findViewById(R.id.zoomExtButton);
        zoomExtButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                GBUtilities.getInstance().showStatus(getActivity(),  R.string.zoom_ext_button_label);

                boolean doZoom = true;
                buildNewZoom(mZoomMarkers, doZoom);
                isAutoResizeOn = true;

            }
        });


        //FOOTER WIDGETS


        //Footer fields with status and quality info about GPS source
        TextView currentProjectField  = (TextView)v.findViewById(R.id.currentProjectField);

        TextView modelField           = (TextView)v.findViewById(R.id.modelField);
        TextView snField              = (TextView)v.findViewById(R.id.snField);
        TextView hdopField            = (TextView)v.findViewById(R.id.hdop_field);
        TextView vdopField            = (TextView)v.findViewById(R.id.vdop_field);
        TextView pdopField            = (TextView)v.findViewById(R.id.pdop_field);
        TextView tdopField            = (TextView)v.findViewById(R.id.tdop_field);
        TextView hrmsField            = (TextView)v.findViewById(R.id.hrms_field);
        TextView vrmsField            = (TextView)v.findViewById(R.id.vrms_field);


        //  Esc and Enter buttons are enabled on the collect screen

        //Esc Button
        Button escButton = (Button) v.findViewById(R.id.escButton);
        //have to set the color and enable the button as the default is NOT enabled/grayed out
        escButton.setEnabled(true);
        escButton.setTextColor(Color.BLACK);
        escButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isMeanInProgress()){
                    mMeanToken.setMeanInProgress(false);
                    handleCancelMeaning();
                    GBUtilities.getInstance().showStatus(getActivity(), R.string.meaning_terminated);
                } else {
                    GBUtilities.getInstance().showStatus(getActivity(), R.string.exit_without_save);
                    //Switch the fragment to the previous fragment.
                    // But the switching happens on the container Activity
                    GBActivity myActivity = (GBActivity) getActivity();
                    if (myActivity != null) {
                        myActivity.switchToPopBackstack();
                    }
                }

            }
        });

        //Enter Button
        Button enterButton = (Button) v.findViewById(R.id.enterButton);
        //have to set the color and enable the button as the default is NOT enabled/grayed out
        enterButton.setEnabled(true);
        enterButton.setTextColor(Color.BLACK);
        enterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
            //Let user know data was saved
                GBUtilities.getInstance().showStatus(getActivity(),  R.string.save_contents);

                //This knows whether we are in the middle of meaning or just use current position
                GBCoordinateMean meanCoordinate =  null;
                if (isMeanInProgress()){
                    // TODO: 1/21/2017 Are we using the last point twice in the mean???
                    //calculate the mean using the coordinates in the list
                    meanCoordinate = mMeanToken.getMeanCoordinate();
                }
                setFocus(mNmeaData);

                handleStorePosition(mNmeaData, meanCoordinate);

                //Switch the fragment to the previous fragment.
                // But the switching happens on the container Activity
                GBActivity myActivity = (GBActivity) getActivity();
                if (myActivity != null){
                    myActivity.switchToPopBackstack();
                }

            }
        });


    }

    /* **********************************************************/
    /* ****      Utilities and Call backs for pictures     ******/
    /* **********************************************************/
    //The place where intents return to this fragment
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_IMAGE_CAPTURE) {
            if (resultCode == Activity.RESULT_OK) {
                //Ths picture has been successfully taken
                //record the picture in on the point or the project


                //String temp = mCurrentPhotoPath;
                //Add the picture to the gallery so the user can see it there
                galleryAddPic();

                GBProject openProject = GBUtilities.getInstance().getOpenProject((GBActivity)getActivity());

                //create the picture object, The null is for a point ID
                GBPicture picture = new GBPicture(mCurrentPhotoTimestamp,
                        openProject,
                        null);

                picture.setPathName(mCurrentPhotoPath);
                picture.setFileName(mCurrentPhotoFileName);

                GBProjectManager projectManager = GBProjectManager.getInstance();
                //The focus flags tell us where to put the picture
                if (isFocusOnPoint) {

                    GBPoint point = getPointFromFocus(openProject);
                    if (point == null){
                        tellUserNoPoint();
                        startGps();
                        return ;
                    }

                    //add the point id to the picture
                    picture.setPointID(point.getPointID());

                    //update the point in the DB

                    //No need for a cascading update, only update the picture and the point
                    if (!projectManager.addPictureToDB(picture)){
                        GBUtilities.getInstance().showStatus(getActivity(),
                                R.string.error_adding_picture_to_db);
                    } else {
                        //add the picture to the point
                        point.addPicture(picture);
                    }

                } else {

                    //No need for a cascading update of the project
                    // and ALLLLL of its subordinate objects.
                    // Just update the project and the picture in the DB
                    if (!projectManager.addPictureToDB(picture)){
                        GBUtilities.getInstance().showStatus(getActivity(),
                                R.string.error_adding_picture_to_db);
                    } else {
                        //put the picture on the open project
                        openProject.addPicture(picture);
                    }
                }
                startGps();


                //thumbnail comes back in the data intent, but need to deal with the whole picture
                /* code for displaying a thumbprint of the picture
                Bitmap bmp = (Bitmap) data.getExtras().get("data");
                ByteArrayOutputStream stream = new ByteArrayOutputStream();

                bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] byteArray = stream.toByteArray();

                // convert byte array to Bitmap

                int offset = 0;
                Bitmap bitmap = BitmapFactory.decodeByteArray(byteArray, offset, byteArray.length);

                imageView.setImageBitmap(bitmap);
                */
            }
        }
    }

     private void tellUserNoPoint(){
         GBUtilities.getInstance().showStatus(getActivity(),  R.string.point_not_created_at_marker_yet);

    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        PackageManager packageManager = getActivity().getPackageManager();

        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                GBUtilities.getInstance().showStatus(getActivity(),  R.string.unable_to_create_picture_file);
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {

                Uri photoURI = URI.fromFile(photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_CODE_IMAGE_CAPTURE);
            } else {
                // Error occurred while creating the File
                GBUtilities.getInstance().showStatus(getActivity(), R.string.unable_to_create_picture_file);

            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        // TODO: 6/17/2017 do timestamp format through a Utilities method. Add in Locale considerations
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

        GBProject project = GBUtilities.getInstance().getOpenProject((GBActivity)getActivity());
        String imageFileName = project.getProjectName() + "_" + timeStamp + "_";
        /*
        //getExternalStoragePublicDirectory() //accessible to all apps and the user
        //with the DIRECTORY_PICTURES argument
        //requires manifest permission
        //<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
        //
        // if use: getExternalFilesDir()
        // files are deleted when app uninstalled
        //requires manifest permission:
        //<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"
        //                               android:maxSdkVersion="18" />
        */

        //Assure the path exists
        File projectDirectory = new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                //getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                project.getProjectName().toString());

        //check if the project subdirectory already exists
        if (!projectDirectory.exists()){
            //if not, create it
            if (!projectDirectory.mkdirs()){
                //Unable to create the file
                return null;
            }
        }

        File imageFile = File.createTempFile(imageFileName,  /* prefix */
                                         ".jpg",             /* suffix */
                                         projectDirectory    /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoFileName = imageFileName + " ";//won't store in DB without a space at the end
        mCurrentPhotoPath = imageFile.getAbsolutePath();
        mCurrentPhotoTimestamp = timeStamp;
        return imageFile;
    }


    //Make the photo available through Gallery
    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        getActivity().sendBroadcast(mediaScanIntent);
    }


    /* **********************************************************/
    /* **** Handlers for Buttons on Collect Points screen  ******/
    /* **********************************************************/
    private void handleCurrentPosition(){
        isAutoResizeOn = true;
        //ignore the event if meaning is in process
        if (isMeanInProgress()) return;

        //set screen focus to this location
        setFocus(mNmeaData);
        //This sets PointID as a side effect
        getMarkerByFocus();

        //update screen with the nmea data
        updateUIwNmeaPosition(mNmeaData);
    }

    private void handleAveragePosition(){
        if (mNmeaData == null) {
            GBUtilities.getInstance().showStatus(getActivity(),  R.string.gps_not_running);
            return;
        }
        //set screen focus to this location
        setFocus(mNmeaData);

        mMeanToken = initializeMeanToken(mMeanToken);


        //Actual meaning and creation of marker will happen on the next NMEA interrupt handler
        //when it notices the mean flag is set true
    }

    private GBMeanToken initializeMeanToken(GBMeanToken meanToken){
        if (meanToken == null) {
            meanToken = new GBMeanToken();
        }
        long openProjectID = GBUtilities.getInstance().getOpenProjectID((GBActivity)getActivity());
        meanToken.setProjectID(openProjectID);
        //start the meaning process by setting the proper flags
        meanToken.setMeanInProgress  (true);
        meanToken.setFirstPointInMean(true);
        meanToken.setLastPointInMean (false);
        meanToken.setCurrentMeanTime (0d);
        meanToken.setRawReadings     (0);
        meanToken.setFixedReadings   (0);
        meanToken.resetCoordinates   ();
        meanToken.resetMeanCoordinate();

        return meanToken;
    }

    private boolean isMeanInProgress(){
        return ((mMeanToken != null) && mMeanToken.isMeanInProgress());
    }
    private boolean isMeanStopped(){return !isMeanInProgress();}

    private void handleCancelMeaning(){
        mMeanToken = initializeMeanToken(mMeanToken);


        mMarkers.remove(mLastMarkerAdded);
        mLastMarkerAdded.remove();
    }



    //handler is smart enough to know whether mean or button press
    //This is the only place where a point and it's coordinate is actually created
    //If we are storing a meaned position, the meanCoordinate will be non-null
    private void handleStorePosition(GBNmea nmeaData,
                                     GBCoordinateMean meanCoordinate){

        if (nmeaData == null) return;
        setFocus(nmeaData);
        if ((mLatitudeScreenFocus == 0d) || (mLongitudeScreenFocus == 0d)) return;


        //knows whether we have to stop the mean, or use current position

        //step 1 create new point
        //step 2 create the WSG coordinate
        //step 3 determine which kind of coordinate, create it and add to the point:
        //Step 4 add the point to the project list
        //step 5 change the final location and color of the marker
        //step 6 draw lines and do zooming or panning necessary
        //step 7 change the info window
        //step 8 change screen focus to this location
        //step 9 change the flag saying the meaning is over
        //step 10 reset variables in preparation for next meaning process

        //step 1 create point from the open project
        //get the open project
        GBProject project = GBUtilities.getInstance().getOpenProject((GBActivity)getActivity());
        GBPoint point     = createPoint(project);

        //Update the UI with the point id
        updateUIwPointID(point.getPointID());

        //update the meaning coordinate with the point id
        if (meanCoordinate == null){
            //if we aren't coming from completing the mean, the passed coordinate will be null
            //however, we still need one for the marker info window
            meanCoordinate = new GBCoordinateMean(nmeaData);
        }
        meanCoordinate.setPointID(point.getPointID());


        //step 2 create the WSG coordinate
        GBCoordinateWGS84 wgs84Coordinate;
        //starts with the WSG84 coordinate, regardless of the final type
        //valid coordinate is set automatically based on latitude/longitude
        if (isMeanStopped()) {
            //have to create the meanCoordinate from current position
            wgs84Coordinate =  new GBCoordinateWGS84((GBActivity)getActivity(), nmeaData);
        } else {
            //creates coordinate from the meaned information
            wgs84Coordinate = new GBCoordinateWGS84((GBActivity)getActivity(), meanCoordinate);
        }

        //step 2.1 Add the offset position to the location

        if ((mOffsetDistance  != 0) ||
            (mOffsetHeading   != 0) ||
            (mOffsetElevation != 0)) {



            //record the offset itself
            point.setOffsetDistance (mOffsetDistance);
            point.setOffsetHeading  (mOffsetHeading);
            point.setOffsetElevation(mOffsetElevation);

            wgs84Coordinate = (GBCoordinateWGS84)point.getCoordinateWithOffsets((GBActivity)getActivity());

            //then reset the offsets if it was for a single point
            if (mOffsetCheckedID == (OFFSET_NEXT_ONLY + OFFSET_ID_CONSTANT)) {
                //reset the offset constants as they have now been applied to a point
                mOffsetDistance  = 0;
                mOffsetHeading   = 0;
                mOffsetElevation = 0;
            }
        }

        //Step 2.2 Alter Elevation if necessary with Height
        String heightString = getHeightFromUI();
        if (!GBUtilities.isEmpty(heightString)) {
            // TODO: 6/18/2017 need to pay attention to either Locale or project settings here
            double height = Double.valueOf(heightString);

            if (height != 0d) {
                double elevation = wgs84Coordinate.getElevation();
                elevation = elevation - height;
                wgs84Coordinate.setElevation(elevation);
            }
        }

        //step 3 determine which kind of coordinate, create it, add to point:
        // This method updates the DB version of the coordinate as well
        createCoordinateFromWSG(project, point, wgs84Coordinate);


        //Step 4 add the point to the project
        //need to explicitly add the coordinate and the meanToken to the point,
        // as the manager can't do that as a cascade
        GBPointManager pointManager = GBPointManager.getInstance();
        boolean addToDBToo = true;
        if (!pointManager.addPointToProject(project, point, addToDBToo)){
            //The DB add failed, recover
            GBUtilities.getInstance().showStatus(getActivity(),  getString(R.string.error_adding_point));

            //remove the point from the project
            if (!project.removePoint(point)){
                GBUtilities.getInstance().showStatus(getActivity(),  getString(R.string.error_removing_point));
            }

            //get rid of marker
            if (mLastMarkerAdded != null){
                mLastMarkerAdded.remove();
            }
        } else {

            //Step 4a, update the mean token as well on the point
            point.setMeanToken(mMeanToken);
        }


        //step 5 change the final location and color of the marker
        LatLng currentPosition = new LatLng(wgs84Coordinate.getLatitude(),
                                            wgs84Coordinate.getLongitude());
        if (mLastMarkerAdded == null){
            //No marker, so create one
            String markerName = String.format(Locale.getDefault(),
                                                getString(R.string.point_id_fillin),
                                                point.getPointID());

            //mLastMarkerAdded is updated as a side effect
            //it actually returns a LatLng
            mLastMarkerAdded = makeNewMarker(currentPosition, markerName, markerColorFinal);
        } else {
            //change the position of the marker to the designated position
            mLastMarkerAdded.setPosition(currentPosition);
            mLastMarkerAdded.setIcon(BitmapDescriptorFactory.defaultMarker(markerColorFinal));
        }


        //step 6 draw any lines and do any zooming or panning necessary
        addPointToLine(currentPosition);


        //step 7 change the info window
        mLastMarkerAdded.setTag(meanCoordinate);
        mLastMarkerAdded.showInfoWindow();


        //step 8 Change screen focus to this location
        setFocus(meanCoordinate.getLatitude(), meanCoordinate.getLongitude());



        //step 9 change the flags to indicating the meaning is over
        initializeMeanToken(mMeanToken);

        //step 10 reset variables in preparation for next meaning process
        mLastMarkerAdded    = null;

    }

    private void handleMapTouch(LatLng latLng){
        if (isMeanInProgress()) return;
        setFocus(latLng);
        getMarkerByFocus();//It sets the PointID as a side effect


        double latitude = latLng.latitude;

        double longitude = latLng.longitude;

        GBActivity myActivity = (GBActivity)getActivity();
        int locPrecision = GBGeneralSettings.getLocPrecision(myActivity);


        String msg = getString(R.string.map_touched) +
            " " + getString(R.string.latitude_label)+ " " +
            GBUtilities.truncatePrecisionString(latitude, locPrecision) +
            ", " + getString(R.string.longitude_label)+ " " +
            GBUtilities.truncatePrecisionString(longitude,locPrecision) ;

        GBUtilities.getInstance().showStatus(getActivity(), msg);

        updateScaleFactor();

        isAutoResizeOn = false;
    }


    private void handleNotes(){
        if (!determineFocus())return;

        if (isFocusOnPoint){
            GBPoint point = getPointFromFocus(GBUtilities.getInstance().
                                                    getOpenProject((GBActivity)getActivity()));
            if (point != null) {
                mNotes = point.getPointNotes();
            }
        } else {
            mNotes = GBUtilities.getInstance().getOpenProject((GBActivity)getActivity()).
                                                    getProjectDescription();
        }

        askNotesText();
    }


    //handler is smart enough to know whether mean or button press
    private void handleShowAllPoints(){

        //Step 0 clear any old markers and reset variables
        //step 1 Get the open project
        //step 2 For each point on the project
        //step 3 Create a Marker
        //Step 4 Create a tag
        //step 5 Add to poly line, draw lines and do zooming or panning necessary

        //step 7 change screen focus to last point's location


        mLineOptions = null; //clear any old lines
        mZoomBounds    = null;
        mZoomBuilder   = null;

        isAutoResizeOn = true;
        //clear the map of old markers
        mMap.clear();
        mMarkers.clear();
        mZoomMarkers.clear();

        //step 1 Get the open project
        //get the open project
        GBProject project = GBUtilities.getInstance().getOpenProject((GBActivity)getActivity());

        ArrayList<GBPoint> points = project.getPoints();
        int last = points.size();

        //step 2 For each point on the project
        int          position;
        GBPoint point = null;

        LatLng       markerLocation = null;
        GBCoordinateWGS84 coordinateWGS84;
        GBCoordinateMean coordinateTag;
        for (position = 0; position < last; position++){
            point = points.get(position);

            //step 3 Create a Marker
            coordinateWGS84 = getCoordinateFromPoint(project, point);
            LatLng mapLocation = new LatLng(coordinateWGS84.getLatitude(), coordinateWGS84.getLongitude());

            String markerName = String.valueOf(point.getPointID());

            mLastMarkerAdded = makeNewMarker(mapLocation, markerName, markerColorFinal);

            //Step 4 Create a tag
            coordinateTag = new GBCoordinateMean(coordinateWGS84);
            coordinateTag.setPointID(point.getPointID());
            mLastMarkerAdded.setTag(coordinateTag);



            //step 5 Add to poly line, draw lines and do zooming or panning necessary
            markerLocation = new LatLng(coordinateWGS84.getLatitude(),
                                        coordinateWGS84.getLongitude());
            addPointToLine(markerLocation);

        }
        //step 8 change screen focus to last point's location
        if (markerLocation != null) {
            setFocus(markerLocation);
        }

        //update the current position fields to the last point
        View v = getView();
        if ((point != null) && (v != null)){
            try {
                TextView currentPointIDField     = (TextView) v.findViewById(R.id.pointIDField);
                TextView currentHeightField      = (EditText) v.findViewById(R.id.currentHeightField);
                TextView currentFeatureCodeField = (EditText) v.findViewById(R.id.currentFeatureCodeField);

                currentPointIDField    .setText(String.valueOf(point.getPointID()));
                currentFeatureCodeField.setText(point.getPointFeatureCode());
                currentHeightField     .setText(String.valueOf(point.getHeight()));
            } catch (NullPointerException e){
                GBUtilities.getInstance().showStatus(getActivity(),  R.string.unable_to_update_ui);

            }
        }

    }


    private void handleTakePicture(){

        //focus must be valid to take a picture
        if (!determineFocus()) return;


        PackageManager packageManager = getActivity().getPackageManager();
        boolean hasCamera =
                packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY);

        //In order to take pictures, we must both have a camera and
        // Permission to take pictures
        if (!hasCamera) {
            GBUtilities.getInstance().showStatus(getActivity(),  R.string.no_camera_on_this_device);
        } else if (
                ContextCompat.checkSelfPermission(getActivity(),
                        Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            GBUtilities.getInstance().showStatus(getActivity(), R.string.need_permission_to_take_pictures);

        } else {

            //Send off an intent to the camera
            dispatchTakePictureIntent();
        }
    }



    /* **********************************************************/
    /* ****    Utilities to communicate with UI            ******/
    /* **********************************************************/
    private void updateUIwPointID(long pointID){
        View v = getView();
        if (v != null) {
            TextView currentPointIDField = (TextView) v.findViewById(R.id.pointIDField);
            currentPointIDField.setText(String.valueOf(pointID));
        }
    }

    private String getHeightFromUI(){

        View v = getView();
        if (v == null)return "";

        TextView currentHeightField = (EditText)v.findViewById(R.id.currentHeightField);
        return currentHeightField.getText().toString();

    }




   /* **********************************************************/
    /* ****     AlertDialog Utilities                      ******/
    /* **********************************************************/

    private void askNotesText(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.enter_notes));
        builder.setIcon(R.drawable.ground_station_icon);

        // Set up the input
        final EditText input = new EditText(getActivity());
        input.setText(String.valueOf(mNotes));
        // Specify the type of input expected
        input.setInputType(InputType.TYPE_CLASS_TEXT );
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mNotes = input.getText().toString();
                storeNotesOnPointOrProject(mNotes);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }


    private void askOffsetPosition(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.enter_offsets));
        builder.setIcon(R.drawable.ground_station_icon);

        LinearLayout layout = new LinearLayout(getActivity());
        layout.setOrientation(LinearLayout.VERTICAL);

        // Set up the input

        //Radio button for which points to apply to: next/all future buttons
        final RadioButton[] radioButtons = new RadioButton[3];
        RadioGroup radioGroup = new RadioGroup(getActivity()); //create the RadioGroup
        radioGroup.setOrientation(RadioGroup.HORIZONTAL);//or RadioGroup.VERTICAL

        radioButtons[OFFSET_NEXT_ONLY] = new RadioButton(getActivity());
        radioButtons[OFFSET_NEXT_ONLY].setId(OFFSET_NEXT_ONLY + OFFSET_ID_CONSTANT);
        radioGroup.addView(radioButtons[OFFSET_NEXT_ONLY]);
        radioButtons[OFFSET_NEXT_ONLY].setText(getString(R.string.next_point));
        radioButtons[OFFSET_NEXT_ONLY].setSelected(true);
        mOffsetCheckedID = OFFSET_NEXT_ONLY + OFFSET_ID_CONSTANT;

        radioButtons[OFFSET_ALL_FUTURE] = new RadioButton(getActivity());
        radioButtons[OFFSET_ALL_FUTURE].setId(OFFSET_ALL_FUTURE + OFFSET_ID_CONSTANT);
        radioGroup.addView(radioButtons[OFFSET_ALL_FUTURE]);
        radioButtons[OFFSET_ALL_FUTURE].setText(getString(R.string.all_future_points));

        radioButtons[OFFSET_RESET] = new RadioButton(getActivity());
        radioButtons[OFFSET_RESET].setId(OFFSET_RESET + OFFSET_ID_CONSTANT);
        radioGroup.addView(radioButtons[OFFSET_RESET]);
        radioButtons[OFFSET_RESET].setText(getString(R.string.reset_offset));

        layout.addView(radioGroup);



        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                mOffsetCheckedID = radioGroup.getCheckedRadioButtonId();
            }
        });

        //Distance
        LinearLayout layoutDistance = new LinearLayout(getActivity());
        layoutDistance.setOrientation(LinearLayout.HORIZONTAL);

        final TextView distanceLabel = new TextView(getActivity());
        distanceLabel.setText(R.string.offset_distance_meters);


        final EditText distance = new EditText(getActivity());
        distance.setInputType(InputType.TYPE_CLASS_NUMBER |
                              InputType.TYPE_NUMBER_FLAG_DECIMAL |
                              InputType.TYPE_NUMBER_FLAG_SIGNED);
        distance.setHint(R.string.offset_distance_meters);
        layoutDistance.addView(distanceLabel);
        layoutDistance.addView(distance);
        layout.addView(layoutDistance);

        //Heading
        LinearLayout layoutHeading = new LinearLayout(getActivity());
        layoutHeading.setOrientation(LinearLayout.HORIZONTAL);

        final TextView headingLabel = new TextView(getActivity());
        headingLabel.setText(R.string.point_offset_heading_label);

        final EditText heading = new EditText(getActivity());
        heading.setInputType(InputType.TYPE_CLASS_NUMBER |
                               InputType.TYPE_NUMBER_FLAG_DECIMAL |
                               InputType.TYPE_NUMBER_FLAG_SIGNED);
        heading.setHint(R.string.offset_heading);

        layoutHeading.addView(headingLabel);
        layoutHeading.addView(heading);
        layout.addView(layoutHeading);

        //elevation
        LinearLayout layoutElevation = new LinearLayout(getActivity());
        layoutElevation.setOrientation(LinearLayout.HORIZONTAL);

        final TextView elevationLabel = new TextView(getActivity());
        elevationLabel.setText(R.string.point_offset_elevation_label);

        final EditText elevation = new EditText(getActivity());
        elevation.setInputType(InputType.TYPE_CLASS_NUMBER |
                               InputType.TYPE_NUMBER_FLAG_DECIMAL |
                               InputType.TYPE_NUMBER_FLAG_SIGNED);
        elevation.setHint(R.string.offset_elevation);

        layoutElevation.addView(elevationLabel);
        layoutElevation.addView(elevation);
        layout.addView(layoutElevation);

        builder.setView(layout);

        distance.setText(String.valueOf(mOffsetDistance));
        heading.setText(String.valueOf(mOffsetHeading));
        elevation.setText(String.valueOf(mOffsetElevation));


        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                if (radioButtons[OFFSET_RESET].isChecked()){
                    //reset all offsets to zero
                    mOffsetDistance  = 0d;
                    mOffsetHeading   = 0d;
                    mOffsetElevation = 0d;
                } else {
                    String distanceValue = distance.getText().toString();
                    String headingValue = heading.getText().toString();
                    String elevationValue = elevation.getText().toString();
                    if (distanceValue.isEmpty()) {
                        mOffsetDistance = 0;
                    } else {
                        mOffsetDistance = Double.valueOf(distanceValue);
                    }
                    if (headingValue.isEmpty()) {
                        mOffsetHeading = 0;
                    } else {
                        mOffsetHeading = Double.valueOf(headingValue);
                    }
                    if (elevationValue.isEmpty()) {
                        mOffsetElevation = 0;
                    } else {
                        mOffsetElevation = Double.valueOf(elevationValue);
                    }
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.cancel();
            }
        });

        builder.show();
    }


    private void askMarker(GBPoint point, final Marker marker){
        // TODO: 12/25/2016 There must be a better way to pass arguments to the handler routines
        final GBPoint   openPoint         = point;
        final Marker    mapMarker         = marker;

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("GB Collect Points Marker Options");
        builder.setIcon(R.drawable.ground_station_icon);
        builder.setItems(new CharSequence[] {
                        getString(R.string.marker_edit_point),
                        getString(R.string.marker_remove_point),
                        getString(R.string.marker_remove_marker),
                        getString(R.string.marker_exclude_point),
                        getString(R.string.marker_set_focus),
                        getString(R.string.marker_show_info),
                        getString(R.string.marker_hide_info) },
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // The 'which' argument contains the index position
                        // of the selected item
                        switch (which) {
                            case 0:
                                GBUtilities.getInstance().showStatus(getActivity(), getString(R.string.marker_edit_point));
                                //Switch to the Edit Point Fragment
                                GBPath pointPath  = new GBPath(GBPath.sEditFromMaps);
                                ((GBActivity)getActivity()).switchToPointEditScreen(pointPath,
                                                                                    openPoint);
                                break;
                            case 1:
                                GBUtilities.getInstance().showStatus(getActivity(), getString(R.string.marker_remove_point));
                                //Delete the point from the project and from the DB
                                GBPointManager pointManager = GBPointManager.getInstance();
                                pointManager.removePoint(
                                        GBUtilities.getInstance().
                                        getOpenProjectID((GBActivity)getActivity()), openPoint);

                                //remove the marker
                                mapMarker.remove();
                                // TODO: 1/8/2017 redraw the map after point/marker is removed?
                                //redraw the map?
                                break;
                            case 2:
                                GBUtilities.getInstance().showStatus(getActivity(),  getString(R.string.marker_remove_marker));
                                mapMarker.remove();

                                break;
                            case 3:
                                GBUtilities.getInstance().showStatus(getActivity(), getString(R.string.marker_exclude_point));
                                mZoomMarkers.remove(mapMarker);
                                break;
                            case 4:
                                GBUtilities.getInstance().showStatus(getActivity(),  getString(R.string.marker_set_focus));
                                LatLng mapPosition = mapMarker.getPosition();
                                setFocus(mapPosition);
                                mFocusMarker = getMarkerByFocus();

                                break;
                            case 5:
                                GBUtilities.getInstance().showStatus(getActivity(), getString(R.string.marker_show_info));
                                dialog.cancel();
                                break;
                            case 6:
                                GBUtilities.getInstance().showStatus(getActivity(), R.string.marker_hide_info);
                                mapMarker.hideInfoWindow();
                                break;
                        }
                    }
                });
        builder.create().show();
    }


    private void storeNotesOnPointOrProject(CharSequence notes){

        GBProject openProject = GBUtilities.getInstance().getOpenProject((GBActivity)getActivity());

        //The focus flags tell us where to put the notes
        if (isFocusOnPoint) {
            //put the notes on the point of the marker with current focus
            GBCoordinateMean coordinateTag = (GBCoordinateMean) mFocusMarker.getTag();
            //The following check was done when the picture was started
            //but it never hurts to check again
            if (coordinateTag == null){
                tellUserNoPoint();
                startGps();
                return;
            }
            long pointID = coordinateTag.getPointID();
            if (pointID < 1) {
                tellUserNoPoint();
                startGps();
                return;
            }
            //find the point object the marker stands for
            GBPoint point = openProject.getPoint(coordinateTag.getPointID());

            if (point == null){
                tellUserNoPoint();
                startGps();
                return;
            }

            //add the notes to the point

            point.setPointNotes(notes);

            //update the point in the DB
            GBPointManager pointManager = GBPointManager.getInstance();

            //No need for a cascading update, only update the picture and the point
            //this routine does not update coordinate or meanToken
            boolean addToDB = true;
            pointManager.addPointToProject(openProject, point, addToDB);


        } else {
            //put the notes on the open project
            openProject.setProjectDescription(mNotes);
            GBProjectManager projectManager = GBProjectManager.getInstance();

            //No need for a cascading update of the project
            // and ALLLLL of its subordinate objects.
            // Just update the project and the picture in the DB
            projectManager.updateSingleProjectInDB(openProject);
        }
        startGps();


    }

    //* *********************************************************/
    //* ***      Point and Coordinate Utilities            ******/
    //* *********************************************************/

    //This routine creates the point object, but not it's coordinate
    private GBPoint createPoint(GBProject project){
        View v = getView();
        //create the point
        GBPoint point = new GBPoint();
        initializePoint(project, point);
        //ID will be assigned when the point is first saved to the DB
        point.setPointID(GBUtilities.ID_DOES_NOT_EXIST);
        point.setPointNotes(mNotes);
        if (v != null) {
            TextView currentFeatureCodeField = (EditText) v.findViewById(R.id.currentFeatureCodeField);
            point.setPointFeatureCode(currentFeatureCodeField.getText());
        }

        //update the quality fields
        point.setHdop(mPointQualityToken.getHdop());
        point.setVdop(mPointQualityToken.getVdop());
        point.setTdop(mPointQualityToken.getTdop());
        point.setPdop(mPointQualityToken.getPdop());
        point.setHrms(mPointQualityToken.getHrms());
        point.setVrms(mPointQualityToken.getVrms());

        return point;

    }

    private void initializePoint(GBProject project, GBPoint point){
        point.setForProjectID(project.getProjectID());
        point.setHeight(project.getHeight());
        point.setPointNumber(project.getNextPointNumber((GBActivity)getActivity()));
        //the point number is not incremented until the point is saved for the first time
        //The SQL Helper is in charge of assigning both
        // the DB ID and then incrementing the point number
//project.incrementPointNumber((GBActivity)getActivity());
        return;
    }


    //create coordinate and add to the point
    private void createCoordinateFromWSG(GBProject project,
                                         GBPoint   point,
                                         GBCoordinateWGS84 wsg84Coordinate){

        CharSequence coordinateType = project.getProjectCoordinateType();

        if (coordinateType.equals(GBCoordinate.sCoordinateTypeWGS84)){
            //put the point ID on the coordinate
            wsg84Coordinate.setPointID(point.getPointID());

            //put the new coordinate on the point
            point.setCoordinate(wsg84Coordinate);
        } else if (coordinateType.equals(GBCoordinate.sCoordinateTypeNAD83)){
            GBCoordinateNAD83 newNad83Coordinate = new GBCoordinateNAD83(wsg84Coordinate);
            newNad83Coordinate.setPointID(point.getPointID());
            point.setCoordinate(newNad83Coordinate);
        } else if (coordinateType.equals(GBCoordinate.sCoordinateTypeUTM)){
            GBCoordinateUTM newUtmCoordinate = new GBCoordinateUTM(wsg84Coordinate);
            newUtmCoordinate.setPointID(point.getPointID());
            point.setCoordinate(newUtmCoordinate);
        } else if (coordinateType.equals(GBCoordinate.sCoordinateTypeSPCS)){
            // TODO: 6/20/2017 need zone for creation of spcs coordinate
            /*
            GBCoordinateSPCS newSpcsCoordinate = new GBCoordinateSPCS(wsg84Coordinate);
            newSpcsCoordinate.setPointID(point.getPointID());
            point.setCoordinate(newSpcsCoordinate);

            */
        } else {
            throw new RuntimeException("Something wrong with Coordinate type in Collect Points");
        }
    }



    private GBCoordinateWGS84 getCoordinateFromPoint(GBProject project, GBPoint point){

        GBCoordinateWGS84 coordinateWGS84;
        CharSequence coordinateType = project.getProjectCoordinateType();

        if (coordinateType.equals(GBCoordinate.sCoordinateTypeWGS84)){
            coordinateWGS84 = (GBCoordinateWGS84) point.getCoordinate();

        } else if (coordinateType.equals(GBCoordinate.sCoordinateTypeNAD83)){
            GBCoordinateNAD83 coordinateNAD83 = (GBCoordinateNAD83) point.getCoordinate();
            coordinateWGS84 = new GBCoordinateWGS84(coordinateNAD83);

        } else if (coordinateType.equals(GBCoordinate.sCoordinateTypeUTM)){
            GBCoordinateUTM coordinateUTM = (GBCoordinateUTM) point.getCoordinate();
            coordinateWGS84 = new GBCoordinateWGS84(coordinateUTM);

        } else if (coordinateType.equals(GBCoordinate.sCoordinateTypeSPCS)){
            GBCoordinateSPCS coordinateSPCS = (GBCoordinateSPCS) point.getCoordinate();
            coordinateWGS84 = new GBCoordinateWGS84(coordinateSPCS);
        } else {
            throw new RuntimeException("Something wrong with Coordinate type in Collect Points");
        }


        return coordinateWGS84;
    }





    private GBNmea getNmeaFromTestData(){

        //Step 1 - Get Test data object
        GBTestLocationDataManager testLocationDataManager =
                                                        GBTestLocationDataManager.getInstance();
        mTestData = testLocationDataManager.get(mTestDataCounter);
        mTestDataCounter++;
        if (mTestDataCounter >= mTestDataMax)mTestDataCounter = 0;

        //Convert the Test Data Point to GPS digital degrees
        if (mTestData == null) {
            startGps();
            GBUtilities.getInstance().showStatus(getActivity(),  R.string.test_data_invalid);
            throw new RuntimeException(getString(R.string.test_data_invalid));

        }

        //Step 2 - Convert DMS to DD
        //create a coordinate as the easiest way to convert DMS to DD
        GBCoordinateWGS84 wgsCoordinate = new GBCoordinateWGS84 (
                mTestData.getLatitudeDegrees(),  mTestData.getLatitudeMinutes(),  mTestData.getLatitudeSeconds(),
                mTestData.getLongitudeDegrees(), mTestData.getLongitudeMinutes(), mTestData.getLongitudeSeconds());

        wgsCoordinate.setElevation(mTestData.getElevation());
        wgsCoordinate.setGeoid    (mTestData.getGeoid());
        if (!wgsCoordinate.isValidCoordinate()) {
            startGps();
            GBUtilities.getInstance().showStatus(getActivity(),  R.string.test_data_invalid);
            throw new RuntimeException(getString(R.string.test_data_invalid));
        }

        //Step 2 - Create NMEA data object
        //update the screen with the location
        //build a nmeaData with the TestData location
        GBNmea nmeaData = new GBNmea();
        //nmeaData.setNmeaType("GGA");//set to GPGGA when initialized
        nmeaData.setLatitude (wgsCoordinate.getLatitude());
        nmeaData.setLongitude(wgsCoordinate.getLongitude());
        nmeaData.setOrthometricElevation(wgsCoordinate.getElevation());
        nmeaData.setGeoid    (wgsCoordinate.getGeoid());
        nmeaData.setSatellites(7);

        return nmeaData;
    }


    //* ****************************************************/
    //* ********      Map Utilities          ***************/
    //* ****************************************************/

    private void zoomToFit(){
        int markerPadding = GBGlobalSettings.getInstance().getMarkerPadding();
        int forceZoomAfter = GBGlobalSettings.getInstance().getForceZoomAfter();

        //only zoom after the first couple of points have been plotted
        if (mMarkers.size() > forceZoomAfter) {
            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(mZoomBounds, markerPadding);
            updateCamera(cu);
        }

    }

    private void updateScaleFactor(){
        View v = getView();
        if (v == null)return;

        int digitsOfPrecision = 2;
        String sfString = GBUtilities.truncatePrecisionString(getScaleFactorMetersPerInch(), digitsOfPrecision);

        TextView scaleFactorField = (TextView)v.findViewById(R.id.scaleFactorField) ;
        String msg = getString(R.string.scale_factor_msg, sfString);

        scaleFactorField.setText(msg);
    }

    private double getScaleFactorMetersPerInch(){
        //DisplayMetrics dm = getActivity().getResources().getDisplayMetrics();
        //dm.density;

        LatLng center      = mMap.getCameraPosition().target;
        double latitude    = center.latitude;
        float  zoomLevel   = mMap.getCameraPosition().zoom;
        double scaleFactor = GBUtilities.getMetersPerScreenInch(latitude, zoomLevel);

        return scaleFactor;
    }


    private void centerMap(GBNmea nmeaData){
        //update the maps
        LatLng newPoint = new LatLng(nmeaData.getLatitude(), nmeaData.getLongitude());

        CameraUpdate myZoom = CameraUpdateFactory.newLatLngZoom(newPoint, 15);
        updateCamera(myZoom);

    }

    private void updateCamera(CameraUpdate cu){
        mMap.animateCamera(cu);
        updateScaleFactor();
    }

    private Marker makeNewMarker(LatLng newPoint, String markerName, float markerColor){

        //update the maps
        MarkerOptions newPointMarkerOptions =
                    new MarkerOptions().position(newPoint)
                                       .title(markerName)
                                       .draggable(false)
                                       .icon(BitmapDescriptorFactory.defaultMarker(markerColor));
        Marker newMarker = mMap.addMarker(newPointMarkerOptions);
        mMarkers.add(newMarker);
        mZoomMarkers.add(newMarker);

        //get zoom level from the mpa
        float mapZoom = mMap.getCameraPosition().zoom;

        CameraUpdate myZoom = CameraUpdateFactory.newLatLngZoom(newPoint, mapZoom);
        updateCamera(myZoom);

        return newMarker;
    }



    private double getProximity(){
        // TODO: 12/28/2016 calculate proximity based on zoom level
        // TODO: 1/8/2017 what do we do if there is more than one marker within the proximity???
        return 50d;
    }


    private void addPointToLine(LatLng newPoint){
        //add the new point to the line
        if (mLineOptions == null){
            mLineOptions = new PolylineOptions();
        }
        mLineOptions.add(newPoint).width(25).color(Color.BLUE).geodesic(true);
        mPointsLine = mMap.addPolyline(mLineOptions);
        //The way to find out how many points we've processed so far
        //mPointsLine.getPoints().size();

        //now update the zoom boundary around the set of points
        if (mZoomBuilder == null){
            mZoomBuilder = new LatLngBounds.Builder();
        }

        mZoomBuilder = mZoomBuilder.include(newPoint);
        //update the zoom to fit bounds
        mZoomBounds = mZoomBuilder.build();


        if (isAutoResizeOn) {
            zoomToFit();
        }
    }

    private void redrawMapMarkers(ArrayList<Marker> lineMarkers, boolean doMapDraw){
        //build the mLineOptions from scratch
        mLineOptions = new PolylineOptions();

        int last = lineMarkers.size();
        int position = 0;
        Marker lineMarker;
        LatLng location;
        while (position < last){
            lineMarker = lineMarkers.get(position);
            lineMarker.setVisible(true);
            location = lineMarker.getPosition();
            mLineOptions.add(location).width(25).color(Color.BLUE).geodesic(true);
            position++;
        }
        if (doMapDraw) mPointsLine = mMap.addPolyline(mLineOptions);
    }

    private void buildNewZoom(ArrayList<Marker> zoomMarkers, boolean doZoom){
        //now update the zoom boundary around the set of points
        if (mZoomBuilder == null){
            mZoomBuilder = new LatLngBounds.Builder();
        }


        int last = zoomMarkers.size();
        int position = 0;
        Marker zoomMarker;
        while (position < last){
            zoomMarker = zoomMarkers.get(position);
            mZoomBuilder = mZoomBuilder.include(zoomMarker.getPosition());
        }

        //update the zoom to fit bounds
        mZoomBounds = mZoomBuilder.build();

        if (doZoom) zoomToFit();
    }


    //* ****************************************************/
    //* ********      Focus Utilities        ***************/
    //* ****************************************************/
    private boolean determineFocus(){

        //What object the picture is to be associated with depends upon the screen focus
        if ((mLatitudeScreenFocus == 0) || (mLongitudeScreenFocus == 0)){
            GBUtilities.getInstance().showStatus(getActivity(),  R.string.invalid_screen_focus_for_picture);
            return false;
        }

        //see if screen focus is near a marker
        isFocusOnPoint = true;
        mFocusMarker = getMarkerByFocus();

        if (mFocusMarker == null){
            //we aren't near a point marker, assign the picture to the open project
            isFocusOnPoint = false;
        } else {
            //Focus is on a marker,
            // but the point must be associated with the marker before we can take a picture
            GBCoordinateMean coordinateTag = (GBCoordinateMean)mFocusMarker.getTag();
            //if point id > 1, the point has not yet been created
            if ((coordinateTag == null) || (coordinateTag.getPointID() < 1)){
                GBUtilities.getInstance().showStatus(getActivity(),  R.string.point_not_created_at_marker_yet);
                return false;
            }
            isFocusOnPoint = true;
        }

        return true;

    }

    private boolean setFocus(double latitude, double longitude) {
        LatLng latLng = new LatLng(latitude, longitude);
        return setFocus(latLng);
    }

    private boolean setFocus(GBNmea nmeaData) {
        LatLng latLng = new LatLng(nmeaData.getLatitude(), nmeaData.getLongitude());
        return setFocus(latLng);
    }

    private boolean setFocus(LatLng latLng) {
        boolean returnCode = false; //focus has not changed
        if (!(mLatitudeScreenFocus  == latLng.latitude)   ||
                !(mLongitudeScreenFocus == latLng.longitude)) {
            returnCode = true;
            mLatitudeScreenFocus  = latLng.latitude;
            mLongitudeScreenFocus = latLng.longitude;

            //only actually change the focus if the auto resize is off
            if (isAutoResizeOn) {
                //update the maps zoom focus
                CameraUpdate center = CameraUpdateFactory.newLatLng(latLng);
                updateCamera(center);
            }
        }
        return returnCode;
    }

    private Marker getMarkerByFocus(){
        View v = getView();
        if (v == null)return null;
        TextView currentPointIDField = (TextView) v.findViewById(R.id.pointIDField);

        int last = mMarkers.size();
        LatLng focusLocation = getFocusLatLng();
        double distanceToMarker;
        Marker marker;
        for (int position = 0; position < last; position++){
            marker = mMarkers.get(position);
            distanceToMarker = SphericalUtil.computeDistanceBetween(marker.getPosition(), focusLocation);

            if( distanceToMarker < getProximity() ) { // distance depends upon zoom level
                //set the point ID of the marker
                GBCoordinateMean markerTag = (GBCoordinateMean)marker.getTag();
                long pointID = markerTag.getPointID();
                if (pointID != 0){
                    currentPointIDField.setText(String.valueOf(pointID));
                }
                return marker;
            }
        }

        currentPointIDField.setText("");//There is no point nearby

        return null;
    }

    private LatLng getFocusLatLng(){
        return new LatLng(mLatitudeScreenFocus, mLongitudeScreenFocus);
    }

    private GBPoint getPointFromFocus(GBProject openProject){
        //put the picture on the point of the marker with current focus
        GBCoordinateMean coordinateTag = (GBCoordinateMean) mFocusMarker.getTag();
        //The following check was done when the picture was started
        //but it never hurts to check again
        if ((coordinateTag == null) || (coordinateTag.getPointID() < 1)) {

            return null;
        }
        //find the point object the marker stands for
        return openProject.getPoint(coordinateTag.getPointID());

    }


}


