package service;

import models.reservation;
import utils.MaConnexion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.sql.Timestamp;

public class ReservationService {

    public void add(reservation res) throws SQLException {
        String query = "INSERT INTO reservation (topic, start_time, status, duration, avaibility_id) " +
                "VALUES (?, ?, ?, ?, ?)";

        Connection conn = MaConnexion.getInstance().getConnection();
        PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);

        stmt.setString(1, res.getTopic());
        stmt.setTimestamp(2, new Timestamp(res.getStart_time().getTime()));
        stmt.setString(3, res.getStatus());
        stmt.setInt(4, res.getDuration());
        stmt.setInt(5, res.getAvaibility_id());

        int rowsAffected = stmt.executeUpdate();

        if (rowsAffected == 0) {
            throw new SQLException("Creating reservation failed, no rows affected.");
        }

        // Get the generated ID if needed
        ResultSet generatedKeys = stmt.getGeneratedKeys();
        if (generatedKeys.next()) {
            res.setId(generatedKeys.getInt(1));
        }
    }

    public void update(reservation res) throws SQLException {
        String query = "UPDATE reservation SET topic = ?, start_time = ?, status = ?, " +
                "duration = ?, avaibility_id = ? WHERE id = ?";

        Connection conn = MaConnexion.getInstance().getConnection();
        PreparedStatement stmt = conn.prepareStatement(query);

        stmt.setString(1, res.getTopic());
        stmt.setTimestamp(2, new Timestamp(res.getStart_time().getTime()));
        stmt.setString(3, res.getStatus());
        stmt.setInt(4, res.getDuration());
        stmt.setInt(5, res.getAvaibility_id());
        stmt.setInt(6, res.getId());

        stmt.executeUpdate();
    }

    public void delete(int id) throws SQLException {
        String query = "DELETE FROM reservation WHERE id = ?";

        Connection conn = MaConnexion.getInstance().getConnection();
        PreparedStatement stmt = conn.prepareStatement(query);

        stmt.setInt(1, id);
        stmt.executeUpdate();
    }

    public reservation getById(int id) throws SQLException {
        String query = "SELECT * FROM reservation WHERE id = ?";

        Connection conn = MaConnexion.getInstance().getConnection();
        PreparedStatement stmt = conn.prepareStatement(query);

        stmt.setInt(1, id);
        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            return extractReservationFromResultSet(rs);
        }

        return null; // No reservation found with the given ID
    }

    public List<reservation> getAll() throws SQLException {
        List<reservation> reservations = new ArrayList<>();
        String query = "SELECT * FROM reservation";

        Connection conn = MaConnexion.getInstance().getConnection();
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(query);

        while (rs.next()) {
            reservations.add(extractReservationFromResultSet(rs));
        }

        return reservations;
    }

    public List<reservation> getReservationsByAvaibilityId(int avaibilityId) throws SQLException {
        String query = "SELECT * FROM reservation WHERE avaibility_id = ? ORDER BY start_time";
        Connection conn = MaConnexion.getInstance().getConnection();
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setInt(1, avaibilityId);
        ResultSet rs = stmt.executeQuery();

        List<reservation> reservations = new ArrayList<>();
        while (rs.next()) {
            reservations.add(extractReservationFromResultSet(rs));
        }
        return reservations;
    }

    private reservation extractReservationFromResultSet(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String topic = rs.getString("topic");
        java.util.Date startTime = new java.util.Date(rs.getTimestamp("start_time").getTime());
        String status = rs.getString("status");
        int duration = rs.getInt("duration");
        int avaibilityId = rs.getInt("avaibility_id");

        return new reservation(id, topic, startTime, status, duration, avaibilityId);
    }
}