package com.vsbnotifier.service;

//add necessary imports
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.vsbnotifier.model.CourseInfo;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import com.vsbnotifier.model.CourseInfo; //import the CourseInfo model
import com.vsbnotifier.model.SectionInfo; //import the SectionInfo model
import net.lightbody.bmp.client.ClientUtil;
import net.lightbody.bmp.core.har.HarContent;
import net.lightbody.bmp.core.har.HarEntry;
import org.openqa.selenium.Proxy;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.*;
import org.xml.sax.SAXException;

import java.time.Duration;

public class McGillCourseChecker {
    ChromeOptions options = new ChromeOptions(); // to set up the ChromeDriver options

    public CourseInfo checkCourseAvailability(String term, String courseCode) throws Exception {
        CourseInfo courseInfo = new CourseInfo(); // create a new CourseInfo object
        BrowserMobProxy proxy = new BrowserMobProxyServer();
        proxy.start(0); // dynamic port
        // get Selenium proxy object
        Proxy seleniumProxy = ClientUtil.createSeleniumProxy(proxy);
        // configure Chrome options to use the proxy
        options.setProxy(seleniumProxy);
        // set up the ChromeDriver options
        WebDriver driver = new ChromeDriver(options);
        // record traffic
        proxy.newHar("vsb");
        driver.get("https://vsb.mcgill.ca/criteria.jsp");

        // TODO: selenium logic to navigate page, get course infomation

        // click on the correct term
        driver.findElement(By.linkText(term)).click();
        // click on the box to select the course
        driver.findElement(By.id("code_number")).sendKeys(courseCode); // click and enter the course code
        driver.findElement(By.id("code_number")).sendKeys(Keys.ENTER); // click enter
        // create a new waitdriver object
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(3)); // wait for 3 seconds
        // get all requests and responses in the network

        // boolean to check if the course is found
        java.util.concurrent.atomic.AtomicBoolean found = new java.util.concurrent.atomic.AtomicBoolean(false);

        proxy.getHar().getLog().getEntries().forEach(entry -> {
            String url = entry.getRequest().getUrl();
            // Check if the URL contains "class-data?"
            if (url.contains("class-data?")) {
                System.out.println("Found matching URL: " + url);
                System.out.println("Status code: " + entry.getResponse().getStatus());
                found.set(true); // set the found boolean to true
            }
        });
        AtomicReference<HarEntry> matchedEntry = new AtomicReference<>(); // atomic reference because we need to capture
                                                                          // the entry later
        if (!found.get()) {
            driver.navigate().refresh(); // refresh the page if the class-data? is not found
            proxy.newHar("vsb-retry"); // Start fresh recording
            // Wait a bit for new requests
            Thread.sleep(2000);
            // Recheck the HAR entries
            proxy.getHar().getLog().getEntries().forEach(entry -> {
                String url = entry.getRequest().getUrl();
                // Check if the URL contains "class-data?"
                if (url.contains("class-data?") && !found.get()) {
                    // capture entry
                    matchedEntry.set(entry);
                    found.set(true); // its been found
                }
                // get response and extract the course information
                HarContent content = entry.getResponse().getContent();
                String body = content.getText(); // transofrm into xml string body

                // prse the xml body to extract the crn, section code, and available seats
                if (body == null || body.isEmpty()) {
                    System.out.println("No content found in the response.");
                }
                // parse
                // create document builder factory
                try {
                    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder builder = factory.newDocumentBuilder();
                    // parse into document
                    Document doc = builder.parse(new ByteArrayInputStream(body.getBytes()));

                    // normalize the document
                    doc.getDocumentElement().normalize();
                    // get the block element
                    NodeList block = doc.getElementsByTagName("block");
                    // get the course element
                    NodeList course = doc.getElementsByTagName("offering");

                    // loop through the block elements and find the course information
                    Map<String, SectionInfo> sections = new HashMap<>(); // map to store the sections
                    // get the courseCode and courseName
                    for (int j = 0; j < course.getLength(); j++) {
                        // get the course element
                        Element courseElement = (Element) course.item(j);
                        String courseCodeText = courseElement.getAttribute("key");
                        String courseNameText = courseElement.getAttribute("title");
                        // set the course code and name in the courseInfo object
                        courseInfo.setCourseCode(courseCodeText); // set the course code
                        courseInfo.setCourseName(courseNameText); // set the course name
                        for (int i = 0; i < block.getLength(); i++) {
                            // get the block element
                            Element sectionElement = (Element) block.item(i);
                            // get the crn, section code, and available seats
                            String crn = sectionElement.getAttribute("key");
                            String sectionCode = sectionElement.getAttribute("disp");
                            String availableSeats = sectionElement.getAttribute("os");
                            // store inside a SectionInfo object
                            SectionInfo sectionInfo = new SectionInfo(crn, sectionCode, availableSeats);
                            // add to the courseInfo object
                            sections.put(crn, sectionInfo); // use crn as key
                        }
                    }
                    courseInfo.setSections(sections); // set the sections in the courseInfo object
                } catch (ParserConfigurationException | SAXException | IOException e) {
                    e.printStackTrace();
                }

            });
        }
        driver.quit(); // quit the driver
        proxy.stop(); // stop the proxy
        System.out.println("Course Code: " + courseInfo.getCourseCode());
        System.out.println("Course Name: " + courseInfo.getCourseName());
        System.out
                .println("Sections found: " + (courseInfo.getSections() != null ? courseInfo.getSections().size() : 0));
        if (courseInfo.getSections() != null) {
            for (SectionInfo section : courseInfo.getSections().values()) {
                System.out.println("CRN: " + section.getCrn() +
                        ", Section: " + section.getSectionCode() +
                        ", Available Seats: " + section.getAvailableSeats());
            }
        }
        return courseInfo;
    }

    public static void main(String[] args) {
        try {
            McGillCourseChecker checker = new McGillCourseChecker();
            // Test with a known course and term
            CourseInfo course = checker.checkCourseAvailability("Fall 2025", "COMP 370");
            // Print out all extracted info for verification
            System.out.println("Course Code: " + course.getCourseCode());
            System.out.println("Course Name: " + course.getCourseName());
            if (course.getSections() != null && !course.getSections().isEmpty()) {
                System.out.println("Sections found: " + course.getSections().size());
                for (SectionInfo section : course.getSections().values()) {
                    System.out.println("CRN: " + section.getCrn() +
                            ", Section: " + section.getSectionCode() +
                            ", Available Seats: " + section.getAvailableSeats());
                }
            } else {
                System.out.println("No sections found or course not available.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
