package de.wise2324.puf.grpl.shrimps.shrimpsserver.util;

import de.wise2324.puf.grpl.shrimps.shrimpsserver.model.Game;
import lombok.Getter;
import lombok.Setter;
import de.wise2324.puf.grpl.shrimps.shrimpsserver.dto.ShrimpDTO;

@Getter
@Setter
public class HitEntry {
    private ShrimpDTO       targetHit;
    private GamePosition    positionHit;
    private int             damageDone;

    public HitEntry() {

        this.targetHit      = null;
        this.positionHit    = null;
        this.damageDone     = 0;
    }

    public HitEntry(ShrimpDTO target, GamePosition position, int damage) {
        this.targetHit      = target;
        this.positionHit    = position;
        this.damageDone     = damage;
    }
}
