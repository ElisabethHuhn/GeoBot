package com.asc.msigeosystems.geobot;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Elisabeth Huhn on 5/15/2016.
 * patterned after GBProjectAdapter
 * The Adapter manages the connection between
 *  - The data store of Points
 *  - Lists of Point on the UI
 *
 *  Adapters follow a pattern. This adapter follows the pattern
 */
class GBPointAdapter extends RecyclerView.Adapter<GBPointAdapter.MyViewHolder> {

    private ArrayList<GBPoint> mPointList;
    private GBActivity         mActivity;

    //implement the ViewHolder as an inner class
    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView  pointNb, pointEHemi, pointEasting, pointNHemi, pointNorthing,
                  pointElevation, pointFeatureCode;
        TextView  pointLatDeg, pointLatMin, pointLatSec, pointLngDeg, pointLngMin, pointLngSec;

        MyViewHolder(View v) {
            super(v);

            boolean isPM     = GBGeneralSettings.isPM(mActivity);
            boolean isLngLat = GBGeneralSettings.isLngLat(mActivity);
            boolean isEN     = GBGeneralSettings.isEN(mActivity);
            boolean isDD     = GBGeneralSettings.isLocDD(mActivity);
            boolean isDMS    = GBGeneralSettings.isLocDMS(mActivity);

            GBProject openProject = GBUtilities.getInstance().getOpenProject(mActivity);
            int coordType    = GBUtilities.getCoordinateTypeFromProject(openProject);


            pointNb          = (TextView) v.findViewById(R.id.pointRowPointNb);
            pointEHemi       = (TextView) v.findViewById(R.id.pointRowLatHemiLabel);
            pointNHemi       = (TextView) v.findViewById(R.id.pointRowLngHemiLabel);
            pointEasting     = (TextView) v.findViewById(R.id.pointRowEasting);
            pointNorthing    = (TextView) v.findViewById(R.id.pointRowNorthing) ;
            pointElevation   = (TextView) v.findViewById(R.id.pointRowElevation);
            pointFeatureCode = (TextView) v.findViewById(R.id.pointRowFeatureCode);

            pointLatDeg = (TextView) v.findViewById(R.id.pointRowLatitudeDeg);
            pointLatMin = (TextView) v.findViewById(R.id.pointRowLatitudeMin);
            pointLatSec = (TextView) v.findViewById(R.id.pointRowLatitudeSec);
            pointLngDeg = (TextView) v.findViewById(R.id.pointRowLongitudeDeg);
            pointLngMin = (TextView) v.findViewById(R.id.pointRowLongitudeMin);
            pointLngSec = (TextView) v.findViewById(R.id.pointRowLongitudeSec);

            //Determine if the order of the fields must be reversed
            if (((coordType == GBCoordinate.sLLWidgets) && (isLngLat)) ||
                ((coordType == GBCoordinate.sNEWidgets) && (isEN))){
                //reverse the order of the fields on the screen
                pointEasting     = (TextView) v.findViewById(R.id.pointRowNorthing);
                pointNorthing    = (TextView) v.findViewById(R.id.pointRowEasting) ;

                pointLatDeg = (TextView) v.findViewById(R.id.pointRowLongitudeDeg);
                pointLatMin = (TextView) v.findViewById(R.id.pointRowLongitudeMin);
                pointLatSec = (TextView) v.findViewById(R.id.pointRowLongitudeSec);
                pointLngDeg = (TextView) v.findViewById(R.id.pointRowLatitudeDeg);
                pointLngMin = (TextView) v.findViewById(R.id.pointRowLatitudeMin);
                pointLngSec = (TextView) v.findViewById(R.id.pointRowLatitudeSec);
            }

            //Determine which fields must be removed from the screen
            if (coordType == GBCoordinate.sNEWidgets) {
                //no need for hemisphere directions
                pointNHemi.setVisibility(View.GONE);
                pointEHemi.setVisibility(View.GONE);

                //no need for DMS fields
                pointLatDeg.setVisibility(View.GONE);
                pointLatMin.setVisibility(View.GONE);
                pointLatSec.setVisibility(View.GONE);
                pointLngDeg.setVisibility(View.GONE);
                pointLngMin.setVisibility(View.GONE);
                pointLngSec.setVisibility(View.GONE);

            }
            if ((coordType == GBCoordinate.sLLWidgets) && (isPM)){
                //no need for hemisphere directions
                pointNHemi.setVisibility(View.GONE);
                pointEHemi.setVisibility(View.GONE);

            }
            if ((coordType == GBCoordinate.sLLWidgets) && (isDD)){
                //show DD, get rid of DMS
                pointLatDeg.setVisibility(View.GONE);
                pointLatMin.setVisibility(View.GONE);
                pointLatSec.setVisibility(View.GONE);
                pointLngDeg.setVisibility(View.GONE);
                pointLngMin.setVisibility(View.GONE);
                pointLngSec.setVisibility(View.GONE);

            } else if ((coordType == GBCoordinate.sLLWidgets) && (isDMS)) {
                //show DMS, get rid of DD
                pointEasting.setVisibility(View.GONE);
                pointNorthing.setVisibility(View.GONE);
            }
        }

    } //end inner class MyViewHolder

    //Constructor for GBPointAdapter
    GBPointAdapter(GBActivity activity, ArrayList<GBPoint> pointList){
        this.mPointList = pointList;
        this.mActivity  = activity;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.data_list_row_point, parent,  false);
        return new MyViewHolder(itemView);

    }

    ArrayList<GBPoint> getPointList(){
        return mPointList;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position){
        boolean isDD      = GBGeneralSettings.isLocDD(mActivity);
        boolean isDir     = GBGeneralSettings.isDir(mActivity);
        GBProject openProject = GBUtilities.getInstance().getOpenProject(mActivity);
        int coordType    = GBUtilities.getCoordinateTypeFromProject(openProject);
        int distUnits    = openProject.getDistanceUnits();
        int locDigOfPrecision = GBGeneralSettings.getLocPrecision(mActivity);

        String no_coordinate = "No Location";
        if (mPointList == null ) {
            //point list is null

            holder.pointNb.setText("");
            holder.pointFeatureCode.setText("");
            holder.pointEasting.setText(no_coordinate);
            holder.pointNorthing.setText(no_coordinate);
            holder.pointElevation.setText(no_coordinate);
        } else {
            GBPoint point = mPointList.get(position);

            holder.pointNb         .setText(String.valueOf(point.getPointNumber()));
            holder.pointFeatureCode.setText(point.getPointFeatureCode().toString());

            GBCoordinate coordinate = point.getCoordinate();

            if (coordinate == null){
                holder.pointEasting.setText(no_coordinate);
                holder.pointNorthing.setText(no_coordinate);
                holder.pointElevation.setText(no_coordinate);
            }else {

                if (coordType == GBCoordinate.sLLWidgets) {
                    if (isDD) {
                        double latitude = ((GBCoordinateLL) coordinate).getLatitude();
                        double longitude = ((GBCoordinateLL) coordinate).getLongitude();
                        int posHemi = R.string.hemisphere_N;
                        int negHemi = R.string.hemisphere_S;
                        GBUtilities.locDD(mActivity, latitude, locDigOfPrecision,
                                isDir, posHemi, negHemi,
                                holder.pointEHemi, holder.pointEasting);
                        posHemi = R.string.hemisphere_E;
                        negHemi = R.string.hemisphere_W;
                        GBUtilities.locDD(mActivity, longitude, locDigOfPrecision,
                                            isDir, posHemi, negHemi,
                                            holder.pointNHemi, holder.pointNorthing);
                    } else {
                        int deg = ((GBCoordinateLL) coordinate).getLatitudeDegree();
                        int min = ((GBCoordinateLL) coordinate).getLatitudeMinute();
                        double sec = ((GBCoordinateLL) coordinate).getLatitudeSecond();
                        int posHemi = R.string.hemisphere_N;
                        int negHemi = R.string.hemisphere_S;

                        GBUtilities.locDMS(mActivity, deg, min, sec, locDigOfPrecision,
                                        isDir, posHemi, negHemi, holder.pointEHemi,
                                        holder.pointLatDeg, holder.pointLatMin, holder.pointLatSec);

                        deg = ((GBCoordinateLL) coordinate).getLongitudeDegree();
                        min = ((GBCoordinateLL) coordinate).getLongitudeMinute();
                        sec = ((GBCoordinateLL) coordinate).getLongitudeSecond();
                        posHemi = R.string.hemisphere_E;
                        negHemi = R.string.hemisphere_W;

                        GBUtilities.locDMS(mActivity, deg, min, sec, locDigOfPrecision,
                                        isDir, posHemi, negHemi, holder.pointNHemi,
                                        holder.pointLngDeg, holder.pointLngMin, holder.pointLngSec);
                    }
                    double elevation = ((GBCoordinateLL)coordinate).getElevation();
                    GBUtilities.locDistance(mActivity, elevation, holder.pointElevation);

                }else {
                    double easting   = ((GBCoordinateEN)coordinate).getEasting();
                    double northing  = ((GBCoordinateEN)coordinate).getNorthing();
                    double elevation = ((GBCoordinateEN)coordinate).getElevation();

                    GBUtilities.locDistance(mActivity, northing, holder.pointNorthing);
                    GBUtilities.locDistance(mActivity, easting, holder.pointEasting);
                    GBUtilities.locDistance(mActivity, elevation, holder.pointElevation);

                 }
            }
        }
    }

    @Override
    public int getItemCount(){

        int returnValue = 0;

        if (mPointList != null) {
            returnValue = mPointList.size();
        }
        return returnValue;
    }


}
