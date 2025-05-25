package org.ssafy.sid.posts.model;

import jakarta.persistence.*;
import lombok.*;
import org.ssafy.sid.profiles.model.Profiles;

@Entity
@Getter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class PostImages {
	@Id
	@Column(columnDefinition = "INT(11) UNSIGNED")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "post_id", columnDefinition = "INT(11) UNSIGNED", nullable = false)
	private Posts post;

	@Column(length = 128)
	private String image;

	@Column(columnDefinition = "INT(11) UNSIGNED")
	private long imageOrder;

//	public void update(PostImageUpdateDTO dto) {
//		if (dto.getOrder() != null) {
//			this.order = dto.getOrder();
//		}
//	}
}
