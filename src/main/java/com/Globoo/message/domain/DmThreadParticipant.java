package com.Globoo.message.domain;


import jakarta.persistence.*;

@Entity @Table(name = "dm_thread_participants")
public class DmThreadParticipant {
    @Id
    private Long userId; // composite key -> 추후 @IdClass 사용
}
