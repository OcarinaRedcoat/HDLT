package pt.tecnico.sec.hdlt.entities;

public class ReadAck {
    private int wts;
    private Object value;

    public ReadAck(int wts, Object value) {
        this.wts = wts;
        this.value = value;
    }

    public int getWts() {
        return wts;
    }

    public void setWts(int wts) {
        this.wts = wts;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}
