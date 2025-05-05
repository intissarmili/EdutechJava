package service;

import java.sql.SQLException;
import java.util.List;

public interface IServicee<T>{

    public void create (T t) throws SQLException;
    public void update (T t) throws SQLException;
    public void delete (T t) throws SQLException;
    public List<T> readAll() throws SQLException;

}
