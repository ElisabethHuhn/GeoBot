package com.asc.msigeosystems.geobot;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;


/**
 * This fragment displays the Mean Token information and
 *  lists all the raw data from the meaning process.
 *
 * Created by Elisabeth Huhn on 7/8/2017
 */
public class GBMeanTokenFragment extends Fragment {

    private static final String TAG = "MEAN_TOKEN_FRAGMENT";


    private long mTokenID;



    //
    /*-********************************************************/
    //                     Constructor                        //
    /*-********************************************************/

    public GBMeanTokenFragment() {
        //for now, we don't need to initialize anything when the fragment
        //  is first created
    }

    /*-********************************************************/
    //          Fragment Lifecycle Functions                  //
    /*-********************************************************/


    public static GBMeanTokenFragment newInstance(long tokenID){

        Bundle args = new Bundle();
        GBMeanToken.putTokenInArguments(args, tokenID);

        GBMeanTokenFragment fragment = new GBMeanTokenFragment();

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState){

        super.onCreate(savedInstanceState);

        //This only works for the open project
        mTokenID = GBMeanToken.getTokenFromArguments(getArguments());
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //1) Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_mean_token, container, false);

        initializeRecyclerView(v);

        initializeUI(v);

        setSubtitle();

        //9) return the view
        return v;
    }

    @Override
    public void onResume(){
        super.onResume();
        setSubtitle();

    }

    private GBMeanToken getToken(){
        GBMeanTokenManager tokenManager = GBMeanTokenManager.getInstance();
        return tokenManager.getMeanTokenFromDB(mTokenID);
    }

    private void setSubtitle() {

        ((GBActivity) getActivity()).setSubtitle(R.string.subtitle_raw_data);

    }

    /*-********************************************************/
    //          Initialization                                //
    /*-********************************************************/

    private void initializeUI(View v) {
        // TODO: 7/9/2017 Still have other variables like StdDev, # satellites that aren't on the screen yet

        initializeMeanProperties(v);
        initializeMeanTitleRow(v);
        initializeMeanCoordinate(v);

        initializeRawCoordTitleWidgets(v);


    }
    private void initializeMeanProperties(View v){

        GBActivity myActivity = (GBActivity)getActivity();
        GBProject openProject = GBUtilities.getInstance().getOpenProject(myActivity);

        int hRmsLbl = R.string.hrms_label;
        int vRmsLbl = R.string.vrms_label;
        if (GBGeneralSettings.isStdDev((GBActivity)getActivity())) {
            hRmsLbl = R.string.mean_lat_sigma;
            vRmsLbl = R.string.mean_lng_sigma;
        }

        EditText projIDOutput    = (EditText)v.findViewById(R.id.tokenProjIDInput) ;
        EditText projNameOutput  = (EditText)v.findViewById(R.id.tokenProjNameInput) ;
        EditText pointIDOutput   = (EditText)v.findViewById(R.id.tokenPointIDInput) ;
        EditText pointNbOutput   = (EditText)v.findViewById(R.id.tokenPointNumbInput) ;
        EditText startTimeOutput = (EditText)v.findViewById(R.id.tokenStartTimeInput);
        EditText endTimeOutput   = (EditText)v.findViewById(R.id.tokenEndTimeInput);
        EditText nbFixedOutput   = (EditText)v.findViewById(R.id.tokenFixedInput);
        EditText nbRawOutput     = (EditText)v.findViewById(R.id.tokenRawInput);
        EditText distUnitsOutput = (EditText)v.findViewById(R.id.tokenDistanceUnitsInput);
        TextView hRmsLabel       = (TextView)v.findViewById(R.id.tokenHrmsLabel) ;
        TextView vRmsLabel       = (TextView)v.findViewById(R.id.tokenVrmsLabel) ;

        long openProjectID = openProject.getProjectID();
        GBMeanToken token  = getToken();
        long  openPointID  = token.getPointID();
        GBPoint openPoint  = GBPointManager.getInstance().getPoint(openProjectID, openPointID);


        projIDOutput  .setText(String.valueOf(openProjectID));
        projNameOutput.setText(String.valueOf(openProject.getProjectName()));
        pointIDOutput .setText(String.valueOf(openPointID));
        pointNbOutput .setText(String.valueOf(openPoint.getPointNumber()));


        long timestamp = (long)token.getStartMeanTime();
        String startTimeStampString = GBUtilities.getDateTimeString(timestamp);
        startTimeOutput.setText(startTimeStampString);

        timestamp = (long)token.getEndMeanTime();
        String endTimeStampString = GBUtilities.getDateTimeString(timestamp);
        endTimeOutput.setText(endTimeStampString);

        nbFixedOutput.setText(String.valueOf(token.getFixedReadings()));
        nbRawOutput  .setText(String.valueOf(token.getRawReadings()));

        distUnitsOutput.setText(openProject.getDistUnitString());

        String hRmsLblString = getString(hRmsLbl);
        String vRmsLblString = getString(vRmsLbl);
        hRmsLabel.setText(hRmsLblString);
        vRmsLabel.setText(vRmsLblString);

    }

