package com.asc.msigeosystems.geobot;


import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.Locale;

import static com.asc.msigeosystems.geobot.GBPath.sDeleteTag;
import static com.asc.msigeosystems.geobot.GBPath.sShowTag;
import static com.asc.msigeosystems.geobot.R.string.action_global_settings;
import static com.asc.msigeosystems.geobot.R.string.action_settings;
import static com.asc.msigeosystems.geobot.R.string.subtitle_general_settings;
import static com.asc.msigeosystems.geobot.R.string.subtitle_support;

public class GBActivity extends AppCompatActivity {

    /* *********************************************************************/
    /* ********   Static Constants  ****************************************/
    /* *********************************************************************/

    //GeoBot Version

    static final String sGeoBotVersion = "Alpha Release 0.1";

    //DEFINE constants / literals
    static final int MY_PERMISSIONS_REQUEST_COURSE_LOCATIONS = 1;
    static final int MY_PERMISSIONS_REQUEST_FINE_LOCATIONS = 2;

    static final int    sCollectPointsRequestCode  = 1;
    static final String sDestinationFragmentKey    = "DESTINATION_KEY";
    static final String sPopToBackStackTag     = "POP_TO_BACKSTACK";

    static final String sOpenProjectIDTag      = "OPEN_PROJECT_ID";
    static final String sOpenPointIDTag        = "OPEN_POINT_ID";

    private static final String sHomeTag               = "HOME";//HOME screen fragment

    private static final String sCurrentFragmentTag    = "CURRENT_FRAGMENT";

    private static final String sProjectTopTag         = "PROJECT_TOP";
    private static final String sProjectCreateTag      = "PROJECT_CREATE_TOP";
    private static final String sProjectOpenTag        = "PROJECT_OPEN";
    //private static final String sProjectCopyTag        = "PROJECT_COPY";
            static final String sProjectEditTag        = "PROJECT_EDIT";
    //private static final String sProjectUpdateTag      = "PROJECT_UPDATE";
    //private static final String sProjectDeleteTag      = "PROJECT_DELETE";

    static final String sExportTag             = "EXPORT_TAG";

    private static final String sPointTopTag           = "POINT_TOP";
    private static final String sPointCreateTag        = "POINT_CREATE_TOP";
    private static final String sPointOpenTag          = "POINT_OPEN";
    private static final String sPointCopyTag          = "POINT_COPY";
            static final String sPointEditTag          = "POINT_EDIT";
    //private static final String sPointUpdateTag        = "POINT_UPDATE";
    private static final String sPointDeleteTag        = "POINT_DELETE";
    private static final String sPointShowTag          = "POINT_SHOW";
    //private static final String sPointSettingsTag      = "POINT_SETTINGS";
    private static final String sMeanTokenTag          = "MEAM_TOKEN";

    private static final String sCollectTopTag         = "COLLECT_TOP";
    static final String sCollectPointsTag              = "COLLECT_POINTS";
    static final String sMapPointsTag                  = "MAP_POINTS";

    private static final String sStakeoutTopTag        = "STAKEOUT_TOP";

    private static final String sCogoTopTag            = "COGO_TOP";
    //private static final String sCogoCnversionTag      = "COGO_CONVERSION";
    private static final String sCogoWorkflowTag       = "COGO_WORKFLOW";

    private static final String sMapsTopTag            = "MAPS_TOP";
    private static final String sMeasureTag            = "MEASURE";

    private static final String sSkyplotTopTag         = "SKYPLOT_TOP";
    private static final String sSkyplotListNmeaTag    = "SKYPLOT_LIST_NMEA";
    private static final String sSkyplotListSatelliteTag = "SKYPLOT_LIST_SATELLITE";
    private static final String sSkyplotGpsNmeaTag     = "SKYPLOT_GPS_NMEA";

    private static final String sConfigTopTag          = "CONFIG_TOP";

    private static final String sSettingsTopTag        = "SETTINGS_TOP";
    private static final String sSettingsGlobalTag     = "SETTINGS_GLOBAL";
    //private static final String sSettingsProjectDefaultTag = "SETTINGS_PROJECT_DEFAULT";
    private static final String sSettingsGeneralTag    = "SETTINGS_GENERAL";
    private static final String sCompassTag            = "COMPASS";

    private static final String sSupportTopTag        = "SUPPORT_TOP";

    private static final String sConversionTag         = "CONVERSION";





    //* *********************************************************************/
    //* ********   Member Variables  ****************************************/
    //* *********************************************************************/

