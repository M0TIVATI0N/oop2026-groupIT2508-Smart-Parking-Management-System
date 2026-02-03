package edu.aitu.oop3.repositories;

import edu.aitu.oop3.data.IDB;
import edu.aitu.oop3.models.ParkingSpot;
import edu.aitu.oop3.models.Reservation;
import edu.aitu.oop3.models.Tariff;
import edu.aitu.oop3.models.Vehicle;
import edu.aitu.oop3.services.ParkingSpotFactory;

import java.sql.*;

public class ReservationRepository implements IReservationRepository {
    private final IDB db;

    public ReservationRepository(IDB db) {
        this.db = db;
    }

    @Override
    public Reservation findById(int id) {
        String sql = """
        SELECT r.id, r.start_time, r.end_time,
               v.id AS v_id, v.licensePlate,
               ps.id AS ps_id, ps.is_reserved, ps.spot_type, -- Добавили spot_type
               t.id AS t_id, t.name, t.cost
        FROM reservation r
        JOIN vehicles v ON r.vehicle_id = v.id
        JOIN parking_spot ps ON r.parking_spot_id = ps.id
        JOIN tariff t ON ps.tariff_id = t.id
        WHERE r.id = ?
    """;

        try (Connection conn = db.getConnection();
             PreparedStatement st = conn.prepareStatement(sql)) {

            st.setInt(1, id);
            ResultSet rs = st.executeQuery();

            if (rs.next()) {
                Vehicle v = new Vehicle(rs.getInt("v_id"), rs.getString("licensePlate"));
                Tariff t = new Tariff(rs.getInt("t_id"), rs.getString("name"), rs.getInt("cost"));

                ParkingSpot ps = ParkingSpotFactory.createSpot(
                        rs.getInt("ps_id"),
                        t,
                        rs.getBoolean("is_reserved"),
                        rs.getString("spot_type")
                );

                return new Reservation(rs.getInt("id"), rs.getTimestamp("start_time"), rs.getTimestamp("end_time"), v, ps);
            }
        } catch (SQLException e) {
            System.out.println("FindById Error: " + e.getMessage());
        }
        return null;
    }

    @Override
    public Reservation addReservation(Reservation reservation) {
        String sql = """
            INSERT INTO reservation (vehicle_id, parking_spot_id, start_time, end_time)
            VALUES (?, ?, ?, ?)
            RETURNING id
        """;

        try (Connection conn = db.getConnection();
             PreparedStatement st = conn.prepareStatement(sql)) {

            st.setInt(1, reservation.getVehicle().getId());
            st.setInt(2, reservation.getSpot().getId());
            st.setTimestamp(3, new java.sql.Timestamp(reservation.getFrom().getTime()));

            if (reservation.getTo() != null) {
                st.setTimestamp(4, new java.sql.Timestamp(reservation.getTo().getTime()));
            } else {
                st.setNull(4, java.sql.Types.TIMESTAMP);
            }

            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                int id = rs.getInt("id");
                return new Reservation(
                        id,
                        reservation.getFrom(),
                        reservation.getTo(),
                        reservation.getVehicle(),
                        reservation.getSpot()
                );
            }

        } catch (SQLException e) {
            System.out.println("Error adding reservation: " + e.getMessage());
        }

        return null;
    }

    @Override
    public void releaseReservation(int reservationId) {
        String sql = "UPDATE reservation SET end_time = CURRENT_TIMESTAMP WHERE id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement st = conn.prepareStatement(sql)) {
            st.setInt(1, reservationId);
            st.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error releasing reservation: " + e.getMessage());
        }
    }
}
