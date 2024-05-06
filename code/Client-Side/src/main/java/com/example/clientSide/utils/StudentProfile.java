package com.example.clientSide.utils;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Arrays;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentProfile {
    private String message;
    private StudentData data;
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StudentData {
        private byte[] profilePicture;
        private String name;
        private String matriculationNumber;
        private String email;
        private String phoneNumber;
        private Course[] courses;
        private String address;
        private String dateOfBirth;
        private String attendanceCount;
        private String totalPossible;
        private String attendanceScore;
        private String department;
        private String faculty;

        @Override
        public String toString() {
            return "StudentData{" +
                    "\nprofilePicture=" + Arrays.toString(profilePicture) +
                    ", \nname='" + name + '\'' +
                    ", \nmatriculationNumber='" + matriculationNumber + '\'' +
                    ", \nemail='" + email + '\'' +
                    ", \nphoneNumber='" + phoneNumber + '\'' +
                    ", \ncourses=" + Arrays.toString(courses) +
                    ", \naddress='" + address + '\'' +
                    ", \ndateOfBirth='" + dateOfBirth + '\'' +
                    ", \nattendanceCount='" + attendanceCount + '\'' +
                    ", \ntotalPossible='" + totalPossible + '\'' +
                    ", \nattendanceScore='" + attendanceScore + '\'' +
                    ", \ndepartment='" + department + '\'' +
                    ", \nfaculty='" + faculty + '\'' +
                    '}';
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Course {
        private String name;
        private String title;

        @Override
        public String toString() {
            return "Course{" +
                    "\nname='" + name + '\'' +
                    ",\n title='" + title + '\'' +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "StudentProfile{" +
                "\nmessage='" + message + '\'' +
                ",\n data=" + data +
                '}';
    }
}