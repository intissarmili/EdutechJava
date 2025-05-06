package controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import models.Feed;
import models.FeedStatistics;
import service.CommentaireService;
import service.FeedService;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class FeedStatisticsController implements Initializable {

    @FXML
    private BarChart<String, Number> commentCountChart;
    
    @FXML
    private PieChart contentLengthPieChart;
    
    @FXML
    private Label totalFeedsLabel;
    
    @FXML
    private Label averageCommentsLabel;
    
    @FXML
    private Label mostCommentedFeedLabel;
    
    @FXML
    private TableView<FeedStatistics> statsTableView;
    
    @FXML
    private TableColumn<FeedStatistics, Integer> colFeedId;
    
    @FXML
    private TableColumn<FeedStatistics, String> colPublication;
    
    @FXML
    private TableColumn<FeedStatistics, Integer> colCommentCount;
    
    @FXML
    private TableColumn<FeedStatistics, Integer> colLikes;
    
    @FXML
    private TableColumn<FeedStatistics, Integer> colDislikes;
    
    @FXML
    private TableColumn<FeedStatistics, Void> colActions;
    
    private FeedService feedService;
    private CommentaireService commentaireService;
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        feedService = new FeedService();
        commentaireService = new CommentaireService();
        
        // Initialize table columns
        setupTableColumns();
        
        // Load the data
        loadStatistics();
    }
    
    private void setupTableColumns() {
        colFeedId.setCellValueFactory(new PropertyValueFactory<>("feedId"));
        colPublication.setCellValueFactory(new PropertyValueFactory<>("publication"));
        colCommentCount.setCellValueFactory(new PropertyValueFactory<>("commentCount"));
        colLikes.setCellValueFactory(new PropertyValueFactory<>("totalLikes"));
        colDislikes.setCellValueFactory(new PropertyValueFactory<>("totalDislikes"));
        
        // Setup action buttons
        setupActionButtons();
    }
    
    private void setupActionButtons() {
        colActions.setCellFactory(column -> {
            return new javafx.scene.control.TableCell<FeedStatistics, Void>() {
                private final javafx.scene.control.Button viewButton = new javafx.scene.control.Button("Détails");
                {
                    viewButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
                    viewButton.setOnAction(event -> {
                        FeedStatistics stats = getTableView().getItems().get(getIndex());
                        viewFeedDetails(stats.getFeedId());
                    });
                }
                
                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        setGraphic(viewButton);
                    }
                }
            };
        });
    }
    
    private void viewFeedDetails(int feedId) {
        try {
            // Get the feed and display its details
            Feed feed = feedService.getFeedById(feedId);
            if (feed != null) {
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
                alert.setTitle("Détails de la publication #" + feedId);
                alert.setHeaderText("Publication #" + feedId);
                
                StringBuilder content = new StringBuilder();
                content.append("Contenu: ").append(feed.getPublication()).append("\n\n");
                content.append("Dernière modification: ").append(feed.getLastModified()).append("\n");
                
                // Add statistics
                FeedStatistics stats = feedService.getFeedStatistics(feedId);
                content.append("\nStatistiques:\n");
                content.append("- Commentaires: ").append(stats.getCommentCount()).append("\n");
                content.append("- Likes: ").append(stats.getTotalLikes()).append("\n");
                content.append("- Dislikes: ").append(stats.getTotalDislikes()).append("\n");
                
                alert.setContentText(content.toString());
                alert.showAndWait();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Show error message
        }
    }
    
    private void loadStatistics() {
        try {
            List<Feed> feeds = feedService.getAllFeeds();
            
            // Update total feeds count
            totalFeedsLabel.setText(String.valueOf(feeds.size()));
            
            // Load comment statistics
            loadCommentStatistics(feeds);
            
            // Load content length statistics
            loadContentLengthStatistics(feeds);
            
            // Load table data
            loadTableData();
            
        } catch (SQLException e) {
            e.printStackTrace();
            // Here you would show an error message to the user
        }
    }
    
    private void loadTableData() {
        try {
            List<FeedStatistics> statistics = feedService.getAllFeedStatistics();
            statsTableView.setItems(FXCollections.observableArrayList(statistics));
        } catch (SQLException e) {
            e.printStackTrace();
            // Show error message to user
        }
    }
    
    private void loadCommentStatistics(List<Feed> feeds) {
        try {
            int totalComments = 0;
            Feed mostCommentedFeed = null;
            int maxComments = 0;
            
            // Clear previous data
            commentCountChart.getData().clear();
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Nombre de commentaires");
            
            for (Feed feed : feeds) {
                int commentCount = commentaireService.getCommentCountForFeed(feed.getId());
                totalComments += commentCount;
                
                // For bar chart
                series.getData().add(new XYChart.Data<>("Feed #" + feed.getId(), commentCount));
                
                // Track most commented feed
                if (commentCount > maxComments) {
                    maxComments = commentCount;
                    mostCommentedFeed = feed;
                }
            }
            
            commentCountChart.getData().add(series);
            
            // Update stats labels
            double averageComments = feeds.isEmpty() ? 0 : (double) totalComments / feeds.size();
            averageCommentsLabel.setText(String.format("%.2f", averageComments));
            
            if (mostCommentedFeed != null) {
                mostCommentedFeedLabel.setText("Feed #" + mostCommentedFeed.getId() + 
                        " (" + maxComments + " commentaires)");
            } else {
                mostCommentedFeedLabel.setText("Aucun");
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private void loadContentLengthStatistics(List<Feed> feeds) {
        Map<String, Integer> lengthCategories = new HashMap<>();
        lengthCategories.put("Short (< 50 chars)", 0);
        lengthCategories.put("Medium (50-200 chars)", 0);
        lengthCategories.put("Long (> 200 chars)", 0);
        
        for (Feed feed : feeds) {
            int length = feed.getPublication().length();
            
            if (length < 50) {
                lengthCategories.put("Short (< 50 chars)", lengthCategories.get("Short (< 50 chars)") + 1);
            } else if (length < 200) {
                lengthCategories.put("Medium (50-200 chars)", lengthCategories.get("Medium (50-200 chars)") + 1);
            } else {
                lengthCategories.put("Long (> 200 chars)", lengthCategories.get("Long (> 200 chars)") + 1);
            }
        }
        
        // Create pie chart data
        contentLengthPieChart.setData(FXCollections.observableArrayList(
                lengthCategories.entrySet().stream()
                        .map(entry -> new PieChart.Data(entry.getKey(), entry.getValue()))
                        .toList()
        ));
    }
    
    @FXML
    private void refreshData() {
        loadStatistics();
    }
    
    @FXML
    private void goBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ListFeed.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) totalFeedsLabel.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
} 