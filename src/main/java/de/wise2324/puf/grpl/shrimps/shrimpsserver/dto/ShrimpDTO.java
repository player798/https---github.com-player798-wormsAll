package de.wise2324.puf.grpl.shrimps.shrimpsserver.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ShrimpDTO {

    private Long id;
    private int hitpoints;

    private String name;
    private int x_position;

    private int y_position;

    /*
    private List<WeaponDTO> loadout;
    */

    private boolean isLookingRight;

    private int lastActiveOnRound;

    private Long parentPlayerStatisticId;
}