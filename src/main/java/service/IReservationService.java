package service;

import models.reservation;
import java.sql.SQLException;
import java.util.List;

public interface IReservationService {

    void add(reservation reservation) throws SQLException;
    void update(reservation reservation) throws SQLException;
    void delete(int id) throws SQLException;
    reservation getById(int id) throws SQLException;
    List<reservation> getAll() throws SQLException;

}
