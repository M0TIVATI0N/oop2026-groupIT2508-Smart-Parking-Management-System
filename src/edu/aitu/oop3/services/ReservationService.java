package edu.aitu.oop3.services;

import edu.aitu.oop3.exceptions.InvalidVehiclePlateException;
import edu.aitu.oop3.exceptions.NoFreeSpotsException;
import edu.aitu.oop3.exceptions.ReservationStatusException;
import edu.aitu.oop3.models.ParkingSpot;
import edu.aitu.oop3.models.Reservation;
import edu.aitu.oop3.models.Vehicle;
import edu.aitu.oop3.repositories.IParkingSpotRepository;
import edu.aitu.oop3.repositories.IReservationRepository;
import edu.aitu.oop3.repositories.IVehicleRepository;

import java.util.Date;
import java.util.List;

public class ReservationService implements IReservationService {
    private final IParkingSpotRepository parkingSpotRepository;
    private final IReservationRepository reservationRepository;
    private final IVehicleRepository vehicleRepository;

    public ReservationService(IParkingSpotRepository spotRepo, IReservationRepository resRepo, IVehicleRepository vehicleRepository) {
        this.parkingSpotRepository = spotRepo;
        this.reservationRepository = resRepo;
        this.vehicleRepository = vehicleRepository;
    }

    @Override
    public List<ParkingSpot> findFreeSpots() {
        return parkingSpotRepository.findFree();
    }

    @Override
    public Reservation reserveSpot(ParkingSpot spot, Vehicle vehicle, Date startDate) {
        if (vehicle.getLicensePlate() == null || vehicle.getLicensePlate().isEmpty()) {
            throw new InvalidVehiclePlateException("Vehicle plate cannot be empty");
        }

        Vehicle existingVehicle = vehicleRepository.findByPlate(vehicle.getLicensePlate());

        if (existingVehicle == null) {
            existingVehicle = vehicleRepository.add(vehicle);
        }

        if (spot.isReserved()) {
            throw new NoFreeSpotsException("This spot is already taken!");
        }

        final Reservation reservation = new Reservation(0, startDate, null, existingVehicle, spot);

        parkingSpotRepository.updateStatus(spot.getId(), true);

        return reservationRepository.addReservation(reservation);
    }

    @Override
    public void releaseSpot(Reservation reservation) {
        if (reservation.getTo() != null) {
            throw new ReservationStatusException("This reservation has already been closed before");
        }
        reservationRepository.releaseReservation(reservation.getId());
    }
}
