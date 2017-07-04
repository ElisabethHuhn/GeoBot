package com.asc.msigeosystems.geobot;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import static com.asc.msigeosystems.geobot.GBProjectSettings.sUIAngleDisplayDD;
import static com.asc.msigeosystems.geobot.GBProjectSettings.sUIAngleDisplayDM;
import static com.asc.msigeosystems.geobot.GBProjectSettings.sUIAngleDisplayDMS;
import static com.asc.msigeosystems.geobot.GBProjectSettings.sUIAngleDisplayGONS;
import static com.asc.msigeosystems.geobot.GBProjectSettings.sUIAngleDisplayMils;
import static com.asc.msigeosystems.geobot.GBProjectSettings.sUIDecimalDisplayCommas;
import static com.asc.msigeosystems.geobot.GBProjectSettings.sUIDecimalDisplayNoCommas;
import static com.asc.msigeosystems.geobot.GBProjectSettings.sUIDistanceIntFeet;
import static com.asc.msigeosystems.geobot.GBProjectSettings.sUIDistanceMeters;
import static com.asc.msigeosystems.geobot.GBProjectSettings.sUIDistanceUSFeet;


/**
 * The Project Settings Fragment is the UI
 * when the user customizes settings for this project
 * Created by Elisabeth Huhn on 4/13/2016.
 */
public class GBProjectSettingsFragment extends Fragment {



    //Arrays for enumerated types
    private CharSequence[] mDistanceTypes =
                            new CharSequence[]{sUIDistanceMeters, sUIDistanceUSFeet, sUIDistanceIntFeet};
    private CharSequence[] mDecimalDisplayTypes =
                            new CharSequence[] {sUIDecimalDisplayCommas, sUIDecimalDisplayNoCommas};
    /*
    private CharSequence[] mAngleUnitTypes =
                            new CharSequence[] {sUIAngleUnitsDeg, sUIAngleUnitsRad};
                            */
    private CharSequence[] mAngleDisplayTypes =
                            new CharSequence[]{sUIAngleDisplayDD, sUIAngleDisplayDM,
                                               sUIAngleDisplayDMS, sUIAngleDisplayGONS,
                                               sUIAngleDisplayMils};


/*
    //these same values are set in setDefaults()
    private int mDistanceUnits     = sDBDistanceMeters;
    private int mDecimalDisplay    = sDBDecimalDisplayCommas;
    private int mAngleUnits        = sDBAngleUnitsDeg;
    private int mAngleDisplay      = sDBAngleDisplayDD;
    private CharSequence mGridDirection     = "North Azimuth";
    private double       mScaleFactor       = .99982410;
    private CharSequence mSeaLevel          = "Off";
    private CharSequence mRefraction        = "Off";
    private CharSequence mDatum             = "NAD 1983 (2011)";
    private CharSequence mProjection        = "US State Plane Coordinates";
    private CharSequence mZone              = "Georgia West (1002)";
    private CharSequence mCoordinateDisplay = "North, East";
    private CharSequence mGeoidModel        = "GEOID99";
    private CharSequence mStartingPointID   = "1001";
    private CharSequence mAlphanumeric      = "Off";
    private CharSequence mFeatureCodes      = "RHM2";
    private CharSequence mFCControlFile     = "CP2";
    private CharSequence mFCTimeStamp       = "NO";
*/




