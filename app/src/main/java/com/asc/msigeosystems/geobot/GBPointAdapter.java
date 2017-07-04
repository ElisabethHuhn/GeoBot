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

    //implement the ViewHolder as an inner class
    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView projectID,    pointID,
                        pointEasting, pointNorthing, pointElevation, pointFeatureCode;

        MyViewHolder(View v) {
            super(v);

            projectID        = (TextView) v.findViewById(R.id.pointRowProjectID);
            pointID          = (TextView) v.findViewById(R.id.pointRowPointID);
            pointEasting     = (TextView) v.findViewById(R.id.pointRowEasting);
            pointNorthing    = (TextView) v.findViewById(R.id.pointRowNorthing) ;
            pointElevation   = (TextView) v.findViewById(R.id.pointRowElevation);
            pointFeatureCode = (TextView) v.findViewById(R.id.pointRowFeatureCode);
        }

    } //end inner class MyViewHolder

    //Constructor for GBPointAdapter
    GBPointAdapter(ArrayList<GBPoint> pointList){
        this.mPointList = pointList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.data_point_list_row, parent,  false);
        return new MyViewHolder(itemView);

    }

    void removeItem(int position, long projectID) {
        GBPoint point = mPointList.get(position);

        //remove the pictures from the point
        GBProjectManager projectManager = GBProjectManager.getInstance();
        projectManager.removePicturesFromPoint(point);

        //Can't maintain the project's point list directly
        //Have to ask the pointManager to do it
        GBPointManager pointsManager = GBPointManager.getInstance();
        pointsManager.removePoint(projectID, point);

        notifyItemRemoved(position); //update the UI


    }

    ArrayList<GBPoint> getPointList(){
        return mPointList;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position){
        String no_coordinate = "No Location";
        if (mPointList != null ) {
            GBPoint point = mPointList.get(position);

            holder.projectID       .setText(String.valueOf(point.getForProjectID()));
            holder.pointID         .setText(String.valueOf(point.getPointID()));
            holder.pointFeatureCode.setText(point.getPointFeatureCode().toString());

            GBCoordinate coordinate = point.getCoordinate();

            if (coordinate == null){
                holder.pointEasting.setText(no_coordinate);
                holder.pointNorthing.setText(no_coordinate);
                holder.pointElevation.setText(no_coordinate);
            }else {
                CharSequence coordinateType = coordinate.getCoordinateType();
                if ((coordinateType.equals(GBCoordinate.sCoordinateTypeWGS84)) ||
                    (coordinateType.equals(GBCoordinate.sCoordinateTypeNAD83))){
                    holder.pointEasting.setText(String.valueOf(((GBCoordinateLL)coordinate).getLatitude()));
                    holder.pointNorthing.setText(String.valueOf(((GBCoordinateLL)coordinate).getLongitude()));
                    holder.pointElevation.setText(String.valueOf(((GBCoordinateLL)coordinate).getElevation()));
                }else {
                    holder.pointEasting.setText(String.valueOf(((GBCoordinateEN)coordinate).getEasting()));
                    holder.pointNorthing.setText(String.valueOf(((GBCoordinateEN)coordinate).getNorthing()));
                    holder.pointElevation.setText(String.valueOf(((GBCoordinateEN)coordinate).getElevation()));
                }
            }

        } else {

            holder.projectID       .setText(R.string.no_points);
            holder.pointID         .setText("");
            holder.pointFeatureCode.setText("");
            holder.pointEasting.setText(no_coordinate);
            holder.pointNorthing.setText(no_coordinate);
            holder.pointElevation.setText(no_coordinate);

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
