package com.vsbnotifier.main;

import com.vsbnotifier.service.McGillCourseChecker;
import com.vsbnotifier.service.TwilioNotifier;

public class McGillNotifier {
    //orchestrate the course checking and notification process
    private McGillCourseChecker courseChecker;
    private TwilioNotifier twilioNotifier;
    private String courseCode;
    private String phoneNumber;
}
