package com.asc.msigeosystems.geobot;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Elisabeth Huhn on 5/18/2016.
 *
 * Manager for NMEA data. This is a simple manager, as we never commit NMEA to the DB
 */
class GBNmeaManager {

    private static List<GBNmea> sNmeaList;

    private static GBNmeaManager ourInstance ;

    public static GBNmeaManager getInstance() {
        if (ourInstance == null){
            ourInstance = new GBNmeaManager();
        }
        return ourInstance;
    }



    private GBNmeaManager() {

        sNmeaList = new ArrayList<>();

        //for now, no dummy data. We'll build up enough just listening
        //prepareNmeaDataset();
    }

    //returns the list of NMEA sentences
    List<GBNmea> getNmeaList() {
        return sNmeaList;
    }

    void removeListContents() { sNmeaList.clear();}

    void add(GBNmea newNmea){
        sNmeaList.add(newNmea);
    }


    //Mock up some nmea sentences
    private void prepareNmeaDataset() {
        //Nmea sentences come from GPS

        // create some dummy data
        // add some NMEA Sentences
        GBNmea nmea = new GBNmea("$GPRMC,092204.999,A,4250.5589,S,14718.5084,E,0.00,89.68,211200,,*25");

        nmea.setNmeaSentence("$GPGGA,123519,4807.038,N,01131.000,E,1,08,0.9,545.4,M,46.9,M,,*47");
        sNmeaList.add(nmea);

        nmea.setNmeaSentence("$GPGLL,4916.45,N,12311.12,W,225444,A,*1D");
        sNmeaList.add(nmea);

        nmea.setNmeaSentence("$GPRMC,123519,A,4807.038,N,01131.000,E,022.4,084.4,230394,003.1,W*6A");
        sNmeaList.add(nmea);

        nmea.setNmeaSentence("$GPRMC,001225,A,2832.1834,N,08101.0536,W,12,25,251211,1.2,E,A*03");
        sNmeaList.add(nmea);


        nmea.setNmeaSentence("$GPGSA,A,3,04,05,,09,12,,,24,,,,,2.5,1.3,2.1*39");
        sNmeaList.add(nmea);

        nmea.setNmeaSentence("$GPGSV,2,1,08,01,40,083,46,02,17,308,41,12,07,344,39,14,22,228,45*75");
        sNmeaList.add(nmea);

        nmea.setNmeaSentence("$GPVTG,054.7,T,034.4,M,005.5,N,010.2,K*48");
        sNmeaList.add(nmea);



        nmea.setNmeaSentence("$GPGGA,235947.000,0000.0000,N,00000.0000,E,0,00,0.0,0.0,M,,,,0000*00");
        sNmeaList.add(nmea);


        nmea.setNmeaSentence("$GPGGA,092204.999,4250.5589,S,14718.5084,E,1,04,24.4,19.7,M,,,,0000*1F");
        sNmeaList.add(nmea);


        nmea.setNmeaSentence(" $GPGLL,0000.0000,N,00000.0000,E,235947.000,V*2D");
        sNmeaList.add(nmea);


        nmea.setNmeaSentence("$GPGLL,4250.5589,S,14718.5084,E,092204.999,A*2D");
        sNmeaList.add(nmea);


        nmea.setNmeaSentence("$GPGSA,A,1,,,,,,,,,,,,,0.0,0.0,0.0*30");
        sNmeaList.add(nmea);


        nmea.setNmeaSentence("$GPGSA,A,3,01,20,19,13,,,,,,,,,40.4,24.4,32.2*0A");
        sNmeaList.add(nmea);


        nmea.setNmeaSentence("$GPGSV,1,1,01,21,00,000,*4B");
        sNmeaList.add(nmea);

        nmea.setNmeaSentence("$GPGSV,3,1,10,20,78,331,45,01,59,235,47,22,41,069,,13,32,252,45*70\n");
        sNmeaList.add(nmea);

        nmea.setNmeaSentence(" $GPRMC,235947.000,V,0000.0000,N,00000.0000,E,,,041299,,*1D");
        sNmeaList.add(nmea);


    }

}
