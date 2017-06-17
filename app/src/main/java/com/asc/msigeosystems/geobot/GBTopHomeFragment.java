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
 * A placeholder fragment containing a simple view.
 */
public class GBTopHomeFragment extends Fragment {


    public GBTopHomeFragment() {
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
        TextView screenLabel = (TextView) v.findViewById(R.id.matrix_screen_label);

        screenLabel.setText(GBUtilities.getInstance().getOpenProjectIDMessage(getActivity()));
        int color = ContextCompat.getColor(getActivity(), R.color.colorWhite);
        screenLabel.setBackgroundColor(color);



        //projects Button
        Button projectButton = (Button) v.findViewById(R.id.row1Button1);
        projectButton.setText(R.string.projects_button_label);
        //the order of images here is left, top, right, bottom
        projectButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_001_folders, 0, 0);
        projectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ((GBActivity) getActivity()).switchToTopProjectScreen();


            }
        });

        //collect Button
        Button collectButton = (Button) v.findViewById(R.id.row1Button2);
        collectButton.setText(R.string.collect_button_label);
        //the order of images here is left, top, right, bottom
        collectButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_002_collect, 0, 0);
        collectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ((GBActivity) getActivity()).switchToTopCollectScreen();

            }
        });

        //stakeout Button
        Button stakeoutButton = (Button) v.findViewById(R.id.row1Button3);
        stakeoutButton.setText(R.string.stakeout_button_label);
        //the order of images here is left, top, right, bottom
        stakeoutButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_003_stakeout, 0, 0);
        stakeoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ((GBActivity) getActivity()).switchToTopStakeoutScreen();

            }
        });


        //cogo Button
        Button cogoButton = (Button) v.findViewById(R.id.row2Button1);
        cogoButton.setText(R.string.cogo_button_label);
        //the order of images here is left, top, right, bottom
        cogoButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_004_cogo, 0, 0);
        cogoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ((GBActivity) getActivity()).switchToTopCogoScreen();

            }
        });

        //maps Button
        Button mapsButton = (Button) v.findViewById(R.id.row2Button2);
        mapsButton.setText(R.string.maps_button_label);
        //the order of images here is left, top, right, bottom
        mapsButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_005_maps, 0, 0);
        mapsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GBUtilities.getInstance().showStatus(getActivity(), R.string.maps_button_label);

                ((GBActivity) getActivity()).switchToTopMapsScreen();


            }
        });


        //skyplot Button
        Button skyplotButton = (Button) v.findViewById(R.id.row2Button3);
        skyplotButton.setText(R.string.skyplot_button_label);
        skyplotButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_006_skyplots, 0, 0);
        skyplotButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ((GBActivity) getActivity()).switchToTopSkyplotScreen();


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
