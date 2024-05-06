//package com.example.clientSide;
//
//
//import com.example.clientSide.service.AuthenticationService;
//import com.example.clientSide.service.ProfilePictureService;
//import com.example.clientSide.state.ApplicationContext;
//import com.example.clientSide.utils.AuthenticationRequest;
//import com.google.common.io.Files;
//import javafx.application.Platform;
//import javafx.fxml.FXMLLoader;
//import javafx.scene.Parent;
//import javafx.scene.Scene;
//import javafx.stage.Stage;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.boot.WebApplicationType;
//import org.springframework.boot.autoconfigure.SpringBootApplication;
//import org.springframework.boot.builder.SpringApplicationBuilder;
//import org.springframework.core.io.ByteArrayResource;
//import org.springframework.web.multipart.MultipartFile;
//import org.springframework.web.multipart.support.StandardMultipartHttpServletRequest;
//
//import javax.imageio.ImageIO;
//import java.awt.image.BufferedImage;
//import java.io.ByteArrayOutputStream;
//import java.io.File;
//import java.net.URL;
//
//@SpringBootApplication
//@Slf4j
//public class JavaFxApplicationSupport extends javafx.application.Application {
//  @Override
//  public void init() throws Exception {
//    SpringApplicationBuilder builder = new
//            SpringApplicationBuilder(JavaFxApplicationSupport.class);
//    builder.application().setWebApplicationType(WebApplicationType.NONE);
//  }
//  @Override
//  public void start(Stage primaryStage) throws Exception {
//
//
//  }
//
//
//  public static void main(String[] args) {
//    launch(args);
//  }
//
//}