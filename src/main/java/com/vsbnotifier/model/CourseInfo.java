package com.vsbnotifier.model;

import java.util.ArrayList;
import java.util.List;

public class CourseInfo {
    private String courseCode; // e.g., "COMP 202"
    private String courseName; // e.g. "foundations of programming"
    private List<SectionInfo> sections; // List of sections for this course

    //constructor
    public CourseInfo(String courseCode, String courseName, List<SectionInfo> sections) {
        this.courseCode = courseCode;
        this.courseName = courseName;
        this.sections = sections;
    }
    public CourseInfo() {
        this.sections = new ArrayList<>(); //store all course info in a list
    }
    public String getCourseCode() {
        return courseCode;
    }
    public String getCourseName(){
        return courseName;
    }
    public List<SectionInfo> getSections(){
        return sections;
    }
    //setters
    public void setSections(List<SectionInfo> sections) {
        this.sections = sections;
    }
    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }
    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }
}
