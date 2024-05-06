package com.example.clientSide;

import com.example.clientSide.service.AuthenticationService;
import com.example.clientSide.terminal.login.LoginUser;
import com.example.clientSide.utils.AuthenticationRequest;

public class Main {
    public static void main(String[] args) {
        LoginUser loginUser = new LoginUser();
        loginUser.login();

    }
}
