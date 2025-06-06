package com.vsbnotifier.service;

//add necessary imports
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
import java.net.URLEncoder;
import java.io.IOException;
import java.io.StringReader;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import com.vsbnotifier.model.CourseInfo; //import the CourseInfo model
import com.vsbnotifier.model.SectionInfo; //import the SectionInfo model

public class McGillCourseChecker {
    private static final String API_URL = "https://vsb.mcgill.ca/api/class-data";

    // Method to check course availability
    public CourseInfo checkCourseAvailability(String term, String courseCode) throws Exception {

        // 1. Create HTTP client with cookie support
        CookieManager cookieManager = new CookieManager(); // new CookieManager instance
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL); // accept all cookies
        // build the HttpClient with the cookie manager
        HttpClient client = HttpClient.newBuilder().cookieHandler(cookieManager).build();

        // 2. visit the main VSB page to make session and set cookies
        String termEncoded = URLEncoder.encode(term, StandardCharsets.UTF_8);
        String urlString = "https://vsb.mcgill.ca/criteria.jsp?term=" + termEncoded;
        URI uri = URI.create(urlString);
        // main page URL
        // request to visit the main page
        HttpRequest mainPageRequest = HttpRequest.newBuilder()
                .uri(URI.create(urlString)) // set the URI
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36") // set user agent
                .GET() // set request method to GET
                .build(); // build the request
        // send the request and check the response to ensure the session is established
        HttpResponse<String> mainPageResponse = client.send(mainPageRequest, HttpResponse.BodyHandlers.ofString()); // send
                                                                                                                    // the
                                                                                                                    // request
        if (mainPageResponse.statusCode() != 200) {
            throw new IOException("Failed to establish session with VSB main page.");
        }

        // 3. make API call to get course data
        String url = buildUrl(term, courseCode); // build the URL with parameters
        System.out.println("API REQUEST URL: " + url); // <-- Add this line        // request to get course data
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url)) // set the URI
                .header("Accept", "application/xml, text/xml, */*; q=0.01") // set accept header
                .header("Referer", urlString) // set referer header
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36") // set user agent
                .header("X-Requested-With", "XMLHttpRequest") // set X-Requested-With header (important missing header
                                                              // from last time)
                .GET() // set request method to GET
                .build(); // build the request
        // send the request and check the response
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString()); // send the request
        if (response.statusCode() != 200) {
            throw new IOException("Failed to fetch course data from VSB API.");
        }
        System.out.println(java.util.TimeZone.getDefault());
        System.out.println(java.time.ZonedDateTime.now());
        System.out.println("RAW XML RESPONSE:");
        System.out.println(response.body());

        // 4. Parse XML response
        Document doc = parseXmlResponse(response.body()); // parse the XML response
        // Check if the response contains valid data
        if (doc == null) {
            throw new IOException("Invalid XML response from server.");
        }
        // 5. Extract and return course info (including all sections)
        return extractCourseInfo(doc); // Add this return statement

    }

    // Helper method to parse XML response
    private Document parseXmlResponse(String xmlContent) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(xmlContent));
            return builder.parse(is);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Helper method to build the URL with parameters
    private String buildUrl(String term, String courseCode) {
        String termEncoded = URLEncoder.encode(term, StandardCharsets.UTF_8);
        String formattedCourseCode = URLEncoder.encode(courseCode.replace(" ", "-"), StandardCharsets.UTF_8);
        
        // Hard-code va_0_0 for testing
        String vaParam = "2ce0"; // <-- Replace with the value from your browser

        return API_URL + "?" +
                "term=" + termEncoded + // encoded term parameter
                "&course_0_0=" + formattedCourseCode + // encoded course code
                "&va_0_0=" + vaParam + // <-- Add this line
                "&rq_0_0=" + // request code
                "&t=438" + // request time
                "&e=27" + // event code
                "&nouser=1" + // no user
                "&_=" + System.currentTimeMillis(); // current timestamp
    }

    // Helper method to extract all sections info from Document
    private CourseInfo extractCourseInfo(Document doc) {
        CourseInfo courseInfo = new CourseInfo();
        Map<String, SectionInfo> sectionMap = new HashMap<>();

        // 1. Extract course-level information
        NodeList offeringNodes = doc.getElementsByTagName("offering");
        if (offeringNodes.getLength() > 0) {
            Element offeringElement = (Element) offeringNodes.item(0);
            String courseTitle = offeringElement.getAttribute("title");
            String courseDesc = offeringElement.getAttribute("desc");
            courseInfo.setCourseCode(courseTitle);
            courseInfo.setCourseName(courseDesc);
        }

        // 2. Extract all sections directly into the map
        NodeList blocks = doc.getElementsByTagName("block");
        for (int i = 0; i < blocks.getLength(); i++) {
            Element block = (Element) blocks.item(i);
            String crn = block.getAttribute("key");
            String availableSeats = block.getAttribute("os");
            String courseSection = block.getAttribute("secNo");
            SectionInfo sectionInfo = new SectionInfo(crn, courseSection, availableSeats);
            sectionMap.put(crn, sectionInfo); // Directly add to the map
        }
        courseInfo.setSections(sectionMap);
        return courseInfo;
    }
}
