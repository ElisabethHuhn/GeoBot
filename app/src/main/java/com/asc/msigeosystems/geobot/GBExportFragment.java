package com.asc.msigeosystems.geobot;

import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.SwitchCompat;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Elisabeth Huhn on 2/4/2017.
 * This is the UI for defining a filter for Exporting A project and its points
 */

public class GBExportFragment extends Fragment {
    private static final String TAG = "ExportFragment";


    private static final int EXPORT_EMAIL         = 0;
    private static final int EXPORT_SMS           = 1;
    private static final int EXPORT_FILE          = 2;
    private static final int EXPORT_GENERAL       = 3;


    //**********************************************/
    /*          UI Widgets                         */
    //**********************************************/

    //**********************************************/
    /*          Static Variables                   */
    //**********************************************/

    private static final String sCDF_FILENAME_TAG  = "cdfFilenameTag" ;
    private static final String sCDF_PATH_TAG      = "cdfPathTag" ;
   // private static final String sCDF_TIMESTAMP_TAG = "cdfTimestampTag" ;

    //**********************************************/
    /*          Member Variables                   */
    //**********************************************/
    private boolean  isUIChanged = false;

    CharSequence mCDFileName ;
    CharSequence mCDFPath ;


    //**********************************************/
    /*          Static Methods                     */
    //**********************************************/



    //**********************************************/
    /*          Constructor                        */
    //**********************************************/
    public GBExportFragment() {
    }

    //**********************************************/
    /*          Lifecycle Methods                  */
    //**********************************************/

    //pull the arguments out of the fragment bundle and store in the member variables
    //In this case, prepopulate the personID this screen refers to
    @Override
    public void onCreate(Bundle savedInstanceState){

        super.onCreate(savedInstanceState);

        //Initialize the DB if necessary

        try {
            GBDatabaseManager.getInstance(getActivity());
        }catch (Exception e) {
            Log.e(TAG,Log.getStackTraceString(e));
        }

    }


    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        if (savedInstanceState != null){
            //the fragment is being restored so restore the person ID
            mCDFileName   = savedInstanceState.getString(sCDF_FILENAME_TAG);
            mCDFPath      = savedInstanceState.getString(sCDF_PATH_TAG);

        }


        //Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_exchange_gb, container, false);

        //Wire up the UI widgets so they can handle events later
        wireWidgets(v);

        initializeUI(v);

        setUIChanged();
        
        return v;
    }


    @Override
    public void onSaveInstanceState(Bundle savedInstanceState){
        // Save custom values into the bundle
        savedInstanceState.putString(sCDF_FILENAME_TAG,   mCDFileName.toString());
        savedInstanceState.putString(sCDF_PATH_TAG,       mCDFPath.toString());


        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }


    @Override
    public void onResume(){
        super.onResume();

        //set the title bar subtitle
        ((GBActivity) getActivity()).setSubtitle(R.string.subtitle_export);

        //Set the FAB invisible
        ((GBActivity) getActivity()).hideFAB();

        //get rid of soft keyboard if it is visible
        GBUtilities utilities = GBUtilities.getInstance();
        utilities.hideSoftKeyboard(getActivity());

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

    }




    //**********************************************/
    /*          Member Methods                     */
    //**********************************************/
    private void   wireWidgets(View v) {
        TextView label;

        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
                //This tells you that text is about to change.
                // Starting at character "start", the next "count" characters
                // will be changed with "after" number of characters

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                //This tells you where the text has changed
                //Starting at character "start", the "before" number of characters
                // has been replaced with "count" number of characters

            }

            @Override
            public void afterTextChanged(Editable editable) {
                //This tells you that somewhere within editable, it's text has changed
                setUIChanged();
            }
        };

        //export
        Button exportButton = (Button) v.findViewById(R.id.exportButton);

        exportButton.setText(R.string.exchange_button_label);
        //the order of images here is left, top, right, bottom
        // exportButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_project, 0, 0);
        exportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                onExport();
            }
        });


        //View Existing Points Button
        final Button projectViewExistingButton = (Button) v.findViewById(R.id.exportViewExistingButton);
        projectViewExistingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){

                switchToListPoints();

            }
        });




        label = (TextView) (v.findViewById(R.id.directoryPathLabel));
        label.setText(R.string.directory_path_label);

        final EditText directoryInput = (EditText) (v.findViewById(R.id.directoryPath));
        directoryInput.setInputType(InputType.TYPE_TEXT_FLAG_AUTO_CORRECT);
        //directoryInput.setHint(R.string.person_text_addr_hint);
        directoryInput.addTextChangedListener(textWatcher);

        label = (TextView) (v.findViewById(R.id.fileNameLabel));
        label.setText(R.string.export_filename_label);

        final EditText fileNameInput = (EditText) (v.findViewById(R.id.fileName));
        //fileNameInput.setHint(R.string.person_order_hint);
        fileNameInput.addTextChangedListener(textWatcher);

        label = (TextView) (v.findViewById(R.id.fileExtentLabel));
        label.setText(R.string.filename_extent_label);

        final EditText fileNameExtentInput = (EditText) (v.findViewById(R.id.fileExtent));
        fileNameExtentInput.setHint(R.string.extent_hint);
        fileNameExtentInput.addTextChangedListener(textWatcher);




        //initialize
        directoryInput.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.colorGray));
        fileNameInput.setBackgroundColor (ContextCompat.getColor(getActivity(), R.color.colorGray));
        fileNameExtentInput.setBackgroundColor(ContextCompat.
                                                    getColor(getActivity(), R.color.colorGray));

        directoryInput         .setEnabled(false);
        fileNameInput          .setEnabled(false);
        fileNameExtentInput    .setEnabled(false);

    }


    private void   initializeUI(View v){
        //determine if a project is yet associated with the fragment
        long openProjectID = GBUtilities.getInstance().getOpenProjectID((GBActivity)getActivity());
        if (openProjectID != GBUtilities.ID_DOES_NOT_EXIST){
            GBProject project = GBUtilities.getInstance().getOpenProject((GBActivity)getActivity());
            //if there is a project corresponding to the projectID, put the name up on the screen
            if (project != null) {
                EditText projectIDView = (EditText) v.findViewById(R.id.exportProjectIDInput);
                projectIDView.setText(String.valueOf(openProjectID));
                //Project Nick Name
                TextView projectNameView = (TextView) v.findViewById(R.id.exportProjectNameInput);
                //There are no events associated with this field
                projectNameView.setText(project.getProjectName().toString().trim());
            }
        }

        EditText directoryPath = (EditText) v.findViewById(R.id.directoryPath);
        mCDFPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).toString();

        File pathFile = new File(mCDFPath.toString(), getString(R.string.app_name));
        mCDFPath = pathFile.getAbsolutePath();
