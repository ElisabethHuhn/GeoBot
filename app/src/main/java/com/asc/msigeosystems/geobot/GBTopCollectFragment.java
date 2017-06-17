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
 * The Project Fragment is the UI
 * when the user is creating / making changes to the project definition
 * Created by elisabethhuhn on 4/13/2016.
 */
public class GBTopCollectFragment extends Fragment {

    public GBTopCollectFragment() {
        //for now, we don't need to initialize anything when the fragment
        //  is first created
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_top_matrix, container, false);


        //Wire up the UI widgets so they can handle events later
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
        ((GBActivity) getActivity()).setSubtitle(R.string.subtitle_collect);
    }

    private void wireWidgets(View v){
        //Tell the user which project is open
        TextView screenLabel = (TextView) v.findViewById(R.id.matrix_screen_label);
        screenLabel.setText(GBUtilities.getInstance().getOpenProjectIDMessage(getActivity()));
        int color = ContextCompat.getColor(getActivity(), R.color.colorWhite);
        screenLabel.setBackgroundColor(color);


        //Measure Points Button
        Button pointsButton = (Button) v.findViewById(R.id.row1Button1);
        pointsButton.setText(R.string.collect_points_button_label);
        //the order of images here is left, top, right, bottom
        pointsButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_211_dcpoints, 0, 0);
        pointsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Location source must also be defined
                // TODO: 12/2/2016 define how location source will be identified in configurations
                //Can only collect points if a project is open
                GBActivity myActivity = (GBActivity) getActivity();
                GBProject openProject = GBUtilities.getInstance().getOpenProject();
                if (openProject != null) {
                    //Switch the fragment to the collect with maps fragment.
                    // But the switching happens on the container Activity
                    myActivity.switchToCollectPointsScreen();
                } else {
                    GBUtilities.getInstance().showStatus(getActivity(),
                                   R.string.collect_project_must_be_open);
                }
            }
        });

        //Measure Lines Button
        Button linesButton = (Button) v.findViewById(R.id.row1Button2);
        linesButton.setText(R.string.collect_lines_button_label);
        linesButton.setBackgroundResource(R.color.colorGray);
        //the order of images here is left, top, right, bottom
        linesButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_212_dclines, 0, 0);
        linesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GBUtilities.getInstance().showStatus(getActivity(), R.string.collect_lines_button_label);

            }
        });


        //Measure Areas Button
        Button areasButton = (Button) v.findViewById(R.id.row1Button3);
        areasButton.setText(R.string.collect_areas_button_label);
        areasButton.setBackgroundResource(R.color.colorGray);
        areasButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_213_dcareas, 0, 0);
        areasButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){

                GBUtilities.getInstance().showStatus(getActivity(), R.string.collect_areas_button_label);

            }
        });

        //Autostore Button
        Button autoStoreButton = (Button) v.findViewById(R.id.row2Button1);
        autoStoreButton.setText(R.string.autostore_button_label);
        autoStoreButton.setBackgroundResource(R.color.colorGray);
        autoStoreButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_214_dcauto, 0, 0);
        autoStoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                GBUtilities.getInstance().showStatus(getActivity(), R.string.autostore_button_label);

            }
        });


        //Traverse Button
        Button traverseButton = (Button) v.findViewById(R.id.row2Button2);
        traverseButton.setText(R.string.traverse_button_label);
        traverseButton.setBackgroundResource(R.color.colorGray);
        traverseButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_215_dctraverse, 0, 0);
        traverseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){

                GBUtilities.getInstance().showStatus(getActivity(), R.string.traverse_button_label);

            }
        });

        //Resection Button
        Button resectionButton = (Button) v.findViewById(R.id.row2Button3);
        resectionButton.setText(R.string.resection_button_label);
        resectionButton.setBackgroundResource(R.color.colorGray);
        resectionButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_216_dcresection, 0, 0);
        resectionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){

                GBUtilities.getInstance().showStatus(getActivity(), R.string.resection_button_label);

            }
        });

        //Monitor Button
        Button monitorButton = (Button) v.findViewById(R.id.row3Button1);
        monitorButton.setText(R.string.monitor_button_label);
        monitorButton.setBackgroundResource(R.color.colorGray);
        monitorButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_217_dcmonitor, 0, 0);
        monitorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){

                GBUtilities.getInstance().showStatus(getActivity(), R.string.monitor_button_label);

            }
        });

        //Scan Button
        Button scanButton = (Button) v.findViewById(R.id.row3Button2);
        scanButton.setText(R.string.scan_button_label);
        scanButton.setBackgroundResource(R.color.colorGray);
        scanButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_218_dcscan, 0, 0);
        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){

                GBUtilities.getInstance().showStatus(getActivity(), R.string.scan_button_label);

            }
        });

        //Level Button
        Button levelButton = (Button) v.findViewById(R.id.row3Button3);
        levelButton.setText(R.string.level_button_label);
        levelButton.setBackgroundResource(R.color.colorGray);
        levelButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_219_dclevel, 0, 0);
        levelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){

                GBUtilities.getInstance().showStatus(getActivity(), R.string.level_button_label);

            }
        });

    }
}


