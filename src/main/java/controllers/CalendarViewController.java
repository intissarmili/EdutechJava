package controllers;

import com.calendarfx.model.Calendar;
import com.calendarfx.model.Calendar.Style;
import com.calendarfx.model.CalendarSource;
import com.calendarfx.model.Entry;
import com.calendarfx.view.CalendarView;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import models.avaibility;
import models.reservation;
import service.AvaibilityService;
import service.ReservationService;

import java.net.URL;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;


public class CalendarViewController implements Initializable {

    @FXML
    private VBox calendarContainer;

    @FXML
    private Label availabilityInfoLabel;

    private avaibility currentAvailability;
    private ReservationService reservationService;
    private AvaibilityService availabilityService;

    private CalendarView calendarView;
    private Calendar reservationsCalendar;
    @FXML
    private Button confirmButton;
    private Entry<reservation> selectedEntry;

    public void setAvailability(avaibility availability) {
        this.currentAvailability = availability;
        availabilityInfoLabel.setText("Availability: " + availability.getDate() +
                " from " + availability.getStartTime() +
                " to " + availability.getEndTime());
        loadReservations();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        reservationService = new ReservationService();
        availabilityService = new AvaibilityService();

        // Set up the calendar
        setupCalendar();
    }




    private void setupCalendar() {
        calendarView = new CalendarView();
        CalendarSource source = new CalendarSource("Reservations");
        reservationsCalendar = new Calendar("Reservations");
        reservationsCalendar.setStyle(Style.STYLE1);
        source.getCalendars().add(reservationsCalendar);
        calendarView.getCalendarSources().add(source);

        calendarView.setShowAddCalendarButton(false);
        calendarView.setShowPrintButton(false);
        calendarView.setShowSourceTrayButton(false);
        calendarView.setShowPageToolBarControls(false);

        // Add entry factory to customize entry appearance
        // Set up entry interaction
        calendarView.setEntryDetailsCallback(param -> {
            Entry<?> entry = param.getEntry();
            if (entry.getUserObject() instanceof reservation) {
                showReservationDetails((reservation) entry.getUserObject());
            }
            return null;
        });


        // Add context menu for toggling confirmation
        calendarView.setEntryContextMenuCallback(param -> {
            Entry<?> entry = param.getEntry();
            if (entry != null && entry.getUserObject() instanceof reservation) {
                return createReservationContextMenu((Entry<reservation>) entry);
            }
            return null;
        });

        calendarContainer.getChildren().add(calendarView);
    }

    private ContextMenu createReservationContextMenu(Entry<reservation> entry) {
        ContextMenu menu = new ContextMenu();
        reservation res = entry.getUserObject();

        MenuItem toggleConfirmItem = new MenuItem();

        if ("Confirmed".equals(res.getStatus())) {
            toggleConfirmItem.setText("Unconfirm Reservation");
        } else {
            toggleConfirmItem.setText("Confirm Reservation");
        }

        toggleConfirmItem.setOnAction(e -> toggleReservationStatus(entry, res));

        menu.getItems().add(toggleConfirmItem);
        return menu;
    }







    private void customizeEntryAppearance(Entry<reservation> entry) {
        reservation res = entry.getUserObject();

        // Add checkmark to the title based on status
        if ("Confirmed".equals(res.getStatus())) {
            if (!entry.getTitle().startsWith("✓ ")) {
                entry.setTitle("✓ " + entry.getTitle());
            }
            entry.getStyleClass().add("confirmed-reservation");
        } else {
            if (entry.getTitle().startsWith("✓ ")) {
                entry.setTitle(entry.getTitle().substring(2));
            }
            entry.getStyleClass().add("pending-reservation");
        }
    }





