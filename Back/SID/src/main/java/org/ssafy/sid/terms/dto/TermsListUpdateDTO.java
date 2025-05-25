package org.ssafy.sid.terms.dto;

import lombok.*;

@Getter
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class TermsListUpdateDTO {
	private String title;
	private String description;
	private Boolean isRequired;
}
