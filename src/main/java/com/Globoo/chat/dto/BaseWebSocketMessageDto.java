package com.Globoo.chat.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type", visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = ChatMessageSendReqDto.class, name = "MESSAGE"), // 기존 메시지 보내기
        @JsonSubTypes.Type(value = ReadMessageReqDto.class, name = "READ"), // 읽음 알림 보내기
        @JsonSubTypes.Type(value = LeaveRoomReqDto.class, name = "LEAVE"), // 퇴장 알림 보내기
        @JsonSubTypes.Type(value = ChatMessageSendResDto.class, name = "MESSAGE_ACK"), // 메시지 수신 응답
        @JsonSubTypes.Type(value = ReadReceiptResDto.class, name = "READ_RECEIPT"), // 읽음 확인 수신 응답
        @JsonSubTypes.Type(value = LeaveRoomResDto.class, name = "LEAVE_NOTICE")
})
public abstract class BaseWebSocketMessageDto {
    @JsonProperty("type")
    private String type;
}