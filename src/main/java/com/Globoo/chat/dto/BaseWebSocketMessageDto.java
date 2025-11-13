package com.Globoo.chat.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;

@Getter
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type",
        visible = false
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = ChatRoomJoinReqDto.class, name = "JOIN"),
        @JsonSubTypes.Type(value = ChatMessageSendReqDto.class, name = "MESSAGE"),
        @JsonSubTypes.Type(value = ReadMessageReqDto.class, name = "READ"),
        @JsonSubTypes.Type(value = LeaveRoomReqDto.class, name = "LEAVE"),
        @JsonSubTypes.Type(value = ChatMessageSendResDto.class, name = "MESSAGE_ACK"),
        @JsonSubTypes.Type(value = ReadReceiptResDto.class, name = "READ_RECEIPT"),
        @JsonSubTypes.Type(value = LeaveRoomResDto.class, name = "LEAVE_NOTICE")
})
public abstract class BaseWebSocketMessageDto {
}