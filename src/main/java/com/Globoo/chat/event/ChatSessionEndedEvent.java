package com.Globoo.chat.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class ChatSessionEndedEvent extends ApplicationEvent {
    private final Long userId;

    public ChatSessionEndedEvent(Object source, Long userId) {
        super(source);
        this.userId = userId;
    }
}