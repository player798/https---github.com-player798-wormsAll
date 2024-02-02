package de.wise2324.puf.grpl.shrimps.shrimpsserver.dto;

import de.wise2324.puf.grpl.shrimps.shrimpsserver.model.Weapon;

public class WeaponDTO {

    public Long id;
    public String name;
    public int magazin;
    public int baseDamage;

    public static WeaponDTO getDTOFromObject(Weapon weapon) {
        WeaponDTO result    = new WeaponDTO();

        result.id           = weapon.getId();
        result.name         = weapon.getName();
        result.magazin      = weapon.getMagazin();
        result.baseDamage   = weapon.getBaseDamage();

        return result;
    }
}