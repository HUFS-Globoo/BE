package com.Globoo.chat.dto;

public record ChatMessagePayload(Long roomId, Long senderId, String content) {}
