

package service;

import models.avaibility;
import utils.MaConnexion;


import java.util.ArrayList;
import java.util.List;




import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.sql.Timestamp;












public class AvaibilityService {

    // Singleton pattern connection management via MaConnexion
    public void add(avaibility a) throws SQLException {
        String query = "INSERT INTO avaibility (date, start_time, end_time, tutor_id) VALUES (?, ?, ?, ?)";

        Connection conn = MaConnexion.getInstance().getConnection();
        PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);

        stmt.setString(1, a.getDate());
        stmt.setString(2, a.getStartTime());
        stmt.setString(3, a.getEndTime());
        stmt.setInt(4, a.getTutorId());

        int rowsAffected = stmt.executeUpdate();

        if (rowsAffected == 0) {
            throw new SQLException("Creating availability failed, no rows affected.");
        }

        // Get the generated ID if needed
        ResultSet generatedKeys = stmt.getGeneratedKeys();
        if (generatedKeys.next()) {
            a.setId(generatedKeys.getInt(1));
        }

        // No need to close the connection, reused from Singleton pattern
    }

    public void update(avaibility a) throws SQLException {
        String query = "UPDATE avaibility SET date = ?, start_time = ?, end_time = ?, tutor_id = ? WHERE id = ?";

        Connection conn = MaConnexion.getInstance().getConnection();
        PreparedStatement stmt = conn.prepareStatement(query);

        stmt.setString(1, a.getDate());
        stmt.setString(2, a.getStartTime());
        stmt.setString(3, a.getEndTime());
        stmt.setInt(4, a.getTutorId());
        stmt.setInt(5, a.getId());

        stmt.executeUpdate();
    }

    public void delete(int id) throws SQLException {
        String query = "DELETE FROM avaibility WHERE id = ?";

        Connection conn = MaConnexion.getInstance().getConnection();
        PreparedStatement stmt = conn.prepareStatement(query);

        stmt.setInt(1, id);
        stmt.executeUpdate();
    }

    public avaibility getById(int id) throws SQLException {
        String query = "SELECT * FROM avaibility WHERE id = ?";

        Connection conn = MaConnexion.getInstance().getConnection();
        PreparedStatement stmt = conn.prepareStatement(query);

        stmt.setInt(1, id);
        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            return extractavaibilityFromResultSet(rs);
        }

        return null; // No availability found with the given ID
    }

    public List<avaibility> getAll() throws SQLException {
        List<avaibility> list = new ArrayList<>();
        String query = "SELECT * FROM avaibility";

        Connection conn = MaConnexion.getInstance().getConnection();
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(query);

        while (rs.next()) {
            list.add(extractavaibilityFromResultSet(rs));
        }

        return list;
    }

    private avaibility extractavaibilityFromResultSet(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String date = rs.getString("date");
        String startTime = rs.getString("start_time");
        String endTime = rs.getString("end_time");
        int tutorId = rs.getInt("tutor_id");

        return new avaibility(id, date, startTime, endTime, tutorId);
    }
}
