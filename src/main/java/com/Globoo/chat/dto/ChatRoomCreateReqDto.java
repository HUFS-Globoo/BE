package com.Globoo.chat.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ChatRoomCreateReqDto {
    private Long participantUserId; // 상대방(매칭된 사람2)의 ID
}

//pr 동일