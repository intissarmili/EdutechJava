package interfaces;


import models.Favorite;

import java.util.List;

public interface IFavService {
    void addFavorite(int userId, int certificationId);
    void removeFavorite(int userId, int certificationId);
    List<Favorite> getFavoritesByUserId(int userId);
    boolean isFavorite(int userId, int certificationId);
}
