package com.Globoo.study.domain;

import com.Globoo.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "study_members",
        uniqueConstraints = {
                // 한 유저가 한 스터디에 중복 가입 방지
                @UniqueConstraint(
                        name = "uk_member_user_study",
                        columnNames = {"user_id", "study_post_id"}
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StudyMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_post_id", nullable = false)
    private StudyPost studyPost;

    //  (삭제) Role 필드 제거
    // public enum Role { LEADER, MEMBER }
    // @Enumerated(EnumType.STRING)
    // @Column(nullable = false, length = 10)
    // private Role role;

    @Builder
    public StudyMember(User user, StudyPost studyPost) { //  Role 파라미터 제거
        this.user = user;
        this.studyPost = studyPost;
        // this.role = role; //  Role 할당 제거
    }
}