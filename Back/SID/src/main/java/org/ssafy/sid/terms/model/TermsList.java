package org.ssafy.sid.terms.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.ssafy.sid.terms.dto.TermsListDeleteDTO;
import org.ssafy.sid.terms.dto.TermsListUpdateDTO;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

@Entity
@Getter
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@SQLDelete(sql = "UPDATE terms_list SET is_deleted = true WHERE id = ?")
@Where(clause = "is_deleted = false")
public class TermsList {
	@Id
	@Column(columnDefinition = "INT(11) UNSIGNED")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@Column(length = 32)
	private String title;

	@Column(columnDefinition = "TEXT")
	private String description;

	@Column(columnDefinition = "TINYINT(1)")
	private Boolean isRequired;

	@CreatedDate
	@Column(updatable = false, nullable = false, columnDefinition="CHAR(19)")
	private String createdAt;

	@LastModifiedDate
	@Column(nullable = false, columnDefinition="CHAR(19)")
	private String updatedAt;

	@Column(columnDefinition = "TINYINT(1)")
	private Boolean isDeleted;

	@PrePersist
	public void onPrePersist(){
		this.createdAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
		this.updatedAt = this.createdAt;
	}
	@PreUpdate
	public void onPreUpdate(){
		this.updatedAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
	}

	public void update(TermsListUpdateDTO dto) {
		if (dto.getTitle() != null) {
			this.title = dto.getTitle();
		}
		if (dto.getIsRequired() != null) {
			this.isRequired = dto.getIsRequired();
		}
		if (dto.getDescription() != null) {
			this.description = dto.getDescription();
		}
	}

	public void delete(TermsListDeleteDTO dto) {
		if (Objects.equals(dto.getId(), this.id)) {
			this.isDeleted = true;
		}
	}
}
