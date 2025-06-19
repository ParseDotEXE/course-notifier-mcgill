package com.vsbnotifier.service;

//add necessary imports
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
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
            WebElement lectures = driver.findElement(By.cssSelector("h4[aria-label*='" + courseCode + "']"));
            lectures.click(); // click on the sections to expand them
            System.out.println("Clicked on course title to expand sections");
            Thread.sleep(2000); // Wait for sections to load
        } catch (Exception e) {
            System.out.println("Could not click course title: " + e.getMessage());
        }
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

    public void debugCourseTitle(String term, String courseCode) throws Exception {
        WebDriverManager.chromedriver().setup();
        WebDriver driver = new ChromeDriver(options);

        try {
            driver.get("https://vsb.mcgill.ca/criteria.jsp");
            driver.findElement(By.linkText(term)).click();
            driver.findElement(By.id("code_number")).sendKeys(courseCode);
            driver.findElement(By.id("code_number")).sendKeys(Keys.ENTER);
            Thread.sleep(3000);

            System.out.println("=== SEARCHING FOR COURSE TITLE ELEMENTS ===");

            // Search for any elements containing the course code
            List<WebElement> elementsWithCourseCode = driver
                    .findElements(By.xpath("//*[contains(text(), '" + courseCode + "')]"));
            System.out.println("Found " + elementsWithCourseCode.size() + " elements containing '" + courseCode + "':");

            for (int i = 0; i < elementsWithCourseCode.size(); i++) {
                WebElement elem = elementsWithCourseCode.get(i);
                System.out.println("Element " + i + ":");
                System.out.println("  Tag: " + elem.getTagName());
                System.out.println("  Text: '" + elem.getText() + "'");
                System.out.println("  Class: '" + elem.getAttribute("class") + "'");
                System.out.println("  Clickable: " + elem.isEnabled());
                System.out.println("  HTML: " + elem.getAttribute("outerHTML"));
                System.out.println();
            }

            // Also search for elements containing "FOUNDATIONS"
            List<WebElement> elementsWithFoundations = driver
                    .findElements(By.xpath("//*[contains(text(), 'FOUNDATIONS')]"));
            System.out.println("Found " + elementsWithFoundations.size() + " elements containing 'FOUNDATIONS':");

            for (int i = 0; i < elementsWithFoundations.size(); i++) {
                WebElement elem = elementsWithFoundations.get(i);
                System.out.println("Element " + i + ":");
                System.out.println("  Tag: " + elem.getTagName());
                System.out.println("  Text: '" + elem.getText() + "'");
                System.out.println("  Class: '" + elem.getAttribute("class") + "'");
                System.out.println("  Clickable: " + elem.isEnabled());
                System.out.println();
            }

        } finally {
            driver.quit();
        }
    }

    public void testClickableElements(String term, String courseCode) throws Exception {
        WebDriverManager.chromedriver().setup();
        WebDriver driver = new ChromeDriver(options);

        try {
            driver.get("https://vsb.mcgill.ca/criteria.jsp");
            driver.findElement(By.linkText(term)).click();
            //wait a bit
            Thread.sleep(2000);
            WebElement searchBox =  driver.findElement(By.id("code_number"));
            searchBox.sendKeys(courseCode);
            searchBox.sendKeys(Keys.ENTER);
            Thread.sleep(3000);

            System.out.println("=== TESTING DIFFERENT CLICKABLE ELEMENTS ===");

            // Test 1: Try the tooltip "Expand detail" div
            System.out.println("\n--- Test 1: Clicking 'Expand detail' tooltip ---");
            try {
                WebElement expandTooltip = driver.findElement(By.cssSelector("div.cnf_tip_expand"));
                expandTooltip.click();
                Thread.sleep(2000);

                // Check how many table rows we have now
                List<WebElement> rows = driver.findElements(By.cssSelector(".inner_legend_table tr"));
                System.out.println("After clicking tooltip: Found " + rows.size() + " table rows");

            } catch (Exception e) {
                System.out.println("Tooltip click failed: " + e.getMessage());
            }

            // Test 2: Try clicking the h4 course title
            System.out.println("\n--- Test 2: Clicking h4 course title ---");
            try {
                WebElement courseTitle = driver.findElement(By.cssSelector("h4.course_title"));
                courseTitle.click();
                Thread.sleep(2000);

                // Check table rows again
                List<WebElement> rows = driver.findElements(By.cssSelector(".inner_legend_table tr"));
                System.out.println("After clicking h4: Found " + rows.size() + " table rows");

            } catch (Exception e) {
                System.out.println("H4 click failed: " + e.getMessage());
            }

            // Test 3: Try clicking the span with course code
            System.out.println("\n--- Test 3: Clicking span with course code ---");
            try {
                WebElement courseSpan = driver
                        .findElement(By.xpath("//span[@class='nonmobile' and contains(text(), '" + courseCode + "')]"));
                courseSpan.click();
                Thread.sleep(2000);

                // Check table rows again
                List<WebElement> rows = driver.findElements(By.cssSelector(".inner_legend_table tr"));
                System.out.println("After clicking span: Found " + rows.size() + " table rows");

            } catch (Exception e) {
                System.out.println("Span click failed: " + e.getMessage());
            }

            // Final check: Print all current table content
            System.out.println("\n=== FINAL TABLE STATE ===");
            try {
                List<WebElement> finalRows = driver.findElements(By.cssSelector(".inner_legend_table tr"));
                System.out.println("Total rows in table: " + finalRows.size());

                for (int i = 0; i < finalRows.size(); i++) {
                    List<WebElement> crnSpans = finalRows.get(i).findElements(By.cssSelector("span[data-crns]"));
                    if (!crnSpans.isEmpty()) {
                        String crn = crnSpans.get(0).getAttribute("data-crns");
                        WebElement strong = finalRows.get(i).findElement(By.cssSelector("strong"));
                        String section = strong.getAttribute("innerHTML");
                        System.out.println("Row " + i + ": CRN=" + crn + ", Section=" + section);
                    }
                }
            } catch (Exception e) {
                System.out.println("Error reading final table: " + e.getMessage());
            }

        } finally {
            driver.quit();
        }
    }

    public static void main(String[] args) {
        try {
            McGillCourseChecker checker = new McGillCourseChecker();

            // Run the debug first
            //System.out.println("=== DEBUGGING COMPLETE TABLE STRUCTURE ===");
            //checker.debugCourseTitle("Fall 2025", "COMP 202");

            // Run the clickable elements test
            System.out.println("=== TESTING CLICKABLE ELEMENTS ===");
            checker.testClickableElements("Fall 2025", "COMP 202");

            // CourseInfo course = checker.checkCourseAvailability("Fall 2025", "COMP 202");

            // Print out all extracted info for verification
            // System.out.println("Course Code: " + course.getCourseCode());
            // System.out.println("Course Name: " + course.getCourseName());

            /*
             * if (course.getSections() != null && !course.getSections().isEmpty()) {
             * System.out.println("Sections found: " + course.getSections().size());
             * for (SectionInfo section : course.getSections().values()) {
             * System.out.println("CRN: " + section.getCrn() +
             * ", Section: " + section.getSectionCode() +
             * ", Available Seats: " + section.getAvailableSeats());
             * }
             * } else {
             * System.out.println("No sections found or course not available.");
             * }
             */
        } catch (Exception e) {
            System.out.println("Error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
