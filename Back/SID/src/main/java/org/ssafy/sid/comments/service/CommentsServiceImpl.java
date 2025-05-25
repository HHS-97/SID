package org.ssafy.sid.comments.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.ssafy.sid.comments.dto.CommentsSaveDTO;
import org.ssafy.sid.comments.dto.CommentsUpdateDTO;
import org.ssafy.sid.comments.model.Comments;
import org.ssafy.sid.comments.model.CommentsRepository;
import org.ssafy.sid.posts.model.Posts;
import org.ssafy.sid.profiles.model.Profiles;
import org.ssafy.sid.reaction.model.CommentReactionsRepository;

@Service
@RequiredArgsConstructor
public class CommentsServiceImpl implements CommentsService {

	private final CommentsRepository commentsRepository;
	private final CommentReactionsRepository commentReactionsRepository;

	@Override
	@Transactional
	public Comments saveComment(Posts post, Profiles profile, CommentsSaveDTO commentsSaveDTO) {
		Comments comment = commentsSaveDTO.toEntity(profile, post);
		commentsRepository.save(comment);
		return comment;
	}

	@Override
	@Transactional
	public void updateComment(CommentsUpdateDTO commentsUpdateDTO, Comments comment) {
		comment.update(commentsUpdateDTO);
	}

	@Override
	@Transactional
	public void deleteComment(Comments comment) {
		commentReactionsRepository.deleteAllByComment(comment);
		commentsRepository.delete(comment);
	}
}
