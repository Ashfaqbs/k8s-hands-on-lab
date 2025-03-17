package com.ashfaq.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ashfaq.user.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    
}
