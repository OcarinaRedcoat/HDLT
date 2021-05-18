package pt.tecnico.sec.hdlt.entities;

import pt.tecnico.sec.hdlt.communication.LocationReport;
import pt.tecnico.sec.hdlt.communication.Proofs;
import pt.tecnico.sec.hdlt.communication.SignedLocationProof;

import java.util.ArrayList;
import java.util.List;

import static pt.tecnico.sec.hdlt.utils.GeneralUtils.F;
import static pt.tecnico.sec.hdlt.utils.GeneralUtils.N_SERVERS;

public class ListOfReceivedMyProofs {
    private List<Proofs> listOfReceivedProofs;
    private List<Integer> numberOfOccurrences;

    public ListOfReceivedMyProofs() {
        this.listOfReceivedProofs = new ArrayList<>();
        this.numberOfOccurrences = new ArrayList<>();
    }

    public void addReceivedProofs(Proofs receivedMyProofs) {
        for (int i = 0; i < this.listOfReceivedProofs.size(); i++) {
            if(this.listOfReceivedProofs.get(i).equals(receivedMyProofs)){
                this.numberOfOccurrences.set(i, this.numberOfOccurrences.get(i) + 1);
                return;
            }
        }
        this.listOfReceivedProofs.add(receivedMyProofs);
        this.numberOfOccurrences.add(1);
    }

    public Proofs getBestProofs(){
        Proofs best = null;
        int count = 0;
        for (int i = 0; i < this.listOfReceivedProofs.size(); i++) {
            if(numberOfOccurrences.get(i) > count){
                best = this.listOfReceivedProofs.get(i);
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
