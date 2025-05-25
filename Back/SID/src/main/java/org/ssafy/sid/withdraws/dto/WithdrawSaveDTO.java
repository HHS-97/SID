package org.ssafy.sid.withdraws.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.ssafy.sid.users.model.Users;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WithdrawSaveDTO {
	private Users user;
	private String reason;
}
