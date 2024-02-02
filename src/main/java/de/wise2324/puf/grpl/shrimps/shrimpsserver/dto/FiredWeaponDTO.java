package de.wise2324.puf.grpl.shrimps.shrimpsserver.dto;

import de.wise2324.puf.grpl.shrimps.shrimpsserver.util.GamePosition;
import de.wise2324.puf.grpl.shrimps.shrimpsserver.util.HitEntry;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class FiredWeaponDTO {
    private List<GamePosition>  projectilePath;
    private List<HitEntry>      targetsHit;

    public FiredWeaponDTO() {
        this.setProjectilePath(new ArrayList<>());
        this.setTargetsHit(new ArrayList<>());
    }
}
