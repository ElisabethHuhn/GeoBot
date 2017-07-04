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
import static com.asc.msigeosystems.geobot.R.id.spcScaleFactorOutput;

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
    private GBMeanToken mMeanToken;

    private GBPoint mPointBeingMaintained;


     private boolean isGpsOn            = true;


    //**********************************************************/
    //*****  DataSource types for Spinner Widgets     **********/
    //**********************************************************/
    String[] mDataSourceTypes;
    private Spinner  mSpinner;


    private int    mCurDataSource;

    //**********************************************************/
    //*****      Static Methods                       **********/
    //**********************************************************/
    public static GBCoordConvertFragment newInstance(GBPoint point) {

        Bundle args = GBPoint.putPointInArguments(new Bundle(), point);

        GBCoordConvertFragment fragment = new GBCoordConvertFragment();

        fragment.setArguments(args);
        return fragment;
    }

    //**********************************************************/
    //*****  Constructor                              **********/
    //**********************************************************/

    public GBCoordConvertFragment() {
        //for now, we don't need to initialize anything when the fragment
        //  is first created
    }


    //**********************************************************/
    //*****  Lifecycle Methods                        **********/
    //**********************************************************/

    @Override
    public void onCreate(Bundle savedInstanceState){

        super.onCreate(savedInstanceState);


        mPointBeingMaintained = GBPoint.getPointFromArguments((GBActivity)getActivity(), getArguments());
        if (mPointBeingMaintained == null){
            mPointBeingMaintained = new GBPoint();
            //Theoretically the project id might be null,
            // but you can't really get this far if the project does not exist and is not open
            mPointBeingMaintained.setForProjectID(GBUtilities.getInstance().
                    getOpenProjectID((GBActivity)getActivity()));
        }
        long projectID = mPointBeingMaintained.getForProjectID();
        if (projectID == GBUtilities.ID_DOES_NOT_EXIST){
            projectID = GBUtilities.getInstance().getOpenProjectID((GBActivity)getActivity());
            mPointBeingMaintained.setForProjectID(projectID);
        }

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

                //if we don't succeed in saving, and GPS was on, we need to turn it back on
                boolean gpsIsRunning = isGpsOn;
                if (isGpsOn) {
                    stopGps();
                }
                boolean saveCode = onSave();

                if (saveCode) {
                    ((GBActivity) getActivity()).switchToPointEditScreen(new GBPath(GBPath.sEditTag),
                            mPointBeingMaintained);
                } else {
                    if (gpsIsRunning){
                        startGps();;
                    }
                }


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
                clearForm();
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
    void onExit() {
        ((GBActivity) getActivity()).switchToPointEditScreen(new GBPath(GBPath.sEditTag),
                                                             mPointBeingMaintained);
    }


    private boolean onSave(){
        if (isGpsOn){
            GBUtilities.getInstance().showStatus(getActivity(), R.string.gps_off_before_save);
            return false;
        }
        if (isMeanInProgress()){
            GBUtilities.getInstance().showStatus(getActivity(), R.string.save_not_available);
            return false ;
        }

        long openProjectID = GBUtilities.getInstance().getOpenProjectID((GBActivity)getActivity());
        if (openProjectID == GBUtilities.ID_DOES_NOT_EXIST){
            GBUtilities.getInstance().showStatus(getActivity(), R.string.project_not_open);
            return false;
        }

        GBProject openProject = GBUtilities.getInstance().getOpenProject((GBActivity)getActivity());

        CharSequence coordinateType = openProject.getProjectCoordinateType();

        GBCoordinate coordinate = null;

        if (coordinateType.equals(GBCoordinate.sCoordinateTypeWGS84)){

            if ((mMeanToken != null) && (mMeanToken.getCoordinateSize() > 0)){
                //Use the meaned result to generate coordinate
                coordinate = new GBCoordinateWGS84((GBActivity)getActivity(),
                                                    mMeanToken.getMeanCoordinate());
            } else {
                //use the raw WGS input to generate the coordinate
                coordinate = convertWgsInputs();
            }

        } else if (coordinateType.equals(GBCoordinate.sCoordinateTypeUTM)){
            coordinate = convertUtmInputs();

        } else if (coordinateType.equals(GBCoordinate.sCoordinateTypeSPCS)){
            coordinate = convertSpcsInputs();

        }
        coordinate.setProjectID(openProjectID);

        if (coordinate.isValidCoordinate()) {
            //store the mean token
            GBMeanTokenManager tokenManager = GBMeanTokenManager.getInstance();
            long returnCode = GBUtilities.ID_DOES_NOT_EXIST + 1;
            if (mMeanToken != null) {
                returnCode = tokenManager.addMeanToDB(mMeanToken);
            }
            if (returnCode != GBUtilities.ID_DOES_NOT_EXIST) {
                //when here, know the coordinate is valid, and
                // the mean token has been successfully saved to the DB
                if (mPointBeingMaintained == null) {
                    //Create the new point and put it on the project
                    mPointBeingMaintained = new GBPoint();
                    mPointBeingMaintained.setForProjectID(openProjectID);
                }
                //now add the point to memory and db
                //Have to make sure the point is in the DB before the coordinate
                // TODO: 7/2/2017 do we really need to write point to DB twice???
                GBPointManager pointManager = GBPointManager.getInstance();
                boolean addToDBToo = true;
                if (!pointManager.addPointToProject(openProject, mPointBeingMaintained, addToDBToo)) {
                    //This will NOT do a cascade add of coordinate, meanToken
                    GBUtilities.getInstance().showStatus(getActivity(), getString(R.string.error_adding_point));
                    return false;
                }
                //The setCoordinate() and setMeanToken() routines updatae the DB as well as the local point object
                //update the coordinate with the point id
                coordinate.setPointID(mPointBeingMaintained.getPointID());
                coordinate.setProjectID(openProjectID);
                mPointBeingMaintained.setCoordinate(coordinate);
                if (mMeanToken != null) {
                    //record it in DB an on the point
                    //Writing to the DB results in the ID being assigned
                    mPointBeingMaintained.setMeanToken(mMeanToken);
                    mPointBeingMaintained.setMeanTokenID(mMeanToken.getMeanTokenID());
                }
                //If the coordinate was changed, we need to update it in the DB again.
                if (!pointManager.addPointToProject(openProject, mPointBeingMaintained, addToDBToo)) {
                    //This will NOT do a cascade add of coordinate, meanToken
                    GBUtilities.getInstance().showStatus(getActivity(), getString(R.string.error_adding_point));
                    return false;
                }


            }
        } else {
            GBUtilities.getInstance().showStatus(getActivity(), R.string.coordinate_not_valid);
            return false;
        }
        return true;

    }

     //******************************************************************//
    //            Process the received NMEA sentence                    //
    //******************************************************************//
    void handleNmeaReceived(long timestamp, String nmea) {

        try {
            //no need to process the sentence if no longer attached to the activity
            if (getActivity() == null)return;

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
                GBCoordinateMean meanCoordinate = mMeanToken.updateMean((GBActivity)getActivity(),
                                                                         mNmeaData);
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
                    // TODO: 6/27/2017 sound a tone indicating the mean is complete
                }
                coordinateWGS84 = new GBCoordinateWGS84((GBActivity)getActivity(), nmeaData);

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
        if (mMeanToken == null)mMeanToken = new GBMeanToken();
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
        //TextView TimeOutput   = (TextView) v.findViewById(R.id.gpsWgs84TimeOutput);
        TextView TimestampOutput = (TextView)v.findViewById(R.id.gpsWgs84TimestampOutput);
        //GPS Latitude
        TextView LatitudeInput   = (TextView) v.findViewById(R.id.gpsWgs84LatitudeInput);
        TextView LatDegreesInput = (TextView) v.findViewById(R.id.gpsWgs84LatDegreesInput);
        TextView LatMinutesInput = (TextView) v.findViewById(R.id.gpsWgs84LatMinutesInput);
        TextView LatSecondsInput = (TextView) v.findViewById(R.id.gpsWgs84LatSecondsInput);

        //GPS Longitude
        TextView LongitudeInput   = (TextView) v.findViewById(R.id.gpsWgs84LongitudeInput);
        TextView LongDegreesInput = (TextView) v.findViewById(R.id.gpsWgs84LongDegreesInput);
        TextView LongMinutesInput = (TextView) v.findViewById(R.id.gpsWgs84LongMinutesInput);
        TextView LongSecondsInput = (TextView) v.findViewById(R.id.gpsWgs84LongSecondsInput);

        //Elevation
        TextView ElevationMetersInput   = (TextView) v.findViewById(R.id.gpsWgs84ElevationMetersInput);
        TextView GeoidHeightMetersInput = (TextView) v.findViewById(R.id.gpsWgs84GeoidHeightMetersInput);
        TextView ElevationFeetInput     = (TextView) v.findViewById(R.id.gpsWgs84ElevationFeetInput);
        TextView GeoidHeightFeetInput   = (TextView) v.findViewById(R.id.gpsWgs84GeoidHeightFeetInput);

        //convergence & scale factor
        TextView ConvergenceAngleInput  = (TextView) v.findViewById(R.id.gpsWgs84ConvergenceInput);
        TextView CAdegInput  = (TextView) v.findViewById(R.id.gpsWgs84ConvDegreesInput);
        TextView CAminInput  = (TextView) v.findViewById(R.id.gpsWgs84ConvMinutesInput);
        TextView CAsecInput  = (TextView) v.findViewById(R.id.gpsWgs84ConvSecondsInput);

        //scale factor
        TextView ScaleFactorInput       = (TextView) v.findViewById(R.id.gpsWgs84ScaleFactor);

        String nmeaTimeString = String.format(Locale.getDefault(), "%.0f", nmeaData.getTime());
        //String wgsTimeString  = String.format(Locale.getDefault(), "%.0f", coordinateWGS84.getTime());
        //TimeOutput.setText(nmeaTimeString);
                //todo fix time between nmea and coordinate WGS84
                //setText(Double.toString(coordinateWGS84.getTime()));
                //setText(Double.toString(nmeaData.getTime()));

        //String timestampString = GBUtilities.getDateTimeString((long)nmeaData.getTimeStamp());
        String timestampString = GBUtilities.getDateTimeString(coordinateWGS84.getTime());
        TimestampOutput.setText(timestampString);

        LatitudeInput  .setText(doubleToUI(coordinateWGS84.getLatitude()));

        LongitudeInput  .setText(doubleToUI(coordinateWGS84.getLongitude()));

        ElevationMetersInput  .setText(doubleToUI(coordinateWGS84.getElevation()));
        GeoidHeightMetersInput.setText(doubleToUI(coordinateWGS84.getGeoid()));
        ElevationFeetInput.setText(doubleToUI(coordinateWGS84.getElevationFeet()));
        GeoidHeightFeetInput.setText(doubleToUI(coordinateWGS84.getGeoidFeet()));


        //input range check depends on what is being converted
        boolean isCA = false;
        boolean isLatitude = true;
        convertDDtoDMS(getActivity(),
                        LatitudeInput,
                        LatDegreesInput,
                        LatMinutesInput,
                        LatSecondsInput,
                        isCA,
                        isLatitude);

        isLatitude = false;
        convertDDtoDMS(getActivity(),
                        LongitudeInput,
                        LongDegreesInput,
                        LongMinutesInput,
                        LongSecondsInput,
                        isCA,
                        isLatitude);

        isCA = true;
        convertDDtoDMS(getActivity(),
                        ConvergenceAngleInput,
                        CAdegInput,
                        CAminInput,
                        CAsecInput,
                        isCA,
                        isLatitude);


        return true;
    }

    private boolean updateMeanUI(GBCoordinateMean meanCoordinate, GBMeanToken meanToken){


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

        //input range check depends on what is being converted
        boolean isCA = false;
        boolean isLatitude = true;
        convertDDtoDMS(getActivity(),
                        meanWgs84LatitudeInput,
                        meanWgs84LatDegreesInput,
                        meanWgs84LatMinutesInput,
                        meanWgs84LatSecondsInput,
                        isCA,
                        isLatitude);

        isLatitude = false;
        convertDDtoDMS(getActivity(),
                        meanWgs84LongitudeInput,
                        meanWgs84LongDegreesInput,
                        meanWgs84LongMinutesInput,
                        meanWgs84LongSecondsInput,
                        isCA,
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
        //TextView TimeOutput   = (TextView) v.findViewById(R.id.gpsWgs84TimeOutput);
        TextView TimestampOutput = (TextView)v.findViewById(R.id.gpsWgs84TimestampOutput);
        //GPS Latitude
        TextView LatitudeInput   = (TextView) v.findViewById(R.id.gpsWgs84LatitudeInput);
        TextView LatDegreesInput = (TextView) v.findViewById(R.id.gpsWgs84LatDegreesInput);
        TextView LatMinutesInput = (TextView) v.findViewById(R.id.gpsWgs84LatMinutesInput);
        TextView LatSecondsInput = (TextView) v.findViewById(R.id.gpsWgs84LatSecondsInput);

        //GPS Longitude
        TextView LongitudeInput   = (TextView) v.findViewById(R.id.gpsWgs84LongitudeInput);
        TextView LongDegreesInput = (TextView) v.findViewById(R.id.gpsWgs84LongDegreesInput);
        TextView LongMinutesInput = (TextView) v.findViewById(R.id.gpsWgs84LongMinutesInput);
        TextView LongSecondsInput = (TextView) v.findViewById(R.id.gpsWgs84LongSecondsInput);

        //Elevation
        TextView ElevationMetersInput   = (TextView) v.findViewById(R.id.gpsWgs84ElevationMetersInput);
        TextView GeoidHeightMetersInput = (TextView) v.findViewById(R.id.gpsWgs84GeoidHeightMetersInput);
        TextView ElevationFeetInput     = (TextView) v.findViewById(R.id.gpsWgs84ElevationFeetInput);
        TextView GeoidHeightFeetInput   = (TextView) v.findViewById(R.id.gpsWgs84GeoidHeightFeetInput);

        //convergence & scale factor
        TextView ConvergenceAngleInput  = (TextView) v.findViewById(R.id.gpsWgs84ConvergenceInput);
        TextView CAdegInput             = (TextView) v.findViewById(R.id.gpsWgs84ConvDegreesInput);
        TextView CAminInput             = (TextView) v.findViewById(R.id.gpsWgs84ConvMinutesInput);
        TextView CAsecInput             = (TextView) v.findViewById(R.id.gpsWgs84ConvSecondsInput);

        long timeStamp = coordinateWGS84.getTime();
        String timestampString = GBUtilities.getDateTimeString(timeStamp);

        String timeString = String.format(Locale.getDefault(), "%d", coordinateWGS84.getTime());
        //TimeOutput.setText(timeString);

        if (timeStamp == 0){
            TimestampOutput.setText("0");
        } else {
            TimestampOutput.setText(timestampString);
        }

        LatitudeInput  .setText(doubleToUI(coordinateWGS84.getLatitude()));

        LongitudeInput  .setText(doubleToUI(coordinateWGS84.getLongitude()));

        ElevationMetersInput  .setText(doubleToUI(coordinateWGS84.getElevation()));
        GeoidHeightMetersInput.setText(doubleToUI(coordinateWGS84.getGeoid()));
        ElevationFeetInput.setText(doubleToUI(coordinateWGS84.getElevationFeet()));
        GeoidHeightFeetInput.setText(doubleToUI(coordinateWGS84.getGeoidFeet()));


        //input range check depends on what is being converted
        boolean isCA = false;
        boolean isLatitude = true;
        convertDDtoDMS(getActivity(),
                        LatitudeInput,
                        LatDegreesInput,
                        LatMinutesInput,
                        LatSecondsInput,
                        isCA,
                        isLatitude);

        isLatitude = false;
        convertDDtoDMS(getActivity(),
                        LongitudeInput,
                        LongDegreesInput,
                        LongMinutesInput,
                        LongSecondsInput,
                        isCA,
                        isLatitude);

        isCA = true;
        convertDDtoDMS(getActivity(),
                        ConvergenceAngleInput,
                        CAdegInput,
                        CAminInput,
                        CAsecInput,
                        isCA,
                        isLatitude);



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

        //convergence & scale factor
        TextView ConvergenceAngleInput  = (TextView) v.findViewById(R.id.utmConvergenceInput);
        TextView CAdegInput  = (TextView) v.findViewById(R.id.utmConvDegreesInput);
        TextView CAminInput  = (TextView) v.findViewById(R.id.utmConvMinutesInput);
        TextView CAsecInput  = (TextView) v.findViewById(R.id.utmConvSecondsInput);

        //scale factor
        TextView ScaleFactorInput       = (TextView) v.findViewById(R.id.utmScaleFactor);

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

        ConvergenceAngleInput .setText(String.valueOf(coordinateUTM.getConvergenceAngle()));
        // degrees minutes seconds
        boolean isCA = true;
        convertDDtoDMS(getActivity(),
                        ConvergenceAngleInput,
                        CAdegInput,
                        CAminInput,
                        CAsecInput,
                        isCA,
                        false);

        ScaleFactorInput .setText(String.valueOf(coordinateUTM.getScaleFactor()));
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
            clearSpcUI(v);
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

        //convergence & scale factor
        TextView ConvergenceAngleInput  = (TextView) v.findViewById(R.id.spcConvergenceInput);
        TextView CAdegInput  = (TextView) v.findViewById(R.id.spcConvDegreesInput);
        TextView CAminInput  = (TextView) v.findViewById(R.id.spcConvMinutesInput);
        TextView CAsecInput  = (TextView) v.findViewById(R.id.spcConvSecondsInput);

        //scale factor
        TextView ScaleFactorInput       = (TextView) v.findViewById(R.id.spcScaleFactor);


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

        ConvergenceAngleInput   .setText(String.valueOf(doubleToUI(coordinateSPCS.getConvergenceAngle())));

        boolean isCA = true;
        convertDDtoDMS(getActivity(),
                        ConvergenceAngleInput,
                        CAdegInput,
                        CAminInput,
                        CAsecInput,
                        isCA,
                        false);

        ScaleFactorInput   .setText(String.valueOf(doubleToUI(coordinateSPCS.getScaleFactor())));

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
                           boolean  isCA,
                           boolean  isLatitude) {

        String tudeString = tudeDDInput.getText().toString().trim();
        if (tudeString.isEmpty()) {
            tudeString = context.getString(R.string.zero_decimal_string);
            tudeDDInput.setText(tudeString);
        }

        double tude = Double.parseDouble(tudeString);

        //The user inputs have to be within range to be
        //no range check necessary for convergence angle
        if (((!isCA) &&   isLatitude   && ((tude < -90.0) || (tude >= 90.0)))  || //Latitude
            ((!isCA) && (!isLatitude)  && ((tude < -180.) || (tude >= 180.)))) {  //Longitude

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
        View v = getView();
        if (v == null)return;

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
                clearUtmUI(v);
                updateUtmUI(coordinateWGS84);

                //Convert to State Plane
                clearSpcUIxZone(v);
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

                clearWgsUI(v);
                updateWgsUI(coordinateWGS84);
                clearSpcUIxZone(v);
                updateSpcsUI(coordinateSPCS);
                clearUtmUI(v);
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
                clearWgsUI(v);
                updateWgsUI(coordinateWGS84);

                //Convert to UTM Coordinate and display it
                clearSpcUIxZone(v);
                updateSpcsUI(coordinateWGS84);

                //display UTM Coordinate
                clearUtmUI(v);
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

                clearWgsUI(v);
                updateWgsUI(coordinateWGS84);

                //Convert the WGS84 to UTM, and display the UTM coordinate
                clearUtmUI(v);
                updateUtmUI(coordinateWGS84);

                //Convert to State Plane and display the SPCS coordinate
                clearSpcUIxZone(v);
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
            coordinateWGS84 = new GBCoordinateWGS84((GBActivity)getActivity(),
                                                    mMeanToken.getMeanCoordinate(true));
        }
        return coordinateWGS84;
    }

    private GBCoordinateWGS84 convertWgsInputs() {
        View v = getView();
        if (v == null)return null;

        //Time
        //EditText TimeOutput      = (EditText) v.findViewById(R.id.gpsWgs84TimeOutput);
        TextView TimeStampOutput = (TextView) v.findViewById(R.id.gpsWgs84TimestampOutput);
        //GPS Latitude
        EditText LatitudeInput   = (EditText) v.findViewById(R.id.gpsWgs84LatitudeInput);
        EditText LatDegreesInput = (EditText) v.findViewById(R.id.gpsWgs84LatDegreesInput);
        EditText LatMinutesInput = (EditText) v.findViewById(R.id.gpsWgs84LatMinutesInput);
        EditText LatSecondsInput = (EditText) v.findViewById(R.id.gpsWgs84LatSecondsInput);

        //GPS Longitude
        EditText LongitudeInput   = (EditText) v.findViewById(R.id.gpsWgs84LongitudeInput);
        EditText LongDegreesInput = (EditText) v.findViewById(R.id.gpsWgs84LongDegreesInput);
        EditText LongMinutesInput = (EditText) v.findViewById(R.id.gpsWgs84LongMinutesInput);
        EditText LongSecondsInput = (EditText) v.findViewById(R.id.gpsWgs84LongSecondsInput);

        //Elevation
        EditText ElevationMetersInput   = (EditText) v.findViewById(R.id.gpsWgs84ElevationMetersInput);
        EditText GeoidHeightMetersInput = (EditText) v.findViewById(R.id.gpsWgs84GeoidHeightMetersInput);
        EditText ElevationFeetInput     = (EditText) v.findViewById(R.id.gpsWgs84ElevationFeetInput);
        EditText GeoidHeightFeetInput   = (EditText) v.findViewById(R.id.gpsWgs84GeoidHeightFeetInput);


        //convergence & scale factor
        TextView ConvergenceAngleInput  = (TextView) v.findViewById(R.id.gpsWgs84ConvergenceInput);
        TextView CAdegInput             = (TextView) v.findViewById(R.id.gpsWgs84ConvDegreesInput);
        TextView CAminInput             = (TextView) v.findViewById(R.id.gpsWgs84ConvMinutesInput);
        TextView CAsecInput             = (TextView) v.findViewById(R.id.gpsWgs84ConvSecondsInput);

        TextView ScaleFactorInput       = (TextView) v.findViewById(R.id.gpsWgs84ScaleFactor);


        long timeStamp = GBUtilities.getDateTimeFromString(getActivity(),
                TimeStampOutput.getText().toString());
        GBCoordinateWGS84 coordinateWGS84 = new GBCoordinateWGS84(
                                                timeStamp,
                                                LatitudeInput.getText().toString(),
                                                LatDegreesInput.getText().toString(),
                                                LatMinutesInput.getText().toString(),
                                                LatSecondsInput.getText().toString(),
                                                LongitudeInput.getText().toString(),
                                                LongDegreesInput.getText().toString(),
                                                LongMinutesInput.getText().toString(),
                                                LongSecondsInput.getText().toString(),
                                                ElevationMetersInput.getText().toString(),
                                                ElevationFeetInput.getText().toString(),
                                                GeoidHeightMetersInput.getText().toString(),
                                                GeoidHeightFeetInput.getText().toString());
        if (!coordinateWGS84.isValidCoordinate()){

            GBUtilities.getInstance().showStatus(getActivity(), R.string.coordinate_try_again);
            return null;
        }


        String timeStampString = GBUtilities.getDateTimeString(coordinateWGS84.getTime());
        TimeStampOutput.setText(timeStampString);
        //TimeOutput.setText(String.valueOf(timeStamp));
        //display the coordinate values in the UI
        LatitudeInput  .setText(String.valueOf(coordinateWGS84.getLatitude()));
        LatDegreesInput.setText(String.valueOf(coordinateWGS84.getLatitudeDegree()));
        LatMinutesInput.setText(String.valueOf(coordinateWGS84.getLatitudeMinute()));
        LatSecondsInput.setText(String.valueOf(coordinateWGS84.getLatitudeSecond()));

        LongitudeInput  .setText(String.valueOf(coordinateWGS84.getLongitude()));
        LongDegreesInput.setText(String.valueOf(coordinateWGS84.getLongitudeDegree()));
        LongMinutesInput.setText(String.valueOf(coordinateWGS84.getLongitudeMinute()));
        LongSecondsInput.setText(String.valueOf(coordinateWGS84.getLongitudeSecond()));

        ElevationMetersInput.setText(String.valueOf(coordinateWGS84.getElevation()));
        ElevationFeetInput  .setText(String.valueOf(coordinateWGS84.getElevationFeet()));
        GeoidHeightMetersInput.setText(String.valueOf(coordinateWGS84.getGeoid()));
        GeoidHeightFeetInput  .setText(String.valueOf(coordinateWGS84.getGeoidFeet()));


        boolean isCA = true;
        convertDDtoDMS(getActivity(),
                ConvergenceAngleInput,
                CAdegInput,
                CAminInput,
                CAsecInput,
                isCA,
                false);


        ScaleFactorInput     .setText(String.valueOf(coordinateWGS84.getScaleFactor()));

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

        TextView ConvergenceOutput      = (TextView) v.findViewById(R.id.spcConvergenceOutput);
        TextView CAdegInput             = (TextView) v.findViewById(R.id.spcConvDegreesInput);
        TextView CAminInput             = (TextView) v.findViewById(R.id.spcConvMinutesInput);
        TextView CAsecInput             = (TextView) v.findViewById(R.id.spcConvSecondsInput);
        TextView ScaleFactorOutput      = (TextView) v.findViewById(spcScaleFactorOutput);




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
                                                    ConvergenceOutput.getText().toString(),
                                                    ScaleFactorOutput.getText().toString());
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

        ConvergenceOutput.setText(String.valueOf(coordinateSPCS.getConvergenceAngle()));
        boolean isCA = true;
        convertDDtoDMS(getActivity(),
                        ConvergenceOutput,
                        CAdegInput,
                        CAminInput,
                        CAsecInput,
                        isCA,
                        false);

        ScaleFactorOutput.setText(String.valueOf(coordinateSPCS.getScaleFactor()));



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

        TextView ConvergenceOutput      = (TextView) v.findViewById(R.id.utmConvergence);
        TextView CAdegInput             = (TextView) v.findViewById(R.id.utmConvDegreesInput);
        TextView CAminInput             = (TextView) v.findViewById(R.id.utmConvMinutesInput);
        TextView CAsecInput             = (TextView) v.findViewById(R.id.utmConvSecondsInput);
        TextView ScaleFactorOutput      = (TextView) v.findViewById(R.id.utmScaleFactor);


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
                                                    ConvergenceOutput.getText().toString(),
                                                    ScaleFactorOutput.getText().toString());
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

        ConvergenceOutput.setText(String.valueOf(coordinateUTM.getConvergenceAngle()));
        boolean isCA = true;
        convertDDtoDMS(getActivity(),
                        ConvergenceOutput,
                        CAdegInput,
                        CAminInput,
                        CAsecInput,
                        isCA,
                        false);

        ScaleFactorOutput.setText(String.valueOf(coordinateUTM.getScaleFactor()));

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
        // EditText TimeOutput   = (EditText) v.findViewById(R.id.gpsWgs84TimeOutput);
        TextView TimeStampOutput = (TextView) v.findViewById(R.id.gpsWgs84TimestampOutput);
        //GPS Latitude
        EditText LatitudeInput   = (EditText) v.findViewById(R.id.gpsWgs84LatitudeInput);
        EditText LatDegreesInput = (EditText) v.findViewById(R.id.gpsWgs84LatDegreesInput);
        EditText LatMinutesInput = (EditText) v.findViewById(R.id.gpsWgs84LatMinutesInput);
        EditText LatSecondsInput = (EditText) v.findViewById(R.id.gpsWgs84LatSecondsInput);

        //GPS Longitude
        EditText LongitudeInput   = (EditText) v.findViewById(R.id.gpsWgs84LongitudeInput);
        EditText LongDegreesInput = (EditText) v.findViewById(R.id.gpsWgs84LongDegreesInput);
        EditText LongMinutesInput = (EditText) v.findViewById(R.id.gpsWgs84LongMinutesInput);
        EditText LongSecondsInput = (EditText) v.findViewById(R.id.gpsWgs84LongSecondsInput);

        //Elevation
        EditText ElevationMetersInput   = (EditText) v.findViewById(R.id.gpsWgs84ElevationMetersInput);
        EditText GeoidHeightMetersInput = (EditText) v.findViewById(R.id.gpsWgs84GeoidHeightMetersInput);
        EditText ElevationFeetInput     = (EditText) v.findViewById(R.id.gpsWgs84ElevationFeetInput);
        EditText GeoidHeightFeetInput   = (EditText) v.findViewById(R.id.gpsWgs84GeoidHeightFeetInput);


        //convergence & scale factor
        TextView ConvergenceAngleInput  = (TextView) v.findViewById(R.id.gpsWgs84ConvergenceInput);
        TextView CAdegInput             = (TextView) v.findViewById(R.id.gpsWgs84ConvDegreesInput);
        TextView CAminInput             = (TextView) v.findViewById(R.id.gpsWgs84ConvMinutesInput);
        TextView CAsecInput             = (TextView) v.findViewById(R.id.gpsWgs84ConvSecondsInput);
        TextView ScaleFactorOutput      = (TextView) v.findViewById(R.id.gpsWgs84ScaleFactor);

        //TimeOutput     .setEnabled(enableFlag);
        TimeStampOutput.setEnabled(enableFlag);

        LatitudeInput  .setEnabled(enableFlag);
        LatDegreesInput.setEnabled(enableFlag);
        LatMinutesInput.setEnabled(enableFlag);
        LatSecondsInput.setEnabled(enableFlag);

        LongitudeInput  .setEnabled(enableFlag);
        LongDegreesInput.setEnabled(enableFlag);
        LongMinutesInput.setEnabled(enableFlag);
        LongSecondsInput.setEnabled(enableFlag);

        ElevationMetersInput  .setEnabled(enableFlag);
        ElevationFeetInput    .setEnabled(enableFlag);
        GeoidHeightMetersInput.setEnabled(enableFlag);
        GeoidHeightFeetInput  .setEnabled(enableFlag);

        ConvergenceAngleInput.setEnabled(enableFlag);
        CAdegInput           .setEnabled(enableFlag);
        CAminInput           .setEnabled(enableFlag);
        CAsecInput           .setEnabled(enableFlag);
        ScaleFactorOutput    .setEnabled(enableFlag);

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


        //convergence & scale factor
        TextView ConvergenceAngleInput  = (TextView) v.findViewById(R.id.spcConvergenceInput);
        TextView CAdegInput             = (TextView) v.findViewById(R.id.spcConvDegreesInput);
        TextView CAminInput             = (TextView) v.findViewById(R.id.spcConvMinutesInput);
        TextView CAsecInput             = (TextView) v.findViewById(R.id.spcConvSecondsInput);

        TextView ScaleFactorOutput      = (TextView) v.findViewById(R.id.spcScaleFactor);

        spcZoneInput           .setEnabled(true);//always enabled for input, regardless

        spcStateOutput         .setEnabled(enableFlag);
        spcEastingMetersOutput .setEnabled(enableFlag);
        spcNorthingMetersOutput.setEnabled(enableFlag);
        spcEastingFeetOutput   .setEnabled(enableFlag);
        spcNorthingFeetOutput  .setEnabled(enableFlag);

        spcsElevationMetersInput  .setEnabled(enableFlag);
        spcsGeoidHeightMetersInput.setEnabled(enableFlag);
        spcsElevationFeetInput    .setEnabled(enableFlag);
        spcsGeoidHeightFeetInput  .setEnabled(enableFlag);


        ConvergenceAngleInput.setEnabled(enableFlag);
        CAdegInput.setEnabled(enableFlag);
        CAminInput.setEnabled(enableFlag);
        CAsecInput.setEnabled(enableFlag);
        ScaleFactorOutput.setEnabled(enableFlag);

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

        //Elevation
        TextView utmElevationMetersInput   = (TextView) v.findViewById(R.id.utmElevationMetersInput);
        TextView utmGeoidHeightMetersInput = (TextView) v.findViewById(R.id.utmGeoidHeightMetersInput);
        TextView utmElevationFeetInput     = (TextView) v.findViewById(R.id.utmElevationFeetInput);
        TextView utmGeoidHeightFeetInput   = (TextView) v.findViewById(R.id.utmGeoidHeightFeetInput);

        //convergence & scale factor
        TextView ConvergenceAngleInput  = (TextView) v.findViewById(R.id.spcConvergenceInput);
        TextView CAdegInput             = (TextView) v.findViewById(R.id.spcConvDegreesInput);
        TextView CAminInput             = (TextView) v.findViewById(R.id.spcConvMinutesInput);
        TextView CAsecInput             = (TextView) v.findViewById(R.id.spcConvSecondsInput);

        TextView ScaleFactorOutput      = (TextView) v.findViewById(R.id.spcScaleFactor);

        utmZoneOutput          .setEnabled(enableFlag);

        utmLatbandOutput       .setEnabled(enableFlag);
        utmHemisphereOutput    .setEnabled(enableFlag);
        utmEastingMetersOutput .setEnabled(enableFlag);
        utmNorthingMetersOutput.setEnabled(enableFlag);

        utmElevationMetersInput. setEnabled(enableFlag);
        utmElevationFeetInput   .setEnabled(enableFlag);
        utmGeoidHeightMetersInput.setEnabled(enableFlag);
        utmGeoidHeightFeetInput. setEnabled(enableFlag);

        utmEastingFeetOutput   .setEnabled(enableFlag);
        utmNorthingFeetOutput  .setEnabled(enableFlag);

        ConvergenceAngleInput.setEnabled(enableFlag);
        CAdegInput.setEnabled(enableFlag);
        CAminInput.setEnabled(enableFlag);
        CAsecInput.setEnabled(enableFlag);
        ScaleFactorOutput   .setEnabled(enableFlag);

    }


    private void clearForm() {
        View v = getView();
        if (v == null)return;

        clearWgsUI(v);

        clearMeanUI(v);

        clearSpcUI(v);

        clearUtmUI(v);

        //clearNadUI(v);
    }

    private void clearWgsUI(View v){

        //Timestamp
        TextView TimestampOutput = (TextView) v.findViewById(R.id.gpsWgs84TimestampOutput);

        //GPS Latitude
        TextView LatitudeInput   = (TextView) v.findViewById(R.id.gpsWgs84LatitudeInput);
        TextView LatDegreesInput = (TextView) v.findViewById(R.id.gpsWgs84LatDegreesInput);
        TextView LatMinutesInput = (TextView) v.findViewById(R.id.gpsWgs84LatMinutesInput);
        TextView LatSecondsInput = (TextView) v.findViewById(R.id.gpsWgs84LatSecondsInput);

        //GPS Longitude
        TextView LongitudeInput   = (TextView) v.findViewById(R.id.gpsWgs84LongitudeInput);
        TextView LongDegreesInput = (TextView) v.findViewById(R.id.gpsWgs84LongDegreesInput);
        TextView LongMinutesInput = (TextView) v.findViewById(R.id.gpsWgs84LongMinutesInput);
        TextView LongSecondsInput = (TextView) v.findViewById(R.id.gpsWgs84LongSecondsInput);

        //Elevation
        TextView ElevationMetersInput   = (TextView) v.findViewById(R.id.gpsWgs84ElevationMetersInput);
        TextView GeoidHeightMetersInput = (TextView) v.findViewById(R.id.gpsWgs84GeoidHeightMetersInput);
        TextView ElevationFeetInput     = (TextView) v.findViewById(R.id.gpsWgs84ElevationFeetInput);
        TextView GeoidHeightFeetInput   = (TextView) v.findViewById(R.id.gpsWgs84GeoidHeightFeetInput);

        //convergence & scale factor
        TextView ConvergenceAngleInput  = (TextView) v.findViewById(R.id.gpsWgs84ConvergenceInput);
        TextView CAdegInput             = (TextView) v.findViewById(R.id.gpsWgs84ConvDegreesInput);
        TextView CAminInput             = (TextView) v.findViewById(R.id.gpsWgs84ConvMinutesInput);
        TextView CAsecInput             = (TextView) v.findViewById(R.id.gpsWgs84ConvSecondsInput);
        TextView ScaleFactorOutput      = (TextView) v.findViewById(R.id.gpsWgs84ScaleFactor);


        TimestampOutput   .setText("");

        LatitudeInput     .setText("");
        LatDegreesInput   .setText("");
        LatMinutesInput   .setText("");
        LatSecondsInput   .setText("");

        LongitudeInput     .setText("");
        LongDegreesInput   .setText("");
        LongMinutesInput   .setText("");
        LongSecondsInput   .setText("");

        ElevationMetersInput  .setText("");
        ElevationFeetInput    .setText("");
        GeoidHeightMetersInput.setText("");
        GeoidHeightFeetInput  .setText("");

        ConvergenceAngleInput.setText("");
        CAdegInput.setText("");
        CAminInput.setText("");
        CAsecInput.setText("");
        ScaleFactorOutput.setText("");

    }

    private void clearMeanUI(View v){

        mMeanToken = null;

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



        //convergence & scale factor
        TextView ConvergenceAngleInput  = (TextView) v.findViewById(R.id.gpsWgs84ConvergenceInput);
        TextView CAdegInput             = (TextView) v.findViewById(R.id.gpsWgs84ConvDegreesInput);
        TextView CAminInput             = (TextView) v.findViewById(R.id.gpsWgs84ConvMinutesInput);
        TextView CAsecInput             = (TextView) v.findViewById(R.id.gpsWgs84ConvSecondsInput);
        TextView ScaleFactorOutput      = (TextView) v.findViewById(R.id.gpsWgs84ScaleFactor);


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

        ConvergenceAngleInput.   setText("");
        CAdegInput.setText("");
        CAminInput.setText("");
        CAsecInput.setText("");
        ScaleFactorOutput.   setText("");

    }

    private void clearSpcUIxZone(View v) {
        TextView spcZoneOutput = (TextView) v.findViewById(R.id.spcZoneOutput);
        String zone = spcZoneOutput.getText().toString().trim();
        clearSpcUI(v);
        spcZoneOutput.setText(zone);
    }
    private void clearSpcUI(View v){

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


        //convergence & scale factor
        TextView ConvergenceAngleInput  = (TextView) v.findViewById(R.id.gpsWgs84ConvergenceInput);
        TextView CAdegInput             = (TextView) v.findViewById(R.id.gpsWgs84ConvDegreesInput);
        TextView CAminInput             = (TextView) v.findViewById(R.id.gpsWgs84ConvMinutesInput);
        TextView CAsecInput             = (TextView) v.findViewById(R.id.gpsWgs84ConvSecondsInput);
        TextView ScaleFactorOutput      = (TextView) v.findViewById(R.id.gpsWgs84ScaleFactor);


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

        ConvergenceAngleInput.   setText("");
        CAdegInput.setText("");
        CAminInput.setText("");
        CAsecInput.setText("");
        ScaleFactorOutput.   setText("");


    }


    private void setColor(GBCoordinateWGS84 coordinateWGS84){
        View v = getView();
        if (v == null)return;

        //Time
        //EditText TimeOutput      = (EditText) v.findViewById(R.id.gpsWgs84TimeOutput);
        TextView TimeStampOutput = (TextView) v.findViewById(R.id.gpsWgs84TimestampOutput);
        //GPS Latitude
        EditText LatitudeInput   = (EditText) v.findViewById(R.id.gpsWgs84LatitudeInput);
        EditText LatDegreesInput = (EditText) v.findViewById(R.id.gpsWgs84LatDegreesInput);
        EditText LatMinutesInput = (EditText) v.findViewById(R.id.gpsWgs84LatMinutesInput);
        EditText LatSecondsInput = (EditText) v.findViewById(R.id.gpsWgs84LatSecondsInput);

        //GPS Longitude
        EditText LongitudeInput   = (EditText) v.findViewById(R.id.gpsWgs84LongitudeInput);
        EditText LongDegreesInput = (EditText) v.findViewById(R.id.gpsWgs84LongDegreesInput);
        EditText LongMinutesInput = (EditText) v.findViewById(R.id.gpsWgs84LongMinutesInput);
        EditText LongSecondsInput = (EditText) v.findViewById(R.id.gpsWgs84LongSecondsInput);

        //Elevation
        EditText ElevationMetersInput   = (EditText) v.findViewById(R.id.gpsWgs84ElevationMetersInput);
        EditText GeoidHeightMetersInput = (EditText) v.findViewById(R.id.gpsWgs84GeoidHeightMetersInput);
        EditText ElevationFeetInput     = (EditText) v.findViewById(R.id.gpsWgs84ElevationFeetInput);
        EditText GeoidHeightFeetInput   = (EditText) v.findViewById(R.id.gpsWgs84GeoidHeightFeetInput);


        //convergence & scale factor
        TextView ConvergenceAngleInput  = (TextView) v.findViewById(R.id.gpsWgs84ConvergenceInput);
        TextView CAdegInput             = (TextView) v.findViewById(R.id.gpsWgs84ConvDegreesInput);
        TextView CAminInput             = (TextView) v.findViewById(R.id.gpsWgs84ConvMinutesInput);
        TextView CAsecInput             = (TextView) v.findViewById(R.id.gpsWgs84ConvSecondsInput);

        TextView ScaleFactorInput       = (TextView) v.findViewById(R.id.gpsWgs84ScaleFactor);

        int uiColor;
        if (coordinateWGS84.getLatitude() >= 0.0) {
            uiColor = colorPosNumber;

        } else {
            uiColor = colorNegNumber;
        }


        LatitudeInput  .setTextColor(ContextCompat.getColor(getActivity(), uiColor));
        LatDegreesInput.setTextColor(ContextCompat.getColor(getActivity(), uiColor));
        LatMinutesInput.setTextColor(ContextCompat.getColor(getActivity(), uiColor));
        LatSecondsInput.setTextColor(ContextCompat.getColor(getActivity(), uiColor));

        if (coordinateWGS84.getLongitude() >= 0.0) {
            uiColor = colorPosNumber;

        } else {
            uiColor = colorNegNumber;
        }

        LongitudeInput  .setTextColor(ContextCompat.getColor(getActivity(), uiColor));
        LongDegreesInput.setTextColor(ContextCompat.getColor(getActivity(), uiColor));
        LongMinutesInput.setTextColor(ContextCompat.getColor(getActivity(), uiColor));
        LongSecondsInput.setTextColor(ContextCompat.getColor(getActivity(), uiColor));

        if (coordinateWGS84.getElevation() >= 0.0) {
            uiColor = colorPosNumber;

        } else {
            uiColor = colorNegNumber;
        }

        ElevationMetersInput.setTextColor(ContextCompat.getColor(getActivity(), uiColor));
        ElevationFeetInput  .setTextColor(ContextCompat.getColor(getActivity(), uiColor));


        if (coordinateWGS84.getGeoid() >= 0.0) {
            uiColor = colorPosNumber;

        } else {
            uiColor = colorNegNumber;
        }

        GeoidHeightMetersInput.setTextColor(ContextCompat.getColor(getActivity(), uiColor));
        GeoidHeightFeetInput  .setTextColor(ContextCompat.getColor(getActivity(), uiColor));

        if (coordinateWGS84.getConvergenceAngle() >= 0.0) {
            uiColor = colorPosNumber;

        } else {
            uiColor = colorNegNumber;
        }

        ConvergenceAngleInput.setTextColor(ContextCompat.getColor(getActivity(),uiColor));
        CAdegInput.setTextColor(ContextCompat.getColor(getActivity(), uiColor));
        CAminInput.setTextColor(ContextCompat.getColor(getActivity(), uiColor));
        CAsecInput.setTextColor(ContextCompat.getColor(getActivity(), uiColor));

        if (coordinateWGS84.getScaleFactor() >= 0.0) {
            uiColor = colorPosNumber;

        } else {
            uiColor = colorNegNumber;
        }

        ScaleFactorInput     .setTextColor(ContextCompat.getColor(getActivity(),uiColor));

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

        //convergence & scale factor
        TextView ConvergenceAngleInput  = (TextView) v.findViewById(R.id.spcConvergenceInput);
        TextView CAdegInput             = (TextView) v.findViewById(R.id.spcConvDegreesInput);
        TextView CAminInput             = (TextView) v.findViewById(R.id.spcConvMinutesInput);
        TextView CAsecInput             = (TextView) v.findViewById(R.id.spcConvSecondsInput);

        TextView ScaleFactorInput       = (TextView) v.findViewById(R.id.spcScaleFactor);

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

        ConvergenceAngleInput.setTextColor(ContextCompat.getColor(getActivity(), uiColor));
        CAdegInput.setTextColor(ContextCompat.getColor(getActivity(), uiColor));
        CAminInput.setTextColor(ContextCompat.getColor(getActivity(), uiColor));
        CAsecInput.setTextColor(ContextCompat.getColor(getActivity(), uiColor));

        if (coordinateSPCS.getScaleFactor() >= 0.0) {
            uiColor = colorPosNumber;

        } else {
            uiColor = colorNegNumber;
        }

        ScaleFactorInput.setTextColor(ContextCompat.getColor(getActivity(), uiColor));

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


