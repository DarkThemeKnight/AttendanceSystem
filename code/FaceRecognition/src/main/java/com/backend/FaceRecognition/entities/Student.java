package com.backend.FaceRecognition.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

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

    public void add(Subject subject){
        subjects.add(subject);
    }
    public void add(Collection<Subject> subjects){
        this.subjects.addAll(subjects);
    }
    public void remove(Subject subject){
        this.subjects.remove(subject);
    }
    public void remove(Collection<Subject> subjects){
        this.subjects.removeAll(subjects);
    }
}