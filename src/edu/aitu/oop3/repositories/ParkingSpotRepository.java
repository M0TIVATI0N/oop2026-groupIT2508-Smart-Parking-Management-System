package edu.aitu.oop3.repositories;

import edu.aitu.oop3.data.IDB;
import edu.aitu.oop3.models.*;
import edu.aitu.oop3.services.ParkingSpotFactory;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ParkingSpotRepository implements IParkingSpotRepository {
    private final IDB db;

    public ParkingSpotRepository(IDB db) {
        this.db = db;
    }

    @Override
    public ListResult<ParkingSpot> getAllSpots() {
        List<ParkingSpot> spots = new ArrayList<>();
        String sql = """
        SELECT ps.id, ps.is_reserved, ps.spot_type, 
               t.id AS t_id, t.name AS t_name, t.cost AS t_cost
        FROM parking_spot ps
        JOIN tariff t ON ps.tariff_id = t.id
    """;

        try (Connection conn = db.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Tariff tariff = new Tariff(
                        rs.getInt("t_id"),
                        rs.getString("t_name"),
                        rs.getInt("t_cost")
                );

                ParkingSpot spot = ParkingSpotFactory.createSpot(
                        rs.getInt("id"),
                        tariff,
                        rs.getBoolean("is_reserved"),
                        rs.getString("spot_type")
                );
                spots.add(spot);
            }
        } catch (SQLException e) {
            System.out.println("Query error: " + e.getMessage());
        }

        return new ListResult<>(spots, spots.size());
    }


    @Override
    public void updateSpotStatus(int id, boolean isReserved) {
        String sql = "UPDATE parking_spot SET is_reserved = ? WHERE id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setBoolean(1, isReserved);
            pstmt.setInt(2, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Update error: " + e.getMessage());
        }
    }
}