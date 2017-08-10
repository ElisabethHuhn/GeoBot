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

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.SphericalUtil;

import java.util.Date;
import java.util.Locale;

import static com.asc.msigeosystems.geobot.R.color.colorNegNumber;
import static com.asc.msigeosystems.geobot.R.color.colorPosNumber;


/**
 * The Collect Fragment is the UI
 * when the workflow from WGS84 GPS to NAD83 to UTM/State Plane Coordinates
 * Created by Elisabeth Huhn on 6/15/2016.
 */
public class GBCoordinateMeasureFragment extends Fragment implements GpsStatus.Listener, LocationListener, GpsStatus.NmeaListener {

    // TODO: 7/20/2017 Make sure that this is removed for production
    boolean isDebug = false;
    boolean isFirst = true;





    //These must be in the same order as the items are
    // added to the spinner in wireDataSourceSpinner{}

    private static final boolean sENABLE = true;
    private static final boolean sDISABLE = false;


    private GBNmeaParser mNmeaParser = GBNmeaParser.getInstance();
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
    //*****     Coordinates being displayed           **********/
    //**********************************************************/
    private GBCoordinateWGS84 mCurrentUIWGS84;
    private GBCoordinateUTM   mCurrentUIUTM;
    private GBCoordinateSPCS  mCurrentUISPCS;


    //**********************************************************/
    //*****  DataSource types for Spinner Widgets     **********/
    //**********************************************************/
    private int    mCurDataSource;

    //**********************************************************/
    //*****      Static Methods                       **********/
    //**********************************************************/
    public static GBCoordinateMeasureFragment newInstance(GBPoint point) {

        Bundle args = GBPoint.putPointInArguments(new Bundle(), point);

        GBCoordinateMeasureFragment fragment = new GBCoordinateMeasureFragment();

        fragment.setArguments(args);
        return fragment;
    }

    //**********************************************************/
    //*****  Constructor                              **********/
    //**********************************************************/

    public GBCoordinateMeasureFragment() {
        //for now, we don't need to initialize anything when the fragment
        //  is first created
    }


    //**********************************************************/
    //*****  Lifecycle Methods                        **********/
    //**********************************************************/

    @Override
    public void onCreate(Bundle savedInstanceState){

        super.onCreate(savedInstanceState);

        GBProject openProject;
        mPointBeingMaintained = GBPoint.getPointFromArguments((GBActivity)getActivity(), getArguments());
        if (mPointBeingMaintained == null){
            mPointBeingMaintained = new GBPoint();
            //Theoretically the project id might be null,
            // but you can't really get this far if the project does not exist and is not open
            initializePoint();
        }
        long projectID = mPointBeingMaintained.getForProjectID();
        if (projectID == GBUtilities.ID_DOES_NOT_EXIST){
            initializePoint();
        }

    }


    private void initializePoint(){
        GBProject openProject = GBUtilities.getInstance().getOpenProject((GBActivity)getActivity());
        if (openProject == null)return;
        mPointBeingMaintained.setForProjectID(openProject.getProjectID());
        mPointBeingMaintained.setHeight(openProject.getHeight());
        mPointBeingMaintained.setPointNumber(openProject.getNextPointNumber((GBActivity)getActivity()));
        //The point number is not incremented until the point is saved to the DB for the first time
        //Then the SQL Helper recognizes that a DB ID is being assigned, and
        // also increments the point number
        //openProject.incrementPointNumber((GBActivity)getActivity());
        return;
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_coord_measure, container, false);

        wireWidgets(v);
        wireDataSourceSpinner(v);

        initializeUI(v);
        initializeAutosaveUI(v);

        initializeMeanProgressUI(v);

        // TODO: 7/20/2017 Remove this debug call
        setInitialTestValues(v);
        isFirst = true;