    private void initializeMeanTitleRow(View v){
        View field_container;
        TextView label;

        GBActivity myActivity = (GBActivity)getActivity();
        boolean isDD = GBGeneralSettings.isLocDD(myActivity);
        boolean isPM = GBGeneralSettings.isPM(myActivity);

        //set up the labels for the medication list
        field_container = v.findViewById(R.id.meanedCoordinateTitleRow);

        label = (TextView) (field_container.findViewById(R.id.coordinateRowRaw));
        label.setText(getString(R.string.coordinate_row_raw_label));
        label.setBackgroundColor(ContextCompat.getColor(myActivity, R.color.colorGrayish));

        label = (TextView) (field_container.findViewById(R.id.coordinateRowFixed));
        label.setText(getString(R.string.coordinate_row_fixed_label));
        label.setBackgroundColor(ContextCompat.getColor(myActivity, R.color.colorGrayish));


        label = (TextView) (field_container.findViewById(R.id.coordinateRowValid));
        label.setText(getString(R.string.coordinate_row_valid_label));
        label.setBackgroundColor(ContextCompat.getColor(myActivity, R.color.colorGrayish));

        if (isPM){
            //Sign of lat/lng set by plus minus, not direction. So get rid of direction fields
            label = (TextView) (field_container.findViewById(R.id.coordinateRowLatDir));
            label.setVisibility(View.GONE);
            label = (TextView) (field_container.findViewById(R.id.coordinateRowLngDir));
            label.setVisibility(View.GONE);

        }

        if (isDD) {
            label = (TextView) (field_container.findViewById(R.id.coordinateRowLatitude));
            label.setText(getString(R.string.coordinate_row_latitude_label));
            label.setBackgroundColor(ContextCompat.getColor(myActivity, R.color.colorGrayish));

            label = (TextView) (field_container.findViewById(R.id.coordinateRowLongitude));
            label.setText(getString(R.string.coordinate_row_longitude_label));
            label.setBackgroundColor(ContextCompat.getColor(myActivity, R.color.colorGrayish));

            label = (TextView) (field_container.findViewById(R.id.coordinateRowLatitudeDeg));
            label.setVisibility(View.GONE);
            label = (TextView) (field_container.findViewById(R.id.coordinateRowLatitudeMin));
            label.setVisibility(View.GONE);
            label = (TextView) (field_container.findViewById(R.id.coordinateRowLatitudeSec));
            label.setVisibility(View.GONE);

            label = (TextView) (field_container.findViewById(R.id.coordinateRowLongitudeDeg));
            label.setVisibility(View.GONE);
            label = (TextView) (field_container.findViewById(R.id.coordinateRowLongitudeMin));
            label.setVisibility(View.GONE);
            label = (TextView) (field_container.findViewById(R.id.coordinateRowLongitudeSec));
            label.setVisibility(View.GONE);

        } else {
            label = (TextView) (field_container.findViewById(R.id.coordinateRowLatitude));
            label.setVisibility(View.GONE);
            label = (TextView) (field_container.findViewById(R.id.coordinateRowLongitude));
            label.setVisibility(View.GONE);


            label = (TextView) (field_container.findViewById(R.id.coordinateRowLatitudeDeg));
            label.setText(getString(R.string.coordinate_row_lat_deg_label));
            label.setBackgroundColor(ContextCompat.getColor(myActivity, R.color.colorGrayish));

            label = (TextView) (field_container.findViewById(R.id.coordinateRowLatitudeMin));
            label.setText(getString(R.string.coordinate_row_min_label));
            label.setBackgroundColor(ContextCompat.getColor(myActivity, R.color.colorGrayish));

            label = (TextView) (field_container.findViewById(R.id.coordinateRowLatitudeSec));
            label.setText(getString(R.string.coordinate_row_sec_label));
            label.setBackgroundColor(ContextCompat.getColor(myActivity, R.color.colorGrayish));

            label = (TextView) (field_container.findViewById(R.id.coordinateRowLongitudeDeg));
            label.setText(getString(R.string.coordinate_row_lng_deg_label));
            label.setBackgroundColor(ContextCompat.getColor(myActivity, R.color.colorGrayish));

            label = (TextView) (field_container.findViewById(R.id.coordinateRowLongitudeMin));
            label.setText(getString(R.string.coordinate_row_min_label));
            label.setBackgroundColor(ContextCompat.getColor(myActivity, R.color.colorGrayish));

            label = (TextView) (field_container.findViewById(R.id.coordinateRowLongitudeSec));
            label.setText(getString(R.string.coordinate_row_sec_label));
            label.setBackgroundColor(ContextCompat.getColor(myActivity, R.color.colorGrayish));
        }

        label = (TextView) (field_container.findViewById(R.id.coordinateRowElevation));
        label.setText(getString(R.string.coord_row_elevation_label));
        label.setBackgroundColor(ContextCompat.getColor(myActivity, R.color.colorGrayish));

        label = (TextView) (field_container.findViewById(R.id.coordinateRowGeoid));
        label.setText(getString(R.string.coord_row_geoid_label));
        label.setBackgroundColor(ContextCompat.getColor(myActivity, R.color.colorGrayish));
    }
    private void initializeMeanCoordinate(View v){

        View field_container;
        TextView label, tudeView, tudeDView, tudeMView, tudeSView;

        GBActivity myActivity = (GBActivity)getActivity();
        GBProject openProject = GBUtilities.getInstance().getOpenProject(myActivity);
        int distanceUnits     = openProject.getDistanceUnits();
        boolean isDD          = GBGeneralSettings.isLocDD(myActivity);
        boolean isDir         = GBGeneralSettings.isDir(myActivity);
        int locDigOfPrec      = GBGeneralSettings.getLocPrecision(myActivity);

        GBMeanToken token     = getToken();
        GBCoordinateMean coordinate = token.getMeanCoordinate();


        //set up the labels for the final meaned coordinate
        field_container = v.findViewById(R.id.meanedCoordinateRow);

        label = (TextView) (field_container.findViewById(R.id.coordinateRowRaw));
        label.setText(String.valueOf(coordinate.getRawReadings()));
        label.setBackgroundColor(ContextCompat.getColor(myActivity, R.color.colorWhite));

        label = (TextView) (field_container.findViewById(R.id.coordinateRowFixed));
        label.setText(String.valueOf(coordinate.getFixedReadings()));
        label.setBackgroundColor(ContextCompat.getColor(myActivity, R.color.colorWhite));

        String validString = "F";
        if (coordinate.isValidCoordinate())validString = "T";
        label = (TextView) (field_container.findViewById(R.id.coordinateRowValid));
        label.setText(String.valueOf(validString));
        label.setBackgroundColor(ContextCompat.getColor(myActivity, R.color.colorWhite));


        if (!isDir){
            label    = (TextView) (field_container.findViewById(R.id.coordinateRowLatDir));
            label.setVisibility(View.GONE);
            label    = (TextView) (field_container.findViewById(R.id.coordinateRowLngDir));
            label.setVisibility(View.GONE);
        }

        if ( isDD) {
            double lat = coordinate.getLatitude();
            label    = (TextView) (field_container.findViewById(R.id.coordinateRowLatDir));
            tudeView = (TextView) (field_container.findViewById(R.id.coordinateRowLatitude));
            int posHemi = R.string.hemisphere_N;
            int negHemi = R.string.hemisphere_S;
            GBUtilities.locDD(myActivity, lat, locDigOfPrec,
                              isDir, posHemi, negHemi, label, tudeView);

            double lng = coordinate.getLatitude();
            label    = (TextView) (field_container.findViewById(R.id.coordinateRowLngDir));
            tudeView = (TextView) (field_container.findViewById(R.id.coordinateRowLongitude));
            posHemi = R.string.hemisphere_E;
            negHemi = R.string.hemisphere_W;
            GBUtilities.locDD(myActivity, lng, locDigOfPrec,
                              isDir, posHemi, negHemi, label, tudeView);

            tudeDView = (TextView) (field_container.findViewById(R.id.coordinateRowLatitudeMin));
            tudeDView.setVisibility(View.GONE);
            tudeMView = (TextView) (field_container.findViewById(R.id.coordinateRowLatitudeDeg));
            tudeMView.setVisibility(View.GONE);
            tudeSView = (TextView) (field_container.findViewById(R.id.coordinateRowLatitudeSec));
            tudeSView.setVisibility(View.GONE);

            tudeDView = (TextView) (field_container.findViewById(R.id.coordinateRowLongitudeDeg));
            tudeDView.setVisibility(View.GONE);
            tudeMView = (TextView) (field_container.findViewById(R.id.coordinateRowLongitudeMin));
            tudeMView.setVisibility(View.GONE);
            tudeSView = (TextView) (field_container.findViewById(R.id.coordinateRowLongitudeSec));
            tudeSView.setVisibility(View.GONE);

        } else {
            label    = (TextView) (field_container.findViewById(R.id.coordinateRowLatDir));
            label.setVisibility(View.GONE);
            tudeView = (TextView) (field_container.findViewById(R.id.coordinateRowLatitude));
            tudeView.setVisibility(View.GONE);
            label    = (TextView) (field_container.findViewById(R.id.coordinateRowLngDir));
            label.setVisibility(View.GONE);
            tudeView = (TextView) (field_container.findViewById(R.id.coordinateRowLongitude));
            tudeView.setVisibility(View.GONE);

            int latDeg = coordinate.getLatitudeDegree();
            int latMin = coordinate.getLatitudeMinute();
            double latSec = coordinate.getLatitudeSecond();
            label = (TextView) (field_container.findViewById(R.id.coordinateRowLatDir));
            tudeDView = (TextView) (field_container.findViewById(R.id.coordinateRowLatitudeMin));
            tudeMView = (TextView) (field_container.findViewById(R.id.coordinateRowLatitudeDeg));
            tudeSView = (TextView) (field_container.findViewById(R.id.coordinateRowLatitudeSec));
            int posHemi = R.string.hemisphere_N;
            int negHemi = R.string.hemisphere_S;

            GBUtilities.locDMS(myActivity, latDeg, latMin, latSec, locDigOfPrec,
                               isDir, posHemi, negHemi,
                               label,tudeDView,tudeMView,tudeSView);

            int lngDeg = coordinate.getLatitudeDegree();
            int lngMin = coordinate.getLatitudeMinute();
            double lngSec = coordinate.getLatitudeSecond();
            label = (TextView) (field_container.findViewById(R.id.coordinateRowLatDir));
            tudeDView = (TextView) (field_container.findViewById(R.id.coordinateRowLongitudeDeg));
            tudeMView = (TextView) (field_container.findViewById(R.id.coordinateRowLongitudeMin));
            tudeSView = (TextView) (field_container.findViewById(R.id.coordinateRowLongitudeSec));
            posHemi = R.string.hemisphere_E;
            negHemi = R.string.hemisphere_W;
            GBUtilities.locDMS(myActivity, lngDeg, lngMin, lngSec, locDigOfPrec,
                               isDir, posHemi, negHemi,
                               label,tudeDView,tudeMView,tudeSView);

        }

        double elevation ;
        double geoid;
        if (distanceUnits == GBProject.sMeters){
            elevation = coordinate.getElevation();
            geoid     = coordinate.getGeoid();
        } else if (distanceUnits == GBProject.sFeet){
            elevation = coordinate.getElevationFeet();
            geoid     = coordinate.getGeoidFeet();
        } else {//international feet
            elevation = coordinate.getElevationIFeet();
            geoid     = coordinate.getGeoidIFeet();
        }

        int tudeColor= R.color.colorPosNumber;
        if (elevation < 0.)  {
            tudeColor = R.color.colorNegNumber;
        }

        label = (TextView) (field_container.findViewById(R.id.coordinateRowElevation));
        label.setText(String.valueOf(elevation));
        label.setBackgroundColor(ContextCompat.getColor(myActivity, R.color.colorWhite));
        label .setTextColor(ContextCompat.getColor(myActivity, tudeColor));

        tudeColor= R.color.colorPosNumber;
        if (geoid < 0.)  {
            tudeColor = R.color.colorNegNumber;
        }

        label = (TextView) (field_container.findViewById(R.id.coordinateRowGeoid));
        label.setText(String.valueOf(geoid));
        label.setBackgroundColor(ContextCompat.getColor(myActivity, R.color.colorWhite));
        label .setTextColor(ContextCompat.getColor(myActivity, tudeColor));

        //Standard deviation is displayed above the mean values
        EditText hRmsInput       = (EditText)v.findViewById(R.id.tokenHrmsInput) ;
        EditText vRmsInput       = (EditText)v.findViewById(R.id.tokenVrmsInput) ;
        hRmsInput.setText(String.valueOf(coordinate.getLongitudeStdDev()));
        vRmsInput.setText(String.valueOf(coordinate.getLatitudeStdDev()));
    }

