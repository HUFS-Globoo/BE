package com.Globoo.message.domain;

import com.Globoo.user.domain.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "dm_thread_participants")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DmThreadParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "thread_id")
    private DmThread thread;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
}