    //Variables that need to be saved/restored on re-configure






    //* *********************************************************************/
    //* ********   Setters and Getters  *************************************/
    //* *********************************************************************/


    //* *********************************************************************/
    //* ********   Lifecycle Methods  ***************************************/
    //* *********************************************************************/


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null)restoreState(savedInstanceState);

        setContentView(R.layout.activity_gb1);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initializeDB();

        initializeFragment();

        //initialize the floating action bar here if we add one
        initializeFAB();

        setSubtitle(R.string.subtitle_home);

        initializeGps();

    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
            Fragment fragment = fm.findFragmentById(R.id.fragment_container);

            if (fragment instanceof GBPointEditFragment) {
                ((GBPointEditFragment) fragment).onExit();

            } else if (fragment instanceof GBCoordinateMeasureFragment) {
                ((GBCoordinateMeasureFragment) fragment).onExit();

            } else if (fragment instanceof GBProjectEditFragment) {
                ((GBProjectEditFragment) fragment).onExit();

            } else {

                onBackPressed();
            }
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed(); // finish();
    }


    private void restoreState(Bundle savedInstanceState){

    }


    //* *********************************************************************/
    //* ********    Location Methods & Callbacks ****************************/
    //* *********************************************************************/

    private void initializeGps() {

        //make sure we have GPS permissions
        //check for permission to continue
        int permissionCheckCourse = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION);
        int permissionCheckFine = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);

        //If we don't currently have permission, we have to ask for it
        if (permissionCheckCourse != PackageManager.PERMISSION_GRANTED){
            //find out if we need to explain to the user why we need GPS
/*
 if (
 //shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION)
 false){
 //// TODO: 9/5/2016 need to add code if GPS is off
 //tell the user why GPS is required
 // Show an expanation to the user *asynchronously* -- don't block
 // this thread waiting for the user's response! After the user
 // sees the explanation, try again to request the permission.
 } else {
*/
            // No explanation needed, we can request the permission.

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    GBActivity.MY_PERMISSIONS_REQUEST_COURSE_LOCATIONS);

            // MY_PERMISSIONS_REQUEST_COURSE_LOCATIONS is an
            // app-defined int constant. The callback method gets the
            // result of the request.
            //}
        }

        //If we don't currently have permission, we have to ask for it
        if (permissionCheckFine != PackageManager.PERMISSION_GRANTED) {
            //find out if we need to explain to the user why we need GPS
/*
 if (false) {
 //shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)){

 // TODO: 9/5/2016 so write the code to tell user why we need GPS permissions
 //tell the user why GPS is required
 // Show an expanation to the user *asynchronously* -- don't block
 // this thread waiting for the user's response! After the user
 // sees the explanation, try again to request the permission.
 } else {
 *******/
            // No explanation needed, we can request the permission.

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    GBActivity.MY_PERMISSIONS_REQUEST_FINE_LOCATIONS);

            // MY_PERMISSIONS_REQUEST_FINE_LOCATIONS is an
            // app-defined int constant. The callback method gets the
            // result of the request.

            //}
            //So now signup for the GpsStatus.NmeaListener

        }
    }

    //Callbacks for permission requests
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_COURSE_LOCATIONS: {
                // If request is cancelled, the result arrays are empty.
                // TODO: 9/5/2016 Build in this functionality
/*
 if (grantResults.length > 0
 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

 // permission was granted, yay! Do the
 // contacts-related task you need to do.


 } else {

 // permission denied, boo! Disable the
 // functionality that depends on this permission.
 }
 *****/
            }
            case MY_PERMISSIONS_REQUEST_FINE_LOCATIONS: {
                // If request is cancelled, the result arrays are empty.
                // TODO: 9/5/2016  fill this in
/*
 if (grantResults.length > 0
 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

 // permission was granted, yay! Do the
 // contacts-related task you need to do.


 } else {

 // permission denied, boo! Disable the
 // functionality that depends on this permission.
 }
 ********/
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main_gb, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        long openProjectID = GBUtilities.getInstance().getOpenProjectID(this);

        switch (item.getItemId()){
            case R.id.action_home :
                switchToHomeScreen();
                return true;

            case  R.id.action_project_create:
                switchToProjectCreateScreen();
                return true;

            case  R.id.action_project_open:
                switchToProjectOpenScreen();
                return true;

            case  R.id.action_project_edit:
                switchToProjectEditScreen();
                return true;

            case  R.id.action_point_measure:
                if (openProjectID != GBUtilities.ID_DOES_NOT_EXIST) {
                    GBProject openProject = GBUtilities.getInstance().getOpenProject(this);
                    switchToPointCreateScreen(openProject);
                }
                return true;

            case  R.id.action_map_points:
                if (openProjectID != GBUtilities.ID_DOES_NOT_EXIST) {
                    switchToMapPointsScreen();
                }
                return true;

            case  R.id.action_point_list:
                if (openProjectID != GBUtilities.ID_DOES_NOT_EXIST) {
                    switchToPointsListScreen(new GBPath(GBPath.sEditTag));
                }
                return true;

            case  R.id.action_project_export:
                if (openProjectID == GBUtilities.ID_DOES_NOT_EXIST){
                    switchToExportScreen();
                }
                return true;

            case  R.id.action_settings:
                switchToGeneralSettingsScreen();
                return true;



        } //end switch

        return super.onOptionsItemSelected(item);
    }


    //* ********************************************************************
    //* Screen switching as a result of response from invoked Activity
    //* ********************************************************************/

    //This method is invoked when a child Activity sends back a response
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent dataIntent) {
        super.onActivityResult(requestCode, resultCode, dataIntent);
        switch(requestCode) {
            case (sCollectPointsRequestCode) : {
                if (resultCode == Activity.RESULT_OK) {
                    if (dataIntent != null) {
                        // TODO Extract the data returned from the child Activity.
                        String destinationTag = dataIntent.getStringExtra(sDestinationFragmentKey);
                        if (!destinationTag.isEmpty()){
                            if (destinationTag.equals(sPopToBackStackTag)){
                                switchToPopBackstack();
                            }

                        }
                    }
                }
                break;
            }
        }
    }



    /* ************************************************************/
    /* ******** Methods dealing with the FAB          *************/
    /* ************************************************************/
    private void initializeFAB(){
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleFAB(view);
            }
        });
    }

    private void handleFAB(View view){
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.fragment_container);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        if (fragment instanceof GBCoordinateMeasureFragment){
            GBSatelliteManager satelliteManager = GBSatelliteManager.getInstance();
            String dopValues = String.format(Locale.getDefault(),
                                        "HDOP = %.3f     VDOP = %.3f      PDOP = %.3f",
                                         satelliteManager.getHdop(),
                                         satelliteManager.getVdop(),
                                         satelliteManager.getPdop());




            //Snackbar.make(view, dopValues, Snackbar.LENGTH_INDEFINITE).setAction("Action", null).show();

            if (fab.getVisibility() == FloatingActionButton.VISIBLE){
                hideFAB();
            } else {
                showFAB();
            }

        }    else {
             hideFAB();
        }
    }

    public void showFAB(){
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setVisibility(FloatingActionButton.VISIBLE);
    }

    public void hideFAB(){
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setVisibility(FloatingActionButton.GONE);
    }

    ///* ************************************************************/
    ///* *****************        Initialization        *************/
    ///* ************************************************************/
    private void initializeDB(){
        GBDatabaseManager.getInstance(this);
    }

    ///* ************************************************************/
    ///* ***************** Routines to switch fragments *************/
    ///* ************************************************************/


    private void initializeFragment() {

        //Set the fragment to Home screen
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.fragment_container);

        if (fragment == null) {
            //when we first create the activity, the fragment needs to be the home screen
            //fragment = new GBTopHomeFragment();
            //fragment = new GBTopConversionFragment();
            fragment = new GBSplashFragment();
            fm.beginTransaction()
                    .add(R.id.fragment_container, fragment)
                    .commit();
        }
    }


    private Fragment getCurrentFragment(){
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        return fm.findFragmentById(R.id.fragment_container);
    }

    private void clearBackStack(){
        //Need the Fragment Manager to do the swap for us
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        //clear the back stack

        while (fm.getBackStackEntryCount() > 0){
            fm.popBackStackImmediate();
        }
    }

    ///* *** Routines to actually switch the screens *******/
    private void switchScreenWithStack(Fragment fragment, String tag) {

        //Need the Fragment Manager to do the swap for us
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        //Are any fragments already being displayed?
        Fragment oldFragment = fm.findFragmentById(R.id.fragment_container);

        if (oldFragment == null) {
            //It shouldn't ever be the case that we got this far with no fragments on the screen,
            // but code defensively. Who knows how the app will evolve
            fm.beginTransaction()
                    .add(R.id.fragment_container, fragment, tag)
                    .commit();
        } else {
            fm.beginTransaction()
                    //replace whatever is being displayed with the Home fragment
                    .replace(R.id.fragment_container, fragment, tag)
                    //and add the transaction to the back stack
                    .addToBackStack(tag)
                    .commit();
        }

    }
    private void switchScreen(Fragment fragment, String tag) {
        //clear the back stack
         clearBackStack();

        switchScreenWithStack(fragment, tag);
    }
    private void switchScreen(Fragment fragment, String tag, int subtitle) {
        switchScreen(fragment, tag);

        setSubtitle(subtitle);
    }
    private void switchScreen(Fragment fragment, String tag, String subtitle) {
        switchScreen(fragment, tag);

        setSubtitle(subtitle);
    }

    void setSubtitle(int subtitle){

        //Put the name of the fragment on the title bar

        if (getSupportActionBar() != null){
            getSupportActionBar().setSubtitle(subtitle);
        }


    }
    void setSubtitle(String subtitle){

        //Put the name of the fragment on the title bar

        if (getSupportActionBar() != null){
            getSupportActionBar().setSubtitle(subtitle);
        }


    }


    void switchToPopBackstack(){
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();

        //settings is at the top of the back stack, so pop it off
        fm.popBackStack();

    }


    void popToScreen(String tag){
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        //fm.popBackStack(tag, FragmentManager.POP_BACK_STACK_INCLUSIVE);

        boolean stillLooking = true;
        if (fm.getBackStackEntryCount() == 0) stillLooking = false;

        int i;
        CharSequence fragName;
        while (stillLooking){
            i = fm.getBackStackEntryCount()-1;
            fragName = fm.getBackStackEntryAt(i).getName();
            if (fragName.equals(tag)){
                stillLooking = false;
            } else {
                fm.popBackStackImmediate();
                if (fm.getBackStackEntryCount() == 0) stillLooking = false;
            }
        }


    }


    void switchToHomeScreen(){
        //replace the fragment with the Home UI

        //Fragment fragment    = new GBTopHomeFragment();
        //Fragment fragment    = new GBTopConversionFragment();
        Fragment fragment    = new GBSplashFragment();
        String   tag         = sHomeTag;
        int      title       = R.string.subtitle_home;

        switchScreen(fragment, tag, title);

    }


    void switchToMeasureScreen(GBPoint point){

        Fragment fragment    = GBCoordinateMeasureFragment.newInstance(point);
        String   tag         = sMeasureTag;

        switchScreen(fragment, tag);

    }


    void switchToPrism4DHomeScreen(){
        //replace the fragment with the Home UI

        Fragment fragment    = new GBTopHomeFragment();
        String   tag         = sHomeTag;
        int      title       = R.string.subtitle_home;

        switchScreen(fragment, tag, title);

    }



    //* ****************************************
    //* PROJECTS
    //* *******************************************/

    void switchToTopProjectScreen() {

        Fragment fragment = new GBTopProjectFragment();
        String tag        = sProjectTopTag;
        int subTitle      = R.string.action_project;

        switchScreen(fragment, tag, subTitle);
    }


    void popToTopProjectScreen(){
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        fm.popBackStack(sProjectTopTag, FragmentManager.POP_BACK_STACK_INCLUSIVE);

    }


    void switchToProjectListScreen(GBPath projectPath){

        Fragment fragment = GBProjectsListFragment.newInstance(projectPath);
        String tag        = sProjectOpenTag;
        String subTitle   = projectPath.getPath().toString();

        switchScreen(fragment, tag, subTitle);


    }


    void switchToProjectCreateScreen(){

        //Close any currently open project
        GBUtilities.getInstance().closeOpenProject(this);

        //Gets the project which contains the defaults for all other projects
        //GBProject project = getProjectForCreate();
        //set the action for the project to create
        GBPath projectPath = new GBPath(GBPath.sCreateTag);

        Fragment fragment = GBProjectEditFragment.newInstance(projectPath);
        String tag        = sProjectCreateTag;
        int subTitle      = R.string.subtitle_create_project;

        switchScreen(fragment, tag, subTitle);


    }


    void switchToProjectOpenScreen(){

        //set the action for the project to open
        GBPath projectPath = new GBPath(GBPath.sOpenTag);

        Fragment fragment = GBProjectsListFragment.newInstance(projectPath);
        String tag        = sProjectOpenTag;
        int subTitle      = R.string.action_open;

        switchScreen(fragment, tag, subTitle);


    }


    void switchToProjectEditScreen(){

        GBProject openProject = GBUtilities.getInstance().getOpenProject(this);
        Fragment fragment;

        if (openProject != null) {
            //if a project is open, assume that is the one the user wants to edit
             //create the path for Edit
            GBPath path = new GBPath(GBPath.sEditTag);
            fragment = GBProjectEditFragment.newInstance(path);
        } else {
            GBPath path = new GBPath(GBPath.sEditTag);
            fragment = GBProjectsListFragment.newInstance(path);
        }
        String tag = sProjectEditTag;

        switchScreen(fragment, tag);
    }


    void switchToProjectCopyScreen(){

        //create the path for copy
        GBPath path = new GBPath(GBPath.sCopyTag);

        Fragment fragment = GBProjectsListFragment.newInstance(path);
        String tag        = sProjectOpenTag;
        int subTitle      = R.string.action_open;

        switchScreen(fragment, tag, subTitle);


    }



    void switchToProjectDeleteScreen(){

        //create the path for open
        GBPath path = new GBPath(sDeleteTag);

        Fragment fragment = GBProjectsListFragment.newInstance(path);
        String tag        = sDeleteTag;
        int subTitle      = R.string.action_delete;

        switchScreen(fragment, tag, subTitle);


    }



    // ******************************************
    // * Export
    // *******************************************/
    void switchToExportScreen(){


        Fragment fragment = new GBExportFragment();
        String tag        = sExportTag;

        switchScreen(fragment, tag);


    }


    // ******************************************
    // * POINTS
    // *******************************************/

    void switchToTopMaintainPointsScreen(
            GBProject project,
            GBPath projectPath){


        Fragment fragment =  GBTopPointFragment.newInstance(project, projectPath);
        String tag        = sPointTopTag;
        int subTitle      = R.string.subtitle_maintain_point;

        switchScreen(fragment, tag, subTitle);


    }


    void switchToPointCreateScreen(GBProject project){

        GBPath pointPath = new GBPath(GBPath.sCreateTag);

        Fragment fragment =  GBPointEditFragment.newInstance(pointPath, null);
        String tag        = sPointCreateTag;
        int subTitle      = R.string.subtitle_create_point;

        switchScreen(fragment, tag, subTitle);

    }

    void switchToPointsListScreen(GBPath pointPath){

        //lists the points on the currently open project
        Fragment fragment =  GBPointListFragment.newInstance(pointPath);
        switchScreen(fragment, getPointTag(pointPath));
    }

    private String getPointTag(GBPath path){

        //figure out the Tag from the path we are on
        CharSequence pointPath = path.getPath();
        String tag;
        if (pointPath.equals(GBPath.sCopyTag)){
            tag = sPointCopyTag;
        } else if (pointPath.equals(GBPath.sOpenTag)) {
            tag = sPointOpenTag;
        } else if (pointPath.equals(sDeleteTag)) {
            tag = sPointDeleteTag;
        } else if (pointPath.equals(sShowTag)){
            tag = sPointShowTag;
        } else {
            //todo probably need to throw an exception
            tag = getResources().getString(R.string.unknown_process);
        }
        return tag;
    }

    void switchToPointEditScreen(GBPath  pointPath,
                                 GBPoint point){

        Fragment fragment =  GBPointEditFragment.newInstance(pointPath, point);
        String tag        = sPointEditTag;

        switchScreen(fragment, tag);
     }


    void switchToRawListScreen(long tokenID){

        //lists the raw coordinates
        Fragment fragment = GBMeanTokenFragment.newInstance(tokenID);
        String tag        = sMeanTokenTag;

        switchScreen(fragment, tag);
    }


    // ******************************************
     // * COLLECT
     // *******************************************/

    void switchToTopCollectScreen(){

        Fragment fragment = new GBTopCollectFragment();
        String tag        = sCollectTopTag;
        int subTitle      = R.string.action_collect;

        switchScreen(fragment, tag, subTitle);

    }


    void switchToCollectPointsScreen(){

        Fragment fragment = new GBPointCollectFragment();
        String tag        = sCollectPointsTag;

        switchScreen(fragment, tag);


    }


    void switchToMapPointsScreen(){

        Fragment fragment = new GBPointMapFragment();
        String tag        = sMapPointsTag;

        switchScreen(fragment, tag);


    }


    // ******************************************
    // * STACKOUT
    // *******************************************/

    void switchToTopStakeoutScreen(){

        Fragment fragment = new GBTopStakeoutFragment();
        String tag        = sStakeoutTopTag;
        int subTitle      = R.string.action_stakeout;

        switchScreen(fragment, tag, subTitle);

    }


    // ******************************************
    // * COGO
    // *******************************************/


    void switchToTopCogoScreen(){

        Fragment fragment = new GBTopCogoFragment();
        String tag        = sCogoTopTag;
        int subTitle      = R.string.action_cogo;

        switchScreen(fragment, tag, subTitle);


    }




    // ******************************************
    // * Coordinate Conversion Fragments
    // *******************************************/

    void switchToCoordConvert(){

        Fragment fragment = new GBCoordinateMeasureFragment();
        String tag        = sConversionTag;
        int subTitle      = R.string.subtitle_measure;

        switchScreen(fragment, tag, subTitle);


    }

    void switchToCoordWorkflow(){

        Fragment fragment = new GBCoordWorkflowFragment();
        String tag        = sCogoWorkflowTag;
        int subTitle      = R.string.subtitle_workflow;

        switchScreen(fragment, tag, subTitle);


    }

    void switchToConvertScreen(){

        Fragment fragment = new GBCoordConversionOldFragment();
        String tag        = sConversionTag;
        int subTitle      = R.string.subtitle_measure;

        switchScreen(fragment, tag, subTitle);


    }




    // ******************************************
     // * Maps
     // *******************************************/

    void switchToTopMapsScreen(){

        Fragment fragment = new GBTopMapsFragment();
        String tag        = sMapsTopTag;
        int subTitle      = R.string.subtitle_maps;

        switchScreen(fragment, tag, subTitle);


    }




    // ******************************************
    // * SKYPLOT
    // *******************************************/

    void switchToTopSkyplotScreen(){

        Fragment fragment = new GBTopSkyplotsFragment();
        String tag        = sSkyplotTopTag;
        int subTitle      = R.string.action_skyplots;

        switchScreen(fragment, tag, subTitle);


    }



    void switchToListNmeaScreen(){

        Fragment fragment = new GBNmeaListFragment();
        String tag        = sSkyplotListNmeaTag;
        int subTitle      = R.string.subtitle_list_nmea;

        switchScreen(fragment, tag, subTitle);

    }



    void switchToListSatellitesScreen(){

        Fragment fragment = new GBSatellitesListFragment();
        String tag        = sSkyplotListSatelliteTag;
        int subTitle      = R.string.subtitle_list_satellites;

        switchScreenWithStack(fragment, tag);


    }



    void switchToGpsNmeaScreen(){

        Fragment fragment = new GBGpsFromNmeaFragment();
        String tag        = sSkyplotGpsNmeaTag;
        int subTitle      = R.string.subtitle_gps_nmea;

        switchScreen(fragment, tag, subTitle);


    }



    // ******************************************
    // * CONFIG
    // *******************************************/


    void switchToTopConfigScreen(){

        Fragment fragment = new GBTopConfigurationsFragment();
        String tag        = sConfigTopTag;
        int subTitle      = R.string.action_config;

        switchScreen(fragment, tag, subTitle);


    }

    void switchToCompassScreen(){

        Fragment fragment = new GBCompassFragment();
        String tag        = sCompassTag;
        int subTitle      = R.string.subtitle_compass;

        switchScreen(fragment, tag, subTitle);

    }



    // ******************************************
    // * SETTINGS
    // *******************************************/



    void switchToTopSettingsScreen(){

        Fragment fragment = new GBTopSettingsFragment();
        String tag        = sSettingsTopTag;
        int subTitle      = action_settings;

        switchScreen(fragment, tag, subTitle);

    }



    void switchToGeneralSettingsScreen(){

        Fragment fragment = new GBGeneralSettingsFragment();
        String tag        = sSettingsGeneralTag;
        int subTitle      = subtitle_general_settings;

        switchScreen(fragment, tag, subTitle);


    }






    void switchToSettingsGlobalScreen(){

        Fragment fragment = new GBSettingsGlobalFragment();
        String tag        = sSettingsGlobalTag;
        int subTitle      = action_global_settings;

        switchScreen(fragment, tag, subTitle);


    }





    // ******************************************
     // * Support
     // *******************************************/


    void switchToTopSupportScreen(){

        Fragment fragment = new GBTopSupportFragment();
        String tag        = sSupportTopTag;
        int subTitle      = subtitle_support;

        switchScreen(fragment, tag, subTitle);


    }








}
