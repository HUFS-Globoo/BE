package com.Globoo.message.domain;


import jakarta.persistence.*;

@Entity @Table(name = "dm_threads")
public class DmThread {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
}
