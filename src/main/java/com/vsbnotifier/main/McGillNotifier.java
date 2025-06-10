package com.vsbnotifier.main;

import com.vsbnotifier.service.McGillCourseChecker;
import com.vsbnotifier.service.TwilioNotifier;
import java.util.ArrayList;
import java.util.List;

import com.vsbnotifier.model.CourseInfo;
import com.vsbnotifier.model.UserRequest;
import com.vsbnotifier.service.McGillCourseChecker;

public class McGillNotifier {
    //orchestrate the course checking and notification process
    /*private McGillCourseChecker courseChecker;
    private List<UserRequest> requests = new ArrayList<>();
    private TwilioNotifier twilioNotifier;
    private String courseCode;
    private String phoneNumber;
    //addrequest method
    public void addRequest(UserRequest request) {
        //add the request to the list of requests
        requests.add(request);
    } */
    //TODO: implement a ScheduledExecutorService (look up how to do this)
    public static void main(String[] args){
        try{
            McGillCourseChecker checker = new McGillCourseChecker();
            //example course
            CourseInfo course = checker.checkCourseAvailability("Fall 2025", "COMP 370");
            System.out.println("Course: " + course.getCourseCode() + " - " + course.getCourseName());
            System.out.println("Sections with available seats:");
            System.out.println(course.getCrnAndLec());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }    
}
