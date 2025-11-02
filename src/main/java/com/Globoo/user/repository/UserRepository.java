// src/main/java/com/Globoo/user/domain/UserRepository.java
package com.Globoo.user.repository;

import com.Globoo.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
    Optional<User> findByEmail(String email);
}
