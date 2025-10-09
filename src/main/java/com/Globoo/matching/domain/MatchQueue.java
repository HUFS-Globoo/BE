package com.Globoo.matching.domain;


import jakarta.persistence.*;

@Entity @Table(name = "match_queue")
public class MatchQueue {
    @Id
    private String id;
}
