package com.example.authentificationservice.service;

import com.example.authentificationservice.domain.Role;
import com.example.authentificationservice.domain.User;
import com.example.authentificationservice.repo.RoleRepo;
import com.example.authentificationservice.repo.UserRepo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

@Service @RequiredArgsConstructor @Transactional @Slf4j
public class UserServiceImpl implements UserService, UserDetailsService {
    private final UserRepo userRepo;
    private final RoleRepo roleRepo;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepo.findUserByEmail(email);
        if(user == null) {
            log.error("User not found in the database");
            throw new UsernameNotFoundException("User not found in the database");
        }
        else {
            log.info("User {} found in the database", email);
        }
        Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
        user.getRoles().forEach(role -> {
            authorities.add(new SimpleGrantedAuthority(role.getName()));
        });
        return new org.springframework.security.core.userdetails.User(user.getEmail(), user.getPassword(), authorities);
    }

    @Override
    public boolean existUserById(String id) {
        return userRepo.existsById(id);
    }

    @Override
    public boolean isAllowedToManipulate(String id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentPrincipalName = authentication.getName();
        User currentUser = getUser(currentPrincipalName);

        List<String> roles = currentUser.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toList());
        return currentUser.getId().equals(id) || roles.contains("ROLE_ADMIN");
    }

    @Override
    public User saveUser(User user) {
        log.info("user saved {}", user.getFirstName());
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepo.save(user);
    }

    @Override
    public boolean existUser(String email) {
        return userRepo.existsByEmail(email);
    }

    @Override
    public User updateUser(String id, Object jsonObj) throws JsonProcessingException {
        log.info("user updated {}", jsonObj.toString());
        User user = userRepo.findById(id).get();
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String json = ow.writeValueAsString(jsonObj);
        JSONObject jsonObject = new JSONObject(json);
        Iterator<String> keys = jsonObject.keys();
        log.info(jsonObject.toString());

        while(keys.hasNext()) {
            String key = keys.next();
            if (key.equals("password")) {
                jsonObject.put(key, passwordEncoder.encode(jsonObject.getString(key)));
            }
            user.setField(key, jsonObject.getString(key));
        }
        log.info("!! {}", user.getFirstName());
        return userRepo.save(user);
    }



    @Override
    public void deleteUser(String id) {
        userRepo.deleteById(id);
    }

    @Override
    public List<Role> getRoles() {
        return roleRepo.findAll();
    }

    @Override
    public Role getRole(String roleName) {
        return roleRepo.findRoleByName(roleName);
    }

    @Override
    public Role saveRole(Role role) {
        log.info("role saved {}", role.getName());
        return roleRepo.save(role);
    }

    @Override
    public void addRoleToUser(String email, String roleName) {
        User user = userRepo.findUserByEmail(email);
        Role role = roleRepo.findRoleByName(roleName);
        Collection<Role> roles = user.getRoles();
        roles.add(role);
        user.setRoles(roles);
        userRepo.save(user);

        log.info("Adding role {} to user {}", roleName, email);
    }

    @Override
    public User getUser(String email) {
        log.info("Fetching user {}", email);
        return userRepo.findUserByEmail(email);
    }

    @Override
    public List<User> getUsers() {
        log.info("Fetching all users");
        return userRepo.findAll();
    }


}
