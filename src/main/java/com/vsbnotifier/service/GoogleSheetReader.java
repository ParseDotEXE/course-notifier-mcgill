package com.vsbnotifier.service;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.vsbnotifier.main.McGillNotifier;
import com.vsbnotifier.model.UserRequest;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class GoogleSheetReader {
    //google sheet reader to fetch course data from a Google Sheet
    private static final String APPLICATION_NAME = "McGill VSB Course Notifier";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance(); //Handles JSON serialization/deserialization for API communication
    private static final String TOKENS_DIRECTORY_PATH = "tokens"; //Stores authentication tokens after the first login
    private static final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS_READONLY); //Defines permission levels your app needs (read-only access to spreadsheets)
    private static final String CREDENTIALS_FILE_PATH = "/google-credentials.json"; //Path to my OAuth credentials file from Google Cloud Console
    //operational fields
    private final String spreadsheetId;
    private final String range; //Which cells to read
    private final long checkIntervalMinutes;
    private final ScheduledExecutorService scheduler; //Manages periodic checking using a background thread
    private final McGillNotifier notifier; //Reference to the main class to process new requests
    private int lastProcessedRow = 1; //Tracks which rows you've already processed (its the header)

    //constructor
    public GoogleSheetReader(String spreadSheetId, String range, long checkIntervalMinutes, McGillNotifier notifier){
        this.spreadsheetId = spreadSheetId;
        this.range = range;
        this.checkIntervalMinutes = checkIntervalMinutes;
        this.notifier = notifier;
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
    }
    //start the periodic checking
    public void startMonitoring(){
        scheduler.scheduleAtFixedRate(this::checkForNewSubmissions, 0, checkIntervalMinutes, TimeUnit.MINUTES);
    }
    //stop the periodic checking
    public void stopMonitoring(){
        scheduler.shutdown();
    }
    //check for new submissions
    public void checkForNewSubmissions(){
        try{
            final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            Sheets service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT)).setApplicationName(APPLICATION_NAME).build();
            ValueRange response = service.spreadsheets().values().get(spreadsheetId, range).execute();
            //get the values from the response
            List<List<Object>> values = response.getValues();
            //null check
            if(values == null || values.isEmpty()){
                System.out.println("No data found.");
                return;
            }
            List<UserRequest> newRequests = new ArrayList<>();

            //process all rows after the last processed row
            for(int i = lastProcessedRow; i < values.size(); i++){
                List<Object> row = values.get(i);
                if(row.size() >= 4){
                    try{
                        //map each column to the UserRequest object
                        String timeStamp = row.get(0).toString().trim();
                        String phoneNumber = row.get(1).toString().trim();
                        String term = row.get(2).toString().trim();
                        String courseCode = row.get(3).toString().trim();

                        //craeate a new UserREquest object
                        UserRequest request = new UserRequest(courseCode, term, phoneNumber);
                        newRequests.add(request); //add the request to the list
                        System.out.println("Processed new submission: " + request);
                    } catch (Exception e){
                        System.err.println("Error processing row " + i + ": " + e.getMessage());
                    }
                }
            }
            //update last processed row
            lastProcessedRow = values.size();

            //add the new requests to the notifier
            for(UserRequest request : newRequests){
                notifier.addRequest(request); //TODO: make the addrequest method
            }
        }catch(Exception e){
            System.err.println("Error checking Google Sheet: " + e.getMessage());
            e.printStackTrace();
        }
    }

    //implement the getCredentials method
    private Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        // Load client secrets
        InputStream in = GoogleSheetReader.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        //throw null check
        if(in == null){
            throw new FileNotFoundException(CREDENTIALS_FILE_PATH + "resourcses not found :()");
        }
        //make a client secrets object
        InputStreamReader reader = new InputStreamReader(in); //create a reader and pass the input stream
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, reader); //load the client secrets from the reader
        // Build flow and trigger user authorization request
        //create a java.io.File object to store the tokens
        java.io.File tokenPath = new java.io.File(TOKENS_DIRECTORY_PATH);
        //pass the toen path to the FileDataStoreFactory
        FileDataStoreFactory dataStoreFactory = new FileDataStoreFactory(tokenPath);
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(dataStoreFactory)
                .setAccessType("offline")
                .build();
        //create a local server receiver to handle the authorization code
        LocalServerReceiver receiver = new LocalServerReceiver.Builder()
                .setPort(8888) //set the port to 8888
                .build();
        //make a new authorization code installed app
        AuthorizationCodeInstalledApp app = new AuthorizationCodeInstalledApp(flow, receiver);
        return app.authorize("user"); //authorize the user to get the credentials so we can access the Google Sheets API
    }
}
