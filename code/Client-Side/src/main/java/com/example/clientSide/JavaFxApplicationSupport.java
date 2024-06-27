package com.example.clientSide;


import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.opencv.core.Core;
import org.opencv.core.MatOfRect;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
@SpringBootApplication
@Slf4j
public class JavaFxApplicationSupport extends javafx.application.Application {
  static {
    System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
  }
  @Override
  public void init() throws Exception {
    SpringApplicationBuilder builder = new
            SpringApplicationBuilder(JavaFxApplicationSupport.class);
    builder.application().setWebApplicationType(WebApplicationType.NONE);
  }
  @Override
  public void start(Stage primaryStage) throws Exception {
    FXMLLoader loader = new FXMLLoader(getClass().getResource("/static/MarkAttendancePage.fxml"));
    Parent root = loader.load();
    Scene scene = new Scene(root);
    primaryStage.setTitle("Login Page");
    primaryStage.setScene(scene);
    primaryStage.show();
  }
  public static void main(String[] args) {
    launch(args);
  }

}