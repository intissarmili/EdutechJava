package utils;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.ConferenceData;
import com.google.api.services.calendar.model.ConferenceSolutionKey;
import com.google.api.services.calendar.model.CreateConferenceRequest;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventAttendee;
import com.google.api.services.calendar.model.EventDateTime;

import javafx.application.Platform;
import javafx.scene.control.Alert;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Google Meet service for creating video conference links
 * Using Google Calendar API to create events with conferencing
 */
public class GoogleMeetService {
    private static final String APPLICATION_NAME = "Tutor Availability System";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    private static final List<String> SCOPES = Collections.singletonList(CalendarScopes.CALENDAR);
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";
    
    private static Calendar service;
    private static boolean initialized = false;
    
    /**
     * Initialize the Google Calendar service
     * This will prompt the user to authorize the application on first run
     */
    public static void initialize() {
        try {
            final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            Credential credential = getCredentials(HTTP_TRANSPORT);
            
            // Build the Calendar service
            service = new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                    .setApplicationName(APPLICATION_NAME)
                    .build();
            
            initialized = true;
            System.out.println("Google Meet service initialized successfully");
        } catch (Exception e) {
            System.err.println("Failed to initialize Google Meet service: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Creates a Google Calendar event with Google Meet link
     * 
     * @param tutorEmail The tutor's email address
     * @param studentEmail The student's email address 
     * @param topic Meeting topic/title
     * @param startTime Start time of the meeting
     * @param durationMinutes Duration in minutes
     * @return The Google Meet link URL or null if failed
     */
    public static String createMeetingLink(
            String tutorEmail, 
            String studentEmail, 
            String topic, 
            Date startTime, 
            int durationMinutes) {
        
        if (!initialized) {
            showAlert("Meet Service Error", "Google Meet service is not initialized", Alert.AlertType.WARNING);
            return null;
        }
        
        try {
            // Convert start time to DateTime
            DateTime startDateTime = new DateTime(startTime);
            
            // Calculate end time
            LocalDateTime endLocalDateTime = startTime.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime()
                    .plusMinutes(durationMinutes);
            
            Date endTime = Date.from(endLocalDateTime
                    .atZone(ZoneId.systemDefault())
                    .toInstant());
            
            DateTime endDateTime = new DateTime(endTime);
            
            // Create event attendees
            EventAttendee tutorAttendee = new EventAttendee()
                    .setEmail(tutorEmail)
                    .setOrganizer(true);
            
            EventAttendee studentAttendee = new EventAttendee()
                    .setEmail(studentEmail);
            
            // Create conference request (Google Meet)
            ConferenceSolutionKey conferenceSolution = new ConferenceSolutionKey()
                    .setType("hangoutsMeet");
            
            CreateConferenceRequest conferenceRequest = new CreateConferenceRequest()
                    .setRequestId(String.valueOf(System.currentTimeMillis()))
                    .setConferenceSolutionKey(conferenceSolution);
            
            ConferenceData conferenceData = new ConferenceData()
                    .setCreateRequest(conferenceRequest);
            
            // Create the event with Google Meet
            Event event = new Event()
                    .setSummary("Tutoring Session: " + topic)
                    .setDescription("Tutoring session for " + topic)
                    .setStart(new EventDateTime().setDateTime(startDateTime))
                    .setEnd(new EventDateTime().setDateTime(endDateTime))
                    .setAttendees(Arrays.asList(tutorAttendee, studentAttendee))
                    .setConferenceData(conferenceData);
            
            // Insert the event
            Event createdEvent = service.events()
                    .insert("primary", event)
                    .setConferenceDataVersion(1)
                    .setSendNotifications(true)
                    .execute();
            
            System.out.println("Event created: " + createdEvent.getHtmlLink());
            
            // Get the Google Meet link
            if (createdEvent.getConferenceData() != null && 
                createdEvent.getConferenceData().getEntryPoints() != null && 
                !createdEvent.getConferenceData().getEntryPoints().isEmpty()) {
                
                String meetLink = createdEvent.getConferenceData().getEntryPoints().get(0).getUri();
                System.out.println("Google Meet link: " + meetLink);
                return meetLink;
            } else {
                System.err.println("Failed to get Google Meet link from event");
                return createdEvent.getHtmlLink(); // Return event link as fallback
            }
            
        } catch (IOException e) {
            System.err.println("Error creating meeting: " + e.getMessage());
            e.printStackTrace();
            showAlert("Meeting Creation Error", "Failed to create meeting: " + e.getMessage(), Alert.AlertType.ERROR);
            return null;
        }
    }
    
    /**
     * Get user credentials using OAuth
     */
    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        // Load client secrets
        InputStream in = GoogleMeetService.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    /**
     * Show an alert on the JavaFX UI thread
     */
    private static void showAlert(String title, String content, Alert.AlertType type) {
        Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }
    
    /**
     * Check if Google Meet service is configured and initialized
     */
    public static boolean isAvailable() {
        return initialized;
    }
} 