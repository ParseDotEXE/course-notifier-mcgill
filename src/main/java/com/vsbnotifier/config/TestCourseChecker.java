package com.vsbnotifier.config;

import com.vsbnotifier.service.McGillCourseChecker;
import com.vsbnotifier.service.McGillCourseChecker.CourseInfo;

public class TestCourseChecker {
    public static void main(String[] args) {
        try {
            McGillCourseChecker checker = new McGillCourseChecker();
            
            // Test 1: COMP-273 for Fall 2025
            System.out.println("Test 1: COMP-273 for Fall 2025");
            McGillCourseChecker.CourseInfo info1 = checker.checkCourseAvailability("202509", "COMP 273");
            System.out.println("Successfully retrieved course info!");
            
            // Test 2: MATH-240 for Fall 2025
            System.out.println("\nTest 2: MATH-240 for Fall 2025");
            McGillCourseChecker.CourseInfo info2 = checker.checkCourseAvailability("202509", "MATH 240");
            System.out.println("Successfully retrieved course info!");
            
            // Test 3: Invalid course (should fail gracefully)
            System.out.println("\nTest 3: Invalid course");
            try {
                McGillCourseChecker.CourseInfo info3 = checker.checkCourseAvailability("202509", "FAKE 999");
                System.out.println("Retrieved info for fake course?");
            } catch (Exception e) {
                System.out.println("Correctly handled invalid course: " + e.getMessage());
            }
            
        } catch (Exception e) {
            System.err.println("Error during testing: " + e.getMessage());
            e.printStackTrace();
        }
    }
}