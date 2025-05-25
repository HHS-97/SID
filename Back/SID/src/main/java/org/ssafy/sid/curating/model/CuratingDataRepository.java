package org.ssafy.sid.curating.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.ssafy.sid.posts.model.Posts;
import org.ssafy.sid.profiles.model.Profiles;

import java.util.List;
import java.util.Optional;

public interface CuratingDataRepository extends JpaRepository<CuratingData, Long> {
	List<CuratingData> findByProfileAndPostAndType(Profiles profile, Posts post, char type);
	List<CuratingData> findAllByPost(Posts post);
}
