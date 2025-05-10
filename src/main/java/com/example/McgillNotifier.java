package com.example;

public class McGillNotifier {
    //orchestrate the course checking and notification process
    private McGillCourseChecker courseChecker;
    private TwilioNotifier twilioNotifier;
    private String courseCode;
    private String phoneNumber;
}
