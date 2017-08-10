package com.asc.msigeosystems.geobot;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;

/**
 * The UI Settings Fragment
 * These settings deal with how data is shown to the User
 *
 * Created by Elisabeth Huhn on 7/29/17.
 */
public class GBGeneralSettingsFragment extends    Fragment implements CompoundButton.OnCheckedChangeListener{

    private static final String TAG = "GENERAL_SETTINGS_FRAGMENT";





    //**********************************************************************/
    //*********   Member Variables  ****************************************/
    //**********************************************************************/


    //Constructor
    public GBGeneralSettingsFragment() {
        //for now, we don't need to initialize anything when the fragment
        //  is first created with this constructor
    }


    //**********************************************************/
    //****     Lifecycle Methods                         *******/
    //**********************************************************/

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //Inflate the layout for this fragment
        View v = inflater.inflate( R.layout.fragment_general_settings_gb, container,  false);

        wireWidgets(v);

        initializeUI(v);


        return v;
    }

    @Override
    public void onResume(){
        super.onResume();
        //set the title bar subtitle
        setSubtitle();

    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            enableSaveButton();
        } else {
            enableSaveButton();
        }
    }
    //**********************************************************/
    //****     Initialize                                *******/
    //**********************************************************/
    private void wireWidgets(View v){
        Button saveButton = (Button)v.findViewById(R.id.genSettingSaveButton);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSave();
            }
        });

        SwitchCompat autoVmanalSaveSwitch = (SwitchCompat) v.findViewById(R.id.switchAutosave);
        SwitchCompat rmsVstddevSwitch     = (SwitchCompat) v.findViewById(R.id.switchRmsVStdDev);
        SwitchCompat latlngVlnglatSwitch  = (SwitchCompat) v.findViewById(R.id.switchLatLng);
        SwitchCompat neVenSwitch          = (SwitchCompat) v.findViewById(R.id.switchNeEn);
        SwitchCompat locDDvDMSSwitch      = (SwitchCompat) v.findViewById(R.id.switchLocDDvDMS);
        SwitchCompat caDDvDMSSwitch       = (SwitchCompat) v.findViewById(R.id.switchCADDvDMS);
        SwitchCompat hemiIndicatorSwitch  = (SwitchCompat) v.findViewById(R.id.switchHemiDirVpm);

        EditText locPrecision = (EditText) v.findViewById(R.id.locPrecisionInput);
        EditText stdPrecision = (EditText) v.findViewById(R.id.stdDevPrecisionInput);
        EditText sfPrecision  = (EditText) v.findViewById(R.id.sfPrecisionInput);
        EditText caPrecision  = (EditText) v.findViewById(R.id.caPrecisionInput);


        autoVmanalSaveSwitch.setOnCheckedChangeListener(this);
        rmsVstddevSwitch    .setOnCheckedChangeListener(this);
        latlngVlnglatSwitch.setOnCheckedChangeListener(this);
        neVenSwitch.setOnCheckedChangeListener(this);
        locDDvDMSSwitch.setOnCheckedChangeListener(this);

        caDDvDMSSwitch.setOnCheckedChangeListener(this);
        hemiIndicatorSwitch.setOnCheckedChangeListener(this);

        locPrecision.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus)enableSaveButton();
            }
        });
        stdPrecision.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus)enableSaveButton();
            }
        });
        sfPrecision.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus)enableSaveButton();
            }
        });
        caPrecision.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus)enableSaveButton();
            }
        });



        locPrecision.setEnabled(true);
        stdPrecision.setEnabled(true);
        sfPrecision.setEnabled(true);
        caPrecision.setEnabled(true);
     }

    private void initializeUI(View v) {
        GBActivity myActivity = (GBActivity)getActivity();

        SwitchCompat autoVmanalSaveSwitch = (SwitchCompat) v.findViewById(R.id.switchAutosave);
        SwitchCompat rmsVstddevSwitch     = (SwitchCompat) v.findViewById(R.id.switchRmsVStdDev);
        SwitchCompat latlngVlnglatSwitch  = (SwitchCompat) v.findViewById(R.id.switchLatLng);
        SwitchCompat neVenSwitch          = (SwitchCompat) v.findViewById(R.id.switchNeEn);
        SwitchCompat locDDvDMSSwitch      = (SwitchCompat) v.findViewById(R.id.switchLocDDvDMS);
        SwitchCompat caDDvDMSSwitch       = (SwitchCompat) v.findViewById(R.id.switchCADDvDMS);
        SwitchCompat hemiIndicatorSwitch  = (SwitchCompat) v.findViewById(R.id.switchHemiDirVpm);

        autoVmanalSaveSwitch.setChecked(GBGeneralSettings.isAutosave(myActivity));
        rmsVstddevSwitch    .setChecked(GBGeneralSettings.isRms(myActivity));
        latlngVlnglatSwitch .setChecked(GBGeneralSettings.isLatLng(myActivity));
        neVenSwitch         .setChecked(GBGeneralSettings.isNE(myActivity));
        locDDvDMSSwitch     .setChecked(GBGeneralSettings.isLocDD(myActivity));
        caDDvDMSSwitch      .setChecked(GBGeneralSettings.isCADD(myActivity));
        hemiIndicatorSwitch .setChecked(GBGeneralSettings.isDir(myActivity));

        EditText locPrecision = (EditText) v.findViewById(R.id.locPrecisionInput);
        EditText stdPrecision = (EditText) v.findViewById(R.id.stdDevPrecisionInput);
        EditText sfPrecision  = (EditText) v.findViewById(R.id.sfPrecisionInput);
        EditText caPrecision  = (EditText) v.findViewById(R.id.caPrecisionInput);


        locPrecision.setText(String.valueOf(GBGeneralSettings.getLocPrecision(   myActivity)));
        stdPrecision.setText(String.valueOf(GBGeneralSettings.getStdDevPrecision(myActivity)));
        sfPrecision .setText(String.valueOf(GBGeneralSettings.getSfPrecision(    myActivity)));
        caPrecision .setText(String.valueOf(GBGeneralSettings.getCAPrecision(    myActivity)));

        disableSaveButton();
    }

    private void setSubtitle(){

        ((GBActivity) getActivity()).setSubtitle(getString(R.string.subtitle_general_settings));

    }


    private void enableSaveButton(){
        View v = getView();
        if (v == null)return;

        Button saveButton = (Button) v.findViewById(R.id.genSettingSaveButton);
        saveButton.setEnabled(true);
        saveButton.setTextColor(Color.BLACK);
    }

    private void disableSaveButton(){
        View v = getView();
        if (v == null)return;

        Button saveButton = (Button) v.findViewById(R.id.genSettingSaveButton);
        saveButton.setEnabled(false);
        saveButton.setTextColor(Color.GRAY);
    }


    //***********************************/
    //****     Save Button        *******/
    //***********************************/
    private void onSave() {
        View v = getView();
        if (v == null)return;

        SwitchCompat autoVmanalSaveSwitch = (SwitchCompat) v.findViewById(R.id.switchAutosave);
        SwitchCompat rmsVstddevSwitch     = (SwitchCompat) v.findViewById(R.id.switchRmsVStdDev);
        SwitchCompat latlngVlnglatSwitch  = (SwitchCompat) v.findViewById(R.id.switchLatLng);
        SwitchCompat neVenSwitch          = (SwitchCompat) v.findViewById(R.id.switchNeEn);
        SwitchCompat locDDvDMSSwitch      = (SwitchCompat) v.findViewById(R.id.switchLocDDvDMS);
        SwitchCompat caDDvDMSSwitch       = (SwitchCompat) v.findViewById(R.id.switchCADDvDMS);
        SwitchCompat hemiIndicatorSwitch  = (SwitchCompat) v.findViewById(R.id.switchHemiDirVpm);

        EditText locPrecision = (EditText) v.findViewById(R.id.locPrecisionInput);
        EditText stdPrecision = (EditText) v.findViewById(R.id.stdDevPrecisionInput);
        EditText sfPrecision  = (EditText) v.findViewById(R.id.sfPrecisionInput);
        EditText caPrecision  = (EditText) v.findViewById(R.id.caPrecisionInput);


        GBActivity myActivity = (GBActivity)getActivity();

        if (autoVmanalSaveSwitch.isChecked()){
            GBGeneralSettings.setAutosave(myActivity);
        } else {
            GBGeneralSettings.setManualSave(myActivity);
        }

        if (rmsVstddevSwitch.isChecked()){
            GBGeneralSettings.setRms(myActivity);
        } else {
            GBGeneralSettings.setStdDev(myActivity);
        }


        if (latlngVlnglatSwitch.isChecked()){
            GBGeneralSettings.setLatLng(myActivity);
        } else {
            GBGeneralSettings.setLngLat(myActivity);
        }

        if (neVenSwitch.isChecked()){
            GBGeneralSettings.setNE(myActivity);
        } else {
            GBGeneralSettings.setEN(myActivity);
        }


        if (locDDvDMSSwitch.isChecked()){
            GBGeneralSettings.setLocDD(myActivity);
        } else {
            GBGeneralSettings.setLocDMS(myActivity);
        }


        if (caDDvDMSSwitch.isChecked()){
            GBGeneralSettings.setCADD(myActivity);
        } else {
            GBGeneralSettings.setCADMS(myActivity);
        }

        if (hemiIndicatorSwitch.isChecked()){
            GBGeneralSettings.setDir(myActivity);
        } else {
            GBGeneralSettings.setPM(myActivity);
        }

        GBGeneralSettings.setLocPrecision(myActivity,
                                            Integer.valueOf(locPrecision.getText().toString()));
        GBGeneralSettings.setStdDevPrecision(myActivity,
                                            Integer.valueOf(stdPrecision.getText().toString()));
        GBGeneralSettings.setSfPrecision(myActivity,
                                            Integer.valueOf(sfPrecision.getText().toString()));
        GBGeneralSettings.setCAPrecision(myActivity,
                                            Integer.valueOf(caPrecision.getText().toString()));


        disableSaveButton();
    }

}


