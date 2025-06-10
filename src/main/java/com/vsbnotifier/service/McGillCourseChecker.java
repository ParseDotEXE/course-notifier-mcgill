package com.vsbnotifier.service;

//add necessary imports
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
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
import io.github.bonigarcia.wdm.WebDriverManager; //webdrivermanager import
import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.*;
import org.xml.sax.SAXException;

import java.time.Duration;

public class McGillCourseChecker {
    ChromeOptions options = new ChromeOptions(); // to set up the ChromeDriver options

    public CourseInfo checkCourseAvailability(String term, String courseCode) throws Exception {
        CourseInfo courseInfo = new CourseInfo(); // create a new CourseInfo object
        WebDriverManager.chromedriver().setup(); // setup the ChromeDriver using WebDriverManager
        // get Selenium proxy object
        // set up the ChromeDriver options
        WebDriver driver = new ChromeDriver(options);

        driver.get("https://vsb.mcgill.ca/criteria.jsp");

        // TODO: selenium logic to navigate page, get course infomation

        // click on the correct term
        driver.findElement(By.linkText(term)).click();
        // click on the box to select the course
        driver.findElement(By.id("code_number")).sendKeys(courseCode); // click and enter the course code
        driver.findElement(By.id("code_number")).sendKeys(Keys.ENTER); // click enter
        // create a new waitdriver object
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(3)); // wait for 3 seconds
        Thread.sleep(3000); // give the page time to load results
        // extract the course infomation
        try {
            WebElement courseName = driver.findElement(By.cssSelector("input[aria-label*='" + courseCode + "']"));
            // transform to string
            String courseNameText = courseName.getAttribute("aria-label");

            // parse course code and name from aria-label
            String[] parts = courseNameText.split(" ", 3);
            if (parts.length >= 3) {
                courseInfo.setCourseCode(parts[0] + " " + parts[1]); // EG: COMP 370
                courseInfo.setCourseName(parts[2]);
            } else {
                // if parsing fails
                System.out.println("Failed to parse course code and name from aria-label: " + courseNameText);
                courseInfo.setCourseCode(courseCode); // if parsing fails, set course code to the input
                courseInfo.setCourseName("No courses found bruh sorry twin");
            }
            // step 2: find all sections of the course
            WebElement courseTable = driver.findElement(By.cssSelector(".inner_legend_table")); // find the course table
            System.out.println("Found legend table!");

            List<WebElement> rows = courseTable.findElements(By.tagName("tr")); // get all rows in the table
            System.out.println("Found " + rows.size() + " rows in the table");

            Map<String, SectionInfo> sections = new HashMap<>(); // create a map to store sections
           
            // extract each row's information
            // for each web element in the rows
            for (WebElement element : rows) {
                try {
                    // get the crn first
                    List<WebElement> crnSpans = element.findElements(By.cssSelector("span[data-crns]"));
                    if(crnSpans.isEmpty()) {
                        System.out.println("Skipping row - no data-crns found");
                        continue; // Skip this row
                    }
                    String crn = crnSpans.get(0).getAttribute("data-crns").trim(); // get the first span with data-crns

                    // get section from strong tag
                    /*WebElement sectionElement = element.findElement(By.cssSelector("strong.leftnclear.type_block"));
                    String sectionCode = sectionElement.getText().trim(); //get the text version*/
                    //alternative way to get the section code
                    WebElement sectionElement = element.findElement(By.cssSelector("strong"));
                    String sectionCode = sectionElement.getText().trim();
                    System.out.println("DEBUG - Section element found, text: '" + sectionCode + "'");
                    
                    // get seat availability
                    /*WebElement seatsElement = element.findElement(By.cssSelector("span.seatText"));
                    String seatStatus = seatsElement.getText().trim();*/
                    //alternative way to get the seat availability
                    WebElement seatsElement = element.findElement(By.cssSelector("span.seatText"));
                    String seatStatus = seatsElement.getText().trim();
                    System.out.println("DEBUG - Seats element found, text: '" + seatStatus + "'");

                    // create a new sectionInfo object and add to the map
                    SectionInfo sectionInfo = new SectionInfo();
                    sectionInfo.setCrn(crn); // set the crn
                    sectionInfo.setSectionCode(sectionCode); // set the section code
                    sectionInfo.setAvailableSeats(seatStatus); // set the available seats
                    sections.put(crn, sectionInfo);
                    System.out.println(
                            "Found section - CRN: " + crn + ", Section: " + sectionCode + ", Status: " + seatStatus);
                } catch (Exception e) {
                    // handle error
                    System.out.println("Error processing row: " + e.getMessage());
                }
            }
            courseInfo.setSections(sections); // set the sections in the courseInfo object
        } catch (Exception e) {
            System.out.println("Error extracting course data: " + e.getMessage());
            e.printStackTrace();
        }
        driver.quit(); // close the driver
        return courseInfo; // return the course info object
    }


    /*public void debugTableStructure(String term, String courseCode) throws Exception {
        WebDriverManager.chromedriver().setup();
        WebDriver driver = new ChromeDriver(options);

        try {
            driver.get("https://vsb.mcgill.ca/criteria.jsp");
            driver.findElement(By.linkText(term)).click();
            driver.findElement(By.id("code_number")).sendKeys(courseCode);
            driver.findElement(By.id("code_number")).sendKeys(Keys.ENTER);
            Thread.sleep(3000);

            // Debug the table structure
            WebElement courseTable = driver.findElement(By.cssSelector(".inner_legend_table"));
            List<WebElement> rows = courseTable.findElements(By.tagName("tr"));
            System.out.println("Found " + rows.size() + " rows in the table");

            for (int i = 0; i < rows.size(); i++) {
                WebElement row = rows.get(i);
                System.out.println("=== ROW " + i + " ===");
                System.out.println("Row HTML: " + row.getAttribute("outerHTML"));

                List<WebElement> spans = row.findElements(By.tagName("span"));
                List<WebElement> strongs = row.findElements(By.tagName("strong"));

                System.out.println("Found " + spans.size() + " spans and " + strongs.size() + " strong tags");

                for (int j = 0; j < spans.size(); j++) {
                    String dataCrns = spans.get(j).getAttribute("data-crns");
                    String spanText = spans.get(j).getText();
                    String spanClass = spans.get(j).getAttribute("class");
                    if (dataCrns != null && !dataCrns.isEmpty()) {
                        System.out.println("Span " + j + " has data-crns: " + dataCrns);
                    }
                    if (!spanText.isEmpty()) {
                        System.out.println("Span " + j + " text: '" + spanText + "' class: '" + spanClass + "'");
                    }
                }

                for (int j = 0; j < strongs.size(); j++) {
                    String strongText = strongs.get(j).getText();
                    System.out.println("Strong " + j + " text: '" + strongText + "'");
                }
                System.out.println("=== END ROW " + i + " ===\n");
            }

        } finally {
            driver.quit();
        }
    }*/

    public static void main(String[] args) {
        try {
            McGillCourseChecker checker = new McGillCourseChecker();

            // Run the debug first
            //System.out.println("=== DEBUGGING TABLE STRUCTURE ===");
            //checker.debugTableStructure("Fall 2025", "COMP 250");
            // Test with a known course and term
            CourseInfo course = checker.checkCourseAvailability("Fall 2025", "COMP 250");

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
            System.out.println("Error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
