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
                    if (crnSpans.isEmpty()) {
                        System.out.println("Skipping row - no data-crns found");
                        continue; // Skip this row
                    }
                    String crn = crnSpans.get(0).getAttribute("data-crns").trim(); // get the first span with data-crns

                    // get section from strong tag
                    /*
                     * WebElement sectionElement =
                     * element.findElement(By.cssSelector("strong.leftnclear.type_block"));
                     * String sectionCode = sectionElement.getText().trim(); //get the text version
                     */
                    // alternative way to get the section code
                    WebElement sectionElement = element.findElement(By.cssSelector("strong"));
                    String sectionCode = sectionElement.getAttribute("innerHTML").trim();
                    System.out.println("DEBUG - Section element found, text: '" + sectionCode + "'");

                    // get seat availability
                    String seatStatus;
                    try {
                        // Try to find available seats first
                        WebElement seatsElement = element.findElement(By.cssSelector("span.seatText"));
                        seatStatus = seatsElement.getAttribute("innerHTML").trim();
                    } catch (Exception e) {
                        // If no seatText, try to find "Full" indicator
                        try {
                            WebElement fullElement = element.findElement(By.cssSelector("span.fullText"));
                            seatStatus = "Full";
                        } catch (Exception e2) {
                            // If neither, check the background color from the span with data-crns
                            String dataColor = crnSpans.get(0).getAttribute("data-color");
                            if ("red".equals(dataColor)) {
                                seatStatus = "Full";
                            } else {
                                seatStatus = "Unknown";
                            }
                        }
                    }
                    System.out.println("DEBUG - Seat status: '" + seatStatus + "'");

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

    /*public void debugCompleteTableStructure(String term, String courseCode) throws Exception {
        WebDriverManager.chromedriver().setup();
        WebDriver driver = new ChromeDriver(options);

        try {
            driver.get("https://vsb.mcgill.ca/criteria.jsp");
            driver.findElement(By.linkText(term)).click();
            driver.findElement(By.id("code_number")).sendKeys(courseCode);
            driver.findElement(By.id("code_number")).sendKeys(Keys.ENTER);

            // Wait longer for content to load
            Thread.sleep(3000);

            // Find the legend table
            WebElement courseTable = driver.findElement(By.cssSelector(".inner_legend_table"));
            System.out.println("=== COMPLETE TABLE HTML ===");
            System.out.println(courseTable.getAttribute("outerHTML"));
            System.out.println("=== END COMPLETE TABLE HTML ===\n");

            // Get all rows
            List<WebElement> rows = courseTable.findElements(By.tagName("tr"));
            System.out.println("Found " + rows.size() + " rows in the table\n");

            // Analyze each row in detail
            for (int i = 0; i < rows.size(); i++) {
                WebElement row = rows.get(i);
                System.out.println("=== ROW " + i + " ANALYSIS ===");
                System.out.println("Row HTML: " + row.getAttribute("outerHTML"));

                // Check for CRN spans
                List<WebElement> crnSpans = row.findElements(By.cssSelector("span[data-crns]"));
                System.out.println("CRN spans found: " + crnSpans.size());
                for (int j = 0; j < crnSpans.size(); j++) {
                    System.out
                            .println("  CRN span " + j + ": data-crns = " + crnSpans.get(j).getAttribute("data-crns"));
                }

                // Check all strong elements
                List<WebElement> strongs = row.findElements(By.tagName("strong"));
                System.out.println("Strong elements found: " + strongs.size());
                for (int j = 0; j < strongs.size(); j++) {
                    System.out.println("  Strong " + j + ": text = '" + strongs.get(j).getText() + "'");
                    System.out.println(
                            "  Strong " + j + ": innerHTML = '" + strongs.get(j).getAttribute("innerHTML") + "'");
                    System.out.println("  Strong " + j + ": class = '" + strongs.get(j).getAttribute("class") + "'");
                }

                // Check all spans
                List<WebElement> spans = row.findElements(By.tagName("span"));
                System.out.println("Span elements found: " + spans.size());
                for (int j = 0; j < spans.size(); j++) {
                    String text = spans.get(j).getText();
                    String className = spans.get(j).getAttribute("class");
                    String innerHTML = spans.get(j).getAttribute("innerHTML");
                    System.out.println("  Span " + j + ": text = '" + text + "' class = '" + className + "'");
                    if (!innerHTML.isEmpty() && !innerHTML.equals(text)) {
                        System.out.println("  Span " + j + ": innerHTML = '" + innerHTML + "'");
                    }
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
            //System.out.println("=== DEBUGGING COMPLETE TABLE STRUCTURE ===");
            //checker.debugCompleteTableStructure("Fall 2025", "COMP 250");
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
