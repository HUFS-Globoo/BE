package com.Globoo.study.service;

import com.Globoo.study.DTO.CommentPageRes;
import com.Globoo.study.DTO.CommentReq;
import com.Globoo.study.DTO.CommentRes;
import com.Globoo.study.domain.Comment;
import com.Globoo.study.domain.StudyPost;
import com.Globoo.study.repository.CommentRepository;
import com.Globoo.study.repository.StudyPostRepository;
import com.Globoo.user.domain.Profile;
import com.Globoo.user.domain.User;
import com.Globoo.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final StudyPostRepository studyPostRepository;
    private final UserRepository userRepository; // 사용자 조회를 위해 추가

    /** 댓글 작성 */
    @Transactional
    public CommentRes createComment(Long postId, CommentReq request, Long authorId) {
        // 1. 게시글 조회
        StudyPost studyPost = studyPostRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다. (ID: " + postId + ")"));

        // 2. 작성자(User) 조회
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다. (ID: " + authorId + ")"));

        // 3. 댓글 Entity 생성
        Comment newComment = Comment.builder()
                .content(request.getContent())
                .studyPost(studyPost)
                .user(author) // 작성자 정보 설정
                .build();

        // 4. 댓글 저장
        Comment savedComment = commentRepository.save(newComment);

        // 5. 응답 DTO로 변환하여 반환
        return convertToResponseDto(savedComment);
    }

    /** 댓글 단건 조회 */
    @Transactional(readOnly = true)
    public CommentRes getComment(Long postId, Long commentId) {
        // 1. 댓글 ID로 댓글 조회
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("요청한 리소스를 찾을 수 없습니다. (댓글 ID: " + commentId + ")"));

        // 2. 댓글이 요청한 postId에 속하는지 확인
        if (!comment.getStudyPost().getId().equals(postId)) {
            // postId가 일치하지 않으면, 해당 게시글에 없는 댓글이므로 404 처리
            throw new IllegalArgumentException("요청한 게시글에서 해당 댓글을 찾을 수 없습니다. (게시글 ID: " + postId + ", 댓글 ID: " + commentId + ")");
        }

        // 3. DTO로 변환하여 반환
        return convertToResponseDto(comment);
    }

    /** 댓글 목록 페이징 조회 */
    @Transactional(readOnly = true)
    public CommentPageRes getComments(Long postId, int page, int size, String sort, String order) {

        // 1. 게시글 존재 여부 확인
        if (!studyPostRepository.existsById(postId)) {
            throw new IllegalArgumentException("게시글을 찾을 수 없습니다. (ID: " + postId + ")");
        }

        // 2. 페이징 및 정렬 파라미터 검증 및 생성
        // 2-1. size 검증 (최대 100)
        int validatedSize = Math.min(Math.max(size, 1), 100);

        // 2-2. page 검증 (0 이상)
        int validatedPage = Math.max(page, 0);

        // 2-3. sort(정렬 필드) 검증 (createdAt, id만 허용)
        String sortProperty = (sort.equals("id") || sort.equals("createdAt")) ? sort : "createdAt";

        // 2-4. order(정렬 방향) 검증
        Sort.Direction direction = order.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        String orderDirection = direction.name().toLowerCase(); // 응답 DTO에 넣어줄 문자열

        // 2-5. Pageable 객체 생성
        Pageable pageable = PageRequest.of(validatedPage, validatedSize, Sort.by(direction, sortProperty));

        // 3. 데이터 조회 (Repository 호출)
        Page<Comment> commentPage = commentRepository.findByStudyPostId(postId, pageable);

        // 4. Page<Comment> -> Page<CommentRes>로 변환
        Page<CommentRes> commentResPage = commentPage.map(this::convertToResponseDto);

        // 5. 최종 응답 DTO(CommentPageRes) 생성
        return new CommentPageRes(commentResPage, sortProperty, orderDirection);
    }

    /** 댓글 수정 */
    @Transactional
    public CommentRes updateComment(Long postId, Long commentId, CommentReq request, Long currentUserId) {

        // 1. 댓글 조회 (404 Not Found)
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("요청한 리소스를 찾을 수 없습니다. (댓글 ID: " + commentId + ")"));

        // 2. 게시글 ID 검증 (404 Not Found)
        if (!comment.getStudyPost().getId().equals(postId)) {
            throw new IllegalArgumentException("요청한 게시글에서 해당 댓글을 찾을 수 없습니다. (게시글 ID: " + postId + ", 댓글 ID: " + commentId + ")");
        }

        // 3. 권한 검증 (403 Forbidden)
        if (!comment.getUser().getId().equals(currentUserId)) {
            throw new SecurityException("권한이 없는 사용자입니다. 이 요청을 처리할 권한이 없습니다.");
        }

        // 4. 댓글 내용 수정 (Dirty Checking)
        comment.updateContent(request.getContent());

        // 5. DTO로 변환하여 반환
        return convertToResponseDto(comment);
    }

    /** 댓글 삭제 */
    @Transactional
    public void deleteComment(Long postId, Long commentId, Long currentUserId) {

        // 1. 댓글 조회 (404 Not Found)
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("요청한 리소스를 찾을 수 없습니다. (댓글 ID: " + commentId + ")"));

        // 2. 게시글 ID 검증 (404 Not Found)
        if (!comment.getStudyPost().getId().equals(postId)) {
            throw new IllegalArgumentException("요청한 게시글에서 해당 댓글을 찾을 수 없습니다. (게시글 ID: " + postId + ", 댓글 ID: " + commentId + ")");
        }

        // 3. 권한 검증 (403 Forbidden)
        if (!comment.getUser().getId().equals(currentUserId)) {
            throw new SecurityException("댓글을 삭제할 권한이 없습니다.");
        }

        // 4. 댓글 삭제
        commentRepository.delete(comment);
    }


    /** Entity -> DTO 변환 (공통) */
    private CommentRes convertToResponseDto(Comment comment) {
        User user = comment.getUser();
        Profile profile = user.getProfile();

        String nickname;
        if (profile != null) {
            nickname = profile.getNickname();
        } else {
            // 프로필이 없는 경우, User의 username을 대신 사용
            nickname = user.getUsername();
        }

        CommentRes.AuthorDto authorDto = CommentRes.AuthorDto.builder()
                .id(user.getId())
                .nickname(nickname)
                .profileImageUrl(user.getProfileImageUrl())
                .build();

        return CommentRes.builder()
                .id(comment.getId())
                .postId(comment.getStudyPost().getId())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt()) // 3. updatedAt 필드 추가
                .author(authorDto)
                .build();
    }
}