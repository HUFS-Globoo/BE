package com.Globoo.profile.store;

import com.Globoo.user.domain.*;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public final class ProfileSpecs {
    private ProfileSpecs() {}

    // 현재 User.active 같은 필드가 없으므로 '항상 참' 반환
    public static Specification<Profile> activeUser() {
        return (root, q, cb) -> cb.conjunction(); // 항상 참 (필요시 schoolVerified 등으로 교체)
    }


    public static Specification<Profile> eqCampus(Campus campus) {
        if (campus == null) return null;
        return (root, q, cb) -> cb.equal(root.get("campus"), campus);
    }

    public static Specification<Profile> hasNativeLang(String code) {
        if (code == null || code.isBlank()) return null;
        return (root, q, cb) -> {
            var u = root.join("user", JoinType.INNER);
            var langs = u.join("userLanguages", JoinType.INNER);
            q.distinct(true);
            return cb.and(
                    cb.equal(cb.lower(langs.get("language").get("code")), code.toLowerCase()),
                    cb.equal(langs.get("type"), LanguageType.NATIVE)
            );
        };
    }

    public static Specification<Profile> hasLearnLang(String code) {
        if (code == null || code.isBlank()) return null;
        return (root, q, cb) -> {
            var u = root.join("user", JoinType.INNER);
            var langs = u.join("userLanguages", JoinType.INNER);
            q.distinct(true);
            return cb.and(
                    cb.equal(cb.lower(langs.get("language").get("code")), code.toLowerCase()),
                    cb.equal(langs.get("type"), LanguageType.LEARN)
            );
        };
    }

    // 키워드: "하나라도 포함" (OR)
    public static Specification<Profile> hasAnyKeywordIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return null;
        return (root, q, cb) -> {
            var u = root.join("user", JoinType.INNER);
            var uk = u.join("userKeywords", JoinType.INNER);
            q.distinct(true);
            return uk.get("keyword").get("id").in(ids);
        };
    }
}
