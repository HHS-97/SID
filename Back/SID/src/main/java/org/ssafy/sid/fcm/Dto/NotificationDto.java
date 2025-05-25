package org.ssafy.sid.fcm.Dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDto {
	private Long notificationId;
    private String title;
    private String body;

    private boolean isRead;
    private String createdAt;
    private String type;
    private Long referenceId;
    private SenderDto sender;
    private ReceiverDto receiver;
    private String room;
 

    public void updateIsRead(boolean isRead) {
        this.isRead = isRead;
    }
}
