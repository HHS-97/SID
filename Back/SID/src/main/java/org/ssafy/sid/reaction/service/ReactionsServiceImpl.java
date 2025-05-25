package org.ssafy.sid.reaction.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.ssafy.sid.comments.model.Comments;
import org.ssafy.sid.posts.model.Posts;
import org.ssafy.sid.profiles.model.Profiles;
import org.ssafy.sid.reaction.dto.CommentReactionsSaveDTO;
import org.ssafy.sid.reaction.dto.CommentReactionsUpdateDTO;
import org.ssafy.sid.reaction.dto.PostReactionsSaveDTO;
import org.ssafy.sid.reaction.dto.PostReactionsUpdateDTO;
import org.ssafy.sid.reaction.model.CommentReactions;
import org.ssafy.sid.reaction.model.CommentReactionsRepository;
import org.ssafy.sid.reaction.model.PostReactions;
import org.ssafy.sid.reaction.model.PostReactionsRepository;

@Service
@RequiredArgsConstructor
public class ReactionsServiceImpl implements ReactionsService {

	private final PostReactionsRepository postReactionsRepository;
	private final CommentReactionsRepository commentReactionsRepository;

	@Override
	@Transactional
	public PostReactions updatePostLikeReactions(PostReactions postReactions) {
		PostReactionsUpdateDTO postReactionsUpdateDTO = PostReactionsUpdateDTO.builder().positive(true).build();
		postReactions.update(postReactionsUpdateDTO);
		return postReactions;
	};

	@Override
	@Transactional
	public PostReactions createPostLikeReactions(Posts post, Profiles profile) {
		PostReactionsSaveDTO postReactionsSaveDTO = PostReactionsSaveDTO.builder()
				.post(post)
				.positive(true)
				.profile(profile)
				.build();

		PostReactions postReaction = postReactionsSaveDTO.toEntity();

		postReactionsRepository.save(postReaction);
		return postReaction;
	};

	@Override
	@Transactional
	public void deletePostReactions(PostReactions postReactions) {
		postReactionsRepository.delete(postReactions);
	};

	@Override
	@Transactional
	public PostReactions updatePostDislikeReactions(PostReactions postReactions) {
		PostReactionsUpdateDTO postReactionsUpdateDTO = PostReactionsUpdateDTO.builder().positive(false).build();
		postReactions.update(postReactionsUpdateDTO);
		return postReactions;
	};

	@Override
	@Transactional
	public PostReactions createPostDislikeReactions(Posts post, Profiles profile) {
		PostReactionsSaveDTO postReactionsSaveDTO = PostReactionsSaveDTO.builder()
				.post(post)
				.positive(false)
				.profile(profile)
				.build();

		PostReactions postReaction = postReactionsSaveDTO.toEntity();

		postReactionsRepository.save(postReaction);
		return postReaction;
	};

	@Override
	@Transactional
	public CommentReactions updateCommentLikeReactions(CommentReactions commentReactions) {
		CommentReactionsUpdateDTO commentReactionsUpdateDTO = CommentReactionsUpdateDTO.builder().positive(true).build();
		commentReactions.update(commentReactionsUpdateDTO);
		return commentReactions;
	};

	@Override
	@Transactional
	public CommentReactions createCommentLikeReactions(Comments comment, Profiles profile) {
		CommentReactionsSaveDTO commentReactionsSaveDTO = CommentReactionsSaveDTO.builder()
				.comment(comment)
				.positive(true)
				.profile(profile)
				.build();

		CommentReactions commentReaction = commentReactionsSaveDTO.toEntity();

		commentReactionsRepository.save(commentReaction);
		return commentReaction;
	};

	@Override
	@Transactional
	public void deleteCommentReactions(CommentReactions commentReactions) {
		commentReactionsRepository.delete(commentReactions);
	};

	@Override
	@Transactional
	public CommentReactions updateCommentDislikeReactions(CommentReactions commentReactions) {
		CommentReactionsUpdateDTO commentReactionsUpdateDTO = CommentReactionsUpdateDTO.builder().positive(false).build();
		commentReactions.update(commentReactionsUpdateDTO);
		return commentReactions;
	};

	@Override
	@Transactional
	public CommentReactions createCommentDislikeReactions(Comments comment, Profiles profile) {
		CommentReactionsSaveDTO commentReactionsSaveDTO = CommentReactionsSaveDTO.builder()
				.comment(comment)
				.positive(false)
				.profile(profile)
				.build();

		CommentReactions commentReaction = commentReactionsSaveDTO.toEntity();

		commentReactionsRepository.save(commentReaction);
		return commentReaction;
	};
}
