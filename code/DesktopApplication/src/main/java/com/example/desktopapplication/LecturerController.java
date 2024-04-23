package com.example.desktopapplication;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;

public class LecturerClassManagement{
    private Button currentActiveButton; // Reference to the currently active button

    public LecturerClassManagement() {
    }
    @FXML
    private AnchorPane display;
    @FXML
    private Button dashboardButton, classManagementButton,
            attendanceStatisticsButton, userSettingsButton;

    @FXML
    public void initialize() throws IOException {
        setCurrentActiveButton(dashboardButton);
        buildDashBoard();
        Platform.runLater(()-> DashBoardController.scheduler.shutdown());
    }
    private void setCurrentActiveButton(Button button) {
        // Remove the active style from the previous active button
        if (currentActiveButton != null) {
            currentActiveButton.getStyleClass().remove("active-button");
        }
        // Add the active style to the new active button
        button.getStyleClass().add("active-button");
        currentActiveButton = button;
    }
    public void buildDashBoard() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("fxml/lecturer_dashboard.fxml"));
        Node newNode = loader.load();
        display.getChildren().setAll(newNode);
    }

    public void buildClassManagement() throws IOException {
        DashBoardController.scheduler.shutdownNow();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("fxml/class_management.fxml"));
        Node newNode = loader.load();
        display.getChildren().setAll(newNode);
    }
    public void buildStatisticsManagement() {
        // Implement method logic here
    }

    public void buildSettingsManagement() {
        // Implement method logic here
    }
    @FXML
    public  void handleButtonClick(ActionEvent event) throws IOException {
        // Get the button that triggered the event
        Button clickedButton = (Button) event.getSource();
        // Update the active button
        setCurrentActiveButton(clickedButton);

        if (currentActiveButton.equals(dashboardButton)){
            buildDashBoard();
        }
        if (currentActiveButton.equals(classManagementButton)){
            buildClassManagement();
        }
        if (currentActiveButton.equals(attendanceStatisticsButton)){
            buildStatisticsManagement();
        }
        if (currentActiveButton.equals(userSettingsButton)){
            buildSettingsManagement();
        }
    }


}
