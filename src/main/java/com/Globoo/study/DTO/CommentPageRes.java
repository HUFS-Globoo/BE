package com.Globoo.study.DTO;

import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

/** 댓글 목록 페이징 조회의 응답 DTO */
@Getter
public class CommentPageRes {

    private final List<CommentRes> content; // 댓글 목록
    private final int page;                 // 현재 페이지 번호 (0부터 시작)
    private final int size;                 // 페이지 크기
    private final long totalElements;       // 전체 댓글 수
    private final int totalPages;           // 전체 페이지 수
    private final String sort;              // 정렬 기준 필드
    private final String order;             // 정렬 방향 (asc/desc)

    /** Spring Data의 Page 객체와 정렬 정보를 받아 커스텀 응답 DTO를 생성 */
    public CommentPageRes(Page<CommentRes> pageData, String sort, String order) {
        this.content = pageData.getContent();
        this.page = pageData.getNumber(); // Spring Page는 page number를 0부터 시작
        this.size = pageData.getSize();
        this.totalElements = pageData.getTotalElements();
        this.totalPages = pageData.getTotalPages();
        this.sort = sort;
        this.order = order;
    }
}