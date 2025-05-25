package org.ssafy.sid.categories.model;

import jakarta.persistence.*;
import lombok.*;
import org.ssafy.sid.profiles.model.Profiles;

@Entity
@Getter
@Builder
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@IdClass(InterestCategoriesId.class)
public class InterestCategories {
	@Id
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "profile_id", columnDefinition = "INT(11) UNSIGNED", nullable = false)
	private Profiles profile;

	@Id
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "category_id", columnDefinition = "INT(11) UNSIGNED", nullable = false)
	private Categories category;
}
