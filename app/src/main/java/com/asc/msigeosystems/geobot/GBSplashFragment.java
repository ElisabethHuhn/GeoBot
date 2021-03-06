package com.asc.msigeosystems.geobot;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

/**
 * The Collect Fragment is the UI
 * when the workflow from WGS84 GPS to NAD83 to UTM/State Plane Coordinates
 * Created by Elisabeth Huhn on 6/15/2016.
 */
public class GBSplashFragment extends Fragment  {

    //********************************/
    //*****  Constructor    **********/
    //********************************/
     public GBSplashFragment() {
        //for now, we don't need to initialize anything when the fragment
        //  is first created
    }

    //**************************************/
    //*****  Lifecycle Methods    **********/
    //**************************************/
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_splash, container, false);

        wireWidgets(v);

        initializeUI(v);

        setSubtitle();
        return v;
    }


    //Ask for location events to start
    @Override
    public void onResume() {
        super.onResume();

        setSubtitle();
    }


    private void setSubtitle() {
        ((GBActivity) getActivity()).setSubtitle(R.string.action_splash);
    }


   //+****************************************************


    //+**********************************************


    private void wireWidgets(View v) {


        //open Button
        Button openButton = (Button) v.findViewById(R.id.splashOpenButton);
        openButton.setText(R.string.open_button_label);
        //the order of images here is left, top, right, bottom
        openButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_112_openfolder, 0, 0);
        openButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((GBActivity)getActivity()).switchToProjectOpenScreen();


            }
        });



        //Create Button
        Button createButton = (Button) v.findViewById(R.id.splashCreateButton);
        createButton.setText(R.string.create_button_label);
        //the order of images here is left, top, right, bottom
        createButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_111_createfolder, 0, 0);
        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Switch the fragment to the collect with maps fragment.
                // But the switching happens on the container Activity
                ((GBActivity)getActivity()).switchToProjectCreateScreen();


            }
        });



        //Edit Button
        Button editButton = (Button) v.findViewById(R.id.splashEditButton);
        editButton.setEnabled(true);
        editButton.setText(R.string.edit_button_label);
        editButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_114_editfolder, 0, 0);
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                GBUtilities.getInstance().showStatus(getActivity(), R.string.edit_button_label);

                ((GBActivity)getActivity()).switchToProjectEditScreen();


            }
        });

        //List Points Button
        Button listPointsButton = (Button) v.findViewById(R.id.splashListPointsButton);
        listPointsButton.setText(R.string.list_points_button_label);
        listPointsButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_117_pointsfile, 0, 0);
        listPointsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){

                GBActivity myActivity = (GBActivity) getActivity();
                GBProject openProject = GBUtilities.getInstance().getOpenProject(myActivity);
                if (openProject != null){
                    myActivity.switchToPointsListScreen(new GBPath(GBPath.sEditTag));
                } else {
                    GBUtilities.getInstance().showStatus(getActivity(),  R.string.project_not_open_to_list_points);
                }
            }
        });

        //Measure Points Button
        Button measurePointsButton = (Button) v.findViewById(R.id.splashMeasureButton);
        measurePointsButton.setText(R.string.project_measure_button_label);
        measurePointsButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_002_collect, 0, 0);
        measurePointsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){


                //regardless of whether they actually exit, hide the keyboard
                GBUtilities.getInstance().hideKeyboard(getActivity());
                GBProject project = GBUtilities.getInstance().getOpenProject((GBActivity)getActivity());
                if (project == null){
                    GBUtilities.getInstance().showStatus(getActivity(), R.string.project_not_open);
                    return;
                }
                ((GBActivity)getActivity()).switchToPointCreateScreen(project);
            }
        });

        //Map Points Button
        Button mapPointsButton = (Button) v.findViewById(R.id.splashMapButton);
        mapPointsButton.setText(R.string.project_map_button_label);
        mapPointsButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_002_collect, 0, 0);
        mapPointsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){


                //regardless of whether they actually exit, hide the keyboard
                GBUtilities.getInstance().hideKeyboard(getActivity());
                GBProject project = GBUtilities.getInstance().getOpenProject((GBActivity)getActivity());
                if (project == null){
                    GBUtilities.getInstance().showStatus(getActivity(), R.string.project_not_open);
                    return;
                }
                ((GBActivity)getActivity()).switchToMapPointsScreen();

            }
        });

