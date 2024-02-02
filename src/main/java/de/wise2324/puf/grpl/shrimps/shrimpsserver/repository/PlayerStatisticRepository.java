package de.wise2324.puf.grpl.shrimps.shrimpsserver.repository;

import de.wise2324.puf.grpl.shrimps.shrimpsserver.model.PlayerStatistic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface PlayerStatisticRepository extends JpaRepository<PlayerStatistic, Long> {

    @Query(value="SELECT * FROM player_statistic ps WHERE ps.player_id = ?1", nativeQuery = true)
    List<PlayerStatistic> getPlayedGamesFor(Long playerId);

    @Query(value=
        """
        SELECT COUNT(DISTINCT s.id) as liveShrimp
        FROM shrimp s
        WHERE s.player_statistic_id = ?1
        AND s.hitpoints > 0
        """
    , nativeQuery = true)
    public int getLiveShrimpCountFor(Long playerStatisticId);

    @Query(value=
            """
            SELECT p.id, p.username, 
                   COUNT(DISTINCT gs_game.id) AS GamesFinished,
                   COUNT(DISTINCT g_game.id) AS GamesStarted,
                   COUNT(DISTINCT CASE WHEN gs_game.status = 'FINISHED' AND gs_game.current_player_id = p.id THEN gs_game.id END) AS Wins,
                   COUNT(DISTINCT CASE WHEN gs_game.status = 'FINISHED' AND gs_game.current_player_id != p.id THEN gs_game.id END) AS Losses
            FROM player p
            LEFT JOIN game g_game ON p.id = g_game.current_player_id
            LEFT JOIN player_statistic ps_game ON p.id = ps_game.player_id
            LEFT JOIN game gs_game ON ps_game.game_id = gs_game.id
            GROUP BY p.id, p.username
            ORDER BY Wins DESC, Losses ASC, GamesFinished DESC, username DESC;
            """, nativeQuery = true)
    List<Object[]> getPlayerLeaderboard();

}