        return v;
    }

    void setInitialTestValues(View v){
        // TODO: 7/20/2017 Remove this debug routine
        if (!isDebug) return;

        if (v == null){
            v = getView();
            if (v == null)return;
        }
        EditText latitudeInput  = (EditText) v.findViewById(R.id.gpsWgs84LatitudeInput);
        EditText latDegInput    = (EditText) v.findViewById(R.id.gpsWgs84LatDegreesInput);
        EditText latMinInput    = (EditText) v.findViewById(R.id.gpsWgs84LatMinutesInput);
        EditText latSecInput    = (EditText) v.findViewById(R.id.gpsWgs84LatSecondsInput);
        EditText longitudeInput = (EditText) v.findViewById(R.id.gpsWgs84LongitudeInput);
        EditText longDegInput   = (EditText) v.findViewById(R.id.gpsWgs84LongDegreesInput);
        EditText longMinInput   = (EditText) v.findViewById(R.id.gpsWgs84LongMinutesInput);
        EditText longSecInput   = (EditText) v.findViewById(R.id.gpsWgs84LongSecondsInput);
        EditText zoneInput      = (EditText) v.findViewById(R.id.spcZoneOutput);
        /*
        latitudeInput .setText("34.072935");
        longitudeInput.setText("-84.189707");

        latDegInput.setText("34");
        latMinInput.setText("4");
        latSecInput.setText("22.566");

        longDegInput.setText("-84");
        longMinInput.setText("11");
        longSecInput.setText("22.9542");
        */

        EditText northingInput = (EditText) v.findViewById(R.id.spcNorthingMetersOutput);
        EditText eastingInput  = (EditText) v.findViewById(R.id.spcEastingMetersOutput);
        northingInput.setText("451593.292");
        eastingInput .setText("697873.457");

        zoneInput     .setText("1002");
    }


    //Ask for location events to start
    @Override
    public void onResume() {
        super.onResume();

        if ((mCurDataSource == GBProject.sDataSourcePhoneGps) && (isGpsOn)) {
            startGps();
        }
        setSubtitle();
    }

    private void setSubtitle() {
        ((GBActivity) getActivity()).setSubtitle(R.string.subtitle_measure);
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

    private boolean autoSave(){
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
                startGps();
            }
        }
        return saveCode;
    }

    private void wireWidgets(View v) {

        //make DD vs DMS on the project work by making various views invisible
        turnOffViews(v);



        //Save Button
        Button saveButton = (Button) v.findViewById(R.id.saveButton);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){

                GBUtilities.getInstance().showStatus(getActivity(), R.string.save_label);

                autoSave();

            }//End on Click
        });

        //Start/Stop Data  Button
        Button startStopDataButton = (Button) v.findViewById(R.id.startStopDataButton);
        startStopDataButton.setLongClickable(true);
        startStopDataButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                ((GBActivity)getActivity()).switchToListSatellitesScreen();
                return false;
            }
        });
        startStopDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){

                // TODO: 6/22/2017 this is only if the data source is phone gps
                int msg = 0;
                switch(mCurDataSource){
                    case GBProject.sDataSourceNoneSelected:
                        msg = R.string.select_data_source;
                        enableManualWgsInput(sDISABLE);
                        enableManualSpcsInput(sDISABLE);
                        enableManualUtmInput(sDISABLE);
                        stopGps();
                        break;
                    case GBProject.sDataSourceWGSManual: //manual
                        msg = R.string.manual_wgs_data_source;
                        stopGps();
                        enableManualWgsInput(sENABLE);
                        enableManualSpcsInput(sDISABLE);
                        enableManualUtmInput(sDISABLE);

                        break;
                    case GBProject.sDataSourceSPCSManual: //manual
                        msg = R.string.manual_spcs_data_source;
                        stopGps();
                        enableManualWgsInput(sDISABLE);
                        enableManualSpcsInput(sENABLE);
                        enableManualUtmInput(sDISABLE);

                        break;
                    case GBProject.sDataSourceUTMManual: //manual
                        msg = R.string.manual_utm_data_source;
                        stopGps();
                        enableManualWgsInput(sDISABLE);
                        enableManualSpcsInput(sDISABLE);
                        enableManualUtmInput(sENABLE);

                        break;
                    case GBProject.sDataSourcePhoneGps://pnone GPS
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
                    case GBProject.sDataSourceExternalGps://external gps
                        enableManualWgsInput(sDISABLE);
                        enableManualSpcsInput(sDISABLE);
                        enableManualUtmInput(sDISABLE);
                        msg = R.string.external_gps;
                        msg = R.string.external_gps_not_available;
                        stopGps();
                        break;
                    case GBProject.sDataSourceCellTowerTriangulation: //cell tower triangulation
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



        //Conversion Button
        Button convertButton = (Button) v.findViewById(R.id.convertButton);
        convertButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){

                int message = R.string.select_data_source;
                if (mCurDataSource == GBProject.sDataSourceNoneSelected){
                    message = R.string.select_data_source;
                } else if (!isMeanInProgress() ) {
                    message = onConvert();
                } else {
                    message = R.string.convert_error_mean;
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







        //Start / stop Mean Button
        Button startMeanButton = (Button) v.findViewById(R.id.startMeanButton);
        startMeanButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {


                return true;
            }
        });
        startMeanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){

                int message;
                if (isMeanInProgress()){

                    message = completeMeanProcessing(mMeanToken, mNmeaData);
                    GBProject openProject = GBUtilities.getInstance().getOpenProject((GBActivity)getActivity());
                    if (GBGeneralSettings.isAutosave((GBActivity)getActivity())){
                        autoSave();
                    }

                } else if (mCurDataSource == GBProject.sDataSourceNoneSelected){
                    message = R.string.select_data_source;
                } else if ((mCurDataSource == GBProject.sDataSourceWGSManual) ||
                           (mCurDataSource == GBProject.sDataSourceSPCSManual)||
                           (mCurDataSource == GBProject.sDataSourceUTMManual))  {
                    // TODO: 6/22/2017 think about how to lift limitation on meaning manual data
                    message = R.string.can_not_mean_manual;
                } else {
                    message = startMeaning(mMeanToken);
                }
                updateMeanProgressUI();

                GBUtilities.getInstance().showStatus(getActivity(), message);
            }//End on Click
        });

        //Set RMS v Standard Deviation Label
        //Set the label to Standard Deviation if in settings
        if (GBGeneralSettings.isRms((GBActivity)getActivity())) {

            GBProject openProject = GBUtilities.getInstance().getOpenProject((GBActivity)getActivity());
            int coordType = openProject.getCoordinateType();
            int eRmsLabel = R.string.ele_rms_label;
            int hRmsLabel = R.string.hrms_label;
            int vRmsLabel = R.string.vrms_label;
            if ((coordType == GBCoordinate.sCoordinateDBTypeSPCS) ||
                    (coordType == GBCoordinate.sCoordinateDBTypeUTM)) {
                hRmsLabel = R.string.eing_rms_label;
                vRmsLabel = R.string.ning_rms_label;
            }
            TextView eleLabelView  = (TextView) v.findViewById(R.id.meanWgs84ElevSigmaLabel);
            TextView hrmsLabelView = (TextView) v.findViewById(R.id.meanWgs84LngSigmaLabel);
            TextView vrmsLabelView = (TextView) v.findViewById(R.id.meanWgs84LatSigmaLabel);
            eleLabelView.setText (eRmsLabel);
            hrmsLabelView.setText(hRmsLabel);
            vrmsLabelView.setText(vRmsLabel);
        }

    }

    private void showDop() {
        GBSatelliteManager satelliteManager = GBSatelliteManager.getInstance();
        double hdopValue  = satelliteManager.getHdop();
        String hdopString = String.format(Locale.getDefault(),"%.3f", hdopValue);
        double vdopValue  = satelliteManager.getHdop();
        String vdopString = String.format(Locale.getDefault(),"%.3f", vdopValue);
        double pdopValue  = satelliteManager.getHdop();
        String pdopString = String.format(Locale.getDefault(),"%.3f", pdopValue);

        View view = getView();
        if (view == null)return ;

        TextView dopView = (TextView)view.findViewById(R.id.hdopOutput);
        dopView.setText(hdopString);
        dopView = (TextView)view.findViewById(R.id.vdopOutput);
        dopView.setText(vdopString);
        dopView = (TextView)view.findViewById(R.id.pdopOutput);
        dopView.setText(pdopString);

        //Snackbar.make(view, dopValues, Snackbar.LENGTH_LONG).setAction("Action", null).show();

    }


    private void turnOffViews(View v){

        //set up the control flags based on Global and Project settings
        GBActivity myActivity = (GBActivity)getActivity();
        GBProject openProject = GBUtilities.getInstance().getOpenProject(myActivity);
        int distUnits         = openProject.getDistanceUnits();
        boolean isDD          = GBGeneralSettings.isLocDD(myActivity);
        boolean isPM          = GBGeneralSettings.isPM(myActivity);

        //Raw GPS
        //Meaned WGS84
        //SPCS
        //UTM

        //Raw GPS
        //GPS Latitude
        TextView LatitudeDirInput = (TextView) v.findViewById(R.id.gpsWgs84LatDirInput);
        TextView LatitudeInput   = (TextView) v.findViewById(R.id.gpsWgs84LatitudeInput);
        TextView LatDegreesInput = (TextView) v.findViewById(R.id.gpsWgs84LatDegreesInput);
        TextView LatMinutesInput = (TextView) v.findViewById(R.id.gpsWgs84LatMinutesInput);
        TextView LatSecondsInput = (TextView) v.findViewById(R.id.gpsWgs84LatSecondsInput);

        //GPS Longitude
        TextView LongitudeDirInput = (TextView) v.findViewById(R.id.gpsWgs84LngDirInput);
        TextView LongitudeInput   = (TextView) v.findViewById(R.id.gpsWgs84LongitudeInput);
        TextView LongDegreesInput = (TextView) v.findViewById(R.id.gpsWgs84LongDegreesInput);
        TextView LongMinutesInput = (TextView) v.findViewById(R.id.gpsWgs84LongMinutesInput);
        TextView LongSecondsInput = (TextView) v.findViewById(R.id.gpsWgs84LongSecondsInput);

        //Elevation
        TextView ElevationMetersInput   = (TextView) v.findViewById(R.id.gpsWgs84ElevationMetersInput);
        TextView GeoidHeightMetersInput = (TextView) v.findViewById(R.id.gpsWgs84GeoidHeightMetersInput);

        //convergence & scale factor
        TextView ConvergenceAngleInput  = (TextView) v.findViewById(R.id.gpsWgs84ConvergenceInput);
        TextView CAdegInput  = (TextView) v.findViewById(R.id.gpsWgs84ConvDegreesInput);
        TextView CAminInput  = (TextView) v.findViewById(R.id.gpsWgs84ConvMinutesInput);
        TextView CAsecInput  = (TextView) v.findViewById(R.id.gpsWgs84ConvSecondsInput);

        if (isPM){
            LatitudeDirInput.setVisibility(View.GONE);
            LongitudeDirInput.setVisibility(View.GONE);
        }

        if (isDD){
            LatDegreesInput.setVisibility(View.GONE);
            LatMinutesInput.setVisibility(View.GONE);
            LatSecondsInput.setVisibility(View.GONE);

            LongDegreesInput.setVisibility(View.GONE);
            LongMinutesInput.setVisibility(View.GONE);
            LongSecondsInput.setVisibility(View.GONE);

            CAdegInput .setVisibility(View.GONE);
            CAminInput .setVisibility(View.GONE);
            CAsecInput .setVisibility(View.GONE);

        } else { //is DMS
            LatitudeInput        .setVisibility(View.GONE);
            LongitudeInput       .setVisibility(View.GONE);
            ConvergenceAngleInput.setVisibility(View.GONE);
        }


        //Mean Latitude
        TextView meanWgs84LatitudeDirInput = (TextView) v.findViewById(R.id.meanWgs84LatDirInput);
        TextView meanWgs84LatitudeInput   = (TextView) v.findViewById(R.id.meanWgs84LatitudeInput);
        TextView meanWgs84LatDegreesInput = (TextView) v.findViewById(R.id.meanWgs84LatDegreesInput);
        TextView meanWgs84LatMinutesInput = (TextView) v.findViewById(R.id.meanWgs84LatMinutesInput);
        TextView meanWgs84LatSecondsInput = (TextView) v.findViewById(R.id.meanWgs84LatSecondsInput);

        TextView meanWgs84LongitudeDirInput = (TextView) v.findViewById(R.id.meanWgs84LngDirInput);
        TextView meanWgs84LongitudeInput   = (TextView) v.findViewById(R.id.meanWgs84LongitudeInput);
        TextView meanWgs84LongDegreesInput = (TextView) v.findViewById(R.id.meanWgs84LongDegreesInput);
        TextView meanWgs84LongMinutesInput = (TextView) v.findViewById(R.id.meanWgs84LongMinutesInput);
        TextView meanWgs84LongSecondsInput = (TextView) v.findViewById(R.id.meanWgs84LongSecondsInput);

        //Elevation
        TextView meanWgs84ElevationMetersInput   = (TextView) v.findViewById(
                                                                R.id.meanWgs84ElevationMetersInput);

        TextView meanWgs84GeoidHeightMetersInput = (TextView) v.findViewById(
                                                                R.id.meanWgs84GeoidHeightMetersInput);


        if (isPM){
            meanWgs84LatitudeDirInput.setVisibility(View.GONE);
            meanWgs84LongitudeDirInput.setVisibility(View.GONE);
        }

        if (isDD){
            meanWgs84LatDegreesInput.setVisibility(View.GONE);
            meanWgs84LatMinutesInput.setVisibility(View.GONE);
            meanWgs84LatSecondsInput.setVisibility(View.GONE);

            meanWgs84LongDegreesInput.setVisibility(View.GONE);
            meanWgs84LongMinutesInput.setVisibility(View.GONE);
            meanWgs84LongSecondsInput.setVisibility(View.GONE);


        } else { //is DMS
            meanWgs84LatitudeInput        .setVisibility(View.GONE);
            meanWgs84LongitudeInput       .setVisibility(View.GONE);
         }





        //SPC
        TextView spcEastingMetersOutput  = (TextView) v.findViewById(R.id.spcEastingMetersOutput);
        TextView spcNorthingMetersOutput = (TextView) v.findViewById(R.id.spcNorthingMetersOutput);

        //Elevation
        TextView spcsElevationMetersInput   = (TextView) v.findViewById(R.id.spcsElevationMetersInput);
        TextView spcsGeoidHeightMetersInput = (TextView) v.findViewById(R.id.spcsGeoidHeightMetersInput);

        //convergence & scale factor
        ConvergenceAngleInput  = (TextView) v.findViewById(R.id.spcConvergenceInput);
        CAdegInput  = (TextView) v.findViewById(R.id.spcConvDegreesInput);
        CAminInput  = (TextView) v.findViewById(R.id.spcConvMinutesInput);
        CAsecInput  = (TextView) v.findViewById(R.id.spcConvSecondsInput);




        if (isDD){
            CAdegInput .setVisibility(View.GONE);
            CAminInput .setVisibility(View.GONE);
            CAsecInput .setVisibility(View.GONE);

        } else { //is DMS

            ConvergenceAngleInput.setVisibility(View.GONE);

        }


        TextView utmEastingMetersOutput  = (TextView) v.findViewById(R.id.utmEastingMetersOutput);
        TextView utmNorthingMetersOutput = (TextView) v.findViewById(R.id.utmNorthingMetersOutput);

        TextView utmElevationOutput       = (TextView) v.findViewById(R.id.utmElevationMetersInput) ;
        TextView utmGeoidOutput           = (TextView) v.findViewById(R.id.utmGeoidHeightMetersInput) ;

        //convergence & scale factor
        ConvergenceAngleInput  = (TextView) v.findViewById(R.id.utmConvergenceInput);
        CAdegInput  = (TextView) v.findViewById(R.id.utmConvDegreesInput);
        CAminInput  = (TextView) v.findViewById(R.id.utmConvMinutesInput);
        CAsecInput  = (TextView) v.findViewById(R.id.utmConvSecondsInput);


        if (isDD){
            CAdegInput .setVisibility(View.GONE);
            CAminInput .setVisibility(View.GONE);
            CAsecInput .setVisibility(View.GONE);

        } else { //is DMS

            ConvergenceAngleInput.setVisibility(View.GONE);

        }


    }




    private void wireDataSourceSpinner(View v){

        //Create the array of spinner choices from the Types of Coordinates defined
        String[] dataSourceTypes = new String[]{getString(R.string.select_data_source),
                                        getString(R.string.manual_wgs_data_source),
                                        getString(R.string.manual_spcs_data_source),
                                        getString(R.string.manual_utm_data_source),
                                        getString(R.string.phone_gps),
                                        getString(R.string.external_gps),
                                        getString(R.string.cell_tower_triangulation)};

        //Then initialize the spinner itself
        Spinner dataSourceSpinner = (Spinner) v.findViewById(R.id.data_source_spinner);

        // Create an ArrayAdapter using the Activities context AND
        // the string array and a default spinner layout
        ArrayAdapter<String> adapter = new ArrayAdapter<>(  getActivity(),
                                                            android.R.layout.simple_spinner_item,
                                                            dataSourceTypes);

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        dataSourceSpinner.setAdapter(adapter);

        //attach the listener to the spinner
        dataSourceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                clearForm();
                mCurDataSource = position;

                int msg = 0;
                switch(mCurDataSource){
                    case GBProject.sDataSourceNoneSelected:
                        enableManualWgsInput(sDISABLE);
                        enableManualSpcsInput(sDISABLE);
                        enableManualUtmInput(sDISABLE);
                        stopGps();
                        msg = R.string.select_data_source;
                        break;
                    case GBProject.sDataSourceWGSManual:
                        msg = R.string.manual_wgs_data_source;
                        enableManualWgsInput(sENABLE);
                        enableManualSpcsInput(sDISABLE);
                        enableManualUtmInput(sDISABLE);
                        stopGps();

                        break;
                    case GBProject.sDataSourceSPCSManual:
                        msg = R.string.manual_spcs_data_source;
                        enableManualWgsInput(sDISABLE);
                        enableManualSpcsInput(sENABLE);
                        enableManualUtmInput(sDISABLE);
                        stopGps();

                        break;
                    case GBProject.sDataSourceUTMManual:
                        msg = R.string.manual_utm_data_source;
                        enableManualWgsInput(sDISABLE);
                        enableManualSpcsInput(sDISABLE);
                        enableManualUtmInput(sENABLE);
                        stopGps();

                        break;
                    case GBProject.sDataSourcePhoneGps:
                        // TODO: 6/22/2017 need to check that GPS is supported on this device
                        initializeGPS();
                        startGps();
                        enableManualWgsInput(sDISABLE);
                        enableManualSpcsInput(sDISABLE);
                        enableManualUtmInput(sDISABLE);
                        msg = R.string.phone_gps;

                        break;
                    case GBProject.sDataSourceExternalGps:
                        msg = R.string.external_gps;
                        msg = R.string.external_gps_not_available;
                        enableManualWgsInput(sDISABLE);
                        enableManualSpcsInput(sDISABLE);
                        enableManualUtmInput(sDISABLE);
                        stopGps();
                        break;
                    case GBProject.sDataSourceCellTowerTriangulation:
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

                if (isDebug && isFirst){
                    setInitialTestValues(null);
                    isFirst = false;
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //for now, do nothing
            }
        });


        //set default setting from the project
        GBProject openProject = GBUtilities.getInstance().getOpenProject((GBActivity)getActivity());
        int dataSource = openProject.getDataSource();
        dataSourceSpinner.setSelection(dataSource);


    }

    private void initializeUI(View v){
        GBProject openProject = GBUtilities.getInstance().getOpenProject((GBActivity)getActivity());
        int zone = openProject.getZone();
        updateSpcsZone(v, zone);

        String distUnitString = openProject.getDistUnitString();
        updateDistUnits(v, distUnitString);

        TextView latitudeLabel        = (TextView) v.findViewById(R.id.gpsWgs84LatitudeInputLabel);
        TextView longitudeLabel       = (TextView) v.findViewById(R.id.gpsWgs84LongitudeInputLabel);

        TextView meanaLatitudeLabel   = (TextView) v.findViewById(R.id.meanWgs84LatitudeInputLabel);
        TextView meanLongitudeLabel   = (TextView) v.findViewById(R.id.meanWgs84LongitudeInputLabel);

        TextView northingLabel        = (TextView) v.findViewById(R.id.utmNorthingInputLabel) ;
        TextView eastingLabel         = (TextView) v.findViewById(R.id.utmEasingInputLabel);

        TextView spcNorthingLabel     = (TextView) v.findViewById(R.id.spcNorthingInputLabel);
        TextView spcEastingLabel      = (TextView) v.findViewById(R.id.spcEasingInputLabel);


        if (GBGeneralSettings.isLngLat((GBActivity)getActivity())){


            //Longitude comes before Latitude
            //Easting   comes before Northing
            //confusingly, this is done by switching lables on the screen

            latitudeLabel        = (TextView) v.findViewById(R.id.gpsWgs84LongitudeInputLabel);
            longitudeLabel       = (TextView) v.findViewById(R.id.gpsWgs84LatitudeInputLabel);

            meanaLatitudeLabel   = (TextView) v.findViewById(R.id.meanWgs84LongitudeInputLabel);
            meanLongitudeLabel   = (TextView) v.findViewById(R.id.meanWgs84LatitudeInputLabel);

            northingLabel        = (TextView) v.findViewById(R.id.utmEasingInputLabel) ;
            eastingLabel         = (TextView) v.findViewById(R.id.utmNorthingInputLabel);

            spcNorthingLabel     = (TextView) v.findViewById(R.id.spcEasingInputLabel);
            spcEastingLabel      = (TextView) v.findViewById(R.id.spcNorthingInputLabel);

        }
        latitudeLabel     .setText(getString(R.string.latitude_label));
        longitudeLabel    .setText(getString(R.string.longitude_label));
        meanaLatitudeLabel.setText(getString(R.string.latitude_label));
        meanLongitudeLabel.setText(getString(R.string.longitude_label));
        northingLabel     .setText(getString(R.string.northing_label));
        eastingLabel      .setText(getString(R.string.easting_label));
        spcNorthingLabel  .setText(getString(R.string.northing_label));
        spcEastingLabel   .setText(getString(R.string.easting_label));
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

        // TODO: 7/26/2017 These conversions are invalid. The screen values have been truncated

        if (coordinateType.equals(GBCoordinate.sCoordinateTypeWGS84)){

            if ((mMeanToken != null) && (mMeanToken.getCoordinateSize() > 0)){
                //Use the meaned result to generate coordinate
                coordinate = new GBCoordinateWGS84((GBActivity)getActivity(),
                                                    mMeanToken.getMeanCoordinate());
            } else {
                //use the raw WGS input to generate the coordinate
                coordinate = mCurrentUIWGS84;
            }

        } else if (coordinateType.equals(GBCoordinate.sCoordinateTypeUTM)){
            coordinate = convertUtmInputs();

        } else if (coordinateType.equals(GBCoordinate.sCoordinateTypeSPCS)){
            coordinate = convertSpcsInputs();

        }
        if (coordinate == null){
            GBUtilities.getInstance().showStatus(getActivity(), R.string.coordinate_not_valid);
            return false;
        }
        coordinate.setProjectID(openProjectID);

        if (coordinate.isValidCoordinate()) {
            //store the mean token
            GBMeanTokenManager tokenManager = GBMeanTokenManager.getInstance();

            //when here, know the coordinate is valid, and
            // the mean token has been successfully saved to the DB
            if (mPointBeingMaintained == null) {
                //Create the new point and put it on the project
                mPointBeingMaintained = new GBPoint();
                initializePoint();//initialize point with values from project
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
            //The setCoordinate() and setMeanToken() routines update
            // the DB as well as the local point object

            //update the coordinate with the point id
            coordinate.setPointID(mPointBeingMaintained.getPointID());
            coordinate.setProjectID(openProjectID);
            //setCoordinate() also stores the coordinate in the DB
            mPointBeingMaintained.setCoordinate(coordinate);
            if (mMeanToken != null) {
                //record it in DB an on the point
                //Writing to the DB results in the ID being assigned
                mMeanToken.setProjectID(openProjectID);
                mMeanToken.setPointID(mPointBeingMaintained.getPointID());
                //setting the Mean Token on the point writes the token to the DB as well
                mPointBeingMaintained.setMeanToken(mMeanToken);

                GBCoordinateMean coordinateMean = mMeanToken.getMeanCoordinate();
                //set the RMS of the point
                mPointBeingMaintained.setVrms(coordinateMean.getLatitudeStdDev());
                mPointBeingMaintained.setHrms(coordinateMean.getLongitudeStdDev());

            }
            mPointBeingMaintained.setHdop(GBSatelliteManager.getInstance().getHdop());
            mPointBeingMaintained.setVdop(GBSatelliteManager.getInstance().getVdop());
            mPointBeingMaintained.setPdop(GBSatelliteManager.getInstance().getPdop());

            //As the coordinate & token were changed, we need to update the point in the DB again.
            if (!pointManager.addPointToProject(openProject, mPointBeingMaintained, addToDBToo)) {
                //This will NOT do a cascade add of coordinate, meanToken
                GBUtilities.getInstance().showStatus(getActivity(), getString(R.string.error_adding_point));
                return false;
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

        showDop();


        try {
            //no need to process the sentence if no longer attached to the activity
            if (getActivity() == null)return;

            //create an object with all the fields from the string
            if (mNmeaParser == null)mNmeaParser = GBNmeaParser.getInstance();

            GBNmea nmeaData = mNmeaParser.parse(nmea);
            if (nmeaData == null) return;

            nmeaData.setTimeStamp(timestamp);

            nmeaData = filterNmeaData(nmeaData);
            if (nmeaData == null)return;

            //so we know it's a good point
            mNmeaData = nmeaData;


            GBCoordinateWGS84 coordinateWGS84;
            if (isMeanInProgress()){
                if (mMeanToken == null){
                    initializeMeanToken();
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

                    updateNmeaUI(mMeanToken.getLastCoordinate(), mNmeaData);

                    updateMeanUI(meanCoordinate, mMeanToken);

                    //determine if we have enough fixed points to be done with the mean
                    GBProject openProject = GBUtilities.getInstance().getOpenProject((GBActivity)getActivity());
                    int numMean = openProject.getNumMean();
                    if (numMean <= mMeanToken.getFixedReadings()){
                        completeMeanProcessing(mMeanToken, mNmeaData);
                        if (GBGeneralSettings.isAutosave((GBActivity)getActivity())){
                            autoSave();
                        }
                    }
                }
            } else {
                if ((mMeanToken != null) && (mMeanToken.isLastPointInMean())){
                    //no need to recalcuclate the mean.
                    updateMeanUI(mMeanToken.getMeanCoordinate(false), mMeanToken);
                    mMeanToken.setLastPointInMean(false);
                }
                coordinateWGS84 = new GBCoordinateWGS84((GBActivity)getActivity(), nmeaData);

                //update the UI from the coordinate
                updateNmeaUI(coordinateWGS84, nmeaData);
            }

        } catch (RuntimeException e){
            //there was an exception processing the NMEA Sentence
            GBUtilities.getInstance().showStatus(getActivity(), e.getMessage());
            //throw new RuntimeException(e);
            e.printStackTrace();
        }
    }

    private GBNmea filterNmeaData(GBNmea nmeaData){
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


        // TODO: 7/6/2017 need to include Satellite sentences to be able to reject points from satellites too low on the horizon
        if (!type.contains("GGA")) {
            if (!type.contains("GNS")){
                //put satellite DOP in footer
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
        long openProjectID = GBUtilities.getInstance().getOpenProjectID((GBActivity)getActivity());
        long pointID = mPointBeingMaintained.getPointID();
        mMeanToken.setProjectID(openProjectID);
        mMeanToken.setPointID(pointID);
        mMeanToken.setMeanInProgress(false);
        mMeanToken.setFirstPointInMean(false);
        mMeanToken.setLastPointInMean(false);
        mMeanToken.resetCoordinates();
        updateMeanProgressUI();
    }


    private int startMeaning(GBMeanToken token){
        //set flags to start taking mean
        initializeMeanToken();
        mMeanToken.setFirstPointInMean(true);
        mMeanToken.setMeanInProgress(true);
        updateMeanProgressUI();

        return R.string.start_mean_button_label;
    }

    private int completeMeanProcessing(GBMeanToken token, GBNmea nmeaData){
        //set flags that mean is done
        token.setMeanInProgress(false);
        token.setEndMeanTime(nmeaData.getTimeStamp());
        token.setLastPointInMean(true);
        GBUtilities.soundMeanComplete((GBActivity)getActivity());
        updateMeanProgressUI();
        onConvert();
        return  R.string.stop_mean_button_label;
    }



    //******************************************************************//
    //       Update UI with a coordinate                                //
    //******************************************************************//

    private boolean updateNmeaUI(GBCoordinateWGS84 coordinateWGS84, GBNmea nmeaData){
        View v = getView();
        if (v == null)return false;

        //Time
        TextView TimestampOutput = (TextView)v.findViewById(R.id.gpsWgs84TimestampOutput);

        String nmeaTimeString = String.format(Locale.getDefault(), "%.0f", nmeaData.getTime());
        //String wgsTimeString  = String.format(Locale.getDefault(), "%.0f", coordinateWGS84.getTime());
        //TimeOutput.setText(nmeaTimeString);
                //todo fix time between nmea and coordinate WGS84
                //setText(Double.toString(coordinateWGS84.getTime()));
                //setText(Double.toString(nmeaData.getTime()));

        //String timestampString = GBUtilities.getDateTimeString((long)nmeaData.getTimeStamp());

        String timestampString = GBUtilities.getDateTimeString(coordinateWGS84.getTime());
        TimestampOutput.setText(timestampString);

        updateWgsLocUI(coordinateWGS84);

         return true;
    }

    private boolean updateWgsUI(GBCoordinateWGS84 coordinateWGS84){
        View v = getView();
        if (v == null)return false;

        //Time
        TextView TimestampOutput = (TextView)v.findViewById(R.id.gpsWgs84TimestampOutput);

        long timeStamp = coordinateWGS84.getTime();
        String timestampString = GBUtilities.getDateTimeString(timeStamp);

        String timeString = String.format(Locale.getDefault(), "%d", coordinateWGS84.getTime());

        if (timeStamp == 0){
            timestampString = "0";
        }
        TimestampOutput.setText(timestampString);


        updateWgsLocUI(coordinateWGS84);
        return true;
    }

    private boolean updateWgsLocUI(GBCoordinateWGS84 coordinateWGS84){
        View v = getView();
        if (v == null)return false;

        mCurrentUIWGS84 = coordinateWGS84;

        //Latitude comes before Longitude

        //GPS Latitude
        TextView latitudeLabel   = (TextView) v.findViewById(R.id.gpsWgs84LatitudeInputLabel);
        TextView latitudeDir     = (TextView) v.findViewById(R.id.gpsWgs84LatDirInput);
        TextView latitudeInput   = (TextView) v.findViewById(R.id.gpsWgs84LatitudeInput);
        TextView latDegreesInput = (TextView) v.findViewById(R.id.gpsWgs84LatDegreesInput);
        TextView latMinutesInput = (TextView) v.findViewById(R.id.gpsWgs84LatMinutesInput);
        TextView latSecondsInput = (TextView) v.findViewById(R.id.gpsWgs84LatSecondsInput);

        //GPS Longitude
        TextView longitudeLabel   = (TextView) v.findViewById(R.id.gpsWgs84LongitudeInputLabel);
        TextView longitudeDir     = (TextView) v.findViewById(R.id.gpsWgs84LngDirInput);
        TextView longitudeInput   = (TextView) v.findViewById(R.id.gpsWgs84LongitudeInput);
        TextView longDegreesInput = (TextView) v.findViewById(R.id.gpsWgs84LongDegreesInput);
        TextView longMinutesInput = (TextView) v.findViewById(R.id.gpsWgs84LongMinutesInput);
        TextView longSecondsInput = (TextView) v.findViewById(R.id.gpsWgs84LongSecondsInput);

        if (GBGeneralSettings.isLngLat((GBActivity)getActivity())){


            //Longitude comes before Latitude

            //GPS Latitude
            latitudeLabel   = (TextView) v.findViewById(R.id.gpsWgs84LongitudeInputLabel);
            latitudeDir     = (TextView) v.findViewById(R.id.gpsWgs84LngDirInput);
            latitudeInput   = (TextView) v.findViewById(R.id.gpsWgs84LongitudeInput);
            latDegreesInput = (TextView) v.findViewById(R.id.gpsWgs84LongDegreesInput);
            latMinutesInput = (TextView) v.findViewById(R.id.gpsWgs84LongMinutesInput);
            latSecondsInput = (TextView) v.findViewById(R.id.gpsWgs84LongSecondsInput);

            //GPS Longitude
            longitudeLabel   = (TextView) v.findViewById(R.id.gpsWgs84LatitudeInputLabel);
            longitudeDir     = (TextView) v.findViewById(R.id.gpsWgs84LatDirInput);
            longitudeInput   = (TextView) v.findViewById(R.id.gpsWgs84LatitudeInput);
            longDegreesInput = (TextView) v.findViewById(R.id.gpsWgs84LatDegreesInput);
            longMinutesInput = (TextView) v.findViewById(R.id.gpsWgs84LatMinutesInput);
            longSecondsInput = (TextView) v.findViewById(R.id.gpsWgs84LatSecondsInput);

        }

        //convergence & scale factor
        TextView ConvergenceAngleInput  = (TextView) v.findViewById(R.id.gpsWgs84ConvergenceInput);
        TextView CAdegInput             = (TextView) v.findViewById(R.id.gpsWgs84ConvDegreesInput);
        TextView CAminInput             = (TextView) v.findViewById(R.id.gpsWgs84ConvMinutesInput);
        TextView CAsecInput             = (TextView) v.findViewById(R.id.gpsWgs84ConvSecondsInput);
        EditText ScaleFactorOutput      = (EditText) v.findViewById(R.id.gpsWgs84ScaleFactor);






        GBActivity myActivity = (GBActivity)getActivity();
        boolean isDD = GBGeneralSettings.isLocDD(myActivity);
        boolean isDir = GBGeneralSettings.isDir(myActivity);

        int locDigOfPrecision = GBGeneralSettings.getLocPrecision(myActivity);
        if (isDD){
            double latitude  = coordinateWGS84.getLatitude();
            int posHemi = R.string.hemisphere_N;
            int negHemi = R.string.hemisphere_S;
            GBUtilities.locDD(myActivity, latitude, locDigOfPrecision,
                    isDir, posHemi, negHemi, latitudeDir, latitudeInput);

            double longitude = coordinateWGS84.getLongitude();
            posHemi = R.string.hemisphere_E;
            negHemi = R.string.hemisphere_W;
            GBUtilities.locDD(myActivity, longitude, locDigOfPrecision,
                    isDir, posHemi, negHemi, longitudeDir, longitudeInput);

            double convAngle = coordinateWGS84.getConvergenceAngle();
            GBUtilities.caDD(myActivity, convAngle, locDigOfPrecision, ConvergenceAngleInput);

        } else {
            int deg = coordinateWGS84.getLatitudeDegree();
            int min = coordinateWGS84.getLatitudeMinute();
            double sec = coordinateWGS84.getLatitudeSecond();

            int posHemi = R.string.hemisphere_N;
            int negHemi = R.string.hemisphere_S;

            GBUtilities.locDMS(myActivity, deg, min, sec, locDigOfPrecision,
                    isDir, posHemi, negHemi, latitudeDir,
                    latDegreesInput, latMinutesInput, latSecondsInput);

            deg = coordinateWGS84.getLongitudeDegree();
            min = coordinateWGS84.getLongitudeMinute();
            sec = coordinateWGS84.getLongitudeSecond();
            posHemi = R.string.hemisphere_E;
            negHemi = R.string.hemisphere_W;

            GBUtilities.locDMS(myActivity, deg, min, sec, locDigOfPrecision,
                    isDir, posHemi, negHemi, longitudeDir,
                    longDegreesInput, longMinutesInput, longSecondsInput);


            int caDegOfPrecision = GBGeneralSettings.getCAPrecision(myActivity);
            deg = coordinateWGS84.getConvergenceAngleDegree();
            min = coordinateWGS84.getConvergenceAngleMinute();
            sec = coordinateWGS84.getConvergenceAngleSecond();
            GBUtilities.caDMS(myActivity,deg, min, sec,
                              caDegOfPrecision,
                              CAdegInput, CAminInput, CAsecInput);

        }

        //Elevation
        TextView ElevationMetersInput   = (TextView) v.findViewById(R.id.gpsWgs84ElevationMetersInput);
        TextView GeoidHeightMetersInput = (TextView) v.findViewById(R.id.gpsWgs84GeoidHeightMetersInput);

        double elevation = coordinateWGS84.getElevation();
        double geoid     = coordinateWGS84.getGeoid();

        GBUtilities.locDistance(myActivity, elevation, ElevationMetersInput);
        GBUtilities.locDistance(myActivity, geoid, GeoidHeightMetersInput);

        int sfPrecision  = GBGeneralSettings.getSfPrecision(myActivity);
        ScaleFactorOutput   .setText(truncatePrecisionString(sfPrecision, coordinateWGS84.getScaleFactor()));

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

        //Latitude comes before Longitude

        TextView latitudeLabel   = (TextView) v.findViewById(R.id.meanWgs84LatitudeInputLabel);
        TextView latitudeDir     = (TextView) v.findViewById(R.id.meanWgs84LatDirInput);
        TextView latitudeInput   = (TextView) v.findViewById(R.id.meanWgs84LatitudeInput);
        TextView latDegreesInput = (TextView) v.findViewById(R.id.meanWgs84LatDegreesInput);
        TextView latMinutesInput = (TextView) v.findViewById(R.id.meanWgs84LatMinutesInput);
        TextView latSecondsInput = (TextView) v.findViewById(R.id.meanWgs84LatSecondsInput);

        //Longitude
        TextView longitudeLabel   = (TextView) v.findViewById(R.id.meanWgs84LongitudeInputLabel);
        TextView longitudeDir     = (TextView) v.findViewById(R.id.meanWgs84LngDirInput);
        TextView longitudeInput   = (TextView) v.findViewById(R.id.meanWgs84LongitudeInput);
        TextView longDegreesInput = (TextView) v.findViewById(R.id.meanWgs84LongDegreesInput);
        TextView longMinutesInput = (TextView) v.findViewById(R.id.meanWgs84LongMinutesInput);
        TextView longSecondsInput = (TextView) v.findViewById(R.id.meanWgs84LongSecondsInput);

        if (GBGeneralSettings.isLngLat((GBActivity)getActivity())){


            //Longitude comes before Latitude

            latitudeLabel   = (TextView) v.findViewById(R.id.meanWgs84LongitudeInputLabel);
            latitudeDir     = (TextView) v.findViewById(R.id.meanWgs84LngDirInput);
            latitudeInput   = (TextView) v.findViewById(R.id.meanWgs84LongitudeInput);
            latDegreesInput = (TextView) v.findViewById(R.id.meanWgs84LongDegreesInput);
            latMinutesInput = (TextView) v.findViewById(R.id.meanWgs84LongMinutesInput);
            latSecondsInput = (TextView) v.findViewById(R.id.meanWgs84LongSecondsInput);

            //Longitude
            longitudeLabel   = (TextView) v.findViewById(R.id.meanWgs84LatitudeInputLabel);
            longitudeDir     = (TextView) v.findViewById(R.id.meanWgs84LatDirInput);
            longitudeInput   = (TextView) v.findViewById(R.id.meanWgs84LatitudeInput);
            longDegreesInput = (TextView) v.findViewById(R.id.meanWgs84LatDegreesInput);
            longMinutesInput = (TextView) v.findViewById(R.id.meanWgs84LatMinutesInput);
            longSecondsInput = (TextView) v.findViewById(R.id.meanWgs84LatSecondsInput);

        }


        //Elevation
        TextView meanWgs84ElevationMetersInput   = (TextView) v.findViewById(
                                                            R.id.meanWgs84ElevationMetersInput);
         TextView meanWgs84GeoidHeightMetersInput = (TextView) v.findViewById(
                                                            R.id.meanWgs84GeoidHeightMetersInput);

        //Mean Standard Deviations
        TextView meanWgs84LatSigmaOutput = (TextView)v.findViewById(R.id.meanWgs84LatSigmaOutput);
        TextView meanWgs84LongSigmaOutput= (TextView)v.findViewById(R.id.meanWgs84LngSigmaOutput);
        TextView meanWgs84ElevSigmaOutput= (TextView)v.findViewById(R.id.meanWgs84ElevSigmaOutput);





        //show the mean and standard deviation on the screen
        meanWgs84PointsInMeanOutput.setText(String.valueOf(meanCoordinate.getMeanedReadings()));

        long startTimestamp = (long)meanToken.getStartMeanTime();
        String startTimeStampString = GBUtilities.getDateTimeString(startTimestamp);

        long endTimestamp   = (long)meanToken.getEndMeanTime();
        String endTimestampString = GBUtilities.getDateTimeString(endTimestamp);

        meanWgs84StartTimestampOutput.setText(String.valueOf(startTimeStampString));

        if (endTimestamp == 0){
            endTimestampString = "0";
        }
        meanWgs84EndTimestampOutput.setText(String.valueOf(endTimestampString));

        GBActivity myActivity = (GBActivity)getActivity();
        int locPrecision = GBGeneralSettings.getLocPrecision(myActivity);
        int stdPrecision = GBGeneralSettings.getStdDevPrecision(myActivity);

        latitudeLabel         .setText(getString(R.string.latitude_label));
        longitudeLabel        .setText(getString(R.string.longitude_label));



        boolean isDD = GBGeneralSettings.isLocDD(myActivity);
        boolean isDir = GBGeneralSettings.isDir(myActivity);

        int locDigOfPrecision = GBGeneralSettings.getLocPrecision(myActivity);
        if (isDD){
            double latitude  = meanCoordinate.getLatitude();
            int posHemi = R.string.hemisphere_N;
            int negHemi = R.string.hemisphere_S;
            GBUtilities.locDD(myActivity, latitude, locDigOfPrecision,
                    isDir, posHemi, negHemi, latitudeDir, latitudeInput);

            double longitude = meanCoordinate.getLongitude();
            posHemi = R.string.hemisphere_E;
            negHemi = R.string.hemisphere_W;
            GBUtilities.locDD(myActivity, longitude, locDigOfPrecision,
                    isDir, posHemi, negHemi, longitudeDir, longitudeInput);

        } else {
            int deg = meanCoordinate.getLatitudeDegree();
            int min = meanCoordinate.getLatitudeMinute();
            double sec = meanCoordinate.getLatitudeSecond();

            int posHemi = R.string.hemisphere_N;
            int negHemi = R.string.hemisphere_S;

            GBUtilities.locDMS(myActivity, deg, min, sec, locDigOfPrecision,
                    isDir, posHemi, negHemi, latitudeDir,
                    latDegreesInput, latMinutesInput, latSecondsInput);

            deg = meanCoordinate.getLongitudeDegree();
            min = meanCoordinate.getLongitudeMinute();
            sec = meanCoordinate.getLongitudeSecond();
            posHemi = R.string.hemisphere_E;
            negHemi = R.string.hemisphere_W;

            GBUtilities.locDMS(myActivity, deg, min, sec, locDigOfPrecision,
                    isDir, posHemi, negHemi, longitudeDir,
                    longDegreesInput, longMinutesInput, longSecondsInput);

        }

        //Elevation
        TextView ElevationMetersInput   = (TextView) v.findViewById(R.id.gpsWgs84ElevationMetersInput);
        TextView GeoidHeightMetersInput = (TextView) v.findViewById(R.id.gpsWgs84GeoidHeightMetersInput);

        double elevation = meanCoordinate.getElevation();
        double geoid     = meanCoordinate.getGeoid();

        GBUtilities.locDistance(myActivity, elevation, ElevationMetersInput);
        GBUtilities.locDistance(myActivity, geoid, GeoidHeightMetersInput);



        meanWgs84LatSigmaOutput .setText(truncatePrecisionString(stdPrecision, meanCoordinate.getLatitudeStdDev()));
        meanWgs84LongSigmaOutput.setText(truncatePrecisionString(stdPrecision, meanCoordinate.getLongitudeStdDev()));
        meanWgs84ElevSigmaOutput.setText(truncatePrecisionString(stdPrecision, meanCoordinate.getElevationStdDev()));


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

        //Easting before Northing
        TextView eastingLabel         = (TextView) v.findViewById(R.id.utmEasingInputLabel);
        TextView northingLabel        = (TextView) v.findViewById(R.id.utmNorthingInputLabel) ;
        TextView eastingMetersOutput  = (TextView) v.findViewById(R.id.utmEastingMetersOutput);
        TextView northingMetersOutput = (TextView) v.findViewById(R.id.utmNorthingMetersOutput);


        if (GBGeneralSettings.isNE((GBActivity)getActivity())){

            //Northing comes before Easting

            eastingLabel         = (TextView) v.findViewById(R.id.utmNorthingInputLabel);
            northingLabel        = (TextView) v.findViewById(R.id.utmEasingInputLabel) ;
            eastingMetersOutput  = (TextView) v.findViewById(R.id.utmNorthingMetersOutput);
            northingMetersOutput = (TextView) v.findViewById(R.id.utmEastingMetersOutput);
        }

        TextView utmElevationOutput       = (TextView) v.findViewById(R.id.utmElevationMetersInput) ;
        TextView utmGeoidOutput           = (TextView) v.findViewById(R.id.utmGeoidHeightMetersInput) ;

        //convergence & scale factor
        TextView ConvergenceAngleInput  = (TextView) v.findViewById(R.id.utmConvergenceInput);
        TextView CAdegInput  = (TextView) v.findViewById(R.id.utmConvDegreesInput);
        TextView CAminInput  = (TextView) v.findViewById(R.id.utmConvMinutesInput);
        TextView CAsecInput  = (TextView) v.findViewById(R.id.utmConvSecondsInput);

        //scale factor
        TextView ScaleFactorInput       = (TextView) v.findViewById(R.id.utmScaleFactor);


        GBActivity myActivity = (GBActivity)getActivity();
        boolean isDD = GBGeneralSettings.isLocDD(myActivity);
        int locDigOfPrecision = GBGeneralSettings.getLocPrecision(myActivity);
        int caDigOfPrecision  = GBGeneralSettings.getCAPrecision(myActivity);

        //Also output the result in separate fields
        utmZoneOutput        .setText(String.valueOf(coordinateUTM.getZone()));
        utmHemisphereOutput  .setText(String.valueOf(coordinateUTM.getHemisphere()));
        utmLatbandOutput     .setText(String.valueOf(coordinateUTM.getLatBand()));

        eastingLabel       .setText(getString(R.string.easting_label));
        northingLabel      .setText(getString(R.string.northing_label));

        GBUtilities.locDistance(myActivity, coordinateUTM.getEasting(),  eastingMetersOutput);
        GBUtilities.locDistance(myActivity, coordinateUTM.getNorthing(), northingMetersOutput);

        GBUtilities.locDistance(myActivity, coordinateUTM.getElevation(),utmElevationOutput);
        GBUtilities.locDistance(myActivity, coordinateUTM.getGeoid(),     utmGeoidOutput);

        if (isDD){
            double convAngle = coordinateUTM.getConvergenceAngle();
            GBUtilities.caDD(myActivity, convAngle, caDigOfPrecision, ConvergenceAngleInput);

        } else {

            int deg = coordinateUTM.getConvergenceAngleDegree();
            int min = coordinateUTM.getConvergenceAngleMinute();
            double sec = coordinateUTM.getConvergenceAngleSecond();
            GBUtilities.caDMS(myActivity,deg, min, sec, caDigOfPrecision,
                    CAdegInput, CAminInput, CAsecInput);

        }

        int sfPrecision  = GBGeneralSettings.getSfPrecision(myActivity);
        ScaleFactorInput .setText(truncatePrecisionString(sfPrecision, coordinateUTM.getScaleFactor()));
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

        //Easting before Northing
        TextView eastingLabel         = (TextView) v.findViewById(R.id.spcEasingInputLabel);
        TextView northingLabel        = (TextView) v.findViewById(R.id.spcNorthingInputLabel);
        TextView eastingMetersOutput  = (TextView) v.findViewById(R.id.spcEastingMetersOutput);
        TextView northingMetersOutput = (TextView) v.findViewById(R.id.spcNorthingMetersOutput);




        if (GBGeneralSettings.isNE((GBActivity)getActivity())) {
            //Northing comes before Easting
            eastingLabel         = (TextView) v.findViewById(R.id.spcNorthingInputLabel);
            northingLabel        = (TextView) v.findViewById(R.id.spcEasingInputLabel);
            eastingMetersOutput  = (TextView) v.findViewById(R.id.spcNorthingMetersOutput);
            northingMetersOutput = (TextView) v.findViewById(R.id.spcEastingMetersOutput);

        }
            //Elevation
        TextView spcsElevationMetersInput   = (TextView) v.findViewById(R.id.spcsElevationMetersInput);
        TextView spcsGeoidHeightMetersInput = (TextView) v.findViewById(R.id.spcsGeoidHeightMetersInput);

        //convergence & scale factor
        TextView ConvergenceAngleInput  = (TextView) v.findViewById(R.id.spcConvergenceInput);
        TextView CAdegInput  = (TextView) v.findViewById(R.id.spcConvDegreesInput);
        TextView CAminInput  = (TextView) v.findViewById(R.id.spcConvMinutesInput);
        TextView CAsecInput  = (TextView) v.findViewById(R.id.spcConvSecondsInput);

        //scale factor
        TextView ScaleFactorInput       = (TextView) v.findViewById(R.id.spcScaleFactor);


        spcZoneInput           .setText(String.valueOf(coordinateSPCS.getZone()));
        spcStateOutput         .setText(coordinateSPCS.getState());

        eastingLabel       .setText(getString(R.string.easting_label));
        northingLabel      .setText(getString(R.string.northing_label));

        GBActivity myActivity = (GBActivity)getActivity();
        boolean isDD = GBGeneralSettings.isLocDD(myActivity);

        int caPrecision  = GBGeneralSettings.getCAPrecision(myActivity);
        int sfPrecision  = GBGeneralSettings.getSfPrecision(myActivity);

        GBUtilities.locDistance(myActivity, coordinateSPCS.getEasting(),  eastingMetersOutput);
        GBUtilities.locDistance(myActivity, coordinateSPCS.getNorthing(), northingMetersOutput);

        GBUtilities.locDistance(myActivity, coordinateSPCS.getElevation(),spcsElevationMetersInput);
        GBUtilities.locDistance(myActivity, coordinateSPCS.getGeoid(),    spcsGeoidHeightMetersInput);

        if (isDD){
            double convAngle = coordinateSPCS.getConvergenceAngle();
            GBUtilities.caDD(myActivity, convAngle, caPrecision, ConvergenceAngleInput);

        } else {

            int deg = coordinateSPCS.getConvergenceAngleDegree();
            int min = coordinateSPCS.getConvergenceAngleMinute();
            double sec = coordinateSPCS.getConvergenceAngleSecond();
            GBUtilities.caDMS(myActivity,deg, min, sec, caPrecision,
                    CAdegInput, CAminInput, CAsecInput);
        }

        ScaleFactorInput .setText(truncatePrecisionString(sfPrecision, coordinateSPCS.getScaleFactor()));
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

    private boolean initializeAutosaveUI(View v) {

        GBProject openProject = GBUtilities.getInstance().getOpenProject((GBActivity)getActivity());
        //Time
        TextView autosaveOnOffOutput = (TextView) v.findViewById(R.id.autosave_on_off_output);
        int message;
        if (GBGeneralSettings.isAutosave((GBActivity)getActivity())){
            message = R.string.autosave_on;
        } else {
            message = R.string.autosave_off;
        }
        autosaveOnOffOutput.setText(getString(message));

        return true;
    }

    private boolean initializeMeanProgressUI(View v) {

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

        return initializeMeanProgressUI(v);
    }

    private boolean updateSpcsZone(View v, int zone){


        //need to ask for zone, then convert based on the zone
        EditText spcZoneInput = (EditText)v.findViewById(R.id.spcZoneOutput);
        TextView spcStateOutput  = (TextView) v.findViewById(R.id.spcStateOutput);
        if (zone == 0){
            spcStateOutput.setText(getString(R.string.spc_zone_error));
            return false;
        }

        String zoneString = String.valueOf(zone);
        if (GBUtilities.isEmpty(zoneString)){
            spcStateOutput.setText(getString(R.string.spc_zone_error));
            return false;
        }



        GBCoordinateConstants constants = new GBCoordinateConstants(zone);
        int spcsZone = constants.getZone();
        if (spcsZone != (int)GBUtilities.ID_DOES_NOT_EXIST) {
            String state = constants.getState();
            spcStateOutput.setText(state);
            spcZoneInput.setText(zoneString);
        }

        return true;
    }

    private boolean updateDistUnits(View v, String distUnitString){


        //need to ask for zone, then convert based on the zone
        EditText distUnitsOutput = (EditText)v.findViewById(R.id.measureDistanceUnitsInput);
        distUnitsOutput.setText(distUnitString);

        return true;
    }


    //truncate digits of precision
    private String truncatePrecisionString(int digitsOfPrecision, double reading) {
       return GBUtilities.truncatePrecisionString(reading, digitsOfPrecision);
    }




    //******************************************************************//
    //            Convert input WGS84 fields into a CoordinateWGS84     //
    //******************************************************************//
    private int onConvert(){

        if (isMeanInProgress())return R.string.convert_error_mean;
        View v = getView();
        if (v == null)return R.string.convert_error_ui;


        GBCoordinateWGS84 coordinateWGS84 = null;
        switch(mCurDataSource){

            case GBProject.sDataSourceWGSManual:

                //Create teh coordinate from the user inputs
                coordinateWGS84 = convertWgsInputs();

                if ((coordinateWGS84 == null) || !coordinateWGS84.isValidCoordinate()){

                    return R.string.convert_error_source;
                }

                //add in any point offsets before converting
                coordinateWGS84 = addOffsetsToCoordinate(coordinateWGS84);

                //Convert the WGS84 to UTM
                clearUtmUI(v);
                updateUtmUI(coordinateWGS84);

                //Convert to State Plane
                clearSpcUIxZone(v);
                updateSpcsUI(coordinateWGS84);

                break;

            case GBProject.sDataSourceSPCSManual:
                GBCoordinateSPCS coordinateSPCS = convertSpcsInputs();
                if ((coordinateSPCS == null) || !coordinateSPCS.isValidCoordinate()){
                    return R.string.convert_error_source;
                }
                coordinateWGS84 = new GBCoordinateWGS84(coordinateSPCS);
                if ((coordinateWGS84 == null) || !coordinateWGS84.isValidCoordinate()){
                    return R.string.convert_failed;
                }

                //Add in offsets
                coordinateWGS84 = addOffsetsToCoordinate(coordinateWGS84);

                clearWgsUI(v);
                updateWgsUI(coordinateWGS84);

                //convert back to SPCS using the offset values
                clearSpcUIxZone(v);
                updateSpcsUI(coordinateWGS84);

                clearUtmUI(v);
                updateUtmUI(coordinateWGS84);
                break;

            case GBProject.sDataSourceUTMManual:
                GBCoordinateUTM coordinateUTM = convertUtmInputs();
                if ((coordinateUTM == null) || !coordinateUTM.isValidCoordinate()){
                    return R.string.convert_error_source;
                }
                coordinateWGS84 = new GBCoordinateWGS84(coordinateUTM);
                if ( !coordinateWGS84.isValidCoordinate()){
                    return R.string.convert_failed;
                }

                //add in offsets
                coordinateWGS84 = addOffsetsToCoordinate(coordinateWGS84);

                //display WGS Coordinate
                clearWgsUI(v);
                updateWgsUI(coordinateWGS84);

                //Convert to UTM Coordinate and display it
                clearSpcUIxZone(v);
                updateSpcsUI(coordinateWGS84);

                //convert back to UTM with any offsets

                //display UTM Coordinate
                clearUtmUI(v);
                updateUtmUI(coordinateWGS84);
                break;

            case GBProject.sDataSourcePhoneGps:
            case GBProject.sDataSourceExternalGps:
                stopGps();
                coordinateWGS84 = convertMeanedOrRaw();
                if ((coordinateWGS84 == null) || (!coordinateWGS84.isValidCoordinate())){
                    return R.string.convert_error_source;
                }

                coordinateWGS84 = addOffsetsToCoordinate(coordinateWGS84);

                clearWgsUI(v);
                updateWgsUI(coordinateWGS84);

                //Convert the WGS84 to UTM, and display the UTM coordinate
                clearUtmUI(v);
                updateUtmUI(coordinateWGS84);

                //Convert to State Plane and display the SPCS coordinate
                clearSpcUIxZone(v);
                updateSpcsUI(coordinateWGS84);

                break;


            case GBProject.sDataSourceCellTowerTriangulation:
                //for now, cell tower conversion is not supported
                return R.string.cell_tower_triangu_not_available;

            default:
                return R.string.unknown_data_source;
        }
        return R.string.convert_success;
    }

    private GBCoordinateWGS84 addOffsetsToCoordinate(GBCoordinateWGS84 coordinateWGS84){

        double offsetDistance  = mPointBeingMaintained.getOffsetDistance();
        double offsetHeading   = mPointBeingMaintained.getOffsetHeading();
        double offsetElevation = mPointBeingMaintained.getOffsetElevation();

        //calculate the location using the offset
        LatLng fromLocation = new LatLng(coordinateWGS84.getLatitude(), coordinateWGS84.getLongitude());
        LatLng toLocation = SphericalUtil.computeOffset(fromLocation, offsetDistance, offsetHeading);

        double newElevation = coordinateWGS84.getElevation() + offsetElevation;
        coordinateWGS84.setElevation(newElevation);
        return coordinateWGS84;

    }

    private GBCoordinateWGS84 convertMeanedOrRaw(){
        GBCoordinateWGS84 coordinateWGS84;

        //even though the mean is not in progress, it might not have been run yet
        //or it might have been reset
        if ((mMeanToken == null) || (mMeanToken.getCoordinateSize() == 0)) {
            coordinateWGS84 = mCurrentUIWGS84;
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
        EditText LatitudeDirInput = (EditText) v.findViewById(R.id.gpsWgs84LatDirInput);
        EditText LatitudeInput   = (EditText) v.findViewById(R.id.gpsWgs84LatitudeInput);
        EditText LatDegreesInput = (EditText) v.findViewById(R.id.gpsWgs84LatDegreesInput);
        EditText LatMinutesInput = (EditText) v.findViewById(R.id.gpsWgs84LatMinutesInput);
        EditText LatSecondsInput = (EditText) v.findViewById(R.id.gpsWgs84LatSecondsInput);

        //GPS Longitude
        EditText LngDirInput      = (EditText) v.findViewById(R.id.gpsWgs84LngDirInput);
        EditText LongitudeInput   = (EditText) v.findViewById(R.id.gpsWgs84LongitudeInput);
        EditText LongDegreesInput = (EditText) v.findViewById(R.id.gpsWgs84LongDegreesInput);
        EditText LongMinutesInput = (EditText) v.findViewById(R.id.gpsWgs84LongMinutesInput);
        EditText LongSecondsInput = (EditText) v.findViewById(R.id.gpsWgs84LongSecondsInput);

        //Elevation
        EditText ElevationMetersInput   = (EditText) v.findViewById(R.id.gpsWgs84ElevationMetersInput);
        EditText GeoidHeightMetersInput = (EditText) v.findViewById(R.id.gpsWgs84GeoidHeightMetersInput);


        //convergence & scale factor
        TextView ConvergenceAngleInput  = (TextView) v.findViewById(R.id.gpsWgs84ConvergenceInput);
        TextView CAdegInput             = (TextView) v.findViewById(R.id.gpsWgs84ConvDegreesInput);
        TextView CAminInput             = (TextView) v.findViewById(R.id.gpsWgs84ConvMinutesInput);
        TextView CAsecInput             = (TextView) v.findViewById(R.id.gpsWgs84ConvSecondsInput);

        TextView ScaleFactorInput       = (TextView) v.findViewById(R.id.gpsWgs84ScaleFactor);


        String timeStampUIString = TimeStampOutput.getText().toString();

        if (timeStampUIString.equals("")){
            Date now = new Date();
            timeStampUIString = GBUtilities.getDateTimeString(now.getTime());
        }
        long timeStamp = GBUtilities.getDateTimeFromString(getActivity(), timeStampUIString);
        GBCoordinateWGS84 coordinateWGS84 = new GBCoordinateWGS84((GBActivity)getActivity(),
                                                timeStamp,
                                                GBGeneralSettings.isDir((GBActivity)getActivity()),
                                                LatitudeDirInput.getText().toString(),
                                                LatitudeInput.getText().toString(),
                                                LatDegreesInput.getText().toString(),
                                                LatMinutesInput.getText().toString(),
                                                LatSecondsInput.getText().toString(),
                                                LngDirInput.getText().toString(),
                                                LongitudeInput.getText().toString(),
                                                LongDegreesInput.getText().toString(),
                                                LongMinutesInput.getText().toString(),
                                                LongSecondsInput.getText().toString(),
                                                ElevationMetersInput.getText().toString(),
                                                GeoidHeightMetersInput.getText().toString(),
                                                ConvergenceAngleInput.getText().toString(),
                                                ScaleFactorInput.getText().toString());
        if (!coordinateWGS84.isValidCoordinate()){

            GBUtilities.getInstance().showStatus(getActivity(), R.string.coordinate_try_again);
            return null;
        }


        return coordinateWGS84;

    }

    private GBCoordinateSPCS  convertSpcsInputs() {
        View v = getView();
        if (v == null)return null;

        EditText spcZoneInput            = (EditText)v.findViewById(R.id.spcZoneOutput);
        TextView spcStateOutput          = (TextView) v.findViewById(R.id.spcStateOutput);


        //Easting before Northing
        TextView eastingLabel         = (TextView) v.findViewById(R.id.spcEasingInputLabel);
        TextView northingLabel        = (TextView) v.findViewById(R.id.spcNorthingInputLabel);
        TextView eastingMetersOutput  = (TextView) v.findViewById(R.id.spcEastingMetersOutput);
        TextView northingMetersOutput = (TextView) v.findViewById(R.id.spcNorthingMetersOutput);




        if (GBGeneralSettings.isNE((GBActivity)getActivity())) {
            //Northing comes before Easting
            eastingLabel         = (TextView) v.findViewById(R.id.spcNorthingInputLabel);
            northingLabel        = (TextView) v.findViewById(R.id.spcEasingInputLabel);
            eastingMetersOutput  = (TextView) v.findViewById(R.id.spcNorthingMetersOutput);
            northingMetersOutput = (TextView) v.findViewById(R.id.spcEastingMetersOutput);

        }

        //Elevation
        TextView spcsElevationMetersInput   = (TextView) v.findViewById(R.id.spcsElevationMetersInput);
        TextView spcsGeoidHeightMetersInput = (TextView) v.findViewById(R.id.spcsGeoidHeightMetersInput);


        TextView ConvergenceInput       = (TextView) v.findViewById(R.id.spcConvergenceInput);
        TextView CAdegInput             = (TextView) v.findViewById(R.id.spcConvDegreesInput);
        TextView CAminInput             = (TextView) v.findViewById(R.id.spcConvMinutesInput);
        TextView CAsecInput             = (TextView) v.findViewById(R.id.spcConvSecondsInput);
        EditText ScaleFactorOutput      = (EditText) v.findViewById(R.id.spcScaleFactor);

        String geoidMString           = spcsGeoidHeightMetersInput.getText().toString();
        String convergenceAngleString = ConvergenceInput.getText().toString();
        String scaleFactorString      = ScaleFactorOutput.getText().toString();




        GBCoordinateSPCS coordinateSPCS = new GBCoordinateSPCS((GBActivity)getActivity(),
                                                    spcZoneInput.getText().toString(),
                                                    spcStateOutput.getText().toString(),
                                                    eastingMetersOutput.getText().toString(),
                                                    northingMetersOutput.getText().toString(),
                                                    spcsElevationMetersInput.getText().toString(),
                                                    spcsGeoidHeightMetersInput.getText().toString(),
                                                    ConvergenceInput.getText().toString(),
                                                    ScaleFactorOutput.getText().toString());
        if (!coordinateSPCS.isValidCoordinate()){

            GBUtilities.getInstance().showStatus(getActivity(), R.string.coordinate_try_again);
            return null;
        }


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


        //Elevation
        TextView utmElevationMetersInput   = (TextView) v.findViewById(R.id.utmElevationMetersInput);
        TextView utmGeoidHeightMetersInput = (TextView) v.findViewById(R.id.utmGeoidHeightMetersInput);


        TextView ConvergenceOutput      = (TextView) v.findViewById(R.id.utmConvergenceInput);
        TextView CAdegInput             = (TextView) v.findViewById(R.id.utmConvDegreesInput);
        TextView CAminInput             = (TextView) v.findViewById(R.id.utmConvMinutesInput);
        TextView CAsecInput             = (TextView) v.findViewById(R.id.utmConvSecondsInput);
        TextView ScaleFactorOutput      = (TextView) v.findViewById(R.id.utmScaleFactor);


        GBCoordinateUTM coordinateUTM = new GBCoordinateUTM((GBActivity)getActivity(),
                                                    utmZoneOutput.getText().toString(),
                                                    utmLatbandOutput.getText().toString(),
                                                    utmHemisphereOutput.getText().toString(),
                                                    utmEastingMetersOutput.getText().toString(),
                                                    utmNorthingMetersOutput.getText().toString(),
                                                    utmElevationMetersInput.getText().toString(),
                                                    utmGeoidHeightMetersInput.getText().toString(),
                                                    ConvergenceOutput.getText().toString(),
                                                    ScaleFactorOutput.getText().toString());
        if (!coordinateUTM.isValidCoordinate()){

            GBUtilities.getInstance().showStatus(getActivity(), R.string.coordinate_try_again);
            return null;
        }

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
        GeoidHeightMetersInput.setEnabled(enableFlag);

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


        //Elevation
        TextView spcsElevationMetersInput   = (TextView) v.findViewById(R.id.spcsElevationMetersInput);
        TextView spcsGeoidHeightMetersInput = (TextView) v.findViewById(R.id.spcsGeoidHeightMetersInput);



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

        spcsElevationMetersInput  .setEnabled(enableFlag);
        spcsGeoidHeightMetersInput.setEnabled(enableFlag);

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

        //Elevation
        TextView utmElevationMetersInput   = (TextView) v.findViewById(R.id.utmElevationMetersInput);
        TextView utmGeoidHeightMetersInput = (TextView) v.findViewById(R.id.utmGeoidHeightMetersInput);

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
        utmGeoidHeightMetersInput.setEnabled(enableFlag);

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

        clearSpcUIxZone(v);

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
        GeoidHeightMetersInput.setText("");

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
        TextView meanWgs84GeoidHeightMetersInput = (TextView) v.findViewById(
                                                                R.id.meanWgs84GeoidHeightMetersInput);


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
        meanWgs84GeoidHeightMetersInput.setText("");


    }

    private void clearUtmUI(View v){
        TextView utmZoneOutput           = (TextView) v.findViewById(R.id.utmZoneOutput);

        TextView utmLatbandOutput        = (TextView) v.findViewById(R.id.utmLatbandOutput);
        TextView utmHemisphereOutput     = (TextView) v.findViewById(R.id.utmHemisphereOutput);

        TextView utmEastingMetersOutput  = (TextView) v.findViewById(R.id.utmEastingMetersOutput);
        TextView utmNorthingMetersOutput = (TextView) v.findViewById(R.id.utmNorthingMetersOutput);

        //Elevation
        TextView utmElevationMetersInput   = (TextView) v.findViewById(R.id.utmElevationMetersInput);
        TextView utmGeoidHeightMetersInput = (TextView) v.findViewById(R.id.utmGeoidHeightMetersInput);


        //convergence & scale factor
        TextView ConvergenceAngleInput  = (TextView) v.findViewById(R.id.utmConvergenceInput);
        TextView CAdegInput             = (TextView) v.findViewById(R.id.utmConvDegreesInput);
        TextView CAminInput             = (TextView) v.findViewById(R.id.utmConvMinutesInput);
        TextView CAsecInput             = (TextView) v.findViewById(R.id.utmConvSecondsInput);
        TextView ScaleFactorOutput      = (TextView) v.findViewById(R.id.utmScaleFactor);


        utmZoneOutput.          setText("");
        utmLatbandOutput.       setText("");
        utmHemisphereOutput.    setText("");

        utmEastingMetersOutput. setText("");
        utmNorthingMetersOutput.setText("");


        utmElevationMetersInput. setText("");
        utmGeoidHeightMetersInput.setText("");


        ConvergenceAngleInput.   setText("");
        CAdegInput.setText("");
        CAminInput.setText("");
        CAsecInput.setText("");
        ScaleFactorOutput.   setText("");

    }

    private void clearSpcUIxZone(View v) {
        TextView spcZoneOutput = (TextView) v.findViewById(R.id.spcZoneOutput);
        TextView spcStateOutput = (TextView) v.findViewById(R.id.spcStateOutput);
        String zone = spcZoneOutput.getText().toString().trim();
        String state = spcStateOutput.getText().toString().trim();
        clearSpcUI(v);
        spcZoneOutput.setText(zone);
        spcStateOutput.setText(state);
    }
    private void clearSpcUI(View v){

        //SPC
        TextView spcZoneOutput           = (TextView) v.findViewById(R.id.spcZoneOutput);
        TextView spcStateOutput          = (TextView) v.findViewById(R.id.spcStateOutput);

        TextView spcEastingMetersOutput  = (TextView) v.findViewById(R.id.spcEastingMetersOutput);
        TextView spcNorthingMetersOutput = (TextView) v.findViewById(R.id.spcNorthingMetersOutput);


        //Elevation
        TextView spcsElevationMetersInput   = (TextView) v.findViewById(R.id.spcsElevationMetersInput);
        TextView spcsGeoidHeightMetersInput = (TextView) v.findViewById(R.id.spcsGeoidHeightMetersInput);


        //convergence & scale factor
        TextView ConvergenceAngleInput  = (TextView) v.findViewById(R.id.spcConvergenceInput);
        TextView CAdegInput             = (TextView) v.findViewById(R.id.spcConvDegreesInput);
        TextView CAminInput             = (TextView) v.findViewById(R.id.spcConvMinutesInput);
        TextView CAsecInput             = (TextView) v.findViewById(R.id.spcConvSecondsInput);
        TextView ScaleFactorOutput      = (TextView) v.findViewById(R.id.spcScaleFactor);


        spcZoneOutput          .setText("");
        spcStateOutput         .setText("");

        spcEastingMetersOutput .setText("");
        spcNorthingMetersOutput.setText("");


        spcsElevationMetersInput. setText("");
        spcsGeoidHeightMetersInput.setText("");


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



        if (coordinateWGS84.getGeoid() >= 0.0) {
            uiColor = colorPosNumber;

        } else {
            uiColor = colorNegNumber;
        }

        GeoidHeightMetersInput.setTextColor(ContextCompat.getColor(getActivity(), uiColor));


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

        //Elevation
        TextView spcsElevationMetersInput   = (TextView) v.findViewById(R.id.spcsElevationMetersInput);
        TextView spcsGeoidHeightMetersInput = (TextView) v.findViewById(R.id.spcsGeoidHeightMetersInput);

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


        if (coordinateSPCS.getNorthing() >= 0.0) {
            uiColor = colorPosNumber;

        } else {
            uiColor = colorNegNumber;
        }

        spcNorthingMetersOutput  .setTextColor(ContextCompat.getColor(getActivity(), uiColor));

        if (coordinateSPCS.getElevation() >= 0.0) {
            uiColor = colorPosNumber;

        } else {
            uiColor = colorNegNumber;
        }

        spcsElevationMetersInput.setTextColor(ContextCompat.getColor(getActivity(), uiColor));


        if (coordinateSPCS.getGeoid() >= 0.0) {
            uiColor = colorPosNumber;

        } else {
            uiColor = colorNegNumber;
        }

        spcsGeoidHeightMetersInput.setTextColor(ContextCompat.getColor(getActivity(), uiColor));


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

        //Elevation
        TextView utmElevationMetersInput   = (TextView) v.findViewById(R.id.utmElevationMetersInput);
        TextView utmGeoidHeightMetersInput = (TextView) v.findViewById(R.id.utmGeoidHeightMetersInput);


        TextView utmConvergenceOutput    = (TextView) v.findViewById(R.id.utmConvergenceInput);
        TextView utmScaleFactorOutput    = (TextView) v.findViewById(R.id.utmScaleFactor);

        int uiColor;

        if (coordinateUTM.getEasting() >= 0.0) {
            uiColor = colorPosNumber;

        } else {
            uiColor = colorNegNumber;
        }


        utmEastingMetersOutput  .setTextColor(ContextCompat.getColor(getActivity(), uiColor));


        if (coordinateUTM.getNorthing() >= 0.0) {
            uiColor = colorPosNumber;

        } else {
            uiColor = colorNegNumber;
        }

        utmNorthingMetersOutput  .setTextColor(ContextCompat.getColor(getActivity(), uiColor));

        if (coordinateUTM.getElevation() >= 0.0) {
            uiColor = colorPosNumber;

        } else {
            uiColor = colorNegNumber;
        }

        utmElevationMetersInput.setTextColor(ContextCompat.getColor(getActivity(), uiColor));


        if (coordinateUTM.getGeoid() >= 0.0) {
            uiColor = colorPosNumber;

        } else {
            uiColor = colorNegNumber;
        }

        utmGeoidHeightMetersInput.setTextColor(ContextCompat.getColor(getActivity(), uiColor));

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

    private int getSpcZone(){
        View v = getView();
        if (v == null)return 0;
        return getSpcZone(v);
    }
    private int getSpcZone(View v) {
        TextView spcZoneOutput = (TextView) v.findViewById(R.id.spcZoneOutput);
        String zoneString = spcZoneOutput.getText().toString().trim();
        return Integer.valueOf(zoneString);
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


