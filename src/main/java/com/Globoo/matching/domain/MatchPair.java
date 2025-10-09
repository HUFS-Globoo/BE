package com.Globoo.matching.domain;


import jakarta.persistence.*;

@Entity @Table(name = "match_pair")
public class MatchPair {
    @Id
    private String id;
}
