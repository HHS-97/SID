package org.ssafy.sid.reaction.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.ssafy.sid.posts.model.Posts;
import org.ssafy.sid.profiles.model.Profiles;

import java.util.List;
import java.util.Optional;

public interface PostReactionsRepository extends JpaRepository<PostReactions, Long> {
	Optional<PostReactions> findByPostAndProfile(Posts post, Profiles profile);
	List<PostReactions> findByPositiveAndProfile(Boolean positive, Profiles profile);
	Long countByPostAndPositive(Posts post, Boolean positive);
	void deleteAllByProfile(Profiles profile);
	void deleteAllByPost(Posts post);
}
