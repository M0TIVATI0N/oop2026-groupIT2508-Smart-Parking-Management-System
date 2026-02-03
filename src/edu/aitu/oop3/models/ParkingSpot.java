package edu.aitu.oop3.models;

public class ParkingSpot {
    private final int id;
    private final Tariff tariff;
    private final boolean isReserved;
    private final String type;

    public ParkingSpot(int id, Tariff tariff, boolean isReserved, String type) {
        this.id = id;
        this.tariff = tariff;
       this.isReserved = isReserved;
        this.type = type;
    }

    public int getId() {
        return id;
    }
    public Tariff getTariff() {
        return tariff;
    }
    public boolean isReserved() {
        return isReserved;
    }
    public String getType() { return type; }
}
