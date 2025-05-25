package org.ssafy.sid.categories.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InterestCategoriesId implements Serializable {
	private Long profile;
	private Long category;
}