    public GBProjectSettingsFragment() {
        //for now, we don't need to initialize anything when the fragment
        //  is first created
    }




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_project_settings_gb, container, false);


        //Wire up the UI widgets so they can handle events later
        wireWidgets(v);

        wireSpinners(v);


        //Initialize the fields with the defaults
        //set and display default values
        setDefaults();

        return v;

    }//end CreateView

    @Override
    public void onResume(){
        super.onResume();
        setSubtitle();
    }


    private void setSubtitle(){
        ((GBActivity)getActivity()).setSubtitle(R.string.subtitle_project_settings);
    }


    private void wireWidgets(View v){
/*
        //Project ID
        mProjectIDOutput = (TextView) v.findViewById(R.id.projectIDOutput);

        //Project Name
        mProjectNameOutput = (TextView) v.findViewById(R.id.projectNameOutput);


        //AngleUnits
        //mAngleUnitsInput = (EditText) v.findViewById(R.id.angleUnitsInput);

        //AngleDisplay
        mAngleDisplayInput = (EditText) v.findViewById(R.id.angleDisplayInput);

        //GridDirection
        mGridDirectionInput = (EditText) v.findViewById(R.id.gridDirectionInput);

        //ScaleFactor
        mScaleFactorInput = (EditText) v.findViewById(R.id.scaleFactorInput);

        //SeaLevel
        mSeaLevelInput = (EditText) v.findViewById(R.id.seaLevelInput);

        //Refraction
        mRefractionInput = (EditText) v.findViewById(R.id.refractionInput);

        //Datum
        mDatumInput = (EditText) v.findViewById(R.id.datumInput);

        //Projection
        mProjectionInput = (EditText) v.findViewById(R.id.projectionInput);

        //Zone
        mZoneInput = (EditText) v.findViewById(R.id.zoneInput);

        //CoordinateDisplay
        mCoordinateDisplayInput = (EditText) v.findViewById(R.id.coordinateDisplayInput);

        //GeoidModel
        mGeoidModelInput = (EditText) v.findViewById(R.id.geoidModelInput);

        //StartingPointID
        mStartingPointIDInput = (EditText) v.findViewById(R.id.startingPointIDInput);

        //Alphanumeric
        mAlphanumericInput = (EditText) v.findViewById(R.id.alphanumericInput);

        //FeatureCodes
        mFeatureCodesInput = (EditText) v.findViewById(R.id.featureCodesInput);

        //FCControlFile
        mFCControlFileInput = (EditText) v.findViewById(R.id.fcControlFileInput);

        //FCTimeStamp
        mFCTimeStampInput = (EditText) v.findViewById(R.id.fcTimeStampInput);
*/




        //Reset Defaults Button
        Button resetButton = (Button) v.findViewById(R.id.resetDefaultsButton);
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){

                //set the variables to their default values and display them
                setDefaults();
            }
        });


        //FOOTER WIDGETS


    }



    private void wireSpinners(View v){

        //defaults are already set in mPSBeingMaintained;

        //initialize the spinner
        Spinner spinnerDistUnits = (Spinner) v.findViewById(R.id.distance_units_spinner);

        // Create an ArrayAdapter using the Activities context AND
        // the string array and a default spinner layout
        ArrayAdapter<CharSequence> distUnitsAdapter = new ArrayAdapter<>(getActivity(),
                                                            android.R.layout.simple_spinner_item,
                                                            mDistanceTypes);

        // Specify the layout to use when the list of choices appears
        distUnitsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the spinner
        spinnerDistUnits.setAdapter(distUnitsAdapter);

        //Set the value of the spinner to the value in the object being maintained
        spinnerDistUnits.setSelection(getPSBeingMaintained().getDistanceUnits());

        //attach the listener to the spinner
        spinnerDistUnits.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                getPSBeingMaintained().setDistanceUnits(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });





        Spinner spinnerDecDisplay = (Spinner) v.findViewById(R.id.decimal_display_spinner);
        ArrayAdapter<CharSequence> decDispAdapter = new ArrayAdapter<>(getActivity(),
                                                            android.R.layout.simple_spinner_item,
                                                            mDecimalDisplayTypes);
        decDispAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDecDisplay.setAdapter(decDispAdapter);
        spinnerDecDisplay.setSelection(getPSBeingMaintained().getDecimalDisplay());
        spinnerDecDisplay.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                getPSBeingMaintained().setDecimalDisplay(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });




        Spinner spinnerAngleDisplay = (Spinner) v.findViewById(R.id.angle_units_spinner);
        ArrayAdapter<CharSequence> angDispAdapter = new ArrayAdapter<>(getActivity(),
                                                            android.R.layout.simple_spinner_item,
                                                            mAngleDisplayTypes);
        angDispAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAngleDisplay.setAdapter(angDispAdapter);
        spinnerAngleDisplay.setSelection(getPSBeingMaintained().getAngleDisplay());
        spinnerAngleDisplay.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                getPSBeingMaintained().setAngleDisplay(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });



    }


    private void setDefaults() {

        getPSBeingMaintained().setDefaults();
        //display the new defaults
        initializeUI();
    }

    private void initializeUI() {
        View v = getView();
        if (v == null)return;

        GBProjectSettings psSettings = getPSBeingMaintained();

        //Project ID
        TextView projectIDOutput = (TextView) v.findViewById(R.id.projectIDOutput);

        //Project Name
        TextView projectNameOutput = (TextView) v.findViewById(R.id.projectNameOutput);

        GBProject project = GBUtilities.getInstance().getOpenProject((GBActivity)getActivity());
        if (project != null) {
            projectIDOutput.setText(String.valueOf(project.getProjectID()));
            projectNameOutput.setText(String.valueOf(project.getProjectName()));
        }

        Spinner spinnerDistUnits = (Spinner) v.findViewById(R.id.distance_units_spinner);
        Spinner spinnerDecDisplay = (Spinner) v.findViewById(R.id.decimal_display_spinner);
        Spinner spinnerAngleDisplay = (Spinner) v.findViewById(R.id.angle_units_spinner);

        spinnerDistUnits.setSelection(psSettings.getDistanceUnits());
        spinnerDecDisplay.setSelection(psSettings.getDecimalDisplay());
        spinnerAngleDisplay.setSelection(psSettings.getAngleDisplay());


  /*
        mAngleDisplayInput.   setText(psSettings.getAngleDisplay());
        mGridDirectionInput.  setText(psSettings.getGridDirection());
        mScaleFactorInput.    setText(String.valueOf(psSettings.getScaleFactor()));

        mSeaLevelInput.       setText(psSettings.getSeaLevel());
        mRefractionInput.     setText(psSettings.getRefraction());
        mDatumInput.          setText(psSettings.getDatum());
        mProjectionInput.     setText(psSettings.getProjection());
        mZoneInput.           setText(psSettings.getZone());
        mCoordinateDisplayInput.setText(psSettings.getCoordinateDisplay());
        mGeoidModelInput.     setText(psSettings.getGeoidModel());
        mStartingPointIDInput.setText(psSettings.getStartingPointID());
        mAlphanumericInput.   setText(psSettings.isAlphanumericID());
        mFeatureCodesInput.   setText(psSettings.getFeatureCodes());
        mFCControlFileInput.  setText(psSettings.getFCControlFile());
        mFCTimeStampInput.    setText(psSettings.getFCTimeStamp());
*/

    }


    GBProjectSettings getPSBeingMaintained (){
        GBProject project = GBUtilities.getInstance().getOpenProject((GBActivity)getActivity());
        if (project == null)return null;
        GBProjectSettings psSettings = project.getSettings();
        if (psSettings == null){
            psSettings = new GBProjectSettings(); //filled with default values
            project = GBUtilities.getInstance().getOpenProject((GBActivity)getActivity());
            project.setSettings(psSettings);

        }
        return psSettings;
    }

}


