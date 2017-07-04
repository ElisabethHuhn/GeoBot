package com.asc.msigeosystems.geobot;

import android.content.Intent;
import android.net.Uri;
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
public class GBTopSkyplotsFragment extends Fragment {

    public GBTopSkyplotsFragment() {
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
        ((GBActivity) getActivity()).setSubtitle(R.string.subtitle_skyplots);
    }



    private void wireWidgets(View v){
        //Tell the user which project is open
        TextView screenLabel = (TextView) v.findViewById(R.id.matrix_screen_label);
        screenLabel.setText(GBUtilities.getInstance().
                                                getOpenProjectIDMessage((GBActivity)getActivity()));
        int color = ContextCompat.getColor(getActivity(), R.color.colorWhite);
        screenLabel.setBackgroundColor(color);



        //Skyplots Button
        Button skyplotsButton = (Button) v.findViewById(R.id.row1Button1);
        skyplotsButton.setText(R.string.skyplot_plots_button_label);
        //skyplotsButton.setText(R.string.skyplot_ygps);
        skyplotsButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_611_skyplots, 0, 0);
        skyplotsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){

                GBUtilities.getInstance().showStatus(getActivity(), R.string.skyplot_plots_button_label);
                //Intent intent = new Intent(getActivity(), YGPS.class);
                //startActivity(intent);

            }
        });


        //Sat Info Button
        Button satInfoButton = (Button) v.findViewById(R.id.row1Button2);
        satInfoButton.setText(R.string.skyplot_satinfo_button_label);

        satInfoButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_612_qcposition, 0, 0);
        satInfoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                GBUtilities.getInstance().showStatus(getActivity(), R.string.skyplot_satinfo_button_label);
                GBActivity myActivity = (GBActivity) getActivity();
                if (myActivity != null) {
                    myActivity.switchToListSatellitesScreen();
                }


            }
        });


        //QC Position Button
        Button qcPositionButton = (Button) v.findViewById(R.id.row1Button3);
        qcPositionButton.setText(R.string.skyplot_qc_position_button_label);
        qcPositionButton.setBackgroundResource(R.color.colorGray);
        //the order of images here is left, top, right, bottom
        qcPositionButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_613_qcpositions, 0, 0);
        qcPositionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GBUtilities.getInstance().showStatus(getActivity(), R.string.skyplot_qc_position_button_label);
            }
        });




        //Accelerometer Button
        Button accelerometerButton = (Button) v.findViewById(R.id.row2Button1);
        accelerometerButton.setText(R.string.skyplot_accelerometer_button_label);
        accelerometerButton.setBackgroundResource(R.color.colorGray);
        //the order of images here is left, top, right, bottom
        accelerometerButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_614_accelerometer, 0, 0);
        accelerometerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GBUtilities.getInstance().showStatus(getActivity(), R.string.skyplot_accelerometer_button_label);


            }
        });



        //Compass Button
        Button compassButton = (Button) v.findViewById(R.id.row2Button2);
        compassButton.setText(R.string.skyplot_compass_button_label);
        compassButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_615_compass, 0, 0);
        compassButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){

                GBUtilities.getInstance().showStatus(getActivity(), R.string.skyplot_compass_button_label);

                ((GBActivity)getActivity()).switchToCompassScreen();


            }
        });

        //Geocashing Button
        Button geocashingButton = (Button) v.findViewById(R.id.row2Button3);
        geocashingButton.setText(R.string.skyplot_geocasching_button_label);
        geocashingButton.setBackgroundResource(R.color.colorGray);
        geocashingButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_616_geocaching, 0, 0);
        geocashingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){

                GBUtilities.getInstance().showStatus(getActivity(), R.string.skyplot_geocasching_button_label);

            }
        });


        //Waypoints Button
        Button waypointsButton = (Button) v.findViewById(R.id.row3Button1);
        waypointsButton.setText(R.string.skyplot_waypoints_button_label);
        waypointsButton.setBackgroundResource(R.color.colorGray);
        waypointsButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_617_waypoints, 0, 0);
        waypointsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){

                GBUtilities.getInstance().showStatus(getActivity(), R.string.skyplot_waypoints_button_label);

            }
        });

        //Navigate Button
        Button navigateButton = (Button) v.findViewById(R.id.row3Button2);
        navigateButton.setText(R.string.skyplot_navigate_button_label);
        navigateButton.setBackgroundResource(R.color.colorGray);
        navigateButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_618_navigate, 0, 0);
        navigateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){

                GBUtilities.getInstance().showStatus(getActivity(), R.string.skyplot_navigate_button_label);

            }
        });

        //NOAA Button
        Button noaaButton = (Button) v.findViewById(R.id.row3Button3);
        noaaButton.setText(R.string.skyplot_noaa_button_label);
        noaaButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_619_swpc, 0, 0);
        noaaButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){

                GBUtilities.getInstance().showStatus(getActivity(), R.string.skyplot_noaa_button_label);

                String url = "http://www.swpc.noaa.gov/";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);

            }
        });


    }
}


