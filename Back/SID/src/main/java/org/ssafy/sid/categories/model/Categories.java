package org.ssafy.sid.categories.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.ssafy.sid.categories.dto.CategoryDTO;
import org.ssafy.sid.comments.dto.CommentsUpdateDTO;

@Entity
@Getter
@Builder
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Categories {
	@Id
	@Column(columnDefinition = "INT(11) UNSIGNED")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@Column(length = 16)
	private String tag;

	public void update(CategoryDTO dto) {
		if (dto.getTag() != null) {
			this.tag = dto.getTag();
		}
	}
}
