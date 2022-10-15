package com.example.authentificationservice;

import com.example.authentificationservice.domain.Role;
import com.example.authentificationservice.domain.User;
import com.example.authentificationservice.service.UserDetailsServiceImp;
import com.example.authentificationservice.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class AuthentificationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthentificationServiceApplication.class, args);
    }

    @Bean
    PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }


    @Bean
    CommandLineRunner run(UserService userService) {
        return args -> {
//            userService.saveRole(new Role(null, "ROLE_LIBRARY_OWNER"));
//            userService.saveRole(new Role(null, "ROLE_ADMIN"));
//
//
//            userService.saveUser(new User("admin@kotobrepo.ma", "admin", new ArrayList<>()));
//            userService.saveUser(new
//                    User(null, "youness", "aabaoui", "youness@gmail.com", "youness", new ArrayList<>(), new ArrayList<>()));
//            userService.saveUser(new
//                    User(null, "alae", "abjabja", "alae@gmail.com", "alae", new ArrayList<>(), new ArrayList<>()));
//
//            userService.addRoleToUser("admin@kotobrepo.ma", "ROLE_ADMIN");
//            userService.addRoleToUser("youness@gmail.com", "ROLE_LIBRARY_OWNER");
//            userService.addRoleToUser("alae@gmail.com", "ROLE_LIBRARY_OWNER");
        };
    }

}
