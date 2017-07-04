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
 * Currently, this has all the utilities code for development, but not the final product
 *
 * Created by Elisabeth Huhn on 11/19/2016.
 */
public class GBTopSettingsGeneralFragment extends Fragment {

    /* *********************************************************************/
    /* ********   Member Variables  ****************************************/
    /* *********************************************************************/


    /* *********************************************************************/
    /* ********      Constructor    ****************************************/
    /* *********************************************************************/


    public GBTopSettingsGeneralFragment() {
        //for now, we don't need to initialize anything when the fragment
        //  is first created
    }

    /* *********************************************************************/
    /* ********   LifeCycle Methods ****************************************/
    /* *********************************************************************/

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_top_matrix, container, false);


        //Wire up the UI widgets so they can handle events later
        wireWidgets(v);



        return v;
    }

    public void onResume(){
        super.onResume();
        setSubtitle();
    }

    private void setSubtitle(){
        ((GBActivity)getActivity()).setSubtitle(R.string.subtitle_general_settings);
    }

    private void wireWidgets(View v){
        //Tell the user which project is open
        TextView screenLabel = (TextView) v.findViewById(R.id.matrix_screen_label);
        screenLabel.setText(GBUtilities.getInstance().
                                                getOpenProjectIDMessage((GBActivity)getActivity()));
        int color = ContextCompat.getColor(getActivity(), R.color.colorWhite);
        screenLabel.setBackgroundColor(color);




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




        Button m4Button = (Button) v.findViewById(R.id.row2Button1);
        m4Button.setEnabled(false);
        m4Button.setText("");

        m4Button.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_817_generalsettings, 0, 0);
        m4Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                GBUtilities.getInstance().showStatus(getActivity(), "");

                //Intent intent = new Intent(getActivity(), GBGPSActivity.class);
                //startActivity(intent);

            }
        });


        Button gpsFromNmeaButton = (Button) v.findViewById(R.id.row2Button2);
        gpsFromNmeaButton.setText(R.string.skyplot_gps_from_nmea);
        gpsFromNmeaButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_817_generalsettings, 0, 0);
        gpsFromNmeaButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){


                ((GBActivity)getActivity()).switchToGpsNmeaScreen();

            }
        });


        Button listSattelitesButton = (Button) v.findViewById(R.id.row2Button3);
        listSattelitesButton.setText(R.string.skyplot_list_satellites);
        listSattelitesButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_817_generalsettings, 0, 0);
        listSattelitesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){

                ((GBActivity) getActivity()).switchToListSatellitesScreen();

            }
        });




        Button m7Button = (Button) v.findViewById(R.id.row3Button1);
        m7Button.setText("Prism4D Home");
        m7Button.setBackgroundResource(R.color.colorWhite);
        m7Button.setFocusable(false);
        m7Button.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_817_generalsettings, 0, 0);
        m7Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                ((GBActivity)getActivity()).switchToPrism4DHomeScreen();

            }
        });


        Button compassFragmentButton = (Button) v.findViewById(R.id.row3Button2);
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


        Button listNmeaSentencesButton = (Button) v.findViewById(R.id.row3Button3);
        listNmeaSentencesButton.setText(R.string.skyplot_nmea_sentence_label);
        listNmeaSentencesButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_817_generalsettings, 0, 0);
        listNmeaSentencesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                ((GBActivity)getActivity()).switchToListNmeaScreen();


            }
        });




    }
}


