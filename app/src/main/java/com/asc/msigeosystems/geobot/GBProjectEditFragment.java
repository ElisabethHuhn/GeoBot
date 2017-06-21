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
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * The Update Project Fragment
 * is passed a project on startup. The project attribute fields are
 * pre-populated prior to updating the project
 *
 * Created by Elisabeth Huhn on 4/13/2016.
 */
public class GBProjectEditFragment extends    Fragment
                                            implements AdapterView.OnItemSelectedListener{

    private static final String TAG = "EDIT_PROJECT_FRAGMENT";

    //**
     /* Create variables for all the widgets
     /*
     */


    //**********************************************************************/
    //*********   UI Widget Variables  *************************************/
    //**********************************************************************/

    //Input / Output Fields on screen



    //**********************************************************/
    //*****  Coordinate types for Spinner Widgets     **********/
    //**********************************************************/
     String[] mCoordinateTypes;
    private Spinner  mSpinner;

    private String   mSelectedCoordinateType;
    private int      mSelectedCoordinateTypePosition;


    //**********************************************************/
    //*****         Recycler View Widgets             **********/
    //**********************************************************/
    List<GBPicture>            mPictureList = new ArrayList<>();




    //**********************************************************************/
    //*********   Member Variables  ****************************************/
    //**********************************************************************/
    private GBProject mProjectBeingMaintained;

    private boolean      mProjectChanged = false;
    private CharSequence mProjectPath;




    //Constructor
    public GBProjectEditFragment() {
        //for now, we don't need to initialize anything when the fragment
        //  is first created with this constructor
    }



    //newInstance() stores the passed parameters in the fragments argument bundle
    public static GBProjectEditFragment newInstance(GBProject project,
                                                    GBPath projectPath) {

        //Put the project into an arguments bundle
        Bundle args = GBProject.putProjectInArguments(new Bundle(), project);

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

        mProjectBeingMaintained = GBProject.getProjectFromArguments(getArguments());

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

        initializeRecyclerView(v);


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
        TextView projectIDLabel = (TextView) v.findViewById(R.id.projectIDLabel);
        if (mProjectPath == GBPath.sCreateTag){
            projectIDLabel.setText(getString(R.string.project_id_will_be_label));
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




        //View Settings Button
        Button projectViewSettingsButton = (Button) v.findViewById(R.id.projectViewSettingsButton);
        projectViewSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){

                if (mProjectChanged){
                    areYouSureViewSettings();
                } else {
                    switchToProjectSettings();
                }

            }
        });


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

        //Exit Button
        Button projectExitButton = (Button) v.findViewById(R.id.projectExitButton);
        //button is always enabled
        projectExitButton.setEnabled(true);
        projectExitButton.setTextColor(Color.BLACK);
        projectExitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){

               //regardless of whether they actually exit, hide the keyboard
                GBUtilities.getInstance().hideKeyboard(getActivity());

                //If the project changed, ask before exiting
                if (mProjectChanged) {
                    areYouSureExit();
                } else {
                    switchToExit();
                }
            }
        });


    }


    private void wireCoordinateSpinner(View v){
        if (mProjectBeingMaintained == null)return;

        //set the default
        mSelectedCoordinateType = GBCoordinate.sCoordinateTypeClassUTM;

        //Create the array of spinner choices from the Types of Coordinates defined
        mCoordinateTypes = new String[]{
                getString(R.string.enter_coordinate_type),
                GBCoordinate.sCoordinateTypeWGS84,
                GBCoordinate.sCoordinateTypeNAD83,
                GBCoordinate.sCoordinateTypeUTM,
                GBCoordinate.sCoordinateTypeSPCS };

        //Then initialize the spinner itself
        mSpinner = (Spinner) v.findViewById(R.id.coordinate_type_spinner);

        // Create an ArrayAdapter using the Activities context AND
        // the string array and a default spinner layout
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(),
                                                          android.R.layout.simple_spinner_item,
                                                          mCoordinateTypes);

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        mSpinner.setAdapter(adapter);

        //attach the listener to the spinner
        mSpinner.setOnItemSelectedListener(this);

        //The size of a project is it's number of points
        if (mProjectBeingMaintained.getSize() > 0){
            //can't change the coordinate type if any points are on the project
            mSpinner.setEnabled(false);
            mSpinner.setClickable(false);
            mSpinner.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.colorGray));
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
        mPictureList = mProjectBeingMaintained.getPictures();


        //5) Use the data to Create and set out points Adapter
        GBPictureAdapter adapter = new GBPictureAdapter(mPictureList);
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
        EditText projectDescInput  = (EditText) v.findViewById(R.id.projectDescInput);

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
        projectDescInput .setText(mProjectBeingMaintained.getProjectDescription());
        //mProjectCoordTypeOutput.setText(mProjectBeingMaintained.getProjectCoordinateType());


        CharSequence spinnerSelection = mProjectBeingMaintained.getProjectCoordinateType();
        //NOTE: THIS NEXT SECTION MUST BE IN THE SAME ORDER AS THAT WHEN THE SPINNER
        //IS CREATED. THE ORDERING IS ENFORCED BY THE PROGRAMMER AT CODING TIME
        //THERE IS NOTHING AUTOMATIC ABOUT THIS HARD CODING
        //I'm not proud of it, but it works, so.......... be it
        if (spinnerSelection.equals(GBCoordinate.sCoordinateTypeNAD83)) {
            mSelectedCoordinateType = GBCoordinate.sCoordinateTypeClassNAD83;
            mSelectedCoordinateTypePosition = 2;
            mSpinner.setSelection(2);
        } else if (spinnerSelection == GBCoordinate.sCoordinateTypeUTM) {
            mSelectedCoordinateType = GBCoordinate.sCoordinateTypeUTM;
            mSelectedCoordinateTypePosition = 3;
            mSpinner.setSelection(3);
        } else if (spinnerSelection == GBCoordinate.sCoordinateTypeSPCS){
            mSelectedCoordinateType = GBCoordinate.sCoordinateTypeSPCS;
            mSelectedCoordinateTypePosition = 4;
            mSpinner.setSelection(4);
        } else  { //Use WGS84 as the default
            mSelectedCoordinateType = GBCoordinate.sCoordinateTypeWGS84;
            mSelectedCoordinateTypePosition = 1;
            mSpinner.setSelection(1);

            // TODO: 3/14/2017 Shouldn't the next two lines be outside the brackets??? 
            mProjectBeingMaintained.setProjectCoordinateType(mSelectedCoordinateType);
            setProjectChangedFlags();
        }

        //mPointCoordinateTypePrompt.setText();
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


    //********************************************/
    //*********     Spinner Callbacks   **********/
    //********************************************/

    public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
        mSelectedCoordinateTypePosition = position;
        mSelectedCoordinateType = (String) parent.getItemAtPosition(position);

        setProjectChangedFlags();

        int msg = 0;
        switch(position){
            case 1:
                msg = R.string.wgs84_prompt;

                break;
            case 2:
                msg = R.string.nad83_prompt;

                break;
            case 3:
                msg = R.string.utm_prompt;

                break;
            case 4:
                msg = R.string.spc_prompt;

                break;
            default:
                msg = R.string.enter_coordinate_type;
         }


    }

    public void onNothingSelected(AdapterView<?> parent) {
        //for now, do nothing
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
            Toast.makeText(getActivity(),
                    R.string.save_new_project,
                    Toast.LENGTH_SHORT).show();


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
                TextView projectIDLabel = (TextView) v.findViewById(R.id.projectIDLabel);
                projectIDLabel.setText(R.string.project_id_label);

                //enable the list points button if there are any points
                Button projectListPointsButton =
                                             (Button) v.findViewById(R.id.projectListPointsButton) ;
                if (mProjectBeingMaintained.getSize() > 0) {
                    projectListPointsButton.setEnabled(true);
                    projectListPointsButton.setTextColor(Color.BLACK);
                }

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

            //************************ UNKNOWN *******************************************/
        } else {
            Toast.makeText(getActivity(),
                    R.string.unrecognized_path_encountered,
                    Toast.LENGTH_SHORT).show();
            throw new RuntimeException(getString(R.string.unrecognized_path_encountered));

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
                switchToProjectListProjectsScreen(new GBPath(mProjectPath));
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
        if (mProjectBeingMaintained == null)return;

        //cant't do this if we are creating the project
        //The project must be saved and the path changed to EDIT
        if (!(mProjectPath.equals(GBPath.sCreateTag))) {
            GBActivity myActivity = (GBActivity) getActivity();
            myActivity.switchToListPointsScreen(mProjectBeingMaintained,
                                                new GBPath(mProjectPath));
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


