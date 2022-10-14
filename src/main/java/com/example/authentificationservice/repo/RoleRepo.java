package com.example.authentificationservice.repo;

import com.example.authentificationservice.domain.Role;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface RoleRepo extends MongoRepository<Role, String> {
    Role findRoleByName(String name);
}
