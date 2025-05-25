package org.ssafy.sid.withdraws.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.ssafy.sid.withdraws.dto.WithdrawSaveDTO;
import org.ssafy.sid.withdraws.model.Withdraws;
import org.ssafy.sid.withdraws.model.WithdrawsRepository;

@Service
@RequiredArgsConstructor
public class WithdrawsServiceImpl implements WithdrawsService{

	private final WithdrawsRepository withdrawsRepository;

	@Override
	@Transactional
	public void withdrawSub(WithdrawSaveDTO withdrawSaveDTO) {
		// 회원탈퇴 신청
		Withdraws withdraws = Withdraws.builder()
				.user(withdrawSaveDTO.getUser())
				.withdrawReason(withdrawSaveDTO.getReason())
				.build();

		withdrawsRepository.save(withdraws);
	}

	@Override
	@Transactional
	public void withdrawCancel(Withdraws withdraw) {
		withdrawsRepository.delete(withdraw);
	}
}
