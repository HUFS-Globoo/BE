package com.Globoo.user.domain;


import jakarta.persistence.*;

@Entity @Table(name = "profiles")
public class Profile {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
}
