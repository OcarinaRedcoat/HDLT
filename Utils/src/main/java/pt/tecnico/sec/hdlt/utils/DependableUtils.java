package pt.tecnico.sec.hdlt.utils;

import pt.tecnico.sec.hdlt.communication.LocationReport;
import pt.tecnico.sec.hdlt.communication.Proofs;
import pt.tecnico.sec.hdlt.entities.ReadAck;

import java.util.List;

public class DependableUtils {

    public static ReadAck highestAck(List<ReadAck> readList){
        ReadAck highestValue = null;
        int highestWts = -1;
        for (ReadAck ack: readList){
            if(ack.getWts() > highestWts){
                highestValue = ack;
            }
        }
        return highestValue;
    }

    public static Proofs biggestProofsList(List<ReadAck> readList){
        Proofs proofs = null;
        int highestCount = 0;
        for (ReadAck ack: readList){
            if(((Proofs) ack.getValue()).getLocationProofCount() > highestCount){
                proofs = (Proofs) ack.getValue();
            }
        }
        return proofs;
    }

}
