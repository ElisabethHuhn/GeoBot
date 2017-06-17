package com.asc.msigeosystems.geobot;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * The Project Fragment is the UI
 * when the user is creating / making changes to the project definition
 *
 * Created by Elisabeth Huhn on 4/13/2016.
 */
public class GBTopProjectFragment extends Fragment {

    /* *********************************************************************/
    /* ********   Member Variables  ****************************************/
    /* *********************************************************************/




    /* *********************************************************************/
    /* ********      Constructor    ****************************************/
    /* *********************************************************************/

    public GBTopProjectFragment() {
        //for now, we don't need to initialize anything when the fragment
        //  is first created
    }


    /* *********************************************************************/
    /* ********   LifeCycle Methods  ***************************************/
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


    @Override
    public void onResume(){
        super.onResume();
        setSubtitle();
    }

    private void setSubtitle() {
        ((GBActivity) getActivity()).setSubtitle(R.string.subtitle_projects);
    }



    private void wireWidgets(View v){
        //Tell the user which project is open
        TextView screenLabel = (TextView) v.findViewById(R.id.matrix_screen_label);
        screenLabel.setText(GBUtilities.getInstance().getOpenProjectIDMessage(getActivity()));
        int color = ContextCompat.getColor(getActivity(), R.color.colorWhite);
        screenLabel.setBackgroundColor(color);



        //Create Button
        Button createButton = (Button) v.findViewById(R.id.row1Button1);
        createButton.setText(R.string.create_button_label);
        //the order of images here is left, top, right, bottom
        createButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_111_createfolder, 0, 0);
        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Switch the fragment to the collect with maps fragment.
                // But the switching happens on the container Activity
                ((GBActivity)getActivity()).switchToProjectCreateScreen();


            }
        });

        //open Button
        Button openButton = (Button) v.findViewById(R.id.row1Button2);
        openButton.setText(R.string.open_button_label);
        //the order of images here is left, top, right, bottom
        openButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_112_openfolder, 0, 0);
        openButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((GBActivity)getActivity()).switchToProjectOpenScreen();


            }
        });


        //copy Button
        Button copyButton = (Button) v.findViewById(R.id.row1Button3);
        copyButton.setText(R.string.copy_button_label);
        copyButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_113_copyfolder, 0, 0);
        copyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                ((GBActivity)getActivity()).switchToProjectCopyScreen();

            }
        });

        //Edit Button
        Button editButton = (Button) v.findViewById(R.id.row2Button1);
        editButton.setEnabled(true);
        editButton.setText(R.string.edit_button_label);
        editButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_114_editfolder, 0, 0);
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                GBUtilities.getInstance().showStatus(getActivity(), R.string.edit_button_label);

                ((GBActivity)getActivity()).switchToProjectEditScreen();


            }
        });

        //Delete Button
        Button deleteButton = (Button) v.findViewById(R.id.row2Button2);
        deleteButton.setText(R.string.delete_button_label);
        deleteButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_115_deletefolder, 0, 0);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){

                ((GBActivity)getActivity()).switchToProjectDeleteScreen();
            }
        });


        //Control Button
        Button controlButton = (Button) v.findViewById(R.id.row2Button3);
        controlButton.setText(R.string.control_button_label);
        controlButton.setBackgroundResource(R.color.colorGray);
        controlButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_116_controlfile, 0, 0);
        controlButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                GBUtilities.getInstance().showStatus(getActivity(), R.string.control_button_label);

            }
        });


        //List Points Button
        Button listPointsButton = (Button) v.findViewById(R.id.row3Button1);
        listPointsButton.setText(R.string.list_points_button_label);
        listPointsButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_117_pointsfile, 0, 0);
        listPointsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){

                GBActivity myActivity = (GBActivity) getActivity();
                GBProject openProject = GBUtilities.getInstance().getOpenProject();
                if (openProject != null){
                    myActivity.switchToListPointsScreen(openProject,
                            new GBPath(GBPath.sEditTag));
                }

                GBUtilities.getInstance().showStatus(getActivity(),
                                                        R.string.project_not_open_to_list_points);
            }
        });

        //Feature Codes Button
        Button featureCodesButton = (Button) v.findViewById(R.id.row3Button2);
        featureCodesButton.setText(R.string.feature_codes_button_label);
        featureCodesButton.setBackgroundResource(R.color.colorGray);
        featureCodesButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_118_fcfile, 0, 0);
        featureCodesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){

                GBUtilities.getInstance().showStatus(getActivity(), R.string.feature_codes_button_label);

            }
        });

        //Exchange Button
        Button exchangeButton = (Button) v.findViewById(R.id.row3Button3);
        exchangeButton.setText(R.string.exchange_button_label);
        exchangeButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_119_exchangefolder, 0, 0);
        exchangeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){

                //GBUtilities.getInstance().showStatus(getActivity(),R.string.exchange_button_label);

                onExchange();

            }
        });


    }

    private void onExchange(){
        GBProject openProject = GBUtilities.getInstance().getOpenProject();
        if (openProject == null){
            GBUtilities.getInstance().showStatus(getActivity(),R.string.no_project_open);
            return;
        }
        ArrayList pointsList = openProject.getPoints();
        if (pointsList == null){
            GBUtilities.getInstance().showStatus(getActivity(),R.string.no_points_in_project);
            return;
        }

        int last = pointsList.size();
        if (last == 0){
            GBUtilities.getInstance().showStatus(getActivity(),R.string.no_points_in_project);
            return;
        }

        String subject = getString(R.string.exchange_subject) + " " + openProject.getProjectName();
        String message = getString(R.string.email_message_prologue_1) +
                         " "+ openProject.getProjectName() + " "+
                         getString(R.string.email_message_prologue_2)+
                         System.getProperty("line.separator")+
                         System.getProperty("line.separator")+
                         System.getProperty("line.separator");

        message = message + openProject.convertToCDF() ;
        message = message +
                    System.getProperty("line.separator")+
                    System.getProperty("line.separator")+
                    System.getProperty("line.separator");

        GBPoint point;

        // TODO: 12/23/2016 Do we need to include project settings? for now, no

        for (int i = 0; i < last; i++) {
            point = (GBPoint) pointsList.get(i);
            message = message + point.convertToCDF();
        }

        Intent emailApp = new Intent(Intent.ACTION_SEND);
        emailApp.putExtra(Intent.EXTRA_SUBJECT, subject);
        emailApp.putExtra(Intent.EXTRA_TEXT, message);
        emailApp.setType("message/rfc822");
        startActivity(Intent.createChooser(emailApp, "Send Email Via"));
    }
}


