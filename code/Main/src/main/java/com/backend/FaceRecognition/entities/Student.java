package com.backend.FaceRecognition.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;
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
    private String faculty;
    private String department;
    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(
            name = "student_subject",
            joinColumns = @JoinColumn(name = "matriculation_number"),
            inverseJoinColumns = @JoinColumn(name = "subject_code")
    )
    private Set<Subject> subjects = new HashSet<>();

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
    public void clear(){
        subjects.clear();
    }
}