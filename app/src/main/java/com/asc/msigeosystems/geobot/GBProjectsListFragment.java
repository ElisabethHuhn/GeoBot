package com.asc.msigeosystems.geobot;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import java.util.List;

/**
 * The List Project Fragment is the UI
 * when the user can see the projects residing on this handset
 * Created by Elisabeth Huhn on 5/15/2016.
 */
public class GBProjectsListFragment extends Fragment {

    private static final String TAG = "LIST_PROJECTS_FRAGMENT";
    /*
     * Create variables for all the widgets
     * although in the mockup, most will be statically defined in the xml
     */

    private List<GBProject>  mProjectList;

    private GBProject        mSelectedProject;
    private int              mSelectedPosition;

    private CharSequence     mProjectPath;


    /* ********************************************************/
    //          Fragment Lifecycle Functions                  //
    /* ********************************************************/

    //this is called by Activity to store parameters before fragment is instantiated
    public static GBProjectsListFragment newInstance(GBPath projectPath) {

        Bundle args = new Bundle();
        GBPath.putPathInArguments(args, projectPath);

        GBProjectsListFragment fragment = new GBProjectsListFragment();

        fragment.setArguments(args);
        return fragment;
    }

    //Constructor
    public GBProjectsListFragment() {
        //for now, we don't need to initialize anything when the fragment
        //  is first created
    }

    //This is where parameters are unbundled
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        GBPath path = GBPath.getPathFromArguments(getArguments());
        mProjectPath = path.getPath();

