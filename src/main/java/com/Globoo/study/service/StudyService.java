package com.Globoo.study.service;

import com.Globoo.study.DTO.StudyPostDto;
import com.Globoo.study.domain.StudyPost;
import com.Globoo.study.repository.StudyPostRepository;
import com.Globoo.user.domain.User; // 임포트
import com.Globoo.user.repository.UserRepository; // 임포트
import jakarta.persistence.criteria.JoinType; // 임포트
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class StudyService {

    private final StudyPostRepository studyPostRepository;
    private final UserRepository userRepository; // UserRepository 주입

    public StudyService(StudyPostRepository studyPostRepository, UserRepository userRepository) {
        this.studyPostRepository = studyPostRepository;
        this.userRepository = userRepository; // 생성자 주입
    }

    // =========================
    // 목록 조회 (필터 포함)
    // =========================
    @Transactional(readOnly = true)
    public List<StudyPostDto.Response> getStudyPosts(
            String status, List<String> campus, List<String> language, Integer minCapacity, Integer maxCapacity
    ) {
        final String normStatus = normalizeStatus(status);
        final List<String> normCampuses = normalizeCampusList(campus);
        final List<String> normLangs = normalizeLanguageList(language);

        Integer minCap = minCapacity;
        Integer maxCap = maxCapacity;
        if (minCap != null && maxCap != null && minCap > maxCap) {
            int tmp = minCap; minCap = maxCap; maxCap = tmp;
        }
        final Integer fMinCap = minCap;
        final Integer fMaxCap = maxCap;

        // Specification 수정
        Specification<StudyPost> spec = (root, query, cb) -> {
            // N+1 문제 해결을 위한 Fetch Join 추가 (user -> profile)
            if (query.getResultType() != Long.class && query.getResultType() != long.class) {
                root.fetch("user", JoinType.LEFT).fetch("profile", JoinType.LEFT);
            }
            query.distinct(true);

            Predicate predicate = cb.conjunction();

            if (normStatus != null) {
                predicate = cb.and(predicate, cb.equal(root.get("status"), normStatus));
            }
            if (fMinCap != null) {
                predicate = cb.and(predicate, cb.greaterThanOrEqualTo(root.get("capacity"), fMinCap));
            }
            if (fMaxCap != null) {
                predicate = cb.and(predicate, cb.lessThanOrEqualTo(root.get("capacity"), fMaxCap));
            }

            // 캠퍼스: 리스트 중 하나라도 일치하면
            if (normCampuses != null && !normCampuses.isEmpty()) {
                predicate = cb.and(predicate, root.join("campuses").in(normCampuses));
            }

            // 언어: 리스트 중 하나라도 일치하면
            if (normLangs != null && !normLangs.isEmpty()) {
                predicate = cb.and(predicate, root.join("languages").in(normLangs));
            }

            return predicate;
        };

        return studyPostRepository.findAll(spec).stream()
                .map(StudyPostDto.Response::new)
                .toList();
    }

    // =========================
    // 단일 조회
    // =========================
    @Transactional(readOnly = true)
    public StudyPostDto.Response getStudyPost(Long id) {
        // N+1 방지를 위해 join fetch 쿼리 사용
        StudyPost post = studyPostRepository.findByIdWithUserAndProfile(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 스터디 글을 찾을 수 없습니다. id=" + id));
        return new StudyPostDto.Response(post);
    }

    // =========================
    // 생성
    // =========================
    // (중요) currentUserId 파라미터 추가
    public StudyPostDto.Response createStudyPost(StudyPostDto.Request req, Long currentUserId) {

        // 현재 사용자 조회
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new IllegalArgumentException("사용자 정보를 찾을 수 없습니다."));

        // 유효성 검사 및 Set 변환
        Set<String> campuses = validateAndNormalizeCampuses(req.getCampuses());
        if (campuses.isEmpty()) {
            throw new IllegalArgumentException("campus는 하나 이상 선택해야 합니다.");
        }

        Set<String> languages = validateAndNormalizeLanguages(req.getLanguages());
        if (languages.isEmpty()) {
            throw new IllegalArgumentException("language는 하나 이상 선택해야 합니다.");
        }

        StudyPost post = new StudyPost(
                req.getTitle(),
                req.getContent(),
                normalizeStatusOrDefault(req.getStatus(), "모집중"),
                campuses,
                languages,
                validateCapacity(req.getCapacity()),
                user // user 객체 전달
        );

        StudyPost saved = studyPostRepository.save(post);
        return new StudyPostDto.Response(saved);
    }

    // =========================
    // 부분 수정 (PATCH)
    // =========================
    //  currentUserId 파라미터 추가
    public StudyPostDto.Response updateStudyPost(Long id, StudyPostDto.Request req, Long currentUserId) {
        // 권한 확인을 위해 User 정보까지 fetch
        StudyPost post = studyPostRepository.findByIdWithUserAndProfile(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 스터디 글을 찾을 수 없습니다. id=" + id));

        // 본인 확인 로직
        if (!post.getUser().getId().equals(currentUserId)) {
            throw new SecurityException("수정 권한이 없습니다."); // 403 Forbidden 예외 처리 권장
        }

        // 제목
        if (isPresentAndNotString(req.getTitle())) {
            post.setTitle(req.getTitle().trim());
        }

        // 내용
        if (isPresentAndNotString(req.getContent())) {
            post.setContent(req.getContent().trim());
        }

        // 상태 (모집중/모집완료)
        if (isPresentAndNotString(req.getStatus())) {
            String n = normalizeStatus(req.getStatus());
            if (n != null) post.setStatus(n);
        }

        // 캠퍼스 (null이 아니면 업데이트)
        if (req.getCampuses() != null) {
            Set<String> campuses = validateAndNormalizeCampuses(req.getCampuses());
            if (campuses.isEmpty()) {
                throw new IllegalArgumentException("campus는 하나 이상 선택해야 합니다.");
            }
            post.setCampuses(campuses);
        }

        // 언어 (null이 아니면 업데이트)
        if (req.getLanguages() != null) {
            Set<String> languages = validateAndNormalizeLanguages(req.getLanguages());
            if (languages.isEmpty()) {
                throw new IllegalArgumentException("language는 하나 이상 선택해야 합니다.");
            }
            post.setLanguages(languages);
        }

        // 최대 인원
        if (req.getCapacity() != null) {
            post.setCapacity(validateCapacity(req.getCapacity()));
        }

        return new StudyPostDto.Response(studyPostRepository.save(post));
    }

    // =========================
    // 삭제
    // =========================
    // (중요) currentUserId 파라미터 추가
    public void deleteStudyPost(Long id, Long currentUserId) {
        // 권한 확인을 위해 User 정보까지 fetch
        StudyPost post = studyPostRepository.findByIdWithUserAndProfile(id)
                .orElseThrow(() -> new IllegalArgumentException("이미 삭제되었거나 존재하지 않는 글입니다. id=" + id));

        // 본인 확인 로직
        if (!post.getUser().getId().equals(currentUserId)) {
            throw new SecurityException("삭제 권한이 없습니다."); // 403 Forbidden 예외 처리 권장
        }

        studyPostRepository.deleteById(id);
    }

    // =========================
    // 유틸 함수
    // =========================

    private boolean isPresentAndNotString(String v) {
        if (v == null) return false;
        String t = v.trim();
        return !t.isEmpty() && !"string".equalsIgnoreCase(t);
    }

    private String normalizeStatus(String raw) {
        if (raw == null) return null;
        String s = raw.trim().toLowerCase();
        if (s.isEmpty()) return null;
        if (s.equals("open") || s.equals("모집중") || s.equals("recruiting")) return "모집중";
        if (s.equals("closed") || s.equals("close") || s.equals("모집완료")) return "모집완료";
        return raw.trim();
    }

    private String normalizeStatusOrDefault(String raw, String dft) {
        String n = normalizeStatus(raw);
        return (n == null ? dft : n);
    }

    // --- 단일 값 정규화 ---
    private String normalizeCampus(String raw) {
        if (raw == null) return null;
        String s = raw.trim().toLowerCase();
        if (s.isEmpty()) return null;
        if (s.equals("global") || s.equals("글로벌")) return "글로벌";
        if (s.equals("seoul") || s.equals("서울"))   return "서울";
        return raw.trim(); // 유효하지 않은 값
    }

    // --- (필터용) 리스트 정규화 ---
    private List<String> normalizeCampusList(List<String> rawList) {
        if (rawList == null) return null;
        return rawList.stream()
                .map(this::normalizeCampus) // 단일 정규화
                .filter(StudyPost.getAllowedCampuses()::contains) // 유효한 값만
                .distinct()
                .toList();
    }

    private List<String> normalizeLanguageList(List<String> rawList) {
        if (rawList == null) return null;
        List<String> allowed = StudyPost.getAllowedLanguages();
        return rawList.stream()
                .map(s -> s == null ? "" : s.trim())
                .filter(allowed::contains) // 유효한 값만
                .distinct()
                .toList();
    }

    // --- (생성/수정용) 리스트 유효성 검사 및 Set 변환 ---

    private Set<String> validateAndNormalizeCampuses(List<String> rawCampuses) {
        if (rawCampuses == null) {
            return new HashSet<>();
        }
        List<String> allowed = StudyPost.getAllowedCampuses();

        return rawCampuses.stream()
                .map(this::normalizeCampus) // "global" -> "글로벌"
                .map(s -> {
                    if (s == null || s.trim().isEmpty()) return null;
                    if (!allowed.contains(s)) {
                        throw new IllegalArgumentException("캠퍼스는 '서울' 또는 '글로벌'만 가능합니다: " + s);
                    }
                    return s;
                })
                .filter(s -> s != null)
                .collect(Collectors.toSet());
    }

    private Set<String> validateAndNormalizeLanguages(List<String> rawLangs) {
        if (rawLangs == null) {
            return new HashSet<>();
        }
        List<String> allowed = StudyPost.getAllowedLanguages();

        return rawLangs.stream()
                .map(s -> s == null ? "" : s.trim())
                .filter(s -> !s.isEmpty())
                .map(s -> {
                    if (!allowed.contains(s)) {
                        throw new IllegalArgumentException("지원하지 않는 언어입니다: " + s);
                    }
                    return s;
                })
                .collect(Collectors.toSet());
    }

    private Integer validateCapacity(Integer capacity) {
        if (capacity == null) {
            throw new IllegalArgumentException("capacity(최대 인원)는 필수입니다. (1~6)");
        }
        if (capacity < 1 || capacity > 6) {
            throw new IllegalArgumentException("capacity는 1 이상 6 이하만 가능합니다.");
        }
        return capacity;
    }
}