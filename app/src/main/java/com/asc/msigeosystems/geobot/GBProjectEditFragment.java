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
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Date;

/**
 * The Update Project Fragment
 * is passed a project on startup. The project attribute fields are
 * pre-populated prior to updating the project
 *
 * Created by Elisabeth Huhn on 4/13/2016.
 */
public class GBProjectEditFragment extends    Fragment {

    private static final String TAG = "EDIT_PROJECT_FRAGMENT";


    //**********************************************************/
    //*****  Coordinate types for Spinner Widgets     **********/
    //**********************************************************/



    private String   mSelectedCoordinateType;


    //set defaults
    private int      mSelectedCoordinateTypePosition;
    private int      mSelectedDistUnitPosition      = GBProject.sMeters;
    private int      mSelectedENvNEPosition         = GBProject.sNE;
    private int      mSelectedLatLngPosition        = GBProject.sLatLng;
    private int      mSelectedRMSvStdDevPosition    = GBProject.sStdDev;
    private int      mSelectedDDvDMSPosition        = GBProject.sDMS;
    private int      mSelectedDirVPlusMinusPosition = GBProject.sDirections;



    //**********************************************************************/
    //*********   Member Variables  ****************************************/
    //**********************************************************************/

    // TODO: 7/1/2017 configuration change might be problematic with complex variables like project
    private GBProject    mProjectBeingMaintained;

    private boolean      mProjectChanged = false;
    private CharSequence mProjectPath;

    //Constructor
    public GBProjectEditFragment() {
        //for now, we don't need to initialize anything when the fragment
        //  is first created with this constructor
    }

    //newInstance() stores the passed parameters in the fragments argument bundle
    public static GBProjectEditFragment newInstance(GBPath projectPath) {

        Bundle args = new Bundle();

        //put the path into the same bundle
        args = GBPath.putPathInArguments(args, projectPath);

        GBProjectEditFragment fragment = new GBProjectEditFragment();

        fragment.setArguments(args);
        return fragment;
    }


    //**********************************************************/
    //****     Lifecycle Methods                         *******/
    //**********************************************************/

    //pull the arguments out of the fragment bundle and store in the member variables
    //In this case, prepopulate the path (create/update/etc) and project
    @Override
    public void onCreate(Bundle savedInstanceState){

        super.onCreate(savedInstanceState);

        mProjectBeingMaintained = GBUtilities.getInstance().getOpenProject((GBActivity)getActivity());
        if (mProjectBeingMaintained == null){
            mProjectBeingMaintained = new GBProject();
        }

        GBPath path             = GBPath.getPathFromArguments(getArguments());
        mProjectPath            = path.getPath();

        //put this info on the screen in initializeUI() after the view has been created

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //Inflate the layout for this fragment
        View v = inflater.inflate( R.layout.fragment_project_edit_gb, container,  false);


        //Wire up the UI widgets so they can handle events later
        wireWidgets(v);

        wireCoordinateSpinner(v);
        wireSpinners(v);

        // TODO: 6/30/2017 make this the list of points, not the list of pictures
        //initializeRecyclerView(v);


        //If we had any arguments passed, update the screen with them
        initializeUI(v);

        //set the title bar subtitle
        setSubtitle();

        return v;
    }

    @Override
    public void onResume(){
        super.onResume();
        setSubtitle();
    }


