package com.asc.msigeosystems.geobot;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Elisabeth Huhn on 7/8/2017
 * patterned after GBPointAdapter
 * The Adapter manages the connection between
 *  - The ArrayList of raw coordinates on the MeanToken
 *  - Lists of raw WGS Coordinate Locations on the UI
 *
 *  Adapters follow a pattern. This adapter follows the pattern
 */
class GBCoordinateRawAdapter extends RecyclerView.Adapter<GBCoordinateRawAdapter.MyViewHolder> {


    private ArrayList<GBCoordinateWGS84> mCoordinateList;
    private GBActivity mActivity;
    private boolean mIsDD;       //true = Digital Degrees, False = Degrees, Minutes, Seconds
    private int mMetersVfeet;  //0 = Meters, 1 = Feet, 2 = international Feet
    private boolean mIsDir;   //true Dir field determines sign of location


    //implement the ViewHolder as an inner class
    class MyViewHolder extends RecyclerView.ViewHolder {

        TextView timeView, validView, fixedView, latDDView, latDegView, latMinView, latSecView;
        TextView lngDDView, lngDegView, lngMinView, lngSecView, eleView, geoidView;
        TextView latDirView, lngDirView;

        MyViewHolder(View v) {
            super(v);

            timeView   = (TextView) v.findViewById(R.id.coordinateRowTime);
            validView  = (TextView) v.findViewById(R.id.coordinateRowValid);
            fixedView  = (TextView) v.findViewById(R.id.coordinateRowFixed);
            eleView    = (TextView) v.findViewById(R.id.coordinateRowElevation);
            geoidView  = (TextView) v.findViewById(R.id.coordinateRowGeoid) ;

            latDirView = (TextView) v.findViewById(R.id.coordinateRowLatDir);
            lngDirView = (TextView) v.findViewById(R.id.coordinateRowLngDir);

            latDDView  = (TextView) v.findViewById(R.id.coordinateRowLatitude) ;
            lngDDView  = (TextView) v.findViewById(R.id.coordinateRowLongitude) ;

            latDegView = (TextView) v.findViewById(R.id.coordinateRowLatitudeDeg);
            latMinView = (TextView) v.findViewById(R.id.coordinateRowLatitudeMin);
            latSecView = (TextView) v.findViewById(R.id.coordinateRowLatitudeSec);

            lngDegView = (TextView) v.findViewById(R.id.coordinateRowLongitudeDeg);
            lngMinView = (TextView) v.findViewById(R.id.coordinateRowLongitudeMin);
            lngSecView = (TextView) v.findViewById(R.id.coordinateRowLongitudeSec);

            if (!mIsDir){
                latDirView.setVisibility(View.GONE);
                lngDirView.setVisibility(View.GONE);
            }
             if (mIsDD){

                latDegView.setVisibility(View.GONE);
                latMinView.setVisibility(View.GONE);
                latSecView.setVisibility(View.GONE);

                lngDegView.setVisibility(View.GONE);
                lngMinView.setVisibility(View.GONE);
                lngSecView.setVisibility(View.GONE);

            } else {
                latDDView.setVisibility(View.GONE);
                lngDDView.setVisibility(View.GONE);

             }

        }

    } //end inner class MyViewHolder

    //Constructor for GBRawCoordinateAdapter
    GBCoordinateRawAdapter(GBActivity activity,
                           ArrayList<GBCoordinateWGS84> coordinateList,
                           boolean ddVdms, boolean isDir,
                           int metersVfeet){
        this.mActivity       = activity;
        this.mCoordinateList = coordinateList;
        this.mIsDD = ddVdms;
        this.mIsDir          = isDir;
        this.mMetersVfeet    = metersVfeet;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView;
        itemView = LayoutInflater.from(parent.getContext())
                                    .inflate(R.layout.data_list_row_coordinate_raw, parent, false);

        return new MyViewHolder(itemView);

    }

    ArrayList<GBCoordinateWGS84> getCoordinateList(){
        return mCoordinateList;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position){
        if (mCoordinateList == null)return;


        GBCoordinateWGS84 coordinate = mCoordinateList.get(position);

        long timestamp = coordinate.getTime();
        String timeStampString = GBUtilities.getDateTimeString(timestamp);
        holder.timeView .setText(timeStampString);

        CharSequence validString = "F";
        if (coordinate.isValidCoordinate())validString = "T";
        holder.validView.setText(validString.toString());

        CharSequence fixedString = "F";
        if (coordinate.isFixed())fixedString = "T";
        holder.fixedView.setText(fixedString);


        int locDigOfPrecision = GBGeneralSettings.getLocPrecision(mActivity);
        if (mIsDD){
            double latitude  = coordinate.getLatitude();
            double longitude = coordinate.getLongitude();
            int posHemi = R.string.hemisphere_N;
            int negHemi = R.string.hemisphere_S;
            GBUtilities.locDD(mActivity, latitude, locDigOfPrecision,
                              mIsDir, posHemi, negHemi, holder.latDirView, holder.latDDView);
            posHemi = R.string.hemisphere_E;
            negHemi = R.string.hemisphere_W;
            GBUtilities.locDD(mActivity, longitude, locDigOfPrecision,
                              mIsDir, posHemi, negHemi, holder.lngDirView, holder.lngDDView);


        } else {
            int deg = coordinate.getLatitudeDegree();
            int min = coordinate.getLatitudeMinute();
            double sec = coordinate.getLatitudeSecond();
            int posHemi = R.string.hemisphere_N;
            int negHemi = R.string.hemisphere_S;

            GBUtilities.locDMS(mActivity, deg, min, sec, locDigOfPrecision,
                                mIsDir, posHemi, negHemi, holder.latDirView,
                                holder.latDegView, holder.latMinView, holder.latSecView);

            deg = coordinate.getLongitudeDegree();
            min = coordinate.getLongitudeMinute();
            sec = coordinate.getLongitudeSecond();
            posHemi = R.string.hemisphere_E;
            negHemi = R.string.hemisphere_W;

            GBUtilities.locDMS(mActivity, deg, min, sec, locDigOfPrecision,
                                mIsDir, posHemi, negHemi, holder.latDirView,
                                holder.lngDegView, holder.lngMinView, holder.lngSecView);

        }
        double elevation = coordinate.getElevation();
        double geoid     = coordinate.getGeoid();

        GBUtilities.locDistance(mActivity, elevation, holder.eleView);
        GBUtilities.locDistance(mActivity, geoid, holder.geoidView);


    }

    @Override
    public int getItemCount(){

        int returnValue = 0;

        if (mCoordinateList != null) {
            returnValue = mCoordinateList.size();
        }
        return returnValue;
    }


}
