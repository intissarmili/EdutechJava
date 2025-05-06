package interfaces;

import java.util.List;

public interface eventinterface<T> {
    void add(T t);
    void delete(T t);  // méthode qui prend un objet de type T
    List<T> getAll();
    void update(T t);
}
