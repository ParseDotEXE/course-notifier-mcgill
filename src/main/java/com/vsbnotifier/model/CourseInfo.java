package com.vsbnotifier.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CourseInfo {
    private String courseCode; // e.g., "COMP 202"
    private String courseName; // e.g. "foundations of programming"
    private Map<String, SectionInfo> sections; // key: CRN or section number
    //constructor
    public CourseInfo(String courseCode, String courseName, Map<String, SectionInfo> sections) {
        this.courseCode = courseCode;
        this.courseName = courseName;
        this.sections = sections;
    }
    public CourseInfo() {
        this.sections = new HashMap<>(); //store all course info in a map
    }
    public String getCourseCode() {
        return courseCode;
    }
    public String getCourseName(){
        return courseName;
    }
    public Map<String, SectionInfo> getSections(){
        return sections;
    }
    //setters
    public void setSections(Map<String, SectionInfo> sections) {
        this.sections = sections;
    }
    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }
    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }
    //method to return the course info if there are seats
    public List<String> getCrnAndLec(){
        List<String> crnAndLec = new ArrayList<>();
        for(Map.Entry<String, SectionInfo> entry : sections.entrySet()){
            SectionInfo section = entry.getValue(); //get the section info
            if(!section.getAvailableSeats().equals("0")){ //check if it has available seats
                crnAndLec.add(section.getCrn() + " - " + section.getSectionCode() + " - " + section.getAvailableSeats());
            }
        }
        if(crnAndLec.isEmpty()){
            return null; //if there are no seats, return null
        }
        return crnAndLec; //return the list of CRN and section codes with available seats
    }

}
