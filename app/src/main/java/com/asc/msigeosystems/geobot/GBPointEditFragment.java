package com.asc.msigeosystems.geobot;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;

import static com.asc.msigeosystems.geobot.R.color.colorNegNumber;
import static com.asc.msigeosystems.geobot.R.color.colorPosNumber;

/**
 * The Maintain Point Fragment
 * is passed a point on startup. The point attribute fields are
 * pre-populated prior to updating the point
 * Created by Elisabeth Huhn on 4/13/2016.
 */
public class GBPointEditFragment extends Fragment  {

    private static final String TAG = "EDIT_POINT_FRAGMENT";


    //**********************************************************/
    /*  Lat/Long Old values to determine if they have changed **/
    //**********************************************************/
    private String mPointLatitudeDDOld;
    private String mPointLatitudeDOld;
    private String mPointLatitudeMOld;
    private String mPointLatitudeSOld;

    private String mPointLongitudeDDOld;
    private String mPointLongitudeDOld;
    private String mPointLongitudeMOld;
    private String mPointLongitudeSOld;

    private String mPointElevationMetersOld;
    private String mPointElevationFeetOld;
    private String mPointGeoidMetersOld;
    private String mPointGeoidFeetOld;



    //**********************************************************/
    /*    E/N Old values to determine if they have changed    **/
    //**********************************************************/
    private String       mPointEastingMetersOld;
    private String       mPointNorthingMetersOld;
    private String       mPointEastingFeetOld;
    private String       mPointNorthingFeetOld;
    private String       mPointENElevationMetersOld;
    private String       mPointENElevationFeetOld;
/*
    private String       mPointZoneOld;
    private String       mPointHemisphereOld;
    private String       mPointLatbandOld;
    private String       mPointConvergenceOld;
    private String       mPointScaleFactorOld;
    private String       mPointElevationOld;
*/
    //**********************************************************/
    //*****         Recycler View Widgets             **********/
    //**********************************************************/

    private ImageView mPictureImage;



    //**********************************************************/
    //*********  Point Attribute Variables      ****************/
    //**********************************************************/

    private GBPoint mPointBeingMaintained;




    //**********************************************************/
    //*********  Lat/Long Coordinate Variables  ****************/
    //**********************************************************/

    //**********************************************************/
    //*********  E/N Coordinate Variables       ****************/
    //**********************************************************/


    //**********************************************************/
    //*********  Other Variables                ****************/
    //**********************************************************/


    private boolean      mPointChanged = false;
    private CharSequence mPointPath;




    //**********************************************************/
    //*********  Constructor                    ****************/
    //**********************************************************/

    public GBPointEditFragment() {
        //for now, we don't need to initialize anything when the fragment
        //  is first created
    }


    //**********************************************************/
    //*********  Lifecycle Methods                   ***********/
    //**********************************************************/

    public static GBPointEditFragment newInstance(GBPath    pointPath,
                                                  GBPoint   point) {

        Bundle args = new Bundle();
        args = GBPoint.putPointInArguments(args, point);
        args = GBPath.putPathInArguments(args, pointPath);

        GBPointEditFragment fragment = new GBPointEditFragment();

        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState){

        super.onCreate(savedInstanceState);

        GBPath path = GBPath.getPathFromArguments(getArguments());
        mPointPath  = path.getPath();

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
        //the point number is not incremented until the point is saved for the first time
        //The SQL Helper is in charge of assigning both
        // the DB ID and then incrementing the point number
        //openProject.incrementPointNumber((GBActivity)getActivity());
        return;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_point_edit_gb, container, false);


        //Wire up the UI widgets so they can handle events later
        //For now ignore the text view widgets, as this is just a mockup
        //      for the real screen we'll have to actually fill the fields
        wireWidgets(v);

        int coordinateWidgetType = wireCoordinateWidgets(v);


        initializeUI(v);

        //initializeRecyclerView(v);

        if (coordinateWidgetType == GBCoordinate.sLLWidgets){
            saveLLFieldsAsOldValues();
        }else if (coordinateWidgetType == GBCoordinate.sENWidgets) {
            saveENFieldsAsOldValues();
        }

        //Show the subtitle for this fragment
        ((GBActivity) getActivity()).setSubtitle(getString(R.string.subtitle_edit_point));

