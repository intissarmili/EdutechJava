package interfaces;


import models.Ordre;

import java.util.List;

public interface IOrdreService {
    void add(Ordre ordre);
    void delete(Ordre ordre);
    List<Ordre> getByUserId(int userId);
    double calculerTotal(int userId);
}
