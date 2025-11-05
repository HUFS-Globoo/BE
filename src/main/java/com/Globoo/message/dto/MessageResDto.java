// src/main/java/com/Globoo/message/dto/MessageResDto.java
package com.Globoo.message.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class MessageResDto {

    private Long id;
    private UserSummaryDto sender;
    private UserSummaryDto receiver;
    private String content;
    private Boolean isRead;
    private LocalDateTime createdAt;

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    public static class UserSummaryDto {
        private Long id;
        private String username;
        private String profileImageUrl;
    }

    // DmThread용 DTO
    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    public static class ThreadDto {
        private Long id;
        private UserSummaryDto user1;
        private UserSummaryDto user2;
        private LocalDateTime createdAt;
        private List<MessageResDto> messages;
    }
}
