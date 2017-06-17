package com.asc.msigeosystems.geobot;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * The Support Fragment is the top level selection UI
 * for support features
 *
 * Created by elisabethhuhn on 5/1/2016.
 */
public class GBTopSupportFragment extends Fragment {


    public GBTopSupportFragment() {
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
        ((GBActivity) getActivity()).setSubtitle(R.string.subtitle_support);
    }

    private void wireWidgets(View v){

        Button manualsButton = (Button) v.findViewById(R.id.row1Button1);
        manualsButton.setText(R.string.support_manuals_button_label);
        manualsButton.setBackgroundResource(R.color.colorGray);
        //the order of images here is left, top, right, bottom
        manualsButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_911_manuals, 0, 0);
        manualsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                GBUtilities.getInstance().showStatus(getActivity(),
                        R.string.support_manuals_button_label);

            }
        });





        Button mFaqButton = (Button) v.findViewById(R.id.row1Button2);
        mFaqButton.setText(R.string.support_faq_button_label);
        mFaqButton.setBackgroundResource(R.color.colorGray);
        //the order of images here is left, top, right, bottom
        mFaqButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_912_faqs, 0, 0);
        mFaqButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                GBUtilities.getInstance().showStatus(getActivity(),
                        R.string.support_faq_button_label);

            }
        });


        //
        Button mVideosButton = (Button) v.findViewById(R.id.row1Button3);
        mVideosButton.setText(R.string.support_videos_button_label);
        mVideosButton.setBackgroundResource(R.color.colorGray);
        mVideosButton.setEnabled(true);
        mVideosButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_913_videos, 0, 0);
        mVideosButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){

                GBUtilities.getInstance().showStatus(getActivity(),
                        R.string.support_videos_button_label);

            }
        });


        Button modulesButton = (Button) v.findViewById(R.id.row2Button1);
        modulesButton.setText(R.string.support_modules_button_label);
        modulesButton.setBackgroundResource(R.color.colorGray);
        modulesButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_914_modules, 0, 0);
        modulesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                GBUtilities.getInstance().showStatus(getActivity(),
                        R.string.support_modules_button_label);

            }
        });


        Button upgradesButton = (Button) v.findViewById(R.id.row2Button2);
        upgradesButton.setText(R.string.support_upgrades_button_label);
        upgradesButton.setBackgroundResource(R.color.colorGray);
        upgradesButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_915_upgrades, 0, 0);
        upgradesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){

                GBUtilities.getInstance().showStatus(getActivity(),
                        R.string.support_upgrades_button_label);

            }
        });

        //
        Button registrationButton = (Button) v.findViewById(R.id.row2Button3);
        registrationButton.setText(R.string.support_registration_button_label);
        registrationButton.setBackgroundResource(R.color.colorGray);
        registrationButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_916_registration, 0, 0);
        registrationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){

                GBUtilities.getInstance().showStatus(getActivity(),
                        R.string.support_registration_button_label);

            }
        });

        //
        Button supportButton = (Button) v.findViewById(R.id.row3Button1);
        supportButton.setText(R.string.support_support_button_label);
        supportButton.setBackgroundResource(R.color.colorGray);
        supportButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_917_support, 0, 0);
        supportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){

                GBUtilities.getInstance().showStatus(getActivity(),
                        R.string.support_support_button_label);

            }
        });

        //
        Button feedbackButton = (Button) v.findViewById(R.id.row3Button2);
        feedbackButton.setText(R.string.support_feedback_button_label);
        feedbackButton.setBackgroundResource(R.color.colorGray);
        feedbackButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_818_localizations, 0, 0);
        feedbackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){

                GBUtilities.getInstance().showStatus(getActivity(),
                        R.string.support_feedback_button_label);

            }
        });

        //
        Button aboutUsButton = (Button) v.findViewById(R.id.row3Button3);
        aboutUsButton.setText(R.string.support_aboutus_button_label);
        aboutUsButton.setBackgroundResource(R.color.colorGray);
        aboutUsButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_919_aboutus, 0, 0);
        aboutUsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){

                GBUtilities.getInstance().showStatus(getActivity(),
                        R.string.support_aboutus_button_label);

            }
        });

    }
}


