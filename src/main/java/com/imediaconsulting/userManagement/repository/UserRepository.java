package com.imediaconsulting.userManagement.repository;

import com.imediaconsulting.userManagement.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmail(String email);
}