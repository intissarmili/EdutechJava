package api;

import static spark.Spark.*;
import com.google.gson.Gson;
import service.NotificationService;
import models.Notification;
import java.util.List;
import static spark.Spark.webSocket;
import websocket.NotificationWebSocketHandler;
import spark.Request;
import spark.Response;


public class NotificationAPI {

    public static void main(String[] args) {

        port(4567); // Serveur sur localhost:4567
        Gson gson = new Gson();
        NotificationService notificationService = new NotificationService();
        webSocket("/notifications/ws", NotificationWebSocketHandler.class);
        init();


        // GET - Afficher toutes les notifications
        get("/notifications", (req, res) -> {
            List<Notification> notifications = notificationService.getAllNotifications();
            res.type("application/json");
            return gson.toJson(notifications);
        });

        // POST - Ajouter une notification
        post("/notifications", (req, res) -> {
            Notification newNotification = gson.fromJson(req.body(), Notification.class);
            notificationService.addNotification(newNotification);

            // Après ajout
            NotificationWebSocketHandler.broadcast("Nouvelle notification : " + newNotification.getContent());

            res.status(201); // HTTP 201 Created
            return "Notification ajoutée avec succès";


        });

        // DELETE - Supprimer une notification
        delete("/notifications/:id", (req, res) -> {
            int id = Integer.parseInt(req.params(":id"));
            notificationService.deleteNotification(id);
            return "Notification supprimée";
        });

        // PUT - Modifier une notification existante
        put("/notifications/:id", (req, res) -> {
            int id = Integer.parseInt(req.params(":id"));
            Notification notificationToUpdate = gson.fromJson(req.body(), Notification.class);
            notificationToUpdate.setId(id); // très important !
            notificationService.updateNotification(notificationToUpdate);
            return "Notification mise à jour avec succès";
        });

    }

    private final NotificationService notificationService;
    private final Gson gson;
    
    public NotificationAPI() {
        this.notificationService = new NotificationService();
        this.gson = new Gson();
    }
    
    /**
     * Get all notifications
     */
    public String getAllNotifications(Request req, Response res) {
        List<Notification> notifications = notificationService.getAllNotifications();
        return gson.toJson(notifications);
    }
    
    /**
     * Create a new notification
     */
    public String createNotification(Request req, Response res) {
        Notification newNotification = gson.fromJson(req.body(), Notification.class);
        Notification createdNotification = notificationService.addNotification(newNotification);
        
        if (createdNotification != null) {
            res.status(201);
            return gson.toJson(createdNotification);
        } else {
            res.status(500);
            return "{\"error\": \"Failed to create notification\"}";
        }
    }
    
    /**
     * Get notifications for a specific user
     */
    public String getUserNotifications(Request req, Response res) {
        int userId = Integer.parseInt(req.params(":userId"));
        List<Notification> notifications = notificationService.getNotificationsByUserId(userId);
        return gson.toJson(notifications);
    }
    
    /**
     * Mark a notification as read
     */
    public String markAsRead(Request req, Response res) {
        int notificationId = Integer.parseInt(req.params(":id"));
        boolean success = notificationService.markAsRead(notificationId);
        
        if (success) {
            return "{\"status\": \"success\"}";
        } else {
            res.status(404);
            return "{\"error\": \"Notification not found\"}";
        }
    }
    
    /**
     * Mark all notifications as read for a user
     */
    public String markAllAsRead(Request req, Response res) {
        int userId = Integer.parseInt(req.params(":userId"));
        int markedCount = notificationService.markAllAsRead(userId);
        return "{\"markedCount\": " + markedCount + "}";
    }
    
    /**
     * Delete a notification
     */
    public String deleteNotification(Request req, Response res) {
        int notificationId = Integer.parseInt(req.params(":id"));
        boolean success = notificationService.deleteNotification(notificationId);
        
        if (success) {
            return "{\"status\": \"success\"}";
        } else {
            res.status(404);
            return "{\"error\": \"Notification not found\"}";
        }
    }
}
