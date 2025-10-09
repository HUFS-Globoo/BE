package com.Globoo.study.domain;


import jakarta.persistence.*;

@Entity @Table(name = "comments")
public class Comment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
}
