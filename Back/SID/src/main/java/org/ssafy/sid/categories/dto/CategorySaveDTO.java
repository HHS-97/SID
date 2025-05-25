package org.ssafy.sid.categories.dto;

import lombok.*;
import org.ssafy.sid.categories.model.Categories;

@Getter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class CategorySaveDTO {
	private String tag;
	private Boolean isAdmin;

	public Categories toEntity() {
		return Categories.builder().tag(tag).build();
	}
}
