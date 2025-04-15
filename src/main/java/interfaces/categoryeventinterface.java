package interfaces;

import java.util.List;
import model.CategoryEvent;

public interface categoryeventinterface {
    void add(CategoryEvent categoryEvent);
    void delete(CategoryEvent categoryEvent);
    List<CategoryEvent> getAll();
    void update(CategoryEvent categoryEvent);
}