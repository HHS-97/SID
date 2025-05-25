package org.ssafy.sid.categories.dto;

import lombok.*;
import org.ssafy.sid.categories.model.Categories;

@Getter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDTO {
	private long categoryId;
	private String tag;
	private Boolean isAdmin;

	public Categories toEntity() {
		return Categories.builder().id(categoryId).tag(tag).build();
	}
}
