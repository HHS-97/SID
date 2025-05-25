package org.ssafy.sid.categories.dto;

import lombok.*;
import org.ssafy.sid.categories.model.Categories;
import org.ssafy.sid.categories.model.InterestCategories;
import org.ssafy.sid.profiles.model.Profiles;

@Getter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class InterestCategorySaveDTO {
	private Profiles profile;
	private Categories category;

	public InterestCategories toEntity() {
		return InterestCategories.builder()
				.profile(this.profile)
				.category(this.category)
				.build();
	}
}
