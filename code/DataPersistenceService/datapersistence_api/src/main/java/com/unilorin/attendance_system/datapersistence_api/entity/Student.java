package com.unilorin.attendance_system.datapersistence_api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Student {
    @Id
    @Column(name = "matriculation_number")
    private String matriculationNumber;
    @Column(name = "school_email")
    private String schoolEmail;
    private String firstname;
    private String lastname;
    @Column(name = "middle_name")
    private String middleName;
    @OneToMany(mappedBy = "student")
    private Set<StudentSubject> studentSubjects;

}
