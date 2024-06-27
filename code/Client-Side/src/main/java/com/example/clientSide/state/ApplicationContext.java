package com.example.clientSide.state;
import com.example.clientSide.helper.Role;
import lombok.Data;
import org.checkerframework.checker.units.qual.A;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
public class ApplicationContext {
    private static ApplicationContext context;
    private String token;
    private Set<Role> roles;
    private LocalDateTime tokenExpiryDate;

    private ApplicationContext(){

    }
    public boolean isExpiredToken(){
        return tokenExpiryDate.isBefore(LocalDateTime.now());
    }
    public static ApplicationContext getInstance(){
        if (context == null){
            context = new ApplicationContext();
        }
        return context;
    }
    public void destroyContext() {
        context = null;
    }

}
