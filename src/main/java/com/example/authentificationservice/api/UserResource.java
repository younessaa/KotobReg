package com.example.authentificationservice.api;


import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.authentificationservice.domain.Role;
import com.example.authentificationservice.domain.User;
import com.example.authentificationservice.service.UserService;
import com.example.authentificationservice.utils.JWTUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Arrays.stream;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserResource {
    private final UserService userService;

    @GetMapping("/users")
    @PostAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<List<User>> getUsers() {
        return ResponseEntity.ok().body(userService.getUsers());
    }

    @PostMapping("/users")
    @PostAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> saveUser(@RequestBody User user) {
        URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/api/users").toUriString());
        if(userService.existUser(user.getEmail())) {
            Map<String, String> error = new HashMap<>();
            error.put("error_message", "User Already Exists With This Email !");
            return ResponseEntity.status(CONFLICT).body(error);
        }

        return ResponseEntity.created(uri).body(userService.saveUser(user));
    }

    @PatchMapping("/users/{id}")
    @PostAuthorize("hasAnyAuthority('ROLE_LIBRARY_OWNER', 'ROLE_ADMIN')")
    public ResponseEntity<?> updateUser(@PathVariable(name = "id") String id ,@RequestBody Object user) throws NoSuchFieldException, IllegalAccessException, JsonProcessingException {

        if(!userService.isAllowedToManipulate(id)) {
            Map<String, String> error = new HashMap<>();
            error.put("error_message", "Not Authorized");
            return ResponseEntity.status(FORBIDDEN).body(error);
        }

        if(!userService.existUserById(id)) {
            Map<String, String> error = new HashMap<>();
            error.put("error_message", "User Not Exists");
            return ResponseEntity.status(NOT_FOUND).body(error);
        }

        return ResponseEntity.ok().body(userService.updateUser(id, user));
    }

    @DeleteMapping("/users/{id}")
    @PostAuthorize("hasAnyAuthority('ROLE_LIBRARY_OWNER', 'ROLE_ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable(name = "id") String id) {

        if(!userService.isAllowedToManipulate(id)) {
            Map<String, String> error = new HashMap<>();
            error.put("error_message", "Not Authorized");
            return ResponseEntity.status(FORBIDDEN).body(error);
        }

        if(!userService.existUserById(id)) {
            Map<String, String> error = new HashMap<>();
            error.put("error_message", "User Not Exists");
            return ResponseEntity.status(NOT_FOUND).body(error);
        }
        userService.deleteUser(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/roles")
    @PostAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<List<Role>> getRoles() {
        return ResponseEntity.ok().body(userService.getRoles());
    }

    @PostMapping("/roles")
    @PostAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Role> saveRole(@RequestBody Role role) {
        URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/api/roles").toUriString());
        return ResponseEntity.created(uri).body(userService.saveRole(role));
    }

    @PostMapping("/role/addtouser")
    @PostAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> addRoleToUser(@RequestBody RoleToUserForm form) {
        if(!userService.existUser(form.getEmail())) {
            Map<String, String> error = new HashMap<>();
            error.put("error_message", "User Not Exists");
            return ResponseEntity.status(NOT_FOUND).body(error);
        }
        userService.addRoleToUser(form.getEmail(), form.getRoleName());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/profile")
    public ResponseEntity<User> getProfile(Principal principal) {
        return ResponseEntity.ok().body(userService.getUser(principal.getName()));
    }

    @GetMapping("/token/refresh")
    public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {

        String authorizationHeader = request.getHeader(AUTHORIZATION);
        if(authorizationHeader != null && authorizationHeader.startsWith(JWTUtils.PREFIX)) {
            try {
                String refresh_token = authorizationHeader.substring(JWTUtils.PREFIX.length());
                Algorithm algorithm = Algorithm.HMAC256(JWTUtils.SECRET.getBytes());
                JWTVerifier verifier = JWT.require(algorithm).build();
                DecodedJWT decodedJWT = verifier.verify(refresh_token);
                String email = decodedJWT.getSubject();
                User user = userService.getUser(email);
                String access_token = JWT.create()
                        .withSubject(user.getEmail())
                        .withExpiresAt(new Date(System.currentTimeMillis() + JWTUtils.ACCESS_TOKEN))
                        .withIssuer(request.getRequestURL().toString())
                        .withClaim(JWTUtils.ROLES, user.getRoles().stream().map(Role::getName).collect(Collectors.toList()))
                        .sign(algorithm);


                Map<String, String> tokens = new HashMap<>();
                tokens.put("access_token", access_token);
                tokens.put("refresh_token", refresh_token);
                response.setContentType(APPLICATION_JSON_VALUE);
                new ObjectMapper().writeValue(response.getOutputStream(), tokens);
            } catch (Exception exception) {
                response.setStatus(FORBIDDEN.value());
                Map<String, String> error = new HashMap<>();
                error.put("error_message", exception.getMessage());
                response.setContentType(APPLICATION_JSON_VALUE);
                new ObjectMapper().writeValue(response.getOutputStream(), error);
            }
        }
        else {
            response.setStatus(BAD_REQUEST.value());
            Map<String, String> error = new HashMap<>();
            error.put("error_message", "Refresh token is missing");
            response.setContentType(APPLICATION_JSON_VALUE);
            new ObjectMapper().writeValue(response.getOutputStream(), error);
            //throw new RuntimeException("Refresh token is missing");
        }

    }
}

@Data
class RoleToUserForm {
    private String email;
    private String roleName;
}
