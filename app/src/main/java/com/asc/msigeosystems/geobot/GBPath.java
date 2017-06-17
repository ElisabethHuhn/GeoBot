package com.asc.msigeosystems.geobot;

import android.os.Bundle;

/**
 * Created by Elisabeth Huhn on 5/13/2016.
 *
 * This object just keeps track of what path through
 * mainenance we are on:
 * Create, Open. Copy or Delete
 */
class GBPath {

    //Tags for fragment arguments
    static final String sProjectPathTag = "PROJECT_PATH";
    static final String sPointPathTag   = "POINT_PATH";

    //Literals defining the possible paths to be taken
    static final String sOpenTag   = "OPEN";
    static final String sCreateTag = "CREATE";
    static final String sCopyTag   = "COPY";
    static final String sDeleteTag = "DELETE";
    static final String sEditTag   = "EDIT";
    static final String sShowTag   = "SHOW";
    static final String sEditFromMaps = "EDITfromMAPS";

    //stores the path of this instance
    private CharSequence mPath;

    //
    /* **************************************************************/
    /*               Constructor                                 */
    /* **************************************************************/
    GBPath(CharSequence path) {
        this.mPath = path;
    }

    /* **************************************************************/
    /*               Static Methods                                 */
    /* **************************************************************/

    static Bundle putPathInArguments(Bundle args, GBPath projectPath) {

        args.putCharSequence(GBPath.sProjectPathTag, projectPath.getPath());
        return args;
    }


    static GBPath getPathFromArguments(Bundle args) {
        return new GBPath(args.getCharSequence(GBPath.sProjectPathTag));
    }


    /* **************************************************************/
    /*              Setters and Getters                             */
    /* **************************************************************/

        CharSequence getPath()                  { return mPath; }
        void         setPath(CharSequence path) {  mPath = path; }


    /* **************************************************************/
    /*               Member Methods                                 */
    /* **************************************************************/

}
