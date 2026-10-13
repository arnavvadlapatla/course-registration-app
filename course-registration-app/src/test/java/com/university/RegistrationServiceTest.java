package com.university;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RegistrationServiceTest {

    @Test
    void shouldRegisterStudentForValidCourse() {

        Student student = new Student("S101", "Anita");

        Course course = new Course(
                "CS301",
                "Data Structures",
                4,
                30,
                true,
                null
        );

        RegistrationService service = new RegistrationService();

        boolean result = service.register(student, course);

        assertTrue(result);
        assertTrue(student.isRegistered("CS301"));
        assertEquals(4, student.getTotalCredits());
        assertEquals(1, course.getCurrentEnrollment());
    }

    @Test
    void shouldRejectInactiveCourse() {

        Student student = new Student("S101", "Anita");

        Course course = new Course(
                "CS301",
                "Data Structures",
                4,
                30,
                false,
                null
        );

        RegistrationService service = new RegistrationService();

        boolean result = service.register(student, course);

        assertFalse(result);
        assertFalse(student.isRegistered("CS301"));
    }

    @Test
    void shouldRejectMissingPrerequisite() {

        Student student = new Student("S101", "Anita");

        Course course = new Course(
                "CS401",
                "Advanced Algorithms",
                4,
                30,
                true,
                "CS301"
        );

        RegistrationService service = new RegistrationService();

        boolean result = service.register(student, course);

        assertFalse(result);
        assertFalse(student.isRegistered("CS401"));
    }

    @Test
    void shouldAllowRegistrationWhenPrerequisiteIsCompleted() {

        Student student = new Student("S101", "Anita");
        student.addCompletedPrerequisite("CS301");

        Course course = new Course(
                "CS401",
                "Advanced Algorithms",
                4,
                30,
                true,
                "CS301"
        );

        RegistrationService service = new RegistrationService();

        boolean result = service.register(student, course);

        assertTrue(result);
        assertTrue(student.isRegistered("CS401"));
    }

    @Test
    void shouldRejectDuplicateRegistration() {

        Student student = new Student("S101", "Anita");

        Course course = new Course(
                "CS301",
                "Data Structures",
                4,
                30,
                true,
                null
        );

        RegistrationService service = new RegistrationService();

        assertTrue(service.register(student, course));

        boolean secondAttempt = service.register(student, course);

        assertFalse(secondAttempt);
        assertEquals(4, student.getTotalCredits());
        assertEquals(1, course.getCurrentEnrollment());
    }

    @Test
    void shouldRejectRegistrationWhenCreditLimitIsExceeded() {

        Student student = new Student("S101", "Anita");

        // Simulate 20 existing credits
        student.addRegistration("CS101", 4);
        student.addRegistration("CS102", 4);
        student.addRegistration("CS201", 4);
        student.addRegistration("CS202", 4);
        student.addRegistration("CS203", 4);

        Course course = new Course(
                "CS301",
                "Data Structures",
                4,
                30,
                true,
                null
        );

        RegistrationService service = new RegistrationService();

        boolean result = service.register(student, course);

        assertFalse(result);
        assertFalse(student.isRegistered("CS301"));

    }

    

    private final RegistrationService capacityService = new RegistrationService();

    
    private Course courseWithSeatsTaken(int capacity, int taken) {
        Course course = new Course("CS301", "Data Structures", 4, capacity, true, null);
        for (int i = 0; i < taken; i++) {
            course.incrementEnrollment();
        }
        return course;
    }

    @Test
    void shouldRejectRegistrationWhenCourseIsAlreadyFull() {
        Student student = new Student("S101", "Anita");
        Course course = courseWithSeatsTaken(2, 2);   

        boolean result = capacityService.register(student, course);

        assertFalse(result);
        assertFalse(student.isRegistered("CS301"));
        assertEquals(0, student.getTotalCredits());
        assertEquals(2, course.getCurrentEnrollment());
    }

    @Test
    void shouldAcceptRegistrationForLastRemainingSeat() {
        Student student = new Student("S101", "Anita");
        Course course = courseWithSeatsTaken(2, 1);   

        boolean result = capacityService.register(student, course);

        assertTrue(result);
        assertTrue(student.isRegistered("CS301"));
        assertEquals(4, student.getTotalCredits());
        assertEquals(2, course.getCurrentEnrollment());
    }

    @Test
    void shouldRejectNextStudentAfterLastSeatIsFilled() {
        Course course = courseWithSeatsTaken(2, 1);
        Student first = new Student("S101", "Anita");
        Student second = new Student("S102", "Ravi");

        assertTrue(capacityService.register(first, course));    
        assertFalse(capacityService.register(second, course));  

        assertFalse(second.isRegistered("CS301"));
        assertEquals(0, second.getTotalCredits());
        assertEquals(2, course.getCurrentEnrollment());
    }

    @Test
    void rejectedAttemptShouldNotChangeExistingStudentState() {
        Student student = new Student("S101", "Anita");
        student.addRegistration("CS101", 4);            
        Course course = courseWithSeatsTaken(1, 1);     

        boolean result = capacityService.register(student, course);

        assertFalse(result);
        assertEquals(4, student.getTotalCredits());
        assertEquals(1, student.getRegisteredCourses().size());
        assertTrue(student.isRegistered("CS101"));
        assertFalse(student.isRegistered("CS301"));
        assertEquals(1, course.getCurrentEnrollment());
    }

    private Course advancedCourse() {
        return new Course("CS401", "Advanced Algorithms", 4, 30, true, "CS301");
    }

    @Test
    void shouldAcceptStudentWhoCompletedCS301() {
        Student student = new Student("S101", "Anita");
        student.addCompletedPrerequisite("CS301");
        Course course = advancedCourse();

        assertTrue(capacityService.register(student, course));
        assertTrue(student.isRegistered("CS401"));
        assertEquals(4, student.getTotalCredits());
        assertEquals(1, course.getCurrentEnrollment());
    }

    @Test
    void shouldAcceptStudentWhoCompletedCS3A1InsteadOfCS301() {
        Student student = new Student("S101", "Anita");
        student.addCompletedPrerequisite("CS3A1");
        Course course = advancedCourse();

        assertTrue(capacityService.register(student, course));
        assertTrue(student.isRegistered("CS401"));
        assertEquals(4, student.getTotalCredits());
        assertEquals(1, course.getCurrentEnrollment());
    }

    @Test
    void shouldRejectStudentWhoCompletedNeitherAndLeaveStateUnchanged() {
        Student student = new Student("S101", "Anita");
        Course course = advancedCourse();

        assertFalse(capacityService.register(student, course));
        assertFalse(student.isRegistered("CS401"));
        assertTrue(student.getRegisteredCourses().isEmpty());
        assertEquals(0, student.getTotalCredits());
        assertEquals(0, course.getCurrentEnrollment());
    }

    @Test
    void shouldRejectStudentWhoCompletedOnlyAnUnrelatedCourse() {
        Student student = new Student("S101", "Anita");
        student.addCompletedPrerequisite("CS201");
        Course course = advancedCourse();

        assertFalse(capacityService.register(student, course));
        assertTrue(student.getRegisteredCourses().isEmpty());
        assertEquals(0, student.getTotalCredits());
        assertEquals(0, course.getCurrentEnrollment());
    }

    @Test
    void equivalenceShouldNotMakeCourseCodesIdenticalForDuplicateChecks() {
        Student student = new Student("S101", "Anita");
        student.addRegistration("CS3A1", 4);   
        Course cs301 = new Course("CS301", "Data Structures", 4, 30, true, null);

        assertTrue(capacityService.register(student, cs301));
        assertTrue(student.isRegistered("CS3A1"));
        assertTrue(student.isRegistered("CS301"));
        assertEquals(8, student.getTotalCredits());
        assertEquals(1, cs301.getCurrentEnrollment());
    }

    @Test
    void equivalenceShouldNotApplyToOtherPrerequisites() {
        Student student = new Student("S101", "Anita");
        student.addCompletedPrerequisite("CS301");
        Course requiresCS3A1 = new Course("CS402", "Other Course", 4, 30, true, "CS3A1");

        assertFalse(capacityService.register(student, requiresCS3A1));
        assertEquals(0, student.getTotalCredits());
        assertEquals(0, requiresCS3A1.getCurrentEnrollment());
    }
}