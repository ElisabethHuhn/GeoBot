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
 * The Configurations Fragment is the top level selection UI
 * for stakeout functions
 * Created by Elisabeth Huhn on 5/1/2016.
 */
public class GBTopConfigurationsFragment extends Fragment {


    public GBTopConfigurationsFragment() {
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
        ((GBActivity) getActivity()).setSubtitle(R.string.subtitle_configurations);
    }

    private void wireWidgets(View v){
        //Tell the user which project is open
        TextView screenLabel = (TextView) v.findViewById(R.id.matrix_screen_label);
        screenLabel.setText(GBUtilities.getInstance().getOpenProjectIDMessage(getActivity()));
        int color = ContextCompat.getColor(getActivity(), R.color.colorWhite);
        screenLabel.setBackgroundColor(color);


        //Equipment Button
        Button equipmentButton = (Button) v.findViewById(R.id.row1Button1);
        equipmentButton.setText(R.string.config_equipment_button_label);
        equipmentButton.setBackgroundResource(R.color.colorGray);
        //the order of images here is left, top, right, bottom
        equipmentButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_711_equipment, 0, 0);
        equipmentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GBUtilities.getInstance().showStatus(getActivity(), R.string.config_equipment_button_label);

            }
        });




        //Communications Button
        Button communicationsButton = (Button) v.findViewById(R.id.row1Button2);
        communicationsButton.setText(R.string.config_communications_button_label);
        //the order of images here is left, top, right, bottom
        communicationsButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_712_communications, 0, 0);
        communicationsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GBUtilities.getInstance().showStatus(getActivity(), R.string.config_communications_button_label);
                ((GBActivity)getActivity()).switchToListNmeaScreen();

            }
        });


        //Corrections Button
        Button correctionsButton = (Button) v.findViewById(R.id.row1Button3);
        correctionsButton.setText(R.string.config_corrections_label);
        correctionsButton.setBackgroundResource(R.color.colorGray);
        correctionsButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_713_corrections, 0, 0);
        correctionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                GBUtilities.getInstance().showStatus(getActivity(), R.string.config_corrections_label);

            }
        });

        //Peripherals Button
        Button peripheralsButton = (Button) v.findViewById(R.id.row2Button1);
        peripheralsButton.setText(R.string.config_peripherals_button_label);
        peripheralsButton.setBackgroundResource(R.color.colorGray);
        peripheralsButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_714_peripherals, 0, 0);
        peripheralsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                GBUtilities.getInstance().showStatus(getActivity(), R.string.config_peripherals_button_label);

            }
        });

        //Calibrations Button
        Button calibrationsButton = (Button) v.findViewById(R.id.row2Button2);
        calibrationsButton.setText(R.string.config_calibrations_button_label);
        calibrationsButton.setBackgroundResource(R.color.colorGray);
        calibrationsButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_715_calibrations, 0, 0);
        calibrationsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){

                GBUtilities.getInstance().showStatus(getActivity(), R.string.config_calibrations_button_label);

            }
        });

        //Log Raw Data Button
        Button logRawDataButton = (Button) v.findViewById(R.id.row2Button3);
        logRawDataButton.setText(R.string.config_log_raw_button_label);
        logRawDataButton.setBackgroundResource(R.color.colorGray);
        logRawDataButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_716_rawdata, 0, 0);
        logRawDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){

                GBUtilities.getInstance().showStatus(getActivity(), R.string.config_log_raw_button_label);

            }
        });

            //General Button
        Button generalButton = (Button) v.findViewById(R.id.row3Button1);
        generalButton.setText(R.string.config_general_button_label);
        generalButton.setBackgroundResource(R.color.colorGray);
        generalButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_717_generalconfig, 0, 0);
        generalButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){

                GBUtilities.getInstance().showStatus(getActivity(), R.string.config_general_button_label);

            }
        });

        //Utilities Button
        Button utilitiesButton = (Button) v.findViewById(R.id.row3Button2);
        utilitiesButton.setText(R.string.config_utilities_button_label);
        utilitiesButton.setBackgroundResource(R.color.colorGray);
        utilitiesButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_718_utilities, 0, 0);
        utilitiesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){

                GBUtilities.getInstance().showStatus(getActivity(), R.string.config_utilities_button_label);

            }
        });

        //Save Button
        Button saveProfileButton = (Button) v.findViewById(R.id.row3Button3);
        saveProfileButton.setText(R.string.save_profile_button_label);
        saveProfileButton.setBackgroundResource(R.color.colorGray);
        saveProfileButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_719_saveprofile, 0, 0);
        saveProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){

                GBUtilities.getInstance().showStatus(getActivity(), R.string.save_profile_button_label);

            }
        });

    }
}