    //**********************************************************/
    //****     Initialize                                *******/
    //**********************************************************/
    private void wireWidgets(View v){
        if (mProjectBeingMaintained == null)return;

        //For now ignore the text view widgets, as this is just a mockup
        //      for the real screen we'll have to actually fill the fields

        //Project Name
        EditText projectNameInput = (EditText) v.findViewById(R.id.projectNameInput);
        projectNameInput.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                setProjectChangedFlags();
                return false;
            }
        });

        //Project ID
        //TextView projectIDLabel = (TextView) v.findViewById(R.id.projectIDLabel);
        if (mProjectPath == GBPath.sCreateTag){
            //projectIDLabel.setText(getString(R.string.project_id_will_be_label));
        }
        EditText projectIDInput = (EditText) v.findViewById(R.id.projectIDInput);
        //Shouldn't be able to change ID
        projectIDInput.setFocusable(false);
        projectIDInput.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                setProjectChangedFlags();
                return false;
            }

        });

        //Project Creation Date
        EditText projectDateInput = (EditText) v.findViewById(R.id.projectCreationDateInput);
        projectDateInput.setFocusable(false);
        /*
        mProjectDateInput.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                setProjectChangedFlags();
                return false;
            }
        });
        */

        //Project Last Modified Date
        EditText projectMaintInput = (EditText) v.findViewById(R.id.projectModifiedDateInput);
        projectMaintInput.setFocusable(false);

        /*
       mProjectMaintInput.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                setProjectChangedFlags();
                return false;
            }
        });
        */

        //Project Number of points
        EditText projectNumPtsOutput = (EditText) v.findViewById(R.id.projectNumPointsOutput);
        projectNumPtsOutput.setFocusable(false);

        //Project Description
        EditText projectDescInput = (EditText) v.findViewById(R.id.projectDescInput);
        projectDescInput.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                setProjectChangedFlags();
                return false;
            }
        });

        //Coordinate Type
       // mProjectCoordTypeOutput = (EditText)v.findViewById(R.id.projectCoordTypeInput);
        //mProjectCoordTypeOutput.setFocusable(false);






        //View Existing Projects Button
        final Button projectViewExistingButton = (Button) v.findViewById(R.id.projectViewExistingButton);
        if (mProjectPath.equals(GBPath.sCreateTag)){
            //disable the button on the create path
            projectViewExistingButton.setEnabled(false);
            projectViewExistingButton.setTextColor(Color.GRAY);
        }
        projectViewExistingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){

                viewProjects();

            }
        });


        //List Points Button
        final Button projectListPointsButton = (Button) v.findViewById(R.id.projectListPointsButton);
        //in order to view the points of this project,
        // the project must already exist, and actually have points
        boolean hasPoints = false;
        if (mProjectBeingMaintained.getSize() > 0)hasPoints = true;
        if ((mProjectPath.equals(GBPath.sCreateTag)) || (!hasPoints) ){
            projectListPointsButton.setEnabled(false);
            projectListPointsButton.setTextColor(Color.GRAY);
        } else {
            projectListPointsButton.setEnabled(true);
            projectListPointsButton.setTextColor(Color.BLACK);
        }

        projectListPointsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mProjectChanged){
                    //need to ask first about abandoning changes
                    areYouSureListPoints();
                } else {
                    switchToListPoints();

                }

            }
        });


        //Save Changes Button
        Button projectSaveChangesButton = (Button) v.findViewById(R.id.projectSaveChangesButton);
        //button is enabled once something changes
        projectSaveChangesButton.setEnabled(false);
        projectSaveChangesButton.setTextColor(Color.GRAY);
        projectSaveChangesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){

                //The project must have been changed for this button to work
                if (mProjectChanged) {
                    onSave();

                    setProjectSavedFlags();
                    //For now, stay around after save
                    //((GBActivity) getActivity()).popToTopProjectScreen();

                }
            }
        });

        //Map Button
        Button projectMapButton = (Button) v.findViewById(R.id.projectMapButton);
        //button is always enabled
        projectMapButton.setEnabled(true);
        projectMapButton.setTextColor(Color.BLACK);
        projectMapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){

               //regardless of whether they actually exit, hide the keyboard
                GBUtilities.getInstance().hideKeyboard(getActivity());

                onMap();
            }
        });

        //Measure Button
        Button projectMeasureButton = (Button) v.findViewById(R.id.projectMeasureButton);
        //button is always enabled
        projectMeasureButton.setEnabled(true);
        projectMeasureButton.setTextColor(Color.BLACK);
        projectMeasureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){

                //regardless of whether they actually exit, hide the keyboard
                GBUtilities.getInstance().hideKeyboard(getActivity());

                onMeasure();
            }
        });
        //Export Button
        Button projectExportButton = (Button) v.findViewById(R.id.projectExportButton);
        //button is always enabled
        projectExportButton.setEnabled(true);
        projectExportButton.setTextColor(Color.BLACK);
        projectExportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){

                //regardless of whether they actually exit, hide the keyboard
                GBUtilities.getInstance().hideKeyboard(getActivity());

                onExport();
            }
        });
    }

    private void wireSpinners(View v){
        if (mProjectBeingMaintained == null)return;

        //
        //Distance Units
        //
        //set the defaults
        mSelectedDistUnitPosition      = GBProject.sMeters;
        mSelectedENvNEPosition         = GBProject.sNE;
        mSelectedLatLngPosition        = GBProject.sLatLng;
        mSelectedRMSvStdDevPosition    = GBProject.sRMS;
        mSelectedDDvDMSPosition        = GBProject.sDD;
        mSelectedDirVPlusMinusPosition = GBProject.sDirections;

        // TODO: 6/29/2017 conditional envne versus latlngvlnglat only one of these on the screen
        //Create the array of spinner choices from the Types of Coordinates defined
        String [] distanceUnits = new String[]{GBProject.sMetersString, GBProject.sFeetString};
        String [] enVne         = new String[]{GBProject.sENString,     GBProject.sNEString};
        String [] latLngVlngLat = new String[]{GBProject.sLatLngString, GBProject.sLngLatString};
        String [] rmsVstddev    = new String[]{GBProject.sRMSString,    GBProject.sStdDevString};
        String [] ddVdms        = new String[]{GBProject.sDDString,     GBProject.sFeetString};
        String [] dirVplusminus = new String[]{GBProject.sDirectionsString, GBProject.sPlusMinusString};


        //Then initialize the spinners themselves
        Spinner distUnitsSpinner     = (Spinner) v.findViewById(R.id.distance_units_spinner);
        Spinner enVneSpinner         = (Spinner) v.findViewById(R.id.en_v_ne_spinner);
        //Spinner latLngvLngLatSpinner = (Spinner) v.findViewById(R.id.);
        Spinner rmsVstddevSpinner    = (Spinner) v.findViewById(R.id.rms_v_stddev_spinner);
        Spinner ddVdmsSpinner        = (Spinner) v.findViewById(R.id.dd_v_dms_spinner);
        Spinner dirVplusminusSpinner = (Spinner) v.findViewById(R.id.dir_v_plusminus_spinner);

        // Create the ArrayAdapters using the Activities context AND
        // the string array and a default spinner layout
        ArrayAdapter<String> duAdapter = new ArrayAdapter<>(getActivity(),
                                                            android.R.layout.simple_spinner_item,
                                                            distanceUnits);

        // This is actually conditional about whether EN or LatLng
        ArrayAdapter<String> enAdapter = new ArrayAdapter<>(getActivity(),
                                                            android.R.layout.simple_spinner_item,
                                                            enVne);

        ArrayAdapter<String> rmsAdapter = new ArrayAdapter<>(getActivity(),
                                                            android.R.layout.simple_spinner_item,
                                                            rmsVstddev);

        ArrayAdapter<String> ddAdapter = new ArrayAdapter<>(getActivity(),
                                                            android.R.layout.simple_spinner_item,
                                                            ddVdms);

        ArrayAdapter<String> dirAdapter = new ArrayAdapter<>(getActivity(),
                                                            android.R.layout.simple_spinner_item,
                                                            dirVplusminus);


        // Specify the layout to use when the list of choices appears
        duAdapter .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        enAdapter .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        rmsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ddAdapter .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dirAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the spinner
        distUnitsSpinner    .setAdapter(duAdapter);
        enVneSpinner        .setAdapter(enAdapter);
        rmsVstddevSpinner   .setAdapter(rmsAdapter);
        ddVdmsSpinner       .setAdapter(ddAdapter);
        dirVplusminusSpinner.setAdapter(dirAdapter);

        distUnitsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (canMakeChange()) {
                    mSelectedDistUnitPosition = position;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        enVneSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (canMakeChange()) {
                    mSelectedENvNEPosition = position;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        rmsVstddevSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mSelectedRMSvStdDevPosition = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        ddVdmsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (canMakeChange()) {
                    mSelectedDDvDMSPosition = position;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        dirVplusminusSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (canMakeChange()) {
                    mSelectedDirVPlusMinusPosition = position;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        //The size of a project is it's number of points
        if (mProjectBeingMaintained.getSize() > 0){
            //can't change if any points are on the project
            distUnitsSpinner.setEnabled(false);
            distUnitsSpinner.setClickable(false);
            distUnitsSpinner.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.colorGray));

            enVneSpinner.setEnabled(false);
            enVneSpinner.setClickable(false);
            enVneSpinner.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.colorGray));

            ddVdmsSpinner.setEnabled(false);
            ddVdmsSpinner.setClickable(false);
            ddVdmsSpinner.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.colorGray));

            dirVplusminusSpinner.setEnabled(false);
            dirVplusminusSpinner.setClickable(false);
            dirVplusminusSpinner.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.colorGray));


        }


        //mPointCoordinateTypePrompt = (TextView) v.findViewById(R.id.coordinate_prompt);

    }

    private boolean canMakeChange(){
        long projectID = mProjectBeingMaintained.getProjectID();
        if (projectID != GBUtilities.ID_DOES_NOT_EXIST){
            int size = mProjectBeingMaintained.getSize();
            if (size > 0){
                //GBUtilities.getInstance().showStatus(getActivity(),R.string.project_can_not_change);
                return false;
            }
        }
        return true;

    }

    private void wireCoordinateSpinner(View v){
        if (mProjectBeingMaintained == null)return;

        //set the default
        mSelectedCoordinateType = GBCoordinate.sCoordinateTypeClassUTM;

        //Create the array of spinner choices from the Types of Coordinates defined
        String [] coordinateTypes = new String[]{getString(R.string.enter_coordinate_type),
                                                 GBCoordinate.sCoordinateTypeWGS84,
                                                 GBCoordinate.sCoordinateTypeNAD83,
                                                 GBCoordinate.sCoordinateTypeUTM,
                                                 GBCoordinate.sCoordinateTypeSPCS };

        //Then initialize the spinner itself
        Spinner coordSpinner = (Spinner) v.findViewById(R.id.coordinate_type_spinner);

        // Create an ArrayAdapter using the Activities context AND
        // the string array and a default spinner layout
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(),
                                                          android.R.layout.simple_spinner_item,
                                                          coordinateTypes);

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        coordSpinner.setAdapter(adapter);

        //attach the listener to the spinner
        coordSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mSelectedCoordinateTypePosition = position;
                mSelectedCoordinateType = (String) parent.getItemAtPosition(position);

                setProjectChangedFlags();

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //The size of a project is it's number of points
        if (mProjectBeingMaintained.getSize() > 0){
            //can't change the coordinate type if any points are on the project
            coordSpinner.setEnabled(false);
            coordSpinner.setClickable(false);
            coordSpinner.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.colorGray));
        }


        //mPointCoordinateTypePrompt = (TextView) v.findViewById(R.id.coordinate_prompt);

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
       if (mProjectBeingMaintained == null)return;
        v.setTag(TAG);

        //2) find and remember the RecyclerView
        RecyclerView recyclerView = (RecyclerView) v.findViewById(R.id.pictureList);


        // The RecyclerView.LayoutManager defines how elements are laid out.
        //3) create and assign a layout manager to the recycler view
        RecyclerView.LayoutManager layoutManager  = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);

        //4) create some dummy data and tell the adapter about it
        //  this is done in the singleton container

        //Get this projects points
        ArrayList <GBPicture> pictureList = mProjectBeingMaintained.getPictures();


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
                new GBPointListFragment.RecyclerTouchListener(getActivity(), recyclerView,
                                                new GBPointListFragment.ClickListener() {

                    @Override
                    public void onClick(View view, int position) {
                        onSelectPicture(position);
                    }

                    @Override
                    public void onLongClick(View view, int position) {
                        //for now, ignore the long click
                    }
                }));
    }



    private void initializeUI(View v) {
        if (mProjectBeingMaintained == null)return;

        //show the data that came out of the input arguments bundle
        EditText projectNameInput  = (EditText) v.findViewById(R.id.projectNameInput);
        EditText projectIDInput    = (EditText) v.findViewById(R.id.projectIDInput);
        EditText projectDateInput  = (EditText) v.findViewById(R.id.projectCreationDateInput);
        EditText projectMaintInput = (EditText) v.findViewById(R.id.projectModifiedDateInput);
        EditText projectNumPtsOutput = (EditText) v.findViewById(R.id.projectNumPointsOutput);
        EditText projectDescInput  = (EditText) v.findViewById(R.id.projectDescInput);

        EditText projectLocPrecisionInput = (EditText) v.findViewById(R.id.projectLocPrecisionInput);
        EditText projectStdDevPrecisionInput = (EditText) v.findViewById(R.id.projectStdDevPrecisionInput);


        //a new id is not assigned until the first save
        if (mProjectPath.equals(GBPath.sCreateTag)){
            long nextID = GBProject.getPotentialNextID((GBActivity)getActivity());
            projectIDInput.setText(String.valueOf(nextID));
        } else {
            projectIDInput.setText(String.valueOf(mProjectBeingMaintained.getProjectID()));
        }
        projectNameInput .setText(mProjectBeingMaintained.getProjectName());
        projectDateInput .setText(mProjectBeingMaintained.getDateString(
                                                mProjectBeingMaintained.getProjectDateCreated()));
        projectMaintInput.setText(mProjectBeingMaintained.getDateString(
                                                mProjectBeingMaintained.getProjectLastModified()));

        int numberOfPoints = mProjectBeingMaintained.getSize();
        projectNumPtsOutput.setText(String.valueOf(numberOfPoints));

        projectDescInput .setText(mProjectBeingMaintained.getProjectDescription());
        //mProjectCoordTypeOutput.setText(mProjectBeingMaintained.getProjectCoordinateType());

        Spinner coordSpinner = (Spinner) v.findViewById(R.id.coordinate_type_spinner);
        CharSequence spinnerSelection = mProjectBeingMaintained.getProjectCoordinateType();
        //NOTE: THIS NEXT SECTION MUST BE IN THE SAME ORDER AS THAT WHEN THE SPINNER
        //IS CREATED. THE ORDERING IS ENFORCED BY THE PROGRAMMER AT CODING TIME
        //THERE IS NOTHING AUTOMATIC ABOUT THIS HARD CODING
        //I'm not proud of it, but it works, so.......... be it
        String selectedCoordinateType;
        int    selectedCoordinateTypePosition;
        if (spinnerSelection.equals(GBCoordinate.sCoordinateTypeNAD83)) {
            selectedCoordinateType = GBCoordinate.sCoordinateTypeClassNAD83;
            selectedCoordinateTypePosition = 2;
            coordSpinner.setSelection(2);
        } else if (spinnerSelection == GBCoordinate.sCoordinateTypeUTM) {
            selectedCoordinateType = GBCoordinate.sCoordinateTypeUTM;
            selectedCoordinateTypePosition = 3;
            coordSpinner.setSelection(3);
        } else if (spinnerSelection == GBCoordinate.sCoordinateTypeSPCS){
            selectedCoordinateType = GBCoordinate.sCoordinateTypeSPCS;
            selectedCoordinateTypePosition = 4;
            coordSpinner.setSelection(4);
        } else  { //Use WGS84 as the default
            selectedCoordinateType = GBCoordinate.sCoordinateTypeWGS84;
            selectedCoordinateTypePosition = 1;
            coordSpinner.setSelection(1);

            //update the project with this new default
            mProjectBeingMaintained.setProjectCoordinateType(selectedCoordinateType);
            setProjectChangedFlags();
        }

        mSelectedCoordinateType         = selectedCoordinateType;
        mSelectedCoordinateTypePosition = selectedCoordinateTypePosition;


        //mPointCoordinateTypePrompt.setText();
        projectLocPrecisionInput.setText(String.valueOf(mProjectBeingMaintained.getLocPrecision()));
        projectStdDevPrecisionInput.setText(String.valueOf(mProjectBeingMaintained.getStdDevPrecision()));

    }

    private void setSubtitle(){
        String msg;

        if (mProjectPath.equals(GBPath.sOpenTag)) {
            msg = getString(R.string.subtitle_open_project);
        } else if (mProjectPath.equals(GBPath.sCopyTag)) {
            msg = getString(R.string.subtitle_copy_project);
        } else if (mProjectPath.equals(GBPath.sCreateTag)) {
            msg = getString(R.string.subtitle_create_project);
        } else if (mProjectPath.equals(GBPath.sDeleteTag)) {
            msg = getString(R.string.subtitle_delete_project);
        } else if (mProjectPath.equals(GBPath.sEditTag)) {
            msg = getString(R.string.subtitle_edit_project);
        } else {
            msg = getString(R.string.subtitle_error_in_path);
        }

        ((GBActivity) getActivity()).setSubtitle(msg);

    }



    //**********************************************************/
    //****     Respond to UI events                      *******/
    //**********************************************************/

    private void onSelectPicture(int position){
        //Toast.makeText(getActivity(), "Picture Selected", Toast.LENGTH_SHORT).show();
        View v = getView();
        if (v == null)return;

        RecyclerView recyclerView = (RecyclerView) v.findViewById(R.id.pictureList);
        GBPictureAdapter adapter = (GBPictureAdapter)recyclerView.getAdapter();

        GBPicture picture = adapter.getPicture(position);
        String pathToPicture = picture.getPathName();
        Bitmap bitmap = BitmapFactory.decodeFile(pathToPicture);

        ImageView pictureImage = (ImageView)v.findViewById(R.id.pictureImage);

        if (bitmap != null) {
            pictureImage.setImageBitmap(bitmap);

            //for some reason, showing the image blanks out the recycler view, so force a redraw
            recyclerView.getRecycledViewPool().clear();
            //mAdapter.notifyDataSetChanged();
            recyclerView.invalidate();
        } else {
            String msg = getString(R.string.missing_picture_file)+ " " + pathToPicture;
            Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
        }

    }


    private void onMap(){

        //((GBActivity)getActivity()).switchToProjectListScreen(new GBPath(GBPath.sCreateTag));
        GBUtilities.getInstance().showStatus(getActivity(), "Mapping of project points not yet supported");
    }

    private void onMeasure(){

        GBProject project = GBUtilities.getInstance().getOpenProject((GBActivity)getActivity());
        if (project == null){
            GBUtilities.getInstance().showStatus(getActivity(), R.string.project_not_open);
            return;
        }
        ((GBActivity)getActivity()).switchToPointCreateScreen(project);

    }

    private void onExport(){

        GBProject project = GBUtilities.getInstance().getOpenProject((GBActivity)getActivity());
        if (project == null){
            GBUtilities.getInstance().showStatus(getActivity(), R.string.project_not_open);
            return;
        }
        //((GBActivity)getActivity()).switchToPointCreateScreen(project);
        GBUtilities.getInstance().showStatus(getActivity(), "Export of project points not yet supported");


    }

    void onExit() {
        ((GBActivity) getActivity()).switchToHomeScreen();
    }

    //***********************************/
    //****     Save Button        *******/
    //***********************************/
    private GBProject onSave() {
        if (mProjectBeingMaintained == null) return null;

        View v = getView();
        if (v == null)return null;

        GBProjectManager projectManager = GBProjectManager.getInstance();

        //****************** CREATE **************************************/
        if (mProjectPath.equals(GBPath.sCreateTag)){
            GBUtilities.getInstance().showStatus(getActivity(), R.string.save_new_project);


            //returns false if there is something wrong with information on the screen and
            //the project can not be updated
            if (updateProjectFromUIFields(mProjectBeingMaintained)) {
                //need to assign a project ID
                long projectID = GBProject.getNextProjectID((GBActivity)getActivity());
                mProjectBeingMaintained.setProjectID(projectID);
                //Make the Project Settings ID match the project ID
                GBProjectSettings projectSettings = mProjectBeingMaintained.getSettings();
                if (projectSettings == null){
                    //provide default settings if necessary
                    projectSettings = new GBProjectSettings(projectID);
                    mProjectBeingMaintained.setSettings(projectSettings);
                } else {
                    projectSettings.setProjectID(projectID);
                }

                //This saves the project. does a cascade add/update
                boolean addToDBToo = true;
                boolean cascadeFlag = true;
                projectManager.addProject(mProjectBeingMaintained, addToDBToo, cascadeFlag);
                //change the path to edit
                mProjectPath = GBPath.sEditTag;

                //change the ID field label
                //TextView projectIDLabel = (TextView) v.findViewById(R.id.projectIDLabel);
                //projectIDLabel.setText(R.string.project_id_label);

                //enable the list points button if there are any points
                Button projectListPointsButton =
                                             (Button) v.findViewById(R.id.projectListPointsButton) ;
                if (mProjectBeingMaintained.getSize() > 0) {
                    projectListPointsButton.setEnabled(true);
                    projectListPointsButton.setTextColor(Color.BLACK);
                }

                GBUtilities.getInstance().setOpenProject((GBActivity)getActivity(), mProjectBeingMaintained);

            }

        //************************************* Edit *****************************/
        } else if (mProjectPath.equals(GBPath.sEditTag)){
            if (mProjectBeingMaintained == null){
               Toast.makeText(getActivity(),
                              R.string.project_missing_exception,
                              Toast.LENGTH_SHORT).show();
                throw new RuntimeException(getString(R.string.project_missing_exception));
            }
            Toast.makeText(getActivity(),
                    R.string.save_existing_project,
                    Toast.LENGTH_SHORT).show();


            if (updateProjectFromUIFields(mProjectBeingMaintained)){
                boolean addToDBToo = true;
                boolean cascadeFlag = true;//cascade to pictures and sesttngs
                projectManager.addProject(mProjectBeingMaintained, addToDBToo, cascadeFlag);
            }

            GBUtilities.getInstance().setOpenProject((GBActivity)getActivity(), mProjectBeingMaintained);


            //**************************   COPY **************************************/
        } else if (mProjectPath.equals(GBPath.sCopyTag)){
            //mProjectBeingMaintained will be null before it is saved for the first time
            //might have been created with the save changes button
            if (mProjectBeingMaintained == null){
                EditText projectIDInput   = (EditText) v.findViewById(R.id.projectIDInput);
                EditText projectNameInput = (EditText) v.findViewById(R.id.projectNameInput);
                String projectIDString    = projectIDInput.getText().toString();
                mProjectBeingMaintained   = new GBProject((GBActivity)getActivity(),
                                                            projectNameInput.getText(),
                                                            Long.valueOf(projectIDString));
            }
            Toast.makeText(getActivity(),
                    R.string.save_copied_project,
                    Toast.LENGTH_SHORT).show();

            if (updateProjectFromUIFields(mProjectBeingMaintained)){
                boolean addToDBToo = true;
                boolean cascadeFlag = true;
                projectManager.addProject(mProjectBeingMaintained, addToDBToo, cascadeFlag);
            }

            GBUtilities.getInstance().setOpenProject((GBActivity)getActivity(), mProjectBeingMaintained);


            //************************ UNKNOWN *******************************************/
        } else {
            GBUtilities.getInstance().showStatus(getActivity(), R.string.unrecognized_path_encountered);
        }

        //update the Last Maintained field
        mProjectBeingMaintained.setProjectLastModified(new Date().getTime());
        //and update it on the screen as well
        initializeUI(v);

        return mProjectBeingMaintained;
    }

    //returns false if there is something wrong with information on the screen
    private boolean updateProjectFromUIFields(GBProject project){
        View v = getView();
        if (v == null) return false;

        boolean returnCode = true;
        EditText projectIDInput   = (EditText) v.findViewById(R.id.projectIDInput) ;
        EditText projectNameInput = (EditText) v.findViewById(R.id.projectNameInput) ;
        EditText projectDescInput = (EditText) v.findViewById(R.id.projectDescInput) ;
        EditText projectLocPrecision = (EditText)v.findViewById(R.id.projectLocPrecisionInput);
        EditText projectStdDevPrecision = (EditText)v.findViewById(R.id.projectStdDevPrecisionInput);


        //show the data that came out of the input arguments bundle
        project.setProjectID(Integer.valueOf(projectIDInput  .getText().toString().trim()));
        project.setProjectName              (projectNameInput.getText().toString().trim());
        project.setProjectDescription       (projectDescInput .getText().toString().trim());
        //The date fields are not modifiable from this screen
        //Set the coordinate type
        if (mSelectedCoordinateType == null){
            //must declare the type of coordinates in order to create the project
            project.setProjectCoordinateType("");
            returnCode = false;
        }else{
            project.setProjectCoordinateType(mSelectedCoordinateType);
        }

        //Project Settings
        project.setDistanceUnits(mSelectedDistUnitPosition);
        //// TODO: 6/30/2017 This may be LatLng. Determine if changes need to be made here
        project.setEnVNe        (mSelectedENvNEPosition);
        project.setRMSvStD      (mSelectedRMSvStdDevPosition);
        project.setDDvDMS       (mSelectedDDvDMSPosition);
        project.setDIRvPlusMinus(mSelectedDirVPlusMinusPosition);

        project.setLocPrecision(Integer.valueOf(projectLocPrecision.getText().toString().trim()));
        project.setStdDevPrecision(Integer.valueOf(projectStdDevPrecision.getText().toString().trim()));


        //Precision digits



        return returnCode;

    }


    //***********************************/
    //****   List Projects Button  ******/
    //***********************************/
    private void viewProjects(){
        //what this button does depends upon the path  {create, open, copy}
        //*********************** CREATE **************************************/
        if (mProjectPath.equals(GBPath.sCreateTag)){
            Toast.makeText(getActivity(),
                    R.string.cant_view_projects,
                    Toast.LENGTH_SHORT).show();

            //switchToProjectList();

            //*********************** OPEN or COPY or EDIT **************************************/
        } else if ((mProjectPath.equals(GBPath.sOpenTag)) ||
                (mProjectPath.equals(GBPath.sCopyTag)) ||
                (mProjectPath.equals(GBPath.sEditTag)) ) {
            if (mProjectChanged){
                //ask the user if should continue
                areYouSureViewProjects();

            } else {
                Toast.makeText(getActivity(),
                        R.string.project_unchanged,
                        Toast.LENGTH_SHORT).show();

                switchToProjectList();

            }

            //*********************** UNKNOWN **************************************/
        } else {
            Toast.makeText(getActivity(),
                    R.string.unrecognized_path_encountered,
                    Toast.LENGTH_SHORT).show();

            //Notice that we don't leave the screen on this condition
        }

    }

    //Build and display the alert dialog
    private void areYouSureViewProjects(){
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.abandon_title)
                .setIcon(R.drawable.ground_station_icon)
                .setMessage(R.string.are_you_sure)
                .setPositiveButton(R.string.continue_abandon_changes,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                //Leave even though project has chaged
                                Toast.makeText(getActivity(),
                                        R.string.continue_abandon_changes,
                                        Toast.LENGTH_SHORT).show();

                               switchToProjectList();
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

    private void switchToProjectList(){
        ((GBActivity) getActivity()).
                switchToProjectListScreen(new GBPath(mProjectPath));
    }



    //***********************************/
    //****  Project Settings Button   ***/
    //***********************************/
    //Build and display the alert dialog
    private void areYouSureViewSettings(){
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.abandon_title)
                .setIcon(R.drawable.ground_station_icon)
                .setMessage(R.string.are_you_sure)
                .setPositiveButton(R.string.continue_abandon_changes,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                //Leave even though project has chaged
                                Toast.makeText(getActivity(),
                                        R.string.continue_abandon_changes,
                                        Toast.LENGTH_SHORT).show();


                                switchToProjectSettings();

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

    private void switchToProjectSettings(){
        if (mProjectBeingMaintained == null)return;

        ((GBActivity) getActivity()).switchToProjectSettingsScreen();
    }



    //***********************************/
    //**** List Points Button     *******/
    //***********************************/
    //Build and display the alert dialog
    private void areYouSureListPoints(){

        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.abandon_title)
                .setIcon(R.drawable.ground_station_icon)
                .setMessage(R.string.are_you_sure)
                .setPositiveButton(R.string.continue_abandon_changes,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                //Leave even though project has chaged
                                GBUtilities.getInstance().showStatus(getActivity(),
                                                                R.string.continue_abandon_changes);
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
        if (mProjectBeingMaintained == null)return;

        // TODO: 7/1/2017 make sure the project is saved before leaving
        //cant't do this if we are creating the project
        //The project must be saved and the path changed to EDIT
        if (!(mProjectPath.equals(GBPath.sCreateTag))) {
            GBActivity myActivity = (GBActivity) getActivity();
            myActivity.switchToPointsListScreen(new GBPath(mProjectPath));
        }

    }


    //***********************************/
    //****     Exit Button        *******/
    //***********************************/
    //Build and display the alert dialog
    private void areYouSureExit(){
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.abandon_title)
                .setIcon(R.drawable.ground_station_icon)
                .setMessage(R.string.are_you_sure)
                .setPositiveButton(R.string.continue_abandon_changes,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                //Leave even though project has chaged
                                Toast.makeText(getActivity(),
                                        R.string.continue_abandon_changes,
                                        Toast.LENGTH_SHORT).show();
                                switchToExit();

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

    private void switchToExit(){
        ((GBActivity) getActivity()).popToTopProjectScreen();

    }


    //**********************************************************/
    //****     Maintain State Flags in this Fragment     *******/
    //**********************************************************/
    private void setProjectChangedFlags(){
        View v = getView();
        if (v == null)return;

        mProjectChanged = true;

        Button projectSaveChangesButton = (Button) v.findViewById(R.id.projectSaveChangesButton);
        //enable the save changes button
        projectSaveChangesButton.setEnabled(true);
        projectSaveChangesButton.setTextColor(Color.BLACK);
    }

    private void setProjectSavedFlags(){
        View v = getView();
        if (v == null)return;

        mProjectChanged = false;
        //enable the enter button as the default is NOT enabled/grayed out
        //mEnterButton.setText(R.string.enter_to_save_button_label);
        //mEnterButton.setEnabled(false);
        //mEnterButton.setTextColor(Color.GRAY);

        Button projectSaveChangesButton = (Button) v.findViewById(R.id.projectSaveChangesButton);
        projectSaveChangesButton.setEnabled(false);
        projectSaveChangesButton.setTextColor(Color.GRAY);
    }


}


