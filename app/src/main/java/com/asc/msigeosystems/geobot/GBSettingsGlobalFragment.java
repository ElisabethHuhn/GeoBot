package com.asc.msigeosystems.geobot;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

/**
 * The Settings Fragment is the top level selection UI
 * for Global Settings for the app
 * Note that each of the other screens has the potential for
 * local settings which are not covered here
 * Created by Elisabeth Huhn on 5/1/2016.
 */
public class GBSettingsGlobalFragment extends Fragment {

    /**
     * Create variables for all the widgets
     *  although in the mockup, most will be statically defined in the xml
     */






    public GBSettingsGlobalFragment() {
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
        ((GBActivity)getActivity()).setSubtitle(R.string.subtitle_global_settings);
    }

    private void wireWidgets(View v){
        //Units Button
        Button unitsButton = (Button) v.findViewById(R.id.row1Button1);
        unitsButton.setText(R.string.setting_units_button_label);
        //the order of images here is left, top, right, bottom
        unitsButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_008_settings, 0, 0);
        unitsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //for now, just put up a toast that the button was pressed
                Toast.makeText(getActivity(),
                        R.string.setting_units_button_label,
                        Toast.LENGTH_SHORT).show();

            }
        });




        //Formats Button
        Button formatsButton = (Button) v.findViewById(R.id.row1Button2);
        formatsButton.setText(R.string.setting_formats_button_label);
        //the order of images here is left, top, right, bottom
        formatsButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_008_settings, 0, 0);
        formatsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //for now, just put up a toast that the button was pressed
                Toast.makeText(getActivity(),
                        R.string.setting_formats_button_label,
                        Toast.LENGTH_SHORT).show();

            }
        });


        //Tolerances Button
        Button tolerancesButton = (Button) v.findViewById(R.id.row3Button3);
        tolerancesButton.setText(R.string.setting_tolerances_button_label);
        tolerancesButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_008_settings, 0, 0);
        tolerancesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){

                ///for now, just put up a toast that the button was pressed
                Toast.makeText(getActivity(),
                        R.string.setting_tolerances_button_label,
                        Toast.LENGTH_SHORT).show();

            }
        });

        //Datums Button
        Button datumsButton = (Button) v.findViewById(R.id.row1Button3);
        datumsButton.setText(R.string.setting_datums_button_label);
        datumsButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_008_settings, 0, 0);
        datumsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                //for now, just put up a toast that the button was pressed
                Toast.makeText(getActivity(),
                        R.string.setting_datums_button_label,
                        Toast.LENGTH_SHORT).show();

            }
        });

        //Projections Button
        Button projectionsButton = (Button) v.findViewById(R.id.row2Button1);
        projectionsButton.setText(R.string.setting_projections_button_label);
        projectionsButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_008_settings, 0, 0);
        projectionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){

                //for now, just put up a toast that the button was pressed
                Toast.makeText(getActivity(),
                        R.string.setting_projections_button_label,
                        Toast.LENGTH_SHORT).show();

            }
        });

        //Geoid Models Button
        Button geoidModelButton = (Button) v.findViewById(R.id.row2Button2);
        geoidModelButton.setText(R.string.setting_geoid_models_button_label);
        geoidModelButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_008_settings, 0, 0);
        geoidModelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){

                ///for now, just put up a toast that the button was pressed
                Toast.makeText(getActivity(),
                        R.string.setting_geoid_models_button_label,
                        Toast.LENGTH_SHORT).show();

            }
        });

        //General Button
        Button generalButton = (Button) v.findViewById(R.id.row2Button3);
        generalButton.setText(R.string.setting_general_button_label);
        generalButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_008_settings, 0, 0);
        generalButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){

                ///for now, just put up a toast that the button was pressed
                Toast.makeText(getActivity(),
                        R.string.setting_general_button_label,
                        Toast.LENGTH_SHORT).show();

            }
        });

        //Localizations Button
        Button localizationsButton = (Button) v.findViewById(R.id.row3Button1);
        localizationsButton.setText(R.string.setting_localizations_button_label);
        localizationsButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_008_settings, 0, 0);
        localizationsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){

                ///for now, just put up a toast that the button was pressed
                Toast.makeText(getActivity(),
                        R.string.setting_localizations_button_label,
                        Toast.LENGTH_SHORT).show();

            }
        });

        //Survey Styles Button
        Button surveyStylesButton = (Button) v.findViewById(R.id.row3Button2);
        surveyStylesButton.setText(R.string.setting_survey_styles_button_label);
        surveyStylesButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_008_settings, 0, 0);
        surveyStylesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){

                ///for now, just put up a toast that the button was pressed
                Toast.makeText(getActivity(),
                        R.string.setting_survey_styles_button_label,
                        Toast.LENGTH_SHORT).show();

            }
        });





        //FOOTER WIDGETS

        //  Esc and Enter buttons are NOT enabled on the collect screen
        //so we can ignore the footer for now


    }

}