        //This would be the place to do anything unique to a given path

    }


    //set up the recycler view
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_project_list_gb, container, false);

        initializeRecyclerView(v);

        wireWidgets(v);

        //subtitle is based on the path
        setSubtitle();



            return v;
    }

    @Override
    public void onResume(){
        super.onResume();
        setSubtitle();
    }


    /* ********************************************************/
    //      Utility Functions used in handling events         //
    /* ********************************************************/

    private void initializeRecyclerView(View v){
        try {

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
            //1) Inflate the layout for this fragment
            v.setTag(TAG);

            //2) find and remember the RecyclerView
            RecyclerView recyclerView = (RecyclerView) v.findViewById(R.id.projectsList);

            //3) create and assign a layout manager to the recycler view
            RecyclerView.LayoutManager mLayoutManager  = new LinearLayoutManager(getActivity());
            recyclerView.setLayoutManager(mLayoutManager);

            //4) read data in from the database and tell the adapter about it
            //   this is now done in the projects container singleton

            //      get the singleton list container
            GBProjectManager projectManager = GBProjectManager.getInstance();
            //      then go get our list of projects
            mProjectList = projectManager.getProjectList();

            //5) Use the data to Create and set out project Adapter
            GBProjectAdapter adapter = new GBProjectAdapter(mProjectList);
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

                        }
                    }));

            //No FOOTER on this screen

            //9) return the view
        }catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }

    }


    private void wireWidgets(View v){

        //Exit Button
        Button projectExitButton = (Button) v.findViewById(R.id.projectExitButton);
        //button is always enabled
        projectExitButton.setEnabled(true);
        projectExitButton.setTextColor(Color.BLACK);
        projectExitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){

                ((GBActivity) getActivity()).popToTopProjectScreen();

            }
        });
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


    //called from onClick(), executed when a project is selected
    private void onSelect(int position){
        //todo need to update selection visually
        mSelectedPosition = position;
        // This is the one case where we can access the list directly
        //all other maintenance must go through the ProjectManager
        mSelectedProject = mProjectList.get(position);

        Toast.makeText(getActivity().getApplicationContext(),
                mSelectedProject.getProjectName() + " is selected!",
                Toast.LENGTH_SHORT).show();

        GBActivity myActivity = (GBActivity) getActivity();
        if (myActivity != null){
            if (mSelectedProject != null) {

                //We'll need to pass the path forward
                //GBPath toProjectPath = new GBPath(mProjectPath);

                /* *************************  OPEN   ***************************/
                if (mProjectPath.equals(GBPath.sOpenTag)){
                    GBUtilities constantsAndUtilities =
                                                        GBUtilities.getInstance();

                    //Save the opened project id up in the Activity
                    constantsAndUtilities.setOpenProject  (mSelectedProject);

                    Toast.makeText(getActivity(),
                                   constantsAndUtilities.getOpenProjectIDMessage(getActivity()),
                                   Toast.LENGTH_SHORT).show();

                    //switch back to the Home Screen
                    myActivity.switchToHomeScreen();


                /* *************************  COPY   ***************************/
                }else if (mProjectPath.equals(GBPath.sCopyTag)){
                    //do a deep copy, assign the next projectID,
                    // and add the copied project to memory and the DB
                    GBProjectManager projectManager = GBProjectManager.getInstance();
                    boolean assignNextID = true;
                    GBProject toProject = projectManager.deepCopyProject((GBActivity) getActivity(),
                                                                          mSelectedProject,
                                                                          assignNextID);
                    boolean addToDBToo = true;
                    boolean cascadeFlag = true;
                    projectManager.addProject(toProject, addToDBToo, cascadeFlag);

                    //then switch to EDIT project with the new project
                    myActivity.switchToProjectEditScreen(toProject);

                /* *************************  DELETE   ***************************/
                }else if (mProjectPath.equals(GBPath.sDeleteTag)){

                    //ask the user if they are sure they want to proceed.
                    areYouSureDelete();

                /* *************************  EDIT   ***************************/
                }else if (mProjectPath.equals(GBPath.sEditTag)){

                    //if the path is Edit, open the selected project for update
                    myActivity.switchToProjectEditScreen(mSelectedProject);

                /* *************************  UNKNOWN!!!   ***************************/
                }else {

                    //todo need to throw an unrecognized path exception
                    Toast.makeText(getActivity(),
                            R.string.unrecognized_path_encountered,
                            Toast.LENGTH_SHORT).show();

                    //for now, go home
                    myActivity.switchToHomeScreen();

                }
            }
        }
    }

    //Build and display the alert dialog
    private void areYouSureDelete(){
        String msg = getString(R.string.delete_title)+ ": " + mSelectedProject.getProjectName();
        new AlertDialog.Builder(getActivity())
                .setTitle(msg)
                .setIcon(R.drawable.ground_station_icon)
                .setMessage(R.string.are_you_sure)
                .setPositiveButton(R.string.continue_delete_dont_save, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // continue with delete
                        deleteProject();
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

    private void deleteProject(){

        View v = getView();
        if (v == null)return;
        RecyclerView recyclerView = (RecyclerView) v.findViewById(R.id.projectsList);

        GBProjectAdapter adapter = (GBProjectAdapter) recyclerView.getAdapter();
        adapter.removeItem(mSelectedPosition);

        GBUtilities constantsAndUtilities =
                                                        GBUtilities.getInstance();

        long openProjectID = constantsAndUtilities.getOpenProjectID();
        if (openProjectID == mSelectedProject.getProjectID()){

            constantsAndUtilities.setOpenProject(null);
        }


        CharSequence message =
                "Project " + mSelectedProject.getProjectName() + " is deleted";
        Toast.makeText(getActivity(),
                message,
                Toast.LENGTH_SHORT).show();

        ((GBActivity) getActivity()).popToTopProjectScreen();

    }



    /* ********************************************************/
    //      RecyclerView Support code                         //
    /* ********************************************************/

    //interface for event handlers for Click and LongClick
    interface ClickListener {
        void onClick(View view, int position);

        void onLongClick(View view, int position);
    }

    static class RecyclerTouchListener implements RecyclerView.OnItemTouchListener {

        private GestureDetector gestureDetector;
        private GBProjectsListFragment.ClickListener clickListener;

        RecyclerTouchListener(Context context,
                                     final RecyclerView recyclerView,
                                     final GBProjectsListFragment.ClickListener
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


