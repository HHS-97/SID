package org.ssafy.sid.reaction.model;

import org.springframework.data.repository.CrudRepository;
import org.ssafy.sid.comments.model.Comments;
import org.ssafy.sid.posts.model.Posts;
import org.ssafy.sid.profiles.model.Profiles;

import java.util.Optional;

public interface CommentReactionsRepository extends CrudRepository<CommentReactions, Long> {
	Optional<CommentReactions> findByCommentAndProfile(Comments comment, Profiles profile);
	Long countByCommentAndPositive(Comments comment, Boolean positive);
	void deleteAllByProfile(Profiles profile);
	void deleteAllByComment(Comments comment);
}
