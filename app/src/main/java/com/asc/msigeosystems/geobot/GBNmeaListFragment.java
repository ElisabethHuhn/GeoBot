package com.asc.msigeosystems.geobot;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
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
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * The List Nmea Fragment is the UI
 * for the user to see the NMEA Sentences received from GPS
 * Created by Elisabeth Huhn on 5/8/2016.
 */
public class GBNmeaListFragment extends Fragment implements //GpsStatus.Listener,
                                                                     LocationListener,
                                                                     GpsStatus.NmeaListener{

    private static final String TAG = "LIST_NMEA_FRAGMENT";
    /**
     * Create variables for all the widgets
     *  although in the mockup, most will be statically defined in the xml
     */


    private List<GBNmea>   mNmeaList = new ArrayList<>();



    private LocationManager mLocationManager;
    private GBNmea          mNmeaData;
    private GBNmeaParser    mNmeaParser = GBNmeaParser.getInstance();

    private GBNmea          mSelectedNmea;
    private int             mSelectedPosition;

    private CharSequence    mNmeaSentenceType;

    Handler                 mHandler;
    long                    mClockSkew;
    //LastFixUpdater          mLastFixUpdater;
    long                    mLastUpdateTime = -1;

    private GpsStatus mGpsStatus = null;

    //private int counter = 0;

   //*********************************************************/
    //          Fragment Lifecycle Functions                  //
   //*********************************************************/


    //Constructor
    public GBNmeaListFragment() {
        //for now, we don't need to initialize anything when the fragment
        //  is first created

    }

    @Override
    public void onCreate(Bundle savedInstanceState){

        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //1) Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_nmea_list, container, false);
        v.setTag(TAG);

        initializeGPS();

        initializeRecyclerView(v);

        wireWidgets(v);

         //9) return the view
        return v;
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

        //2) find and remember the RecyclerView
        RecyclerView recyclerView = (RecyclerView) v.findViewById(R.id.nmeaList);


        // The RecyclerView.LayoutManager defines how elements are laid out.
        //3) create and assign a layout manager to the recycler view
        RecyclerView.LayoutManager mLayoutManager  = new LinearLayoutManager(getActivity());

        recyclerView.setLayoutManager(mLayoutManager);

        //4) create some dummy data and tell the adapter about it
        // we actually aren't going to do that, it'll be enough
        // when NMEA Sentences come from GPA
        GBNmeaManager nmeaManager = GBNmeaManager.getInstance();

        //      then go get our list of nmea
        mNmeaList = nmeaManager.getNmeaList();


        //5) Use the data to Create and set the Adapter
        GBNmeaLocationAdapter adapter = new GBNmeaLocationAdapter(mNmeaList);
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


    private void wireWidgets(View v){
        //TextView nmeaListSize = (TextView) v.findViewById(R.id.nmeaSizeList) ;
        //initialize the sentence type before any buttons are pressed
        changeType(R.string.gns_sentence_label);

        Button ggaButton = (Button) v.findViewById(R.id.ggaButton);
        ggaButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                changeType(R.string.gga_sentence_label);

            }
        });

        Button gsaButton = (Button) v.findViewById(R.id.gsaButton);
        gsaButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                changeType(R.string.gsa_sentence_label);
            }
        });

        Button gsvButton = (Button) v.findViewById(R.id.gsvButton);
        gsvButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                changeType(R.string.gsv_sentence_label);
            }
        });

        Button gnsButton = (Button) v.findViewById(R.id.gnsButton);
        gnsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                changeType(R.string.gns_sentence_label);
            }
        });

    }

    private void changeType(int resourceString){
        mNmeaSentenceType = getString(resourceString);

        //clear the current contents of the list
        GBNmeaManager nmeaManager = GBNmeaManager.getInstance();
        nmeaManager.removeListContents();

        View v = getView();
        if (v != null) {
            RecyclerView recyclerView = (RecyclerView) v.findViewById(R.id.nmeaList);
            recyclerView.getAdapter().notifyDataSetChanged();

            TextView nmeaListSize = (TextView) v.findViewById(R.id.nmeaSizeList) ;
            nmeaListSize.setText(String.valueOf(recyclerView.getAdapter().getItemCount()));
        }

    }


   //***********************************************************************/
    //                Fragment Lifecycle Functions                          //
   //**********************************************************************/

    //Ask for location events to start
    @Override
    public void onResume() {
        super.onResume();
        setSubtitle();

        startGPS();
      }

    @Override
    public void onStop(){

        super.onStop();
    }

    //Ask for location events to stop
    @Override
    public void onPause() {
        super.onPause();

        stopGPS();
    }

   //***********************************************************************/
    //                       GPS Functions                                  //
   //***********************************************************************/

    private void initializeGPS(){
        if (mLocationManager == null) {
            //Make sure we have the proper GPS permissions before starting
            //If we don't currently have permission, bail
            if ((ContextCompat.checkSelfPermission(getActivity(),
                                                        Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) ||
                (ContextCompat.checkSelfPermission(getActivity(),
                                                        Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED)) {
                return;
            }

            mLocationManager =
                    (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        }
        //but don't turn them on until onResume()
    }

    private void startGPS(){

        if (mLocationManager == null)initializeGPS();

        //If we don't currently have permission, bail
        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){
            return;
        }

        //Location Manager has to be receiving updates for us to receive NMEA sentences
        mLocationManager.requestLocationUpdates("gps", 0, 0.0f, this);
        //mLocationManager.addGpsStatusListener(this);
        mLocationManager.addNmeaListener(this);

    }

    private void stopGPS(){
        if (mLocationManager == null)initializeGPS();

        //If we don't currently have permission, bail
        if ((ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) ||
            (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED)){
            return;
        }

        //Location Manager has to be receiving updates for us to receive NMEA sentences
        mLocationManager.removeUpdates(this);
        //mLocationManager.removeGpsStatusListener(this);
        mLocationManager.removeNmeaListener(this);
    }



    //*************** Listener Callback Routines *****************//
    //        Called by OS when a Listener condition is met       //
    //************************************************************//

   //*********************************************************************/
    /*           GPS NEMA Callback                                        */
   //*********************************************************************/
    @Override
    public void onNmeaReceived (long timestamp, String nmea){
        View v = getView();
        if (v == null)return;

        //create an object with all the fields from the string
        //The parser updates the satellite list.
        mNmeaData = mNmeaParser.parse(nmea);
        if (mNmeaData != null) {

             //Which fields have meaning depend upon the type of the sentence
            String type = mNmeaData.getNmeaType().toString();
            if (type.isEmpty())return ;

            if (type.contains(mNmeaSentenceType)){
                mNmeaList.add(0, mNmeaData);//add new sentence at the top of the list

                  //mRecyclerView.getAdapter().notifyDataSetChanged();
                //notify item inserted rather than data set changed
                //why this makes a difference, I don't know. But the other doesn't scroll

                //end of list
                try {
                    RecyclerView recyclerView = (RecyclerView) v.findViewById(R.id.nmeaList);
                    recyclerView.getAdapter().notifyItemInserted(recyclerView.getAdapter().getItemCount());

                    TextView nmeaListSize = (TextView) v.findViewById(R.id.nmeaSizeList) ;
                    nmeaListSize.setText(String.valueOf( recyclerView.getAdapter().getItemCount()));

                } catch (NullPointerException e){
                    GBUtilities.getInstance().errorHandler(getActivity(), R.string.programming_error);
                }
            }
        }
    }

   //*********************************************************************/
    /*           GPS Location Callbacks                                     */
   //*********************************************************************/

    @Override
    public void onProviderDisabled(String provider) {
        if (LocationManager.GPS_PROVIDER.equals(provider)){

        }
    }

    @Override
    public void onProviderEnabled(String provider) {
        if (LocationManager.GPS_PROVIDER.equals(provider)){

        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        if (!LocationManager.GPS_PROVIDER.equals(provider)){
            return;
        }

    }


    //OS calls this callback when
    // a change has been detected in GPS satellite status

   //*********************************************************************/
    /*           GPS Status Callback                                      */
   //*********************************************************************/
    // The state is one of:
    //      GPS_EVENT_STARTED,
    //      GPS_EVENT_STOPPED,
    //      GPS_EVENT_FIRST_FIX ,
    //      GPS_EVENT_SATELLITE_STATUS
    /*
    @Override
    public void onGpsStatusChanged(int state) {

    }
    */



    //OS calls this callback when the location has changed
   //*********************************************************************/
    /*           GPS Location Callback                                    */
   //*********************************************************************/

    @Override
    public void onLocationChanged(Location loc) {

    }



    //*********************** End of Callbacks *******************//

    //*********************** GPS Utilities *************************//



    //inner  class that runs on another thread
    //every time it runs,
    // it posts another message for itself to run again in a second
    private class LastFixUpdater implements Runnable {
        @Override
        public void run() {
            updateLastUpdateTime();
            mHandler.postDelayed(this, 1000);
        }
    }

    private void updateLastUpdateTime(){
        if (mLastUpdateTime >= 0){
            long t = Math.round((System.currentTimeMillis() - mLastUpdateTime - mClockSkew) / 1000);
            long sec = t % 60;
            long min = (t / 60);
            //mTslf.setData(String.format("%d:%02d", min, sec));
        }
        //mDeviceTime.setData(System.currentTimeMillis());
    }







    //************************ Utilities *************************//

    private void setSubtitle(){
        ((GBActivity)getActivity()).setSubtitle(R.string.subtitle_list_nmea);
    }


    //executed when an item in the list is selected
    private void onSelect(int position){
        mSelectedPosition = position;
        mSelectedNmea = mNmeaList.get(position);
        Toast.makeText(getActivity().getApplicationContext(),
                String.valueOf(mSelectedNmea.getNmeaType()) + " is selected!",
                Toast.LENGTH_SHORT).show();

    }

    //Add some code to improve the recycler view
    interface ClickListener {
        void onClick(View view, int position);

        void onLongClick(View view, int position);
    }

    private static class RecyclerTouchListener implements RecyclerView.OnItemTouchListener {

        private GestureDetector gestureDetector;
        private GBNmeaListFragment.ClickListener clickListener;

        RecyclerTouchListener(Context context,
                                     final RecyclerView recyclerView,
                                     final GBNmeaListFragment.ClickListener
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


