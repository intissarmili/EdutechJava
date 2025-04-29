package controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import models.Notification;
import service.NotificationService;

import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Controller for managing notifications in the UI
 */
public class NotificationController implements Initializable {

    @FXML
    private VBox notificationsContainer;
    
    @FXML
    private Button markAllReadButton;
    
    @FXML
    private Label unreadCountLabel;
    
    private NotificationService notificationService;
    private int currentUserId;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, HH:mm");
    private final ObservableList<Notification> notifications = FXCollections.observableArrayList();
    private ScheduledExecutorService scheduler;
    
    /**
     * Initialize the controller
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        notificationService = new NotificationService();
        
        // Set up mark all as read button
        markAllReadButton.setOnAction(event -> markAllAsRead());
        
        // Initial load of notifications
        loadNotifications();
    }
    
    /**
     * Set the current user ID and refresh notifications
     * 
     * @param userId The ID of the current user
     */
    public void setCurrentUserId(int userId) {
        this.currentUserId = userId;
        loadNotifications();
    }
    
    /**
     * Load notifications for the current user
     */
    private void loadNotifications() {
        if (currentUserId <= 0) {
            return;
        }
        
        notificationsContainer.getChildren().clear();
        
        // Get all notifications for the user
        List<Notification> notifications = notificationService.getNotificationsByUserId(currentUserId);
        
        // Update unread count
        int unreadCount = notificationService.getUnreadNotificationCount(currentUserId);
        updateUnreadCount(unreadCount);
        
        // Create notification items
        for (Notification notification : notifications) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/NotificationItem.fxml"));
                VBox notificationItem = loader.load();
                
                // Get the controller and set the notification
                NotificationItemController controller = loader.getController();
                controller.setNotification(notification);
                controller.setNotificationService(notificationService);
                controller.setParentController(this);
                
                // Add click handler to mark as read
                notificationItem.setOnMouseClicked(event -> {
                    if (!notification.isRead()) {
                        notificationService.markAsRead(notification.getId());
                        controller.markAsRead();
                        updateUnreadCount(unreadCount - 1);
                    }
                });
                
                notificationsContainer.getChildren().add(notificationItem);
            } catch (IOException e) {
                System.out.println("Error loading notification item: " + e.getMessage());
            }
        }
    }
    
    /**
     * Mark all notifications as read
     */
    private void markAllAsRead() {
        if (currentUserId <= 0) {
            return;
        }
        
        int markedCount = notificationService.markAllAsRead(currentUserId);
        if (markedCount > 0) {
            updateUnreadCount(0);
            loadNotifications(); // Refresh the list
        }
    }
    
    /**
     * Update the unread count label
     * 
     * @param count The number of unread notifications
     */
    private void updateUnreadCount(int count) {
        unreadCountLabel.setText(count + " unread");
        unreadCountLabel.setTextFill(count > 0 ? Color.RED : Color.GREEN);
    }
    
    /**
     * Refresh the notifications list
     */
    public void refresh() {
        loadNotifications();
    }
    
    /**
     * Start polling for new notifications in background
     */
    private void startNotificationPolling() {
        // Create a scheduler that checks for new notifications every minute
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(this::loadNotifications, 1, 1, TimeUnit.MINUTES);
    }
    
    /**
     * Stop notification polling when controller is no longer needed
     */
    public void shutdown() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
        }
    }
    
    /**
     * Create a UI component for a single notification
     * 
     * @param notification The notification to display
     * @return A VBox containing the notification
     */
    private VBox createNotificationItem(Notification notification) {
        VBox item = new VBox(5);
        item.getStyleClass().add("notification-item");
        item.setStyle("-fx-background-color: " + (notification.isRead() ? "#f5f5f5" : "#e3f2fd") + "; " +
                     "-fx-padding: 10px; -fx-border-color: #e0e0e0; -fx-border-width: 0 0 1 0;");
        
        // Header with indicator and date
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        
        // Unread indicator
        if (!notification.isRead()) {
            Circle unreadIndicator = new Circle(5);
            unreadIndicator.setFill(Color.DODGERBLUE);
            header.getChildren().add(unreadIndicator);
        }
        
        // Type indicator
        Label typeLabel = new Label(notification.getType().toString());
        typeLabel.setStyle("-fx-background-color: " + getColorForType(notification.getType()) + "; " +
                          "-fx-text-fill: white; -fx-padding: 2px 5px; -fx-background-radius: 3px;");
        header.getChildren().add(typeLabel);
        
        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        header.getChildren().add(spacer);
        
        // Date
        Label dateLabel = new Label(dateFormat.format(notification.getCreationDate()));
        dateLabel.setStyle("-fx-text-fill: #757575; -fx-font-size: 0.8em;");
        header.getChildren().add(dateLabel);
        
        // Content
        Label contentLabel = new Label(notification.getContent());
        contentLabel.setWrapText(true);
        contentLabel.setStyle("-fx-font-size: 1.1em;");
        
        // Action buttons
        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_RIGHT);
        
        Button viewButton = new Button("View");
        viewButton.getStyleClass().add("notification-action-button");
        viewButton.setStyle("-fx-background-color: #2196f3; -fx-text-fill: white;");
        viewButton.setOnAction(event -> viewNotification(notification));
        
        Button dismissButton = new Button("Dismiss");
        dismissButton.getStyleClass().add("notification-action-button");
        dismissButton.setStyle("-fx-background-color: #e0e0e0; -fx-text-fill: #424242;");
        dismissButton.setOnAction(event -> dismissNotification(notification));
        
        actions.getChildren().addAll(viewButton, dismissButton);
        
        // Add everything to the item
        item.getChildren().addAll(header, contentLabel, actions);
        
        return item;
    }
    
    /**
     * Get a color for the notification type label
     * 
     * @param type The notification type
     * @return A hex color string
     */
    private String getColorForType(Notification.NotificationType type) {
        return switch (type) {
            case POST -> "#4CAF50"; // Green
            case COMMENT -> "#2196F3"; // Blue
            case LIKE -> "#E91E63"; // Pink
            case DISLIKE -> "#F44336"; // Red
            case MENTION -> "#9C27B0"; // Purple
            case SYSTEM -> "#FF9800"; // Orange
        };
    }
    
    /**
     * View a notification and mark it as read
     * 
     * @param notification The notification to view
     */
    private void viewNotification(Notification notification) {
        // Mark as read in database
        notificationService.markAsRead(notification.getId());
        
        // Update local state
        notification.setRead(true);
        loadNotifications();
        
        // Navigate to the related content based on notification type
        Integer relatedEntityId = notification.getRelatedEntityId();
        if (relatedEntityId != null) {
            navigateToRelatedContent(notification.getType(), relatedEntityId);
        }
    }
    
    /**
     * Navigate to content related to the notification
     * 
     * @param type The notification type
     * @param entityId The related entity ID
     */
    private void navigateToRelatedContent(Notification.NotificationType type, int entityId) {
        // This would typically navigate to a different view based on the notification type
        // For example, open a post detail view, comment thread, etc.
        // Implementation depends on the application's navigation system
        
        System.out.println("Navigating to " + type + " with ID: " + entityId);
        
        // Example:
        // switch (type) {
        //     case POST -> mainController.openFeedDetail(entityId);
        //     case COMMENT -> mainController.openCommentThread(entityId);
        //     // etc.
        // }
    }
    
    /**
     * Dismiss a notification
     * 
     * @param notification The notification to dismiss
     */
    private void dismissNotification(Notification notification) {
        // Delete from database
        notificationService.deleteNotification(notification.getId());
        
        // Remove from the list
        notifications.remove(notification);
        loadNotifications();
    }
    
    /**
     * Remove a notification item from the UI
     * 
     * @param notificationItem The notification item to remove
     */
    public void removeNotificationFromUI(HBox notificationItem) {
        notificationsContainer.getChildren().remove(notificationItem);
    }
} 