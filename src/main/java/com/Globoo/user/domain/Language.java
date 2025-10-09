package com.Globoo.user.domain;

import jakarta.persistence.*;

@Entity @Table(name = "languages")
public class Language {
    @Id
    private String code;
}
