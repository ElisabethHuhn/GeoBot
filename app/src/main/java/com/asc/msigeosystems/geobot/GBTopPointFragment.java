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
 * The Points Fragment is the UI
 * when the user is creating / making changes to project points
 * Created by Elisabeth Huhn on 5/15/2016.
 */
public class GBTopPointFragment extends Fragment {

    /**
     * Create variables for all the widgets
     *  although in the mockup, most will be statically defined in the xml
     */




    //input arguments
    private GBPath mProjectPath;



    //*************************************************
    //********   Constructors
    //*************************************************
    public GBTopPointFragment() {
        //for now, we don't need to initialize anything when the fragment
        //  is first created
    }


    //*************************************************
    //********   Life Cycle Methods
    //*************************************************

    public static GBTopPointFragment newInstance(
            GBProject project,
            GBPath projectPath){

        Bundle args = GBProject.putProjectInArguments(new Bundle(), project);

        args = GBPath.putPathInArguments(args, projectPath);

        GBTopPointFragment fragment = new GBTopPointFragment();

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState){

        super.onCreate(savedInstanceState);

        mProjectPath = GBPath.getPathFromArguments(getArguments());

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_top_matrix, container, false);


        //Wire up the UI widgets so they can handle events later
        wireWidgets(v);

        return v;
    }

    @Override
    public void onResume(){
        super.onResume();
        setSubtitle();
    }

    private void setSubtitle() {
        ((GBActivity) getActivity()).setSubtitle(R.string.subtitle_points);
    }

    //*************************************************
    //********   Initialization Methods
    //*************************************************

    private void wireWidgets(View v){


        //Tell the user which project is open
        TextView screenLabel = (TextView) v.findViewById(R.id.matrix_screen_label);
        GBUtilities constantsAndUtilities = GBUtilities.getInstance();
        screenLabel.setText(constantsAndUtilities.getOpenProjectIDMessage(getActivity()));
        int color = ContextCompat.getColor(getActivity(), R.color.colorWhite);
        screenLabel.setBackgroundColor(color);


        //List Point Button
        Button listPoints = (Button) v.findViewById(R.id.row1Button1);
        listPoints.setText(R.string.list_points_button_label);
        //the order of images here is left, top, right, bottom
        listPoints.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_001_folders, 0, 0);
        listPoints.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GBActivity activity = (GBActivity)getActivity();
                activity.switchToListPointsScreen(
                            GBUtilities.getInstance().getOpenProjectID(),
                            new GBPath(GBPath.sShowTag));

            }
        });

        //Create Button
        Button createButton = (Button) v.findViewById(R.id.row1Button2);
        createButton.setText(R.string.create_button_label);
        //the order of images here is left, top, right, bottom
        createButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_001_folders, 0, 0);
        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Switch the fragment to the collect with maps fragment.
                // But the switching happens on the container Activity
                ((GBActivity)getActivity()).switchToPointCreateScreen(GBUtilities.getInstance().getOpenProject());
            }
        });


        //copy Button
        Button copyButton = (Button) v.findViewById(R.id.row1Button3);
        copyButton.setText(R.string.copy_button_label);
        copyButton.setBackgroundResource(R.color.colorGray);
        copyButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_001_folders, 0, 0);
        copyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                GBUtilities.getInstance().showStatus(getActivity(), R.string.point_no_copy);
                /*
                ((GBActivity)getActivity()).switchToListPointsScreen(
                            mProject.getProjectID(),
                            new GBPath(GBPath.sCopyTag));
                 */
            }
        });

        //Edit Button
        Button editButton = (Button) v.findViewById(R.id.row2Button1);
        editButton.setText(R.string.edit_button_label);
        editButton.setBackgroundResource(R.color.colorGray);
        editButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_001_folders, 0, 0);
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                GBUtilities.getInstance().showStatus(getActivity(), R.string.edit_button_label);

                GBPath pointPath = new GBPath(GBPath.sEditTag) ;
                ((GBActivity)getActivity()).switchToListPointsScreen(GBUtilities.getInstance().
                                                                    getOpenProjectID(),pointPath);

            }
        });

        //Delete Button
        Button deleteButton = (Button) v.findViewById(R.id.row2Button2);
        deleteButton.setText(R.string.delete_button_label);
        deleteButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_001_folders, 0, 0);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){

                ((GBActivity)getActivity()).switchToListPointsScreen(GBUtilities.getInstance()
                                                                    .getOpenProjectID(),
                                                                    new GBPath(GBPath.sDeleteTag));
            }
        });

        //6 Button
        Button m6Button = (Button) v.findViewById(R.id.row2Button3);
        m6Button.setText(R.string.unused_button_label);
        //m6Button.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_001_folders, 0, 0);
        m6Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){

                GBUtilities.getInstance().showStatus(getActivity(), R.string.unused_button_label);

            }
        });

        //7 Button
        Button m7Button = (Button) v.findViewById(R.id.row3Button1);
        m7Button.setText(R.string.unused_button_label);
        //m7Button.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_001_folders, 0, 0);
        m7Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){

                GBUtilities.getInstance().showStatus(getActivity(), R.string.unused_button_label);

            }
        });

        //8 Button
        Button m8Button = (Button) v.findViewById(R.id.row3Button2);
        m8Button.setText(R.string.unused_button_label);
        //m8Button.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_001_folders, 0, 0);
        m8Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){

                GBUtilities.getInstance().showStatus(getActivity(), R.string.unused_button_label);

            }
        });

        //9
        Button m9Button = (Button) v.findViewById(R.id.row3Button3);
        m9Button.setText(R.string.unused_button_label);
        //m9Button.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_001_folders, 0, 0);
        m9Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){

                GBUtilities.getInstance().showStatus(getActivity(), R.string.unused_button_label);

            }
        });

    }
}


