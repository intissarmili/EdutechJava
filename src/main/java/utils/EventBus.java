package utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Une classe simple pour permettre la communication entre différents contrôleurs
 * sans couplage direct entre eux.
 */
public class EventBus {
    
    private static EventBus instance;
    private Map<String, List<EventListener>> listeners = new HashMap<>();
    
    private EventBus() {
        // Constructeur privé pour le singleton
    }
    
    public static synchronized EventBus getInstance() {
        if (instance == null) {
            instance = new EventBus();
        }
        return instance;
    }
    
    /**
     * Ajoute un écouteur pour un type d'événement spécifique
     * @param eventType Le type d'événement à écouter
     * @param listener L'écouteur à ajouter
     */
    public void subscribe(String eventType, EventListener listener) {
        List<EventListener> eventListeners = listeners.getOrDefault(eventType, new ArrayList<>());
        eventListeners.add(listener);
        listeners.put(eventType, eventListeners);
    }
    
    /**
     * Supprime un écouteur pour un type d'événement spécifique
     * @param eventType Le type d'événement
     * @param listener L'écouteur à supprimer
     */
    public void unsubscribe(String eventType, EventListener listener) {
        List<EventListener> eventListeners = listeners.get(eventType);
        if (eventListeners != null) {
            eventListeners.remove(listener);
        }
    }
    
    /**
     * Publie un événement aux écouteurs abonnés
     * @param eventType Le type d'événement
     * @param data Les données associées à l'événement (peut être null)
     */
    public void publish(String eventType, Object data) {
        List<EventListener> eventListeners = listeners.get(eventType);
        if (eventListeners != null) {
            for (EventListener listener : eventListeners) {
                listener.onEvent(eventType, data);
            }
        }
    }
    
    /**
     * Interface pour les écouteurs d'événements
     */
    public interface EventListener {
        void onEvent(String eventType, Object data);
    }
}
