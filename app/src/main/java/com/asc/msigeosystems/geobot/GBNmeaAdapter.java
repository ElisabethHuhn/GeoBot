package com.asc.msigeosystems.geobot;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Elisabeth Huhn on 5/15/2016.
 * patterned after GBProjectAdapter
 */
class GBNmeaAdapter extends RecyclerView.Adapter<GBNmeaAdapter.MyViewHolder> {

    private List<GBNmea> mNmeaList;

    //implement the ViewHolder as an inner class
    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView nmeaSentence;

        MyViewHolder(View v) {
            super(v);

            nmeaSentence = (TextView) v.findViewById(R.id.nmeaRowSentence);
        }

    } //end inner class MyViewHolder

    //Constructor for GBNmeaAdapter
    GBNmeaAdapter(List<GBNmea> nmeaList){
        this.mNmeaList = nmeaList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.data_nmea_list_row, parent,  false);

        return new MyViewHolder(itemView);
    }

    //Usually only the itemManager can control what is in the list, but in this case,
    //The NMEA data is not stored to the DB, so we can add it here
    void addItem(GBNmea nemaSentence){
        mNmeaList.add(nemaSentence);
        //notifyDataSetChanged();
        notifyItemInserted(mNmeaList.size()-1);
        notifyItemRangeChanged(getItemCount(), getItemCount());
    }

    void removeItem(int position) {
        //If there ever is a NmeaManager, pass this request on to the manager
        mNmeaList.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position){
        if (mNmeaList != null ) {
            GBNmea nmea = mNmeaList.get(position);
            holder.nmeaSentence.setText(String.valueOf(nmea.getNmeaSentence()));
        } else {
            holder.nmeaSentence.setText(R.string.skyplot_no_nema_found);
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

