package pt.tecnico.sec.hdlt.server.entities;

import java.util.Objects;

public class LocationReportKey {

    private final int userId;
    private final long epoch;

    public LocationReportKey(int userId, long epoch) {
        this.userId = userId;
        this.epoch = epoch;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LocationReportKey that = (LocationReportKey) o;
        return userId == that.userId && epoch == that.epoch;
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, epoch);
    }
}
