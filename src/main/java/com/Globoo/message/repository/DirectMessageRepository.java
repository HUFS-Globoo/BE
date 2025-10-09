package com.Globoo.message.repository;


import com.Globoo.message.domain.DirectMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DirectMessageRepository extends JpaRepository<DirectMessage, Long> { }
