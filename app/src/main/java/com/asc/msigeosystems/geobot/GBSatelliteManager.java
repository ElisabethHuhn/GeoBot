package com.asc.msigeosystems.geobot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Elisabeth Huhn on 5/18/2016.
 * This is a departure from most managers as it doesn't hide the access of data objects from the DB
 * as satelites are never saved to the DB
 * However, this manager does maintain a list of satellites,
 * which is consistent with other managers in the package
 */
class GBSatelliteManager {

    private static List<GBSatellite> sSatelliteList;

    private static GBSatelliteManager ourInstance ;

    public static GBSatelliteManager getInstance() {
        if (ourInstance == null){
            ourInstance = new GBSatelliteManager();
        }
        return ourInstance;
    }



    private GBSatelliteManager() {

        sSatelliteList = new ArrayList<>();

       //Get the satellite data from GPS
        //prepareSatelliteDataset();
    }


    public List<GBSatellite> getSatellites() {
        return sSatelliteList;
    }


    public GBSatellite getSatellite(int satelliteID) {
        for (GBSatellite satellite : sSatelliteList){
            if (satellite.getSatelliteID() == satelliteID){
                return satellite;
            }
        }
        return null;
    }


    //add a satellite to the list,
    // if it isn't already there, add it
    // if it was previously in the list,
    //       remove the old version before adding the new one
    public void add (GBSatellite satellite) {
        //if the given satellite is already in the list, remove the old one
        boolean foundIt = true;
        //remove any old satellites with this id from the list
        while (foundIt = removeSat(satellite)){}

        sSatelliteList.add(satellite);

        //Then sort the list according to satellite ID
        Collections.sort(sSatelliteList, new GBSatelliteComparator());


    }

    //returns true if match found and removed from the list
    private boolean removeSat (GBSatellite satellite) {
        GBSatellite satellite1;

        for (int i = 0; i < sSatelliteList.size(); i++) {

            satellite1 = sSatelliteList.get(i);

            if ((satellite1 != null) &&
                (satellite1.getSatelliteID() == satellite.getSatelliteID())) {
                sSatelliteList.remove(i);
                return true;

            }
        }
        return false;
    }


    //Mock up some satellites for now
    private void prepareSatelliteDataset(){
        //format of the constructor
        //GBSatellite(int satelliteID, int elevation, int azimuth, int snr)

        GBSatellite satellite = new GBSatellite(01,47,261,20);
        sSatelliteList.add(satellite);

        satellite = new GBSatellite(03,07,036,14);
        sSatelliteList.add(satellite);

        satellite = new GBSatellite(06,69,000,23);
        sSatelliteList.add(satellite);

        satellite = new GBSatellite(12,29,316,18);
        sSatelliteList.add(satellite);

        satellite = new GBSatellite(17,43,075,23);
        sSatelliteList.add(satellite);

        satellite = new GBSatellite(19,59,040,27);
        sSatelliteList.add(satellite);







    }

}
