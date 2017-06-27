package com.asc.msigeosystems.geobot;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;


/**
 * The Home screen for the Conversion product
 * Elisabeth Huhn 6/23/2017
 */
public class GBTopConversionFragment extends Fragment {


    public GBTopConversionFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //Initialize the DB if necessary
        GBDatabaseManager.getInstance(getActivity());

        //Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_top_matrix, container, false);


        //Wire up the UI widgets so they can handle events later
        wireWidgets(v);

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        setSubtitle();
    }

    private void setSubtitle(){
        ((GBActivity)getActivity()).setSubtitle(R.string.subtitle_home);
    }


    private void wireWidgets(View v){
        //Tell the user which project is open
        /*
        TextView screenLabel = (TextView) v.findViewById(R.id.matrix_screen_label);


        screenLabel.setText(GBUtilities.getInstance().getOpenProjectIDMessage(getActivity()));
        int color = ContextCompat.getColor(getActivity(), R.color.colorWhite);
        screenLabel.setBackgroundColor(color);
        */



        Button convertCoordinatesButton = (Button) v.findViewById(R.id.row1Button1);
        convertCoordinatesButton.setText(R.string.convert_coordinates);
        convertCoordinatesButton.setFocusable(false);
        convertCoordinatesButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_002_collect, 0, 0);
        convertCoordinatesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){

                ((GBActivity) getActivity()).switchToCoordConvert();

            }
        });

        Button coordinateWorkflowButton = (Button) v.findViewById(R.id.row1Button2);
        coordinateWorkflowButton.setText(R.string.cogo_workflow_button_label);
        //the order of images here is left, top, right, bottom
        coordinateWorkflowButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_817_generalsettings, 0, 0);
        coordinateWorkflowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ((GBActivity) getActivity()).switchToCoordWorkflow();
            }
        });


        Button convertCoordinatesOldButton = (Button) v.findViewById(R.id.row1Button3);
        convertCoordinatesOldButton.setText(R.string.convert_coordinates);
        //the order of images here is left, top, right, bottom
        convertCoordinatesOldButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_817_generalsettings, 0, 0);
        convertCoordinatesOldButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ((GBActivity) getActivity()).switchToConvertScreen();
            }
        });





        Button listNmeaSentencesButton = (Button) v.findViewById(R.id.row2Button1);
        listNmeaSentencesButton.setText(R.string.skyplot_nmea_sentence_label);
        listNmeaSentencesButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_817_generalsettings, 0, 0);
        listNmeaSentencesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                ((GBActivity)getActivity()).switchToListNmeaScreen();


            }
        });


        Button listSattelitesButton = (Button) v.findViewById(R.id.row2Button2);
        listSattelitesButton.setText(R.string.skyplot_list_satellites);
        listSattelitesButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_817_generalsettings, 0, 0);
        listSattelitesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){

                ((GBActivity) getActivity()).switchToListSatellitesScreen();

            }
        });





        Button compassFragmentButton = (Button) v.findViewById(R.id.row2Button3);
        compassFragmentButton.setText(R.string.skyplot_compass_fragment);
        compassFragmentButton.setFocusable(true);
        compassFragmentButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_817_generalsettings, 0, 0);
        compassFragmentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){

                GBUtilities.getInstance().showStatus(getActivity(), R.string.skyplot_compass_fragment);
                ((GBActivity)getActivity()).switchToCompassScreen();

            }
        });




        //config Button
        Button configButton = (Button) v.findViewById(R.id.row3Button1);
        configButton.setText(R.string.config_button_label);
        configButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_007_config, 0, 0);
        configButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ((GBActivity) getActivity()).switchToTopConfigScreen();


            }
        });

        //settings Button
        Button settingsButton = (Button) v.findViewById(R.id.row3Button2);
        settingsButton.setText(R.string.settings_button_label);
        settingsButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_008_settings, 0, 0);
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){

                ((GBActivity) getActivity()).switchToTopSettingsScreen();
            }
        });

        //help Button
        Button helpButton = (Button) v.findViewById(R.id.row3Button3);
        helpButton.setText(R.string.help_button_label);
        helpButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_009_support, 0, 0);
        helpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                GBUtilities.getInstance().showStatus(getActivity(), R.string.help_button_label);

                ((GBActivity) getActivity()).switchToTopSupportScreen();

            }
        });

        //ESC and Enter are disabled on the Help Screen


    }







}
