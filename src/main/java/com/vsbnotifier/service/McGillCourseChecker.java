package com.vsbnotifier.service;

//add necessary imports
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.Select;

import com.vsbnotifier.model.CourseInfo;


import com.vsbnotifier.model.CourseInfo; //import the CourseInfo model
import com.vsbnotifier.model.SectionInfo; //import the SectionInfo model
import io.github.bonigarcia.wdm.WebDriverManager; //webdrivermanager import

public class McGillCourseChecker {
    ChromeOptions options = new ChromeOptions(); // to set up the ChromeDriver options

    public CourseInfo checkCourseAvailability(String term, String courseCode) throws Exception {
        CourseInfo courseInfo = new CourseInfo(); // create a new CourseInfo object
        WebDriverManager.chromedriver().setup(); // setup the ChromeDriver using WebDriverManager
        WebDriver driver = new ChromeDriver(options);

        try {
            driver.get("https://vsb.mcgill.ca/criteria.jsp");

            // click on the correct term
            driver.findElement(By.linkText(term)).click();
            Thread.sleep(1500);

            // click on the box to select the course
            WebElement searchBox = driver.findElement(By.id("code_number"));
            searchBox.clear();
            searchBox.sendKeys(courseCode);
            searchBox.sendKeys(Keys.ENTER);
            Thread.sleep(1500); // give the page time to load results

            // extract the course information
            try {
                WebElement courseName = driver.findElement(By.cssSelector("input[aria-label*='" + courseCode + "']"));
                String courseNameText = courseName.getAttribute("aria-label");

                // parse course code and name from aria-label
                String[] parts = courseNameText.split(" ", 3);
                if (parts.length >= 3) {
                    courseInfo.setCourseCode(parts[0] + " " + parts[1]); // EG: COMP 370
                    courseInfo.setCourseName(parts[2]);
                } else {
                    System.out.println("Failed to parse course code and name from aria-label: " + courseNameText);
                    courseInfo.setCourseCode(courseCode);
                    courseInfo.setCourseName("Course name not found");
                }

                // step 2: find all sections of the course using the dropdown approach
                List<WebElement> allSelects = driver.findElements(By.tagName("select"));
                System.out.println("Found " + allSelects.size() + " total select elements");

                Select dropdown = null; // to see if its a dropdown or just one section

                // Find the correct dropdown that contains course section options
                for (int i = 0; i < allSelects.size(); i++) {
                    WebElement select = allSelects.get(i);
                    try {
                        Select testDropdown = new Select(select);
                        List<WebElement> options = testDropdown.getOptions();

                        // Check if this dropdown has course section options
                        boolean hasCourseSections = false;
                        for (WebElement option : options) {
                            String optionValue = option.getAttribute("value");
                            String optionText = option.getText();

                            // Look for indicators this is the course sections dropdown
                            if (optionValue.contains("us_--") ||
                                    optionText.contains("Lec") ||
                                    optionText.contains("Try all classes") ||
                                    optionText.contains("Try specific classes")) {
                                hasCourseSections = true;
                                break;
                            }
                        }

                        if (hasCourseSections) {
                            dropdown = testDropdown;
                            System.out.println("Found course sections dropdown at index " + i);
                            break;
                        }

                    } catch (Exception e) {
                        // Skip this select if it causes issues
                        continue;
                    }
                }

                Map<String, SectionInfo> sections = new HashMap<>();

                // Check if we found a course sections dropdown
                if (dropdown == null) {
                    System.out.println("No course sections dropdown found - checking for single section in table");

                    // Fallback: Use original table parsing for single-section courses
                    try {
                        WebElement courseTable = driver.findElement(By.cssSelector(".inner_legend_table"));
                        List<WebElement> rows = courseTable.findElements(By.tagName("tr"));

                        for (WebElement element : rows) {
                            List<WebElement> crnSpans = element.findElements(By.cssSelector("span[data-crns]"));
                            if (!crnSpans.isEmpty()) {
                                String crn = crnSpans.get(0).getAttribute("data-crns").trim();

                                // Use existing extraction logic
                                WebElement sectionElement = element.findElement(By.cssSelector("strong"));
                                String sectionCode = sectionElement.getAttribute("innerHTML").trim();

                                String seatStatus;
                                try {
                                    WebElement seatsElement = element.findElement(By.cssSelector("span.seatText"));
                                    seatStatus = seatsElement.getAttribute("innerHTML").trim();
                                } catch (Exception e) {
                                    // Fallback logic
                                    try {
                                        seatStatus = "Full";
                                    } catch (Exception e2) {
                                        String dataColor = crnSpans.get(0).getAttribute("data-color");
                                        if ("red".equals(dataColor)) {
                                            seatStatus = "Full";
                                        } else {
                                            seatStatus = "Unknown";
                                        }
                                    }
                                }

                                SectionInfo sectionInfo = new SectionInfo();
                                sectionInfo.setCrn(crn);
                                sectionInfo.setSectionCode(sectionCode);
                                sectionInfo.setAvailableSeats(seatStatus);
                                sections.put(crn, sectionInfo);

                                System.out.println("Found single section: CRN=" + crn + ", Section=" + sectionCode
                                        + ", Seats=" + seatStatus);
                            }
                        }
                    } catch (Exception e) {
                        System.out.println("Error with fallback table parsing: " + e.getMessage());
                    }

                } else {
                    // Use dropdown approach for multi-section courses
                    List<WebElement> options = dropdown.getOptions();
                    System.out.println("Found " + options.size() + " dropdown options");

                    for (WebElement option : options) {
                        String optionValue = option.getAttribute("value");
                        String optionText = option.getText();

                        System.out.println("Processing option: value='" + optionValue + "' text='" + optionText + "'");

                        // Only process individual lecture sections
                        if (optionValue.startsWith("us_--") && optionText.startsWith("Lec")) {
                            System.out.println("Found lecture section: " + optionText);

                            try {
                                // Select this specific section
                                dropdown.selectByValue(optionValue);
                                Thread.sleep(1250);

                                // Extract data from the current page
                                List<WebElement> rows = driver.findElements(By.cssSelector(".inner_legend_table tr"));

                                for (WebElement row : rows) {
                                    List<WebElement> crnSpans = row.findElements(By.cssSelector("span[data-crns]"));
                                    if (!crnSpans.isEmpty()) {
                                        String crn = crnSpans.get(0).getAttribute("data-crns").trim();
                                        String sectionCode = optionText;

                                        String seatStatus;
                                        try {
                                            WebElement seatSpan = row.findElement(By.cssSelector("span.seatText"));
                                            seatStatus = seatSpan.getAttribute("innerHTML").trim();
                                            if (seatStatus.isEmpty()) {
                                                seatStatus = seatSpan.getText().trim();
                                            }
                                        } catch (Exception e) {
                                            // Fallback logic
                                            String rowHTML = row.getAttribute("outerHTML");
                                            if (rowHTML.contains("Waitlist") || rowHTML.contains("waitlist")) {
                                                seatStatus = "Waitlist";
                                            } else if (rowHTML.contains("Full") || rowHTML.contains("full")) {
                                                seatStatus = "Full";
                                            } else {
                                                String dataColor = crnSpans.get(0).getAttribute("data-color");
                                                if ("red".equals(dataColor)) {
                                                    seatStatus = "Full";
                                                } else if ("green".equals(dataColor)) {
                                                    seatStatus = "Available";
                                                } else {
                                                    seatStatus = "Unknown";
                                                }
                                            }
                                        }

                                        SectionInfo sectionInfo = new SectionInfo();
                                        sectionInfo.setCrn(crn);
                                        sectionInfo.setSectionCode(sectionCode);
                                        sectionInfo.setAvailableSeats(seatStatus);

                                        sections.put(crn, sectionInfo);
                                        System.out.println("Extracted: CRN=" + crn + ", Section=" + sectionCode
                                                + ", Seats=" + seatStatus);
                                        break;
                                    }
                                }

                            } catch (Exception e) {
                                System.out.println("Error processing section " + optionText + ": " + e.getMessage());
                            }
                        }
                    }
                }

                // Set the sections in courseInfo
                courseInfo.setSections(sections);

            } catch (Exception e) {
                System.out.println("Error extracting course data: " + e.getMessage());
                e.printStackTrace();
            }

        } finally {
            // close the driver
            if (driver != null) {
                driver.quit();
            }
        }

        return courseInfo; // return the course info object
    }

    public static void main(String[] args) {

        // test it
        McGillCourseChecker checker = new McGillCourseChecker();
        try {
            CourseInfo course = checker.checkCourseAvailability("Winter 2026", "COMP 202");
            System.out.println("Sections found: " + course.getSections().size());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
