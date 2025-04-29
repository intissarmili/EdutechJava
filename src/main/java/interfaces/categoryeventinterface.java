package interfaces;

import model.CategoryEvent;
import java.util.List;

public interface categoryeventinterface {
    boolean add(CategoryEvent category);  // Changé de void à boolean
    void delete(CategoryEvent categoryEvent);
    List<CategoryEvent> getAll();
    void update(CategoryEvent categoryEvent);
}