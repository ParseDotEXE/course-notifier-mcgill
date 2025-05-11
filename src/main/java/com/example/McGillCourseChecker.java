package com.example;

//add necessary imports
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.net.URI;
import java.io.IOException;
import java.io.StringReader;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;


public class McGillCourseChecker {
    private static final String API_URL = "https://vsb.mcgill.ca/api/class-data";
    
    // Method to check course availability
    public CourseInfo checkCourseAvailability(String term, String courseCode) throws IOException, InterruptedException {
        // 1. Build URL with parameters
        String url = buildUrl(term, courseCode);
        // 2. Make HTTP GET request
        HttpClient client = HttpClient.newHttpClient(); //make new client instance
        // Set up the request with headers and parameters
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/xml")
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());//send the request and get the response
        
        //check response status
        if(response.statusCode() != 200){
            throw new IOException("Failed to fetch course data. Status: " + response.statusCode());
        }
        // 3. Parse XML response
        Document doc = parseXmlResponse(response.body()); //parse the XML response
        // Check if the response contains valid data
        if (doc == null) {
            throw new IOException("Invalid XML response from server.");
        }  
        // 4. Extract and return course info (including all sections)
        
    }
    // Helper method to parse XML response
    private Document parseXmlResponse(String xmlContent) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance(); //create a new instance of DocumentBuilderFactory
        DocumentBuilder builder = factory.newDocumentBuilder(); //create a new instance of DocumentBuilder
        InputSource is = new InputSource(new StringReader(xmlContent)); //create a new InputSource from the XML string
        return builder.parse(is); //parse the InputSource and return the Document
    }

    
    // Helper method to build the URL with parameters
    private String buildUrl(String term, String courseCode) {
        // Format course code (e.g., "MATH 240" -> "MATH-240")
        // Add all required parameters
        // Include current timestamp
    }
    
    // Helper method to extract all sections info from Document
    private CourseInfo extractCourseInfo(Document doc) {
        // Find all block elements
        // Extract info for each section
        // Return CourseInfo object with all sections
    }
    
    // Inner class to hold course information
    public static class CourseInfo {
        private String courseName;
        private List<SectionInfo> sections;
        // getters/setters
    }
    
    // Inner class to hold section information
    public static class SectionInfo {
        private String sectionNumber;
        private int totalSeats;
        private int availableSeats;
        private boolean isFull;
        private int waitlistSize;
        // getters/setters
    }
}
