package pt.tecnico.sec.hdlt.utils;

import java.util.Calendar;
import java.util.TimeZone;

public class GeneralUtils {
    private static final long EPOCH_SIZE = 604800; // 60*60*24*7 = 1 week
    public static final int F = 1;
    public static final int N_SERVERS = (3 * F) + 1; // 4
    public static final int SERVER_START_PORT = 50050;
    public static final String SERVER_HOST = "localhost";


    public static long getCurrentEpoch(){
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        return Math.floorDiv(calendar.getTimeInMillis() / 1000, EPOCH_SIZE);
    }
}
