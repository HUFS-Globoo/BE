package com.Globoo.study.service;

import com.Globoo.study.DTO.StudyPostDto;
import com.Globoo.study.domain.StudyMember;
import com.Globoo.study.domain.StudyPost;
import com.Globoo.study.repository.StudyMemberRepository;
import com.Globoo.study.repository.StudyPostRepository;
import com.Globoo.user.domain.Language;
import com.Globoo.user.domain.User;
import com.Globoo.user.repository.LanguageRepository;
import com.Globoo.user.repository.UserRepository;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class StudyService {

    private final StudyPostRepository studyPostRepository;
    private final UserRepository userRepository;
    private final StudyMemberRepository studyMemberRepository;
    private final LanguageRepository languageRepository;

    // ✅ 한글 라벨 -> code 매핑 (프론트가 "아랍어" 같은 라벨로 보내는 상황 대응)
    private static final Map<String, String> KO_LABEL_TO_CODE = Map.ofEntries(
            Map.entry("한국어", "ko"),
            Map.entry("영어", "en"),
            Map.entry("중국어", "zh"),
            Map.entry("일본어", "ja"),
            Map.entry("프랑스어", "fr"),
            Map.entry("독일어", "de"),
            Map.entry("스페인어", "es"),
            Map.entry("아랍어", "ar"),
            Map.entry("이탈리아어", "it"),
            Map.entry("러시아어", "ru"),
            Map.entry("폴란드어", "pl"),
            Map.entry("체코어", "cs"),
            Map.entry("슬로바키아어", "sk"),
            Map.entry("루마니아어", "ro"),
            Map.entry("불가리아어", "bg"),
            Map.entry("베트남어", "vi"),
            Map.entry("태국어", "th"),
            Map.entry("인도네시아어", "id"),
            Map.entry("말레이어", "ms"),
            Map.entry("몽골어", "mn"),
            Map.entry("힌디어", "hi"),
            Map.entry("페르시아어", "fa"),
            Map.entry("터키어", "tr"),
            Map.entry("히브리어", "he"),
            Map.entry("카자흐어", "kk"),
            Map.entry("우즈벡어", "uz")
    );

    public StudyService(StudyPostRepository studyPostRepository,
                        UserRepository userRepository,
                        StudyMemberRepository studyMemberRepository,
                        LanguageRepository languageRepository) {
        this.studyPostRepository = studyPostRepository;
        this.userRepository = userRepository;
        this.studyMemberRepository = studyMemberRepository;
        this.languageRepository = languageRepository;
    }

    @Transactional(readOnly = true)
    public List<StudyPostDto.Response> getStudyPosts(
            String status, List<String> campus, List<String> language
    ) {
        final String normStatus = normalizeStatus(status);
        final List<String> normCampuses = normalizeCampusList(campus);
        final List<String> normLangCodes = normalizeLanguageCodes(language);

        Specification<StudyPost> spec = (root, query, cb) -> {
            if (query.getResultType() != Long.class && query.getResultType() != long.class) {
                root.fetch("user", JoinType.LEFT).fetch("profile", JoinType.LEFT);
                root.fetch("members", JoinType.LEFT);
            }
            query.distinct(true);

            Predicate predicate = cb.conjunction();

            if (normStatus != null) {
                predicate = cb.and(predicate, cb.equal(root.get("status"), normStatus));
            }

            if (normCampuses != null && !normCampuses.isEmpty()) {
                Join<StudyPost, String> cJoin = root.join("campuses", JoinType.INNER);
                predicate = cb.and(predicate, cJoin.in(normCampuses));
            }

            // languages에는 code가 저장되어 있으므로 code 리스트로 필터링
            if (normLangCodes != null && !normLangCodes.isEmpty()) {
                Join<StudyPost, String> lJoin = root.join("languages", JoinType.INNER);
                predicate = cb.and(predicate, lJoin.in(normLangCodes));
            }

            return predicate;
        };

        return studyPostRepository.findAll(spec).stream()
                .map(StudyPostDto.Response::new)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<StudyPostDto.Response> getMyStudyPosts(Long currentUserId) {
        return studyPostRepository.findAllByUserIdOrderByCreatedAtDesc(currentUserId)
                .stream()
                .map(StudyPostDto.Response::new)
                .toList();
    }

    @Transactional(readOnly = true)
    public StudyPostDto.Response getStudyPost(Long id) {
        StudyPost post = studyPostRepository.findByIdWithUserAndProfileAndMembers(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 스터디 글을 찾을 수 없습니다. id=" + id));
        return new StudyPostDto.Response(post);
    }

    public StudyPostDto.Response createStudyPost(StudyPostDto.Request req, Long currentUserId) {
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new IllegalArgumentException("사용자 정보를 찾을 수 없습니다."));

        Set<String> campuses = validateAndNormalizeCampuses(req.getCampuses());
        if (campuses.isEmpty()) {
            throw new IllegalArgumentException("campus는 하나 이상 선택해야 합니다.");
        }

        // languages는 DB 기준으로 검증 후 code로 저장
        Set<String> languageCodes = validateAndNormalizeLanguageCodes(req.getLanguages());
        if (languageCodes.isEmpty()) {
            throw new IllegalArgumentException("language는 하나 이상 선택해야 합니다.");
        }

        StudyPost post = new StudyPost(
                req.getTitle(),
                req.getContent(),
                normalizeStatusOrDefault(req.getStatus(), "모집중"),
                campuses,
                languageCodes,
                validateCapacity(req.getCapacity()),
                user
        );

        StudyPost savedPost = studyPostRepository.save(post);

        StudyMember creatorAsMember = StudyMember.builder()
                .user(user)
                .studyPost(savedPost)
                .build();
        studyMemberRepository.save(creatorAsMember);

        savedPost.getMembers().add(creatorAsMember);

        return new StudyPostDto.Response(savedPost);
    }

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
            Set<String> languageCodes = validateAndNormalizeLanguageCodes(req.getLanguages());
            if (languageCodes.isEmpty()) {
                throw new IllegalArgumentException("language는 하나 이상 선택해야 합니다.");
            }
            post.setLanguages(languageCodes);
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

    public void deleteStudyPost(Long id, Long currentUserId) {
        StudyPost post = studyPostRepository.findByIdWithUserAndProfileAndMembers(id)
                .orElseThrow(() -> new IllegalArgumentException("이미 삭제되었거나 존재하지 않는 글입니다. id=" + id));
        if (!post.getUser().getId().equals(currentUserId)) {
            throw new SecurityException("삭제 권한이 없습니다.");
        }
        studyPostRepository.deleteById(id);
    }

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
                .filter(Objects::nonNull)
                .filter(StudyPost.getAllowedCampuses()::contains)
                .distinct()
                .toList();
    }

    /**
     * 요청으로 들어온 language 값(code 또는 name 또는 한글 라벨)을 DB를 통해 code로 정규화한다.
     * - "ru" -> "ru"
     * - "Russian" -> "ru"
     * - "러시아어" -> "ru"
     */
    private String toLanguageCode(String raw) {
        if (raw == null) return null;
        String t = raw.trim();
        if (t.isEmpty()) return null;

        // 0) 한글 라벨이면 code로 먼저 변환
        String mapped = KO_LABEL_TO_CODE.get(t);
        if (mapped != null) {
            return mapped;
        }

        // 1) code로 검색 / 2) 영문 name으로 검색
        return languageRepository.findByCodeIgnoreCase(t)
                .map(Language::getCode)
                .or(() -> languageRepository.findByNameIgnoreCase(t).map(Language::getCode))
                .orElse(null);
    }

    private List<String> normalizeLanguageCodes(List<String> rawList) {
        if (rawList == null) return null;

        return rawList.stream()
                .map(this::toLanguageCode)
                .filter(Objects::nonNull)
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
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    /**
     * 생성/수정 요청의 languages를 DB 기준으로 검증하고 code Set으로 반환
     *  중복/공백으로 억울하게 터지지 않도록 안정화
     */
    private Set<String> validateAndNormalizeLanguageCodes(List<String> rawLangs) {
        if (rawLangs == null) return new HashSet<>();

        List<String> cleaned = rawLangs.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();

        List<String> normalized = cleaned.stream()
                .map(this::toLanguageCode)
                .toList();

        if (normalized.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("지원하지 않는 언어가 포함되어 있습니다.");
        }

        return new HashSet<>(normalized);
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
