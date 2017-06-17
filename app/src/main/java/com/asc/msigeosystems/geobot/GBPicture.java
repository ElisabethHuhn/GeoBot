package com.asc.msigeosystems.geobot;

/**
 * Created by Elisabeth Huhn on 5/15/2016.
 *
 * One of the principal Data Classes of the model
 * A Point is contained within one and only one project
 */
class GBPicture {

    /*-***************************************************/
    /*-******    Static constants                *********/
    /*-***************************************************/




    /*-***********************************/
    /*    Static (class) Variables       */
    /*-***********************************/


    /*-***********************************/
    /*    Member (instance) Variables    */
    /*-***********************************/

    /*-***************************************************/
    /*-******    Attributes stored in the DB     *********/
    /*-***************************************************/

    private String       mPictureID;
    private long         mProjectID;
    private long         mPointID;
    //Pathname is the directory plus file. This is all you need to determine the file
    private String       mPathName;
    //The file name is within the directory that is named after the project
    private String       mFileName;


    /*-**************************************************************/
    /*               Static Methods                                 */
    /*-**************************************************************/



    //Convert point to comma delimited file for exchange
    String convertToCDF() {
        return String.valueOf(this.getProjectID()) + ", " +
               String.valueOf(this.getPointID() )  + ", " +
                this.getPathName()                 + ", " +
                this.getFileName()                 + ", " +

                System.getProperty("line.separator");
    }

    /*-***********************************/
    /*         CONSTRUCTORS              */
    /*-***********************************/

    /*-***************************************************/
    /*-******    Constructors                    *********/
    /*-***************************************************/
    GBPicture(){
        initializeDefaultVariables();
    }


    GBPicture(String timestamp, GBProject project, GBPoint point) {

        initializeDefaultVariables();
        //initialize all variables so we are assured that none are null
        //that way we never have to check for null later
        this.mPictureID = timestamp;
        this.mProjectID = project.getProjectID();
        if (point == null){
            this.mPointID = 0;
        } else {
            this.mPointID = point.getPointID();
        }
    }


    /*-***************************************************/
    /*-******    Setters and Getters             *********/
    /*-***************************************************/

    String getPictureID() {
        return mPictureID;
    }

    void setPictureID(String pictureID) {
        mPictureID = pictureID;
    }


    long getProjectID()               {return mProjectID;}
    void setProjectID(long projectID) {
        mProjectID = projectID;
    }

    long getPointID()                 {return mPointID;}
    void setPointID(long pointID) {
        mPointID = pointID;
    }

    String getPathName() {
        return mPathName;
    }
    void setPathName(String pathName) {
        mPathName = pathName;
    }

    String getFileName() {
        return mFileName;
    }
    void setFileName(String fileName) {
        mFileName = fileName;
    }

    /*-***************************************************/
    /*-******    Private Member Methods          *********/
    /*-***************************************************/

    private void initializeDefaultVariables(){
        this.mPictureID  = ";";
        this.mProjectID  = 0;
        this.mPointID    = 0;
        this.mPathName   = "";
        this.mFileName   = "";
    }



}
