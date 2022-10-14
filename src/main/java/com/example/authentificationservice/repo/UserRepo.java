package com.example.authentificationservice.repo;


import com.example.authentificationservice.domain.User;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepo extends MongoRepository<User, String> {
    User findUserByEmail(String email);
    boolean existsByEmail(String email);
}
