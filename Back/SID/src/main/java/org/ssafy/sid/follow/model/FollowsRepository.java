package org.ssafy.sid.follow.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.ssafy.sid.profiles.model.Profiles;

import java.util.List;
import java.util.Optional;

public interface FollowsRepository extends JpaRepository<Follows, Long> {
	Optional<Follows> findByFollowerAndFollowing(Profiles follower, Profiles following);

	List<Follows> findByFollower(Profiles follower);

	List<Follows> findByFollowing(Profiles following);

	List<Follows> findAllByFollowingOrFollower(Profiles following, Profiles follower);

	boolean existsByFollowerAndFollowing(Profiles follower, Profiles following);

	Long countByFollowing(Profiles following);
	Long countByFollower(Profiles follower);

	void deleteAllByFollower(Profiles follower);
	void deleteAllByFollowing(Profiles following);
}
