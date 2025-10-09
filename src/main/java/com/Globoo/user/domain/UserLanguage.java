package com.Globoo.user.domain;

import jakarta.persistence.*;

@Entity @Table(name = "user_languages")
public class UserLanguage {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
}
