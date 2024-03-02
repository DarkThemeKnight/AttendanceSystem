package com.backend.FaceRecognition.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;

@Entity
@NoArgsConstructor
@Data
@AllArgsConstructor
@Builder
public class Subject {
    @Id
    @Column(name = "subject_code")
    private String subjectCode;
    @Column(name = "subject_title")
    private String subjectTitle;
    @ManyToMany(mappedBy = "subjects")
    private Set<Student> students = new HashSet<>();
    @ManyToOne
    @JoinColumn(name = "lecturer_id") // Define the name of the foreign key column
    private ApplicationUser lecturerInCharge;

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof Subject subject)) return false;
        return Objects.equals(getSubjectCode(), subject.getSubjectCode());
    }
    public void addStudent(Student student){
        students.add(student);
    }
    public void addStudent(Collection<Student> student){
        students.addAll(student);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getSubjectCode());
    }

    public void clearStudents() {
        students.clear();
    }
}
