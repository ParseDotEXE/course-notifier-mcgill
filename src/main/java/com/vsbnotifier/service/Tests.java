package com.vsbnotifier.service;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.Select;

import io.github.bonigarcia.wdm.WebDriverManager;

public class Tests {

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

    public void testDropdownFlow(String term, String courseCode) throws Exception {
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

            System.out.println("=== LOOKING FOR DROPDOWN ===");

            // Step 1: Find and click the "Try all classes (2)" dropdown
            WebElement dropdown = driver.findElement(
                    By.xpath("//select[contains(@class, 'form-control') or contains(text(), 'Try all classes')]"));
            dropdown.click();
            Thread.sleep(1000);

            System.out.println("Clicked dropdown, looking for sections...");

            // Step 2: Check what section options are available
            List<WebElement> options = driver.findElements(By.tagName("option"));
            for (WebElement option : options) {
                System.out.println("Option: " + option.getText());
            }

            // Step 3: Click "Generate Schedules"
            WebElement generateBtn = driver.findElement(By.xpath("//button[contains(text(), 'GENERATE SCHEDULES')]"));
            generateBtn.click();
            Thread.sleep(3000);

            System.out.println("Clicked Generate Schedules, checking table...");

            // Step 4: Check the table now
            List<WebElement> rows = driver.findElements(By.cssSelector(".inner_legend_table tr"));
            System.out.println("Found " + rows.size() + " table rows after Generate Schedules");

        } finally {
            driver.quit();
        }
    }

    public void debugDropdown(String term, String courseCode) throws Exception {
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

            System.out.println("=== SEARCHING FOR DROPDOWN ELEMENTS ===");

            // Search for ANY element containing "Try all classes"
            List<WebElement> dropdownElements = driver
                    .findElements(By.xpath("//*[contains(text(), 'Try all classes')]"));
            System.out.println("Found " + dropdownElements.size() + " elements containing 'Try all classes':");

            for (int i = 0; i < dropdownElements.size(); i++) {
                WebElement elem = dropdownElements.get(i);
                System.out.println("Element " + i + ":");
                System.out.println("  Tag: " + elem.getTagName());
                System.out.println("  Text: '" + elem.getText() + "'");
                System.out.println("  Class: '" + elem.getAttribute("class") + "'");
                System.out.println("  HTML: " + elem.getAttribute("outerHTML"));
            }

        } finally {
            driver.quit();
        }
    }

    public void testSelectWithSelectClass(String term, String courseCode) throws Exception {
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

            System.out.println("=== TESTING WITH SELECT CLASS ===");

            try {
                // Find the select element
                WebElement selectElement = driver
                        .findElement(By.xpath("//select[.//option[contains(text(), 'Try all classes')]]"));
                System.out.println("Found select element");

                // Use Selenium's Select class instead of direct clicking
                Select dropdown = new Select(selectElement);

                System.out.println("Available options:");
                List<WebElement> options = dropdown.getOptions();
                for (int i = 0; i < options.size(); i++) {
                    System.out.println("  Option " + i + ": " + options.get(i).getText());
                }

                // Select the "Try all classes (2)" option by visible text
                dropdown.selectByVisibleText("Try all classes (2)");
                System.out.println("Selected 'Try all classes (2)' using Select class");

                Thread.sleep(2000);

                // Look for Generate Schedules button
                try {
                    WebElement generateBtn = driver
                            .findElement(By.xpath("//button[contains(text(), 'GENERATE SCHEDULES')]"));
                    generateBtn.click();
                    System.out.println("Clicked Generate Schedules button");
                    Thread.sleep(3000);

                    // Check table for sections
                    List<WebElement> rows = driver.findElements(By.cssSelector(".inner_legend_table tr"));
                    System.out.println("Found " + rows.size() + " table rows after Generate Schedules");

                } catch (Exception e) {
                    System.out.println("Could not find Generate Schedules: " + e.getMessage());
                }

            } catch (Exception e) {
                System.out.println("Error with Select dropdown: " + e.getMessage());
            }

        } finally {
            driver.quit();
        }
    }

    public void debugSelectOptions(String term, String courseCode) throws Exception {
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

            System.out.println("=== DEBUGGING SELECT OPTIONS ===");

            try {
                WebElement selectElement = driver
                        .findElement(By.xpath("//select[.//option[contains(text(), 'Try all classes')]]"));
                Select dropdown = new Select(selectElement);

                List<WebElement> options = dropdown.getOptions();
                System.out.println("Found " + options.size() + " options:");

                for (int i = 0; i < options.size(); i++) {
                    WebElement option = options.get(i);
                    System.out.println("Option " + i + ":");
                    System.out.println("  Text: '" + option.getText() + "'");
                    System.out.println("  Value: '" + option.getAttribute("value") + "'");
                    System.out.println("  innerHTML: '" + option.getAttribute("innerHTML") + "'");
                    System.out.println("  HTML: " + option.getAttribute("outerHTML"));
                    System.out.println();
                }

                // Try selecting by value instead of text
                System.out.println("Trying to select by value 'al'...");
                dropdown.selectByValue("al");
                System.out.println("Selected option by value!");

            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }

        } finally {
            driver.quit();
        }
    }

    public void testSelectByValue(String term, String courseCode) throws Exception {
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

            System.out.println("=== TESTING SELECT BY VALUE ===");

            try {
                WebElement selectElement = driver
                        .findElement(By.xpath("//select[.//option[contains(@value, 'al') or contains(@value, 'ss')]]"));
                Select dropdown = new Select(selectElement);

                // Select "ss" for specific sections (to see Lec 001, Lec 002 individually)
                dropdown.selectByValue("ss");
                System.out.println("Selected 'ss' (specific sections)");
                Thread.sleep(2000);

                // Now look for Generate Schedules button
                WebElement generateBtn = driver
                        .findElement(By.xpath("//button[contains(text(), 'GENERATE SCHEDULES')]"));
                generateBtn.click();
                System.out.println("Clicked Generate Schedules");
                Thread.sleep(3000);

                // Check table for multiple sections
                List<WebElement> rows = driver.findElements(By.cssSelector(".inner_legend_table tr"));
                System.out.println("Found " + rows.size() + " table rows");

                // Check each row
                for (int i = 0; i < rows.size(); i++) {
                    List<WebElement> crnSpans = rows.get(i).findElements(By.cssSelector("span[data-crns]"));
                    if (!crnSpans.isEmpty()) {
                        String crn = crnSpans.get(0).getAttribute("data-crns");
                        try {
                            WebElement strong = rows.get(i).findElement(By.cssSelector("strong"));
                            String section = strong.getAttribute("innerHTML");
                            System.out.println("Row " + i + ": CRN=" + crn + ", Section=" + section);
                        } catch (Exception e) {
                            System.out.println("Row " + i + ": CRN=" + crn + ", Section=could not extract");
                        }
                    }
                }

            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }

        } finally {
            driver.quit();
        }
    }

    public void testJavaScriptSelect(String term, String courseCode) throws Exception {
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

            System.out.println("=== TESTING JAVASCRIPT SELECT ===");

            try {
                // Find the select element
                WebElement selectElement = driver
                        .findElement(By.xpath("//select[.//option[contains(@value, 'al') or contains(@value, 'ss')]]"));

                // Use JavaScript to change the value directly
                JavascriptExecutor js = (JavascriptExecutor) driver;
                js.executeScript("arguments[0].value = 'ss';", selectElement);

                // Trigger change event
                js.executeScript("arguments[0].dispatchEvent(new Event('change'));", selectElement);

                System.out.println("Set dropdown value to 'ss' using JavaScript");
                Thread.sleep(2000);

                // Try to find Generate Schedules button
                try {
                    WebElement generateBtn = driver
                            .findElement(By.xpath("//button[contains(text(), 'GENERATE SCHEDULES')]"));
                    generateBtn.click();
                    System.out.println("Clicked Generate Schedules");
                    Thread.sleep(3000);

                    // Check table
                    List<WebElement> rows = driver.findElements(By.cssSelector(".inner_legend_table tr"));
                    System.out.println("Found " + rows.size() + " table rows");

                    // Check each row for sections
                    for (int i = 0; i < rows.size(); i++) {
                        List<WebElement> crnSpans = rows.get(i).findElements(By.cssSelector("span[data-crns]"));
                        if (!crnSpans.isEmpty()) {
                            String crn = crnSpans.get(0).getAttribute("data-crns");
                            try {
                                WebElement strong = rows.get(i).findElement(By.cssSelector("strong"));
                                String section = strong.getAttribute("innerHTML");
                                System.out.println("Row " + i + ": CRN=" + crn + ", Section=" + section);
                            } catch (Exception e) {
                                System.out.println("Row " + i + ": CRN=" + crn + ", Section=could not extract");
                            }
                        }
                    }

                } catch (Exception e) {
                    System.out.println("Could not find Generate Schedules: " + e.getMessage());
                }

            } catch (Exception e) {
                System.out.println("JavaScript approach failed: " + e.getMessage());
            }

        } finally {
            driver.quit();
        }
    }

    public void testWithLongerWait(String term, String courseCode) throws Exception {
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

            System.out.println("=== TESTING WITH LONGER WAIT ===");

            // Set dropdown to 'ss' using JavaScript (this works!)
            WebElement selectElement = driver
                    .findElement(By.xpath("//select[.//option[contains(@value, 'al') or contains(@value, 'ss')]]"));
            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript("arguments[0].value = 'ss';", selectElement);
            js.executeScript("arguments[0].dispatchEvent(new Event('change'));", selectElement);
            System.out.println("Set dropdown to 'ss'");

            // Wait much longer for page to update
            System.out.println("Waiting 10 seconds for page to update...");
            Thread.sleep(6000);

            // Now try to find Generate Schedules button
            try {
                WebElement generateBtn = driver
                        .findElement(By.xpath("//button[contains(text(), 'GENERATE SCHEDULES')]"));
                generateBtn.click();
                System.out.println("Found and clicked Generate Schedules!");
                Thread.sleep(5000); // Wait for results

                // Check table
                List<WebElement> rows = driver.findElements(By.cssSelector(".inner_legend_table tr"));
                System.out.println("Found " + rows.size() + " table rows");

                // Check each row for sections
                for (int i = 0; i < rows.size(); i++) {
                    List<WebElement> crnSpans = rows.get(i).findElements(By.cssSelector("span[data-crns]"));
                    if (!crnSpans.isEmpty()) {
                        String crn = crnSpans.get(0).getAttribute("data-crns");
                        try {
                            WebElement strong = rows.get(i).findElement(By.cssSelector("strong"));
                            String section = strong.getAttribute("innerHTML");
                            System.out.println("Row " + i + ": CRN=" + crn + ", Section=" + section);
                        } catch (Exception e) {
                            System.out.println("Row " + i + ": CRN=" + crn);
                        }
                    }
                }

            } catch (Exception e) {
                System.out.println("Still can't find Generate Schedules after 10 seconds: " + e.getMessage());
            }

        } finally {
            driver.quit();
        }
    }

    public void debugWhatHappens(String term, String courseCode) throws Exception {
        WebDriverManager.chromedriver().setup();
        WebDriver driver = new ChromeDriver(options);

        try {
            System.out.println("1. Loading VSB...");
            driver.get("https://vsb.mcgill.ca/criteria.jsp");

            System.out.println("2. Clicking term: " + term);
            driver.findElement(By.linkText(term)).click();
            Thread.sleep(2000);

            System.out.println("3. Searching for: " + courseCode);
            WebElement searchBox = driver.findElement(By.id("code_number"));
            searchBox.clear();
            searchBox.sendKeys(courseCode);
            searchBox.sendKeys(Keys.ENTER);
            Thread.sleep(3000);

            System.out.println("4. Reached course page, now looking for dropdown...");

            // Check if we can find the dropdown
            try {
                WebElement selectElement = driver
                        .findElement(By.xpath("//select[.//option[contains(@value, 'al') or contains(@value, 'ss')]]"));
                System.out.println("5. FOUND dropdown element!");

                // Try the JavaScript selection
                JavascriptExecutor js = (JavascriptExecutor) driver;
                js.executeScript("arguments[0].value = 'ss';", selectElement);
                js.executeScript("arguments[0].dispatchEvent(new Event('change'));", selectElement);
                System.out.println("6. Set dropdown to 'ss' using JavaScript");

                Thread.sleep(5000);
                System.out.println("7. Waited 5 seconds after dropdown change");

            } catch (Exception e) {
                System.out.println("5. FAILED to find dropdown: " + e.getMessage());
            }

            // Let the browser stay open so you can see what happened
            System.out.println("Keeping browser open for 30 seconds to inspect...");
            Thread.sleep(30000);

        } finally {
            driver.quit();
        }
    }

    public void debugPageState(String term, String courseCode) throws Exception {
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

            System.out.println("=== PAGE STATE DEBUG ===");

            // 1. Check what's actually on the page
            System.out.println("Page title: " + driver.getTitle());
            System.out.println("Current URL: " + driver.getCurrentUrl());

            // 2. Look for ANY select elements
            List<WebElement> allSelects = driver.findElements(By.tagName("select"));
            System.out.println("Found " + allSelects.size() + " select elements");

            // 3. Look for elements containing "Try all classes"
            List<WebElement> tryAllElements = driver.findElements(By.xpath("//*[contains(text(), 'Try all classes')]"));
            System.out.println("Found " + tryAllElements.size() + " elements with 'Try all classes'");

            // 4. Look for GENERATE SCHEDULES button
            List<WebElement> generateButtons = driver.findElements(By.xpath("//*[contains(text(), 'GENERATE')]"));
            System.out.println("Found " + generateButtons.size() + " elements with 'GENERATE'");

            // 5. Take a screenshot of page source
            System.out.println("Page source length: " + driver.getPageSource().length());

            // 6. Wait and see if anything changes
            System.out.println("Waiting 10 seconds to see if page updates...");
            Thread.sleep(10000);

            // 7. Check again after waiting
            List<WebElement> allSelectsAfter = driver.findElements(By.tagName("select"));
            System.out.println("After waiting - Found " + allSelectsAfter.size() + " select elements");

            List<WebElement> tryAllAfter = driver.findElements(By.xpath("//*[contains(text(), 'Try all classes')]"));
            System.out.println("After waiting - Found " + tryAllAfter.size() + " elements with 'Try all classes'");

        } finally {
            driver.quit();
        }
    }

    public void fixedDropdownAndButton(String term, String courseCode) throws Exception {
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

            System.out.println("=== FIXED APPROACH ===");

            // Find the specific dropdown for this course
            WebElement dropdown = driver.findElement(By.cssSelector("select[name*='dropdown_0_0']"));
            Select select = new Select(dropdown);

            System.out
                    .println("Found dropdown, current value: " + select.getFirstSelectedOption().getAttribute("value"));

            // Select "ss" for specific sections
            select.selectByValue("ss");
            System.out.println("Selected 'ss' for specific sections");
            Thread.sleep(2000);

            // Look for the GENERATE SCHEDULES button (exact text)
            WebElement generateBtn = driver.findElement(By.xpath("//button[text()='GENERATE SCHEDULES']"));
            generateBtn.click();
            System.out.println("Clicked GENERATE SCHEDULES button");
            Thread.sleep(5000);

            // Check for results
            List<WebElement> rows = driver.findElements(By.cssSelector(".inner_legend_table tr"));
            System.out.println("Found " + rows.size() + " table rows after generating schedules");

            for (int i = 0; i < rows.size(); i++) {
                List<WebElement> crnSpans = rows.get(i).findElements(By.cssSelector("span[data-crns]"));
                if (!crnSpans.isEmpty()) {
                    String crn = crnSpans.get(0).getAttribute("data-crns");
                    try {
                        WebElement strong = rows.get(i).findElement(By.cssSelector("strong"));
                        String section = strong.getAttribute("innerHTML");
                        System.out.println("Row " + i + ": CRN=" + crn + ", Section=" + section);
                    } catch (Exception e) {
                        System.out.println("Row " + i + ": CRN=" + crn);
                    }
                }
            }

        } finally {
            driver.quit();
        }
    }

    public void debugAllSelects(String term, String courseCode) throws Exception {
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

            System.out.println("=== EXAMINING ALL SELECT ELEMENTS ===");

            // Get ALL select elements
            List<WebElement> allSelects = driver.findElements(By.tagName("select"));
            System.out.println("Found " + allSelects.size() + " select elements:");

            for (int i = 0; i < allSelects.size(); i++) {
                WebElement select = allSelects.get(i);

                System.out.println("\n--- Select " + i + " ---");
                System.out.println("Name: '" + select.getAttribute("name") + "'");
                System.out.println("ID: '" + select.getAttribute("id") + "'");
                System.out.println("Class: '" + select.getAttribute("class") + "'");

                // Check if this select has the "Try all classes" options
                List<WebElement> options = select.findElements(By.tagName("option"));
                System.out.println("Options count: " + options.size());

                boolean hasTryAllClasses = false;
                for (WebElement option : options) {
                    String optionText = option.getText();
                    String optionValue = option.getAttribute("value");
                    System.out.println("  Option: value='" + optionValue + "' text='" + optionText + "'");

                    if (optionText.contains("Try all classes") || optionValue.equals("al")
                            || optionValue.equals("ss")) {
                        hasTryAllClasses = true;
                    }
                }

                if (hasTryAllClasses) {
                    System.out.println("*** THIS SELECT HAS THE DROPDOWN WE WANT! ***");

                    // Try to interact with this one
                    try {
                        Select dropdown = new Select(select);
                        System.out.println(
                                "Current selection: " + dropdown.getFirstSelectedOption().getAttribute("value"));

                        // Try to select "ss"
                        dropdown.selectByValue("ss");
                        System.out.println("Successfully selected 'ss'!");
                        Thread.sleep(2000);

                        // Now look for Generate Schedules button
                        try {
                            List<WebElement> buttons = driver.findElements(By.tagName("button"));
                            System.out.println("Found " + buttons.size() + " buttons:");

                            for (int j = 0; j < buttons.size(); j++) {
                                WebElement btn = buttons.get(j);
                                String btnText = btn.getText();
                                System.out.println("  Button " + j + ": '" + btnText + "'");

                                if (btnText.contains("GENERATE") || btnText.contains("SCHEDULE")) {
                                    System.out.println("*** FOUND GENERATE BUTTON! ***");
                                    btn.click();
                                    System.out.println("Clicked the button!");
                                    Thread.sleep(5000);

                                    // Check results
                                    List<WebElement> rows = driver
                                            .findElements(By.cssSelector(".inner_legend_table tr"));
                                    System.out.println("Found " + rows.size() + " table rows after clicking");

                                    return; // Exit if successful
                                }
                            }

                        } catch (Exception e) {
                            System.out.println("Error with buttons: " + e.getMessage());
                        }

                    } catch (Exception e) {
                        System.out.println("Error interacting with this select: " + e.getMessage());
                    }
                }
            }

        } finally {
            driver.quit();
        }
    }

    public void testIndividualSections(String term, String courseCode) throws Exception {
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

            // Get the correct dropdown (Select 3)
            List<WebElement> allSelects = driver.findElements(By.tagName("select"));
            WebElement correctSelect = allSelects.get(3);
            Select dropdown = new Select(correctSelect);

            System.out.println("=== TESTING INDIVIDUAL SECTION SELECTION ===");

            // Test each section option
            List<WebElement> options = dropdown.getOptions();
            System.out.println("Available options:");
            for (int i = 0; i < options.size(); i++) {
                String value = options.get(i).getAttribute("value");
                String text = options.get(i).getText();
                System.out.println("  Option " + i + ": value='" + value + "' text='" + text + "'");
            }

            // Test Lec 001 specifically
            System.out.println("\n--- TESTING LEC 001 ---");
            for (WebElement option : options) {
                if (option.getText().equals("Lec 001")) {
                    String lecValue = option.getAttribute("value");
                    System.out.println("Found Lec 001 option with value: " + lecValue);

                    dropdown.selectByValue(lecValue);
                    System.out.println("Selected Lec 001");
                    Thread.sleep(3000);

                    // Check what's in the table now
                    checkCurrentTable(driver, "After selecting Lec 001");
                    break;
                }
            }

            // Test Lec 002 specifically
            System.out.println("\n--- TESTING LEC 002 ---");
            for (WebElement option : options) {
                if (option.getText().equals("Lec 002")) {
                    String lecValue = option.getAttribute("value");
                    System.out.println("Found Lec 002 option with value: " + lecValue);

                    dropdown.selectByValue(lecValue);
                    System.out.println("Selected Lec 002");
                    Thread.sleep(3000);

                    // Check what's in the table now
                    checkCurrentTable(driver, "After selecting Lec 002");
                    break;
                }
            }

            // Test "Try specific classes (2/2)"
            System.out.println("\n--- TESTING TRY SPECIFIC CLASSES ---");
            dropdown.selectByValue("ss");
            System.out.println("Selected 'ss' (Try specific classes)");
            Thread.sleep(3000);

            checkCurrentTable(driver, "After selecting Try specific classes");

        } finally {
            driver.quit();
        }
    }

    private void checkCurrentTable(WebDriver driver, String context) {
        System.out.println("\n=== " + context.toUpperCase() + " ===");

        try {
            List<WebElement> rows = driver.findElements(By.cssSelector(".inner_legend_table tr"));
            System.out.println("Found " + rows.size() + " table rows");

            if (rows.size() > 0) {
                for (int i = 0; i < Math.min(10, rows.size()); i++) {
                    WebElement row = rows.get(i);

                    // Look for CRN data
                    List<WebElement> crnSpans = row.findElements(By.cssSelector("span[data-crns]"));
                    if (!crnSpans.isEmpty()) {
                        String crn = crnSpans.get(0).getAttribute("data-crns");

                        try {
                            WebElement strong = row.findElement(By.cssSelector("strong"));
                            String section = strong.getAttribute("innerHTML");

                            // Look for seat information
                            String seatInfo = "N/A";
                            List<WebElement> seatSpans = row.findElements(By.cssSelector("span"));
                            for (WebElement span : seatSpans) {
                                String spanText = span.getText();
                                if (spanText.matches("\\d+") || spanText.equals("Full")) {
                                    seatInfo = spanText;
                                    break;
                                }
                            }

                            System.out.println(
                                    "  Row " + i + ": CRN=" + crn + ", Section=" + section + ", Seats=" + seatInfo);

                        } catch (Exception e) {
                            System.out.println("  Row " + i + ": CRN=" + crn + " (could not extract section/seats)");
                        }
                    } else {
                        // Check if row has any useful text
                        String rowText = row.getText().trim();
                        if (!rowText.isEmpty()) {
                            System.out.println("  Row " + i + ": " + rowText);
                        }
                    }
                }
            } else {
                System.out.println("No table rows found");
            }

            // Also check for any elements that might contain course info outside the table
            List<WebElement> courseElements = driver.findElements(By.xpath(
                    "//*[contains(text(), 'CRN') or contains(text(), 'seats') or contains(text(), 'available')]"));
            if (courseElements.size() > 0) {
                System.out.println("Found " + courseElements.size() + " elements with course info outside table:");
                for (int i = 0; i < Math.min(5, courseElements.size()); i++) {
                    System.out.println("  " + courseElements.get(i).getText());
                }
            }

        } catch (Exception e) {
            System.out.println("Error checking table: " + e.getMessage());
        }
    }
}
