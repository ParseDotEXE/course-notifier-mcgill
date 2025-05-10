package com.example;

//add necessary imports
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
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
    public CourseInfo checkCourseAvailability(String term, String courseCode) throws IOException {
        // 1. Build URL with parameters
        // 2. Make HTTP GET request  
        // 3. Parse XML response
        // 4. Extract and return course info (including all sections)
    }
    
    // Helper method to build the URL with parameters
    private String buildUrl(String term, String courseCode) {
        // Format course code (e.g., "MATH 240" -> "MATH-240")
        // Add all required parameters
        // Include current timestamp
    }
    
    // Helper method to parse XML
    private Document parseXmlResponse(String xmlContent) throws Exception {
        // Parse XML string to Document
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
