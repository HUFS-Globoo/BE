package com.Globoo.user.repository;


import com.Globoo.user.domain.Profile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfileRepository extends JpaRepository<Profile, Long> { }
