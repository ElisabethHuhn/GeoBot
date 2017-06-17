package com.asc.msigeosystems.geobot;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
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
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


/**
 * The List Points Fragment is the UI
 * for the user to see points of a given project
 * Created by Elisabeth Huhn on 5/8/2016.
 */
public class GBPointListFragment extends Fragment {

    private static final String TAG = "LIST_POINTS_FRAGMENT";
    /**
     * Create variables for widgets
     *
     */



    private List<GBPoint>  mPointList = new ArrayList<>();


    private int                 mCoordinateWidgetType;



    private int          mProjectID;
    private GBPoint mPoint;



    private GBPoint mSelectedPoint;
    private int          mSelectedPosition;

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


    public static GBPointListFragment newInstance(long   projectID,
                                                  GBPath pointPath){

        Bundle args = new Bundle();
        //don't need the entire project object, just it's id
        args.putLong         (GBProject.sProjectIDTag,    projectID);
        GBPath.putPathInArguments(args, pointPath);

        GBPointListFragment fragment = new GBPointListFragment();

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState){

        super.onCreate(savedInstanceState);

        mProjectID   = getArguments().getInt(GBProject.sProjectIDTag);

        GBPath path  = GBPath.getPathFromArguments(getArguments());
        mPointPath   = path.getPath();
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //1) Inflate the layout for this fragment
        View v = inflater.inflate
                (R.layout.fragment_point_list_gb, container, false);


        wireWidgets(v);

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

    private void wireWidgets(View v){
         //Need a header line for the list, but attributes of coordinate are different
        //depending upon the type of coordinate: Latitude/Longitude or Easting/Northing
        mCoordinateWidgetType = getCoordinateTypeFromProject();

        if (mCoordinateWidgetType == GBCoordinate.sLLWidgets) {
            TextView eastingLabel = (TextView) v.findViewById(R.id.easting_label);
            TextView northingLabel = (TextView) v.findViewById(R.id.northing_label);

            eastingLabel.setText(R.string.point_row_latitude_label);
            northingLabel.setText(R.string.point_row_longitude_label);
        }

    }

    private int getCoordinateTypeFromProject(){
        GBProjectManager projectManager = GBProjectManager.getInstance();
        GBProject project = projectManager.getProject(mProjectID);

        CharSequence coordinateType = project.getProjectCoordinateType();

        int returnCode = GBCoordinate.sUNKWidgets;

        if (!GBUtilities.isEmpty(coordinateType)){
            if (coordinateType.equals(GBCoordinate.sCoordinateTypeWGS84) ||
                    coordinateType.equals(GBCoordinate.sCoordinateTypeNAD83) ){
                returnCode = GBCoordinate.sLLWidgets;
            } else if (coordinateType.equals(GBCoordinate.sCoordinateTypeUTM) ||
                    coordinateType.equals(GBCoordinate.sCoordinateTypeSPCS) ){
                returnCode = GBCoordinate.sENWidgets;
            }
        }
        return returnCode;
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
        RecyclerView recyclerView = (RecyclerView) v.findViewById(R.id.pointsList);


        // The RecyclerView.LayoutManager defines how elements are laid out.
        //3) create and assign a layout manager to the recycler view
        RecyclerView.LayoutManager mLayoutManager  = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);

        //4) create some dummy data and tell the adapter about it
        //  this is done in the singleton container

        //      get our singleton list container
        GBPointManager pointsManager = GBPointManager.getInstance();
        //Get this projects points
        mPointList = pointsManager.getProjectPointsList(mProjectID);


        //5) Use the data to Create and set out points Adapter
        GBPointAdapter adapter = new GBPointAdapter(mPointList);
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

        //No FOOTER on this screen

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
        mSelectedPosition = position;
        mSelectedPoint = mPointList.get(position);
        Toast.makeText(getActivity().getApplicationContext(),
                String.valueOf(mSelectedPoint.getPointID()) + " is selected!",
                Toast.LENGTH_SHORT).show();


        processSelect();

    }

    //executed when enter is selected
    private void processSelect(){
        //Point has to have been selected to do anything here
        GBActivity myActivity = (GBActivity) getActivity();

        if (myActivity != null){
            if (mSelectedPoint != null) {

                //We'll need to pass the path forward

                //What happens depends upon the path
                //But as of 11/11/2016 the only path we should see is EDIT
                if (mPointPath.equals(GBPath.sEditTag)){

                    //if the path is edit, open the selected point
                    myActivity.switchToEditPointScreen( mProjectID,
                                                        new GBPath(mPointPath),
                                                        mSelectedPoint );

                }


            } else {
                //user hasn't selected anything yet
                //for now, just put up a toast that nothing has been pressed yet
                Toast.makeText(getActivity(),
                        R.string.point_no_selection,
                        Toast.LENGTH_LONG).show();
            }
        }

    }

    //Build and display the alert dialog
    private void areYouSureDelete(){
        final View v = getView();
        if (v == null)return;
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.delete_title)
                .setIcon(R.drawable.ground_station_icon)
                .setMessage(R.string.are_you_sure)
                .setPositiveButton(R.string.continue_delete_dont_save, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // continue with delete
                        String message = String.format(Locale.getDefault(),
                                         getString(R.string.point_deleted),
                                         String.valueOf(mSelectedPoint.getPointID()));


                        RecyclerView recyclerView = (RecyclerView) v.findViewById(R.id.pointsList);
                        GBPointAdapter adapter    = (GBPointAdapter) recyclerView.getAdapter();

                        //Need the project to remove the point
                        adapter.removeItem(mSelectedPosition, mProjectID);

                        Toast.makeText(getActivity(),
                                message,
                                Toast.LENGTH_SHORT).show();

                        //delete the point and return to list points
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

    private GBProject getOurProject (int projectID){
        //get the project list
        GBProjectManager projectManager = GBProjectManager.getInstance();
        //      then go get our project out of the list
        GBProject ourProject = projectManager.getProject(projectID);
        if (ourProject == null){
            Toast.makeText(getActivity(),R.string.project_missing_exception,Toast.LENGTH_SHORT).show();
            throw new RuntimeException(getString(R.string.project_missing_exception));
            //todo really need to throw an exception here?

        }
        return ourProject;
    }

    //Add some code to improve the recycler view
    interface ClickListener {
        void onClick(View view, int position);

        void onLongClick(View view, int position);
    }

    static class RecyclerTouchListener implements RecyclerView.OnItemTouchListener {

        private GestureDetector gestureDetector;
        private GBPointListFragment.ClickListener clickListener;

        public RecyclerTouchListener(Context context,
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


