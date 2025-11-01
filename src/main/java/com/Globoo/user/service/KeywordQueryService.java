// src/main/java/com/Globoo/user/service/KeywordQueryService.java
package com.Globoo.user.service;

import com.Globoo.user.domain.Keyword;
import com.Globoo.user.dto.KeywordGroupRes;
import com.Globoo.user.dto.KeywordRes;
import com.Globoo.user.repository.KeywordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class KeywordQueryService {

    private final KeywordRepository keywordRepo;

    public List<KeywordRes> listAllActive() {
        return keywordRepo.findAllByActiveTrueOrderByCategoryAscSortOrderAscNameAsc()
                .stream().map(KeywordRes::from).toList();
    }

    public List<KeywordRes> listByCategory(Keyword.Category cat) {
        return keywordRepo.findAllByCategoryAndActiveTrueOrderBySortOrderAscNameAsc(cat)
                .stream().map(KeywordRes::from).toList();
    }

    public KeywordGroupRes listGrouped() {
        return KeywordGroupRes.builder()
                .personality(listByCategory(Keyword.Category.PERSONALITY))
                .hobby(listByCategory(Keyword.Category.HOBBY))
                .topic(listByCategory(Keyword.Category.TOPIC))
                .build();
    }
}
