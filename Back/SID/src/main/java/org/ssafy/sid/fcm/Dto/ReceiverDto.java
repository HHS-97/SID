package org.ssafy.sid.fcm.Dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReceiverDto {
    private Long receiverId;
    private String receiverNickname;
}