/*
        String temp = getDocumentDirectory().getAbsolutePath();

        if (!pathFile.isDirectory()){
            if (!pathFile.mkdirs()){
                GBUtilities.getInstance().errorHandler(getActivity(), R.string.error_unable_to_access_storage);
            }
        }
*/
        directoryPath.setText(mCDFPath);

        EditText fileName = (EditText) v.findViewById(R.id.fileName);
        mCDFileName   = getFileName(openProjectID);
        fileName.setText(mCDFileName);

        EditText fileExtent = (EditText) v.findViewById(R.id.fileExtent);
        fileExtent.setText(R.string.export_file_extent);

        //Get slider settings from the preferences

        SwitchCompat projReadabilitySwitch = (SwitchCompat) v.findViewById(R.id.switchReadability);
        boolean isSet = GBProject.getProjectExport((GBActivity)getActivity(),
                                                    GBProject.sProjectReadabilityExportTag);
        projReadabilitySwitch.setChecked(isSet);

        SwitchCompat projHeadersSwitch = (SwitchCompat) v.findViewById(R.id.switchProjHeaders);
        isSet = GBProject.getProjectExport((GBActivity)getActivity(),
                                            GBProject.sProjectHeadersExportTag);
        projHeadersSwitch.setChecked(isSet);

        SwitchCompat projNameSwitch = (SwitchCompat) v.findViewById(R.id.switchProjName);
        isSet = GBProject.getProjectExport((GBActivity)getActivity(),
                                            GBProject.sProjectNameExportTag);
        projNameSwitch.setChecked(isSet);

        SwitchCompat projCreateSwitch = (SwitchCompat) v.findViewById(R.id.switchProjCreate);
        isSet = GBProject.getProjectExport((GBActivity)getActivity(),
                                            GBProject.sProjectCreateExportTag);
        projCreateSwitch.setChecked(isSet);

        SwitchCompat projLastSwitch = (SwitchCompat) v.findViewById(R.id.switchProjLast);
        isSet = GBProject.getProjectExport((GBActivity)getActivity(),
                                            GBProject.sProjectLastMaintExportTag);
        projLastSwitch.setChecked(isSet);

        SwitchCompat projDescSwitch = (SwitchCompat) v.findViewById(R.id.switchProjDesc);
        isSet = GBProject.getProjectExport((GBActivity)getActivity(),
                                            GBProject.sProjectDescExportTag);
        projDescSwitch.setChecked(isSet);

        SwitchCompat projHeightSwitch = (SwitchCompat) v.findViewById(R.id.switchProjHeight);
        isSet = GBProject.getProjectExport((GBActivity)getActivity(),
                                            GBProject.sProjectHeightExportTag);
        projHeightSwitch.setChecked(isSet);

        SwitchCompat projCoordTypeSwitch = (SwitchCompat) v.findViewById(R.id.switchProjCoordType);
        isSet = GBProject.getProjectExport((GBActivity)getActivity(),
                                            GBProject.sProjectCoordTypeExportTag);
        projCoordTypeSwitch.setChecked(isSet);

        SwitchCompat projNbMeanSwitch = (SwitchCompat) v.findViewById(R.id.switchProjNbMean);
        isSet = GBProject.getProjectExport((GBActivity)getActivity(),
                                            GBProject.sProjectNbMeanExportTag);
        projNbMeanSwitch.setChecked(isSet);

        SwitchCompat projZoneSwitch = (SwitchCompat) v.findViewById(R.id.switchProjZone);
        isSet = GBProject.getProjectExport((GBActivity)getActivity(),
                                            GBProject.sProjectZoneExportTag);
        projZoneSwitch.setChecked(isSet);

        SwitchCompat projDistUnitsSwitch = (SwitchCompat) v.findViewById(R.id.switchProjDistUnits);
        isSet = GBProject.getProjectExport((GBActivity)getActivity(),
                                            GBProject.sProjectDistUnitsExportTag);
        projDistUnitsSwitch.setChecked(isSet);

        SwitchCompat projAutosaveSwitch = (SwitchCompat) v.findViewById(R.id.switchProjAutosave);
        isSet = GBProject.getProjectExport((GBActivity)getActivity(),
                                            GBProject.sProjectAutosaveExportTag);
        projAutosaveSwitch.setChecked(isSet);

        SwitchCompat projRMSvSTDSwitch = (SwitchCompat) v.findViewById(R.id.switchProjRMSvSTD);
        isSet = GBProject.getProjectExport((GBActivity)getActivity(),
                                            GBProject.sProjectAutosaveExportTag);
        projRMSvSTDSwitch.setChecked(isSet);

        SwitchCompat projUIOrderSwitch = (SwitchCompat) v.findViewById(R.id.switchProjUIOrder);
        isSet = GBProject.getProjectExport((GBActivity)getActivity(),
                                            GBProject.sProjectUIOrderExportTag);
        projUIOrderSwitch.setChecked(isSet);

        SwitchCompat projDDvDMSSwitch = (SwitchCompat) v.findViewById(R.id.switchProjDDvDMS);
        isSet = GBProject.getProjectExport((GBActivity)getActivity(),
                                            GBProject.sProjectDDvDMSExportTag);
        projDDvDMSSwitch.setChecked(isSet);

        SwitchCompat projDirVPMSwitch = (SwitchCompat) v.findViewById(R.id.switchProjDirVPM);
        isSet = GBProject.getProjectExport((GBActivity)getActivity(),
                                            GBProject.sProjectDirVPMExportTag);
        projDirVPMSwitch.setChecked(isSet);

        SwitchCompat projDataSrcSwitch = (SwitchCompat) v.findViewById(R.id.switchProjDataSrc);
        isSet = GBProject.getProjectExport((GBActivity)getActivity(),
                                            GBProject.sProjectDataSrcExportTag);
        projDataSrcSwitch.setChecked(isSet);

        SwitchCompat projLocPrecisionSwitch = (SwitchCompat) v.findViewById(R.id.switchProjLocPrecision);
        isSet = GBProject.getProjectExport((GBActivity)getActivity(),
                                            GBProject.sProjectLocPrcExportTag);
        projLocPrecisionSwitch.setChecked(isSet);

        SwitchCompat projStdPrecisionSwitch = (SwitchCompat) v.findViewById(R.id.switchProjStdPrc);
        isSet = GBProject.getProjectExport((GBActivity)getActivity(),
                                            GBProject.sProjectStdPrcExportTag);
        projStdPrecisionSwitch.setChecked(isSet);



        SwitchCompat pntNumberSwitch = (SwitchCompat) v.findViewById(R.id.switchPntNumber);
        isSet = GBPoint.getPointExport((GBActivity)getActivity(), GBPoint.sPointNumberExportTag);
        pntNumberSwitch.setChecked(isSet);

        SwitchCompat pntIsMeanedSwitch = (SwitchCompat) v.findViewById(R.id.switchPntIsMeaned);
        isSet = GBPoint.getPointExport((GBActivity)getActivity(), GBPoint.sPointIsMeanedExportTag);
        pntIsMeanedSwitch.setChecked(isSet);

        SwitchCompat pntOffDistSwitch = (SwitchCompat) v.findViewById(R.id.switchPntOffDist);
        isSet = GBPoint.getPointExport((GBActivity)getActivity(), GBPoint.sPointOffDistExportTag);
        pntOffDistSwitch.setChecked(isSet);

        SwitchCompat pntOffHeadSwitch = (SwitchCompat) v.findViewById(R.id.switchPntOffHead);
        isSet = GBPoint.getPointExport((GBActivity)getActivity(), GBPoint.sPointOffHeadExportTag);
        pntOffHeadSwitch.setChecked(isSet);

        SwitchCompat pntOffEleSwitch = (SwitchCompat) v.findViewById(R.id.switchPntOffEle);
        isSet = GBPoint.getPointExport((GBActivity)getActivity(), GBPoint.sPointOffEleExportTag);
        pntOffEleSwitch.setChecked(isSet);

        SwitchCompat pntHeightSwitch = (SwitchCompat) v.findViewById(R.id.switchPntHeight);
        isSet = GBPoint.getPointExport((GBActivity)getActivity(), GBPoint.sPointHeightExportTag);
        pntHeightSwitch.setChecked(isSet);

        SwitchCompat pntFCSwitch = (SwitchCompat) v.findViewById(R.id.switchPntFC);
        isSet = GBPoint.getPointExport((GBActivity)getActivity(), GBPoint.sPointFCExportTag);
        pntFCSwitch.setChecked(isSet);

        SwitchCompat pntNotesSwitch = (SwitchCompat) v.findViewById(R.id.switchPntNotes);
        isSet = GBPoint.getPointExport((GBActivity)getActivity(), GBPoint.sPointNotesExportTag);
        pntNotesSwitch.setChecked(isSet);

        SwitchCompat pntHdopSwitch = (SwitchCompat) v.findViewById(R.id.switchPntHdop);
        isSet = GBPoint.getPointExport((GBActivity)getActivity(), GBPoint.sPointHdopExportTag);
        pntHdopSwitch.setChecked(isSet);

        SwitchCompat pntVdopSwitch = (SwitchCompat) v.findViewById(R.id.switchPntVdop);
        isSet = GBPoint.getPointExport((GBActivity)getActivity(), GBPoint.sPointVdopExportTag);
        pntVdopSwitch.setChecked(isSet);

        SwitchCompat pntPdopSwitch = (SwitchCompat) v.findViewById(R.id.switchPntPDOP);
        isSet = GBPoint.getPointExport((GBActivity)getActivity(), GBPoint.sPointPdopExportTag);
        pntPdopSwitch.setChecked(isSet);

        SwitchCompat pntTdopSwitch = (SwitchCompat) v.findViewById(R.id.switchPntTdop);
        isSet = GBPoint.getPointExport((GBActivity)getActivity(), GBPoint.sPointTdopExportTag);
        pntTdopSwitch.setChecked(isSet);

        SwitchCompat pntHrmsSwitch = (SwitchCompat) v.findViewById(R.id.switchPntHrms);
        isSet = GBPoint.getPointExport((GBActivity)getActivity(), GBPoint.sPointHrmsExportTag);
        pntHrmsSwitch.setChecked(isSet);

        SwitchCompat pntVrmsSwitch = (SwitchCompat) v.findViewById(R.id.switchPntVrms);
        isSet = GBPoint.getPointExport((GBActivity)getActivity(), GBPoint.sPointVrmsExportTag);
        pntVrmsSwitch.setChecked(isSet);
    }




    private void onExport() {

        View v = getView();
        if (v == null)return;

        saveSwitchSettings();

        GBProject openProject = GBUtilities.getInstance().getOpenProject((GBActivity)getActivity());
        if (openProject == null) return;

        RadioButton emailRadio         = (RadioButton) v.findViewById(R.id.radioEmail) ;
        RadioButton textRadio          = (RadioButton) v.findViewById(R.id.radioText) ;
        RadioButton fileRadio          = (RadioButton) v.findViewById(R.id.radioFile) ;
        RadioButton generalRadio       = (RadioButton) v.findViewById(R.id.radioGeneral);

        //set Defaults

        int whereFlag = EXPORT_EMAIL;

        //Determine what the user specified
        if (emailRadio       .isChecked()) whereFlag = EXPORT_EMAIL;
        if (textRadio        .isChecked()) whereFlag = EXPORT_SMS;
        if (fileRadio        .isChecked()) whereFlag = EXPORT_FILE;
        if (generalRadio     .isChecked()) whereFlag = EXPORT_GENERAL;


        String message = getProjectExport();
        String subject = getString(R.string.export_subject, openProject.getProjectName());
        int statusMsg = R.string.export_label;
        GBUtilities utilities = GBUtilities.getInstance();


        utilities.showStatus(getActivity(), statusMsg);



        String chooser_title =getString(R.string.export_chooser_title);
        String emailAddr = "";
        if (whereFlag == EXPORT_EMAIL){


            //GBUtilities.getInstance().sendEmail(getActivity(), subject, emailAddr, message);
            GBUtilities.getInstance()
                           .exportEmail(getActivity(), subject, emailAddr, message, chooser_title );

        } else if (whereFlag == EXPORT_FILE){
            writeFile(message);
            GBUtilities.getInstance().showStatus(getActivity(), R.string.export_file_written);

        } else if (whereFlag == EXPORT_SMS){
            GBUtilities.getInstance().exportSMS(getActivity(), subject, message);
        } else if (whereFlag == EXPORT_GENERAL){
            GBUtilities.getInstance().exportText(getActivity(), subject, message, chooser_title);
        }

    }


    private void saveSwitchSettings(){
        View v = getView();
        if (v == null)return;

        SwitchCompat projReadabilitySwitch = (SwitchCompat) v.findViewById(R.id.switchReadability);
        boolean isSet = projReadabilitySwitch.isChecked();
        GBProject.setProjectExport((GBActivity)getActivity(),
                                    GBProject.sProjectReadabilityExportTag,
                                    isSet);


        SwitchCompat projHeadersSwitch = (SwitchCompat) v.findViewById(R.id.switchProjHeaders);
        isSet = projHeadersSwitch.isChecked();
        GBProject.setProjectExport((GBActivity)getActivity(), GBProject.sProjectHeadersExportTag, isSet);


        SwitchCompat projNameSwitch = (SwitchCompat) v.findViewById(R.id.switchProjName);
        isSet = projNameSwitch.isChecked();
        GBProject.setProjectExport((GBActivity)getActivity(),GBProject.sProjectNameExportTag, isSet);


        SwitchCompat projCreateSwitch = (SwitchCompat) v.findViewById(R.id.switchProjCreate);
        isSet = projCreateSwitch.isChecked();
        GBProject.setProjectExport((GBActivity)getActivity(),GBProject.sProjectCreateExportTag, isSet);


        SwitchCompat projLastSwitch = (SwitchCompat) v.findViewById(R.id.switchProjLast);
        isSet = projLastSwitch.isChecked();
        GBProject.setProjectExport((GBActivity)getActivity(),
                GBProject.sProjectLastMaintExportTag, isSet);

        SwitchCompat projDescSwitch = (SwitchCompat) v.findViewById(R.id.switchProjDesc);
        isSet = projDescSwitch.isChecked();
        GBProject.setProjectExport((GBActivity)getActivity(),
                GBProject.sProjectDescExportTag, isSet);

        SwitchCompat projHeightSwitch = (SwitchCompat) v.findViewById(R.id.switchProjHeight);
        isSet = projHeightSwitch.isChecked();
        GBProject.setProjectExport((GBActivity)getActivity(),
                GBProject.sProjectHeightExportTag, isSet);

        SwitchCompat projCoordTypeSwitch = (SwitchCompat) v.findViewById(R.id.switchProjCoordType);
        isSet = projCoordTypeSwitch.isChecked();
        GBProject.setProjectExport((GBActivity)getActivity(),
                GBProject.sProjectCoordTypeExportTag, isSet);

        SwitchCompat projNbMeanSwitch = (SwitchCompat) v.findViewById(R.id.switchProjNbMean);
        isSet = projNbMeanSwitch.isChecked();
        GBProject.setProjectExport((GBActivity)getActivity(),
                GBProject.sProjectNbMeanExportTag, isSet);

        SwitchCompat projZoneSwitch = (SwitchCompat) v.findViewById(R.id.switchProjZone);
        isSet = projZoneSwitch.isChecked();
        GBProject.setProjectExport((GBActivity)getActivity(),
                GBProject.sProjectZoneExportTag, isSet);

        SwitchCompat projDistUnitsSwitch = (SwitchCompat) v.findViewById(R.id.switchProjDistUnits);
        isSet = projDistUnitsSwitch.isChecked();
        GBProject.setProjectExport((GBActivity)getActivity(),
                GBProject.sProjectDistUnitsExportTag, isSet);

        SwitchCompat projAutosaveSwitch = (SwitchCompat) v.findViewById(R.id.switchProjAutosave);
        isSet = projAutosaveSwitch.isChecked();
        GBProject.setProjectExport((GBActivity)getActivity(),
                GBProject.sProjectAutosaveExportTag, isSet);

        SwitchCompat projRMSvSTDSwitch = (SwitchCompat) v.findViewById(R.id.switchProjRMSvSTD);
        isSet = projRMSvSTDSwitch.isChecked();
        GBProject.setProjectExport((GBActivity)getActivity(),
                GBProject.sProjectAutosaveExportTag, isSet);

        SwitchCompat projUIOrderSwitch = (SwitchCompat) v.findViewById(R.id.switchProjUIOrder);
        isSet = projUIOrderSwitch.isChecked();
        GBProject.setProjectExport((GBActivity)getActivity(),
                GBProject.sProjectUIOrderExportTag, isSet);

        SwitchCompat projDDvDMSSwitch = (SwitchCompat) v.findViewById(R.id.switchProjDDvDMS);
        isSet = projDDvDMSSwitch.isChecked();
        GBProject.setProjectExport((GBActivity)getActivity(),
                GBProject.sProjectDDvDMSExportTag, isSet);

        SwitchCompat projDirVPMSwitch = (SwitchCompat) v.findViewById(R.id.switchProjDirVPM);
        isSet = projDirVPMSwitch.isChecked();
        GBProject.setProjectExport((GBActivity)getActivity(),
                GBProject.sProjectDirVPMExportTag, isSet);

        SwitchCompat projDataSrcSwitch = (SwitchCompat) v.findViewById(R.id.switchProjDataSrc);
        isSet = projDataSrcSwitch.isChecked();
        GBProject.setProjectExport((GBActivity)getActivity(),
                GBProject.sProjectDataSrcExportTag, isSet);

        SwitchCompat projLocPrecisionSwitch = (SwitchCompat) v.findViewById(R.id.switchProjLocPrecision);
        isSet = projLocPrecisionSwitch.isChecked();
        GBProject.setProjectExport((GBActivity)getActivity(),
                GBProject.sProjectLocPrcExportTag, isSet);

        SwitchCompat projStdPrecisionSwitch = (SwitchCompat) v.findViewById(R.id.switchProjStdPrc);
        isSet = projStdPrecisionSwitch.isChecked();
        GBProject.setProjectExport((GBActivity)getActivity(),GBProject.sProjectStdPrcExportTag, isSet);



        SwitchCompat pntNumberSwitch = (SwitchCompat) v.findViewById(R.id.switchPntNumber);
        isSet = pntNumberSwitch.isChecked();
        GBPoint.setPointExport((GBActivity)getActivity(), GBPoint.sPointNumberExportTag, isSet);

        SwitchCompat pntIsMeanedSwitch = (SwitchCompat) v.findViewById(R.id.switchPntIsMeaned);
        isSet = pntIsMeanedSwitch.isChecked();
        GBPoint.setPointExport((GBActivity)getActivity(), GBPoint.sPointIsMeanedExportTag, isSet);

        SwitchCompat pntOffDistSwitch = (SwitchCompat) v.findViewById(R.id.switchPntOffDist);
        isSet = pntOffDistSwitch.isChecked();
        GBPoint.setPointExport((GBActivity)getActivity(), GBPoint.sPointOffDistExportTag, isSet);

        SwitchCompat pntOffHeadSwitch = (SwitchCompat) v.findViewById(R.id.switchPntOffHead);
        isSet = pntOffHeadSwitch.isChecked();
        GBPoint.setPointExport((GBActivity)getActivity(), GBPoint.sPointOffHeadExportTag, isSet);

        SwitchCompat pntOffEleSwitch = (SwitchCompat) v.findViewById(R.id.switchPntOffEle);
        isSet = pntOffEleSwitch.isChecked();
        GBPoint.setPointExport((GBActivity)getActivity(), GBPoint.sPointOffEleExportTag, isSet);

        SwitchCompat pntHeightSwitch = (SwitchCompat) v.findViewById(R.id.switchPntHeight);
        isSet = pntHeightSwitch.isChecked();
        GBPoint.setPointExport((GBActivity)getActivity(), GBPoint.sPointHeightExportTag, isSet);

        SwitchCompat pntFCSwitch = (SwitchCompat) v.findViewById(R.id.switchPntFC);
        isSet = pntFCSwitch.isChecked();
        GBPoint.setPointExport((GBActivity)getActivity(), GBPoint.sPointFCExportTag, isSet);

        SwitchCompat pntNotesSwitch = (SwitchCompat) v.findViewById(R.id.switchPntNotes);
        isSet = pntNotesSwitch.isChecked();
        GBPoint.setPointExport((GBActivity)getActivity(), GBPoint.sPointNotesExportTag, isSet);

        SwitchCompat pntHdopSwitch = (SwitchCompat) v.findViewById(R.id.switchPntHdop);
        isSet = pntHdopSwitch.isChecked();
        GBPoint.setPointExport((GBActivity)getActivity(), GBPoint.sPointHdopExportTag, isSet);

        SwitchCompat pntVdopSwitch = (SwitchCompat) v.findViewById(R.id.switchPntVdop);
        isSet = pntVdopSwitch.isChecked();
        GBPoint.setPointExport((GBActivity)getActivity(), GBPoint.sPointVdopExportTag, isSet);

        SwitchCompat pntPdopSwitch = (SwitchCompat) v.findViewById(R.id.switchPntPDOP);
        isSet = pntPdopSwitch.isChecked();
        GBPoint.setPointExport((GBActivity)getActivity(), GBPoint.sPointPdopExportTag, isSet);

        SwitchCompat pntTdopSwitch = (SwitchCompat) v.findViewById(R.id.switchPntTdop);
        isSet = pntTdopSwitch.isChecked();
        GBPoint.setPointExport((GBActivity)getActivity(), GBPoint.sPointTdopExportTag, isSet);

        SwitchCompat pntHrmsSwitch = (SwitchCompat) v.findViewById(R.id.switchPntHrms);
        isSet = pntHrmsSwitch.isChecked();
        GBPoint.setPointExport((GBActivity)getActivity(), GBPoint.sPointHrmsExportTag, isSet);

        SwitchCompat pntVrmsSwitch = (SwitchCompat) v.findViewById(R.id.switchPntVrms);
        isSet = pntVrmsSwitch.isChecked();
        GBPoint.setPointExport((GBActivity)getActivity(), GBPoint.sPointVrmsExportTag, isSet);


    }



    //***********************************/
    //**** List Points Button     *******/
    //***********************************/

    private void switchToListPoints(){
        GBActivity myActivity = (GBActivity) getActivity();
        myActivity.switchToPointsListScreen(new GBPath(GBPath.sEditTag));
    }


    //*********************************************************/
    //      Methods dealing with whether the UI has changed   //
    //*********************************************************/
    private void setUIChanged(){
        isUIChanged = true;
        exportButtonEnable(GBUtilities.BUTTON_ENABLE);

    }

    private void setUISaved(){
        isUIChanged = false;

        //disable the save button
        exportButtonEnable(GBUtilities.BUTTON_DISABLE);
    }

    private void setUISaved(View v){
        isUIChanged = false;

        //disable the save button
        exportButtonEnable(v, GBUtilities.BUTTON_DISABLE);
    }

    private void exportButtonEnable(boolean isEnabled){
        View v = getView();
        exportButtonEnable(v, isEnabled);
    }

    private void exportButtonEnable(View v, boolean isEnabled){
        if (v == null)return; //onCreateView() hasn't run yet

        Button exportButton = (Button) v.findViewById(R.id.exportButton);

        GBUtilities utilities = GBUtilities.getInstance();
        utilities.enableButton(getActivity(),  exportButton, isEnabled);
    }



    //***********************************/
    //****    File Methods          *****/
    //***********************************/
    private String getFileName(long projectID) {
        GBProject project = GBProjectManager.getInstance().getProject(projectID);
        if (project == null)return null;

        // Create an image file name
        return  project.getProjectName().toString();
    }

    public File getDocumentDirectory(){
        //Get the public directory for documents for this app
        return new File(
                //top-level shared/external storage directory for placing files
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
                //getActivity().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
                getString(R.string.app_name));
    }

    private File createProjectCDFile(int suffix) throws IOException {

        GBProject project = GBProjectManager.getInstance().getProject(getId());
        if (project == null) return null;

        View v = getView();
        if (v == null)return null;

        EditText directoryPath = (EditText) v.findViewById(R.id.directoryPath);
        mCDFPath = directoryPath.getText();

        EditText fileName      = (EditText) v.findViewById(R.id.fileName);
        mCDFileName   = fileName.getText() + getString(suffix);

        EditText fileExtent    = (EditText) v.findViewById(R.id.fileExtent);
        String fileExtentString = fileExtent.getText().toString();



        /*
        //getExternalStoragePublicDirectory()
        //accessible to all apps and the user
        //with the DIRECTORY_DOCUMENTS argument
        //requires manifest permission
        //<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
        //
        // if use the method: getExternalFilesDir()
        // files are deleted when app uninstalled
        //requires manifest permission:
        //<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"
        //                               android:maxSdkVersion="18" />
        */

        //Assure the path exists
        //File mmDirectory = new File(mCDFPath.toString());
        File mmDirectory = getDocumentDirectory();


        //check if the project subdirectory already exists
        if (!mmDirectory.exists()){
            //if not, create it and any intervening directories necessary
            if (!mmDirectory.mkdirs()){
                //Unable to create the file
                return null;
            }
        }

        return File.createTempFile(mCDFileName.toString(),     /*   prefix  */
                                           fileExtentString,   /*   extent  */
                                           mmDirectory );      /* directory */
    }


    private File writeFile(String message){
        File cdfFile = null;
        FileWriter writer;
        View v = getView();
        if (v == null)return null;

        long openProjectID = GBUtilities.getInstance().getOpenProjectID((GBActivity)getActivity());

        if (!GBUtilities.getInstance().isExternalStorageWritable()){
            GBUtilities.getInstance()
                    .errorHandler(getActivity(), R.string.error_unable_to_access_storage);
            return null;
        }

        //must make sure we have permission to write to external storage


        try {

            cdfFile = createProjectCDFile((int)openProjectID);
            if (cdfFile == null){
                GBUtilities.getInstance()
                        .errorHandler(getActivity(), R.string.error_unable_to_create_file);
                return null;
            }
            writer = new FileWriter(cdfFile);

            writer.append(message);

            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return cdfFile;
    }


    //***************************************/
    //****     Export String Builders   *****/
    //***************************************/
    private String getProjectExport(){
        StringBuilder projectString = new StringBuilder();
        String deliminator;
        String ls = System.getProperty("line.separator");
        String comma = ",";

        GBProject openProject = GBUtilities.getInstance().getOpenProject((GBActivity)getActivity());

        View v = getView();
        if (v == null)return null;

        SwitchCompat projReadabilitySwitch = (SwitchCompat) v.findViewById(R.id.switchReadability);

        SwitchCompat projHeadersSwitch = (SwitchCompat) v.findViewById(R.id.switchProjHeaders);
        SwitchCompat projNameSwitch = (SwitchCompat) v.findViewById(R.id.switchProjName);
        SwitchCompat projCreateSwitch = (SwitchCompat) v.findViewById(R.id.switchProjCreate);
        SwitchCompat projLastSwitch = (SwitchCompat) v.findViewById(R.id.switchProjLast);
        SwitchCompat projDescSwitch = (SwitchCompat) v.findViewById(R.id.switchProjDesc);
        SwitchCompat projHeightSwitch = (SwitchCompat) v.findViewById(R.id.switchProjHeight);
        SwitchCompat projCoordTypeSwitch = (SwitchCompat) v.findViewById(R.id.switchProjCoordType);
        SwitchCompat projNbMeanSwitch = (SwitchCompat) v.findViewById(R.id.switchProjNbMean);
        SwitchCompat projZoneSwitch = (SwitchCompat) v.findViewById(R.id.switchProjZone);
        SwitchCompat projDistUnitsSwitch = (SwitchCompat) v.findViewById(R.id.switchProjDistUnits);
        SwitchCompat projAutosaveSwitch = (SwitchCompat) v.findViewById(R.id.switchProjAutosave);
        SwitchCompat projRMSvSTDSwitch = (SwitchCompat) v.findViewById(R.id.switchProjRMSvSTD);
        SwitchCompat projUIOrderSwitch = (SwitchCompat) v.findViewById(R.id.switchProjUIOrder);
        SwitchCompat projDDvDMSSwitch = (SwitchCompat) v.findViewById(R.id.switchProjDDvDMS);
        SwitchCompat projDirVPMSwitch = (SwitchCompat) v.findViewById(R.id.switchProjDirVPM);
        SwitchCompat projDataSrcSwitch = (SwitchCompat) v.findViewById(R.id.switchProjDataSrc);
        SwitchCompat projLocPrecisionSwitch = (SwitchCompat) v.findViewById(R.id.switchProjLocPrecision);
        SwitchCompat projStdPrecisionSwitch = (SwitchCompat) v.findViewById(R.id.switchProjStdPrc);


        SwitchCompat pntNumberSwitch = (SwitchCompat) v.findViewById(R.id.switchPntNumber);

        SwitchCompat pntIsMeanedSwitch = (SwitchCompat) v.findViewById(R.id.switchPntIsMeaned);
        SwitchCompat pntOffDistSwitch = (SwitchCompat) v.findViewById(R.id.switchPntOffDist);
        SwitchCompat pntOffHeadSwitch = (SwitchCompat) v.findViewById(R.id.switchPntOffHead);
        SwitchCompat pntOffEleSwitch = (SwitchCompat) v.findViewById(R.id.switchPntOffEle);
        SwitchCompat pntHeightSwitch = (SwitchCompat) v.findViewById(R.id.switchPntHeight);
        SwitchCompat pntFCSwitch = (SwitchCompat) v.findViewById(R.id.switchPntFC);
        SwitchCompat pntNotesSwitch = (SwitchCompat) v.findViewById(R.id.switchPntNotes);
        SwitchCompat pntHdopSwitch = (SwitchCompat) v.findViewById(R.id.switchPntHdop);
        SwitchCompat pntVdopSwitch = (SwitchCompat) v.findViewById(R.id.switchPntVdop);
        SwitchCompat pntPdopSwitch = (SwitchCompat) v.findViewById(R.id.switchPntPDOP);
        SwitchCompat pntTdopSwitch = (SwitchCompat) v.findViewById(R.id.switchPntTdop);
        SwitchCompat pntHrmsSwitch = (SwitchCompat) v.findViewById(R.id.switchPntHrms);
        SwitchCompat pntVrmsSwitch = (SwitchCompat) v.findViewById(R.id.switchPntVrms);


        try {
            //Set readability parameter
            deliminator = comma;
            if (projReadabilitySwitch.isChecked()){
                deliminator = ls;
            }

            //Export the Project
            if (projNameSwitch.isChecked()) {
                if (projHeadersSwitch.isChecked()) {
                    projectString.append(getString(R.string.exc_switch_proj_name));
                    projectString.append(" = ");
                }
                projectString.append(openProject.getProjectName().toString());
                projectString.append(deliminator);
            }


            if (projCreateSwitch.isChecked()) {
                if (projHeadersSwitch.isChecked()) {
                    projectString.append(getString(R.string.exc_switch_proj_create));
                    projectString.append(" = ");
                }
                String dateString = GBUtilities.getDateTimeString(openProject.getProjectDateCreated());
                projectString.append(dateString);
                projectString.append(deliminator);
            }

            if (projLastSwitch.isChecked()) {
                if (projHeadersSwitch.isChecked()) {
                    projectString.append(getString(R.string.exc_switch_proj_last_touched));
                    projectString.append(" = ");
                }
                String dateString = GBUtilities.getDateTimeString(openProject.getProjectLastModified());
                projectString.append(dateString);
                projectString.append(deliminator);
            }


            if (projDescSwitch.isChecked()) {
                if (projHeadersSwitch.isChecked()) {
                    projectString.append(getString(R.string.exc_switch_proj_desc));
                    projectString.append(" = ");
                }

                projectString.append(openProject.getProjectDescription());
                projectString.append(deliminator);
            }

            //Project Settings

            if (projHeightSwitch.isChecked()) {
                if (projHeadersSwitch.isChecked()) {
                    projectString.append(getString(R.string.exc_switch_proj_height));
                    projectString.append(" = ");
                }

                projectString.append(String.valueOf(openProject.getHeight()));
                projectString.append(deliminator);
            }


            if (projCoordTypeSwitch.isChecked()) {
                if (projHeadersSwitch.isChecked()) {
                    projectString.append(getString(R.string.exc_switch_proj_height));
                    projectString.append(" = ");
                }
                int choice = openProject.getCoordinateType();
                String choiceString = GBCoordinate.sCoordinateTypeUnknown;
                if (choice == GBCoordinate.sCoordinateDBTypeWGS84) {
                    choiceString = GBCoordinate.sCoordinateTypeWGS84;
                } else if (choice == GBCoordinate.sCoordinateDBTypeNAD83) {
                    choiceString = GBCoordinate.sCoordinateTypeNAD83;
                } else if (choice == GBCoordinate.sCoordinateDBTypeUTM) {
                    choiceString = GBCoordinate.sCoordinateTypeUTM;
                } else if (choice == GBCoordinate.sCoordinateDBTypeSPCS) {
                    choiceString = GBCoordinate.sCoordinateTypeSPCS;
                }
                projectString.append(choiceString);
                projectString.append(deliminator);
            }

            if (projNbMeanSwitch.isChecked()) {
                if (projHeadersSwitch.isChecked()) {
                    projectString.append(getString(R.string.exc_switch_proj_nb_mean));
                    projectString.append(" = ");
                }

                projectString.append(String.valueOf(openProject.getNumMean()));
                projectString.append(deliminator);
            }

            if (projZoneSwitch.isChecked()) {
                if (projHeadersSwitch.isChecked()) {
                    projectString.append(getString(R.string.exc_switch_proj_zone));
                    projectString.append(" = ");
                }

                int zone = openProject.getZone();
                projectString.append(String.valueOf(zone));
                projectString.append(" ");
                GBCoordinateConstants constants = new GBCoordinateConstants(zone);

                String state;
                if (constants.getZone() == GBUtilities.ID_DOES_NOT_EXIST){
                    state = getString(R.string.spc_zone_error);
                } else {
                    state = constants.getState();
                }
                projectString.append(state);

                projectString.append(deliminator);
            }


            if (projDistUnitsSwitch.isChecked()) {
                if (projHeadersSwitch.isChecked()) {
                    projectString.append(getString(R.string.exc_switch_proj_dist_units));
                    projectString.append(" = ");
                }
                int choice = openProject.getDistanceUnits();
                String choiceString = GBProject.sMetersString;
                if (choice == GBProject.sFeet) {
                    choiceString = GBProject.sFeetString;
                } else if (choice == GBProject.sIntFeet) {
                    choiceString = GBProject.sIntFeetString;
                }
                projectString.append(choiceString);
                projectString.append(deliminator);
            }


            if (projAutosaveSwitch.isChecked()) {
                if (projHeadersSwitch.isChecked()) {
                    projectString.append(getString(R.string.exc_switch_proj_autosave));
                    projectString.append(" = ");
                }
                int choice = openProject.getAutosave();
                String choiceString = getString(R.string.autosave_item);
                if (choice != GBProject.sAUTOSAVE) {
                    choiceString = getString(R.string.manual_item);
                }
                projectString.append(choiceString);
                projectString.append(deliminator);
            }



            if (projRMSvSTDSwitch.isChecked()) {
                if (projHeadersSwitch.isChecked()) {
                    projectString.append(getString(R.string.exc_switch_proj_rmsVstd));
                    projectString.append(" = ");
                }
                int choice = openProject.getRMSvStD();
                String choiceString = GBProject.sRMSString;
                if (choice != GBProject.sRMS) {
                    choiceString =  GBProject.sStdDevString;
                }
                projectString.append(choiceString);
                projectString.append(deliminator);
            }




            if (projUIOrderSwitch.isChecked()) {
                if (projHeadersSwitch.isChecked()) {
                    projectString.append(getString(R.string.exc_switch_proj_ui_order));
                    projectString.append(" = ");
                }
                int coordinateType = openProject.getCoordinateType();
                int choice = openProject.getUIOrder();
                String choiceString = GBProject.sENString;
                if (coordinateType == GBCoordinate.sCoordinateDBTypeWGS84){
                    if (choice == GBProject.sLatLng){
                        choiceString = GBProject.sLatLngString;
                    } else {
                        choiceString = GBProject.sLngLatString;
                    }
                } else {
                    if (choice == GBProject.sEN){
                        choiceString = GBProject.sENString;
                    } else {
                        choiceString = GBProject.sNEString;
                    }
                }

                projectString.append(choiceString);
                projectString.append(deliminator);
            }


            if (projDDvDMSSwitch.isChecked()) {
                if (projHeadersSwitch.isChecked()) {
                    projectString.append(getString(R.string.exc_switch_proj_ddVdms));
                    projectString.append(" = ");
                }
                int choice = openProject.getDDvDMS();
                String choiceString = GBProject.sDDString;
                if (choice != GBProject.sDD) {
                    choiceString =  GBProject.sDMSString;
                }
                projectString.append(choiceString);
                projectString.append(deliminator);
            }



            if (projDirVPMSwitch.isChecked()) {
                if (projHeadersSwitch.isChecked()) {
                    projectString.append(getString(R.string.exc_switch_proj_dirVpm));
                    projectString.append(" = ");
                }
                int choice = openProject.getDIRvPlusMinus();
                String choiceString = GBProject.sDirectionsString;
                if (choice != GBProject.sDirections) {
                    choiceString =  GBProject.sPlusMinusString;
                }
                projectString.append(choiceString);
                projectString.append(deliminator);
            }


            if (projDataSrcSwitch.isChecked()) {
                if (projHeadersSwitch.isChecked()) {
                    projectString.append(getString(R.string.exc_switch_proj_data_src));
                    projectString.append(" = ");
                }
                int choice = openProject.getDataSource();
                String choiceString = getString(R.string.unknown_data_source);
                if (choice == GBProject.sDataSourceWGSManual) {
                    choiceString =  getString(R.string.manual_wgs_data_source);
                } else if (choice == GBProject.sDataSourceSPCSManual) {
                    choiceString =  getString(R.string.manual_spcs_data_source);
                } else if (choice == GBProject.sDataSourceUTMManual) {
                    choiceString =  getString(R.string.manual_utm_data_source);
                } else if (choice == GBProject.sDataSourcePhoneGps) {
                    choiceString =  getString(R.string.phone_gps);
                } else if (choice == GBProject.sDataSourceExternalGps) {
                    choiceString =  getString(R.string.external_gps);
                } else if (choice == GBProject.sDataSourceCellTowerTriangulation) {
                    choiceString =  getString(R.string.cell_tower_triangulation);
                }
                projectString.append(choiceString);
                projectString.append(deliminator);
            }


            if (projLocPrecisionSwitch.isChecked()) {
                if (projHeadersSwitch.isChecked()) {
                    projectString.append(getString(R.string.exc_switch_proj_loc_prc));
                    projectString.append(" = ");
                }

                projectString.append(String.valueOf(openProject.getLocPrecision()));
                projectString.append(deliminator);
            }


            if (projStdPrecisionSwitch.isChecked()) {
                if (projHeadersSwitch.isChecked()) {
                    projectString.append(getString(R.string.exc_switch_proj_std_prc));
                    projectString.append(" = ");
                }

                projectString.append(String.valueOf(openProject.getStdDevPrecision()));
                projectString.append(deliminator);
            }

            projectString.append(ls);
            projectString.append(ls);


            //Export the points on the openProject


            //export the list of points
            ArrayList<GBPoint> points = openProject.getPoints();
            GBPoint point;
            int last = points.size();
            int position = 0;

            if (last > 0 ){
                if (projHeadersSwitch.isChecked()) {
                    projectString.append(deliminator);
                    projectString.append(openProject.getProjectName());
                    projectString.append(" has the following ");
                    projectString.append(String.valueOf(last));
                    projectString.append("  points:");
                    projectString.append(deliminator);
                    projectString.append(ls);
                }

            }


            while (position < last){
                point = points.get(position);

                if (pntNumberSwitch.isChecked()) {
                    if (projHeadersSwitch.isChecked()) {
                        projectString.append(ls);
                        projectString.append(getString(R.string.exc_switch_pnt_nb));
                        projectString.append(" = ");
                    }

                    projectString.append(String.valueOf(point.getPointNumber()));
                    projectString.append(deliminator);
                }

                if (pntIsMeanedSwitch.isChecked()) {
                    if (projHeadersSwitch.isChecked()) {
                        projectString.append(getString(R.string.exc_switch_pnt_ismeaned));
                        projectString.append(" = ");
                    }

                    long tokenID = point.getMeanTokenID();
                    String hasMean = "true";
                    if (tokenID == GBUtilities.ID_DOES_NOT_EXIST){
                        hasMean = "false";
                    }
                    projectString.append(hasMean);
                    projectString.append(deliminator);
                }

                if (pntFCSwitch.isChecked()) {
                    if (projHeadersSwitch.isChecked()) {
                        projectString.append(getString(R.string.exc_switch_pnt_fc));
                        projectString.append(" = ");
                    }

                    projectString.append(point.getPointFeatureCode());
                    projectString.append(deliminator);
                }

                if (pntHeightSwitch.isChecked()) {
                    if (projHeadersSwitch.isChecked()) {
                        projectString.append(getString(R.string.exc_switch_pnt_height));
                        projectString.append(" = ");
                    }

                    projectString.append(String.valueOf(point.getHeight()));
                    projectString.append(deliminator);
                }

                if (pntNotesSwitch.isChecked()) {
                    if (projHeadersSwitch.isChecked()) {
                        projectString.append(getString(R.string.exc_switch_pnt_notes));
                        projectString.append(" = ");
                    }

                    projectString.append(point.getPointNotes());
                    projectString.append(deliminator);
                }

                if (pntOffDistSwitch.isChecked()) {
                    if (projHeadersSwitch.isChecked()) {
                        projectString.append(getString(R.string.exc_switch_pnt_off_dist));
                        projectString.append(" = ");
                    }

                    projectString.append(String.valueOf(point.getOffsetDistance()));
                    projectString.append(deliminator);
                }

                if (pntOffHeadSwitch.isChecked()) {
                    if (projHeadersSwitch.isChecked()) {
                        projectString.append(getString(R.string.exc_switch_pnt_off_head));
                        projectString.append(" = ");
                    }

                    projectString.append(String.valueOf(point.getOffsetHeading()));
                    projectString.append(deliminator);
                }

                if (pntOffEleSwitch.isChecked()) {
                    if (projHeadersSwitch.isChecked()) {
                        projectString.append(getString(R.string.exc_switch_pnt_off_elev));
                        projectString.append(" = ");
                    }

                    projectString.append(String.valueOf(point.getOffsetElevation()));
                    projectString.append(deliminator);
                }


                if (pntHdopSwitch.isChecked()) {
                    if (projHeadersSwitch.isChecked()) {
                        projectString.append(getString(R.string.exc_switch_pnt_hdop));
                        projectString.append(" = ");
                    }

                    projectString.append(String.valueOf(point.getHdop()));
                    projectString.append(deliminator);
                }


                if (pntVdopSwitch.isChecked()) {
                    if (projHeadersSwitch.isChecked()) {
                        projectString.append(getString(R.string.exc_switch_pnt_vdop));
                        projectString.append(" = ");
                    }

                    projectString.append(String.valueOf(point.getVdop()));
                    projectString.append(deliminator);
                }


                if (pntPdopSwitch.isChecked()) {
                    if (projHeadersSwitch.isChecked()) {
                        projectString.append(getString(R.string.exc_switch_pnt_pdop));
                        projectString.append(" = ");
                    }

                    projectString.append(String.valueOf(point.getPdop()));
                    projectString.append(deliminator);
                }


                if (pntTdopSwitch.isChecked()) {
                    if (projHeadersSwitch.isChecked()) {
                        projectString.append(getString(R.string.exc_switch_pnt_tdop));
                        projectString.append(" = ");
                    }

                    projectString.append(String.valueOf(point.getTdop()));
                    projectString.append(deliminator);
                }


                if (pntHrmsSwitch.isChecked()) {
                    if (projHeadersSwitch.isChecked()) {
                        projectString.append(getString(R.string.exc_switch_pnt_hrms));
                        projectString.append(" = ");
                    }

                    projectString.append(String.valueOf(point.getHrms()));
                    projectString.append(deliminator);
                }


                if (pntVrmsSwitch.isChecked()) {
                    if (projHeadersSwitch.isChecked()) {
                        projectString.append(getString(R.string.exc_switch_pnt_vrms));
                        projectString.append(" = ");
                    }

                    projectString.append(String.valueOf(point.getVrms()));
                    projectString.append(deliminator);
                }




                position++;

            }//end all points for this project


        } catch (Exception e) {
            e.printStackTrace();
        }
        return projectString.toString();
    }



}
