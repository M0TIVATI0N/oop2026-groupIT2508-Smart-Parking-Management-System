package edu.aitu.oop3;

import edu.aitu.oop3.data.*;
import edu.aitu.oop3.repositories.*;
import edu.aitu.oop3.services.*;
import edu.aitu.oop3.models.*;
import edu.aitu.oop3.exceptions.*;

import java.util.Scanner;
import java.util.Date;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        IDB db = PostgresDB.getInstance();

        IParkingSpotRepository spotRepo = new ParkingSpotRepository(db);
        IReservationRepository resRepo = new ReservationRepository(db);
        ITariffRepository tariffRepo = new TariffRepository(db);
        IVehicleRepository vehicleRepo = new VehicleRepository(db);

        IReservationService reservationService = new ReservationService(spotRepo, resRepo, vehicleRepo);
        IPricingService pricingService = new PricingService(resRepo, tariffRepo);

        ParkingLotManager parkingManager = ParkingLotManager.getInstance(reservationService);

        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("\nPARKING MANAGEMENT SYSTEM 2");
            System.out.println("1. Show free parking spots");
            System.out.println("2. Reserve a parking spot");
            System.out.println("3. Leave and pay");
            System.out.println("4. Show all tariffs");
            System.out.println("0. Exit");
            System.out.print("Choose an option: ");

            int choice = -1;
            try {
                choice = scanner.nextInt();
                scanner.nextLine();
            } catch (Exception e) {
                scanner.nextLine();
                System.out.println("Invalid input. Please enter a number.");
                continue;
            }

            try {
                switch (choice) {
                    case 1:
                        parkingManager.displayFreeSpots();
                        break;
                    case 2:
                        reserveSpot(scanner, reservationService, spotRepo);
                        break;
                    case 3:
                        releaseAndPay(scanner, reservationService, pricingService, resRepo, spotRepo);
                        break;
                    case 4:
                        showAllTariffs(tariffRepo);
                        break;
                    case 0:
                        System.out.println("Exiting...");
                        scanner.close();
                        return;
                    default:
                        System.out.println("Invalid choice.");
                }
            } catch (NoFreeSpotsException | InvalidVehiclePlateException | ReservationStatusException e) {
                System.out.println("Business Error: " + e.getMessage());
            } catch (Exception e) {
                System.out.println("Unexpected error: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private static void reserveSpot(Scanner scanner, IReservationService reservationService,
                                    IParkingSpotRepository spotRepo) {
        System.out.print("Enter vehicle plate number: ");
        String plate = scanner.nextLine().trim();

        System.out.print("Enter parking spot ID: ");
        int spotId = scanner.nextInt();
        scanner.nextLine();

        ParkingSpot spot = spotRepo.getById(spotId);

        if (spot == null) {
            System.out.println("Spot not found.");
            return;
        }

        Vehicle vehicle = new Vehicle(0, plate);
        Reservation res = reservationService.reserveSpot(spot, vehicle, new Date());
        System.out.println("Success! Reservation ID: " + res.getId());
    }

    private static void releaseAndPay(Scanner scanner, IReservationService reservationService,
                                      IPricingService pricingService, IReservationRepository resRepo,
                                      IParkingSpotRepository spotRepo) {
        System.out.print("Enter reservation ID: ");
        int resId = scanner.nextInt();
        scanner.nextLine();
        Reservation res = resRepo.findById(resId);
        if (res == null || res.getTo() != null) {
            System.out.println("Invalid or already closed reservation.");
            return;
        }

        reservationService.releaseSpot(res);
        spotRepo.updateSpotStatus(res.getSpot().getId(), false);

        res = resRepo.findById(resId);

        int cost = pricingService.calculateCost(res);

        long diffInMs = res.getTo().getTime() - res.getFrom().getTime();
        int minutes = (int) (diffInMs / (1000 * 60));
        if (minutes <= 0) minutes = 1;

        Invoice invoice = new Invoice.Builder()
                .setPlate(res.getVehicle().getLicensePlate())
                .setAmount(cost)
                .setSpotType(res.getSpot().getType())
                .setDuration(minutes)
                .build();

        System.out.println("---------------------");
        System.out.println("\nFINAL INVOICE");
        System.out.println("Vehicle:     " + invoice.getPlateNumber());
        System.out.println("Spot Type:   " + invoice.getSpotType());
        System.out.println("Duration:    " + invoice.getDurationMinutes() + " minutes");
        System.out.println("Total Price: " + invoice.getTotalAmount() + " KZT");
        System.out.println("---------------------");
    }

    private static void showAllTariffs(ITariffRepository tariffRepo) {
        List<Tariff> tariffs = tariffRepo.getAllTariffs();
        tariffs.forEach(t -> System.out.println(t.getName() + ": " + t.getCost() + " KZT/h"));
    }
}
