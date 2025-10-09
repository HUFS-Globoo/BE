package com.Globoo.user.domain;


import jakarta.persistence.*;

@Entity @Table(name = "keywords")
public class Keyword {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
}
