package de.wise2324.puf.grpl.shrimps.shrimpsserver.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * ToDo: Datenbankeinträge sollten hier benutzt werden
 * Abfeuern einer Waffe für zum beenden der Runde und einem Ready-Zyklus
 * vorerst Hardcoded:
 * Id  Waffe    Dmg    Verwendet Force?
 * 1   Pistole   5          Nein
 *
 *
 * angle wird in Grad angegeben 0° = Norden / Oben, 90° Recht 180° Unten
 * force ist ein Wert zwischen 0 und 100 [0..100]
 */
@Getter
@Setter
public class FireWeaponDTO {
    private Long id;
    private int angle;
    private int force;

    private ShrimpDTO shrimpDTO;
}