    @FXML
    private void handleConfirmReservationAction() {
        if (selectedEntry == null) {
            showAlert("No Selection", "Please select a reservation first", AlertType.WARNING);
            return;
        }

        reservation res = (reservation) selectedEntry.getUserObject();

        Alert confirmation = new Alert(AlertType.CONFIRMATION);
        confirmation.setTitle("Confirm Reservation");
        confirmation.setHeaderText("Confirm this reservation?");
        confirmation.setContentText("Are you sure you want to confirm reservation: " + res.getTopic());

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    // Toggle status
                    String newStatus = "Confirmed".equals(res.getStatus()) ? "Not Confirmed" : "Confirmed";
                    res.setStatus(newStatus);
                    reservationService.update(res);

                    // Update entry appearance
                    updateEntryAppearance(selectedEntry, res);

                    showAlert("Success", "Reservation status updated to " + newStatus, AlertType.INFORMATION);
                } catch (SQLException e) {
                    e.printStackTrace();
                    showAlert("Error", "Failed to update reservation: " + e.getMessage(), AlertType.ERROR);
                }
            }
        });
    }

    private void updateEntryAppearance(Entry<reservation> entry, reservation res) {
        // Clear existing style classes
        entry.getStyleClass().removeAll("confirmed-reservation", "pending-reservation");

        if ("Confirmed".equals(res.getStatus())) {
            entry.getStyleClass().add("confirmed-reservation");
            // Add prefix to title if not already present
            if (!entry.getTitle().startsWith("[Confirmed] ")) {
                entry.setTitle("[Confirmed] " + entry.getTitle());
            }
        } else {
            entry.getStyleClass().add("pending-reservation");
            // Remove prefix if present
            if (entry.getTitle().startsWith("[Confirmed] ")) {
                entry.setTitle(entry.getTitle().substring("[Confirmed] ".length()));
            }
        }
    }


    private void toggleReservationStatus(Entry<reservation> entry, reservation res) {
        try {
            String newStatus = "Confirmed".equals(res.getStatus()) ? "Not Confirmed" : "Confirmed";
            res.setStatus(newStatus);
            reservationService.update(res);

            // Update the entry appearance
            customizeEntryAppearance(entry);

            showAlert("Success",
                    "Reservation status changed to " + newStatus,
                    AlertType.INFORMATION);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error",
                    "Failed to update reservation status: " + e.getMessage(),
                    AlertType.ERROR);
        }
    }



    private void loadReservations() {
        if (currentAvailability == null) return;

        reservationsCalendar.clear();

        try {
            List<reservation> reservations = reservationService.getReservationsByAvaibilityId(currentAvailability.getId());

            for (reservation res : reservations) {
                Entry<reservation> entry = new Entry<>(res.getTopic());
                LocalDateTime startDateTime = convertToLocalDateTime(res.getStart_time());
                LocalDateTime endDateTime = startDateTime.plusMinutes(res.getDuration());

                entry.setInterval(startDateTime, endDateTime);
                entry.setLocation("Tutor ID: " + currentAvailability.getTutorId());
                entry.setUserObject(res);

                // Set initial appearance
                updateEntryAppearance(entry, res);

                reservationsCalendar.addEntry(entry);
            }

            // Set calendar date to availability date
            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                Date availabilityDate = dateFormat.parse(currentAvailability.getDate());
                LocalDate localDate = availabilityDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                calendarView.setToday(localDate);
                calendarView.setDate(localDate);
            } catch (ParseException e) {
                e.printStackTrace();
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load reservations: " + e.getMessage(), AlertType.ERROR);
        }
    }



    private LocalDateTime convertToLocalDateTime(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    @FXML
    private void handleMonthViewAction() {
        calendarView.showMonthPage();
    }

    @FXML
    private void handleWeekViewAction() {
        calendarView.showWeekPage();
    }

    @FXML
    private void handleDayViewAction() {
        calendarView.showDayPage();
    }

    @FXML
    private void handleBackAction() {
        // Close this view and return to previous screen
        Stage stage = (Stage) calendarContainer.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handleAddReservationAction() {
        // TODO: Implementation for adding a new reservation
        // This would typically open a form to create a new reservation
    }

    @FXML
    private void handleRefreshAction() {
        loadReservations();
    }

    private void showReservationDetails(reservation res) {
        if (res == null) {
            return;
        }

        // Create and show a detail dialog
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Reservation Details");
        alert.setHeaderText("Reservation: " + res.getTopic());

        String content = "ID: " + res.getId() + "\n" +
                "Topic: " + res.getTopic() + "\n" +
                "Start Time: " + res.getStart_time() + "\n" +
                "Duration: " + res.getDuration() + " minutes\n" +
                "Status: " + res.getStatus() + "\n" +
                "Availability ID: " + res.getAvaibility_id();

        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showAlert(String title, String message, AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}