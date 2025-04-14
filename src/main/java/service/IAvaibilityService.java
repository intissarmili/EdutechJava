package service;

import models.avaibility;
import java.sql.SQLException;
import java.util.List;

public interface IAvaibilityService {

    void add(avaibility availability) throws SQLException;
    void update(avaibility availability) throws SQLException;
    void delete(int id) throws SQLException;
    avaibility getById(int id) throws SQLException;
    List<avaibility> getAll() throws SQLException;

}
