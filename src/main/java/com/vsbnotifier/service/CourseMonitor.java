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
    private static final int checkInterval = 45; // 45 seconds
    private String term;
    private String courseCode;
    private String section;

    public CourseMonitor(String term, String courseCode, String section) {
        this.term = term;
        this.courseCode = courseCode;
        this.section = section; //section can be either A, B, or both
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
                    if(!shouldMonitorThisSection(section)){
                        //if we should not monitor this section, continue to the next section
                        continue;
                    }
                    String crn = section.getCrn();
                    String currentSeats = section.getAvailableSeats();

                    //now check if we have previous data for this crn
                    if(previousSeatCounts.containsKey(crn)){
                        //compare the current seats with previous seats
                        String previousSeats = previousSeatCounts.get(crn);
                        //TODO: comparison logic
                        if(currentSeats.equals(previousSeats)){
                            //no change, continue to next section
                            continue;
                        }else{
                            //check if the current seats are available
                            if(previousSeatCounts.get(crn).equals("Waitlist") && !currentSeats.equals("Waitlist")){
                                //seats became available, alert the user
                                //TODO: implement alert logic
                                
                                //stop
                                executorService.shutdown();
                                return;
                            }
                            previousSeatCounts.put(crn, currentSeats);
                        }
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
    //helper to filter sections based on the desired section
    public boolean shouldMonitorThisSection(SectionInfo sectionInfo) {
        //case 1: monitor all sections
        if (this.section.equals("any")){
            return true; //monitor all sections
        //case 2: monitor specific sections (Lec 001)
        }
        if(this.section.equals(sectionInfo.getSectionCode())) {
            return true; //monitor this section
        }
        return false; //do not monitor this section
    }
    public static void main(String[] args){
        try{
            //create courseMonitor instance
            CourseMonitor cm = new CourseMonitor("Fall 2025", "MATH 240", "Lec 001");
            //start monitoring
            cm.startPeriodicCheck();
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}
