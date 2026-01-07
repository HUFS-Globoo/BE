package com.Globoo.chat.event;

import com.Globoo.matching.domain.MatchPair;
import com.Globoo.profile.dto.ProfileCardRes;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class MatchFoundEvent extends ApplicationEvent {
    private final MatchPair match;
    private final ProfileCardRes profileA;
    private final ProfileCardRes profileB;

    public MatchFoundEvent(Object source, MatchPair match, ProfileCardRes profileA, ProfileCardRes profileB) {
        super(source);
        this.match = match;
        this.profileA = profileA;
        this.profileB = profileB;
    }
}