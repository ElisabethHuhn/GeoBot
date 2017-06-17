package com.asc.msigeosystems.geobot;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.List;

/**
 * Created by Elisabeth Huhn on 5/8/2016.
 *
 * An adapter sits between the Data Model and a list on the UI
 * However, our Data Model classes all have managers: so...
 * The Adapter uses the Project Manager to actually touch a project instance
 */
class GBProjectAdapter extends RecyclerView.Adapter<GBProjectAdapter.MyViewHolder> {

    //Remember, only the ProjectManager may actually CRUD a member of this list
    private List<GBProject> mProjectList;

    //implement the ViewHolder as an inner class
    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView projectName, projectID, projectLastModified, projectSize;

        MyViewHolder(View v) {
            super(v);

            projectName         = (TextView) v.findViewById(R.id.projectRowName);
            projectID           = (TextView) v.findViewById(R.id.projectRowID);
            projectLastModified = (TextView) v.findViewById(R.id.projectRowLastModified);
            projectSize         = (TextView) v.findViewById(R.id.projectRowSize);
        }

    } //end inner class MyViewHolder

    //Constructor for GBProjectAdapter
    GBProjectAdapter(List<GBProject> projectList){
        this.mProjectList = projectList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType){

        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.data_project_list_row, parent,  false);
        return new MyViewHolder(itemView);

    }

    public void removeItem(int position) {
        //The ProjectManager maintains the in memory container of projects as well as the
        //DB mirror of projects. So need to let the ProjectManager be in charge of
        //maintaining the list of current projects. So pass the request on
        //The Project Manager will take care of deleting all subobjects from the DB as well
        //  as from memory
        GBProjectManager projectManager = GBProjectManager.getInstance();
        projectManager.removeProject(position);

        notifyItemRemoved(position);
        //this line below gives you the animation and also updates the
        //list items after the deleted item
        notifyItemRangeChanged(position, getItemCount());
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position){
        if (mProjectList != null ) {
            //Assume the size of the project is the number of points in the project
            GBProject project = mProjectList.get(position);

            GBPointManager pointsManager = GBPointManager.getInstance();
            int numberPoints = pointsManager.getSize(project.getProjectID());

            holder.projectName.        setText(project.getProjectName());
            holder.projectID.          setText(String.valueOf(project.getProjectID()));
            holder.projectLastModified.setText(
                    DateFormat.getDateInstance().format(project.getProjectLastModified()));
            //number of points in this project
            holder.projectSize.        setText(String.valueOf(numberPoints));

        } else {
            holder.projectName.setText(R.string.no_projects);
            holder.projectID.setText("");
            holder.projectLastModified.setText("");

            holder.projectSize.setText("0");
        }

    }

    @Override
    public int getItemCount(){

        int returnValue = 0;

        if (mProjectList != null) {
            returnValue = mProjectList.size();
        }
        return returnValue;
    }
}
