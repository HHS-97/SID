package org.ssafy.sid.categories.dto;

import lombok.*;

@Getter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDeleteDTO {
	private long categoryId;
	private Boolean isAdmin;
}
