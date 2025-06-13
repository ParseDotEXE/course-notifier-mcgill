package com.vsbnotifier.service;

import java.util.concurrent.*;
import java.util.*;
import java.io.*;
import static java.util.concurrent.TimeUnit.SECONDS;

import com.vsbnotifier.model.CourseInfo;
import com.vsbnotifier.model.SectionInfo;
import com.vsbnotifier.service.McGillCourseChecker;

public class CourseMonitor {
    // instance variables
    private Map<String, String> previousSeatCounts;
    private ScheduledExecutorService executorService;
    private McGillCourseChecker courseChecker;
    private static final int checkInterval = 2 * 60; // 2 minutes in seconds
    private String term;
    private String courseCode;

    public CourseMonitor(String term, String courseCode) {
        this.term = term;
        this.courseCode = courseCode;
        this.previousSeatCounts = new HashMap<>();
        this.executorService = Executors.newScheduledThreadPool(1);
        this.courseChecker = new McGillCourseChecker();
    }

    // logic to start the course monitor
    public void startPeriodicCheck() {
        /*
         * 1- Use the scheduler to run checks every 2 minutes
         * 2- Each check should:
         * Call courseChecker.checkCourseAvailability(term, courseCode)
         * Compare current seats with previousSeatCounts
         * If seats became available (was "0" or "Full", now has seats), alert and stop
         * Update previousSeatCounts with new values
         */
        executorService.scheduleAtFixedRate(() -> {
            //the checking logic goes here
            // 1. Call courseChecker.checkCourseAvailability()
            CourseInfo courseInfo;
            try {
                courseInfo = courseChecker.checkCourseAvailability(term, courseCode);
                //loop through all sections
                for(SectionInfo section : courseInfo.getSections().values()){ //check for each crn/section
                    String crn = section.getCrn();
                    String currentSeats = section.getAvailableSeats();

                    //now check if we have previous data for this crn
                    if(previousSeatCounts.containsKey(crn)){
                        //compare the current seats with previous seats
                        String previousSeats = previousSeatCounts.get(crn);
                        //TODO: comparison logic
                        if
                    }else{
                        //add to the previousSeatCounts map
                        previousSeatCounts.put(crn, currentSeats);
                    }
                }
                // 4. Update previousSeatCounts
            } catch (Exception e) {
                // Handle or log the exception
                e.printStackTrace();
            }
        }, 0, checkInterval, SECONDS);
    }
}
