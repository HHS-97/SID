package org.ssafy.sid.posts.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.ssafy.sid.posts.dto.PostImagesGetDTO;

import java.util.List;
import java.util.Optional;

public interface PostImagesRepository extends JpaRepository<PostImages, Long> {
	Optional<PostImages> findByPost(Posts post);
	Optional<PostImages> findByPostId(Long postId);
	long countByPost(Posts post);
}
