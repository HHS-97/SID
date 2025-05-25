package org.ssafy.sid.comments.model;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.ssafy.sid.posts.model.Posts;

import java.util.List;

public interface CommentsRepository extends JpaRepository<Comments, Long> {
	Long countByPostId(Long postId);
	Page<Comments> findByPost(Posts post, Pageable pageable);
	List<Comments> findAllByPost(Posts post);
}
