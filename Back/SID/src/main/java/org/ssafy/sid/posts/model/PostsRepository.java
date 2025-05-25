package org.ssafy.sid.posts.model;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.ssafy.sid.profiles.model.Profiles;

import java.util.List;
import java.util.Optional;

public interface PostsRepository extends JpaRepository<Posts, Long> {
	Optional<Posts> findById(long id);
	Long countByProfile(Profiles profile);
	void deleteAllByProfile(Profiles profile);
	List<Posts> findAllByProfile(Profiles profile);
	Page<Posts> findByProfile(Profiles profile, Pageable pageable);
	// native query를 이용해 posts 테이블의 모든 게시글과 함께 각 행에 랜덤 숫자(0~99)를 추가하여 반환
	@Query(value = "SELECT p.*, RAND() as random_number " +
			"FROM posts p " +
			"WHERE p.id NOT IN (:postIds) " +
			"ORDER BY random_number DESC",
			countQuery = "SELECT COUNT(*) FROM posts p " +
					"WHERE p.id NOT IN (:postIds)",
			nativeQuery = true)
	Page<Posts> findAllPostsWithRandomNumber(Pageable pageable, @Param("postIds") List<Long> postIds);


	Page<Posts> findByProfileIn(List<Profiles> profiles, Pageable pageable);
	Page<Posts> findByContentContainingIgnoreCase(String keyword, Pageable pageable);

	@Query(value = "SELECT p.id " +
			"FROM posts p " +
			"RIGHT OUTER JOIN ( " +
			"    SELECT post_id, COUNT(*) AS 좋아요 " +
			"    FROM post_reactions " +
			"    WHERE STR_TO_DATE(updated_at, '%Y-%m-%d %H:%i:%s') >= NOW() - INTERVAL 7 DAY " +
			"      AND post_id NOT IN (:excludedIds) " +
			"      AND positive = 1 " +
			"    GROUP BY post_id " +
			"    ORDER BY COUNT(*) DESC " +
			"    LIMIT 20 " +
			") AS popular_ids " +
			"ON p.id = popular_ids.post_id",
			nativeQuery = true)
	List<Long> findPopularPostIds(@Param("excludedIds") List<Long> excludedIds);

}
