package com.vsbnotifier.main;

import com.vsbnotifier.service.McGillCourseChecker;
import com.vsbnotifier.service.TwilioNotifier;
import java.util.ArrayList;
import java.util.List;
import com.vsbnotifier.model.UserRequest;

public class McGillNotifier {
    //orchestrate the course checking and notification process
    private McGillCourseChecker courseChecker;
    private List<UserRequest> requests = new ArrayList<>();
    private TwilioNotifier twilioNotifier;
    private String courseCode;
    private String phoneNumber;
    //addrequest method
    public void addRequest(UserRequest request) {
        //add the request to the list of requests
        requests.add(request);
    }    
}

