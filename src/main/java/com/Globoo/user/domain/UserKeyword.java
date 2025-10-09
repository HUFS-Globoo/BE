package com.Globoo.user.domain;

import jakarta.persistence.*;

@Entity @Table(name = "user_keywords")
public class UserKeyword {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
}
