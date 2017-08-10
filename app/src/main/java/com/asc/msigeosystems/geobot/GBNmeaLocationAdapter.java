package com.asc.msigeosystems.geobot;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Elisabeth Huhn on 7/29/17
 * patterned after GBNmeaAdapter
 * Displays the locations within the NMEA Sentence
 */
class GBNmeaLocationAdapter extends RecyclerView.Adapter<GBNmeaLocationAdapter.MyViewHolder> {

    private List<GBNmea> mNmeaList;

    //implement the ViewHolder as an inner class
    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView typeView, latitudeView, longitudeView;

        MyViewHolder(View v) {
            super(v);

            typeView = (TextView) v.findViewById(R.id.nmeaTypeView);
            latitudeView = (TextView) v.findViewById(R.id.nmeaLatitudeView);
            longitudeView = (TextView) v.findViewById(R.id.nmeaLongitudeView);
         }

    } //end inner class MyViewHolder

    //Constructor for GBNmeaAdapter
    GBNmeaLocationAdapter(List<GBNmea> nmeaList){
        this.mNmeaList = nmeaList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.data_list_row_nmea_location, parent,  false);

        return new MyViewHolder(itemView);
    }


    @Override
    public void onBindViewHolder(MyViewHolder holder, int position){
        if (mNmeaList != null ) {
            GBNmea nmea = mNmeaList.get(position);
            holder.typeView     .setText(nmea.getNmeaType());
            holder.latitudeView .setText(String.valueOf(nmea.getLatitude()));
            holder.longitudeView.setText(String.valueOf(nmea.getLongitude()));
        } else {
            holder.typeView.setText(R.string.skyplot_no_nema_found);
        }

    }
    @Override
    public int getItemCount(){

        int returnValue = 0;

        if (mNmeaList != null) {
            returnValue = mNmeaList.size();
        }
        return returnValue;
    }


}

