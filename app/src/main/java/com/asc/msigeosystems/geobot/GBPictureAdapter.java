package com.asc.msigeosystems.geobot;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Elisabeth Huhn on 1/2/17
 *
 * An adapter sits between the Data Model and a list on the UI
 * However, our Data Model classes all have managers: so...
 * The Adapter uses the Project Manager to manage pictures
 */
class GBPictureAdapter extends RecyclerView.Adapter<GBPictureAdapter.MyViewHolder> {

    //Remember, only the ProjectManager may actually CRUD a member of this list
    private List<GBPicture> mPictureList;

    //implement the ViewHolder as an inner class
    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView fileName;

        MyViewHolder(View v) {
            super(v);

            fileName  = (TextView) v.findViewById(R.id.pictureRowFileName);
        }

    } //end inner class MyViewHolder

    //Constructor for GBPictureAdapter
    GBPictureAdapter(List<GBPicture> pictureList){
        this.mPictureList = pictureList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType){

        View itemView = LayoutInflater.from(parent.getContext())
                                           .inflate(R.layout.data_list_row_picture, parent,  false);
        return new MyViewHolder(itemView);

    }

    public void removeItem(int position) {

        //need to figure out whether the picture is on a project or on a point
        GBPicture picture = mPictureList.get(position);
        long projectID = picture.getProjectID();
        long pointID   = picture.getPointID();

        //remove the picture from the list, which removes it from the project or point in memory
        //Pictures are not stored on the project or point side in the DB, only on the picture
        //so there is no need to update the project DB or point DB tables
        mPictureList.remove(position);

        //update the project/picture in the DB without the
        //Remove the picture from teh DB
        if (projectID == GBUtilities.ID_DOES_NOT_EXIST)return;

        //The ProjectManager maintains the in memory container of pictures as well as the
        //DB mirror of pictures. So need to let the ProjectManager be in charge of
        //maintaining the list of pictures. So pass the request on
        //The Project Manager will take care of deleting all sub-objects from the DB as well
        //  as from memory
        GBProjectManager projectManager = GBProjectManager.getInstance();


        if (pointID == GBUtilities.ID_DOES_NOT_EXIST){
            //the picture is on a project
            GBProject project = projectManager.getProject(projectID);
            projectManager.removeProjectPicture(picture.getPictureID(), project);

            //remove the picture from the DB
        } else {
            //the picture is on a point
            GBPointManager   pointManager   = GBPointManager.getInstance();
            GBPoint point = pointManager.getPoint(projectID, pointID);
            projectManager.removePointPicture(picture.getPictureID(), projectID, point);
        }

        notifyItemRemoved(position);
        //this line below gives you the animation and also updates the
        //list items after the deleted item
        notifyItemRangeChanged(position, getItemCount());
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position){
        if (mPictureList != null ) {
            GBPicture picture = mPictureList.get(position);

            String filen = picture.getFileName();

            holder.fileName.setText(filen);
        } else {
            holder.fileName.setText(R.string.no_pictures);
        }

    }

    @Override
    public int getItemCount(){

        int returnValue = 0;

        if (mPictureList != null) {
            returnValue = mPictureList.size();
        }
        return returnValue;
    }

    GBPicture getPicture(int position){
        return mPictureList.get(position);
    }
}
