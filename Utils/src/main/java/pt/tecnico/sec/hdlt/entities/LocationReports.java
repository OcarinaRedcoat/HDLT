package pt.tecnico.sec.hdlt.entities;

import pt.tecnico.sec.hdlt.communication.LocationReport;

import java.util.ArrayList;
import java.util.List;

import static pt.tecnico.sec.hdlt.utils.GeneralUtils.F;
import static pt.tecnico.sec.hdlt.utils.GeneralUtils.N_SERVERS;

public class LocationReports {
    private List<LocationReport> locationReports;
    private List<Integer> numberOfOccurrences;

    public LocationReports() {
        this.locationReports = new ArrayList<>();
        this.numberOfOccurrences = new ArrayList<>();
    }

    public void addLocationReport(LocationReport locationReport) {
        for (int i = 0; i < this.locationReports.size(); i++) {
            if(this.locationReports.get(i).equals(locationReport)){
                this.numberOfOccurrences.set(i, this.numberOfOccurrences.get(i) + 1);
                return;
            }
        }
        this.locationReports.add(locationReport);
        this.numberOfOccurrences.add(1);
    }

    public LocationReport getBestLocationReport(){
        LocationReport best = null;
        int count = 0;
        for (int i = 0; i < this.locationReports.size(); i++) {
            if(numberOfOccurrences.get(i) > count){
                best = this.locationReports.get(i);
                count = numberOfOccurrences.get(i);
            }
        }

        if(count <= (N_SERVERS + F)/2){
            return null;
        }

        return best;
    }

    public int numberOfAcks(){
        int ackCount = 0;
        for (Integer aux: numberOfOccurrences){
            ackCount+=aux;
        }
        return ackCount;
    }
}
