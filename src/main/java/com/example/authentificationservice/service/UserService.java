package com.example.authentificationservice.service;

import com.example.authentificationservice.domain.Role;
import com.example.authentificationservice.domain.User;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.List;

public interface UserService {
    User saveUser(User user);

    boolean existUserById(String id);

    boolean isAllowedToManipulate(String id);

    boolean existUser(String email);

    User updateUser(String id, Object user) throws NoSuchFieldException, IllegalAccessException, JsonProcessingException;

    void deleteUser(String id);

    List<Role> getRoles();

    Role getRole(String roleName);
    Role saveRole(Role role);
    void addRoleToUser(String email, String roleName);
    User getUser(String email);
    List<User> getUsers();
}
