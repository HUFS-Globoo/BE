package com.Globoo.message.domain;


import jakarta.persistence.*;

@Entity @Table(name = "direct_messages")
public class DirectMessage {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
}
