package com.asc.msigeosystems.geobot;

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

import java.util.ArrayList;

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
    private String mPointGeoidMetersOld;




    //**********************************************************/
    /*    E/N Old values to determine if they have changed    **/
    //**********************************************************/
    private String       mPointEastingMetersOld;
    private String       mPointNorthingMetersOld;
    private String       mPointENElevationMetersOld;

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
        }else if (coordinateWidgetType == GBCoordinate.sNEWidgets) {
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




        //Point Quality PDOP
        EditText pointPdopInput = (EditText) v.findViewById(R.id.pointPdopInput);
        pointPdopInput.addTextChangedListener(textWatcher);


        //Point Quality HRMS
        EditText pointHrmsInput = (EditText) v.findViewById(R.id.pointHrmsInput);
        pointHrmsInput.addTextChangedListener(textWatcher);


        //Point Quality VRMS
        EditText pointVrmsInput = (EditText) v.findViewById(R.id.pointVrmsInput);
        pointVrmsInput.addTextChangedListener(textWatcher);

        //Set the label to Standard Deviation if in settings
        if (GBGeneralSettings.isStdDev((GBActivity)getActivity())) {
            GBProject openProject = GBUtilities.getInstance().getOpenProject((GBActivity)getActivity());
            int coordType = openProject.getCoordinateType();
            int hStdDevLabel = R.string.sd_longitude_label;
            int vStdDevLabel = R.string.sd_latitude_label;
            if ((coordType == GBCoordinate.sCoordinateDBTypeSPCS) ||
                (coordType == GBCoordinate.sCoordinateDBTypeUTM)) {
                hStdDevLabel = R.string.sd_easting_label;
                vStdDevLabel = R.string.sd_northing_label;
            }
            TextView pointHrmsLabel = (TextView) v.findViewById(R.id.pointHrmsLabel);
            TextView pointVrmsLabel = (TextView) v.findViewById(R.id.pointVrmsLabel);
            pointHrmsLabel.setText(hStdDevLabel);
            pointVrmsLabel.setText(vStdDevLabel);
        }


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
        pointMeasureButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                ((GBActivity)getActivity()).switchToListNmeaScreen();

                return true;
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
        } else if (coordinateWidgetType == GBCoordinate.sNEWidgets) {
            //inflate the coordinates, and
            // attach the coordinates to the LinearLayout which exists to contain the coordinates
            layoutInflater.inflate(R.layout.element_coordinate_en, coordinatesContainer, true);

            //then wire up the appropriate widgets for this type of coordinat
            wireNECoordinateWidgets(v);
        }
        return coordinateWidgetType;
    }

    private void wireLLCoordinateWidgets(View v){

        GBActivity myActivity = (GBActivity)getActivity();
        //make DD vs DMS on the project work by making various views invisible
        GBProject openProject = GBUtilities.getInstance().getOpenProject(myActivity);
        boolean isDD = GBGeneralSettings.isLocDD(myActivity);
        boolean isCADD = GBGeneralSettings.isCADD(myActivity);


        int distUnits = openProject.getDistanceUnits();

        //The location fields order may be reversed on the screen
        //NOTE TO MAINTENANCE PROGRAMMER:
        // This can be tricky, so remember the kludge is to just change the names on the UI


        //***************************************************************************/
        //*******             Latitude                                       ********/
        //***************************************************************************/
        View field_container;
        TextView label;

        //set up the UI widgets for the latitude/longitude coordinates
        field_container = v.findViewById(R.id.latitudeContainer);
        if (GBGeneralSettings.isLngLat(myActivity)) {
            field_container = v.findViewById(R.id.longitudeContainer);
        }
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
        if (GBGeneralSettings.isLngLat(myActivity)) {
            field_container = v.findViewById(R.id.latitudeContainer);
        }
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


        if (mPointPath.equals(GBPath.sEditFromMaps)) {

            pointElevationMetersInput.setFocusable(false);
            pointElevationMetersInput.setBackgroundColor(
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

                        mPointElevationMetersOld = temp;

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

        if (mPointPath.equals(GBPath.sEditFromMaps)) {

            pointGeoidMetersInput.setFocusable(false);
            pointGeoidMetersInput.setBackgroundColor(
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
                        mPointGeoidMetersOld = temp;
                    }
                }
            }
        });



        field_container = v.findViewById(R.id.convergenceContainer);
        EditText pointConvergenceInput = (EditText) field_container.findViewById(R.id.convergenceOutput);
        EditText pointCAdegInput       = (EditText) v.findViewById(R.id.convDegreesInput);
        EditText pointCAminInput       = (EditText) v.findViewById(R.id.convMinutesInput);
        EditText pointCAsecInput       = (EditText) v.findViewById(R.id.convSecondsInput);
        EditText pointScaleFactorInput = (EditText) field_container.findViewById(R.id.scaleFactorOutput);

        if (isCADD){
            pointCAdegInput.setVisibility(View.GONE);
            pointCAminInput.setVisibility(View.GONE);
            pointCAsecInput.setVisibility(View.GONE);
        } else {
            pointConvergenceInput.setVisibility(View.GONE);
        }


    }


    private void wireNECoordinateWidgets(View v){

        GBActivity myActivity = (GBActivity)getActivity();

        //make DD vs DMS on the project work by making various views invisible
        GBProject openProject = GBUtilities.getInstance().getOpenProject(myActivity);
        boolean isDD = GBGeneralSettings.isLocDD(myActivity);

        int distUnits = openProject.getDistanceUnits();


        View field_container;
        TextView label;

        //set up the widgets for the easting/northing coordinate

        //The location fields order may be reversed on the screen
        //NOTE TO MAINTENANCE PROGRAMMER:
        // This can be tricky, so remember the kludge is to just change the names on the UI

        field_container = v.findViewById(R.id.eastingContainer);
        if (GBGeneralSettings.isEN(myActivity)) {
            field_container = v.findViewById(R.id.northingContainer);
        }
        label = (TextView) field_container.findViewById(R.id.en_label) ;
        label.setText(getString(R.string.easting_label));
        final EditText pointEastingMetersInput =
                                        (EditText)field_container.findViewById(R.id.metersOutput);
        pointEastingMetersInput.setInputType(InputType.TYPE_CLASS_NUMBER |
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
                        mPointEastingMetersOld = temp;
                    }

                }
            }
        });



        if (mPointPath.equals(GBPath.sEditFromMaps)) {

            pointEastingMetersInput.setFocusable(false);
            pointEastingMetersInput.setBackgroundColor(
                                            ContextCompat.getColor(getActivity(), R.color.colorGray));
        }



        field_container = v.findViewById(R.id.northingContainer);
        if (GBGeneralSettings.isEN(myActivity)) {
            field_container = v.findViewById(R.id.eastingContainer);
        }

        label = (TextView) field_container.findViewById(R.id.en_label) ;
        label.setText(getString(R.string.northing_label));
        final EditText pointNorthingMetersInput = (EditText)field_container.findViewById(R.id.metersOutput);
        pointNorthingMetersInput.setInputType(InputType.TYPE_CLASS_NUMBER |
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
                         mPointNorthingMetersOld = temp;
                    }

                }
            }
        });


        if (mPointPath.equals(GBPath.sEditFromMaps)) {

            pointNorthingMetersInput.setFocusable(false);
            pointNorthingMetersInput.setBackgroundColor(
                    ContextCompat.getColor(getActivity(), R.color.colorGray));

        }



        field_container = v.findViewById(R.id.elevationContainer);
        final EditText pointENElevationMetersInput = (EditText)field_container.findViewById(R.id.elevationMetersInput);
        pointENElevationMetersInput.setInputType(InputType.TYPE_CLASS_NUMBER |
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
                        mPointENElevationMetersOld = temp;
                    }

                }
            }
        });



        if (mPointPath.equals(GBPath.sEditFromMaps)) {

            pointENElevationMetersInput.setFocusable(false);
            pointENElevationMetersInput.setBackgroundColor(
                    ContextCompat.getColor(getActivity(), R.color.colorGray));
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

        return GBCoordinate.getCoordinateCategoryFromProjectID(projectID);
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


        if (mPointBeingMaintained.getCoordinate() == null){
            pointOffsetDistInput.setEnabled(true);
            pointOffsetHeadInput.setEnabled(true);
            pointOffsetEleInput .setEnabled(true);

            pointOffsetDistInput.setBackgroundColor(Color.WHITE);
            pointOffsetEleInput.setBackgroundColor(Color.WHITE);
            pointOffsetHeadInput.setBackgroundColor(Color.WHITE);
        } else {
            pointOffsetDistInput.setEnabled(false);
            pointOffsetHeadInput.setEnabled(false);
            pointOffsetEleInput .setEnabled(false);

            pointOffsetDistInput.setBackgroundColor(Color.GRAY);
            pointOffsetEleInput.setBackgroundColor(Color.GRAY);
            pointOffsetHeadInput.setBackgroundColor(Color.GRAY);

        }
        EditText hdopView      = (EditText) v.findViewById(R.id.pointHdopInput);
        EditText vdopView      = (EditText) v.findViewById(R.id.pointVdopInput);

        EditText pdopView      = (EditText) v.findViewById(R.id.pointPdopInput);
        EditText hrmsView      = (EditText) v.findViewById(R.id.pointHrmsInput);
        EditText vrmsView      = (EditText) v.findViewById(R.id.pointVrmsInput);

        hdopView.setText(String.valueOf(mPointBeingMaintained.getHdop()));
        vdopView.setText(String.valueOf(mPointBeingMaintained.getVdop()));

        pdopView.setText(String.valueOf(mPointBeingMaintained.getPdop()));

        int digitsOfPrecision = GBGeneralSettings.getStdDevPrecision((GBActivity)getActivity());
        String hrmsString = GBUtilities.truncatePrecisionString(mPointBeingMaintained.getHrms(),
                                                                digitsOfPrecision);
        String vrmsString = GBUtilities.truncatePrecisionString(mPointBeingMaintained.getVrms(),
                                                                digitsOfPrecision);
        hrmsView.setText(hrmsString);
        vrmsView.setText(vrmsString);


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

        //The location fields order may be reversed on the screen
        //NOTE TO MAINTENANCE PROGRAMMER:
        // This can be tricky, so remember the kludge is to just change the names on the UI
        GBActivity myActivity = (GBActivity)getActivity();



        View field_container = v.findViewById(R.id.latitudeContainer);
        if (GBGeneralSettings.isLngLat(myActivity)) {
            field_container = v.findViewById(R.id.longitudeContainer);
        }
        EditText pointLatitudeDDInput = (EditText) field_container.findViewById(R.id.ll_dd_input);
        EditText pointLatitudeDInput  = (EditText) field_container.findViewById(R.id.ll_d_Input) ;
        EditText pointLatitudeMInput  = (EditText) field_container.findViewById(R.id.ll_m_Input);
        EditText pointLatitudeSInput  = (EditText) field_container.findViewById(R.id.ll_s_Input) ;

        mPointLatitudeDDOld = pointLatitudeDDInput.getText().toString();
        mPointLatitudeDOld  = pointLatitudeDInput.getText().toString();
        mPointLatitudeMOld  = pointLatitudeMInput.getText().toString();
        mPointLatitudeSOld  = pointLatitudeSInput.getText().toString();


        field_container = v.findViewById(R.id.longitudeContainer);
        if (GBGeneralSettings.isLngLat(myActivity)) {
            field_container = v.findViewById(R.id.latitudeContainer);
        }
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
        EditText pointGeoidMetersInput = (EditText) field_container.findViewById(R.id.geoidHeightMetersInput);


        mPointElevationMetersOld = pointElevationMetersInput.getText().toString();
        mPointGeoidMetersOld     = pointGeoidMetersInput.getText().toString();
    }

    private void saveENFieldsAsOldValues(){
        View v = getView();
        if (v ==null)return;

        //The location fields order may be reversed on the screen
        //NOTE TO MAINTENANCE PROGRAMMER:
        // This can be tricky, so remember the kludge is to just change the names on the UI

        GBActivity myActivity = (GBActivity)getActivity();


        View field_container = v.findViewById(R.id.eastingContainer);
        if (GBGeneralSettings.isEN(myActivity)) {
            field_container = v.findViewById(R.id.northingContainer);
        }
        EditText pointEastingMetersInput = (EditText)field_container.findViewById(R.id.metersOutput);

        field_container = v.findViewById(R.id.northingContainer);
        if (GBGeneralSettings.isEN(myActivity)) {
            field_container = v.findViewById(R.id.eastingContainer);
        }
        EditText pointNorthingMetersInput = (EditText)field_container.findViewById(R.id.metersOutput);

        field_container = v.findViewById(R.id.elevationContainer);
        EditText pointENElevationMetersInput = (EditText)field_container.findViewById(R.id.elevationMetersInput);

        field_container = v.findViewById(R.id.zhlContainer);
        EditText pointZoneInput       = (EditText) field_container.findViewById(R.id.zoneOutput);
        EditText pointHemisphereInput = (EditText) field_container.findViewById(R.id.hemisphereOutput);
        EditText pointLatbandInput    = (EditText) field_container.findViewById(R.id.latbandOutput);

        field_container = v.findViewById(R.id.convergenceContainer);
        EditText pointDatumInput       = (EditText) field_container.findViewById(R.id.datumOutput);
        EditText pointConvergenceInput = (EditText) field_container.findViewById(R.id.convergenceOutput);
        EditText pointScaleFactorInput = (EditText) field_container.findViewById(R.id.scaleFactorOutput);

        mPointEastingMetersOld = pointEastingMetersInput.getText().toString();
        mPointNorthingMetersOld = pointNorthingMetersInput.getText().toString();
        mPointENElevationMetersOld = pointENElevationMetersInput.getText().toString();
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


        GBActivity myActivity = (GBActivity)getActivity();
        GBProject openProject = GBUtilities.getInstance().getOpenProject(myActivity);



        //The location fields order may be reversed on the screen
        //NOTE TO MAINTENAINCE PROGRAMMER:
        // This can be tricky, so remember the kludge is to just change the names
        View field_container = v.findViewById(R.id.latitudeContainer);

        if (GBGeneralSettings.isLngLat(myActivity)) {
            field_container = v.findViewById(R.id.longitudeContainer);
        }

        EditText pointLatitudeHemInput = (EditText) field_container.findViewById(R.id.ll_dir);
        EditText pointLatitudeDDInput = (EditText) field_container.findViewById(R.id.ll_dd_input);
        EditText pointLatitudeDInput  = (EditText) field_container.findViewById(R.id.ll_d_Input) ;
        EditText pointLatitudeMInput  = (EditText) field_container.findViewById(R.id.ll_m_Input);
        EditText pointLatitudeSInput  = (EditText) field_container.findViewById(R.id.ll_s_Input) ;

        field_container = v.findViewById(R.id.longitudeContainer);
        if (GBGeneralSettings.isLngLat(myActivity)) {
            field_container = v.findViewById(R.id.latitudeContainer);
        }

        EditText pointLongitudeHemInput = (EditText) field_container.findViewById(R.id.ll_dir);
        EditText pointLongitudeDDInput = (EditText) field_container.findViewById(R.id.ll_dd_input);
        EditText pointLongitudeDInput  = (EditText) field_container.findViewById(R.id.ll_d_Input) ;
        EditText pointLongitudeMInput  = (EditText) field_container.findViewById(R.id.ll_m_Input);
        EditText pointLongitudeSInput  = (EditText) field_container.findViewById(R.id.ll_s_Input) ;


        field_container = v.findViewById(R.id.elevationGeoidContainer);
        EditText pointElevationMetersInput =
                (EditText) field_container.findViewById(R.id.elevationMetersInput);
        EditText pointGeoidMetersInput =
                (EditText) field_container.findViewById(R.id.geoidHeightMetersInput);


        field_container = v.findViewById(R.id.convergenceContainer);
        EditText pointConvergenceInput = (EditText) field_container.findViewById(R.id.convergenceOutput);
        EditText pointCAdegInput       = (EditText) field_container.findViewById(R.id.convDegreesInput);
        EditText pointCAminInput       = (EditText) field_container.findViewById(R.id.convMinutesInput);
        EditText pointCAsecInput       = (EditText) field_container.findViewById(R.id.convSecondsInput);
        EditText pointScaleFactorInput = (EditText) field_container.findViewById(R.id.scaleFactorOutput);


        boolean isDD = GBGeneralSettings.isLocDD(myActivity);
        boolean isDir = GBGeneralSettings.isDir(myActivity);

        int locDigOfPrecision = GBGeneralSettings.getLocPrecision(myActivity);
        if (isDD){
            double latitude  = coordinate.getLatitude();
            int posHemi = R.string.hemisphere_N;
            int negHemi = R.string.hemisphere_S;
            GBUtilities.locDD(myActivity, latitude, locDigOfPrecision,
                    isDir, posHemi, negHemi, pointLatitudeHemInput, pointLatitudeDDInput);

            double longitude = coordinate.getLongitude();
            posHemi = R.string.hemisphere_E;
            negHemi = R.string.hemisphere_W;
            GBUtilities.locDD(myActivity, longitude, locDigOfPrecision,
                    isDir, posHemi, negHemi, pointLongitudeHemInput, pointLongitudeDDInput);

            double convAngle = coordinate.getConvergenceAngle();
            GBUtilities.caDD(myActivity, convAngle, locDigOfPrecision, pointConvergenceInput);

        } else {
            int deg = coordinate.getLatitudeDegree();
            int min = coordinate.getLatitudeMinute();
            double sec = coordinate.getLatitudeSecond();

            int posHemi = R.string.hemisphere_N;
            int negHemi = R.string.hemisphere_S;

            GBUtilities.locDMS(myActivity, deg, min, sec, locDigOfPrecision,
                    isDir, posHemi, negHemi, pointLatitudeHemInput,
                    pointLatitudeDInput, pointLatitudeMInput, pointLatitudeSInput);

            deg = coordinate.getLongitudeDegree();
            min = coordinate.getLongitudeMinute();
            sec = coordinate.getLongitudeSecond();
            posHemi = R.string.hemisphere_E;
            negHemi = R.string.hemisphere_W;

            GBUtilities.locDMS(myActivity, deg, min, sec, locDigOfPrecision,
                    isDir, posHemi, negHemi, pointLongitudeDInput,
                    pointLongitudeDInput, pointLongitudeMInput, pointLongitudeSInput);


            int caDegOfPrecision = GBGeneralSettings.getCAPrecision(myActivity);
            deg = coordinate.getConvergenceAngleDegree();
            min = coordinate.getConvergenceAngleMinute();
            sec = coordinate.getConvergenceAngleSecond();
            GBUtilities.caDMS(myActivity,deg, min, sec,
                    caDegOfPrecision,
                    pointCAdegInput, pointCAminInput, pointCAsecInput);

        }


        double elevation = coordinate.getElevation();
        double geoid     = coordinate.getGeoid();

        GBUtilities.locDistance(myActivity, elevation, pointElevationMetersInput);
        GBUtilities.locDistance(myActivity, geoid, pointGeoidMetersInput);

        int sfPrecision  = GBGeneralSettings.getSfPrecision(myActivity);
        pointScaleFactorInput   .setText(truncatePrecisionString(sfPrecision, coordinate.getScaleFactor()));

    }

    private void initializeUIFromENCoordinate(View v, GBPoint point ){
        GBCoordinateEN coordinate = (GBCoordinateEN)point.getCoordinate();
        if (coordinate == null)return;

        GBActivity myActivity = (GBActivity)getActivity();

        GBProject openProject = GBUtilities.getInstance().getOpenProject(myActivity);

        View field_container = v.findViewById(R.id.eastingContainer);

        if (GBGeneralSettings.isEN(myActivity)) {
            field_container = v.findViewById(R.id.northingContainer);
        }
        EditText pointEastingMetersInput = (EditText)field_container.findViewById(R.id.metersOutput);



        field_container = v.findViewById(R.id.northingContainer);
        if (GBGeneralSettings.isEN(myActivity)) {
            field_container = v.findViewById(R.id.eastingContainer);
        }
        EditText pointNorthingMetersInput = (EditText)field_container.findViewById(R.id.metersOutput);

        field_container = v.findViewById(R.id.elevationContainer);
        EditText pointENElevationMetersInput = (EditText)field_container.findViewById(R.id.elevationMetersInput);

        field_container = v.findViewById(R.id.zhlContainer);
        TextView pointZoneLabel       = (TextView) field_container.findViewById(R.id.coord_label);
        EditText pointZoneInput       = (EditText) field_container.findViewById(R.id.zoneOutput);
        EditText pointHemisphereInput = (EditText) field_container.findViewById(R.id.hemisphereOutput);
        EditText pointLatbandInput    = (EditText) field_container.findViewById(R.id.latbandOutput);

        field_container = v.findViewById(R.id.convergenceContainer);
        TextView pointDataumLabel      = (TextView) field_container.findViewById(R.id.datumLabel);
        EditText pointDatumInput       = (EditText) field_container.findViewById(R.id.datumOutput);
        EditText pointConvergenceInput = (EditText) field_container.findViewById(R.id.convergenceOutput);
        EditText pointCAdegInput       = (EditText) field_container.findViewById(R.id.convDegreesInput);
        EditText pointCAminInput       = (EditText) field_container.findViewById(R.id.convMinutesInput);
        EditText pointCAsecInput       = (EditText) field_container.findViewById(R.id.convSecondsInput);
        EditText pointScaleFactorInput = (EditText) field_container.findViewById(R.id.scaleFactorOutput);

        field_container = v.findViewById(R.id.elevationContainer);
        EditText pointElevationMetersInput = (EditText) field_container.findViewById(R.id.elevationMetersInput);
        EditText pointGeoidMetersInput = (EditText) field_container.findViewById(R.id.geoidHeightMetersInput);


        int locPrecision = GBGeneralSettings.getLocPrecision(myActivity);
        int caPrecision  = GBGeneralSettings.getCAPrecision(myActivity);
        int sfPrecision  = GBGeneralSettings.getSfPrecision(myActivity);



        boolean isDD = GBGeneralSettings.isLocDD(myActivity);
        int locDigOfPrecision = GBGeneralSettings.getLocPrecision(myActivity);
        int caDigOfPrecision  = GBGeneralSettings.getCAPrecision(myActivity);

        int coordinateDBType = coordinate.getCoordinateDBType();
        if (coordinateDBType == GBCoordinate.sCoordinateDBTypeUTM){
            //fill in the UTM specific fields
            pointZoneInput.setText(String.valueOf(coordinate.getZone()));
            GBCoordinateUTM coordinateUTM = (GBCoordinateUTM) point.getCoordinate();
            char latBandChar    = coordinateUTM.getLatBand();
            char hemisphereChar = coordinateUTM.getHemisphere();

            pointLatbandInput.setText(String.valueOf(latBandChar));
            pointHemisphereInput.setText(String.valueOf(hemisphereChar));
            pointHemisphereInput.setBackgroundColor(ContextCompat.getColor(getActivity(),R.color.colorGrayish));

        } else if (coordinateDBType == GBCoordinate.sCoordinateDBTypeSPCS){
            //Zone is already on the screen, so get rid of this row
            pointZoneLabel      .setVisibility(View.GONE);
            pointZoneInput      .setVisibility(View.GONE);
            pointHemisphereInput.setVisibility(View.GONE);
            pointLatbandInput   .setVisibility(View.GONE);

            pointDataumLabel    .setVisibility(View.GONE);
            pointDatumInput     .setVisibility(View.GONE);
        }


        GBUtilities.locDistance(myActivity, coordinate.getEasting(),  pointEastingMetersInput);
        GBUtilities.locDistance(myActivity, coordinate.getNorthing(), pointNorthingMetersInput);

        GBUtilities.locDistance(myActivity, coordinate.getElevation(),pointElevationMetersInput);
        GBUtilities.locDistance(myActivity, coordinate.getGeoid(),    pointGeoidMetersInput);

        if (isDD){
            double convAngle = coordinate.getConvergenceAngle();
            GBUtilities.caDD(myActivity, convAngle, caDigOfPrecision, pointConvergenceInput);

        } else {

            int deg = coordinate.getConvergenceAngleDegree();
            int min = coordinate.getConvergenceAngleMinute();
            double sec = coordinate.getConvergenceAngleSecond();
            GBUtilities.caDMS(myActivity,deg, min, sec, caDigOfPrecision,
                    pointCAdegInput, pointCAminInput, pointCAsecInput);

        }


        pointScaleFactorInput .setText(truncatePrecisionString(sfPrecision, coordinate.getScaleFactor()));


        // TODO: 7/30/2017 is Datum necessiary to the UI? Should the field be eliminated?
        pointDatumInput.setText(String.valueOf(coordinate.getDatum()));


    }

    //truncate digits of precision
    private String truncatePrecisionString(int digitsOfPrecision, double reading) {
        return GBUtilities.truncatePrecisionString(reading, digitsOfPrecision);
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


