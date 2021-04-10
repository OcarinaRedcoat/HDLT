package pt.tecnico.sec.hdlt.client.utils;

import java.util.Calendar;
import java.util.TimeZone;

public class GeneralUtils {
    private static final long EPOCH_SIZE = 604800; // 60*60*24*7 = 1 week

    public static long getCurrentEpoch(){
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        return Math.floorDiv(calendar.getTimeInMillis() / 1000, EPOCH_SIZE);
    }
}