    private void initializeRawCoordTitleWidgets(View v){
        View field_container;
        TextView label;

        GBActivity myActivity = (GBActivity)getActivity();
        boolean isDD = GBGeneralSettings.isLocDD(myActivity);
        boolean isPM = GBGeneralSettings.isPM(myActivity);

        //set up the labels for the medication list
        field_container = v.findViewById(R.id.rawCoordinateTitleRow);

        label = (TextView) (field_container.findViewById(R.id.coordinateRowTime));
        label.setText(R.string.coordinate_row_time_label);
        label.setBackgroundColor(ContextCompat.getColor(myActivity, R.color.colorGrayish));

        label = (TextView) (field_container.findViewById(R.id.coordinateRowFixed));
        label.setText(R.string.coordinate_row_fixed_label);
        label.setBackgroundColor(ContextCompat.getColor(myActivity, R.color.colorGrayish));

        label = (TextView) (field_container.findViewById(R.id.coordinateRowValid));
        label.setText(R.string.coordinate_row_valid_label);
        label.setBackgroundColor(ContextCompat.getColor(myActivity, R.color.colorGrayish));

        if (isPM){
            //Sign of lat/lng set by plus minus, not direction. So get rid of direction fields
            label = (TextView) (field_container.findViewById(R.id.coordinateRowLatDir));
            label.setVisibility(View.GONE);
            label = (TextView) (field_container.findViewById(R.id.coordinateRowLngDir));
            label.setVisibility(View.GONE);

        }

        if (isDD) {
            label = (TextView) (field_container.findViewById(R.id.coordinateRowLatitude));
            label.setText(R.string.coordinate_row_latitude_label);
            label.setBackgroundColor(ContextCompat.getColor(myActivity, R.color.colorGrayish));

            label = (TextView) (field_container.findViewById(R.id.coordinateRowLongitude));
            label.setText(R.string.coordinate_row_longitude_label);
            label.setBackgroundColor(ContextCompat.getColor(myActivity, R.color.colorGrayish));

            label = (TextView) (field_container.findViewById(R.id.coordinateRowLatitudeDeg));
            label.setVisibility(View.GONE);
            label = (TextView) (field_container.findViewById(R.id.coordinateRowLatitudeMin));
            label.setVisibility(View.GONE);
            label = (TextView) (field_container.findViewById(R.id.coordinateRowLatitudeSec));
            label.setVisibility(View.GONE);

            label = (TextView) (field_container.findViewById(R.id.coordinateRowLongitudeDeg));
            label.setVisibility(View.GONE);
            label = (TextView) (field_container.findViewById(R.id.coordinateRowLongitudeMin));
            label.setVisibility(View.GONE);
            label = (TextView) (field_container.findViewById(R.id.coordinateRowLongitudeSec));
            label.setVisibility(View.GONE);
        } else {
            label = (TextView) (field_container.findViewById(R.id.coordinateRowLatitude));
            label.setVisibility(View.GONE);
            label = (TextView) (field_container.findViewById(R.id.coordinateRowLongitude));
            label.setVisibility(View.GONE);


            label = (TextView) (field_container.findViewById(R.id.coordinateRowLatitudeDeg));
            label.setText(R.string.coordinate_row_deg_label);
            label.setBackgroundColor(ContextCompat.getColor(myActivity, R.color.colorGrayish));

            label = (TextView) (field_container.findViewById(R.id.coordinateRowLatitudeMin));
            label.setText(R.string.coordinate_row_min_label);
            label.setBackgroundColor(ContextCompat.getColor(myActivity, R.color.colorGrayish));

            label = (TextView) (field_container.findViewById(R.id.coordinateRowLatitudeSec));
            label.setText(R.string.coordinate_row_sec_label);
            label.setBackgroundColor(ContextCompat.getColor(myActivity, R.color.colorGrayish));

            label = (TextView) (field_container.findViewById(R.id.coordinateRowLongitudeDeg));
            label.setText(R.string.coordinate_row_deg_label);
            label.setBackgroundColor(ContextCompat.getColor(myActivity, R.color.colorGrayish));

            label = (TextView) (field_container.findViewById(R.id.coordinateRowLongitudeMin));
            label.setText(R.string.coordinate_row_min_label);
            label.setBackgroundColor(ContextCompat.getColor(myActivity, R.color.colorGrayish));

            label = (TextView) (field_container.findViewById(R.id.coordinateRowLongitudeSec));
            label.setText(R.string.coordinate_row_sec_label);
            label.setBackgroundColor(ContextCompat.getColor(myActivity, R.color.colorGrayish));

        }

        label = (TextView) (field_container.findViewById(R.id.coordinateRowElevation));
        label.setText(R.string.coordinate_row_elevation_label);
        label.setBackgroundColor(ContextCompat.getColor(myActivity, R.color.colorGrayish));

        label = (TextView) (field_container.findViewById(R.id.coordinateRowGeoid));
        label.setText(R.string.coordinate_row_geoid_label);
        label.setBackgroundColor(ContextCompat.getColor(myActivity, R.color.colorGrayish));
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
        v.setTag(TAG);

        //2) find and remember the RecyclerView
        RecyclerView recyclerView = (RecyclerView) v.findViewById(R.id.rawCoordinateList);


        // The RecyclerView.LayoutManager defines how elements are laid out.
        //3) create and assign a layout manager to the recycler view
        RecyclerView.LayoutManager mLayoutManager  = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);

