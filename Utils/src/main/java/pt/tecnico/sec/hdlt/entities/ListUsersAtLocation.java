package pt.tecnico.sec.hdlt.entities;

import pt.tecnico.sec.hdlt.communication.SignedLocationReportList;

import java.util.ArrayList;
import java.util.List;

import static pt.tecnico.sec.hdlt.utils.GeneralUtils.F;
import static pt.tecnico.sec.hdlt.utils.GeneralUtils.N_SERVERS;

public class ListUsersAtLocation {
    private List<SignedLocationReportList> signedLocationReportLists;
    private List<Integer> numberOfOccurrences;

    public ListUsersAtLocation() {
        this.signedLocationReportLists = new ArrayList<>();
        this.numberOfOccurrences = new ArrayList<>();
    }

    public synchronized void addReceivedSignedLocationReport(SignedLocationReportList receivedSignedLocationReportList) {
        for (int i = 0; i < this.signedLocationReportLists.size(); i++) {
            if(this.signedLocationReportLists.get(i).equals(receivedSignedLocationReportList)){
                this.numberOfOccurrences.set(i, this.numberOfOccurrences.get(i) + 1);
                return;
            }
        }
        this.signedLocationReportLists.add(receivedSignedLocationReportList);
        this.numberOfOccurrences.add(1);
    }

    public synchronized SignedLocationReportList getBestSignedLocationReportList(){
        SignedLocationReportList best = null;
        int count = 0;
        for (int i = 0; i < this.signedLocationReportLists.size(); i++) {
            if(numberOfOccurrences.get(i) > count){
                best = this.signedLocationReportLists.get(i);
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
