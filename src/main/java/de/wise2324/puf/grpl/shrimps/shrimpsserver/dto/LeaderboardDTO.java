package de.wise2324.puf.grpl.shrimps.shrimpsserver.dto;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LeaderboardDTO {
    private long playerId;
    private String username;
    private int gamesFinished;
    private int gamesStarted;
    private int wins;
    private int losses;

    public LeaderboardDTO() {

    }
}
