package com.sparta.hanghaebloglv3.post.entity;

import com.sparta.hanghaebloglv3.comment.entity.CommentEntity;
import com.sparta.hanghaebloglv3.common.entity.Timestamped;
import com.sparta.hanghaebloglv3.heart.heartFeed.entity.HeartFeed;
import com.sparta.hanghaebloglv3.post.dto.PostRequestDto;
import com.sparta.hanghaebloglv3.user.entity.UserEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * PostEntity.
 */
@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "tb_post")
public class PostEntity extends Timestamped {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "post_id")
	private Long postId;

	@Column(name = "title", nullable = false)
	private String title;

	@Column(name = "content", nullable = false)
	private String content;

	@ManyToOne(fetch = FetchType.LAZY)
	// FetchType.LAZY는 연관 관계로 걸린 엔티티가 참조 되어야 하는 시점에 읽는 방법.
	// JPA N + 1 Problem을 방지하기 위한 가장 기초적인 옵션 값.
	@JoinColumn(name = "user_id")
	// 외래 키를 매핑할 때 사용하는 어노테이션, name = "매핑할 외래 키 컬럼명, referencedColumnName = 대상 테이블의 컬럼명
	// 해당 어노테이션을 생략해도 연관 관계가 걸려 있을 경우, 자동으로 외래 키를 탐색함.
	private UserEntity userEntity;

	@OneToMany(mappedBy = "postEntity", cascade = CascadeType.PERSIST, orphanRemoval = true)
	private List<CommentEntity> commentEntityList = new ArrayList<>();

	@OneToMany(mappedBy = "postEntity", cascade = CascadeType.PERSIST, orphanRemoval = true)
	private List<HeartFeed> heartFeedList = new ArrayList<>();

	/**
	 * Initializer using Builder.
	 */
	@Builder
	public PostEntity(PostRequestDto requestDto, UserEntity userEntity) {
		this.title = requestDto.getTitle();
		this.content = requestDto.getContent();
		this.userEntity = userEntity;
	}

	/**
	 * Update post.
	 */
	public void update(PostRequestDto requestDto) {
		this.title = requestDto.getTitle();
		this.content = requestDto.getContent();
	}
}