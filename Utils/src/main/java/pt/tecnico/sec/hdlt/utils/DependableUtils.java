package pt.tecnico.sec.hdlt.utils;

import pt.tecnico.sec.hdlt.entities.ReadAck;

import java.util.List;

public class DependableUtils {

    public static Object highestVal(List<ReadAck> readList){
        Object highestValue = null;
        int highestWts = -1;
        for (ReadAck ack: readList){
            if(ack.getWts() > highestWts){
                highestValue = ack.getValue();
            }
        }
        return highestValue;
    }

}
