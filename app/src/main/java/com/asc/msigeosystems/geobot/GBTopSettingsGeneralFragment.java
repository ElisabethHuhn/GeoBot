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
        screenLabel.setText(GBUtilities.getInstance().getOpenProjectIDMessage(getActivity()));
        int color = ContextCompat.getColor(getActivity(), R.color.colorWhite);
        screenLabel.setBackgroundColor(color);



        Button coordinateWorkflowButton = (Button) v.findViewById(R.id.row1Button1);
        coordinateWorkflowButton.setText(R.string.cogo_workflow_button_label);
        //the order of images here is left, top, right, bottom
        coordinateWorkflowButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_817_generalsettings, 0, 0);
        coordinateWorkflowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GBActivity myActivity = (GBActivity) getActivity();
                if (myActivity != null){
                    myActivity.switchToCoordWorkflow();
                }


            }
        });


        Button convertCoordinatesButton = (Button) v.findViewById(R.id.row1Button2);
        convertCoordinatesButton.setText(R.string.convert_coordinates);
        //the order of images here is left, top, right, bottom
        convertCoordinatesButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_817_generalsettings, 0, 0);
        convertCoordinatesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GBActivity myActivity = (GBActivity) getActivity();
                if (myActivity != null){
                    myActivity.switchToConvertScreen();
                }


            }
        });



        Button listNmeaSentencesButton = (Button) v.findViewById(R.id.row1Button3);
        listNmeaSentencesButton.setText(R.string.skyplot_nmea_sentence_label);
        listNmeaSentencesButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_817_generalsettings, 0, 0);
        listNmeaSentencesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                ((GBActivity)getActivity()).switchToListNmeaScreen();


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




        Button compassActivityButton = (Button) v.findViewById(R.id.row3Button1);
        compassActivityButton.setText("");
        compassActivityButton.setBackgroundResource(R.color.colorGray);
        compassActivityButton.setFocusable(false);
        compassActivityButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_817_generalsettings, 0, 0);
        compassActivityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){


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


        Button m9Button = (Button) v.findViewById(R.id.row3Button3);
        m9Button.setText("");
        m9Button.setFocusable(false);
        m9Button.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_817_generalsettings, 0, 0);
        m9Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){

                GBUtilities.getInstance().showStatus(getActivity(), "");

            }
        });


    }
}


