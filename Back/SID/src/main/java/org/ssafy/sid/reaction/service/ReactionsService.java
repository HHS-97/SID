package org.ssafy.sid.reaction.service;

import org.ssafy.sid.comments.model.Comments;
import org.ssafy.sid.posts.model.Posts;
import org.ssafy.sid.profiles.model.Profiles;
import org.ssafy.sid.reaction.model.CommentReactions;
import org.ssafy.sid.reaction.model.PostReactions;

public interface ReactionsService {
	// 게시글
	PostReactions updatePostLikeReactions(PostReactions postReactions);
	PostReactions createPostLikeReactions(Posts post, Profiles profile);
	void deletePostReactions(PostReactions postReactions);
	PostReactions updatePostDislikeReactions(PostReactions postReactions);
	PostReactions createPostDislikeReactions(Posts post, Profiles profile);
	// 댓글
	CommentReactions updateCommentLikeReactions(CommentReactions postReactions);
	CommentReactions createCommentLikeReactions(Comments comment, Profiles profile);
	void deleteCommentReactions(CommentReactions postReactions);
	CommentReactions updateCommentDislikeReactions(CommentReactions postReactions);
	CommentReactions createCommentDislikeReactions(Comments comment, Profiles profile);
}
