package org.ssafy.sid.lastprofiles.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.ssafy.sid.users.model.Users;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LastProfileDeleteDTO {
	private Users user;
}
