package edu.aitu.oop3.services;

import edu.aitu.oop3.models.ParkingSpot;
import edu.aitu.oop3.models.Reservation;
import edu.aitu.oop3.models.Vehicle;

import java.util.Date;
import java.util.List;

public interface IReservationService {
    List<ParkingSpot> findFreeSpots();
    Reservation reserveSpot(ParkingSpot spot, Vehicle vehicle, Date startDate);
    void releaseSpot(Reservation reservation);
}
