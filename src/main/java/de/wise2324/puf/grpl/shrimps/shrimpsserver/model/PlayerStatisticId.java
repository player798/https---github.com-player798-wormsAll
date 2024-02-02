package de.wise2324.puf.grpl.shrimps.shrimpsserver.model;

import java.io.Serializable;

public class PlayerStatisticId implements Serializable {
    private Long playerId;
    private Long gameId;

    public int hashCode() {
        return (int)(playerId + gameId);
    }

    public boolean equals(Object object) {
        if (object instanceof PlayerStatisticId) {
            PlayerStatisticId otherId = (PlayerStatisticId) object;
            return (otherId.playerId == this.playerId) && (otherId.gameId == this.gameId);
        }
        return false;
    }
}