package interfaces;

import models.Certification;

import java.util.List;

public interface ICertifService {
    List<Certification> getAll(); // Consulter toutes les certifications
    Certification getById(int id); // Obtenir une certification spécifique par ID
}
