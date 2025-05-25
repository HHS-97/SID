package org.ssafy.sid.fcm.Dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SenderDto {
    private Long senderId;
    private String senderNickname;
    private String senderProfileImage;
}
