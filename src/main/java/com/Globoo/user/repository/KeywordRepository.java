package com.Globoo.user.repository;

import com.Globoo.user.domain.Keyword;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface KeywordRepository extends JpaRepository<Keyword, Long> {

    Optional<Keyword> findByName(String name);

    // 회원가입·마이페이지에서 사용: category별로 name 목록으로 조회
    List<Keyword> findAllByCategoryAndNameIn(Keyword.Category category, Collection<String> names);

    //호환성용 (기존 로직 유지)
    List<Keyword> findAllByNameIn(Collection<String> names);

    //전체 키워드 조회 API (/api/keywords/grouped 등)
    List<Keyword> findAllByActiveTrueOrderByCategoryAscSortOrderAscNameAsc();

    //특정 카테고리별 조회 (예: "성격만")
    List<Keyword> findAllByCategoryAndActiveTrueOrderBySortOrderAscNameAsc(Keyword.Category category);
}
