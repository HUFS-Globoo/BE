package com.Globoo.message.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MessageRequestDto {
    private Long partnerId;
    private String content;
}
