package com.asc.msigeosystems.geobot;

import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
    private boolean mDdVdms;       //true = Digital Degrees, False = Degrees, Minutes, Seconds
    private int mMetersVfeet;  //0 = Meters, 1 = Feet, 2 = international Feet

    //implement the ViewHolder as an inner class
    class MyViewHolder extends RecyclerView.ViewHolder {

        TextView timeView, validView, fixedView, latDDView, latDegView, latMinView, latSecView;
        TextView lngDDView, lngDegView, lngMinView, lngSecView, eleView, geoidView;

        MyViewHolder(View v) {
            super(v);

            timeView   = (TextView) v.findViewById(R.id.coordinateRowTime);
            validView  = (TextView) v.findViewById(R.id.coordinateRowValid);
            fixedView  = (TextView) v.findViewById(R.id.coordinateRowFixed);
            eleView    = (TextView) v.findViewById(R.id.coordinateRowElevation);
            geoidView  = (TextView) v.findViewById(R.id.coordinateRowGeoid) ;

            if (mDdVdms){
                latDDView  = (TextView) v.findViewById(R.id.coordinateRowLatitude) ;
                lngDDView  = (TextView) v.findViewById(R.id.coordinateRowLongitude) ;

            } else {
                latDegView = (TextView) v.findViewById(R.id.coordinateRowLatitudeDeg);
                latMinView = (TextView) v.findViewById(R.id.coordinateRowLatitudeMin);
                latSecView = (TextView) v.findViewById(R.id.coordinateRowLatitudeSec);

                lngDegView = (TextView) v.findViewById(R.id.coordinateRowLongitudeDeg);
                lngMinView = (TextView) v.findViewById(R.id.coordinateRowLongitudeMin);
                lngSecView = (TextView) v.findViewById(R.id.coordinateRowLongitudeSec);

            }

        }

    } //end inner class MyViewHolder

    //Constructor for GBRawCoordinateAdapter
    GBCoordinateRawAdapter(GBActivity activity,
                           ArrayList<GBCoordinateWGS84> coordinateList,
                           boolean ddVdms,
                           int metersVfeet){
        this.mActivity       = activity;
        this.mCoordinateList = coordinateList;
        this.mDdVdms         = ddVdms;
        this.mMetersVfeet    = metersVfeet;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView;
        if (mDdVdms) {
            itemView = LayoutInflater.from(parent.getContext())
                                .inflate(R.layout.data_list_row_coordinate_raw_dd, parent, false);
        } else {
            itemView = LayoutInflater.from(parent.getContext())
                                .inflate(R.layout.data_list_row_coordinate_raw_dms, parent, false);
        }
        return new MyViewHolder(itemView);

    }

    ArrayList<GBCoordinateWGS84> getCoordinateList(){
        return mCoordinateList;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position){
        if (mCoordinateList == null)return;


        GBCoordinateWGS84 coordinate = mCoordinateList.get(position);

        long timestamp = (long)coordinate.getTime();
        String timeStampString = GBUtilities.getDateTimeString(timestamp);
        holder.timeView .setText(timeStampString);

        CharSequence validString = "F";
        if (coordinate.isValidCoordinate())validString = "T";
        holder.validView.setText(validString.toString());

        CharSequence fixedString = "F";
        if (coordinate.isFixed())fixedString = "T";
        holder.fixedView.setText(fixedString);


        if (mDdVdms){
            double latitude  = coordinate.getLatitude();
            double longitude = coordinate.getLongitude();

            setDD(latitude, holder.latDDView);
            setDD(longitude, holder.lngDDView);

        } else {
            int deg = coordinate.getLatitudeDegree();
            int min = coordinate.getLatitudeMinute();
            double sec = coordinate.getLatitudeSecond();

            setDMS(deg, min, sec, holder.latDegView, holder.latMinView, holder.latSecView);

            deg = coordinate.getLongitudeDegree();
            min = coordinate.getLongitudeMinute();
            sec = coordinate.getLongitudeSecond();

            setDMS(deg, min, sec, holder.lngDegView, holder.lngMinView, holder.lngSecView);
        }
        double elevation = coordinate.getElevation();
        double geoid     = coordinate.getGeoid();


        if (mMetersVfeet == GBProject.sFeet){

            elevation = coordinate.getElevationFeet();
            geoid     = coordinate.getGeoidFeet();

        } else if (mMetersVfeet == GBProject.sIntFeet){ //mMetersVfeet = GBProject.sIntFeet

            elevation = coordinate.getElevationIFeet();
            geoid     = coordinate.getGeoidIFeet();

        }
        String elevationString = String.valueOf(elevation);
        String geoidString     = String.valueOf(geoid);

        holder.eleView.setText(elevationString);
        holder.geoidView.setText(geoidString);

    }

    @Override
    public int getItemCount(){

        int returnValue = 0;

        if (mCoordinateList != null) {
            returnValue = mCoordinateList.size();
        }
        return returnValue;
    }


    //Conversion for UI DD to DMS fields
    //last parameter indicates whether latitude (true) or longitude (false)

    boolean convertDDtoDMS(GBActivity activity,
                           double  tude,
                           TextView tudeDInput,
                           TextView tudeMInput,
                           TextView tudeSInput,
                           boolean  isLatitude) {

        String tudeString;
        if (tude == 0d){
            tudeString = activity.getString(R.string.zero_decimal_string);
        } else {
            tudeString = String.valueOf(tude);
        }


        //The user inputs have to be within range to be
        if (   (isLatitude   && ((tude < -90.0) || (tude >= 90.0)))  || //Latitude
             ((!isLatitude)  && ((tude < -180.) || (tude >= 180.)))) {  //Longitude

            tudeDInput.setText(R.string.zero_decimal_string);
            tudeMInput.setText(R.string.zero_decimal_string);
            tudeSInput.setText(R.string.zero_decimal_string);
            return false;
        }

        //check sign of tude
        boolean isTudePos = true;
        int tudeColor= R.color.colorPosNumber;
        if (tude < 0) {
            //tude is negative, remember this and work with the absolute value
            tude = Math.abs(tude);
            isTudePos = false;
            tudeColor = R.color.colorNegNumber;
        }

        //strip out the decimal parts of tude
        int tudeDegree = (int) tude;

        double degree = tudeDegree;

        //digital degrees minus degrees will be decimal minutes plus seconds
        //converting to int strips out the seconds
        double minuteSec = tude - degree;
        double minutes = minuteSec * 60.;
        int tudeMinute = (int) minutes;
        double minuteOnly = (double) tudeMinute;

        //start with the DD, subtract out Degrees, subtract out Minutes
        //convert the remainder into whole seconds
        double tudeSecond = (tude - degree - (minuteOnly / 60.)) * (60. * 60.);
        //tudeSecond = (tude - minutes) * (60. *60.);

        //If tude was negative before, restore it to negative
        if (!isTudePos) {
            //tude       = 0. - tude;
            tudeDegree = 0 - tudeDegree;
            tudeMinute = 0 - tudeMinute;
            tudeSecond = 0. - tudeSecond;
        }

        //truncate to a reasonable number of decimal digits
        BigDecimal bd = new BigDecimal(tudeSecond).setScale(GBUtilities.sMicrometerDigitsOfPrecision,
                RoundingMode.HALF_UP);
        tudeSecond = bd.doubleValue();

        //show the user the result
        tudeDInput.setText(String.valueOf(tudeDegree));
        tudeMInput.setText(String.valueOf(tudeMinute));
        tudeSInput.setText(String.valueOf(tudeSecond));


        tudeDInput .setTextColor(ContextCompat.getColor(activity, tudeColor));
        tudeMInput .setTextColor(ContextCompat.getColor(activity, tudeColor));
        tudeSInput .setTextColor(ContextCompat.getColor(activity, tudeColor));

        return true;
    }

    //Sets color and value for Digital Degree fields
    void setDD(double tude, TextView tudeView){
        String tudeString = String.valueOf(tude);
        int tudeColor= R.color.colorPosNumber;
        if (tude < 0) {
            tudeColor = R.color.colorNegNumber;
        }
        tudeView.setTextColor(ContextCompat.getColor(mActivity, tudeColor));
        tudeView.setText(tudeString);
    }

    void setDMS(int d, int m, double s, TextView dView, TextView mView, TextView sView){

        int tudeColor= R.color.colorPosNumber;
        if ((d < 0) || (m < 0) || (s < 0)) {
            tudeColor = R.color.colorNegNumber;
        }
        dView.setTextColor(ContextCompat.getColor(mActivity, tudeColor));
        dView.setText(String.valueOf(d));
        mView.setTextColor(ContextCompat.getColor(mActivity, tudeColor));
        mView.setText(String.valueOf(m));
        sView.setTextColor(ContextCompat.getColor(mActivity, tudeColor));
        sView.setText(String.valueOf(s));
    }




}
