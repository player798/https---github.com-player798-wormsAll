package de.wise2324.puf.grpl.shrimps.shrimpsserver.repository;

import de.wise2324.puf.grpl.shrimps.shrimpsserver.model.Weapon;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WeaponRepository extends JpaRepository<Weapon, Long> {
}
