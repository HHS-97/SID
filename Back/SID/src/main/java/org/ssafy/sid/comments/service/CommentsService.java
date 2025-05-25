package org.ssafy.sid.comments.service;

import org.ssafy.sid.comments.dto.CommentsSaveDTO;
import org.ssafy.sid.comments.dto.CommentsUpdateDTO;
import org.ssafy.sid.comments.model.Comments;
import org.ssafy.sid.posts.model.Posts;
import org.ssafy.sid.profiles.model.Profiles;

public interface CommentsService {
	Comments saveComment(Posts post, Profiles profile, CommentsSaveDTO commentsSaveDTO);
	void updateComment(CommentsUpdateDTO commentsUpdateDTO, Comments comment);
	void deleteComment(Comments comment);
}
