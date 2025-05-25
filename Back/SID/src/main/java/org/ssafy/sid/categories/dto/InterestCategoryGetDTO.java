package org.ssafy.sid.categories.dto;

import lombok.*;
import org.ssafy.sid.categories.model.InterestCategories;

@Getter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class InterestCategoryGetDTO {
	private long interestCategoryId;
	private String interestCategoryName;

	public InterestCategoryGetDTO toDTO(InterestCategories interestCategory) {
		return InterestCategoryGetDTO.builder()
				.interestCategoryId(interestCategory.getCategory().getId())
				.interestCategoryName(interestCategory.getCategory().getTag())
				.build();
	}
}
