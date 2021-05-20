package pt.tecnico.sec.hdlt.entities;

import pt.tecnico.sec.hdlt.communication.LocationReport;
import pt.tecnico.sec.hdlt.communication.SignedLocationReport;

import java.util.ArrayList;
import java.util.List;

import static pt.tecnico.sec.hdlt.utils.GeneralUtils.F;
import static pt.tecnico.sec.hdlt.utils.GeneralUtils.N_SERVERS;

public class SignedLocationReports {
    private List<SignedLocationReport> signedLocationReports;
    private List<Integer> numberOfOccurrences;

    public SignedLocationReports() {
        this.signedLocationReports = new ArrayList<>();
        this.numberOfOccurrences = new ArrayList<>();
    }

    public synchronized void addLocationReport(SignedLocationReport locationReport) {
        for (int i = 0; i < this.signedLocationReports.size(); i++) {
            if(this.signedLocationReports.get(i).equals(locationReport)){
                this.numberOfOccurrences.set(i, this.numberOfOccurrences.get(i) + 1);
                return;
            }
        }
        this.signedLocationReports.add(locationReport);
        this.numberOfOccurrences.add(1);
    }

    public synchronized SignedLocationReport getBestLocationReport(){
        SignedLocationReport best = null;
        int count = 0;
        for (int i = 0; i < this.signedLocationReports.size(); i++) {
            if(numberOfOccurrences.get(i) > count){
                best = this.signedLocationReports.get(i);
                count = numberOfOccurrences.get(i);
            }
        }

        if(count <= (N_SERVERS + F)/2){
            return null;
        }

        return best;
    }

    public synchronized int numberOfAcks(){
        int ackCount = 0;
        for (Integer aux: numberOfOccurrences){
            ackCount+=aux;
        }
        return ackCount;
    }
}
