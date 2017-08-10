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
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

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
    private int      mSelectedCoordinateTypePosition;





    //**********************************************************************/
    //*********   Member Variables  ****************************************/
    //**********************************************************************/

    // TODO: 7/1/2017 configuration change might be problematic with complex variables like project
    private GBProject    mProjectBeingMaintained;

    private boolean      mProjectChanged = false;
    private boolean      mProjectFirstTime;
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


        GBPath path             = GBPath.getPathFromArguments(getArguments());
        mProjectPath            = path.getPath();

        if (mProjectPath == GBPath.sCreateTag){
            mProjectBeingMaintained = new GBProject();
        } else {
            mProjectBeingMaintained = GBUtilities.getInstance().getOpenProject((GBActivity) getActivity());
        }
        if (mProjectBeingMaintained == null){
            mProjectBeingMaintained = new GBProject();
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //Inflate the layout for this fragment
        View v = inflater.inflate( R.layout.fragment_project_edit_gb, container,  false);


        //Wire up the UI widgets so they can handle events later
        wireWidgets(v);

        wireSpinners(v);

        // TODO: 6/30/2017 make this the list of points, not the list of pictures
        //initializeRecyclerView(v);


        //If we had any arguments passed, update the screen with them
        initializeUI(v);

        setProjectSavedFlags();
        //If this is the first time through, there wont be a savedInstanceState
        mProjectFirstTime = (savedInstanceState == null);

        return v;
    }

    @Override
    public void onResume(){
        super.onResume();
        //set the title bar subtitle
        setSubtitle();

        if (mProjectFirstTime){
            setProjectSavedFlags();
            mProjectFirstTime = false;
        }
    }


    //**********************************************************/
    //****     Initialize                                *******/
    //**********************************************************/
    private void wireWidgets(View v){
        if (mProjectBeingMaintained == null)return;

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
                setProjectChangedFlags();
            }
        };


        //For now ignore the text view widgets, as this is just a mockup
        //      for the real screen we'll have to actually fill the fields

        //Project Name
        EditText projectNameInput = (EditText) v.findViewById(R.id.projectNameInput);
        projectNameInput.addTextChangedListener(textWatcher);

        //Project ID
        //TextView projectIDLabel = (TextView) v.findViewById(R.id.projectIDLabel);
        //if (mProjectPath == GBPath.sCreateTag){
            //projectIDLabel.setText(getString(R.string.project_id_will_be_label));
        //}
        EditText projectIDInput = (EditText) v.findViewById(R.id.projectIDInput);
        //Shouldn't be able to change ID
        projectIDInput.setFocusable(false);
        projectIDInput.addTextChangedListener(textWatcher);

        //Project Creation Date
        EditText projectDateInput = (EditText) v.findViewById(R.id.projectCreationDateInput);
        projectDateInput.setFocusable(false);

        //Project Last Modified Date
        EditText projectMaintInput = (EditText) v.findViewById(R.id.projectModifiedDateInput);
        projectMaintInput.setFocusable(false);

        //Project Number of points
        EditText projectNumPtsOutput = (EditText) v.findViewById(R.id.projectNumPointsOutput);
        projectNumPtsOutput.setFocusable(false);

        //Project Number of points in mean
        EditText projectNumMeanOutput = (EditText) v.findViewById(R.id.projectNumMeanOutput);
        projectNumMeanOutput.setFocusable(true);
        projectNumMeanOutput.setEnabled(true);
        projectNumMeanOutput.addTextChangedListener(textWatcher);

        //Project Height
        EditText projectHeightOutput = (EditText) v.findViewById(R.id.projectHeightOutput);
        projectHeightOutput.setFocusable(true);
        projectHeightOutput.setEnabled(true);
        projectHeightOutput.addTextChangedListener(textWatcher);

        //Project Next Point Number
        EditText projectNxtPtNbOutput = (EditText) v.findViewById(R.id.projectNxtPointNumOutput);
        projectNxtPtNbOutput.setFocusable(true);
        projectNxtPtNbOutput.setEnabled(true);
        projectNxtPtNbOutput.addTextChangedListener(textWatcher);

        //Project SPC Zone
        EditText projectZoneInput = (EditText) v.findViewById(R.id.projectSpcZoneInput);
        projectZoneInput.setFocusable(true);
        projectZoneInput.setEnabled(true);
        projectZoneInput.addTextChangedListener(textWatcher);


        //Project Description
        EditText projectDescInput = (EditText) v.findViewById(R.id.projectDescInput);
        projectDescInput.addTextChangedListener(textWatcher);


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


         //Create the array of spinner choices from the Types of Coordinates defined
        String [] distanceUnits = new String[]{ GBProject.sMetersString,
                                                GBProject.sFeetString,
                                                GBProject.sIntFeetString};
        String [] coordinateTypes = new String[]{   GBCoordinate.sCoordinateTypeWGS84,
                                                    GBCoordinate.sCoordinateTypeSPCS,
                                                    GBCoordinate.sCoordinateTypeUTM};
        String [] dataSourceTypes = new String[]{getString(R.string.select_data_source),
                                                 getString(R.string.manual_wgs_data_source),
                                                 getString(R.string.manual_spcs_data_source),
                                                 getString(R.string.manual_utm_data_source),
                                                 getString(R.string.phone_gps),
                                                 getString(R.string.external_gps),
                                                 getString(R.string.cell_tower_triangulation)};


        // TODO: 6/29/2017 conditional envne versus latlngvlnglat only one of these on the screen
        //Then initialize the spinner itself

        Spinner distUnitsSpinner           = (Spinner) v.findViewById(R.id.distance_units_spinner);
        Spinner coordSpinner               = (Spinner) v.findViewById(R.id.coordinate_type_spinner);
        Spinner dataSourceSpinner          = (Spinner) v.findViewById(R.id.data_source_spinner);

        // Create the ArrayAdapters using the Activities context AND
        // the string array and a default spinner layout
        ArrayAdapter<String> duAdapter = new ArrayAdapter<>(getActivity(),
                                                            android.R.layout.simple_spinner_item,
                                                            distanceUnits);

        ArrayAdapter<String> ctAdapter = new ArrayAdapter<>(getActivity(),
                                                            android.R.layout.simple_spinner_item,
                                                            coordinateTypes);

        ArrayAdapter<String> dsAdapter = new ArrayAdapter<>(getActivity(),
                                                            android.R.layout.simple_spinner_item,
                                                            dataSourceTypes);


        // Specify the layout to use when the list of choices appears
        duAdapter .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ctAdapter .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dsAdapter .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the spinner
        distUnitsSpinner    .setAdapter(duAdapter);
        coordSpinner        .setAdapter(ctAdapter);
        dataSourceSpinner   .setAdapter(dsAdapter);

        //attach the listener to the spinner
        distUnitsSpinner.setOnItemSelectedListener (new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (canMakeChange()) {
                     setProjectChangedFlags();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        coordSpinner.setOnItemSelectedListener     (new AdapterView.OnItemSelectedListener() {
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
        dataSourceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                int msg;

                switch(position){
                    case GBProject.sDataSourceNoneSelected:

                        msg = R.string.select_data_source;
                        break;
                    case GBProject.sDataSourceWGSManual:
                        msg = R.string.manual_wgs_data_source;

                        break;
                    case GBProject.sDataSourceSPCSManual:
                        msg = R.string.manual_spcs_data_source;
                        break;
                    case GBProject.sDataSourceUTMManual:
                        msg = R.string.manual_utm_data_source;
                        break;
                    case GBProject.sDataSourcePhoneGps:
                        // TODO: 6/22/2017 need to check that GPS is supported on this device
                         msg = R.string.phone_gps;

                        break;
                    case GBProject.sDataSourceExternalGps:
                        msg = R.string.external_gps;
                        msg = R.string.external_gps_not_available;
                        break;
                    case GBProject.sDataSourceCellTowerTriangulation:
                        msg = R.string.cell_tower_triangulation;
                        msg = R.string.cell_tower_triangu_not_available;
                        break;
                    default:
                         msg = R.string.select_data_source;
                }


                String selectMsg = getString(R.string.selected_data_source, getString(msg) );

                GBUtilities.getInstance().showStatus(getActivity(), selectMsg);
                setProjectChangedFlags();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //for now, do nothing
            }
        });

    }


    private void wireDataSourceSpinner(View v){

        //Create the array of spinner choices from the Types of Coordinates defined

        //Then initialize the spinner itself

        // Create an ArrayAdapter using the Activities context AND
        // the string array and a default spinner layout

        // Specify the layout to use when the list of choices appears
        // Apply the adapter to the spinner

        //attach the listener to the spinner

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
        EditText projectNameInput     = (EditText) v.findViewById(R.id.projectNameInput);
        EditText projectIDInput       = (EditText) v.findViewById(R.id.projectIDInput);
        EditText projectDateInput     = (EditText) v.findViewById(R.id.projectCreationDateInput);
        EditText projectMaintInput    = (EditText) v.findViewById(R.id.projectModifiedDateInput);
        EditText projectNumPtsOutput  = (EditText) v.findViewById(R.id.projectNumPointsOutput);
        EditText projectNumMeanOutput = (EditText) v.findViewById(R.id.projectNumMeanOutput);
        EditText projectDescInput     = (EditText) v.findViewById(R.id.projectDescInput);
        EditText projectNxtPointNumOutput = (EditText) v.findViewById(R.id.projectNxtPointNumOutput);
        EditText projectHeightInput   = (EditText) v.findViewById(R.id.projectHeightOutput);
        EditText projectZoneInput     = (EditText) v.findViewById(R.id.projectSpcZoneInput);
        TextView projectStateInput    = (TextView) v.findViewById(R.id.projectSpcStateOutput);


        Spinner distUnitsSpinner           = (Spinner) v.findViewById(R.id.distance_units_spinner);
        Spinner dataSourceSpinner          = (Spinner) v.findViewById(R.id.data_source_spinner);



        //a new id is not assigned until the first save
        if (mProjectPath.equals(GBPath.sCreateTag)){
            projectIDInput.setText(String.valueOf(GBUtilities.ID_DOES_NOT_EXIST));
            // TODO: 7/4/2017 determine good color to use
            projectIDInput.setBackgroundColor(ContextCompat.
                                                getColor(getActivity(), R.color.colorLightPink));
        } else {
            projectIDInput.setText(String.valueOf(mProjectBeingMaintained.getProjectID()));
            projectIDInput.setBackgroundColor(ContextCompat.
                                                getColor(getActivity(), R.color.colorGray));
        }
        projectNameInput .setText(mProjectBeingMaintained.getProjectName());
        projectDateInput .setText(mProjectBeingMaintained.getDateString(
                                                mProjectBeingMaintained.getProjectDateCreated()));
        projectMaintInput.setText(mProjectBeingMaintained.getDateString(
                                                mProjectBeingMaintained.getProjectLastModified()));

        int numberOfPoints = mProjectBeingMaintained.getSize();
        projectNumPtsOutput.setText(String.valueOf(numberOfPoints));

        int numMean = mProjectBeingMaintained.getNumMean();
        projectNumMeanOutput.setText(String.valueOf(numMean));

        projectDescInput .setText(mProjectBeingMaintained.getProjectDescription());

        int nxtPointNumber = mProjectBeingMaintained.getNextPointNumber((GBActivity)getActivity());
        projectNxtPointNumOutput.setText(String.valueOf(nxtPointNumber));

        double height = mProjectBeingMaintained.getHeight();
        projectHeightInput.setText(String.valueOf(height));

        int zone = mProjectBeingMaintained.getZone();
        projectZoneInput.setText(String.valueOf(zone));

        GBCoordinateConstants constants = new GBCoordinateConstants(zone);
        int spcsZone = constants.getZone();
        if (spcsZone != (int)GBUtilities.ID_DOES_NOT_EXIST) {
            String state = constants.getState();
            projectStateInput.setText(state);
        }




        //mProjectCoordTypeOutput.setText(mProjectBeingMaintained.getProjectCoordinateType());

        ArrayAdapter<String> orderAdapter;



        Spinner coordSpinner = (Spinner) v.findViewById(R.id.coordinate_type_spinner);
        CharSequence spinnerSelection = mProjectBeingMaintained.getProjectCoordinateType();
        //The following code assumes that the values of the sCoordinateDBTypeXXX are in the same
        //  order as the spinner selections for coordinate type as defined in wireSpinners
        String selectedCoordinateType;
        int    selectedCoordinateTypePosition;
        if (spinnerSelection.equals(GBCoordinate.sCoordinateTypeNAD83)) {
            selectedCoordinateType         = GBCoordinate.sCoordinateTypeClassNAD83;
            selectedCoordinateTypePosition = GBCoordinate.sCoordinateDBTypeNAD83;
            coordSpinner.setSelection(GBCoordinate.sCoordinateDBTypeNAD83);


        } else if (spinnerSelection.equals(GBCoordinate.sCoordinateTypeUTM)) {
            selectedCoordinateType         = GBCoordinate.sCoordinateTypeUTM;
            selectedCoordinateTypePosition = GBCoordinate.sCoordinateDBTypeUTM;
            coordSpinner.setSelection(GBCoordinate.sCoordinateDBTypeUTM);


        } else if (spinnerSelection.equals(GBCoordinate.sCoordinateTypeSPCS)){
            selectedCoordinateType         = GBCoordinate.sCoordinateTypeSPCS;
            selectedCoordinateTypePosition = GBCoordinate.sCoordinateDBTypeSPCS;
            coordSpinner.setSelection(GBCoordinate.sCoordinateDBTypeSPCS);


        } else  { //Use WGS84 as the default
            selectedCoordinateType         = GBCoordinate.sCoordinateTypeWGS84;
            selectedCoordinateTypePosition = GBCoordinate.sCoordinateDBTypeWGS84;
            coordSpinner.setSelection(GBCoordinate.sCoordinateDBTypeWGS84);


            //update the project with this new default
            mProjectBeingMaintained.setProjectCoordinateType(selectedCoordinateType);
            setProjectChangedFlags();
        }

        mSelectedCoordinateType         = selectedCoordinateType;
        mSelectedCoordinateTypePosition = selectedCoordinateTypePosition;

        //mPointCoordinateTypePrompt.setText();

        distUnitsSpinner    .setSelection(mProjectBeingMaintained.getDistanceUnits());
        dataSourceSpinner   .setSelection(mProjectBeingMaintained.getDataSource());
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
        //GBUtilities.getInstance().showStatus(getActivity(), "Picture Selected", Toast.LENGTH_SHORT).show();
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
            GBUtilities.getInstance().showStatus(getActivity(), msg);
        }

    }


    private void onMap(){

        ((GBActivity)getActivity()).switchToMapPointsScreen();


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
        ((GBActivity)getActivity()).switchToExportScreen();
        //GBUtilities.getInstance().showStatus(getActivity(), "Export of project points not yet supported");


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


            //returns false if there is something wrong with information on the screen and
            //the project can not be updated
            if (updateProjectFromUIFields(mProjectBeingMaintained)) {
                //project ID assigned when project first saved to the DB
                //This saves the project. does a cascade add/update
                boolean addToDBToo = true;
                boolean cascadeFlag = true;
                projectManager.addProject(mProjectBeingMaintained, addToDBToo, cascadeFlag);


                //save it again to pick up the changes just made
                projectManager.addProject(mProjectBeingMaintained, addToDBToo, cascadeFlag);
                //change the path to edit
                mProjectPath = GBPath.sEditTag;

                //enable the list points button if there are any points
                Button projectListPointsButton =
                                             (Button) v.findViewById(R.id.projectListPointsButton) ;
                if (mProjectBeingMaintained.getSize() > 0) {
                    projectListPointsButton.setEnabled(true);
                    projectListPointsButton.setTextColor(Color.BLACK);
                }

                GBUtilities.getInstance().setOpenProject((GBActivity)getActivity(), mProjectBeingMaintained);
                GBUtilities.getInstance().showStatus(getActivity(), R.string.save_new_project);

            }

        //************************************* Edit *****************************/
        } else if (mProjectPath.equals(GBPath.sEditTag)){
            if (mProjectBeingMaintained == null){
               GBUtilities.getInstance().showStatus(getActivity(),
                              R.string.project_missing_exception);
                return null;
            }
            GBUtilities.getInstance().showStatus(getActivity(),
                    R.string.save_existing_project);


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
            GBUtilities.getInstance().showStatus(getActivity(),
                                                    R.string.save_copied_project);

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
        EditText projectIDInput         = (EditText) v.findViewById(R.id.projectIDInput) ;
        EditText projectNameInput       = (EditText) v.findViewById(R.id.projectNameInput) ;
        EditText projectDescInput       = (EditText) v.findViewById(R.id.projectDescInput) ;
        EditText projectNumMean         = (EditText) v.findViewById(R.id.projectNumMeanOutput);
        EditText projectHeight          = (EditText) v.findViewById(R.id.projectHeightOutput);
        EditText projectNxtPtNum        = (EditText) v.findViewById(R.id.projectNxtPointNumOutput);
        EditText projectZone            = (EditText) v.findViewById(R.id.projectSpcZoneInput);

        //show the data that came out of the input arguments bundle
        project.setProjectID(Integer.valueOf(projectIDInput  .getText().toString().trim()));
        project.setProjectName              (projectNameInput.getText().toString().trim());
        project.setProjectDescription       (projectDescInput .getText().toString().trim());
        //The date fields are not modifiable from this screen

        project.setHeight(Double.valueOf(projectHeight.getText().toString()));
        String nxtPtNumString = projectNxtPtNum.getText().toString();
        int nxtPtNum = Integer.valueOf(nxtPtNumString);
        project.setNextPointNumber((GBActivity)getActivity(),nxtPtNum);

        String zoneString = projectZone.getText().toString();
        int zone = Integer.valueOf(zoneString);
        project.setZone(zone);


        //Project Settings
        String numMeanString = projectNumMean.getText().toString();
        int numMean;
        if (GBUtilities.isEmpty(numMeanString)){
            numMean = 0;
        } else {
            numMean = Integer.valueOf(numMeanString);
        }

        project.setNumMean(numMean);

        Spinner distUnitsSpinner     = (Spinner) v.findViewById(R.id.distance_units_spinner);
        Spinner coordSpinner         = (Spinner) v.findViewById(R.id.coordinate_type_spinner);
        Spinner dataSourceSpinner    = (Spinner) v.findViewById(R.id.data_source_spinner);

        project.setDistanceUnits(distUnitsSpinner    .getSelectedItemPosition());
        project.setDataSource   (dataSourceSpinner   .getSelectedItemPosition());

        String coordType = (String) coordSpinner.getSelectedItem();
        //Set the coordinate type
        if (GBUtilities.isEmpty(coordType)){
            //must declare the type of coordinates in order to create the project
            project.setProjectCoordinateType("");
            returnCode = false;
        }else{
            project.setProjectCoordinateType(coordType);
        }
        return returnCode;
    }


    //***********************************/
    //****   List Projects Button  ******/
    //***********************************/
    private void viewProjects(){
        //what this button does depends upon the path  {create, open, copy}
        //*********************** CREATE **************************************/
        if (mProjectPath.equals(GBPath.sCreateTag)){
            GBUtilities.getInstance().showStatus(getActivity(),
                                                            R.string.cant_view_projects);

            //switchToProjectList();

            //*********************** OPEN or COPY or EDIT **************************************/
        } else if ((mProjectPath.equals(GBPath.sOpenTag)) ||
                (mProjectPath.equals(GBPath.sCopyTag)) ||
                (mProjectPath.equals(GBPath.sEditTag)) ) {
            if (mProjectChanged){
                //ask the user if should continue
                areYouSureViewProjects();

            } else {
                GBUtilities.getInstance().showStatus(getActivity(),
                                                        R.string.project_unchanged);

                switchToProjectList();

            }

            //*********************** UNKNOWN **************************************/
        } else {
            GBUtilities.getInstance().showStatus(getActivity(), R.string.unrecognized_path_encountered);

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
                                GBUtilities.getInstance().showStatus(getActivity(), R.string.continue_abandon_changes);

                               switchToProjectList();
                            }
                        })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                        GBUtilities.getInstance().showStatus(getActivity(), R.string.cancel_pressed);
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
                                GBUtilities.getInstance().showStatus(getActivity(), R.string.continue_abandon_changes);
                                switchToListPoints();
                            }
                        })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                        GBUtilities.getInstance().showStatus(getActivity(),R.string.cancel_pressed);
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
                                GBUtilities.getInstance().showStatus(getActivity(), R.string.continue_abandon_changes);
                                switchToExit();

                            }
                        })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                        GBUtilities.getInstance().showStatus(getActivity(),R.string.cancel_pressed);
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


