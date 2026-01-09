package com.Globoo.chat.event;

import com.Globoo.matching.domain.MatchPair;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class ChatReadyEvent extends ApplicationEvent {
    private final MatchPair match;

    public ChatReadyEvent(Object source, MatchPair match) {
        super(source);
        this.match = match;
    }
}