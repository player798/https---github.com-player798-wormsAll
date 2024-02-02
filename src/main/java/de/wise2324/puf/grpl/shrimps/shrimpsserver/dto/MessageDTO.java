package de.wise2324.puf.grpl.shrimps.shrimpsserver.dto;

import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
public class MessageDTO {

    private Long id;
    private PlayerDTO sender;
    private PlayerDTO receiver;
    private String text;
    private GameDTO gameInvite;
    private Timestamp timestamp;

    public MessageDTO() {
        this.sender         = null;
        this.receiver       = null;
        this.timestamp      = new Timestamp(System.currentTimeMillis());
        this.gameInvite     = null;
    }
}
