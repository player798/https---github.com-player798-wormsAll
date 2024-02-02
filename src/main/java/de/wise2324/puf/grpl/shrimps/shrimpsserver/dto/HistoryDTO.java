package de.wise2324.puf.grpl.shrimps.shrimpsserver.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HistoryDTO {
    private PlayerDTO player;
    private int totalGamesStartet;
    private int totalGamesFinished;
    private int totalGamesWon;
    private int totalGamesLost;

    public HistoryDTO() {

    }
}
