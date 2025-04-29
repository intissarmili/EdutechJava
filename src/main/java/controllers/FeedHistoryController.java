package controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import models.Feed;
import models.FeedHistory;
import service.FeedHistoryService;
import service.FeedService;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class FeedHistoryController implements Initializable {

    @FXML
    private TableView<FeedHistory> historyTableView;
    
    @FXML
    private TableColumn<FeedHistory, Integer> feedIdColumn;
    
    @FXML
    private TableColumn<FeedHistory, String> oldContentColumn;
    
    @FXML
    private TableColumn<FeedHistory, String> newContentColumn;
    
    @FXML
    private TableColumn<FeedHistory, String> actionTypeColumn;
    
    @FXML
    private TableColumn<FeedHistory, String> timestampColumn;
    
    @FXML
    private ListView<Feed> feedsListView;
    
    private FeedService feedService;
    private FeedHistoryService historyService;
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        feedService = new FeedService();
        historyService = new FeedHistoryService();
        
        setupTableColumns();
        loadFeeds();
        loadAllHistory();
        
        // Add event listener for feed selection
        feedsListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                loadHistoryForFeed(newValue.getId());
            }
        });
    }
    
    private void setupTableColumns() {
        feedIdColumn.setCellValueFactory(new PropertyValueFactory<>("feedId"));
        oldContentColumn.setCellValueFactory(new PropertyValueFactory<>("oldContent"));
        newContentColumn.setCellValueFactory(new PropertyValueFactory<>("newContent"));
        actionTypeColumn.setCellValueFactory(new PropertyValueFactory<>("actionType"));
        
        // Format the timestamp
        timestampColumn.setCellValueFactory(cellData -> {
            LocalDateTime timestamp = cellData.getValue().getTimestamp();
            String formattedTimestamp = timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            return javafx.beans.binding.Bindings.createStringBinding(() -> formattedTimestamp);
        });
    }
    
    private void loadFeeds() {
        try {
            List<Feed> feeds = feedService.getAllFeeds();
            feedsListView.setItems(FXCollections.observableArrayList(feeds));
        } catch (SQLException e) {
            e.printStackTrace();
            // Show error message to user
        }
    }
    
    private void loadAllHistory() {
        try {
            List<FeedHistory> history = historyService.getAllHistory();
            historyTableView.setItems(FXCollections.observableArrayList(history));
        } catch (SQLException e) {
            e.printStackTrace();
            // Show error message to user
        }
    }
    
    private void loadHistoryForFeed(int feedId) {
        try {
            List<FeedHistory> history = historyService.getHistoryByFeedId(feedId);
            historyTableView.setItems(FXCollections.observableArrayList(history));
        } catch (SQLException e) {
            e.printStackTrace();
            // Show error message to user
        }
    }
    
    @FXML
    private void showAllHistory() {
        loadAllHistory();
        feedsListView.getSelectionModel().clearSelection();
    }
    
    @FXML
    private void refreshData() {
        loadFeeds();
        loadAllHistory();
    }
} 