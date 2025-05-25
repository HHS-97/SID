package org.ssafy.sid.fcm.service;

import org.springframework.stereotype.Service;
import org.ssafy.sid.fcm.repository.NotificationRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService{
	
	private final NotificationRepository notificationRepo;
	
	
}
