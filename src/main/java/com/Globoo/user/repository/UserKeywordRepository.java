package com.Globoo.user.repository;

import com.Globoo.user.domain.UserKeyword;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserKeywordRepository extends JpaRepository<UserKeyword, Long> {

    // user 엔티티의 id를 기준으로 조회 (UserKeyword.user.id)
    List<UserKeyword> findAllByUser_Id(Long userId);

    // user 엔티티의 id를 기준으로 삭제 (UserKeyword.user.id)
    void deleteAllByUser_Id(Long userId);
}
