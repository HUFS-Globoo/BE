package com.Globoo.chat.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class ChatSessionEndedEvent extends ApplicationEvent {

    private final Long roomId;

    public ChatSessionEndedEvent(Object source, Long roomId) {
        super(source);
        this.roomId = roomId;
    }
}
