package com.Globoo.study.service;

import com.Globoo.study.DTO.StudyPostDto;
import com.Globoo.study.domain.StudyPost;
import com.Globoo.study.repository.StudyPostRepository;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class StudyService {

    private final StudyPostRepository studyPostRepository;

    public StudyService(StudyPostRepository studyPostRepository) {
        this.studyPostRepository = studyPostRepository;
    }

    // =========================
    // 목록 조회 (필터 포함)
    // =========================
    @Transactional(readOnly = true)
    public List<StudyPostDto.Response> getStudyPosts(
            String status, String campus, String language, Integer minCapacity, Integer maxCapacity
    ) {
        final String normStatus = normalizeStatus(status);
        final String normCampus = normalizeCampus(campus);
        final String normLang   = normalizeLanguage(language);

        Integer minCap = minCapacity;
        Integer maxCap = maxCapacity;
        if (minCap != null && maxCap != null && minCap > maxCap) {
            int tmp = minCap; minCap = maxCap; maxCap = tmp;
        }
        final Integer fMinCap = minCap;
        final Integer fMaxCap = maxCap;

        Specification<StudyPost> spec = (root, _q, cb) -> cb.conjunction();

        if (normStatus != null)  spec = spec.and((r, _q, cb) -> cb.equal(r.get("status"), normStatus));
        if (normCampus != null)  spec = spec.and((r, _q, cb) -> cb.equal(r.get("campus"), normCampus));
        if (normLang != null)    spec = spec.and((r, _q, cb) -> cb.equal(r.get("language"), normLang));
        if (fMinCap != null)     spec = spec.and((r, _q, cb) -> cb.greaterThanOrEqualTo(r.get("capacity"), fMinCap));
        if (fMaxCap != null)     spec = spec.and((r, _q, cb) -> cb.lessThanOrEqualTo(r.get("capacity"), fMaxCap));

        return studyPostRepository.findAll(spec).stream()
                .map(StudyPostDto.Response::new)
                .toList();
    }

    // =========================
    // 단일 조회
    // =========================
    @Transactional(readOnly = true)
    public StudyPostDto.Response getStudyPost(Long id) {
        StudyPost post = studyPostRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 스터디 글을 찾을 수 없습니다. id=" + id));
        return new StudyPostDto.Response(post);
    }

    // =========================
    // 생성
    // =========================
    public StudyPostDto.Response createStudyPost(StudyPostDto.Request req) {
        StudyPost post = req.toEntity();

        // 상태/캠퍼스 정규화 및 검증
        post.setStatus(normalizeStatusOrDefault(req.getStatus(), "모집중"));
        post.setCampus(requireCampus(normalizeCampus(req.getCampus())));

        // 언어/정원 검증
        post.setLanguage(validateLanguage(req.getLanguage()));
        post.setCapacity(validateCapacity(req.getCapacity()));

        StudyPost saved = studyPostRepository.save(post);
        return new StudyPostDto.Response(saved);
    }

    // =========================
    // 부분 수정 (PATCH)
    // Swagger 기본값 "string"은 null과 동일하게 무시
    // =========================
    public StudyPostDto.Response updateStudyPost(Long id, StudyPostDto.Request req) {
        StudyPost post = studyPostRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 스터디 글을 찾을 수 없습니다. id=" + id));

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

        // 캠퍼스 (서울/글로벌)
        if (isPresentAndNotString(req.getCampus())) {
            String n = normalizeCampus(req.getCampus());
            if (n != null) post.setCampus(requireCampus(n));
        }

        // 언어 (허용 10개 중 하나)
        if (isPresentAndNotString(req.getLanguage())) {
            post.setLanguage(validateLanguage(req.getLanguage()));
        }

        // 최대 인원 (정수만, 1~6)
        if (req.getCapacity() != null) {
            post.setCapacity(validateCapacity(req.getCapacity()));
        }

        return new StudyPostDto.Response(studyPostRepository.save(post));
    }

    // =========================
    // 삭제
    // =========================
    public void deleteStudyPost(Long id) {
        if (!studyPostRepository.existsById(id)) {
            throw new IllegalArgumentException("이미 삭제되었거나 존재하지 않는 글입니다. id=" + id);
        }
        studyPostRepository.deleteById(id);
    }

    // =========================
    // 유틸 함수
    // =========================

    // Swagger 기본값 "string"을 무시 처리
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

    private String normalizeCampus(String raw) {
        if (raw == null) return null;
        String s = raw.trim().toLowerCase();
        if (s.isEmpty()) return null;
        if (s.equals("global") || s.equals("글로벌")) return "글로벌";
        if (s.equals("seoul") || s.equals("서울"))   return "서울";
        return raw.trim();
    }

    private String requireCampus(String campus) {
        if (!"서울".equals(campus) && !"글로벌".equals(campus)) {
            throw new IllegalArgumentException("캠퍼스는 '서울' 또는 '글로벌'만 가능합니다.");
        }
        return campus;
    }

    private String normalizeLanguage(String raw) {
        if (raw == null) return null;
        String s = raw.trim();
        return s.isEmpty() ? null : s;
    }

    private String validateLanguage(String lang) {
        if (lang == null || lang.trim().isEmpty()) {
            throw new IllegalArgumentException("language는 필수입니다. (허용 목록 중 하나)");
        }
        String t = lang.trim();
        if (!StudyPost.getAllowedLanguages().contains(t)) {
            throw new IllegalArgumentException("지원하지 않는 언어입니다. 가능한 언어: " + StudyPost.getAllowedLanguages());
        }
        return t;
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
