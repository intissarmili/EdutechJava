package controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import models.Notification;
import service.NotificationService;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ResourceBundle;

/**
 * Controller for individual notification items in the UI
 */
public class NotificationItemController implements Initializable {

    @FXML
    private HBox notificationItem;
    
    @FXML
    private Circle unreadIndicator;
    
    @FXML
    private Label contentLabel;
    
    @FXML
    private Label timeLabel;
    
    @FXML
    private Button markAsReadButton;
    
    private Notification notification;
    private NotificationService notificationService;
    private NotificationController parentController;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, HH:mm");
    
    /**
     * Initialize the controller
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Set up initial styles
        notificationItem.setStyle("-fx-background-color: white; -fx-padding: 10px; -fx-spacing: 10px;");
        unreadIndicator.setFill(Color.RED);
        
        // Set up event handlers
        markAsReadButton.setOnAction(event -> handleMarkAsRead());
    }
    
    /**
     * Set the notification data for this item
     * 
     * @param notification The notification to display
     */
    public void setNotification(Notification notification) {
        this.notification = notification;
        updateUI();
    }
    
    /**
     * Set the notification service
     * 
     * @param notificationService The notification service
     */
    public void setNotificationService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }
    
    /**
     * Set the parent controller
     * 
     * @param parentController The parent notification controller
     */
    public void setParentController(NotificationController parentController) {
        this.parentController = parentController;
    }
    
    /**
     * Update the UI with the current notification data
     */
    private void updateUI() {
        if (notification == null) {
            return;
        }
        
        // Set content
        contentLabel.setText(notification.getContent());
        
        // Set timestamp
        timeLabel.setText(dateFormat.format(notification.getCreationDate()));
        
        // Update read status
        unreadIndicator.setVisible(!notification.isRead());
        
        // Update background based on read status
        if (notification.isRead()) {
            notificationItem.setStyle("-fx-background-color: #f5f5f5; -fx-padding: 10px; -fx-spacing: 10px;");
        } else {
            notificationItem.setStyle("-fx-background-color: #e3f2fd; -fx-padding: 10px; -fx-spacing: 10px;");
        }
    }
    
    /**
     * Mark this notification as read in the UI
     */
    public void markAsRead() {
        if (notification != null) {
            notification.setRead(true);
            updateUI();
        }
    }
    
    /**
     * Get the notification ID
     * 
     * @return The notification ID
     */
    public int getNotificationId() {
        return notification != null ? notification.getId() : -1;
    }
    
    /**
     * Handle marking the notification as read
     */
    private void handleMarkAsRead() {
        if (notification != null && notificationService != null) {
            notificationService.markAsRead(notification.getId());
            
            // Remove from UI
            if (parentController != null) {
                parentController.removeNotificationFromUI(notificationItem);
            }
        }
    }
} 