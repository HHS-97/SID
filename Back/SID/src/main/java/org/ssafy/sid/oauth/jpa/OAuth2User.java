package org.ssafy.sid.oauth.jpa;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OAuth2User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	
    private String birthDate; // 'birth_date' 필드
    private LocalDateTime createdAt; // 'created_at' 필드
    private String email;
    private String gender;
    private String name;
    private String password; // 이 필드는 null 처리되도록 설정할 수 있음
    private String penalty;
    private String phone;
    private String provider;
    private String role; // 기본값을 설정할 수 있음
    private String status;
    private LocalDateTime updatedAt; // 'updated_at' 필드

}
