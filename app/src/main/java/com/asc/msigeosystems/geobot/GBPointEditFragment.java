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
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Date;

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

    public static GBPointEditFragment newInstance(long      projectID,
                                                  GBPath    pointPath,
                                                  GBPoint   point) {

        Bundle args = GBPoint.putPointInArguments(new Bundle(), projectID, point);
        args = GBPath.putPathInArguments(args, pointPath);

        GBPointEditFragment fragment = new GBPointEditFragment();

        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState){

        super.onCreate(savedInstanceState);

        GBPath path = GBPath.getPathFromArguments(getArguments());
        mPointPath       = path.getPath();

        mPointBeingMaintained = GBPoint.getPointFromArguments(getActivity(), getArguments());

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //Inflate the layout for this fragment
        View v = inflater.inflate(
                R.layout.fragment_point_edit_gb,
                container,
                false);


        //Wire up the UI widgets so they can handle events later
        //For now ignore the text view widgets, as this is just a mockup
        //      for the real screen we'll have to actually fill the fields
        wireWidgets(v);


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
            layoutInflater.inflate(R.layout.element_ll_coordinate, coordinatesContainer, true);
            //then wire up the appropriate widgets for this type of coordinate
            wireLLCoordinateWidgets(v);
        } else if (coordinateWidgetType == GBCoordinate.sENWidgets) {
            //inflate the coordinates, and
            // attach the coordinates to the LinearLayout which exists to contain the coordinates
            layoutInflater.inflate(R.layout.element_en_coordinate, coordinatesContainer, true);

            //then wire up the appropriate widgets for this type of coordinat
            wireENCoordinateWidgets(v);
        }

        initializeUI(v);

        initializeRecyclerView(v);

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


        //Point Description
        EditText pointFeatureCodeInput = (EditText) v.findViewById(R.id.pointFeatureCodeInput);
        pointFeatureCodeInput.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                setPointChangedFlags();
                return false;
            }
        });


        //Point Notes
        EditText pointNotesInput = (EditText) v.findViewById(R.id.pointNotesInput);
        pointNotesInput.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                setPointChangedFlags();
                return false;
            }
        });


        //Point Quality HDOP
        EditText pointHdopInput = (EditText) v.findViewById(R.id.pointHdopInput);
        pointHdopInput.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                setPointChangedFlags();
                return false;
            }
        });


        //Point Quality VDOP
        EditText pointVdopInput = (EditText) v.findViewById(R.id.pointVdopInput);
        pointVdopInput.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                setPointChangedFlags();
                return false;
            }
        });


        //Point Quality TDOP
        EditText pointTdopInput = (EditText) v.findViewById(R.id.pointTdopInput);
        pointTdopInput.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                setPointChangedFlags();
                return false;
            }
        });


        //Point Quality PDOP
        EditText pointPdopInput = (EditText) v.findViewById(R.id.pointPdopInput);
        pointPdopInput.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                setPointChangedFlags();
                return false;
            }
        });


        //Point Quality HRMS
        EditText pointHrmsInput = (EditText) v.findViewById(R.id.pointHrmsInput);
        pointHrmsInput.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                setPointChangedFlags();
                return false;
            }
        });


        //Point Quality VRMS
        EditText pointVrmsInput = (EditText) v.findViewById(R.id.pointVrmsInput);
        pointVrmsInput.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                setPointChangedFlags();
                return false;
            }
        });



        //Point Offset Distance
        EditText pointOffsetDistInput = (EditText) v.findViewById(R.id.pointOffDistInput);
        pointOffsetDistInput.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                setPointChangedFlags();
                return false;
            }
        });


        //Point Offset Heading
        EditText pointOffsetHeadInput = (EditText) v.findViewById(R.id.pointOffHeadInput);
        pointOffsetHeadInput.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                setPointChangedFlags();
                return false;
            }
        });


        //Point Offset Elevation
        EditText pointOffsetEleInput = (EditText) v.findViewById(R.id.pointOffEleInput);
        pointOffsetEleInput.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                setPointChangedFlags();
                return false;
            }
        });


        //Point Height
        EditText pointHeightInput = (EditText) v.findViewById(R.id.pointHeightInput);
        pointHeightInput.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                setPointChangedFlags();
                return false;
            }
        });


        //Coordinate type
        //TextView pointCoordinateTypeLabel = (TextView)v.findViewById(R.id.coordinate_label);


        //Exit Button
        Button pointExitButton = (Button) v.findViewById(R.id.pointExitButton);
        pointExitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                //hide the keyboard if it is visable
                GBUtilities constantsAndUtilities =
                                                        GBUtilities.getInstance();
                constantsAndUtilities.hideKeyboard(getActivity());

                if (mPointPath.equals(GBPath.sEditFromMaps)){
                    //pop back to the Collect Points Screen
                    ((GBActivity)getActivity()).
                                                popToScreen(GBActivity.sCollectPointsTag);
                } else {
                    ((GBActivity) getActivity()).popToTopCogoScreen();
                }
            }
        });



        //View Existing Points Button
        Button pointViewExistingButton = (Button) v.findViewById(R.id.pointViewExistingButton);
        if ((mPointPath.equals(GBPath.sCreateTag)) ||
            (mPointPath.equals(GBPath.sEditFromMaps)))    {
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


        //Save Changes Button
        Button pointSaveChangesButton = (Button) v.findViewById(R.id.pointSaveChangesButton);
        //button is enabled once something changes
        pointSaveChangesButton.setEnabled(false);
        pointSaveChangesButton.setTextColor(Color.GRAY);
        pointSaveChangesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                //ignore the return code. If successful, the save path is consumed, otherwise not
                onSave();
            }
        });
        pointSaveChangesButton.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
               // if(hasFocus) {}
            }
        });

        mPictureImage = (ImageView) v.findViewById(R.id.pictureImage);


    }

    private void wireLLCoordinateWidgets(View v){
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

        //***************************************************************************/
        //*******             Elevation                                      ********/
        //***************************************************************************/

        field_container = v.findViewById(R.id.elevationGeoidContainer);
        final EditText pointElevationMetersInput = (EditText) field_container.findViewById(R.id.elevationMetersInput);
        pointElevationMetersInput.setInputType(InputType.TYPE_CLASS_NUMBER |
                InputType.TYPE_NUMBER_FLAG_DECIMAL |
                InputType.TYPE_NUMBER_FLAG_SIGNED);
        final EditText pointElevationFeetInput   = (EditText) field_container.findViewById(R.id.elevationFeetInput) ;
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

    }


    private void wireENCoordinateWidgets(View v){

        View field_container;
        TextView label;

        GBActivity myActivity = (GBActivity)getActivity();


        //set up the widgets for the easting/northing oordinate


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



        field_container = v.findViewById(R.id.zhlContainer);
        //label = (TextView) field_container.findViewById(R.id.coord_label) ;
        //label.setText();
        EditText pointZoneInput       = (EditText) field_container.findViewById(R.id.zoneOutput);
        EditText pointHemisphereInput = (EditText) field_container.findViewById(R.id.hemisphereOutput);
        EditText pointLatbandInput    = (EditText) field_container.findViewById(R.id.latbandOutput);


        field_container = v.findViewById(R.id.convergenceContainer);
        EditText pointDatumInput       = (EditText) field_container.findViewById(R.id.datumOutput);
        EditText pointConvergenceInput = (EditText) field_container.findViewById(R.id.convergenceOutput);
        EditText pointScaleFactorInput = (EditText) field_container.findViewById(R.id.scaleFactorOutput);

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



    private CharSequence getFullCoordTypeFromProject(){
        if (mPointBeingMaintained == null)return "";
        //show the data that came out of the input arguments bundle
        long projectID = mPointBeingMaintained.getForProjectID();

        GBProjectManager projectManager = GBProjectManager.getInstance();
        GBProject project = projectManager.getProject(projectID);

        return project.getProjectCoordinateType();

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

        long projectID = mPointBeingMaintained.getForProjectID();
        pointProjectIDInput.setText(String.valueOf(projectID));
        pointIDInput.setText       (String.valueOf (mPointBeingMaintained.getPointID()));

        //Project name
        GBProjectManager projectManager = GBProjectManager.getInstance();
        GBProject project = projectManager.getProject(projectID);
        pointProjectNameInput.setText(project.getProjectName());

        //point ID
        if (mPointBeingMaintained.getPointID() != GBUtilities.ID_DOES_NOT_EXIST){

            pointFeatureCodeInput.setText(mPointBeingMaintained.getPointFeatureCode());
            pointNotesInput.setText(mPointBeingMaintained.getPointNotes());
        }


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

        //Height
        EditText pointHeightInput = (EditText) v.findViewById(R.id.pointHeightInput);
        pointHeightInput.setText(String.valueOf(mPointBeingMaintained.getHeight()));
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

        View field_container = v.findViewById(R.id.latitudeContainer);

        EditText pointLatitudeDDInput = (EditText) field_container.findViewById(R.id.ll_dd_input);
        EditText pointLatitudeDInput  = (EditText) field_container.findViewById(R.id.ll_d_Input) ;
        EditText pointLatitudeMInput  = (EditText) field_container.findViewById(R.id.ll_m_Input);
        EditText pointLatitudeSInput  = (EditText) field_container.findViewById(R.id.ll_s_Input) ;

        field_container = v.findViewById(R.id.elevationGeoidContainer);
        EditText pointElevationMetersInput = (EditText) field_container.findViewById(R.id.elevationMetersInput);
        EditText pointElevationFeetInput = (EditText) field_container.findViewById(R.id.elevationFeetInput);
        EditText pointGeoidMetersInput = (EditText) field_container.findViewById(R.id.geoidHeightMetersInput);
        EditText pointGeoidFeetInput = (EditText) field_container.findViewById(R.id.geoidHeightFeetInput);

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
        pointElevationFeetInput.setText(String.valueOf(coordinate.getElevationFeet()));

        pointGeoidMetersInput.setText(String.valueOf(geoid));
        pointGeoidFeetInput.setText(String.valueOf(coordinate.getGeoidFeet()));

        if (latitude < 0.0){
            pointLatitudeDDInput.setTextColor(ContextCompat.getColor(getActivity(), R.color.colorNegNumber));
            pointLatitudeDInput.setTextColor(ContextCompat.getColor(getActivity(),R.color.colorNegNumber));
            pointLatitudeMInput.setTextColor(ContextCompat.getColor(getActivity(), R.color.colorNegNumber));
            pointLatitudeSInput.setTextColor(ContextCompat.getColor(getActivity(),R.color.colorNegNumber));
        }
        if (longitude < 0.0){
            pointLongitudeDDInput.setTextColor(ContextCompat.getColor(getActivity(), R.color.colorNegNumber));
            pointLongitudeDInput.setTextColor(ContextCompat.getColor(getActivity(),R.color.colorNegNumber));
            pointLongitudeMInput.setTextColor(ContextCompat.getColor(getActivity(), R.color.colorNegNumber));
            pointLongitudeSInput.setTextColor(ContextCompat.getColor(getActivity(),R.color.colorNegNumber));
        }
        if (elevation < 0.0){
            pointElevationMetersInput.setTextColor(ContextCompat.getColor(getActivity(), R.color.colorNegNumber));
            pointElevationFeetInput.setTextColor(ContextCompat.getColor(getActivity(),R.color.colorNegNumber));
        }
        if (geoid < 0.0){
            pointGeoidMetersInput.setTextColor(ContextCompat.getColor(getActivity(), R.color.colorNegNumber));
            pointGeoidFeetInput.setTextColor(ContextCompat.getColor(getActivity(),R.color.colorNegNumber));
        }

    }

    private void initializeUIFromENCoordinate(View v, GBPoint point ){
        GBCoordinateEN coordinate = (GBCoordinateEN)point.getCoordinate();
        if (coordinate == null)return;

        double easting = coordinate.getEasting();
        double northing = coordinate.getNorthing();
        double elevation = coordinate.getElevation();

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

        field_container = v.findViewById(R.id.elevationGeoidContainer);
        EditText pointElevationMetersInput = (EditText) field_container.findViewById(R.id.elevationMetersInput);
        EditText pointElevationFeetInput = (EditText) field_container.findViewById(R.id.elevationFeetInput);
        EditText pointGeoidMetersInput = (EditText) field_container.findViewById(R.id.geoidHeightMetersInput);
        EditText pointGeoidFeetInput = (EditText) field_container.findViewById(R.id.geoidHeightFeetInput);


        pointEastingMetersInput.setText(String.valueOf(easting));
        pointEastingFeetInput.setText(String.valueOf(coordinate.getEastingFeet()));

        pointNorthingMetersInput.setText(String.valueOf(northing));
        pointNorthingFeetInput.setText(String.valueOf(coordinate.getNorthingFeet()));

        pointENElevationMetersInput.setText(String.valueOf(elevation));
        pointENElevationFeetInput.setText(String.valueOf(coordinate.getElevationFeet()));

        pointZoneInput.setText(String.valueOf(coordinate.getZone()));
        pointLatbandInput.setText(String.valueOf(coordinate.getLatBand()));
        pointHemisphereInput.setText(String.valueOf(coordinate.getHemisphere()));

        pointDatumInput.setText(String.valueOf(coordinate.getDatum()));
        pointConvergenceInput.setText(String.valueOf(coordinate.getConvergence()));
        pointScaleFactorInput.setText(String.valueOf(coordinate.getScale()));

        if (elevation < 0.0){
            pointElevationMetersInput.setTextColor(ContextCompat.getColor(getActivity(), R.color.colorNegNumber));
            pointElevationFeetInput.setTextColor(ContextCompat.getColor(getActivity(),R.color.colorNegNumber));
        }
        if (easting < 0.0){
            pointEastingMetersInput.setTextColor(ContextCompat.getColor(getActivity(), R.color.colorNegNumber));
            pointEastingFeetInput.setTextColor(ContextCompat.getColor(getActivity(),R.color.colorNegNumber));
        }
        if (northing < 0.0){
            pointNorthingMetersInput.setTextColor(ContextCompat.getColor(getActivity(), R.color.colorNegNumber));
            pointNorthingFeetInput.setTextColor(ContextCompat.getColor(getActivity(),R.color.colorNegNumber));
        }

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

        //Toast.makeText(getActivity(), "Picture Selected", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
        }

    }


    //**********************************************************/
    //*********     Called from Button Listeners     ***********/
    //**********************************************************/

    private boolean onSave(){
        if (mPointBeingMaintained == null)return false;
        boolean returnCode = true;

        //The point must have been changed for this button to work
        if (mPointChanged) {
            //saveChanges();

            //What happens here depends upon the path
            if (mPointPath.equals(GBPath.sCreateTag)){
                returnCode = addNewPoint();

            } else if (mPointPath.equals(GBPath.sEditTag)){

                returnCode = updatePointFromUI(mPointBeingMaintained);

                if (returnCode) {
                    //update the stored point
                    GBProject project = GBUtilities.getInstance().getOpenProject();
                    boolean addToDB = true;
                    GBPointManager pointManager = GBPointManager.getInstance();
                    pointManager.addPointsToProject(project, mPointBeingMaintained, addToDB);
                }
            }
            //all other paths do nothing

            if (returnCode) setPointSavedFlags();
        }
        return returnCode;

    }

    private boolean addNewPoint(){
        if (mPointBeingMaintained == null)return false;
        View v = getView();
        if (v == null)return false;

        boolean returnCode = true;

        long pointID, projectID;
        //assign a new point ID
        GBProjectManager projectManager = GBProjectManager.getInstance();
        projectID = mPointBeingMaintained.getForProjectID();
        GBProject project = projectManager.getProject(projectID);

        //Notice that this is the potential next id. The ID is not incremented yet
        //The ID will be assigned when the point is first saved to the DB
        pointID = GBUtilities.ID_DOES_NOT_EXIST;
        mPointBeingMaintained.setPointID(pointID);

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
        if (!pointManager.addPointsToProject(project, mPointBeingMaintained, addToDBToo)){
            Toast.makeText(getActivity(),
                            getString(R.string.error_adding_point),
                            Toast.LENGTH_SHORT).show();
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
            GBCoordinateSPCS newCoordinate = new GBCoordinateSPCS();
            returnCode = updateENCoordinateFromUI(v, point, newCoordinate);
        }

        if (returnCode) {
            //Project ID
            EditText pointFeatureCodeInput = (EditText) v.findViewById(R.id.pointFeatureCodeInput);
            EditText pointNotesInput       = (EditText) v.findViewById(R.id.pointNotesInput);

            point.setPointFeatureCode(pointFeatureCodeInput.getText().toString().trim());
            point.setPointNotes(pointNotesInput.getText().toString().trim());

            //There are several fields that can not be updated from this screen
            // TODO: 1/12/2017 Height, quality, offset fields should perhaps be written to the point object
            return true;
        } else {
            //Coordinate not valid
            Toast.makeText(getActivity(),
                    getString(R.string.coordinate_not_valid),
                    Toast.LENGTH_SHORT).show();

            return returnCode;
        }

    }


    //returns false if the coordinate is not valid
    private boolean updateLLCoordinateFromUI(View v, GBPoint point, GBCoordinateLL coordinate){
        if (mPointBeingMaintained == null)return false;
        long pointID = point.getPointID();
        long projectID = point.getForProjectID();

        View field_container = v.findViewById(R.id.latitudeContainer);

        EditText pointLatitudeDDInput = (EditText) field_container.findViewById(R.id.ll_dd_input);
        EditText pointLatitudeDInput  = (EditText) field_container.findViewById(R.id.ll_d_Input) ;
        EditText pointLatitudeMInput  = (EditText) field_container.findViewById(R.id.ll_m_Input);
        EditText pointLatitudeSInput  = (EditText) field_container.findViewById(R.id.ll_s_Input) ;

        field_container = v.findViewById(R.id.longitudeContainer);
        EditText pointLongitudeDDInput = (EditText) field_container.findViewById(R.id.ll_dd_input);
        EditText pointLongitudeDInput  = (EditText) field_container.findViewById(R.id.ll_d_Input) ;
        EditText pointLongitudeMInput  = (EditText) field_container.findViewById(R.id.ll_m_Input);
        EditText pointLongitudeSInput  = (EditText) field_container.findViewById(R.id.ll_s_Input) ;

        field_container = v.findViewById(R.id.elevationGeoidContainer);
        EditText pointElevationMetersInput = (EditText) field_container.findViewById(R.id.elevationMetersInput);
        EditText pointElevationFeetInput = (EditText) field_container.findViewById(R.id.elevationFeetInput);
        EditText pointGeoidMetersInput = (EditText) field_container.findViewById(R.id.geoidHeightMetersInput);
        EditText pointGeoidFeetInput = (EditText) field_container.findViewById(R.id.geoidHeightFeetInput);


        String latString   = pointLatitudeDDInput.getText().toString().trim();
        String longString  = pointLongitudeDDInput.getText().toString().trim();
        String eleString   = pointElevationMetersInput.getText().toString().trim();
        String geoidString = pointGeoidMetersInput.getText().toString().trim();

        String dLatString  = pointLatitudeDInput.getText().toString().trim();
        String mLatString  = pointLatitudeMInput.getText().toString().trim();
        String sLatString  = pointLatitudeSInput.getText().toString().trim();

        String dLngString  = pointLongitudeDInput.getText().toString().trim();
        String mLngString  = pointLongitudeMInput.getText().toString().trim();
        String sLngString  = pointLongitudeSInput.getText().toString().trim();
 /*
        if (dString.isEmpty())dString = getString(R.string.zero_decimal_string);
        if (mString.isEmpty())mString = getString(R.string.zero_decimal_string);
        if (sString.isEmpty())sString = getString(R.string.zero_decimal_string);
        if (dString.isEmpty())dString = getString(R.string.zero_decimal_string);
        if (mString.isEmpty())mString = getString(R.string.zero_decimal_string);
        if (sString.isEmpty())sString = getString(R.string.zero_decimal_string);
*/

        //if (eleString.isEmpty())eleString = getString(R.string.zero_decimal_string);
        //if (geoidString.isEmpty())geoidString = getString(R.string.zero_decimal_string);

        //The values on the screen must be valid to make a coordinate
         if ((projectID != GBUtilities.ID_DOES_NOT_EXIST)         &&
             (pointID != GBUtilities.ID_DOES_NOT_EXIST)           &&
            (!latString.isEmpty())  && (!longString.isEmpty())  &&
            (!eleString.isEmpty())  && (!geoidString.isEmpty()) &&
            (!dLatString.isEmpty()) && (!mLatString.isEmpty())  &&
            (!sLatString.isEmpty()) && (!dLngString.isEmpty())  &&
            (!mLngString.isEmpty()) && (!mLngString.isEmpty())  ) {

             coordinate.setProjectID(projectID);
             coordinate.setPointID(pointID);
             coordinate.setTime(new Date().getTime());


             //Latitude
            coordinate.setLatitude      (Double.parseDouble(latString));
            coordinate.setLatitudeDegree(Integer.parseInt(dLatString));
            coordinate.setLatitudeMinute(Integer.parseInt(mLatString));
            coordinate.setLatitudeSecond(Double.parseDouble(sLatString));

            //Longitude
            coordinate.setLongitude      (Double.parseDouble(longString));
            coordinate.setLongitudeDegree(Integer.parseInt(dLngString));
            coordinate.setLongitudeMinute(Integer.parseInt(mLngString));
            coordinate.setLongitudeSecond(Double.parseDouble(sLngString));

            coordinate.setElevation(Double.parseDouble(eleString));
            coordinate.setGeoid(Double.parseDouble(geoidString));

            mPointBeingMaintained.setHasACoordinateID(coordinate.getCoordinateID());
            mPointBeingMaintained.setCoordinate(coordinate);
            return true;
        }else {

            return false;
        }
    }

    private boolean updateENCoordinateFromUI(View v, GBPoint point, GBCoordinateEN coordinate){
        if (mPointBeingMaintained == null)return false;
        long pointID = point.getPointID();
        long projectID = point.getForProjectID();

        View field_container = v.findViewById(R.id.eastingContainer);
        EditText pointEastingMetersInput = (EditText)field_container.findViewById(R.id.metersOutput);
        //EditText pointEastingFeetInput = (EditText)field_container.findViewById(R.id.feetOutput);

        field_container = v.findViewById(R.id.northingContainer);
        EditText pointNorthingMetersInput = (EditText)field_container.findViewById(R.id.metersOutput);
        //EditText pointNorthingFeetInput = (EditText)field_container.findViewById(R.id.feetOutput);

        field_container = v.findViewById(R.id.elevationContainer);
        EditText pointENElevationMetersInput = (EditText)field_container.findViewById(R.id.elevationMetersInput);
        //EditText pointENElevationFeetInput   = (EditText)field_container.findViewById(R.id.elevationFeetInput);

        field_container = v.findViewById(R.id.zhlContainer);
        EditText pointZoneInput       = (EditText) field_container.findViewById(R.id.zoneOutput);
        EditText pointHemisphereInput = (EditText) field_container.findViewById(R.id.hemisphereOutput);
        EditText pointLatbandInput    = (EditText) field_container.findViewById(R.id.latbandOutput);

        field_container = v.findViewById(R.id.convergenceContainer);
        EditText pointDatumInput       = (EditText) field_container.findViewById(R.id.datumOutput);
        EditText pointConvergenceInput = (EditText) field_container.findViewById(R.id.convergenceOutput);
        EditText pointScaleFactorInput = (EditText) field_container.findViewById(R.id.scaleFactorOutput);
/*
        field_container = v.findViewById(R.id.elevationGeoidContainer);

        EditText pointElevationMetersInput = (EditText) field_container.findViewById(R.id.elevationMetersInput);
        EditText pointElevationFeetInput = (EditText) field_container.findViewById(R.id.elevationFeetInput);
        EditText pointGeoidMetersInput = (EditText) field_container.findViewById(R.id.geoidHeightMetersInput);
        EditText pointGeoidFeetInput = (EditText) field_container.findViewById(R.id.geoidHeightFeetInput);
*/



        String eastString        = pointEastingMetersInput.getText().toString().trim();
        String northString       = pointNorthingMetersInput.getText().toString().trim();
        String eleString         = pointENElevationMetersInput.getText().toString().trim();
        String zoneString        = pointZoneInput.getText().toString().trim();
        String latBandString     = pointLatbandInput.getText().toString().trim();
        String hemisphereString  = pointHemisphereInput.getText().toString().trim();
        String datumString       = pointDatumInput.getText().toString().trim();
        String convergenceString = pointConvergenceInput.getText().toString().trim();
        String scaleString       = pointScaleFactorInput.getText().toString().trim();

 /*
            if (zoneString.equals("")) zoneString = getString(R.string.default_zone);
            if (latBandString.equals(""))latBandString = getString(R.string.default_latband);
            if (hemisphere.equals(""))hemisphere = getString(R.string.default_hemisphere);
            if (convergence.equals(""))convergence=getString(R.string.zero_decimal_string);
            if (scale.equals(""))scale = getString(R.string.zero_decimal_string);

  */

        if ((projectID !=GBUtilities.ID_DOES_NOT_EXIST)                     &&
            (pointID != GBUtilities.ID_DOES_NOT_EXIST)                      &&
            (!eastString.isEmpty())        && (!northString.isEmpty())      &&
            (!eleString.isEmpty())         && (!zoneString.isEmpty()        &&
            (!latBandString.isEmpty())     && (!hemisphereString.isEmpty()) &&
            (!convergenceString.isEmpty()) && (!scaleString.isEmpty())      )) {

            coordinate.setProjectID(projectID);
            coordinate.setPointID(pointID);

            coordinate.setEasting  (Double.parseDouble(eastString));
            coordinate.setNorthing (Double.parseDouble(northString));
            coordinate.setElevation(Double.parseDouble(eleString));

            coordinate.setZone(Integer.parseInt(zoneString));
            coordinate.setLatBand((latBandString.charAt(0)));
            coordinate.setHemisphere((hemisphereString).charAt(0));
            coordinate.setDatum(datumString);
            coordinate.setConvergence(Double.parseDouble(convergenceString));
            coordinate.setScale(Double.parseDouble(scaleString));
            coordinate.setValidCoordinate(true);

            mPointBeingMaintained.setHasACoordinateID(coordinate.getCoordinateID());
            mPointBeingMaintained.setCoordinate(coordinate);
            return true;
        }else {
            //Coordinate not valid
            return false;
        }
    }


    private void onListPoints(){
        //what this button does depends upon the path  {create, open, copy, edit, show}
        //**************** CREATE ************************************************/
        if (mPointPath.equals(GBPath.sCreateTag)){
            Toast.makeText(getActivity(),
                    R.string.cant_view_points,
                    Toast.LENGTH_SHORT).show();
            maybeAskFirstListPoints();

        //************************** OPEN / COPY / EDIT / SHOW **************************************/
        } else if ((mPointPath.equals(GBPath.sOpenTag)) ||
                   (mPointPath.equals(GBPath.sCopyTag)) ||
                   (mPointPath.equals(GBPath.sEditTag)) ||
                   (mPointPath.equals(GBPath.sShowTag))) {

            maybeAskFirstListPoints();


        //************************************* UNKNOWN *************************/
        } else {
            Toast.makeText(getActivity(),
                    R.string.unrecognized_path_encountered,
                    Toast.LENGTH_SHORT).show();

            //Don't really know what to do here, but switch path to show and continue
            mPointPath = GBPath.sShowTag;
            maybeAskFirstListPoints();

        }
    }

    private void maybeAskFirstListPoints(){
        if (mPointChanged){
            //ask the user if should continue
            areYouSureListPoints();

        } else {
            Toast.makeText(getActivity(),
                    R.string.point_unchanged,
                    Toast.LENGTH_SHORT).show();


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
                                Toast.makeText(getActivity(),
                                        R.string.continue_abandon_changes,
                                        Toast.LENGTH_SHORT).show();

                                switchToListPoints();
                            }
                        })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                        Toast.makeText(getActivity(),
                                "Pressed Cancel",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .setIcon(R.drawable.ground_station_icon)
                .show();
    }

    private void switchToListPoints(){
        if (mPointBeingMaintained == null)return;
        GBActivity myActivity = (GBActivity) getActivity();
        myActivity.switchToListPointsScreen(mPointBeingMaintained.getForProjectID(),
                new GBPath(mPointPath));
    }


    //**********************************************************/
    //****     Maintain State Flags in this Fragment     *******/
    //**********************************************************/

    private void setPointChangedFlags(){
        View v = getView();
        if (v == null)return;

        Button pointSaveChangesButton = (Button)v.findViewById(R.id.pointSaveChangesButton);

        mPointChanged = true;
        //enable the enter button as the default is NOT enabled/grayed out

        //enable the save changes button too
        //But only if on the Create or Edit path
        if ((mPointPath.equals(GBPath.sCreateTag))||
            (mPointPath.equals(GBPath.sEditTag))) {
            pointSaveChangesButton.setEnabled(true);
            pointSaveChangesButton.setTextColor(Color.BLACK);
        }
    }

    private void setPointSavedFlags(){
        View v = getView();
        if (v == null)return;

        Button pointSaveChangesButton = (Button)v.findViewById(R.id.pointSaveChangesButton);

        mPointChanged = false;

        //enable the save changes button too
        pointSaveChangesButton.setEnabled(false);
        pointSaveChangesButton.setTextColor(Color.GRAY);
    }


}


