package com.sparta.hanghaebloglv3.post.service;

import com.sparta.hanghaebloglv3.comment.dto.CommentResponseDto;
import com.sparta.hanghaebloglv3.comment.entity.CommentEntity;
import com.sparta.hanghaebloglv3.comment.repository.CommentRepository;
import com.sparta.hanghaebloglv3.common.code.HanghaeBlogErrorCode;
import com.sparta.hanghaebloglv3.common.dto.ApiResult;
import com.sparta.hanghaebloglv3.common.exception.HanghaeBlogException;
import com.sparta.hanghaebloglv3.common.jwt.JwtUtil;
import com.sparta.hanghaebloglv3.post.dto.PostRequestDto;
import com.sparta.hanghaebloglv3.post.dto.PostResponseDto;
import com.sparta.hanghaebloglv3.post.entity.PostEntity;
import com.sparta.hanghaebloglv3.post.repository.PostRepository;
import com.sparta.hanghaebloglv3.user.entity.UserEntity;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * PostService.
 */
@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final JwtUtil jwtUtil;

    /**
     * Create Post.
     */
    @Transactional
    public PostResponseDto createPost(PostRequestDto requestDto, HttpServletRequest request) {

        // 토큰 체크 추가
        UserEntity userEntity = jwtUtil.checkToken(request);

        if (userEntity == null) {
            throw new HanghaeBlogException(HanghaeBlogErrorCode.NOT_FOUND_USER, null);
        }

        PostEntity postEntity = PostEntity.builder()
                .requestDto(requestDto)
                .userEntity(userEntity)
                .build();

        postRepository.save(postEntity);
        return new PostResponseDto(postEntity);
    }

    /**
     * Get all post.
     */
    @Transactional(readOnly = true) // readOnly true인 경우, JPA 영속성 컨텍스트에 갱신되지 않기 때문에, 조회 시 false로 설정하는 것보다 더 빠르게 조회가 가능함.
    public List<PostResponseDto> getPostList(HttpServletRequest request) {

        // 토큰 체크 추가
        UserEntity userEntity = jwtUtil.checkToken(request);

        if (userEntity == null) {
            throw new HanghaeBlogException(HanghaeBlogErrorCode.NOT_FOUND_USER, null);
        }


        // Post Db > List<PostEntity>
        List<PostEntity> postEntities = postRepository.findAllByOrderByModifiedAtDesc();

        // List<PostEntity> > List<PostResponseDto>
        List<PostResponseDto> postResponseDtoList = new ArrayList<>();
        postEntities.forEach(postEntity -> postResponseDtoList.add(new PostResponseDto(postEntity)));

        // for문으로 게시글 하나 씩 돌 때 마다 댓글전체도 돌려 postId로 매칭시켜 postResponseDto에 댓글 add 해주기.
        for (PostResponseDto postResponseDto : postResponseDtoList) {
            for (CommentResponseDto commentResponseDto : this.getCommentResponseDtoList()) {
                if (postResponseDto.getPostId()==commentResponseDto.getPostId()){
                    postResponseDto.addCommentResponseDtoList(commentResponseDto);
                }
            }
        }
        return postResponseDtoList;
    }

    /**
     * Get post by id.
     */
    @Transactional(readOnly = true)
    public PostResponseDto getPost(Long id, HttpServletRequest request) {
        // 토큰 체크 추가
        UserEntity userEntity = jwtUtil.checkToken(request);

        if (userEntity == null) {
            throw new HanghaeBlogException(HanghaeBlogErrorCode.NOT_FOUND_USER, null);
        }


        PostEntity postEntity = postRepository.findById(id)
                .orElseThrow(() -> new HanghaeBlogException(HanghaeBlogErrorCode.NOT_FOUND_POST, null));

        PostResponseDto postResponseDto = new PostResponseDto(postEntity);

        // for문으로 전체댓글 돌려 선택된 게시글과 postId로 매칭시켜 해당 게시글의 댓글까지 보이게 하기
        for (CommentResponseDto commentResponseDto : this.getCommentResponseDtoList()) {
            if (postResponseDto.getPostId()==commentResponseDto.getPostId()){
                postResponseDto.addCommentResponseDtoList(commentResponseDto);
            }
        }

        return postResponseDto;
    }

    /**
     * Update post by id.
     */
    @Transactional
    public PostResponseDto updatePost(Long id, PostRequestDto requestDto, HttpServletRequest request) {

        // 토큰 체크 추가
        UserEntity userEntity = jwtUtil.checkToken(request);
        if (userEntity == null) {
            throw new HanghaeBlogException(HanghaeBlogErrorCode.NOT_FOUND_USER, null);
        }

        PostEntity postEntity = postRepository.findById(id).orElseThrow(
                () -> new HanghaeBlogException(HanghaeBlogErrorCode.NOT_FOUND_POST, null)
        );

        // 토큰 사용자정보와 수정할 게시글 작성자가 다를경우
        if (!postEntity.getUserEntity().equals(userEntity)) {
            throw new HanghaeBlogException(HanghaeBlogErrorCode.UNAUTHORIZED_USER, null);
        }

        postEntity.update(requestDto);
        return new PostResponseDto(postEntity);
    }

    /**
     * Delete post.
     */
    @Transactional
    public ApiResult deletePost(Long id, HttpServletRequest request) {

        // 토큰 체크 추가
        UserEntity userEntity = jwtUtil.checkToken(request);
        if (userEntity == null) {
            throw new HanghaeBlogException(HanghaeBlogErrorCode.NOT_FOUND_USER, null);
        }

        PostEntity postEntity = postRepository.findById(id).orElseThrow(
                () -> new HanghaeBlogException(HanghaeBlogErrorCode.NOT_FOUND_POST, null)
        );

        // 토큰 사용자정보와 삭제할 게시글 작성자가 다를경우
        if (!postEntity.getUserEntity().equals(userEntity)) {
            throw new HanghaeBlogException(HanghaeBlogErrorCode.UNAUTHORIZED_USER, null);
        }
        postRepository.delete(postEntity);

        return new ApiResult("게시글 삭제 성공", HttpStatus.OK.value()); // 게시글 삭제 성공시 ApiResult Dto를 사용하여 성공메세지와 statusCode를 띄움
    }

    // 전체 댓글 ResponseDto List로 만들기
    private List<CommentResponseDto> getCommentResponseDtoList() {
        // Comment DB > entityList
        List<CommentEntity> commentEntityList = commentRepository.findAllByOrderByModifiedAtDesc();

        // entityList > List<CommentResponseDto>
        List<CommentResponseDto> commentResponseDtoList = new ArrayList<>();
        for (CommentEntity commentEntity : commentEntityList) {

            CommentResponseDto commentResponseDto = CommentResponseDto.builder()
                    .postId(commentEntity.getPostEntity().getPostId())
                    .commentId(commentEntity.getCommentId())
                    .content(commentEntity.getContent())
                    .username(commentEntity.getUserEntity().getUsername())
                    .createdAt(commentEntity.getCreatedAt())
                    .modifiedAt(commentEntity.getModifiedAt())
                    .build();
            commentResponseDtoList.add(commentResponseDto);
        }
        return commentResponseDtoList;
    }
}