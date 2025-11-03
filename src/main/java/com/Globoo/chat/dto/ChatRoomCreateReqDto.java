package com.Globoo.chat.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter; // [!!!] 1. import 추가

@Getter
@Setter // [!!!] 2. @Setter 추가
@NoArgsConstructor
public class ChatRoomCreateReqDto {

    private Long participantUserId;

}