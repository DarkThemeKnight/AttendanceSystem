package com.unilorin.attendance_system.datapersistence_api.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Student {
    @Id
    @Column(name = "matriculation_number") //this is the id
    private String matriculationNumber;
    @Column(name = "school_email")
    private String schoolEmail;
    private String firstname;
    private String lastname;
    @Column(name = "middle_name")
    private String middleName;
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "student_subject",
            joinColumns = @JoinColumn(name = "matriculation_number"),
            inverseJoinColumns = @JoinColumn(name = "subject_code")
    )
    private Set<Subject> subjects = new HashSet<>();
    @Lob // Annotation for BLOB data
    @Column(name = "face_image")
    private byte[] faceImage; // Storing facial image as BLOB

    public boolean add(Subject subject){
        return subjects.add(subject);
    }
    public void add(Collection<Subject> subjects){
        this.subjects.addAll(subjects);
    }
}
