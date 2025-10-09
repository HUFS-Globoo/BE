package com.Globoo.study.domain;


import jakarta.persistence.*;

@Entity @Table(name = "study_posts")
public class StudyPost {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
}
