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
 * The Stakeout Fragment is the top level selection UI
 * for stakeout functions
 * Created by Elisabeth Huhn on 4/13/2016.
 */
public class GBTopStakeoutFragment extends Fragment {


    public GBTopStakeoutFragment() {
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

    private void setSubtitle() {
        ((GBActivity) getActivity()).setSubtitle(R.string.subtitle_stakeout);
    }



    private void wireWidgets(View v){
        //Tell the user which project is open
        TextView screenLabel = (TextView) v.findViewById(R.id.matrix_screen_label);
        screenLabel.setText(GBUtilities.getInstance().getOpenProjectIDMessage(getActivity()));
        int color = ContextCompat.getColor(getActivity(), R.color.colorWhite);
        screenLabel.setBackgroundColor(color);



        //Stake Points Button
        Button pointsButton = (Button) v.findViewById(R.id.row1Button1);
        pointsButton.setText(R.string.stakeout_points_button_label);
        pointsButton.setBackgroundResource(R.color.colorGray);
        //the order of images here is left, top, right, bottom
        pointsButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_311_sopoints, 0, 0);
        pointsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GBUtilities.getInstance().showStatus(getActivity(), R.string.stakeout_points_button_label);

            }
        });




        //Measure Lines Button
        Button linesButton = (Button) v.findViewById(R.id.row1Button2);
        linesButton.setText(R.string.stakeout_lines_button_label);
        linesButton.setBackgroundResource(R.color.colorGray);
        //the order of images here is left, top, right, bottom
        linesButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_312_solines, 0, 0);
        linesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GBUtilities.getInstance().showStatus(getActivity(), R.string.stakeout_lines_button_label);

            }
        });


        //Arcs Button
        Button arcButton = (Button) v.findViewById(R.id.row1Button3);
        arcButton.setText(R.string.stakeout_arcs_button_label);
        arcButton.setBackgroundResource(R.color.colorGray);
        arcButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_313_socurves, 0, 0);
        arcButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){

                GBUtilities.getInstance().showStatus(getActivity(), R.string.stakeout_arcs_button_label);

            }
        });

        //Offset Button
        Button mOffsetsButton = (Button) v.findViewById(R.id.row2Button1);
        mOffsetsButton.setText(R.string.stakeout_offset_button_label);
        mOffsetsButton.setBackgroundResource(R.color.colorGray);
        mOffsetsButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_314_sooffsets, 0, 0);
        mOffsetsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                GBUtilities.getInstance().showStatus(getActivity(), R.string.stakeout_offset_button_label);

            }
        });

        //Grids
        Button mGridsButton = (Button) v.findViewById(R.id.row2Button2);
        mGridsButton.setText(R.string.stakeout_grids_button_label);
        mGridsButton.setBackgroundResource(R.color.colorGray);
        mGridsButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_315_sogrids, 0, 0);
        mGridsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){

                GBUtilities.getInstance().showStatus(getActivity(), R.string.stakeout_grids_button_label);

            }
        });

        //DTMs Button
        Button mDTMsButton = (Button) v.findViewById(R.id.row2Button3);
        mDTMsButton.setText(R.string.stakeout_dtms_button_label);
        mDTMsButton.setBackgroundResource(R.color.colorGray);
        mDTMsButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_316_sodtms, 0, 0);
        mDTMsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){

                GBUtilities.getInstance().showStatus(getActivity(), R.string.stakeout_dtms_button_label);

            }
        });

        //Allignment Button
        Button allignmentsButton = (Button) v.findViewById(R.id.row3Button1);
        allignmentsButton.setText(R.string.stakeout_alignments_button_label);
        allignmentsButton.setBackgroundResource(R.color.colorGray);
        allignmentsButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_317_soalignments, 0, 0);
        allignmentsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){

                GBUtilities.getInstance().showStatus(getActivity(), R.string.stakeout_alignments_button_label);

            }
        });

        //Sections Button
        Button sectionsButton = (Button) v.findViewById(R.id.row3Button2);
        sectionsButton.setText(R.string.stakeout_sections_button_label);
        sectionsButton.setBackgroundResource(R.color.colorGray);
        sectionsButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_318_sosections, 0, 0);
        sectionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){

                GBUtilities.getInstance().showStatus(getActivity(), R.string.stakeout_sections_button_label);

            }
        });



        //Stakeout Report Button
        Button reportButton = (Button) v.findViewById(R.id.row3Button3);
        reportButton.setText(R.string.stakeout_report_button_label);
        reportButton.setBackgroundResource(R.color.colorGray);
        reportButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_319_soreports, 0, 0);
        reportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){

                GBUtilities.getInstance().showStatus(getActivity(), R.string.stakeout_report_button_label);

            }
        });

    }
}


