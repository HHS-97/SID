package org.ssafy.sid.chat.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ChatMessageRequestDTO {
	private String receiver;    // 메세지 수신자
}
