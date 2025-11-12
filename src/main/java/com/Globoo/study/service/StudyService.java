package com.Globoo.study.service;

import com.Globoo.study.DTO.StudyPostDto;
import com.Globoo.study.domain.StudyMember;
import com.Globoo.study.domain.StudyPost;
import com.Globoo.study.repository.StudyMemberRepository;
import com.Globoo.study.repository.StudyPostRepository;
import com.Globoo.user.domain.User;
import com.Globoo.user.repository.UserRepository;
import jakarta.persistence.criteria.JoinType;
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
    private final UserRepository userRepository;
    private final StudyMemberRepository studyMemberRepository;

    public StudyService(StudyPostRepository studyPostRepository,
                        UserRepository userRepository,
                        StudyMemberRepository studyMemberRepository) {
        this.studyPostRepository = studyPostRepository;
        this.userRepository = userRepository;
        this.studyMemberRepository = studyMemberRepository;
    }

    // =========================
    // 목록 조회 (필터 포함)
    // =========================
    @Transactional(readOnly = true)
    // ✅ (수정) Controller와 파라미터 일치 (min/max Capacity 제거)
    public List<StudyPostDto.Response> getStudyPosts(
            String status, List<String> campus, List<String> language
    ) {
        final String normStatus = normalizeStatus(status);
        final List<String> normCampuses = normalizeCampusList(campus);
        final List<String> normLangs = normalizeLanguageList(language);

        Specification<StudyPost> spec = (root, query, cb) -> {
            if (query.getResultType() != Long.class && query.getResultType() != long.class) {
                root.fetch("user", JoinType.LEFT).fetch("profile", JoinType.LEFT);
                root.fetch("members", JoinType.LEFT);
            }
            query.distinct(true);

            Predicate predicate = cb.conjunction();

            if (normStatus != null)  predicate = cb.and(predicate, cb.equal(root.get("status"), normStatus));
            if (normCampuses != null && !normCampuses.isEmpty())  predicate = cb.and(predicate, root.join("campuses").in(normCampuses));
            if (normLangs != null && !normLangs.isEmpty())    predicate = cb.and(predicate, root.join("languages").in(normLangs));

            // ✅ (삭제) capacity 필터 로직 제거됨

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
        StudyPost post = studyPostRepository.findByIdWithUserAndProfileAndMembers(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 스터디 글을 찾을 수 없습니다. id=" + id));
        return new StudyPostDto.Response(post);
    }

    // =========================
    // 생성
    // =========================
    public StudyPostDto.Response createStudyPost(StudyPostDto.Request req, Long currentUserId) {
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new IllegalArgumentException("사용자 정보를 찾을 수 없습니다."));

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
                user
        );

        StudyPost savedPost = studyPostRepository.save(post);

        //  (수정) Role 제거
        StudyMember creatorAsMember = StudyMember.builder()
                .user(user)
                .studyPost(savedPost)
                // .role(StudyMember.Role.LEADER) // <-- 이 부분이 제거되었습니다.
                .build();
        studyMemberRepository.save(creatorAsMember);

        savedPost.getMembers().add(creatorAsMember); // 500 에러(JPA 캐시) 방지

        return new StudyPostDto.Response(savedPost);
    }

    // =========================
    // 부분 수정 (PATCH)
    // =========================
    public StudyPostDto.Response updateStudyPost(Long id, StudyPostDto.Request req, Long currentUserId) {
        StudyPost post = studyPostRepository.findByIdWithUserAndProfileAndMembers(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 스터디 글을 찾을 수 없습니다. id=" + id));
        if (!post.getUser().getId().equals(currentUserId)) {
            throw new SecurityException("수정 권한이 없습니다.");
        }
        if (isPresentAndNotString(req.getTitle())) post.setTitle(req.getTitle().trim());
        if (isPresentAndNotString(req.getContent())) post.setContent(req.getContent().trim());
        if (isPresentAndNotString(req.getStatus())) {
            String n = normalizeStatus(req.getStatus());
            if (n != null) post.setStatus(n);
        }

        if (req.getCampuses() != null) {
            Set<String> campuses = validateAndNormalizeCampuses(req.getCampuses());
            if (campuses.isEmpty()) {
                throw new IllegalArgumentException("campus는 하나 이상 선택해야 합니다.");
            }
            post.setCampuses(campuses);
        }

        if (req.getLanguages() != null) {
            Set<String> languages = validateAndNormalizeLanguages(req.getLanguages());
            if (languages.isEmpty()) {
                throw new IllegalArgumentException("language는 하나 이상 선택해야 합니다.");
            }
            post.setLanguages(languages);
        }

        if (req.getCapacity() != null) {
            Integer newCapacity = validateCapacity(req.getCapacity());
            int currentParticipants = post.getMembers().size();
            if (newCapacity < currentParticipants) {
                throw new IllegalArgumentException("새로운 최대 인원은 현재 참여 인원(" + currentParticipants + "명)보다 적을 수 없습니다.");
            }
            post.setCapacity(newCapacity);
        }
        return new StudyPostDto.Response(studyPostRepository.save(post));
    }

    // =========================
    // 삭제
    // =========================
    public void deleteStudyPost(Long id, Long currentUserId) {
        StudyPost post = studyPostRepository.findByIdWithUserAndProfileAndMembers(id)
                .orElseThrow(() -> new IllegalArgumentException("이미 삭제되었거나 존재하지 않는 글입니다. id=" + id));
        if (!post.getUser().getId().equals(currentUserId)) {
            throw new SecurityException("삭제 권한이 없습니다.");
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
        if (s.equals("closed") || s.equals("close") || s.equals("모집완료") || s.equals("마감")) return "마감";
        return raw.trim();
    }

    private String normalizeStatusOrDefault(String raw, String dft) {
        String n = normalizeStatus(raw);
        return (n == null ? dft : n);
    }

    private String normalizeCampus(String raw) {
        if (raw == null) return null;
        String s = raw.trim().toLowerCase();
        if (s.isEmpty()) return null;
        if (s.equals("global") || s.equals("글로벌")) return "글로벌";
        if (s.equals("seoul") || s.equals("서울"))   return "서울";
        return raw.trim();
    }
    private List<String> normalizeCampusList(List<String> rawList) {
        if (rawList == null) return null;
        return rawList.stream()
                .map(this::normalizeCampus)
                .filter(StudyPost.getAllowedCampuses()::contains)
                .distinct()
                .toList();
    }
    private List<String> normalizeLanguageList(List<String> rawList) {
        if (rawList == null) return null;
        List<String> allowed = StudyPost.getAllowedLanguages();
        return rawList.stream()
                .map(s -> s == null ? "" : s.trim())
                .filter(allowed::contains)
                .distinct()
                .toList();
    }
    private Set<String> validateAndNormalizeCampuses(List<String> rawCampuses) {
        if (rawCampuses == null) {
            return new HashSet<>();
        }
        List<String> allowed = StudyPost.getAllowedCampuses();
        return rawCampuses.stream()
                .map(this::normalizeCampus)
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
                        throw new IllegalArgumentException("지원하지 않는 언어입니다:" + s);
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