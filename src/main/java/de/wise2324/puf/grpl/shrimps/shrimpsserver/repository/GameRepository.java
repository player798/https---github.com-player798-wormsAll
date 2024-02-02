package de.wise2324.puf.grpl.shrimps.shrimpsserver.repository;

import de.wise2324.puf.grpl.shrimps.shrimpsserver.model.Game;
import de.wise2324.puf.grpl.shrimps.shrimpsserver.model.Player;
import de.wise2324.puf.grpl.shrimps.shrimpsserver.model.PlayerStatistic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface GameRepository extends JpaRepository<Game, Long> {
    @Query(value=
    """
            SELECT COUNT(s.player_statistic_id) as livePlayers
            FROM (
                SELECT s.player_statistic_id, COUNT(*) as liveShrimp
                FROM shrimp s
                WHERE s.player_statistic_id in
                (
                    SELECT ps.id
                    FROM player_statistic ps
                    WHERE ps.game_id = ?1
                )
                AND s.hitpoints > 0
                GROUP BY s.player_statistic_id) as s
    """, nativeQuery = true)
    public int getLivePlayersFor(Long gameId);

    @Query(value=
            """
            SELECT ps.*
            FROM game g
            LEFT JOIN player_statistic ps on ps.game_id = g.id
            LEFT JOIN shrimp s on s.player_statistic_id = ps.id
            WHERE g.id = ?1
            AND s.hitpoints > 0
            """
            , nativeQuery = true
    )
    public PlayerStatistic getWinnerPlayerStatisticFor(Long gameId);


}
