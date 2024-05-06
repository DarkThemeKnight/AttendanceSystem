package com.example.clientSide.terminal.login;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.Console;
import java.util.Scanner;

@NoArgsConstructor
@Slf4j
public class LoginUser {
    private String username;
    private String password;
    Console console = System.console();
    public void login(){
        try(Scanner scanner = new Scanner(System.in)){
            System.out.print("Username: ");
            username = scanner.nextLine();
            System.out.println("\nPassword: ");
            password = new String(console.readPassword());
            System.out.println();
            System.out.println(username);
            System.out.println(password);
        }
    }


}
