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
import com.vsbnotifier.model.CourseInfo; //import the CourseInfo model
import com.vsbnotifier.model.SectionInfo; //import the SectionInfo model
import net.lightbody.bmp.BrowserMobProxy;
import net.lightbody.bmp.BrowserMobProxyServer;
import net.lightbody.bmp.client.ClientUtil;
import org.openqa.selenium.Proxy;

import java.time.Duration;

public class McGillCourseChecker {
    ChromeOptions options = new ChromeOptions(); //to set up the ChromeDriver options
    public CourseInfo checkCouseAvailability(String term, String courseCode) throws Exception {
        CourseInfo courseInfo = new CourseInfo(); //create a new CourseInfo object
        BrowserMobProxy proxy = new BrowserMobProxyServer();
        proxy.start(0); // dynamic port
        //get Selenium proxy object
        Proxy seleniumProxy = ClientUtil.createSeleniumProxy(proxy);
        // configure Chrome options to use the proxy
        options.setProxy(seleniumProxy);
        //set up the ChromeDriver options
        WebDriver driver = new ChromeDriver(options);
        //record traffic
        proxy.newHar("vsb");
        driver.get("https://vsb.mcgill.ca/criteria.jsp");

        //TODO: selenium logic to navigate page, get course infomation
        
        //click on the correct term
        driver.findElement(By.linkText(term)).click();
        //click on the box to select the course
        driver.findElement(By.id("code_number")).sendKeys(courseCode); //click and enter the course code
        driver.findElement(By.id("code_number")).sendKeys(Keys.ENTER); //click enter
        //create a new waitdriver object
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(3)); //wait for 3 seconds
        //get all requests and responses in the network
        proxy.getHar().getLog().getEntries().forEach(entry -> {
            String url = entry.getRequest().getUrl();
            int status = entry.getResponse().getStatus();
            System.out.println("URL: " + url + " | Status: " + status);
        });
        //TODO: handle the edge case where the class-data? isnt found -> solution: refresh and try again
        
        return courseInfo;
    }
}
