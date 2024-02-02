package de.wise2324.puf.grpl.shrimps.shrimpsserver.dto;

import de.wise2324.puf.grpl.shrimps.shrimpsserver.entity.GameState;
import de.wise2324.puf.grpl.shrimps.shrimpsserver.model.Game;
import de.wise2324.puf.grpl.shrimps.shrimpsserver.model.Player;
import de.wise2324.puf.grpl.shrimps.shrimpsserver.model.PlayerStatistic;
import de.wise2324.puf.grpl.shrimps.shrimpsserver.model.Weapon;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.Hibernate;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class GameDTO {

    private Long id;

    private Timestamp timestamp;

    private GameState gameState;
    private int roundToSuddenDeath;
    private int roundTimeLimit;

    private byte[] foregroundMask;

    private int levelForeground;
    private int levelBackground;

    private List<PlayerStatisticDTO> playerStatisticsList;

    private int currentRound;

    // Der Bequemlichkeit halber
    private Long currentPlayerId;
    private Long currentShrimpId;
    private int currentPlayerIdx;
    private int currentShrimpIdx;

    /* // ToDo: Munitionsbegrenzung einführen
    public List<WeaponDTO> defaultLoadout;
    */

    public GameDTO() {
        this.playerStatisticsList = new ArrayList<>();
    }

    public void addPlayerStatisticDTO(PlayerStatisticDTO playerStatisticDTO) {
        this.playerStatisticsList.add(playerStatisticDTO);
    }
}