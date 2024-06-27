package com.example.clientSide.fxmlController;

import com.example.clientSide.helper.Helper;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {
    public TextField username;
    public PasswordField password;
    public Label errorMessage;
    Helper helper;
    @FXML
    public void initialize(){
        helper= new Helper();
    }

    public void login(ActionEvent event) {
        String username = this.username.getText();
        String password = this.password.getText();
        String response = "";
        try {
            response = helper.login(username, password);
        }catch (RuntimeException e){
            errorMessage.setText("service currently unavailable");
        }
        if (!response.equalsIgnoreCase("success")){
            errorMessage.setText("Invalid Username or password");
        }
        //Push to next page..............
    }
}
