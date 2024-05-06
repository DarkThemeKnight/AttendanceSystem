package com.example.clientSide.state;

import com.example.clientSide.utils.ApplicationUser;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ApplicationContext {
    private static ApplicationContext instance;
    private String jwtToken;
    private ApplicationUser loggedInUser;
    private ApplicationContext() {

    }
    public static ApplicationContext getInstance(){
        if (instance == null){instance = new ApplicationContext();}
        return instance;
    }
    public void clearInstance(){
        instance = null;
    }

}
