package org.ssafy.sid.fcm.service;

import java.io.IOException;

import org.ssafy.sid.fcm.Dto.FcmSendDto;

public interface FcmService {
	
	boolean sendNotificationWithData(String token, String title, String body, String type, Long referenceId, String room);
	void saveOrUpdateFcmToken(Long userId, String token);
	
}
