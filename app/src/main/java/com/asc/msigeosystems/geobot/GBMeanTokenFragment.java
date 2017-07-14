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
        GBProject openProject = GBUtilities.getInstance().getOpenProject((GBActivity)getActivity());
        boolean isDD = false;
        if (openProject.getDDvDMS() == GBProject.sDD)isDD = true;

        View v;
        if (isDD) {
            v = inflater.inflate(R.layout.fragment_mean_token_dd, container, false);
        } else {
            v = inflater.inflate(R.layout.fragment_mean_token_dms, container, false);
        }

        wireListTitleWidgets(v);

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



    private void wireListTitleWidgets(View v){
        View field_container;
        TextView label;

        GBProject openProject = GBUtilities.getInstance().getOpenProject((GBActivity)getActivity());
        boolean isDD = false;
        if (openProject.getDDvDMS() == GBProject.sDD) isDD = true;

        GBActivity myActivity = (GBActivity)getActivity();

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

        if (isDD) {
            label = (TextView) (field_container.findViewById(R.id.coordinateRowLatitude));
            label.setText(R.string.coordinate_row_latitude_label);
            label.setBackgroundColor(ContextCompat.getColor(myActivity, R.color.colorGrayish));

            label = (TextView) (field_container.findViewById(R.id.coordinateRowLongitude));
            label.setText(R.string.coordinate_row_longitude_label);
            label.setBackgroundColor(ContextCompat.getColor(myActivity, R.color.colorGrayish));
        } else {

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

    private void initializeUI(View v) {
        // TODO: 7/9/2017 Still have other variables like StdDev, # satellites that aren't on the screen yet

        initializeMeanProperties(v);
        initializeMeanTitleRow(v);
        initializeRawCoordinates(v);
    }

    private void initializeMeanProperties(View v){

        GBProject openProject = GBUtilities.getInstance().getOpenProject((GBActivity)getActivity());

        int distanceUnits = openProject.getDistanceUnits();
        CharSequence duString = openProject.getDistUnitString();

        int rmsVstdDev = openProject.getRMSvStD();
        int hRmsLbl = R.string.hrms_label;
        int vRmsLbl = R.string.vrms_label;
        if (rmsVstdDev == GBProject.sStdDev) {
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
        EditText hRmsInput       = (EditText)v.findViewById(R.id.tokenHrmsInput) ;
        EditText vRmsInput       = (EditText)v.findViewById(R.id.tokenVrmsInput) ;

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

        distUnitsOutput.setText(duString);

        String hRmsLblString = getString(hRmsLbl);
        String vRmsLblString = getString(vRmsLbl);
        hRmsLabel.setText(hRmsLblString);
        vRmsLabel.setText(vRmsLblString);

    }

    private void initializeRawCoordinates(View v){

        View field_container;
        TextView label;

        GBProject openProject = GBUtilities.getInstance().getOpenProject((GBActivity)getActivity());
        boolean isDD = false;
        if (openProject.getDDvDMS() == GBProject.sDD) isDD = true;

        GBMeanToken token = getToken();
        GBCoordinateMean coordinate = token.getMeanCoordinate();

        int distanceUnits = openProject.getDistanceUnits();

        GBActivity myActivity = (GBActivity)getActivity();

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


        if ( isDD) {
            label = (TextView) (field_container.findViewById(R.id.coordinateRowLatitude));
            label.setText(String.valueOf(coordinate.getLatitude()));
            label.setBackgroundColor(ContextCompat.getColor(myActivity, R.color.colorWhite));
            //check sign
            int tudeColor= R.color.colorPosNumber;
            if (coordinate.getLatitude() < 0) {
                tudeColor = R.color.colorNegNumber;
            }
            label .setTextColor(ContextCompat.getColor(myActivity, tudeColor));



            label = (TextView) (field_container.findViewById(R.id.coordinateRowLongitude));
            label.setText(String.valueOf(coordinate.getLongitude()));
            label.setBackgroundColor(ContextCompat.getColor(myActivity, R.color.colorWhite));
            tudeColor= R.color.colorPosNumber;
            if (coordinate.getLongitude() < 0) {
                tudeColor = R.color.colorNegNumber;
            }
            label .setTextColor(ContextCompat.getColor(myActivity, tudeColor));

        } else {
            int tudeColor= R.color.colorPosNumber;
            if ((coordinate.getLatitudeDegree() < 0) ||
                (coordinate.getLatitudeMinute() < 0) ||
                (coordinate.getLatitudeSecond() < 0)) {
                tudeColor = R.color.colorNegNumber;
            }

            label = (TextView) (field_container.findViewById(R.id.coordinateRowLatitudeDeg));
            label.setText(String.valueOf(coordinate.getLatitudeDegree()));
            label.setBackgroundColor(ContextCompat.getColor(myActivity, R.color.colorWhite));
            label .setTextColor(ContextCompat.getColor(myActivity, tudeColor));

            label = (TextView) (field_container.findViewById(R.id.coordinateRowLatitudeMin));
            label.setText(String.valueOf(coordinate.getLatitudeMinute()));
            label.setBackgroundColor(ContextCompat.getColor(myActivity, R.color.colorWhite));
            label .setTextColor(ContextCompat.getColor(myActivity, tudeColor));

            label = (TextView) (field_container.findViewById(R.id.coordinateRowLatitudeSec));
            label.setText(String.valueOf(coordinate.getLatitudeSecond()));
            label.setBackgroundColor(ContextCompat.getColor(myActivity, R.color.colorWhite));
            label .setTextColor(ContextCompat.getColor(myActivity, tudeColor));


            tudeColor= R.color.colorPosNumber;
            if ((coordinate.getLongitudeDegree() < 0) ||
                    (coordinate.getLongitudeMinute() < 0) ||
                    (coordinate.getLongitudeSecond() < 0)) {
                tudeColor = R.color.colorNegNumber;
            }

            label = (TextView) (field_container.findViewById(R.id.coordinateRowLongitudeDeg));
            label.setText(String.valueOf(coordinate.getLongitudeDegree()));
            label.setBackgroundColor(ContextCompat.getColor(myActivity, R.color.colorWhite));
            label .setTextColor(ContextCompat.getColor(myActivity, tudeColor));

            label = (TextView) (field_container.findViewById(R.id.coordinateRowLongitudeMin));
            label.setText(String.valueOf(coordinate.getLongitudeMinute()));
            label.setBackgroundColor(ContextCompat.getColor(myActivity, R.color.colorWhite));
            label .setTextColor(ContextCompat.getColor(myActivity, tudeColor));

            label = (TextView) (field_container.findViewById(R.id.coordinateRowLongitudeSec));
            label.setText(String.valueOf(coordinate.getLongitudeSecond()));
            label.setBackgroundColor(ContextCompat.getColor(myActivity, R.color.colorWhite));
            label .setTextColor(ContextCompat.getColor(myActivity, tudeColor));

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
        if (elevation < 0)  {
            tudeColor = R.color.colorNegNumber;
        }

        label = (TextView) (field_container.findViewById(R.id.coordinateRowElevation));
        label.setText(String.valueOf(elevation));
        label.setBackgroundColor(ContextCompat.getColor(myActivity, R.color.colorWhite));
        label .setTextColor(ContextCompat.getColor(myActivity, tudeColor));

        tudeColor= R.color.colorPosNumber;
        if (geoid < 0)  {
            tudeColor = R.color.colorNegNumber;
        }

        label = (TextView) (field_container.findViewById(R.id.coordinateRowGeoid));
        label.setText(String.valueOf(geoid));
        label.setBackgroundColor(ContextCompat.getColor(myActivity, R.color.colorWhite));
        label .setTextColor(ContextCompat.getColor(myActivity, tudeColor));

        //Standard deviation is in the header
        EditText hRmsInput       = (EditText)v.findViewById(R.id.tokenHrmsInput) ;
        EditText vRmsInput       = (EditText)v.findViewById(R.id.tokenVrmsInput) ;
        hRmsInput.setText(String.valueOf(coordinate.getLongitudeStdDev()));
        vRmsInput.setText(String.valueOf(coordinate.getLatitudeStdDev()));
    }

    private void initializeMeanTitleRow(View v){
        View field_container;
        TextView label;

        GBProject openProject = GBUtilities.getInstance().getOpenProject((GBActivity)getActivity());
        boolean isDD = false;
        if (openProject.getDDvDMS() == GBProject.sDD) isDD = true;


        GBActivity myActivity = (GBActivity)getActivity();

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

        if (isDD) {
            label = (TextView) (field_container.findViewById(R.id.coordinateRowLatitude));
            label.setText(getString(R.string.coordinate_row_latitude_label));
            label.setBackgroundColor(ContextCompat.getColor(myActivity, R.color.colorGrayish));

            label = (TextView) (field_container.findViewById(R.id.coordinateRowLongitude));
            label.setText(getString(R.string.coordinate_row_longitude_label));
            label.setBackgroundColor(ContextCompat.getColor(myActivity, R.color.colorGrayish));
        } else {

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

    private GBMeanToken getToken(){
        GBMeanTokenManager tokenManager = GBMeanTokenManager.getInstance();
        return tokenManager.getMeanTokenFromDB(mTokenID);
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
        int ddVdms = openProject.getDDvDMS();
        boolean isDD = true;
        if (ddVdms == GBProject.sDMS){
            isDD = false;
        }
        ArrayList<GBCoordinateWGS84> rawCoordinates = getToken().getCoordinates();
        GBCoordinateRawAdapter adapter = new GBCoordinateRawAdapter((GBActivity)getActivity(),
                                                                    rawCoordinates,
                                                                    isDD,
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

    private void setSubtitle() {

        ((GBActivity) getActivity()).setSubtitle(R.string.subtitle_raw_data);

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

        public RecyclerTouchListener(Context context,
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


