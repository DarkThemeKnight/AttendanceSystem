package com.unilorin.attendance_system.authentication_api.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "student_subject")
public class StudentSubject {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "matriculation_number", referencedColumnName = "matriculation_number")
    private Student student;
    @ManyToOne
    @JoinColumn(name = "subject_code", referencedColumnName = "subject_code")
    private Subject subject;
}
