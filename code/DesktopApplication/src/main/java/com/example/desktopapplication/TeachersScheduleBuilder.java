package com.example.desktopapplication;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

import java.net.URL;
import java.util.ResourceBundle;

public class TeacherClassesBuilder implements Initializable {
    @FXML
    private Label course_code,course_title,duration,time;

    public TeacherClassesBuilder() {
    }

    public Label getCourse_code() {
        return course_code;
    }

    public Label getCourse_title() {
        return course_title;
    }

    public Label getDuration() {
        return duration;
    }

    public Label getTime() {
        return time;
    }

    public void build(String courseCode, String courseTitle, String duration, String time){
        this.course_code.setText(courseCode);
        this.course_title.setText(courseTitle);
        this.duration.setText(duration);
        this.time.setText(time);
    }
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }
}
