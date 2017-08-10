package com.asc.msigeosystems.geobot;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;




/**
 * The List Points Fragment is the UI
 * for the user to see points of a given project
 * Created by Elisabeth Huhn on 5/8/2016.
 */
public class GBPointListFragment extends Fragment {

    private static final String TAG = "LIST_POINTS_FRAGMENT";


    private CharSequence mPointPath;



    //
    /*-********************************************************/
    //                     Constructor                        //
    /*-********************************************************/

    public GBPointListFragment() {
        //for now, we don't need to initialize anything when the fragment
        //  is first created
    }

    /*-********************************************************/
    //          Fragment Lifecycle Functions                  //
    /*-********************************************************/


    public static GBPointListFragment newInstance(GBPath pointPath){

        Bundle args = new Bundle();
        GBPath.putPathInArguments(args, pointPath);

        GBPointListFragment fragment = new GBPointListFragment();

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState){

        super.onCreate(savedInstanceState);

        //This only works for the open project
        GBPath path  = GBPath.getPathFromArguments(getArguments());
        mPointPath   = path.getPath();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //1) Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_point_list_gb, container, false);

        initializeUI(v);

        initializeRecyclerView(v);

        setSubtitle(mPointPath);

        //9) return the view
        return v;
    }

    @Override
    public void onResume(){
        super.onResume();
        setSubtitle(mPointPath);

    }

    private void initializeUI(View v){
        GBActivity myActivity = (GBActivity)getActivity();
        boolean isPM = GBGeneralSettings.isPM(myActivity);
         //Need a header line for the list, but attributes of coordinate are different
        //depending upon the type of coordinate: Latitude/Longitude or Easting/Northing
        GBProject openProject = GBUtilities.getInstance().getOpenProject(myActivity);
        int coordinateWidgetType = GBUtilities.getCoordinateTypeFromProject(openProject);

        TextView latHemiLabel = (TextView) v.findViewById(R.id.lat_hemisphere_label);
        TextView lngHemiLabel = (TextView) v.findViewById(R.id.lng_hemisphere_label);
        if ((isPM) || (coordinateWidgetType != GBCoordinate.sLLWidgets)){
            //Get rid of the hemisphere labels
            latHemiLabel.setVisibility(View.GONE);
            lngHemiLabel.setVisibility(View.GONE);
        }

        if (coordinateWidgetType == GBCoordinate.sLLWidgets) {

            TextView latitudeLabel  = (TextView) v.findViewById(R.id.northing_label);
            TextView longitudeLabel = (TextView) v.findViewById(R.id.easting_label);
            if (GBGeneralSettings.isLngLat(myActivity)) {
                latitudeLabel  = (TextView) v.findViewById(R.id.easting_label);
                longitudeLabel = (TextView) v.findViewById(R.id.northing_label);
            }

            latitudeLabel .setText(R.string.point_row_latitude_label);
            longitudeLabel.setText(R.string.point_row_longitude_label);


        } else  {

            TextView northingLabel = (TextView) v.findViewById(R.id.northing_label);
            TextView eastingLabel = (TextView) v.findViewById(R.id.easting_label);
            if (GBGeneralSettings.isEN(myActivity)) {
                northingLabel = (TextView) v.findViewById(R.id.easting_label);
                eastingLabel = (TextView) v.findViewById(R.id.northing_label);
            }

            northingLabel.setText(R.string.point_row_northing_label);
            eastingLabel.setText(R.string.easting_label);
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
        GBActivity myActivity = (GBActivity)getActivity();
        v.setTag(TAG);

        //2) find and remember the RecyclerView
        RecyclerView recyclerView = (RecyclerView) v.findViewById(R.id.pointsList);


        // The RecyclerView.LayoutManager defines how elements are laid out.
        //3) create and assign a layout manager to the recycler view
        RecyclerView.LayoutManager mLayoutManager  = new LinearLayoutManager(myActivity);
        recyclerView.setLayoutManager(mLayoutManager);

        //4) Get points from the open Project

        GBProject openProject = GBUtilities.getInstance().getOpenProject(myActivity);
        if (openProject == null)return;

        //5) Use the data to Create and set out points Adapter
        GBPointAdapter adapter = new GBPointAdapter(myActivity, openProject.getPoints());
        recyclerView.setAdapter(adapter);

        //6) create and set the itemAnimator
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        //7) create and add the item decorator
        recyclerView.addItemDecoration(new DividerItemDecoration(myActivity,
                                                                 LinearLayoutManager.VERTICAL));


        //8) add event listeners to the recycler view
        recyclerView.addOnItemTouchListener(
                new RecyclerTouchListener(myActivity, recyclerView, new ClickListener() {

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

    private void setSubtitle(CharSequence path) {
        int subtitle;

        if (path.equals(GBPath.sCopyTag)){
            subtitle = R.string.subtitle_copy_point;

        } else if (path.equals(GBPath.sOpenTag)) {
            subtitle = R.string.subtitle_open_point;

        } else if (path.equals(GBPath.sDeleteTag)){
            subtitle = R.string.subtitle_delete_point;

        } else if (path.equals(GBPath.sEditTag)) {
            subtitle = R.string.subtitle_edit_point;

        } else if (path.equals(GBPath.sShowTag)) {
            subtitle = R.string.subtitle_show_point;

        } else {
            //todo probably need to throw an exception
            subtitle = R.string.subtitle_unknown_error;

        }
        ((GBActivity) getActivity()).setSubtitle(subtitle);

    }


    /*-********************************************************/
    //      Utility Functions used in handling events         //
    /*-********************************************************/

    //executed when an item in the list is selected
    private void onSelect(int position){
        View v = getView();
        if (v == null)return;

        RecyclerView recyclerView = (RecyclerView) v.findViewById(R.id.pointsList);
        GBPointAdapter adapter    = (GBPointAdapter) recyclerView.getAdapter();

        GBPoint selectedPoint = adapter.getPointList().get(position);

        GBUtilities.getInstance().showStatus(getActivity(),
                String.valueOf(selectedPoint.getPointID()) + " is selected!");


        //Point has to have been selected to do anything here
        GBActivity myActivity = (GBActivity) getActivity();

        if (mPointPath.equals(GBPath.sEditTag)){

            //if the path is edit, open the selected point
            long openProjectID = GBUtilities.getInstance().getOpenProjectID((GBActivity)getActivity());
            myActivity.switchToPointEditScreen(new GBPath(mPointPath),
                                               selectedPoint );

        }

    }

    //Add some code to improve the recycler view
    interface ClickListener {
        void onClick(View view, int position);

        void onLongClick(View view, int position);
    }

    static class RecyclerTouchListener implements RecyclerView.OnItemTouchListener {

        private GestureDetector gestureDetector;
        private GBPointListFragment.ClickListener clickListener;

         RecyclerTouchListener(Context context,
                                     final RecyclerView recyclerView,
                                     final GBPointListFragment.ClickListener
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


