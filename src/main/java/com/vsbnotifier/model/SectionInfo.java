package com.vsbnotifier.model;

public class SectionInfo {
    //only care about 3 things
    private String crn;
    private String sectionCode;
    private String availableSeats;


    //simple constructor
    public SectionInfo(){
    }
    //constructor with parameters
    public SectionInfo(String crn, String sectionCode, String availableSeats){
        this.crn = crn;
        this.sectionCode = sectionCode;
        this.availableSeats = availableSeats;
    }
    //getters and setters
    public String getCrn(){
        return crn;
    }
    public void setCrn(String crn){
        this.crn = crn;
    }
    public String getSectionCode(){
        return sectionCode;
    }
    public void setSectionCode(String sectionCode){
        this.sectionCode = sectionCode;
    }
    public String getAvailableSeats(){
        return availableSeats;
    }
    public void setAvailableSeats(String availableSeats){
        this.availableSeats = availableSeats;
    }

    //create notification message
    public String createNotificationMessage(String courseCode){
        return String.format("🧙‍♂️ A rare seat has appeared in %s - Section %s (CRN: %s)! %d magical spot(s) await your presence!", courseCode, sectionCode, crn, availableSeats);
    }
    @Override //to have human readable output
    public String toString(){
        return "Section " + sectionCode + " (CRN: " + crn + "): " + availableSeats + " seat(s) available";
    }
}
