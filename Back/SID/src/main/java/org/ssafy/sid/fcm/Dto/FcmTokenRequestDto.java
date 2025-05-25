package org.ssafy.sid.fcm.Dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FcmTokenRequestDto {

	@JsonProperty(value = "fcmToken")
    private String fcmToken;

    
}
