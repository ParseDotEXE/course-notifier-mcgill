package com.vsbnotifier.model;

public class UserRequest {
    //declare all fields
    private String courseCode;
    private String term;
    private String phoneNumber;
    private boolean notified = false;

    //constructor
    public UserRequest(String courseCode, String term, String phoneNumber) {
        this.courseCode = courseCode;
        this.term = term;
        this.phoneNumber = phoneNumber;
    }
    //getters and setters
    public String getCourseCode() {
        return courseCode;
    }
    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }
    public String getTerm() {
        return term;
    }
    public void setTerm(String term) {
        this.term = term;
    }
    public String getPhoneNumber() {
        return phoneNumber;
    }
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    public boolean isNotified() {
        return notified;
    }
    public void setNotified(boolean notified) {
        this.notified = notified;
    }
    //to have hman readable output
    @Override
    public String toString(){
        return "UserRequest{" +
                "courseCode='" + courseCode + '\'' +
                ", term='" + term + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", notified=" + notified +
                '}';
    }
}
