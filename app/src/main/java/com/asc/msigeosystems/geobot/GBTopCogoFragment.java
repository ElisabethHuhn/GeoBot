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
 * The Cogo Fragment is the UI
 * when the user is 
 * Created by elisabethhuhn on 5/132016.
 */
public class GBTopCogoFragment extends Fragment {

    public GBTopCogoFragment() {
        //for now, we don't need to initialize anything when the fragment
        //  is first created
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

    private void setSubtitle(){
        ((GBActivity)getActivity()).setSubtitle(R.string.subtitle_cogo);
    }

    private void wireWidgets(View v){
        //Tell the user which project is open
        TextView screenLabel = (TextView) v.findViewById(R.id.matrix_screen_label);
        screenLabel.setText(GBUtilities.getInstance().getOpenProjectIDMessage(getActivity()));
        int color = ContextCompat.getColor(getActivity(), R.color.colorWhite);
        screenLabel.setBackgroundColor(color);


        //Key in points Button
        Button pointsButton = (Button) v.findViewById(R.id.row1Button1);
        pointsButton.setText(R.string.cogo_points_button_label);
        pointsButton.setBackgroundResource(R.color.colorWhite);
        //the order of images here is left, top, right, bottom
        pointsButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_411_keyinput, 0, 0);
        pointsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //A project must be open to add points to it
                GBProject project = GBUtilities.getInstance().getOpenProject();
                if (project == null){
                    //Tell the user that a project must be open first
                    GBUtilities.getInstance().showStatus(getActivity(),
                            R.string.cogo_project_must_be_open);
                } else {
                    ((GBActivity)getActivity()).switchToPointCreateScreen(project);
                }
            }
        });


        //Inverse Button
        Button inverseButton = (Button) v.findViewById(R.id.row1Button2);
        inverseButton.setText(R.string.cogo_inverse_button_label);
        inverseButton.setBackgroundResource(R.color.colorGray);

        inverseButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_412_inverse, 0, 0);
        inverseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                GBUtilities.getInstance().showStatus(getActivity(), R.string.cogo_inverse_button_label);

            }
        });


        //Intersections Button
        Button intersectionsButton = (Button) v.findViewById(R.id.row1Button3);
        intersectionsButton.setText(R.string.cogo_intersections_button_label);
        intersectionsButton.setBackgroundResource(R.color.colorGray);
        intersectionsButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_413_intersections, 0, 0);
        intersectionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){

                GBUtilities.getInstance().showStatus(getActivity(), R.string.cogo_intersections_button_label);

            }
        });
        //Triangles Button
        Button trianglesButton = (Button) v.findViewById(R.id.row2Button1);
        trianglesButton.setText(R.string.cogo_triangles_button_label);
        trianglesButton.setBackgroundResource(R.color.colorGray);
        trianglesButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_414_triangles, 0, 0);
        trianglesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){

                GBUtilities.getInstance().showStatus(getActivity(),  R.string.cogo_triangles_button_label);

            }
        });

        //Curves Button
        Button mCurvesButton = (Button) v.findViewById(R.id.row2Button2);
        mCurvesButton.setText(R.string.cogo_curves_button_label);
        mCurvesButton.setBackgroundResource(R.color.colorGray);
        mCurvesButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_415_curves, 0, 0);
        mCurvesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){

                GBUtilities.getInstance().showStatus(getActivity(), R.string.cogo_curves_button_label);

            }
        });

        //Areas Button
        Button mAreasButton = (Button) v.findViewById(R.id.row2Button3);
        mAreasButton.setText(R.string.cogo_areas_button_label);
        mAreasButton.setBackgroundResource(R.color.colorGray);
        mAreasButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_416_areas, 0, 0);
        mAreasButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){

                GBUtilities.getInstance().showStatus(getActivity(),  R.string.cogo_areas_button_label);

            }
        });


        //coordinates Button
        Button mCoordinatesButton = (Button) v.findViewById(R.id.row3Button1);
        mCoordinatesButton.setText(R.string.cogo_coordinates_button_label);
        mCoordinatesButton.setBackgroundResource(R.color.colorGray);
        //the order of images here is left, top, right, bottom
        mCoordinatesButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_417_coordinates, 0, 0);
        mCoordinatesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GBUtilities.getInstance().showStatus(getActivity(),  R.string.cogo_coordinates_button_label);

            }
        });

        //Map Check Button
        Button mMapCheckButton = (Button) v.findViewById(R.id.row3Button2);
        mMapCheckButton.setText(R.string.cogo_map_check_button_label);
        mMapCheckButton.setBackgroundResource(R.color.colorGray);
        //mMapCheckButton.setText(R.string.cogo_workflow_button_label);
        mMapCheckButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_418_mapcheck, 0, 0);
        mMapCheckButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){

                GBUtilities.getInstance().showStatus(getActivity(),
                        //R.string.cogo_map_check_button_label,
                        R.string.cogo_map_check_button_label);



            }
        });

        //T Button
        Button translateButton = (Button) v.findViewById(R.id.row3Button3);
        translateButton.setText(R.string.cogo_convert_button_label);
        translateButton.setBackgroundResource(R.color.colorGray);
        translateButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_419_translate, 0, 0);
        translateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){

                GBUtilities.getInstance().showStatus(getActivity(),
                        //R.string.cogo_map_check_button_label,
                        R.string.cogo_convert_button_label);



            }
        });

    }
}


