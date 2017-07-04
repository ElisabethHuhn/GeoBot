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
 * The Settings Fragment is the top level selection UI
 * for Global Settings for the app
 * Note that each of the other screens has the potential for
 * local settings which are not covered here
 * Created by Elisabeth Huhn on 5/1/2016.
 */
public class GBTopSettingsFragment extends Fragment {


    public GBTopSettingsFragment() {
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
        ((GBActivity) getActivity()).setSubtitle(R.string.subtitle_settings);
    }

    private void wireWidgets(View v){
        //Tell the user which project is open
        TextView screenLabel = (TextView) v.findViewById(R.id.matrix_screen_label);
        screenLabel.setText(GBUtilities.getInstance().
                                                getOpenProjectIDMessage((GBActivity)getActivity()));
        int color = ContextCompat.getColor(getActivity(), R.color.colorWhite);
        screenLabel.setBackgroundColor(color);



        //Units Button for global settings
        Button unitsButton = (Button) v.findViewById(R.id.row1Button1);
        unitsButton.setText(R.string.setting_units_button_label);
        unitsButton.setBackgroundResource(R.color.colorGray);
        //the order of images here is left, top, right, bottom
        unitsButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_811_units, 0, 0);
        unitsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                GBUtilities.getInstance().showStatus(getActivity(), R.string.setting_units_button_label);
                /*
                GBActivity myActivity = (GBActivity) getActivity();
                if (myActivity != null) {
                    myActivity.switchToSettingsGlobalScreen();
                }
                */

            }
        });




        //Formats Button
        Button formatsButton = (Button) v.findViewById(R.id.row1Button2);
        formatsButton.setText(R.string.setting_formats_button_label);
        formatsButton.setBackgroundResource(R.color.colorGray);
        //the order of images here is left, top, right, bottom
        formatsButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_812_formats, 0, 0);
        formatsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                GBUtilities.getInstance().showStatus(getActivity(), R.string.setting_formats_button_label);
                /*
                GBActivity myActivity = (GBActivity) getActivity();
                if (myActivity != null) {
                    myActivity.switchToSettingsProjectDefaultsScreen();
                }
                */

            }
        });


        //Tolerances
        Button tolerancesButton = (Button) v.findViewById(R.id.row1Button3);
        tolerancesButton.setText(R.string.setting_tolerances_button_label);
        tolerancesButton.setBackgroundResource(R.color.colorGray);
        tolerancesButton.setEnabled(true);
        tolerancesButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_813_tolerances, 0, 0);
        tolerancesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){

                GBUtilities.getInstance().showStatus(getActivity(), R.string.setting_tolerances_button_label);

            }
        });

        //Datums
        Button datumsButton = (Button) v.findViewById(R.id.row2Button1);
        datumsButton.setText(R.string.setting_datums_button_label);
        datumsButton.setBackgroundResource(R.color.colorGray);
        datumsButton.setEnabled(true);
        datumsButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_814_datums, 0, 0);
        datumsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                GBUtilities.getInstance().showStatus(getActivity(), R.string.setting_datums_button_label);

            }
        });

        //Projections
        Button projectionsButton = (Button) v.findViewById(R.id.row2Button2);
        projectionsButton.setText(R.string.setting_projections_button_label);
        projectionsButton.setBackgroundResource(R.color.colorGray);
        projectionsButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_815_projections, 0, 0);
        projectionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){

                GBUtilities.getInstance().showStatus(getActivity(), R.string.setting_projections_button_label);

            }
        });

        //Geod
        Button geoidButton = (Button) v.findViewById(R.id.row2Button3);
        geoidButton.setText(R.string.setting_geoid_models_button_label);
        geoidButton.setBackgroundResource(R.color.colorGray);
        geoidButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_816_geoidmodels, 0, 0);
        geoidButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){

                GBUtilities.getInstance().showStatus(getActivity(), R.string.setting_geoid_models_button_label);

            }
        });

        //General
        Button generalButton = (Button) v.findViewById(R.id.row3Button1);
        generalButton.setText(R.string.setting_general_button_label);

        generalButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_817_generalsettings, 0, 0);
        generalButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){

                GBUtilities.getInstance().showStatus(getActivity(), R.string.setting_general_button_label);
                ((GBActivity)getActivity()).switchToGeneralSettingsScreen();

            }
        });

        //Localizations
        Button localizationsButton = (Button) v.findViewById(R.id.row3Button2);
        localizationsButton.setText(R.string.setting_localizations_button_label);
        localizationsButton.setBackgroundResource(R.color.colorGray);
        localizationsButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_818_localizations, 0, 0);
        localizationsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){

                GBUtilities.getInstance().showStatus(getActivity(), R.string.setting_localizations_button_label);

            }
        });

        //Survey Styles
        Button surveyStylesButton = (Button) v.findViewById(R.id.row3Button3);
        surveyStylesButton.setText(R.string.setting_survey_styles_button_label);
        surveyStylesButton.setBackgroundResource(R.color.colorGray);
        surveyStylesButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_819_surveystyles, 0, 0);
        surveyStylesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){

                GBUtilities.getInstance().showStatus(getActivity(), R.string.setting_survey_styles_button_label);

            }
        });

    }
}