        return v;
    }

    @Override
    public void onResume(){
        super.onResume();
        setSubtitle();
    }



    //**********************************************************/
    //*********     Initialization                   ***********/
    //**********************************************************/

    private void setSubtitle(){
        ((GBActivity)getActivity()).setSubtitle(R.string.subtitle_edit_point);
    }

    private void wireWidgets(View v){
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
                //This tells you that text is about to change.
                // Starting at character "start", the next "count" characters
                // will be changed with "after" number of characters

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                //This tells you where the text has changed
                //Starting at character "start", the "before" number of characters
                // has been replaced with "count" number of characters

            }

            @Override
            public void afterTextChanged(Editable editable) {
                //This tells you that somewhere within editable, it's text has changed
                setPointChangedFlags();
            }
        };

        //Project ID
        EditText pointProjectIDInput = (EditText) v.findViewById(R.id.pointProjectIDInput);
        //Can't change the project of a point
        pointProjectIDInput.setFocusable(false);

        //Project name
        EditText pointProjectNameInput = (EditText) v.findViewById(R.id.pointProjectNameInput);
        //Can't change the project of a point
        pointProjectNameInput.setFocusable(false);



        //Point ID
        EditText pointIDInput = (EditText) v.findViewById(R.id.pointIDInput);
        pointIDInput.setFocusable(false);

        //Point Number
        EditText pointNumberInput = (EditText) v.findViewById(R.id.pointNumInput);
        pointNumberInput.setFocusable(true);
        pointNumberInput.setEnabled(true);
        pointNumberInput.addTextChangedListener(textWatcher);


        //Point Description
        EditText pointFeatureCodeInput = (EditText) v.findViewById(R.id.pointFeatureCodeInput);
        pointFeatureCodeInput.addTextChangedListener(textWatcher);



        //Point Notes
        EditText pointNotesInput = (EditText) v.findViewById(R.id.pointNotesInput);
        pointNotesInput.addTextChangedListener(textWatcher);


        //Point Quality HDOP
        EditText pointHdopInput = (EditText) v.findViewById(R.id.pointHdopInput);
        pointHdopInput.addTextChangedListener(textWatcher);


        //Point Quality VDOP
        EditText pointVdopInput = (EditText) v.findViewById(R.id.pointVdopInput);
        pointVdopInput.addTextChangedListener(textWatcher);


        //Point Quality TDOP
        EditText pointTdopInput = (EditText) v.findViewById(R.id.pointTdopInput);
        pointTdopInput.addTextChangedListener(textWatcher);


        //Point Quality PDOP
        EditText pointPdopInput = (EditText) v.findViewById(R.id.pointPdopInput);
        pointPdopInput.addTextChangedListener(textWatcher);


        //Point Quality HRMS
        EditText pointHrmsInput = (EditText) v.findViewById(R.id.pointHrmsInput);
        pointHrmsInput.addTextChangedListener(textWatcher);


        //Point Quality VRMS
        EditText pointVrmsInput = (EditText) v.findViewById(R.id.pointVrmsInput);
        pointVrmsInput.addTextChangedListener(textWatcher);



        //Point Offset Distance
        EditText pointOffsetDistInput = (EditText) v.findViewById(R.id.pointOffDistInput);
        pointOffsetDistInput.addTextChangedListener(textWatcher);


        //Point Offset Heading
        EditText pointOffsetHeadInput = (EditText) v.findViewById(R.id.pointOffHeadInput);
        pointOffsetHeadInput.addTextChangedListener(textWatcher);


        //Point Offset Elevation
        EditText pointOffsetEleInput = (EditText) v.findViewById(R.id.pointOffEleInput);
        pointOffsetEleInput.addTextChangedListener(textWatcher);


        //Point Height
        EditText pointHeightInput = (EditText) v.findViewById(R.id.pointHeightInput);
        pointHeightInput.setEnabled(true);
        pointHeightInput.addTextChangedListener(textWatcher);


        //Coordinate type
        //TextView pointCoordinateTypeLabel = (TextView)v.findViewById(R.id.coordinate_label);



        //Save Changes Button
        Button pointSaveChangesButton = (Button) v.findViewById(R.id.pointSaveChangesButton);
        //button is enabled once something changes
        pointSaveChangesButton.setEnabled(false);
        pointSaveChangesButton.setTextColor(Color.GRAY);
        pointSaveChangesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){

                onSave();
            }
        });


        //Measure Button
        Button pointMeasureButton = (Button) v.findViewById(R.id.pointMeasureButton);
        pointMeasureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                //hide the keyboard if it is visible
                GBUtilities utilities = GBUtilities.getInstance();
                utilities.hideKeyboard(getActivity());

                //If the project has not yet been saved, save it before measure
                if (mPointBeingMaintained.getPointID() == GBUtilities.ID_DOES_NOT_EXIST) {
                    onSave();
                }

                if (mPointPath.equals(GBPath.sEditFromMaps)){
                    //pop back to the Collect Points Screen
                    ((GBActivity)getActivity()).popToScreen(GBActivity.sCollectPointsTag);
                } else {
                    ((GBActivity) getActivity()).switchToMeasureScreen(mPointBeingMaintained);
                }
            }
        });



        //View Existing Points Button
        Button pointViewExistingButton = (Button) v.findViewById(R.id.pointViewExistingButton);
        if ((mPointPath.equals(GBPath.sCreateTag)) || (mPointPath.equals(GBPath.sEditFromMaps))) {
            //disable the button on the create path OR the edit from maps path
            pointViewExistingButton.setEnabled(false);
            pointViewExistingButton.setTextColor(Color.GRAY);
        }
        pointViewExistingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){

                onListPoints();
           }
        });


        //View Raw Points Button
        Button pointViewRawButton = (Button) v.findViewById(R.id.pointViewRawButton);
        pointViewRawButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){

                onListRawPoints();
            }
        });
        GBMeanToken token = mPointBeingMaintained.getMeanToken();
        if (token != null){
            int nbRaw = token.getFixedReadings();
            if (nbRaw == 0){
                pointViewRawButton.setTextColor(Color.GRAY);
                pointViewRawButton.setEnabled(false);
            } else {
                pointViewRawButton.setTextColor(Color.BLACK);
                pointViewRawButton.setEnabled(true);
            }
        } else {
            pointViewRawButton.setTextColor(Color.GRAY);
            pointViewRawButton.setEnabled(false);
        }


        //mPictureImage = (ImageView) v.findViewById(R.id.pictureImage);
    }

    private int wireCoordinateWidgets(View v){

        //Need to add widgets for the coordinates, but there are different widgets
        //depending upon the type of coordinate: Latitude/Longitude or Easting/Northing

        //Regardless, we need the inflater and the LinearLayout container for the coordinates
        //Get an inflater
        LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
        //Get the LinearLayout which will contain the coordinates
        LinearLayout   coordinatesContainer    = (LinearLayout) v.findViewById(R.id.point_coordinate_container);

        //get the coordinate type out of the project
        int coordinateWidgetType = getCoordinateTypeFromProject();

        if (coordinateWidgetType == GBCoordinate.sLLWidgets){
            //inflate the coordinates and
            // attach the coordinates to the LinearLayout which exists to contain the coordinates
            layoutInflater.inflate(R.layout.element_coordinate_ll, coordinatesContainer, true);
            //then wire up the appropriate widgets for this type of coordinate
            wireLLCoordinateWidgets(v);
        } else if (coordinateWidgetType == GBCoordinate.sENWidgets) {
            //inflate the coordinates, and
            // attach the coordinates to the LinearLayout which exists to contain the coordinates
            layoutInflater.inflate(R.layout.element_coordinate_en, coordinatesContainer, true);

            //then wire up the appropriate widgets for this type of coordinat
            wireENCoordinateWidgets(v);
        }
        return coordinateWidgetType;
    }

    private void wireLLCoordinateWidgets(View v){

        //make DD vs DMS on the project work by making various views invisible
        GBProject openProject = GBUtilities.getInstance().getOpenProject((GBActivity)getActivity());
        boolean isDD = false;
        if (openProject.getDDvDMS() == GBProject.sDD)isDD = true;


        int distUnits = openProject.getDistanceUnits();


        //***************************************************************************/
        //*******             Latitude                                       ********/
        //***************************************************************************/
        View field_container;
        TextView label;

        //set up the UI widgets for the latitude/longetude coordinates
        field_container = v.findViewById(R.id.latitudeContainer);
        label = (TextView)field_container.findViewById(R.id.ll_label);
        label.setText(getString(R.string.latitude_label));
        final EditText pointLatitudeDDInput = (EditText) field_container.findViewById(R.id.ll_dd_input);
        pointLatitudeDDInput.setInputType(InputType.TYPE_CLASS_NUMBER |
                                           InputType.TYPE_NUMBER_FLAG_DECIMAL |
                                           InputType.TYPE_NUMBER_FLAG_SIGNED);

        final EditText pointLatitudeDInput  = (EditText) field_container.findViewById(R.id.ll_d_Input) ;
        final EditText pointLatitudeMInput  = (EditText) field_container.findViewById(R.id.ll_m_Input);
        final EditText pointLatitudeSInput  = (EditText) field_container.findViewById(R.id.ll_s_Input) ;
        pointLatitudeSInput.setInputType(InputType.TYPE_CLASS_NUMBER |
                                          InputType.TYPE_NUMBER_FLAG_DECIMAL |
                                          InputType.TYPE_NUMBER_FLAG_SIGNED);


        //Can't change the coordinate if coming from maps
        if (mPointPath.equals(GBPath.sEditFromMaps)){
            pointLatitudeDDInput.setFocusable(false);
            pointLatitudeDDInput.setBackgroundColor(
                                        ContextCompat.getColor(getActivity(), R.color.colorGray));
            pointLatitudeDInput.setFocusable(false);
            pointLatitudeDInput.setBackgroundColor(
                                        ContextCompat.getColor(getActivity(), R.color.colorGray));
            pointLatitudeMInput.setFocusable(false);
            pointLatitudeMInput.setBackgroundColor(
                                        ContextCompat.getColor(getActivity(), R.color.colorGray));
            pointLatitudeSInput.setFocusable(false);
            pointLatitudeSInput.setBackgroundColor(
                                        ContextCompat.getColor(getActivity(), R.color.colorGray));
        }


        pointLatitudeDDInput.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus) {
                    //Digital Degree just lost focus,
                    //has the value changed?
                    String temp = pointLatitudeDDInput.getText().toString();
                    if (!temp.equals(mPointLatitudeDDOld)){
                        setPointChangedFlags();
                        // so convert Latitude DD to DMS
                        boolean isLatitude = true;
                        GBCoordinateLL.convertDDtoDMS( getActivity(),
                                                        pointLatitudeDDInput,
                                                        pointLatitudeDInput,
                                                        pointLatitudeMInput,
                                                        pointLatitudeSInput,
                                                        isLatitude);
                        mPointLatitudeDOld = pointLatitudeDInput.getText().toString();
                        mPointLatitudeMOld = pointLatitudeMInput.getText().toString();
                        mPointLatitudeSOld = pointLatitudeSInput.getText().toString();
                        mPointLatitudeDDOld = temp;
                    }

                }
            }
        });
        pointLatitudeDInput.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus) {
                    //Degree just lost focus,
                    //has the value changed?
                    String tempD = pointLatitudeDInput.getText().toString();
                    String tempM = pointLatitudeMInput.getText().toString();
                    String tempS = pointLatitudeSInput.getText().toString();
                    if (!tempD.equals(mPointLatitudeDOld)){
                        setPointChangedFlags();
                        //only convert if all three fields have values
                        if ((!tempD.isEmpty()) && (!tempM.isEmpty()) && (!tempS.isEmpty())){
                            // so convert Latitude DMS to DD
                            boolean isLatitude = true;
                            GBCoordinateLL.convertDMStoDD( getActivity(),
                                                            pointLatitudeDDInput,
                                                            pointLatitudeDInput,
                                                            pointLatitudeMInput,
                                                            pointLatitudeSInput,
                                                            isLatitude);
                            mPointLatitudeDDOld = pointLatitudeDDInput.getText().toString();
                            mPointLatitudeDOld = tempD;
                        }

                    }

                }
            }
        });
        pointLatitudeMInput.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus) {
                    //Minute just lost focus,
                    //has the value changed?
                    String tempD = pointLatitudeDInput.getText().toString();
                    String tempM = pointLatitudeMInput.getText().toString();
                    String tempS = pointLatitudeSInput.getText().toString();
                    if (!tempM.equals(mPointLatitudeMOld)){
                        setPointChangedFlags();
                        //only convert if all three fields have values
                        if ((!tempD.isEmpty()) && (!tempM.isEmpty()) && (!tempS.isEmpty())) {
                            // so convert Latitude DMS to DD
                            boolean isLatitude = true;
                            GBCoordinateLL.convertDMStoDD(getActivity(),
                                                            pointLatitudeDDInput,
                                                            pointLatitudeDInput,
                                                            pointLatitudeMInput,
                                                            pointLatitudeSInput,
                                                            isLatitude);
                            mPointLatitudeDDOld = pointLatitudeDDInput.getText().toString();
                            mPointLatitudeMOld = tempM;
                        }
                    }

                }
            }
        });
         pointLatitudeSInput.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus) {
                    //Seconds just lost focus,
                    //has the value changed?
                    String tempD = pointLatitudeDInput.getText().toString();
                    String tempM = pointLatitudeMInput.getText().toString();
                    String tempS = pointLatitudeSInput.getText().toString();
                    if (!tempS.equals(mPointLatitudeSOld)){
                        setPointChangedFlags();
                        //only convert if all three fields have values
                        if ((!tempD.isEmpty()) && (!tempM.isEmpty()) && (!tempS.isEmpty())) {
                            // so convert Latitude DMS to DD
                            boolean isLatitude = true;
                            GBCoordinateLL.convertDMStoDD(getActivity(),
                                                            pointLatitudeDDInput,
                                                            pointLatitudeDInput,
                                                            pointLatitudeMInput,
                                                            pointLatitudeSInput,
                                                            isLatitude);
                            mPointLatitudeDDOld = pointLatitudeDDInput.getText().toString();
                            mPointLatitudeSOld = tempS;
                        }
                    }

                }
            }
        });


        //***************************************************************************/
        //*******             Longitude                                      ********/
        //***************************************************************************/


        field_container = v.findViewById(R.id.longitudeContainer);
        label = (TextView)field_container.findViewById(R.id.ll_label);
        label.setText(getString(R.string.longitude_label));
        final EditText pointLongitudeDDInput = (EditText) field_container.findViewById(R.id.ll_dd_input);
        pointLongitudeDDInput.setInputType(InputType.TYPE_CLASS_NUMBER |
                                            InputType.TYPE_NUMBER_FLAG_DECIMAL |
                                            InputType.TYPE_NUMBER_FLAG_SIGNED);

        final EditText pointLongitudeDInput  = (EditText) field_container.findViewById(R.id.ll_d_Input) ;
        final EditText pointLongitudeMInput  = (EditText) field_container.findViewById(R.id.ll_m_Input);
        final EditText pointLongitudeSInput  = (EditText) field_container.findViewById(R.id.ll_s_Input) ;
        pointLongitudeSInput.setInputType( InputType.TYPE_CLASS_NUMBER |
                                            InputType.TYPE_NUMBER_FLAG_DECIMAL |
                                            InputType.TYPE_NUMBER_FLAG_SIGNED);


        if (mPointPath.equals(GBPath.sEditFromMaps)){
            pointLongitudeDDInput.setFocusable(false);
            pointLongitudeDDInput.setBackgroundColor(
                                        ContextCompat.getColor(getActivity(), R.color.colorGray));
            pointLongitudeDInput.setFocusable(false);
            pointLongitudeDInput.setBackgroundColor(
                                        ContextCompat.getColor(getActivity(), R.color.colorGray));
            pointLongitudeMInput.setFocusable(false);
            pointLongitudeMInput.setBackgroundColor(
                                        ContextCompat.getColor(getActivity(), R.color.colorGray));
            pointLongitudeSInput.setFocusable(false);
            pointLongitudeSInput.setBackgroundColor(
                                        ContextCompat.getColor(getActivity(), R.color.colorGray));
        }


        pointLongitudeDDInput.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus) {
                    //Digital Degree just lost focus,
                    //has the value changed?
                    String temp = pointLongitudeDDInput.getText().toString();
                    if (!temp.equals(mPointLongitudeDDOld)){
                        setPointChangedFlags();
                        // so convert Longitude DD to DMS
                        boolean isLatitude = false;
                        GBCoordinateLL.convertDDtoDMS( getActivity(),
                                                        pointLongitudeDDInput,
                                                        pointLongitudeDInput,
                                                        pointLongitudeMInput,
                                                        pointLongitudeSInput,
                                                        isLatitude);

                        mPointLongitudeDDOld = temp;
                        mPointLongitudeDOld  = pointLongitudeDInput.getText().toString();
                        mPointLongitudeMOld  = pointLongitudeMInput.getText().toString();
                        mPointLongitudeSOld  = pointLongitudeSInput.getText().toString();
                    }

                }
            }
        });

        pointLongitudeDInput.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus) {
                    //Degree just lost focus,
                    //has the value changed?
                    String tempD = pointLongitudeDInput.getText().toString();
                    String tempM = pointLongitudeMInput.getText().toString();
                    String tempS = pointLongitudeSInput.getText().toString();
                    if (!tempD.equals(mPointLongitudeDOld)){
                        setPointChangedFlags();
                        //only convert if all three fields have values
                        if ((!tempD.isEmpty()) && (!tempM.isEmpty()) && (!tempS.isEmpty())) {
                            // so convert Longitude DMS to DD
                            boolean isLatitude = false;
                            GBCoordinateLL.convertDMStoDD(getActivity(),
                                    pointLongitudeDDInput,
                                    pointLongitudeDInput,
                                    pointLongitudeMInput,
                                    pointLongitudeSInput,
                                    isLatitude);
                            mPointLongitudeDOld = tempD;
                            mPointLongitudeDDOld = pointLongitudeDDInput.getText().toString();
                        }
                    }

                }
            }
        });


         pointLongitudeMInput.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus) {
                    //Minute just lost focus,
                    //has the value changed?
                    String tempD = pointLongitudeDInput.getText().toString();
                    String tempM = pointLongitudeMInput.getText().toString();
                    String tempS = pointLongitudeSInput.getText().toString();
                    if (!tempM.equals(mPointLongitudeMOld)){
                        setPointChangedFlags();
                        //only convert if all three fields have values
                        if ((!tempD.isEmpty()) && (!tempM.isEmpty()) && (!tempS.isEmpty())) {
                            // so convert Longitude DMS to DD
                            boolean isLatitude = false;
                            GBCoordinateLL.convertDMStoDD(getActivity(),
                                    pointLongitudeDDInput,
                                    pointLongitudeDInput,
                                    pointLongitudeMInput,
                                    pointLongitudeSInput,
                                    isLatitude);
                            mPointLongitudeMOld = tempM;
                            mPointLongitudeDDOld = pointLongitudeDDInput.getText().toString();
                        }
                    }

                }
            }
        });


        pointLongitudeSInput.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus) {
                    //Second just lost focus,
                    //has the value changed?
                    String tempD = pointLongitudeDInput.getText().toString();
                    String tempM = pointLongitudeMInput.getText().toString();
                    String tempS = pointLongitudeSInput.getText().toString();
                    if (!tempS.equals(mPointLongitudeSOld)){
                        setPointChangedFlags();
                        //only convert if all three fields have values
                        if ((!tempD.isEmpty()) && (!tempM.isEmpty()) && (!tempS.isEmpty())) {
                            // so convert Longitude DMS to DD
                            boolean isLatitude = false;
                            GBCoordinateLL.convertDMStoDD(getActivity(),
                                    pointLongitudeDDInput,
                                    pointLongitudeDInput,
                                    pointLongitudeMInput,
                                    pointLongitudeSInput,
                                    isLatitude);
                            mPointLongitudeSOld = tempS;
                            mPointLongitudeDDOld = pointLongitudeDDInput.getText().toString();
                        }
                    }

                }
            }
        });


        if (isDD){
            pointLatitudeDInput.setVisibility(View.GONE);
            pointLatitudeMInput.setVisibility(View.GONE);
            pointLatitudeSInput.setVisibility(View.GONE);

            pointLongitudeDInput.setVisibility(View.GONE);
            pointLongitudeMInput.setVisibility(View.GONE);
            pointLongitudeSInput.setVisibility(View.GONE);
        } else {
            pointLatitudeDDInput.setVisibility(View.GONE);
            pointLongitudeDDInput.setVisibility(View.GONE);
        }


        //***************************************************************************/
        //*******             Elevation                                      ********/
        //***************************************************************************/

        field_container = v.findViewById(R.id.elevationGeoidContainer);
        final EditText pointElevationMetersInput =
                (EditText) field_container.findViewById(R.id.elevationMetersInput);
        pointElevationMetersInput.setInputType(InputType.TYPE_CLASS_NUMBER |
                                               InputType.TYPE_NUMBER_FLAG_DECIMAL |
                                               InputType.TYPE_NUMBER_FLAG_SIGNED);
        final EditText pointElevationFeetInput   =
                (EditText) field_container.findViewById(R.id.elevationFeetInput) ;
        pointElevationFeetInput.setInputType(InputType.TYPE_CLASS_NUMBER |
                                             InputType.TYPE_NUMBER_FLAG_DECIMAL |
                                             InputType.TYPE_NUMBER_FLAG_SIGNED);


        if (mPointPath.equals(GBPath.sEditFromMaps)) {

            pointElevationMetersInput.setFocusable(false);
            pointElevationMetersInput.setBackgroundColor(
                                        ContextCompat.getColor(getActivity(), R.color.colorGray));
            pointElevationFeetInput.setFocusable(false);
            pointElevationFeetInput.setBackgroundColor(
                                        ContextCompat.getColor(getActivity(), R.color.colorGray));

        }

        pointElevationMetersInput.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus) {
                    //Meters just lost focus,
                    // has the value changed
                    String temp = pointElevationMetersInput.getText().toString();
                    if (!temp.equals(mPointElevationMetersOld)){
                        setPointChangedFlags();
                        // so convert Meters to Feet
                        GBUtilities.convertMetersToFeet(getActivity(),
                                                         pointElevationMetersInput,
                                                         pointElevationFeetInput);
                        mPointElevationMetersOld = temp;
                        mPointElevationFeetOld = pointElevationFeetInput.getText().toString();
                    }

                }
            }
        });

        pointElevationFeetInput.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus) {
                    //Feet just lost focus,
                    // has the value changed
                    String temp = pointElevationFeetInput.getText().toString();
                    if (!temp.equals(mPointElevationFeetOld)){
                        setPointChangedFlags();
                        // so convert Feet to Meters
                        GBUtilities.convertFeetToMeters(getActivity(),
                                                         pointElevationMetersInput,
                                                         pointElevationFeetInput);
                        mPointElevationFeetOld = temp;
                        mPointElevationMetersOld = pointElevationMetersInput.getText().toString();
                    }

                }
            }
        });


        //***************************************************************************/
        //*******             Geoid                                          ********/
        //***************************************************************************/
        final EditText pointGeoidMetersInput =
                            (EditText) field_container.findViewById(R.id.geoidHeightMetersInput);
        pointGeoidMetersInput.setInputType(InputType.TYPE_CLASS_NUMBER |
                                            InputType.TYPE_NUMBER_FLAG_DECIMAL |
                                            InputType.TYPE_NUMBER_FLAG_SIGNED);
        final EditText pointGeoidFeetInput   =
                            (EditText) field_container.findViewById(R.id.geoidHeightFeetInput);
        pointGeoidFeetInput.setInputType(InputType.TYPE_CLASS_NUMBER |
                                        InputType.TYPE_NUMBER_FLAG_DECIMAL |
                                        InputType.TYPE_NUMBER_FLAG_SIGNED);

        if (mPointPath.equals(GBPath.sEditFromMaps)) {

            pointGeoidMetersInput.setFocusable(false);
            pointGeoidMetersInput.setBackgroundColor(
                                        ContextCompat.getColor(getActivity(), R.color.colorGray));
            pointGeoidFeetInput.setFocusable(false);
            pointGeoidFeetInput.setBackgroundColor(
                                        ContextCompat.getColor(getActivity(), R.color.colorGray));

        }

        pointGeoidMetersInput.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus) {
                    //Meters just lost focus,
                    // has the value changed
                    String temp = pointGeoidMetersInput.getText().toString();
                    if (!temp.equals(mPointGeoidMetersOld)){
                        setPointChangedFlags();
                        // so convert Meters to Feet
                        GBUtilities.convertMetersToFeet(getActivity(),
                                                         pointGeoidMetersInput,
                                                         pointGeoidFeetInput);
                        mPointGeoidMetersOld = temp;
                        mPointGeoidFeetOld = pointGeoidFeetInput.getText().toString();
                    }
                }
            }
        });

        pointGeoidFeetInput.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus) {
                    //Feet just lost focus,
                    // has the value changed
                    String temp = pointGeoidFeetInput.getText().toString();
                    if (!temp.equals(mPointGeoidFeetOld)){
                        setPointChangedFlags();
                        // so convert Feet to Meters
                        GBUtilities.convertFeetToMeters(getActivity(),
                                                         pointGeoidMetersInput,
                                                         pointGeoidFeetInput);
                        mPointGeoidFeetOld = temp;
                        mPointGeoidMetersOld = pointGeoidMetersInput.getText().toString();
                    }
                }
            }
        });


        if (distUnits == GBProject.sMeters){
            pointElevationFeetInput.setVisibility(View.GONE);
            pointGeoidFeetInput    .setVisibility(View.GONE);

        } else {
            pointElevationMetersInput.setVisibility(View.GONE);
            pointGeoidMetersInput    .setVisibility(View.GONE);

        }



        field_container = v.findViewById(R.id.convergenceContainer);
        EditText pointConvergenceInput = (EditText) field_container.findViewById(R.id.convergenceOutput);
        EditText pointCAdegInput       = (EditText) v.findViewById(R.id.convDegreesInput);
        EditText pointCAminInput       = (EditText) v.findViewById(R.id.convMinutesInput);
        EditText pointCAsecInput       = (EditText) v.findViewById(R.id.convSecondsInput);
        EditText pointScaleFactorInput = (EditText) field_container.findViewById(R.id.scaleFactorOutput);

        if (isDD){
            pointCAdegInput.setVisibility(View.GONE);
            pointCAminInput.setVisibility(View.GONE);
            pointCAsecInput.setVisibility(View.GONE);
        } else {
            pointConvergenceInput.setVisibility(View.GONE);
        }


    }


    private void wireENCoordinateWidgets(View v){

        //make DD vs DMS on the project work by making various views invisible
        GBProject openProject = GBUtilities.getInstance().getOpenProject((GBActivity)getActivity());
        boolean isDD = false;
        if (openProject.getDDvDMS() == GBProject.sDD)isDD = true;

        int distUnits = openProject.getDistanceUnits();


        View field_container;
        TextView label;

        GBActivity myActivity = (GBActivity)getActivity();


        //set up the widgets for the easting/northing coordinate


        field_container = v.findViewById(R.id.eastingContainer);
        label = (TextView) field_container.findViewById(R.id.en_label) ;
        label.setText(getString(R.string.easting_label));
        final EditText pointEastingMetersInput =
                                        (EditText)field_container.findViewById(R.id.metersOutput);
        pointEastingMetersInput.setInputType(InputType.TYPE_CLASS_NUMBER |
                                              InputType.TYPE_NUMBER_FLAG_DECIMAL |
                                              InputType.TYPE_NUMBER_FLAG_SIGNED);

        final EditText pointEastingFeetInput   =
                                        (EditText) field_container.findViewById(R.id.feetOutput);
        pointEastingFeetInput.setInputType(InputType.TYPE_CLASS_NUMBER |
                                            InputType.TYPE_NUMBER_FLAG_DECIMAL |
                                            InputType.TYPE_NUMBER_FLAG_SIGNED);


        pointEastingMetersInput.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus) {
                    //Meters just lost focus,
                    // has the value changed
                    String temp = pointEastingMetersInput.getText().toString();
                    if (!temp.equals(mPointEastingMetersOld)){
                        setPointChangedFlags();
                        // so convert Meters to Feet
                        GBUtilities.convertMetersToFeet(getActivity(),
                                                         pointEastingMetersInput,
                                                         pointEastingFeetInput);
                        mPointEastingMetersOld = temp;
                        mPointEastingFeetOld = pointEastingFeetInput.getText().toString();
                    }

                }
            }
        });



        pointEastingFeetInput.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus) {
                    //Feet just lost focus,
                    // has the value changed
                    String temp = pointEastingFeetInput.getText().toString();
                    if (!temp.equals(mPointEastingFeetOld)){
                        setPointChangedFlags();
                        // so convert Feet to Meters
                        GBUtilities.convertFeetToMeters(getActivity(),
                                                         pointEastingMetersInput,
                                                         pointEastingFeetInput);
                        mPointEastingFeetOld = temp;
                        mPointEastingMetersOld = pointEastingMetersInput.getText().toString();
                    }

                }
            }
        });

        if (mPointPath.equals(GBPath.sEditFromMaps)) {

            pointEastingMetersInput.setFocusable(false);
            pointEastingMetersInput.setBackgroundColor(
                    ContextCompat.getColor(getActivity(), R.color.colorGray));
            pointEastingFeetInput.setFocusable(false);
            pointEastingFeetInput.setBackgroundColor(
                    ContextCompat.getColor(getActivity(), R.color.colorGray));

        }



        field_container = v.findViewById(R.id.northingContainer);
        label = (TextView) field_container.findViewById(R.id.en_label) ;
        label.setText(getString(R.string.northing_label));
        final EditText pointNorthingMetersInput = (EditText)field_container.findViewById(R.id.metersOutput);
        pointNorthingMetersInput.setInputType(InputType.TYPE_CLASS_NUMBER |
                                            InputType.TYPE_NUMBER_FLAG_DECIMAL |
                                            InputType.TYPE_NUMBER_FLAG_SIGNED);
        final EditText pointNorthingFeetInput   = (EditText)field_container.findViewById(R.id.feetOutput);
        pointNorthingFeetInput.setInputType(InputType.TYPE_CLASS_NUMBER |
                                            InputType.TYPE_NUMBER_FLAG_DECIMAL |
                                            InputType.TYPE_NUMBER_FLAG_SIGNED);

        pointNorthingMetersInput.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus) {
                    //Meters just lost focus,
                    // has the value changed
                    String temp = pointNorthingMetersInput.getText().toString();
                    if (!temp.equals(mPointNorthingMetersOld)){
                        setPointChangedFlags();
                        // so convert Meters to Feet
                        GBUtilities.convertMetersToFeet(getActivity(),
                                                         pointNorthingMetersInput,
                                                         pointNorthingFeetInput);
                        mPointNorthingMetersOld = temp;
                        mPointNorthingFeetOld = pointNorthingFeetInput.getText().toString();
                    }

                }
            }
        });

        pointNorthingFeetInput.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus) {
                    //Feet just lost focus,
                    // has the value changed
                    String temp = pointNorthingFeetInput.getText().toString();
                    if (!temp.equals(mPointNorthingFeetOld)){
                        setPointChangedFlags();
                        // so convert Feet to Meters
                        GBUtilities.convertFeetToMeters(getActivity(),
                                                         pointNorthingMetersInput,
                                                         pointNorthingFeetInput);
                        mPointNorthingFeetOld = temp;
                        mPointNorthingMetersOld = pointNorthingMetersInput.getText().toString();
                    }

                }
            }
        });

        if (mPointPath.equals(GBPath.sEditFromMaps)) {

            pointNorthingMetersInput.setFocusable(false);
            pointNorthingMetersInput.setBackgroundColor(
                    ContextCompat.getColor(getActivity(), R.color.colorGray));
            pointNorthingFeetInput.setFocusable(false);
            pointNorthingFeetInput.setBackgroundColor(
                    ContextCompat.getColor(getActivity(), R.color.colorGray));

        }



        field_container = v.findViewById(R.id.elevationContainer);
        final EditText pointENElevationMetersInput = (EditText)field_container.findViewById(R.id.elevationMetersInput);
        pointENElevationMetersInput.setInputType(InputType.TYPE_CLASS_NUMBER |
                                                InputType.TYPE_NUMBER_FLAG_DECIMAL |
                                                InputType.TYPE_NUMBER_FLAG_SIGNED);
        final EditText pointENElevationFeetInput   = (EditText)field_container.findViewById(R.id.elevationFeetInput);
        pointENElevationFeetInput.setInputType(InputType.TYPE_CLASS_NUMBER |
                                                InputType.TYPE_NUMBER_FLAG_DECIMAL |
                                                InputType.TYPE_NUMBER_FLAG_SIGNED);

        pointENElevationMetersInput.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus) {
                    //Meters just lost focus,
                    // has the value changed
                    String temp = pointENElevationMetersInput.getText().toString();
                    if (!temp.equals(mPointENElevationMetersOld)){
                        setPointChangedFlags();
                        // so convert Meters to Feet
                        GBUtilities.convertMetersToFeet(getActivity(),
                                                         pointENElevationMetersInput,
                                                         pointENElevationFeetInput);
                        mPointENElevationMetersOld = temp;
                        mPointENElevationFeetOld = pointENElevationFeetInput.getText().toString();
                    }

                }
            }
        });


        pointENElevationFeetInput.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus) {
                    //Feet just lost focus,
                    // has the value changed
                    String temp = pointENElevationFeetInput.getText().toString();
                    if (!temp.equals(mPointENElevationFeetOld)){
                        setPointChangedFlags();
                        // so convert Feet to Meters
                        GBUtilities.convertFeetToMeters(getActivity(),
                                                        pointENElevationMetersInput,
                                                        pointENElevationFeetInput);
                        mPointENElevationFeetOld = temp;
                        mPointENElevationMetersOld = pointENElevationMetersInput.getText().toString();
                    }

                }
            }
        });

        if (mPointPath.equals(GBPath.sEditFromMaps)) {

            pointENElevationMetersInput.setFocusable(false);
            pointENElevationMetersInput.setBackgroundColor(
                    ContextCompat.getColor(getActivity(), R.color.colorGray));
            pointENElevationFeetInput.setFocusable(false);
            pointENElevationFeetInput.setBackgroundColor(
                    ContextCompat.getColor(getActivity(), R.color.colorGray));

        }


        if (distUnits == GBProject.sMeters){
            pointENElevationFeetInput.setVisibility(View.GONE);
            pointEastingFeetInput    .setVisibility(View.GONE);
            pointNorthingFeetInput   .setVisibility(View.GONE);

        } else {
            pointENElevationMetersInput.setVisibility(View.GONE);
            pointEastingMetersInput    .setVisibility(View.GONE);
            pointNorthingMetersInput   .setVisibility(View.GONE);

        }


        field_container = v.findViewById(R.id.zhlContainer);
        //label = (TextView) field_container.findViewById(R.id.coord_label) ;
        //label.setText();
        EditText pointZoneInput       = (EditText) field_container.findViewById(R.id.zoneOutput);
        EditText pointHemisphereInput = (EditText) field_container.findViewById(R.id.hemisphereOutput);
        EditText pointLatbandInput    = (EditText) field_container.findViewById(R.id.latbandOutput);


        field_container = v.findViewById(R.id.convergenceContainer);
        EditText pointDatumInput       = (EditText) field_container.findViewById(R.id.datumOutput);
        EditText pointConvergenceInput = (EditText) field_container.findViewById(R.id.convergenceOutput);
        EditText pointCAdegInput       = (EditText) v.findViewById(R.id.convDegreesInput);
        EditText pointCAminInput       = (EditText) v.findViewById(R.id.convMinutesInput);
        EditText pointCAsecInput       = (EditText) v.findViewById(R.id.convSecondsInput);
        EditText pointScaleFactorInput = (EditText) field_container.findViewById(R.id.scaleFactorOutput);

        if (isDD){
            pointCAdegInput.setVisibility(View.GONE);
            pointCAminInput.setVisibility(View.GONE);
            pointCAsecInput.setVisibility(View.GONE);
        } else {
            pointConvergenceInput.setVisibility(View.GONE);
        }



        if (mPointPath.equals(GBPath.sEditFromMaps)) {

            pointZoneInput.setFocusable(false);
            pointZoneInput.setBackgroundColor(
                    ContextCompat.getColor(getActivity(), R.color.colorGray));
            pointHemisphereInput.setFocusable(false);
            pointHemisphereInput.setBackgroundColor(
                    ContextCompat.getColor(getActivity(), R.color.colorGray));
            pointLatbandInput.setFocusable(false);
            pointLatbandInput.setBackgroundColor(
                    ContextCompat.getColor(getActivity(), R.color.colorGray));
            pointDatumInput.setFocusable(false);
            pointDatumInput.setBackgroundColor(
                    ContextCompat.getColor(getActivity(), R.color.colorGray));
            pointConvergenceInput.setFocusable(false);
            pointConvergenceInput.setBackgroundColor(
                    ContextCompat.getColor(getActivity(), R.color.colorGray));
            pointScaleFactorInput.setFocusable(false);
            pointScaleFactorInput.setBackgroundColor(
                    ContextCompat.getColor(getActivity(), R.color.colorGray));

        }

    }


    private void initializeRecyclerView(View v){

       /*
         * The steps for doing recycler view in onCreateView() of a fragment are:
         * 1) inflate the .xml
         *
         * the special recycler view stuff is:
         * 2) get and store a reference to the recycler view widget that you created in xml
         * 3) create and assign a layout manager to the recycler view
         * 4) assure that there is data for the recycler view to show.
         * 5) use the data to create and set an adapter in the recycler view
         * 6) create and set an item animator (if desired)
         * 7) create and set a line item decorator
         * 8) add event listeners to the recycler view
         *
         * 9) return the view
         */
       if (mPointBeingMaintained == null)return;
        v.setTag(TAG);

        //2) find and remember the RecyclerView
        RecyclerView recyclerView = (RecyclerView) v.findViewById(R.id.pictureList);


        // The RecyclerView.LayoutManager defines how elements are laid out.
        //3) create and assign a layout manager to the recycler view
        RecyclerView.LayoutManager layoutManager  = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);

        //4) create some dummy data and tell the adapter about it
        //  this is done in the singleton container

        //      get our singleton list container
        GBPointManager pointsManager = GBPointManager.getInstance();
        //Get this projects points
        ArrayList<GBPicture> pictureList = mPointBeingMaintained.getPictures();


        //5) Use the data to Create and set out points Adapter
        GBPictureAdapter adapter = new GBPictureAdapter(pictureList);
        recyclerView.setAdapter(adapter);

        //6) create and set the itemAnimator
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        //7) create and add the item decorator
        recyclerView.addItemDecoration(new DividerItemDecoration(
                getActivity(), LinearLayoutManager.VERTICAL));


        //8) add event listeners to the recycler view
        recyclerView.addOnItemTouchListener(
                new GBPointListFragment.RecyclerTouchListener(getActivity(),
                        recyclerView,
                        new GBPointListFragment.ClickListener() {

                            @Override
                            public void onClick(View view, int position) {
                                onSelectPicture(view, position);
                            }

                            @Override
                            public void onLongClick(View view, int position) {
                                //for now, ignore the long click
                            }
                        }));
    }


    private int getCoordinateTypeFromProject() {
        if (mPointBeingMaintained == null) return 0;
        //show the data that came out of the input arguments bundle
        long projectID = mPointBeingMaintained.getForProjectID();

        return GBCoordinate.getCoordinateTypeFromProjectID(projectID);
    }



    private void initializeUI(){
        View v = getView();
        if (v == null)return;
        initializeUI(v);
    }
    private void initializeUI(View v) {
        if (mPointBeingMaintained == null)return;
        //show the data that came out of the input arguments bundle

        //Project ID
        EditText pointProjectIDInput   = (EditText) v.findViewById(R.id.pointProjectIDInput);
        EditText pointIDInput          = (EditText) v.findViewById(R.id.pointIDInput);
        EditText pointProjectNameInput = (EditText) v.findViewById(R.id.pointProjectNameInput);
        EditText pointFeatureCodeInput = (EditText) v.findViewById(R.id.pointFeatureCodeInput);
        EditText pointNotesInput       = (EditText) v.findViewById(R.id.pointNotesInput);
        EditText pointHeightInput      = (EditText) v.findViewById(R.id.pointHeightInput);
        EditText pointNumInput         = (EditText) v.findViewById(R.id.pointNumInput);
        EditText pointNumRawInput      = (EditText) v.findViewById(R.id.pointNbRawInput);

        long projectID = mPointBeingMaintained.getForProjectID();
        pointProjectIDInput.setText(String.valueOf(projectID));
        pointIDInput.setText       (String.valueOf (mPointBeingMaintained.getPointID()));
        if (mPointBeingMaintained.getPointID() == GBUtilities.ID_DOES_NOT_EXIST){
            pointIDInput.setBackgroundColor(ContextCompat.
                                                getColor(getActivity(), R.color.colorLightPink));
        } else {
            pointIDInput.setBackgroundColor(ContextCompat.
                                                getColor(getActivity(), R.color.colorGray));

        }
        pointNumInput.setText(String.valueOf(mPointBeingMaintained.getPointNumber()));

        //number raw points
        int numRawCoordinates = 0;
        GBMeanToken token = mPointBeingMaintained.getMeanToken();
        if (token != null){
            numRawCoordinates = token.getCoordinateSize();
        }
        pointNumRawInput.setText(String.valueOf(numRawCoordinates));

        //Project name
        GBProjectManager projectManager = GBProjectManager.getInstance();
        GBProject project = projectManager.getProject(projectID);
        pointProjectNameInput.setText(project.getProjectName());


        //point ID
        if (mPointBeingMaintained.getPointID() != GBUtilities.ID_DOES_NOT_EXIST){
            pointIDInput.setText(String.valueOf(mPointBeingMaintained.getPointID()));
            pointFeatureCodeInput.setText(mPointBeingMaintained.getPointFeatureCode());
            pointNotesInput.setText(mPointBeingMaintained.getPointNotes());
        }

        //Height
        pointHeightInput.setText(String.valueOf(mPointBeingMaintained.getHeight()));

        GBProject openProject = GBUtilities.getInstance().getOpenProject((GBActivity)getActivity());
        int zone = openProject.getZone();
        //SPC Zone & State
        initializeSpcsZone(v, zone);

        initializeDistanceUnits(v);

        //Coordinate Type
        CharSequence coordinateType = project.getProjectCoordinateType();
        String msg = getString(R.string.coordinate_type) + " " + coordinateType ;

        if (coordinateType.toString().isEmpty()){
            msg = getString(R.string.point_no_coordinate_type);

        } else if (coordinateType.equals(GBCoordinate.sCoordinateTypeWGS84)) {
            initializeUIFromLLCoordinate(v, mPointBeingMaintained);

        } else if (coordinateType.equals(GBCoordinate.sCoordinateTypeNAD83) ) {
            initializeUIFromLLCoordinate(v, mPointBeingMaintained);

        } else if (coordinateType.equals(GBCoordinate.sCoordinateTypeUTM)) {
            initializeUIFromENCoordinate(v, mPointBeingMaintained);

        } else if (coordinateType.equals(GBCoordinate.sCoordinateTypeSPCS)) {
            initializeUIFromENCoordinate(v, mPointBeingMaintained);

        } else {
            msg = getString(R.string.unk_coord_type);
        }
        TextView pointCoordinateTypeLabel = (TextView)v.findViewById(R.id.coordinate_label);
        pointCoordinateTypeLabel.setText(msg);

        //Offsets
        EditText pointOffsetDistInput = (EditText) v.findViewById(R.id.pointOffDistInput);
        EditText pointOffsetHeadInput = (EditText) v.findViewById(R.id.pointOffHeadInput);
        EditText pointOffsetEleInput  = (EditText) v.findViewById(R.id.pointOffEleInput);

        pointOffsetDistInput.setText(String.valueOf(mPointBeingMaintained.getOffsetDistance()));
        pointOffsetHeadInput.setText(String.valueOf(mPointBeingMaintained.getOffsetHeading()));
        pointOffsetEleInput .setText(String.valueOf(mPointBeingMaintained.getOffsetElevation()));

    }

    private boolean initializeSpcsZone(View v, int zone){


        //need to ask for zone, then convert based on the zone
        EditText pointZoneInput        = (EditText) v.findViewById(R.id.pointSpcZoneInput);
        TextView pointStateInput       = (TextView) v.findViewById(R.id.pointSpcStateOutput);
        if (zone == 0){
            pointZoneInput.setText(getString(R.string.spc_zone_error));
            return false;
        }

        String zoneString = String.valueOf(zone);
        if (GBUtilities.isEmpty(zoneString)){
            pointZoneInput.setText(getString(R.string.spc_zone_error));
            return false;
        }



        GBCoordinateConstants constants = new GBCoordinateConstants(zone);
        int spcsZone = constants.getZone();
        if (spcsZone != (int)GBUtilities.ID_DOES_NOT_EXIST) {
            String state = constants.getState();
            pointStateInput.setText(state);
            pointZoneInput.setText(zoneString);
        }

        return true;
    }

    private void initializeDistanceUnits(View v){

        GBProject openProject = GBUtilities.getInstance().getOpenProject((GBActivity)getActivity());

        int distanceUnits = openProject.getDistanceUnits();
        CharSequence duString;
        if (distanceUnits == GBProject.sMeters){
            duString = "Meters";
        } else if (distanceUnits == GBProject.sFeet){
            duString = "Feet";
        } else {//international feet
            duString = "Int Feet";
        }

        EditText distUnitsOutput = (EditText)v.findViewById(R.id.pointDistanceUnitsInput);


        distUnitsOutput.setText(duString);
    }



    private void saveLLFieldsAsOldValues(){
        View v = getView();
        if (v == null)return;

        View field_container = v.findViewById(R.id.latitudeContainer);

        EditText pointLatitudeDDInput = (EditText) field_container.findViewById(R.id.ll_dd_input);
        EditText pointLatitudeDInput  = (EditText) field_container.findViewById(R.id.ll_d_Input) ;
        EditText pointLatitudeMInput  = (EditText) field_container.findViewById(R.id.ll_m_Input);
        EditText pointLatitudeSInput  = (EditText) field_container.findViewById(R.id.ll_s_Input) ;

        mPointLatitudeDDOld = pointLatitudeDDInput.getText().toString();
        mPointLatitudeDOld  = pointLatitudeDInput.getText().toString();
        mPointLatitudeMOld  = pointLatitudeMInput.getText().toString();
        mPointLatitudeSOld  = pointLatitudeSInput.getText().toString();


        field_container = v.findViewById(R.id.longitudeContainer);
        EditText pointLongitudeDDInput = (EditText) field_container.findViewById(R.id.ll_dd_input);
        EditText pointLongitudeDInput  = (EditText) field_container.findViewById(R.id.ll_d_Input) ;
        EditText pointLongitudeMInput  = (EditText) field_container.findViewById(R.id.ll_m_Input);
        EditText pointLongitudeSInput  = (EditText) field_container.findViewById(R.id.ll_s_Input) ;

        mPointLongitudeDDOld = pointLongitudeDDInput.getText().toString();
        mPointLongitudeDOld  = pointLongitudeDInput.getText().toString();
        mPointLongitudeMOld  = pointLongitudeMInput.getText().toString();
        mPointLongitudeSOld  = pointLongitudeSInput.getText().toString();

        field_container = v.findViewById(R.id.elevationGeoidContainer);
        EditText pointElevationMetersInput = (EditText) field_container.findViewById(R.id.elevationMetersInput);
        EditText pointElevationFeetInput = (EditText) field_container.findViewById(R.id.elevationFeetInput);
        EditText pointGeoidMetersInput = (EditText) field_container.findViewById(R.id.geoidHeightMetersInput);
        EditText pointGeoidFeetInput = (EditText) field_container.findViewById(R.id.geoidHeightFeetInput);


        mPointElevationMetersOld = pointElevationMetersInput.getText().toString();
        mPointElevationFeetOld   = pointElevationFeetInput.getText().toString();
        mPointGeoidMetersOld     = pointGeoidMetersInput.getText().toString();
        mPointGeoidFeetOld       = pointGeoidFeetInput.getText().toString();

    }

    private void saveENFieldsAsOldValues(){
        View v = getView();
        if (v ==null)return;

        View field_container = v.findViewById(R.id.eastingContainer);
        EditText pointEastingMetersInput = (EditText)field_container.findViewById(R.id.metersOutput);
        EditText pointEastingFeetInput = (EditText)field_container.findViewById(R.id.feetOutput);

        field_container = v.findViewById(R.id.northingContainer);
        EditText pointNorthingMetersInput = (EditText)field_container.findViewById(R.id.metersOutput);
        EditText pointNorthingFeetInput = (EditText)field_container.findViewById(R.id.feetOutput);

        field_container = v.findViewById(R.id.elevationContainer);
        EditText pointENElevationMetersInput = (EditText)field_container.findViewById(R.id.elevationMetersInput);
        EditText pointENElevationFeetInput   = (EditText)field_container.findViewById(R.id.elevationFeetInput);

        field_container = v.findViewById(R.id.zhlContainer);
        EditText pointZoneInput       = (EditText) field_container.findViewById(R.id.zoneOutput);
        EditText pointHemisphereInput = (EditText) field_container.findViewById(R.id.hemisphereOutput);
        EditText pointLatbandInput    = (EditText) field_container.findViewById(R.id.latbandOutput);

        field_container = v.findViewById(R.id.convergenceContainer);
        EditText pointDatumInput       = (EditText) field_container.findViewById(R.id.datumOutput);
        EditText pointConvergenceInput = (EditText) field_container.findViewById(R.id.convergenceOutput);
        EditText pointScaleFactorInput = (EditText) field_container.findViewById(R.id.scaleFactorOutput);

        mPointEastingMetersOld = pointEastingMetersInput.getText().toString();
        mPointEastingFeetOld   = pointEastingFeetInput.getText().toString();

        mPointNorthingMetersOld = pointNorthingMetersInput.getText().toString();
        mPointNorthingFeetOld   = pointNorthingFeetInput.getText().toString();

        mPointENElevationMetersOld = pointENElevationMetersInput.getText().toString();
        mPointENElevationFeetOld   = pointENElevationFeetInput.getText().toString();
/*
        mPointZoneOld       = pointZoneInput.getText().toString();
        mPointHemisphereOld = pointHemisphereInput.getText().toString();
        mPointLatbandOld    = pointLatbandInput.getText().toString();


        mPointConvergenceOld = pointConvergenceInput.getText().toString();
        mPointScaleFactorOld = pointScaleFactorInput.getText().toString();
*/
    }



    private void initializeUIFromLLCoordinate(View v, GBPoint point ){

        GBCoordinateLL coordinate = (GBCoordinateLL) point.getCoordinate();
        if (coordinate == null) return;

        double latitude = coordinate.getLatitude();
        double longitude = coordinate.getLongitude();
        double elevation = coordinate.getElevation();
        double geoid     = coordinate.getGeoid();

        GBProject openProject = GBUtilities.getInstance().getOpenProject((GBActivity)getActivity());

        double eleFeet;
        double geoidFeet;
        int distUnits = openProject.getDistanceUnits();
        if ((distUnits == GBProject.sFeet) || (distUnits == GBProject.sMeters)){
            eleFeet = coordinate.getElevationFeet();
            geoidFeet = coordinate.getGeoidFeet();
        } else {//distance units are international feet
            eleFeet = coordinate.getElevationIFeet();
            geoidFeet = coordinate.getGeoidIFeet();
        }


        View field_container = v.findViewById(R.id.latitudeContainer);

        EditText pointLatitudeDDInput = (EditText) field_container.findViewById(R.id.ll_dd_input);
        EditText pointLatitudeDInput  = (EditText) field_container.findViewById(R.id.ll_d_Input) ;
        EditText pointLatitudeMInput  = (EditText) field_container.findViewById(R.id.ll_m_Input);
        EditText pointLatitudeSInput  = (EditText) field_container.findViewById(R.id.ll_s_Input) ;

        field_container = v.findViewById(R.id.elevationGeoidContainer);
        EditText pointElevationMetersInput =
                (EditText) field_container.findViewById(R.id.elevationMetersInput);
        EditText pointElevationFeetInput =
                (EditText) field_container.findViewById(R.id.elevationFeetInput);
        EditText pointGeoidMetersInput =
                (EditText) field_container.findViewById(R.id.geoidHeightMetersInput);
        EditText pointGeoidFeetInput =
                (EditText) field_container.findViewById(R.id.geoidHeightFeetInput);

        pointLatitudeDDInput.setText(String.valueOf(latitude));
        pointLatitudeDInput.setText(String.valueOf(coordinate.getLatitudeDegree()));
        pointLatitudeMInput.setText(String.valueOf(coordinate.getLatitudeMinute()));
        pointLatitudeSInput.setText(String.valueOf(coordinate.getLatitudeSecond()));

        field_container = v.findViewById(R.id.longitudeContainer);
        EditText pointLongitudeDDInput = (EditText) field_container.findViewById(R.id.ll_dd_input);
        EditText pointLongitudeDInput  = (EditText) field_container.findViewById(R.id.ll_d_Input) ;
        EditText pointLongitudeMInput  = (EditText) field_container.findViewById(R.id.ll_m_Input);
        EditText pointLongitudeSInput  = (EditText) field_container.findViewById(R.id.ll_s_Input) ;

        pointLongitudeDDInput.setText(String.valueOf(longitude));
        pointLongitudeDInput.setText(String.valueOf(coordinate.getLongitudeDegree()));
        pointLongitudeMInput.setText(String.valueOf(coordinate.getLongitudeMinute()));
        pointLongitudeSInput.setText(String.valueOf(coordinate.getLongitudeSecond()));


        pointElevationMetersInput.setText(String.valueOf(elevation));
        pointElevationFeetInput.setText(String.valueOf(eleFeet));

        pointGeoidMetersInput.setText(String.valueOf(geoid));
        pointGeoidFeetInput.setText(String.valueOf(geoidFeet));

        if (latitude < 0.0){
            pointLatitudeDDInput.setTextColor(ContextCompat.getColor(getActivity(), colorNegNumber));
            pointLatitudeDInput.setTextColor(ContextCompat.getColor(getActivity(), colorNegNumber));
            pointLatitudeMInput.setTextColor(ContextCompat.getColor(getActivity(), colorNegNumber));
            pointLatitudeSInput.setTextColor(ContextCompat.getColor(getActivity(), colorNegNumber));
        }
        if (longitude < 0.0){
            pointLongitudeDDInput.setTextColor(ContextCompat.getColor(getActivity(), colorNegNumber));
            pointLongitudeDInput.setTextColor(ContextCompat.getColor(getActivity(), colorNegNumber));
            pointLongitudeMInput.setTextColor(ContextCompat.getColor(getActivity(), colorNegNumber));
            pointLongitudeSInput.setTextColor(ContextCompat.getColor(getActivity(), colorNegNumber));
        }
        if (elevation < 0.0){
            pointElevationMetersInput.setTextColor(ContextCompat.getColor(getActivity(), colorNegNumber));
            pointElevationFeetInput.setTextColor(ContextCompat.getColor(getActivity(), colorNegNumber));
        }
        if (geoid < 0.0){
            pointGeoidMetersInput.setTextColor(ContextCompat.getColor(getActivity(), colorNegNumber));
            pointGeoidFeetInput.setTextColor(ContextCompat.getColor(getActivity(), colorNegNumber));
        }



        field_container = v.findViewById(R.id.convergenceContainer);
        EditText pointConvergenceInput = (EditText) field_container.findViewById(R.id.convergenceOutput);
        EditText pointCAdegInput       = (EditText) field_container.findViewById(R.id.convDegreesInput);
        EditText pointCAminInput       = (EditText) field_container.findViewById(R.id.convMinutesInput);
        EditText pointCAsecInput       = (EditText) field_container.findViewById(R.id.convSecondsInput);
        EditText pointScaleFactorInput = (EditText) field_container.findViewById(R.id.scaleFactorOutput);


         pointConvergenceInput.setText(String.valueOf(coordinate.getConvergenceAngle()));

        boolean isCA = true;
        convertDDtoDMS(getActivity(),
                pointConvergenceInput,
                pointCAdegInput,
                pointCAminInput,
                pointCAsecInput,
                isCA,
                false);


        pointScaleFactorInput.setText(String.valueOf(coordinate.getScaleFactor()));

    }

    private void initializeUIFromENCoordinate(View v, GBPoint point ){
        GBCoordinateEN coordinate = (GBCoordinateEN)point.getCoordinate();
        if (coordinate == null)return;

        GBProject openProject = GBUtilities.getInstance().getOpenProject((GBActivity)getActivity());
        double easting   = coordinate.getEasting();
        double northing  = coordinate.getNorthing();
        double elevation = coordinate.getElevation();
        double geoid     = coordinate.getGeoid();

        double eastingFeet;
        double northingFeet;
        double eleFeet;
        double geoidFeet;
        int distUnits = openProject.getDistanceUnits();
        if ((distUnits == GBProject.sFeet) || (distUnits == GBProject.sMeters)){
            eastingFeet  = coordinate.getEastingFeet();
            northingFeet = coordinate.getNorthingFeet();
            eleFeet      = coordinate.getElevationFeet();
            geoidFeet    = coordinate.getGeoidFeet();
        } else {//distance units are international feet
            eastingFeet  = coordinate.getEastingIFeet();
            northingFeet = coordinate.getNorthingIFeet();
            eleFeet      = coordinate.getElevationIFeet();
            geoidFeet    = coordinate.getGeoidIFeet();
        }


        View field_container = v.findViewById(R.id.eastingContainer);
        EditText pointEastingMetersInput = (EditText)field_container.findViewById(R.id.metersOutput);
        EditText pointEastingFeetInput = (EditText)field_container.findViewById(R.id.feetOutput);

        field_container = v.findViewById(R.id.northingContainer);
        EditText pointNorthingMetersInput = (EditText)field_container.findViewById(R.id.metersOutput);
        EditText pointNorthingFeetInput = (EditText)field_container.findViewById(R.id.feetOutput);

        field_container = v.findViewById(R.id.elevationContainer);
        EditText pointENElevationMetersInput = (EditText)field_container.findViewById(R.id.elevationMetersInput);
        EditText pointENElevationFeetInput   = (EditText)field_container.findViewById(R.id.elevationFeetInput);

        field_container = v.findViewById(R.id.zhlContainer);
        EditText pointZoneInput       = (EditText) field_container.findViewById(R.id.zoneOutput);
        EditText pointHemisphereInput = (EditText) field_container.findViewById(R.id.hemisphereOutput);
        EditText pointLatbandInput    = (EditText) field_container.findViewById(R.id.latbandOutput);

        field_container = v.findViewById(R.id.convergenceContainer);
        EditText pointDatumInput       = (EditText) field_container.findViewById(R.id.datumOutput);
        EditText pointConvergenceInput = (EditText) field_container.findViewById(R.id.convergenceOutput);
        EditText pointCAdegInput       = (EditText) field_container.findViewById(R.id.convDegreesInput);
        EditText pointCAminInput       = (EditText) field_container.findViewById(R.id.convMinutesInput);
        EditText pointCAsecInput       = (EditText) field_container.findViewById(R.id.convSecondsInput);
        EditText pointScaleFactorInput = (EditText) field_container.findViewById(R.id.scaleFactorOutput);

        field_container = v.findViewById(R.id.elevationContainer);
        EditText pointElevationMetersInput = (EditText) field_container.findViewById(R.id.elevationMetersInput);
        EditText pointElevationFeetInput = (EditText) field_container.findViewById(R.id.elevationFeetInput);
        EditText pointGeoidMetersInput = (EditText) field_container.findViewById(R.id.geoidHeightMetersInput);
        EditText pointGeoidFeetInput = (EditText) field_container.findViewById(R.id.geoidHeightFeetInput);


        pointEastingMetersInput.setText(String.valueOf(easting));
        pointEastingFeetInput.setText(String.valueOf(eastingFeet));

        pointNorthingMetersInput.setText(String.valueOf(northing));
        pointNorthingFeetInput.setText(String.valueOf(northingFeet));

        pointENElevationMetersInput.setText(String.valueOf(elevation));
        pointENElevationFeetInput.setText(String.valueOf(eleFeet));

        // TODO: 7/14/2017 Does UTM really not have geoid???
       // pointGeoidMetersInput.setText(String.valueOf(geoid));
       // pointGeoidFeetInput.setText(String.valueOf(geoidFeet));

        pointZoneInput.setText(String.valueOf(coordinate.getZone()));

        int coordinateDBType = coordinate.getCoordinateDBType();
        if (coordinateDBType == GBCoordinate.sCoordinateDBTypeUTM){
            //fill in the UTM specific fields
            GBCoordinateUTM coordinateUTM = (GBCoordinateUTM) point.getCoordinate();
            char latBandChar    = coordinateUTM.getLatBand();
            char hemisphereChar = coordinateUTM.getHemisphere();

            pointLatbandInput.setText(String.valueOf(latBandChar));
            pointHemisphereInput.setText(String.valueOf(hemisphereChar));
            pointHemisphereInput.setBackgroundColor(ContextCompat.getColor(getActivity(),R.color.colorGrayish));

        } else if (coordinateDBType == GBCoordinate.sCoordinateDBTypeSPCS){
            GBCoordinateSPCS coordinateSPCS = (GBCoordinateSPCS)point.getCoordinate();
            String stateString = coordinateSPCS.getState().toString();

            pointHemisphereInput.setText(getString(R.string.spc_state_label));
            pointHemisphereInput.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.colorScreenBackground));

            pointLatbandInput.setText(stateString);

        }

        pointDatumInput.setText(String.valueOf(coordinate.getDatum()));
        pointConvergenceInput.setText(String.valueOf(coordinate.getConvergenceAngle()));

        boolean isCA = true;
        convertDDtoDMS(getActivity(),
                pointConvergenceInput,
                pointCAdegInput,
                pointCAminInput,
                pointCAsecInput,
                isCA,
                false);




        pointScaleFactorInput.setText(String.valueOf(coordinate.getScaleFactor()));

        if (elevation < 0.0){
            pointElevationMetersInput.setTextColor(ContextCompat.getColor(getActivity(), colorNegNumber));
            pointElevationFeetInput.setTextColor(ContextCompat.getColor(getActivity(), colorNegNumber));
        }
        if (easting < 0.0){
            pointEastingMetersInput.setTextColor(ContextCompat.getColor(getActivity(), colorNegNumber));
            pointEastingFeetInput.setTextColor(ContextCompat.getColor(getActivity(), colorNegNumber));
        }
        if (northing < 0.0){
            pointNorthingMetersInput.setTextColor(ContextCompat.getColor(getActivity(), colorNegNumber));
            pointNorthingFeetInput.setTextColor(ContextCompat.getColor(getActivity(), colorNegNumber));
        }

    }


    // TODO: 6/19/2017 figrue out how to use the one on GBCoordinateLL
    //Conversion for UI fields
    //last parameter indicates whether latitude (true) or longitude (false)
    boolean convertDDtoDMS(Context context,
                           EditText tudeDDInput,
                           EditText tudeDInput,
                           EditText tudeMInput,
                           EditText tudeSInput,
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



    //**********************************************************/
    //*******     Called from EditText Listeners     ***********/
    //**********************************************************/


    //**********************************************************/
    //*******  Called from RecyclerView Listeners    ***********/
    //**********************************************************/


    private void onSelectPicture(View view, int position){
        View v = getView();
        if (v == null)return;
        RecyclerView recyclerView = (RecyclerView) v.findViewById(R.id.pictureList);
        GBPictureAdapter adapter = (GBPictureAdapter)recyclerView.getAdapter();

        GBPicture picture = adapter.getPicture(position);
        String pathToPicture = picture.getPathName();
        Bitmap bitmap = BitmapFactory.decodeFile(pathToPicture);

        if (bitmap != null) {
            mPictureImage.setImageBitmap(bitmap);

            //for some reason, showing the image blanks out the recycler view, so force a redraw
            recyclerView.getRecycledViewPool().clear();
            //mAdapter.notifyDataSetChanged();
            recyclerView.invalidate();
        } else {
            String msg = getString(R.string.missing_picture_file)+ " " + pathToPicture;
            GBUtilities.getInstance().showStatus(getActivity(), msg);
        }

    }


    //**********************************************************/
    //*********     Called from Button Listeners     ***********/
    //**********************************************************/
    void onExit() {
        ((GBActivity) getActivity()).switchToProjectEditScreen();
    }


    private boolean onSave(){
        if (mPointBeingMaintained == null)return false;
        boolean returnCode = true;

        //The point must have been changed for this button to work
        if (mPointChanged) {
            //saveChanges();

            //What happens here depends upon the path
            if ((mPointPath.equals(GBPath.sCreateTag)) || (mPointPath.equals(GBPath.sEditTag))){
                returnCode = addNewPoint();
            }
            //all other paths do nothing

            GBUtilities.getInstance().hideSoftKeyboard(getActivity());
            initializeUI();
            if (returnCode) setPointSavedFlags();
        }
        return returnCode;

    }

    private boolean addNewPoint(){
        if (mPointBeingMaintained == null)return false;
        View v = getView();
        if (v == null)return false;

        boolean returnCode = true;


        returnCode = updatePointFromUI(mPointBeingMaintained);

        if (!returnCode){
            //The coordinate is invalid, or something else is wrong
            //user is informed in updatePointFromUI
            //reset the point ID to not yet created
              mPointBeingMaintained.setPointID(GBUtilities.ID_DOES_NOT_EXIST);
            //NOTE that we are still on the create path
            return false;
        }

        //and update the screen with the new ID
        initializeUI(v);

        //now add the point to memory and db
        GBPointManager pointManager = GBPointManager.getInstance();
        boolean addToDBToo = true;
        GBProject openProject = GBUtilities.getInstance().getOpenProject((GBActivity)getActivity());
        //This does not update the coordinate or the MeanToken in the DB,
        // but this fragment does not explicitly update either of these objects
        //If you see this comment and disagree with that statement, correct the situation
        if (!pointManager.addPointToProject(openProject, mPointBeingMaintained, addToDBToo)){
            GBUtilities.getInstance().showStatus(getActivity(),  getString(R.string.error_adding_point));
            return false;
        }

        //from now on we are editing the point, not creating it
        mPointPath = GBPath.sEditTag;

        //So we can go look at other points
        Button pointViewExistingButton = (Button) v.findViewById(R.id.pointViewExistingButton);
        pointViewExistingButton.setEnabled(true);
        pointViewExistingButton.setTextColor(Color.BLACK);

        return returnCode;
    }


    //returns false if there are any errors in the point
    private boolean updatePointFromUI(GBPoint point){
        if (point == null)return false;

        View v = getView();
        if (v == null)return false;


        boolean returnCode = true;
// TODO: 7/2/2017 remove updating the coordinate from this screen. Must use convert screen
        /*
        //Create a coordinate of the proper type with the attributes from the screen
        CharSequence coordType = getFullCoordTypeFromProject();
        if (coordType.equals(GBCoordinate.sCoordinateTypeWGS84)){

            GBCoordinateWGS84 newCoordinate = new GBCoordinateWGS84();
            returnCode = updateLLCoordinateFromUI(v, point, newCoordinate);
        } else if (coordType.equals(GBCoordinate.sCoordinateTypeNAD83)){
            GBCoordinateNAD83 newCoordinate = new GBCoordinateNAD83();
           returnCode = updateLLCoordinateFromUI(v, point, newCoordinate);

        } else if (coordType.equals(GBCoordinate.sCoordinateTypeUTM)) {
            GBCoordinateUTM newCoordinate = new GBCoordinateUTM();
            returnCode = updateENCoordinateFromUI(v, point, newCoordinate);

        } else { //GBCoordinate.sCoordinateTypeClassSPCS
            // TODO: 6/20/2017 have to know zone to create a new SPCS coordinate properly

            GBCoordinateSPCS newCoordinate = new GBCoordinateSPCS();
            returnCode = updateENCoordinateFromUI(v, point, newCoordinate);
        }
*/
        if (returnCode) {

            EditText pointNumberInput = (EditText) v.findViewById(R.id.pointNumInput);
            EditText pointFeatureCodeInput = (EditText) v.findViewById(R.id.pointFeatureCodeInput);
            EditText pointNotesInput       = (EditText) v.findViewById(R.id.pointNotesInput);
            EditText pointHeightInput      = (EditText) v.findViewById(R.id.pointHeightInput);
            EditText pointOffsetEleInput   = (EditText) v.findViewById(R.id.pointOffEleInput);
            EditText pointOffsetDistInput  = (EditText) v.findViewById(R.id.pointOffDistInput);
            EditText pointOffsetHeadInput  = (EditText) v.findViewById(R.id.pointOffHeadInput);
            EditText pointHdopInput = (EditText) v.findViewById(R.id.pointHdopInput);
            EditText pointVdopInput = (EditText) v.findViewById(R.id.pointVdopInput);
            EditText pointTdopInput = (EditText) v.findViewById(R.id.pointTdopInput);
            EditText pointPdopInput = (EditText) v.findViewById(R.id.pointPdopInput);
            EditText pointHrmsInput = (EditText) v.findViewById(R.id.pointHrmsInput);
            EditText pointVrmsInput = (EditText) v.findViewById(R.id.pointVrmsInput);


            String pointNumberString = pointNumberInput.getText().toString();
            int pointNumber;
            if (GBUtilities.isEmpty(pointNumberString)){
                pointNumber = 0;
            } else {
                pointNumber = Integer.valueOf(pointNumberString);
            }
            point.setPointNumber(pointNumber);
            point.setPointFeatureCode(pointFeatureCodeInput.getText().toString().trim());
            point.setPointNotes(pointNotesInput.getText().toString().trim());

            //There are several fields that can not be updated from this screen
            // TODO: 1/12/2017 Height, quality, offset fields should perhaps be written to the point object
            return true;
        } else {
            //Coordinate not valid
            GBUtilities.getInstance().showStatus(getActivity(), getString(R.string.coordinate_not_valid));

            return returnCode;
        }

    }




    private void onListPoints(){
        //what this button does depends upon the path  {create, open, copy, edit, show}
        //**************** CREATE ************************************************/
        if (mPointPath.equals(GBPath.sCreateTag)){
            GBUtilities.getInstance().showStatus(getActivity(), R.string.cant_view_points);
            maybeAskFirstListPoints();

            //************************** OPEN / COPY / EDIT / SHOW **************************************/
        } else if ((mPointPath.equals(GBPath.sOpenTag)) ||
                (mPointPath.equals(GBPath.sCopyTag)) ||
                (mPointPath.equals(GBPath.sEditTag)) ||
                (mPointPath.equals(GBPath.sShowTag))) {

            maybeAskFirstListPoints();


            //************************************* UNKNOWN *************************/
        } else {
            GBUtilities.getInstance().showStatus(getActivity(),
                    R.string.unrecognized_path_encountered);

            //Don't really know what to do here, but switch path to show and continue
            mPointPath = GBPath.sShowTag;
            maybeAskFirstListPoints();

        }
    }

    private void onListRawPoints(){
        //The point must exit and have already been saved
        if (mPointChanged)return;
        if (mPointBeingMaintained == null)return;

        //The coordinate must have gone through the meaning process
        GBMeanToken token = mPointBeingMaintained.getMeanToken();
        if (token == null)return;

        long tokenID = token.getMeanTokenID();

        //switch to the screen showing the meaning details
        ((GBActivity)getActivity()).switchToRawListScreen(tokenID);
    }

    private void maybeAskFirstListPoints(){
        if (mPointChanged){
            //ask the user if should continue
            areYouSureListPoints();

        } else {
            GBUtilities.getInstance().showStatus(getActivity(), R.string.point_unchanged);
            switchToListPoints();
        }
    }

    //Build and display the alert dialog
    private void areYouSureListPoints(){
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.abandon_title)
                .setIcon(R.drawable.ground_station_icon)
                .setMessage(R.string.are_you_sure)
                .setPositiveButton(R.string.continue_abandon_changes,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                //Leave even though point has chaged
                                GBUtilities.getInstance().showStatus(getActivity(),R.string.continue_abandon_changes);

                                switchToListPoints();
                            }
                        })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                        GBUtilities.getInstance().showStatus(getActivity(), "Pressed Cancel");
                    }
                })
                .setIcon(R.drawable.ground_station_icon)
                .show();
    }

    private void switchToListPoints(){
        if (mPointBeingMaintained == null)return;
        GBActivity myActivity = (GBActivity) getActivity();
        myActivity.switchToPointsListScreen( new GBPath(mPointPath));
    }


    //**********************************************************/
    //****     Maintain State Flags in this Fragment     *******/
    //**********************************************************/

    private void setPointChangedFlags(){
        View v = getView();
        if (v == null)return;

        Button pointSaveChangesButton = (Button)v.findViewById(R.id.pointSaveChangesButton);
        Button pointMeasureButton     = (Button) v.findViewById(R.id.pointMeasureButton);

        mPointChanged = true;
        //enable the enter button as the default is NOT enabled/grayed out

        //enable the save changes button too
        //But only if on the Create or Edit path
        if ((mPointPath.equals(GBPath.sCreateTag))||
            (mPointPath.equals(GBPath.sEditTag))) {
            pointSaveChangesButton.setEnabled(true);
            pointSaveChangesButton.setTextColor(Color.BLACK);

            pointMeasureButton.setEnabled(false);
            pointMeasureButton.setTextColor(Color.GRAY);
        }
    }

    private void setPointSavedFlags(){
        View v = getView();
        if (v == null)return;

        Button pointSaveChangesButton = (Button)v.findViewById(R.id.pointSaveChangesButton);
        Button pointMeasureButton     = (Button) v.findViewById(R.id.pointMeasureButton);

        mPointChanged = false;

        //disable the save and measure buttons
        pointSaveChangesButton.setEnabled(false);
        pointSaveChangesButton.setTextColor(Color.GRAY);

        pointMeasureButton.setEnabled(true);
        pointMeasureButton.setTextColor(Color.BLACK);
    }


}


