package org.ssafy.sid.withdraws.service;

import org.ssafy.sid.withdraws.dto.WithdrawDeleteDTO;
import org.ssafy.sid.withdraws.dto.WithdrawSaveDTO;
import org.ssafy.sid.withdraws.model.Withdraws;

public interface WithdrawsService {
	void withdrawSub(WithdrawSaveDTO withdrawSaveDTO);
	void withdrawCancel(Withdraws withdraw);
}
