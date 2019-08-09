package io.colyseus.serializer.schema;

public class DataChange {
    public String field;
    public Object value;
    public Object previousValue;

    @Override
    public String toString() {
        return field + ": " + previousValue + " --> " + value;
    }
}
