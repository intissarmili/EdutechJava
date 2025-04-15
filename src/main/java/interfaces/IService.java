package interfaces;

import java.sql.SQLException;
import java.util.List;

public interface IService <T> {



    void create(T t) throws SQLException;

    void update(T t) throws SQLException;

    void delete(T t) throws SQLException;

    List<T> readAll() throws SQLException;
}
