package de.wise2324.puf.grpl.shrimps.shrimpsserver.repository;

import de.wise2324.puf.grpl.shrimps.shrimpsserver.model.Player;
import de.wise2324.puf.grpl.shrimps.shrimpsserver.model.PlayerStatistic;
import de.wise2324.puf.grpl.shrimps.shrimpsserver.model.Shrimp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ShrimpRepository extends JpaRepository<Shrimp, Long> {

    @Query(value=
            """
            SELECT *
            FROM shrimp s
            WHERE s.player_statistic_id = ?1
            AND s.hitpoints > 0
            ORDER BY last_active_on_round ASC, id ASC
            LIMIT 1
            """
            , nativeQuery = true)
    public Shrimp getNextShrimpFor(Long playerStatisticId);

}
