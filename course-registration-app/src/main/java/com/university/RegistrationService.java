package com.university;

public class RegistrationService {

    private static final int MAX_CREDITS = 20;
    private static final String EQUIVALENT_PREREQUISITE_TARGET = "CS301";
    private static final String EQUIVALENT_PREREQUISITE_ALTERNATE = "CS3A1";

    public boolean register(Student student, Course course) {

        if (!course.isActive()) {
            return false;
        }

        if (course.getCurrentEnrollment() >= course.getCapacity()) {
            return false;
        }

        if (student.isRegistered(course.getCourseCode())) {
            return false;
        }

        String prerequisite = course.getPrerequisite();

        if (prerequisite != null
                && !prerequisite.isBlank()
                && !isPrerequisiteSatisfied(student, prerequisite)) {
            return false;
        }

        if (student.getTotalCredits() + course.getCredits() > MAX_CREDITS) {
            return false;
        }

        student.addRegistration(
                course.getCourseCode(),
                course.getCredits());

        course.incrementEnrollment();

        return true;
    }

    private boolean isPrerequisiteSatisfied(Student student, String prerequisite) {
        if (student.hasCompletedPrerequisite(prerequisite)) {
            return true;
        }
        return EQUIVALENT_PREREQUISITE_TARGET.equals(prerequisite)
                && student.hasCompletedPrerequisite(EQUIVALENT_PREREQUISITE_ALTERNATE);
    }
}