/*
        //Old Conversion Button
        Button convertButton = (Button) v.findViewById(R.id.splashOldConvertButton);
        convertButton.setText(R.string.convert_button_label);
        //convertButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_002_collect, 0, 0);
        convertButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){

                //regardless of whether they actually exit, hide the keyboard
                GBUtilities.getInstance().hideKeyboard(getActivity());
                GBProject project = GBUtilities.getInstance().getOpenProject((GBActivity)getActivity());
                if (project == null){
                    GBUtilities.getInstance().showStatus(getActivity(), R.string.project_not_open);
                    return;
                }
                ((GBActivity)getActivity()).switchToConvertScreen();
            }
        });
*/
        //Export
        Button exportButton = (Button) v.findViewById(R.id.splashExportButton);
        exportButton.setText(R.string.exchange_button_label);
        exportButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_119_exchangefolder, 0, 0);
        exportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){

                //regardless of whether they actually exit, hide the keyboard
                GBUtilities.getInstance().hideKeyboard(getActivity());
                GBProject project = GBUtilities.getInstance().getOpenProject((GBActivity)getActivity());
                if (project == null){
                    GBUtilities.getInstance().showStatus(getActivity(), R.string.project_not_open);
                    return;
                }
                onExport();
            }
        });

        //settings Button
        Button settingsButton = (Button) v.findViewById(R.id.splashSettingsButton);
        settingsButton.setText(R.string.settings_button_label);
        settingsButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_008_settings, 0, 0);
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){

                ((GBActivity) getActivity()).switchToGeneralSettingsScreen();
            }
        });

    }

    private void initializeUI(View v){
        //Tell the user which project is open
        TextView screenLabel = (TextView) v.findViewById(R.id.matrix_screen_label);
        screenLabel.setText(GBUtilities.getInstance().getOpenProjectIDMessage((GBActivity)getActivity()));
        int color = ContextCompat.getColor(getActivity(), R.color.colorWhite);
        screenLabel.setBackgroundColor(color);

        //GeoBot Version is stored in GBActivity as a static string
        //GeoBot DB Name and Version is stored in GBDatabaseSqliteHelper
        //GeoBot Conversion Constants Version is stored in GBCoordinateConstants

        String gbVersion = getString(R.string.splash_product_version,GBActivity.sGeoBotVersion);

        String gbDbVersion = getString(R.string.splash_db_version,
                                       GBDatabaseSqliteHelper.DATABASE_NAME,
                                       GBDatabaseSqliteHelper.DATABASE_VERSION);

        String gbCoordConstVersion = getString(R.string.splash_constants_version,
                                               GBCoordinateConstants.CONSTANTS_VERSION);

        TextView gbVersionOutput           = (TextView) v.findViewById(R.id.geobot_version_label);
        TextView gbDbVersionOutput         = (TextView) v.findViewById(R.id.geobot_db_version_label);
        TextView gbCoordConstVersionOutput =
                                    (TextView) v.findViewById(R.id.geobot_constants_version_label);

        gbVersionOutput          .setText(gbVersion);
        gbDbVersionOutput        .setText(gbDbVersion);
        gbCoordConstVersionOutput.setText(gbCoordConstVersion);


    }


    //******************************************************************//
    //            Button Handlers                                       //
    //******************************************************************//
    private void onOpen(){

        ((GBActivity)getActivity()).switchToProjectListScreen(new GBPath(GBPath.sOpenTag));

    }

    private void onCreate(){

        ((GBActivity)getActivity()).switchToProjectListScreen(new GBPath(GBPath.sCreateTag));

    }

    private void onEdit(){

        ((GBActivity)getActivity()).switchToProjectListScreen(new GBPath(GBPath.sEditTag));

    }

    private void onProceed(){

        long projectID = GBUtilities.getInstance().getOpenProjectID((GBActivity)getActivity());
        if (projectID == GBUtilities.ID_DOES_NOT_EXIST){
            onOpen();
            return;
        }

        ((GBActivity)getActivity()).switchToProjectEditScreen();

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


}


