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
 * The Maps Fragment is the top level selection UI for map features
 *
 * when the user is 
 * Created by Elisabeth Huhn on 5/132016.
 */
public class GBTopMapsFragment extends Fragment {


    public GBTopMapsFragment() {
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
        ((GBActivity) getActivity()).setSubtitle(R.string.subtitle_maps);
    }

    private void wireWidgets(View v){
        //Tell the user which project is open
        TextView mScreenLabel = (TextView) v.findViewById(R.id.matrix_screen_label);
        mScreenLabel.setText(GBUtilities.getInstance().
                                                getOpenProjectIDMessage((GBActivity)getActivity()));
        int color = ContextCompat.getColor(getActivity(), R.color.colorWhite);
        mScreenLabel.setBackgroundColor(color);


        //Google Maps Button
        Button googleMapsButton = (Button) v.findViewById(R.id.row1Button1);
        googleMapsButton.setText(R.string.google_maps_button_label);
        googleMapsButton.setBackgroundResource(R.color.colorGray);
        //the order of images here is left, top, right, bottom
        googleMapsButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_511_googlemaps, 0, 0);
        googleMapsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GBUtilities.getInstance().showStatus(getActivity(), R.string.google_maps_button_label);
            }
        });


        //Google Earth Button
        Button earthButton = (Button) v.findViewById(R.id.row1Button2);
        earthButton.setText(R.string.earth_button_label);
        earthButton.setBackgroundResource(R.color.colorGray);
        earthButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_512_googleearth, 0, 0);
        earthButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                GBUtilities.getInstance().showStatus(getActivity(), R.string.earth_button_label);

            }
        });

        //Custom Maps Button
        Button customMapsButton = (Button) v.findViewById(R.id.row1Button3);
        customMapsButton.setText(R.string.custom_maps_button_label);
        customMapsButton.setBackgroundResource(R.color.colorGray);
        //the order of images here is left, top, right, bottom
        customMapsButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_513_custommaps, 0, 0);
        customMapsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GBUtilities.getInstance().showStatus(getActivity(), R.string.custom_maps_button_label);

            }
        });


        //Layers Button
        Button layersButton = (Button) v.findViewById(R.id.row2Button1);
        layersButton.setText(R.string.layers_button_label);
        layersButton.setBackgroundResource(R.color.colorGray);
        layersButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_514_maplayers, 0, 0);
        layersButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                GBUtilities.getInstance().showStatus(getActivity(),
                        //R.string.cogo_map_check_button_label,
                        R.string.layers_button_label);

            }
        });

        //Markers Button
        Button markersButton = (Button) v.findViewById(R.id.row2Button2);
        markersButton.setText(R.string.markers_button_label);
        markersButton.setBackgroundResource(R.color.colorGray);
        markersButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_515_mapmarkers, 0, 0);
        markersButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                GBUtilities.getInstance().showStatus(getActivity(), R.string.markers_button_label);

            }
        });
        //Polylines Button
        Button polylinesButton = (Button) v.findViewById(R.id.row2Button3);
        polylinesButton.setText(R.string.polylines_button_label);
        polylinesButton.setBackgroundResource(R.color.colorGray);
        polylinesButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_516_polylines, 0, 0);
        polylinesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                GBUtilities.getInstance().showStatus(getActivity(), R.string.polylines_button_label);

            }
        });

        //tracks Button
        Button tracksButton = (Button) v.findViewById(R.id.row3Button1);
        tracksButton.setText(R.string.track_button_label);
        tracksButton.setBackgroundResource(R.color.colorGray);
        tracksButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_517_maptracks, 0, 0);
        tracksButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                GBUtilities.getInstance().showStatus(getActivity(), R.string.track_button_label);

            }
        });

        //Background Button
        Button backgroundButton = (Button) v.findViewById(R.id.row3Button2);
        backgroundButton.setText(R.string.background_button_label);
        backgroundButton.setBackgroundResource(R.color.colorGray);
        backgroundButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_518_backgroundmaps, 0, 0);
        backgroundButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                GBUtilities.getInstance().showStatus(getActivity(), R.string.background_button_label);

            }
        });


        //Save Button
        Button saveProfileButton = (Button) v.findViewById(R.id.row3Button3);
        saveProfileButton.setText(R.string.save_profile_button_label);
        saveProfileButton.setBackgroundResource(R.color.colorGray);
        saveProfileButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_519_saveviews, 0, 0);
        saveProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                GBUtilities.getInstance().showStatus(getActivity(), R.string.save_profile_button_label);


            }
        });


    }
}


