package controllers.user;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import models.UserLog;
import services.user.UserLogService;

import java.util.List;

public class AdminLogController {

    @FXML
    private TableView<UserLog> logTable;

    @FXML
    private TableColumn<UserLog, Integer> idColumn;

    @FXML
    private TableColumn<UserLog, Integer> userIdColumn;

    @FXML
    private TableColumn<UserLog, String> actionColumn;

    @FXML
    private TableColumn<UserLog, String> timestampColumn;

    private final UserLogService userLogService = new UserLogService();

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        userIdColumn.setCellValueFactory(new PropertyValueFactory<>("userId"));
        actionColumn.setCellValueFactory(new PropertyValueFactory<>("action"));
        timestampColumn.setCellValueFactory(new PropertyValueFactory<>("timestamp"));

        loadLogs();
    }

    private void loadLogs() {
        List<UserLog> logs = userLogService.getAllLogs();
        ObservableList<UserLog> logList = FXCollections.observableArrayList(logs);
        logTable.setItems(logList);
    }

    @FXML
    private void handleRefresh() {
        loadLogs();
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) logTable.getScene().getWindow();
        stage.close();
    }
}