        //4) Get points from the open Project

        GBProject openProject = GBUtilities.getInstance().getOpenProject((GBActivity)getActivity());
        if (openProject == null)return;

        //5) Use the data to Create and set out points Adapter

        boolean isDD = GBGeneralSettings.isLocDD((GBActivity)getActivity());
        boolean isDir = GBGeneralSettings.isDir(((GBActivity)getActivity()));

        ArrayList<GBCoordinateWGS84> rawCoordinates = getToken().getCoordinates();
        GBCoordinateRawAdapter adapter = new GBCoordinateRawAdapter((GBActivity)getActivity(),
                rawCoordinates,
                isDD, isDir,
                openProject.getDistanceUnits());
        recyclerView.setAdapter(adapter);

        //6) create and set the itemAnimator
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        //7) create and add the item decorator
        recyclerView.addItemDecoration(new DividerItemDecoration(
                getActivity(), LinearLayoutManager.VERTICAL));


        //8) add event listeners to the recycler view
        recyclerView.addOnItemTouchListener(
                new RecyclerTouchListener(getActivity(), recyclerView, new ClickListener() {

                    @Override
                    public void onClick(View view, int position) {
                        onSelect(position);
                    }

                    @Override
                    public void onLongClick(View view, int position) {
                        //for now, ignore the long click
                    }
                }));

    }

    /*-********************************************************/
    //      Utility Functions used in handling events         //
    /*-********************************************************/

    //executed when an item in the list is selected
    private void onSelect(int position){
        View v = getView();
        if (v == null)return;

        RecyclerView recyclerView = (RecyclerView) v.findViewById(R.id.pointsList);
        GBCoordinateRawAdapter adapter    = (GBCoordinateRawAdapter) recyclerView.getAdapter();

        GBCoordinateWGS84 selectedCoordinate = adapter.getCoordinateList().get(position);

        GBUtilities.getInstance().showStatus(getActivity(),
                String.valueOf(selectedCoordinate.getCoordinateID()) + " is selected!");


        //Coordinate has to have been selected to do anything here
        //GBActivity myActivity = (GBActivity) getActivity();

        // TODO: 7/8/2017 do something with selected coordinate

    }

    //Add some code to improve the recycler view
    interface ClickListener {
        void onClick(View view, int position);

        void onLongClick(View view, int position);
    }

    static class RecyclerTouchListener implements RecyclerView.OnItemTouchListener {

        private GestureDetector gestureDetector;
        private GBMeanTokenFragment.ClickListener clickListener;

        RecyclerTouchListener(Context context,
                                     final RecyclerView recyclerView,
                                     final GBMeanTokenFragment.ClickListener
                                             clickListener) {

            this.clickListener = clickListener;
            gestureDetector = new GestureDetector(context,
                    new GestureDetector.SimpleOnGestureListener() {

                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    View child = recyclerView.findChildViewUnder(e.getX(), e.getY());
                    if (child != null && clickListener != null) {
                        clickListener.onLongClick(child, recyclerView.getChildLayoutPosition(child));
                    }
                }
            });
        }

        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {

            View child = rv.findChildViewUnder(e.getX(), e.getY());
            if (child != null && clickListener != null && gestureDetector.onTouchEvent(e)) {
                clickListener.onClick(child, rv.getChildLayoutPosition(child));
            }
            return false;
        }

        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent e) {
        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

        }
    }
}


