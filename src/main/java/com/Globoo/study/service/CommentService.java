package com.Globoo.study.service;

import com.Globoo.study.DTO.CommentPageRes;
import com.Globoo.study.DTO.CommentReq;
import com.Globoo.study.DTO.CommentRes;
import com.Globoo.study.DTO.MyCommentRes;
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

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final StudyPostRepository studyPostRepository;
    private final UserRepository userRepository;

    /** 댓글 작성 */
    @Transactional
    public CommentRes createComment(Long postId, CommentReq request, Long authorId) {
        StudyPost studyPost = studyPostRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다. (ID: " + postId + ")"));

        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다. (ID: " + authorId + ")"));

        Comment newComment = Comment.builder()
                .content(request.getContent())
                .studyPost(studyPost)
                .user(author)
                .build();

        Comment savedComment = commentRepository.save(newComment);

        return convertToResponseDto(savedComment);
    }

    /** 댓글 단건 조회 */
    @Transactional(readOnly = true)
    public CommentRes getComment(Long postId, Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("요청한 리소스를 찾을 수 없습니다. (댓글 ID: " + commentId + ")"));

        if (!comment.getStudyPost().getId().equals(postId)) {
            throw new IllegalArgumentException("요청한 게시글에서 해당 댓글을 찾을 수 없습니다. (게시글 ID: " + postId + ", 댓글 ID: " + commentId + ")");
        }

        return convertToResponseDto(comment);
    }

    /** 댓글 목록 페이징 조회 */
    @Transactional(readOnly = true)
    public CommentPageRes getComments(Long postId, int page, int size, String sort, String order) {

        if (!studyPostRepository.existsById(postId)) {
            throw new IllegalArgumentException("게시글을 찾을 수 없습니다. (ID: " + postId + ")");
        }

        int validatedSize = Math.min(Math.max(size, 1), 100);
        int validatedPage = Math.max(page, 0);

        String sortProperty = (sort.equals("id") || sort.equals("createdAt")) ? sort : "createdAt";

        Sort.Direction direction = order.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        String orderDirection = direction.name().toLowerCase();

        Pageable pageable = PageRequest.of(validatedPage, validatedSize, Sort.by(direction, sortProperty));

        //Repository에서 user/profile을 같이 로딩(EntityGraph 적용됨)
        Page<Comment> commentPage = commentRepository.findByStudyPostId(postId, pageable);

        Page<CommentRes> commentResPage = commentPage.map(this::convertToResponseDto);

        return new CommentPageRes(commentResPage, sortProperty, orderDirection);
    }

    /** 댓글 수정 */
    @Transactional
    public CommentRes updateComment(Long postId, Long commentId, CommentReq request, Long currentUserId) {

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("요청한 리소스를 찾을 수 없습니다. (댓글 ID: " + commentId + ")"));

        if (!comment.getStudyPost().getId().equals(postId)) {
            throw new IllegalArgumentException("요청한 게시글에서 해당 댓글을 찾을 수 없습니다. (게시글 ID: " + postId + ", 댓글 ID: " + commentId + ")");
        }

        if (!comment.getUser().getId().equals(currentUserId)) {
            throw new SecurityException("권한이 없는 사용자입니다. 이 요청을 처리할 권한이 없습니다.");
        }

        comment.updateContent(request.getContent());

        return convertToResponseDto(comment);
    }

    /** 댓글 삭제 */
    @Transactional
    public void deleteComment(Long postId, Long commentId, Long currentUserId) {

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("요청한 리소스를 찾을 수 없습니다. (댓글 ID: " + commentId + ")"));

        if (!comment.getStudyPost().getId().equals(postId)) {
            throw new IllegalArgumentException("요청한 게시글에서 해당 댓글을 찾을 수 없습니다. (게시글 ID: " + postId + ", 댓글 ID: " + commentId + ")");
        }

        if (!comment.getUser().getId().equals(currentUserId)) {
            throw new SecurityException("댓글을 삭제할 권한이 없습니다.");
        }

        commentRepository.delete(comment);
    }

    /**(기존) 마이페이지 - 내가 작성한 댓글 목록 (댓글만) */
    @Transactional(readOnly = true)
    public List<CommentRes> getMyComments(Long currentUserId) {
        List<Comment> comments = commentRepository.findAllByUserIdOrderByCreatedAtDesc(currentUserId);

        return comments.stream()
                .map(this::convertToResponseDto)
                .toList();
    }

    /**(추가) 마이페이지 - 내가 작성한 댓글 목록 (댓글 + 스터디 요약 + 게시글 작성자 프로필/국적 포함) */
    @Transactional(readOnly = true)
    public List<MyCommentRes> getMyCommentsForMyPage(Long currentUserId) {
        List<Comment> comments = commentRepository.findMyCommentsForMyPage(currentUserId);

        return comments.stream()
                .map(this::toMyCommentRes)
                .toList();
    }

    /** Entity -> 댓글 DTO 변환 (공통) */
    private CommentRes convertToResponseDto(Comment comment) {
        User user = comment.getUser();
        Profile profile = user.getProfile();

        String nickname = (profile != null && profile.getNickname() != null)
                ? profile.getNickname()
                : user.getUsername();

        String profileImageUrl = null;
        if (profile != null && profile.getProfileImage() != null) {
            profileImageUrl = profile.getProfileImage();
        } else if (user.getProfileImageUrl() != null) {
            profileImageUrl = user.getProfileImageUrl();
        }

        String country = null;
        if (profile != null && profile.getCountry() != null) {
            country = profile.getCountry();
        }

        CommentRes.AuthorDto authorDto = CommentRes.AuthorDto.builder()
                .id(user.getId())
                .nickname(nickname)
                .profileImageUrl(profileImageUrl)
                .country(country)
                .build();

        return CommentRes.builder()
                .id(comment.getId())
                .postId(comment.getStudyPost().getId())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .author(authorDto)
                .build();
    }

    /**(추가) Entity -> 마이페이지 전용 DTO 변환 */
    private MyCommentRes toMyCommentRes(Comment comment) {
        User me = comment.getUser();
        Profile myProfile = me.getProfile();

        StudyPost post = comment.getStudyPost();
        User postAuthor = post.getUser();
        Profile postAuthorProfile = (postAuthor != null) ? postAuthor.getProfile() : null;

        // 나(author)
        String myNickname = (myProfile != null && myProfile.getNickname() != null)
                ? myProfile.getNickname()
                : me.getUsername();

        String myProfileImage = (myProfile != null) ? myProfile.getProfileImage() : null;
        String myCountry = (myProfile != null) ? myProfile.getCountry() : null;

        MyCommentRes.AuthorDto authorDto = MyCommentRes.AuthorDto.builder()
                .id(me.getId())
                .nickname(myNickname)
                .profileImageUrl(myProfileImage)
                .country(myCountry)
                .build();

        // 게시글 작성자(author)
        MyCommentRes.PostAuthorDto postAuthorDto = null;
        if (postAuthor != null) {
            String paProfileImage = (postAuthorProfile != null) ? postAuthorProfile.getProfileImage() : null;
            String paCountry = (postAuthorProfile != null) ? postAuthorProfile.getCountry() : null;

            postAuthorDto = MyCommentRes.PostAuthorDto.builder()
                    .id(postAuthor.getId())
                    .profileImageUrl(paProfileImage)
                    .country(paCountry)
                    .build();
        }

        // 게시글 요약
        MyCommentRes.StudySummaryDto studyDto = MyCommentRes.StudySummaryDto.builder()
                .postId(post.getId())
                .title(post.getTitle())
                .status(post.getStatus())
                .currentParticipants(post.getMembers() == null ? 0 : post.getMembers().size())
                .capacity(post.getCapacity())
                .campuses(post.getCampuses())
                .languages(post.getLanguages())
                .author(postAuthorDto)
                .build();

        return MyCommentRes.builder()
                .commentId(comment.getId())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .author(authorDto)
                .study(studyDto)
                .build();
    }
}
