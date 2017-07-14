package com.asc.msigeosystems.geobot;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;


class GBSatelliteAdapter extends RecyclerView.Adapter<GBSatelliteAdapter.MyViewHolder> {

    private List<GBSatellite> mSatelliteList;

    //implement the ViewHolder as an inner class
    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView satelliteElevation, satelliteID, satelliteAzimuth, satelliteSignalToNoise;

        MyViewHolder(View v) {
            super(v);

            satelliteID            = (TextView) v.findViewById(R.id.satelliteRowID);
            satelliteElevation     = (TextView) v.findViewById(R.id.satelliteRowElevation);
            satelliteAzimuth       = (TextView) v.findViewById(R.id.satelliteRowAzimuth);
            satelliteSignalToNoise = (TextView) v.findViewById(R.id.satelliteRowSignalToNoise);
        }

    } //end inner class MyViewHolder

    //Constructor for GBSatelliteAdapter
    GBSatelliteAdapter(List<GBSatellite> satelliteList){
        this.mSatelliteList = satelliteList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType){

        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.data_list_row_satellite, parent,  false);
        return new MyViewHolder(itemView);

    }

    public void removeItem(int position) {
        //This list is used locally as well as in the satellite container,
        // so we only have to remove it once
        mSatelliteList.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position){
        if (mSatelliteList != null ) {
            GBSatellite satellite = mSatelliteList.get(position);

            holder.satelliteID.       setText(String.valueOf(satellite.getSatelliteID()));
            holder.satelliteElevation.setText(String.valueOf(satellite.getElevation()));
            holder.satelliteAzimuth.  setText(String.valueOf(satellite.getAzimuth()));
            holder.satelliteSignalToNoise.setText(String.valueOf(satellite.getSnr()));

        } else {
            holder.satelliteID.       setText("No satellites defined");
            holder.satelliteElevation.setText("");
            holder.satelliteAzimuth.  setText("");
            holder.satelliteSignalToNoise.setText("");
        }

    }
    @Override
    public int getItemCount(){

        int returnValue = 0;

        if (mSatelliteList != null) {
            returnValue = mSatelliteList.size();
        }
        return returnValue;
    }
}
