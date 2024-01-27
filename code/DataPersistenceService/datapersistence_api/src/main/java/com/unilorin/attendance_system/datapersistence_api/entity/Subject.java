package com.unilorin.attendance_system.datapersistence_api.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Set;

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
    @OneToMany(mappedBy = "subject")
    private Set<StudentSubject> studentSubjects;
    @ManyToOne
    @JoinColumn(name = "lecturer_id") // Define the name of the foreign key column
    private ApplicationUser lecturerInCharge;
}
