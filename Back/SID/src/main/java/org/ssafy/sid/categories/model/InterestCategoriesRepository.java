package org.ssafy.sid.categories.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.ssafy.sid.profiles.model.Profiles;

import java.util.List;

public interface InterestCategoriesRepository extends JpaRepository<InterestCategories, Long> {
	List<InterestCategories> findByProfile(Profiles profile);
	void deleteAllByProfile(Profiles profile);
}
