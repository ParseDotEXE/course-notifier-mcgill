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
import org.openqa.selenium.support.ui.Select;
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


    public void debugSeatInfo(String term, String courseCode) throws Exception {
        WebDriverManager.chromedriver().setup();
        WebDriver driver = new ChromeDriver(options);

        try {
            driver.get("https://vsb.mcgill.ca/criteria.jsp");
            driver.findElement(By.linkText(term)).click();
            Thread.sleep(2000);

            WebElement searchBox = driver.findElement(By.id("code_number"));
            searchBox.clear();
            searchBox.sendKeys(courseCode);
            searchBox.sendKeys(Keys.ENTER);
            Thread.sleep(3000);

            // Get the correct dropdown and select Lec 001
            List<WebElement> allSelects = driver.findElements(By.tagName("select"));
            WebElement correctSelect = allSelects.get(3);
            Select dropdown = new Select(correctSelect);

            // Select Lec 001 specifically
            dropdown.selectByValue("us_--202509_2404--");
            System.out.println("Selected Lec 001");
            Thread.sleep(3000);

            System.out.println("=== DEBUGGING SEAT INFORMATION ===");

            // Get the table rows and examine them in detail
            List<WebElement> rows = driver.findElements(By.cssSelector(".inner_legend_table tr"));
            System.out.println("Found " + rows.size() + " table rows");

            for (int i = 0; i < rows.size(); i++) {
                WebElement row = rows.get(i);
                System.out.println("\n--- ROW " + i + " DETAILED ANALYSIS ---");

                // Print the entire row HTML
                System.out.println("Full HTML: " + row.getAttribute("outerHTML"));

                // Print all text content
                System.out.println("Row text: '" + row.getText() + "'");

                // Look for ALL span elements in this row
                List<WebElement> allSpans = row.findElements(By.tagName("span"));
                System.out.println("Found " + allSpans.size() + " spans in this row:");

                for (int j = 0; j < allSpans.size(); j++) {
                    WebElement span = allSpans.get(j);
                    String spanText = span.getText();
                    String spanHTML = span.getAttribute("innerHTML");
                    String spanClass = span.getAttribute("class");
                    String spanId = span.getAttribute("id");

                    System.out.println("  Span " + j + ":");
                    System.out.println("    Text: '" + spanText + "'");
                    System.out.println("    HTML: '" + spanHTML + "'");
                    System.out.println("    Class: '" + spanClass + "'");
                    System.out.println("    ID: '" + spanId + "'");

                    // Check if this might be seat information
                    if (spanText.matches("\\d+") || spanText.contains("Full") || spanText.contains("available") ||
                            spanText.contains("seat") || spanClass.contains("seat") || spanId.contains("seat")) {
                        System.out.println("    *** POTENTIAL SEAT INFO! ***");
                    }
                }

                // Look for ALL td elements in this row
                List<WebElement> allTds = row.findElements(By.tagName("td"));
                System.out.println("Found " + allTds.size() + " td elements in this row:");

                for (int k = 0; k < allTds.size(); k++) {
                    WebElement td = allTds.get(k);
                    String tdText = td.getText();
                    String tdHTML = td.getAttribute("innerHTML");
                    String tdClass = td.getAttribute("class");

                    System.out.println("  TD " + k + ":");
                    System.out.println("    Text: '" + tdText + "'");
                    System.out.println("    HTML: '" + tdHTML + "'");
                    System.out.println("    Class: '" + tdClass + "'");

                    if (tdText.matches("\\d+") || tdText.contains("Full") || tdText.contains("available")) {
                        System.out.println("    *** POTENTIAL SEAT INFO IN TD! ***");
                    }
                }
            }

            // Also check outside the table for seat information
            System.out.println("\n=== CHECKING OUTSIDE TABLE FOR SEAT INFO ===");

            // Look for any element containing numbers that might be seats
            List<WebElement> numberElements = driver.findElements(By.xpath("//*[text()[matches(., '\\d+')]]"));
            System.out.println("Found " + numberElements.size() + " elements containing numbers:");

            for (int i = 0; i < Math.min(10, numberElements.size()); i++) {
                WebElement elem = numberElements.get(i);
                String text = elem.getText();
                String tag = elem.getTagName();
                String className = elem.getAttribute("class");

                if (text.trim().matches("\\d+") && !text.equals("202509") && !text.equals("2404")
                        && !text.equals("2405")) {
                    System.out.println("  Element " + i + ": <" + tag + "> '" + text + "' class='" + className + "'");
                }
            }

        } finally {
            driver.quit();
        }
    }

    public static void main(String[] args) {
        try {
            McGillCourseChecker checker = new McGillCourseChecker();

            // Run the dropdown flow test
            // System.out.println("=== TESTING DROPDOWN FLOW ===");
            // checker.testDropdownFlow("Fall 2025", "COMP 202");

            // Run test select with Select class
            // System.out.println("=== TESTING SELECT WITH SELECT CLASS ===");
            // checker.testSelectWithSelectClass("Fall 2025", "COMP 202");

            // Run the debug dropdown test
            // System.out.println("=== DEBUGGING DROPDOWN ELEMENTS ===");
            // checker.debugDropdown("Fall 2025", "COMP 202");

            // Run the debug select options test
            // System.out.println("=== DEBUGGING SELECT OPTIONS ===");
            // checker.debugSelectOptions("Fall 2025", "MATH 240");

            // Run the test select by value
            // System.out.println("=== TESTING SELECT BY VALUE ===");
            // checker.testSelectByValue("Fall 2025", "COMP 202");
            // Run the test JavaScript select
            // System.out.println("=== TESTING JAVASCRIPT SELECT ===");
            // checker.testJavaScriptSelect("Fall 2025", "COMP 202");
            // Run the test with longer wait
            // System.out.println("=== TESTING WITH LONGER WAIT ===");
            // checker.testWithLongerWait("Fall 2025", "COMP 202");
            // Run the debug what happens test
            // System.out.println("=== DEBUGGING WHAT HAPPENS ===");
            // checker.debugWhatHappens("Fall 2025", "COMP 202");
            // Run the debug page state test
            // System.out.println("=== DEBUGGING PAGE STATE ===");
            // checker.debugPageState("Fall 2025", "COMP 202");
            // Run the fixed dropdown and button test
            // System.out.println("=== FIXED DROPDOWN AND BUTTON ===");
            // checker.fixedDropdownAndButton("Fall 2025", "COMP 202");
            // Run the debug all selects test
            // System.out.println("=== DEBUGGING ALL SELECTS ===");
            // checker.debugAllSelects("Fall 2025", "COMP 202");
            // Run the test individual sections
            //System.out.println("=== TESTING INDIVIDUAL SECTIONS ===");
            //checker.testIndividualSections("Fall 2025", "COMP 202");
            // Run the debug seat info test
            System.out.println("=== DEBUGGING SEAT INFO ===");
            checker.debugSeatInfo("Fall 2025", "COMP 202");
            // run the test to select dropdown
            // System.out.println("=== TESTING SELECT DROPDOWN ===");
            // checker.testSelectDropdown("Fall 2025", "COMP 202");

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
